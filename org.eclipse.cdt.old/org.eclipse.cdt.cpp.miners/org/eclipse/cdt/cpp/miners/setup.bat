set ECLIPSEDIR=e:\eclipse\eclipse
set EXTRA=%ECLIPSEDIR%\plugins\com.ibm.dstore.extra
set CORE=%ECLIPSEDIR%\plugins\com.ibm.dstore.core
set MINERS=%ECLIPSEDIR%\plugins\com.ibm.dstore.miners
set CPP_MINERS=%ECLIPSEDIR%\plugins\com.ibm.cpp.miners
set A_PLUGIN_PATH=%CORE%

set CLASSPATH=%CLASSPATH%;%ECLIPSEDIR%\plugins\org.eclipse.core.runtime\runtime.jar;%ECLIPSEDIR%\plugins\org.eclipse.core.boot\boot.jar;%ECLIPSEDIR%\plugins\org.eclipse.core.resources\resources.jar;%ECLIPSEDIR%\plugins\org.eclipse.ui\workbench.jar;%ECLIPSEDIR%\plugins\org.eclipse.swt\swt.jar;%ECLIPSEDIR%\startup.jar;%ECLIPSEDIR%\plugins\com.ibm.lpex\eclpiselpex.jar;%CORE%;%EXTRA%;%MINERS%;%CPP_MINERS%;.;
set PATH=%PATH%;%ECLIPSEDIR%\..\jre\bin;%ECLIPSEDIR%;	

