set ECLIPSEDIR=e:\eclipse\eclipse
set CORE=%ECLIPSEDIR%\plugins\com.ibm.dstore.core
set EXTRA=%ECLIPSEDIR%\plugins\com.ibm.dstore.extra
set UI=%ECLIPSEDIR%\plugins\com.ibm.dstore.ui
set MINERS=%ECLIPSEDIR%\plugins\com.ibm.dstore.miners
set A_PLUGIN_PATH=%CORE%

set CLASSPATH=%CLASSPATH%;%ECLIPSEDIR%\plugins\org.eclipse.core.runtime\runtime.jar;%ECLIPSEDIR%\plugins\org.eclipse.core.boot\boot.jar;%ECLIPSEDIR%\plugins\org.eclipse.core.resources\resources.jar;%ECLIPSEDIR%\plugins\org.eclipse.ui\workbench.jar;%ECLIPSEDIR%\plugins\org.eclipse.swt\swt.jar;%ECLIPSEDIR%\startup.jar;%ECLIPSEDIR%\plugins\com.ibm.lpex\eclpiselpex.jar;%UI%;%CORE%;%EXTRA%;%MINERS%;.;
set PATH=%PATH%;%ECLIPSEDIR%\..\jre\bin;%ECLIPSEDIR%;	

