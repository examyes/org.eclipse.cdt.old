set ECLIPSEDIR=e:\eclipse\eclipse
set CORE=%ECLIPSEDIR%\plugins\com.ibm.dstore.core
set EXTRA=%ECLIPSEDIR%\plugins\com.ibm.dstore.extra
set A_PLUGIN_PATH=%ECLIPSEDIR%
set MINERS=%ECLIPSEDIR%\plugins\com.ibm.dstore.miners

set CLASSPATH=%CLASSPATH%;%ECLIPSEDIR%\plugins\org.eclipse.core.runtime\runtime.jar;%ECLIPSEDIR%\plugins\org.eclipse.core.boot\boot.jar;%ECLIPSEDIR%\plugins\org.eclipse.core.resources\resources.jar;%ECLIPSEDIR%\plugins\org.eclipse.ui\workbench.jar;%ECLIPSEDIR%\plugins\org.eclipse.swt\swt.jar;%ECLIPSEDIR%\startup.jar;%ECLIPSEDIR%\plugins\com.ibm.lpex\eclpiselpex.jar;%CORE%;%EXTRA%;.;%MINERS%

set PATH=%PATH%;%ECLIPSEDIR%\..\jre\bin;%ECLIPSEDIR%;	

