@echo off
set CLASSPATHBACKUP=%CLASSPATH%
set CLASSPATH=d:\eclipse\itp\plugins\com.ibm.cpp.core;%CLASSPATH%
java com.ibm.cpp.core.miners.parser.cpp.CommandLine %1
set CLASSPATH=%CLASSPATHBACKUP%
