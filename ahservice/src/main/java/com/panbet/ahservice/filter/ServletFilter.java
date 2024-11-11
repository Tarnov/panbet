package com.panbet.ahservice.filter;


import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.panbet.kingsdream.v1.api.AA;
import com.panbet.kingsdream.v1.api.KD;
import com.panbet.kingsdream.v1.api.MetricPublisher;
import com.panbet.kingsdream.v1.api.RunnableAction;


public class ServletFilter implements Filter
{
    private final MetricPublisher mp = KD.mp();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {

    }


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException
    {
        String metricName = ((HttpServletRequest) request).getRequestURI();
        mp.execute(AA.name(metricName).group("http"), (RunnableAction) ac -> chain.doFilter(request, response));
    }


    @Override
    public void destroy()
    {

    }
}
