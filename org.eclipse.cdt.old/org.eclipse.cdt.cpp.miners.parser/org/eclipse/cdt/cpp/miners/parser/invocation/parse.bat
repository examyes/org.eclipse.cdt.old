#Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
@echo off
set CLASSPATHBACKUP=%CLASSPATH%
set CLASSPATH=e:\eclipse\plugins\com.ibm.cpp.miners;%CLASSPATH%
java com.ibm.cpp.miners.parser.invocation.CommandLine %1
set CLASSPATH=%CLASSPATHBACKUP%
