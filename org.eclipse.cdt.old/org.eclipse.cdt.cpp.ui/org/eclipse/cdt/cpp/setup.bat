set ECLIPSEDIR=e:\eclipse

set DSCORE=%ECLIPSEDIR%\plugins\com.ibm.dstore.core
set DSUI=%ECLIPSEDIR%\plugins\com.ibm.dstore.ui
set DSMINERS=%ECLIPSEDIR%\plugins\com.ibm.dstore.miners
set DSEXTRA=%ECLIPSEDIR%\plugins\com.ibm.dstore.extra

set DEBUGCORE=%ECLIPSEDIR%\plugins\org.eclipse.debug.core\dtcore.jar
set DEBUGUI=%ECLIPSEDIR%\plugins\org.eclipse.debug.ui\dtui.jar

set CPP=%ECLIPSEDIR%\plugins\com.ibm.cpp.ui
set HOSTS=%ECLIPSEDIR%\plugins\com.ibm.dstore.hosts
set XML=%ECLIPSEDIR%\plugins\com.ibm.xml4j\xml4j.jar

set VCM=%ECLIPSEDIR%\plugins\org.eclipse.vcm.core\vcm.jar
set VCMUI=%ECLIPSEDIR%\plugins\org.eclipse.vcm.ui\vcmui.jar
set TM=%ECLIPSEDIR%\plugins\org.eclipse.core.target\targetmanagement.jar
set SEARCH=%ECLIPSEDIR%\plugins\org.eclipse.search\search.jar
set HELP=%ECLIPSEDIR%\plugins\org.eclipse.help\help.jar

set A_PLUGIN_PATH=%DSCORE%

set SWT=%ECLIPSEDIR%\plugins\org.eclipse.swt\swt.jar
 
set CLASSPATH=%CLASSPATH%;%ECLIPSEDIR%\plugins\org.eclipse.core.runtime\runtime.jar;%ECLIPSEDIR%\plugins\org.eclipse.core.boot\boot.jar;%ECLIPSEDIR%\plugins\org.eclipse.core.resources\resources.jar;%ECLIPSEDIR%\plugins\org.eclipse.update\update.jar;%ECLIPSEDIR%\plugins\org.eclipse.ui\workbench.jar;%ECLIPSEDIR%\startup.jar;%ECLIPSEDIR%\plugins\com.ibm.lpex\eclipselpex.jar;%CPP%;.;%XML%;%VCM%;%VCMUI%;%SEARCH%;%TM%;%DSCORE%;%DSEXTRA%;%DSUI%;%DSMINERS%;%SWT%;%DEBUGCORE%;%DEBUGUI%;%HELP%;%HOSTS%

set PATH=%PATH%;%ECLIPSEDIR%\..\jre\bin;%ECLIPSEDIR%;
