package com.panbet.cyclist

import org.jgrapht.Graph
import org.jgrapht.GraphPath
import org.jgrapht.alg.cycle.CycleDetector
import org.jgrapht.alg.shortestpath.DijkstraShortestPath
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge
import org.springframework.beans.factory.config.BeanDefinitionHolder
import org.springframework.beans.factory.config.BeanReference
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.context.support.AbstractApplicationContext
import org.springframework.util.LinkedMultiValueMap

/**
 * Информация о бине
 */
data class BeanMeta(
    /**
     *  Имя бина
     */
    val name: String,

    /**
     * Тип бина
     */
    val type: String,
    /**
     * Кол-во циркулярных зависимостей
     */
    val numCycles: Int,
    /**
     * Список бинов, которые участвуют в циркулярных зависимостях
     */
    val vertices: List<String>
)

data class BeanWithConstructorInjectedBeansFromCycle(
    /**
     * Имя бина
     */
    val name: String,
    /**
     * Бины, которые инжектятся в конструктор данного бина
     */
    val constructorInjectionArgs: Set<String>,
    /**
     * Количество бинов среди циркулярных зависимостей, которые используют конструкторный инжект зависимостей
     */
    val constructorInjectionArgsCount: Int = constructorInjectionArgs.size
) {
    fun constructorInjectionArgsString() = constructorInjectionArgs.joinToString()
}

/**
 * Информация о циркулярных зависимостях бина
 */
data class BeanLoop(
    /**
     * Имя бина
     */
    val name: String,
    /**
     * Путь от искомого бина до текущего
     */
    val targetBeanToIntermediatePath: GraphPath<String, DefaultEdge>,
    /**
     * Путь текущего бина до искомого
     */
    val intermediateToTargetBeanPath: GraphPath<String, DefaultEdge>,
    /**
     * Кол-во циклов, в котором участвует бин
     */
    var degree: Int = 0,

    /**
     * Бины которые содержатся в цикле и инжектятся в данный бин через конструктор
     */
    val constructorInjectionArgs: Set<String> = setOf()
) {
    override fun toString(): String {
        val leadingPath = targetBeanToIntermediatePath.vertexList
                .toMutableList()
                .dropLast(1)
                .joinToString(" -> ") { it.take(it.indexOf(":")) }
        val outPath = intermediateToTargetBeanPath.vertexList.joinToString(" -> ") { it.take(it.indexOf(":")) }
        return "$leadingPath -> $outPath"
    }

    fun constructorInjections(): String = constructorInjectionArgs.joinToString { "$simpleName -> $it" }

    fun contains(beanName: String): Boolean {
        return targetBeanToIntermediatePath.vertexList.contains(beanName) ||
            intermediateToTargetBeanPath.vertexList.contains(beanName)
    }

    val simpleName: String
        get() = name.take(name.indexOf(":"))
}

/**
 * Велосипед для анализа графа зависимостей бинов
 * @author Nurlan Turdaliev
 */
class CircularDependencyCyclist(private val context: AbstractApplicationContext) {
    private fun beanClassName(name: String): String {
        try {
            return context.beanFactory.getBeanDefinition(name).beanClassName ?: ""
        } catch (e: Exception) {
            return ""
        }
    }

    private val graph: Graph<String, DefaultEdge> by lazy {
        val graph = DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge::class.java)

        val bf: ConfigurableListableBeanFactory = context.beanFactory
        for (name in context.beanDefinitionNames) {
            val nameWithType = name + ":" + beanClassName(name)
            graph.addVertex(nameWithType)

            for (dep in bf.getDependenciesForBean(name)) {
                val depNameWithType = dep + ":" + beanClassName(dep)
                graph.addVertex(depNameWithType)
                graph.addEdge(nameWithType, depNameWithType)
            }
        }
        graph
    }

    private val beansUsedAsConstructorDependencies by lazy {
        val map = LinkedMultiValueMap<String, String>()
        val bf = context.beanFactory
        for (beanName in context.beanDefinitionNames) {
            val beanDefinition = bf.getBeanDefinition(beanName)
            for (arg in beanDefinition.constructorArgumentValues.genericArgumentValues) {
                val value = arg.value
                val argNames = if (value == null) emptyList()
                else if (arg.name != null) listOf(arg?.name + ":" + arg.type)
                else getFromValue(value)

                if (argNames.isNotEmpty()) {
                    map.addAll(beanName, argNames)
                }
            }
        }
        map
    }

    private fun getFromValue(value: Any): List<String> {
        if (value is BeanReference) {
            return listOf(value.beanName + ":" + beanClassName(value.beanName))
        }
        if (value is BeanDefinitionHolder) return listOf(value.beanName + ":" + value.beanDefinition.beanClassName)
        if (value is Collection<*>) return value.flatMap { getFromValue(it ?: return@flatMap emptyList()) }
        return emptyList()
    }

    fun beanWithConstructorInjectedBeansFromCycle(): List<BeanWithConstructorInjectedBeansFromCycle> {
        val cycleDetector = CycleDetector(graph)
        val allCycledBeans = graph.vertexSet().filter { cycleDetector.findCyclesContainingVertex(it).isNotEmpty() }
            .mapTo(HashSet()) { it }
        return graph.vertexSet().mapNotNull { name ->
            val constructorInjectionArgs = findConstructorInjectionArgs(name, allCycledBeans)
            if (constructorInjectionArgs.isEmpty()) return@mapNotNull null
            BeanWithConstructorInjectedBeansFromCycle(name, constructorInjectionArgs)
        }.sortedByDescending { it.constructorInjectionArgsCount }
    }

    fun beanCycleVertexList(): List<BeanMeta> {
        val cycleDetector = CycleDetector(graph)
        val sortedByDescending = graph.vertexSet()
            .map { it to cycleDetector.findCyclesContainingVertex(it) }
            .map {
                val parts = it.first.split(":")

                BeanMeta(
                    parts.first(),
                    parts.last(),
                    it.second.size,
                    it.second.toList()
                )
            }
            .sortedByDescending { it.numCycles }
        return sortedByDescending
    }

    fun beanCyclePaths(bean: String): List<BeanLoop> {
        val beanLoops = beanLoops(graph, CycleDetector(graph), bean)

        for (beanLoop in beanLoops) {
            var count = 1
            for (otherLoop in beanLoops) {
                if (beanLoop == otherLoop) {
                    continue
                }
                if (otherLoop.contains(beanLoop.name)) {
                    count++
                }
            }
            beanLoop.degree = count
        }

        return beanLoops.sortedByDescending { it.degree }
    }

    private fun beanLoops(
        graph: Graph<String, DefaultEdge>,
        cycleDetector: CycleDetector<String, DefaultEdge>,
        targetBean: String
    ): List<BeanLoop> {
        val loops = mutableListOf<BeanLoop>()
        val shortestPath = DijkstraShortestPath(graph)
        for (vertex in cycleDetector.findCyclesContainingVertex(targetBean)) {
            val path = shortestPath.getPath(vertex, targetBean)
            if (path.edgeList.isNotEmpty()) {
                val prevPath = shortestPath.getPath(targetBean, vertex)
                val constructorInjectionArgs =
                    findConstructorInjectionArgs(vertex, (prevPath.vertexList + path.vertexList).toSet())
                loops.add(BeanLoop(vertex, prevPath, path, 0, constructorInjectionArgs))
            }
        }
        return loops
    }

    private fun findConstructorInjectionArgs(bean: String, allBeans: Set<String>): Set<String> {
        return allBeans.mapNotNullTo(HashSet()) { next ->
            if (beansUsedAsConstructorDependencies[bean]?.contains(next) == true) {
                next
            } else {
                null
            }
        }
    }
}
