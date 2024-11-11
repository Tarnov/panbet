#!/bin/bash

mvn clean package -PupdateEjb && mvn clean package -f ./panbetGUI-parent/pom.xml