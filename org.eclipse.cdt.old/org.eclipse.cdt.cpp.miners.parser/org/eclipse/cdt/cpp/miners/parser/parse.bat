@echo off
set CLASSPATHBACKUP=%CLASSPATH%
set CLASSPATH=e:\eclipse\plugins\org.eclipse.cdt.cpp.miners.parser;%CLASSPATH%
java org.eclipse.cdt.cpp.miners.parser.invocation.CommandLine %1
set CLASSPATH=%CLASSPATHBACKUP%
