@echo off
set CLASSPATHBACKUP=%CLASSPATH%
set CLASSPATH=e:\eclipse\plugins\com.ibm.cpp.miners.parser;%CLASSPATH%
java com.ibm.cpp.miners.parser.invocation.CommandLine %1
set CLASSPATH=%CLASSPATHBACKUP%
