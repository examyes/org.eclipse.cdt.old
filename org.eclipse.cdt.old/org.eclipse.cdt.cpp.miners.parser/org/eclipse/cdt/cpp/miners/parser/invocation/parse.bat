#Copyright (C) 2000, 2001, 2002 International Business Machines Corporation and others. All Rights Reserved.  
@echo off
set CLASSPATHBACKUP=%CLASSPATH%
set CLASSPATH=e:\eclipse\plugins\org.eclipse.cdt.cpp.miners;%CLASSPATH%
java org.eclipse.cdt.cpp.miners.parser.invocation.CommandLine %1
set CLASSPATH=%CLASSPATHBACKUP%
