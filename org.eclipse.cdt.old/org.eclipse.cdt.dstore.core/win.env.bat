set ECLIPSEDIR=e:\eclipse\eclipse
set CORE=%ECLIPSEDIR%\plugins\com.ibm.dstore.core
set MINERS=%ECLIPSEDIR%\plugins\com.ibm.dstore.miners
set EXTRA=%ECLIPSEDIR%\plugins\com.ibm.dstore.extra\dstore_extra_server.jar
set CPP_MINERS=%ECLIPSEDIR%\plugins\com.ibm.cpp.miners
set PARSER_MINER=%ECLIPSEDIR%\plugins\com.ibm.cpp.miners.parser

set A_PLUGIN_PATH=%ECLIPSEDIR%\plugins\
set CLASSPATH=%CORE%;%MINERS%;%CPP_MINERS%;%PARSER_MINER%;%EXTRA%;%CLASSPATH%
