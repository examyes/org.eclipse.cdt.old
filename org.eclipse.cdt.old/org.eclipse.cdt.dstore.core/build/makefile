
# Primary Targets
# ---------------
#
#  "development" or "dev"  -> Generate Makefile & Build Environment for All Plugins, and perform build
#  "production" or "prod"  -> Generate a tarball that includes the necessary jars and zipped up source 
#  "all" (or no target)    -> Build all plugins (makefiles are assumed to have been created)
#  "clean"                 -> Wipe out all class files
#  "clean-makefiles"       -> Wipe out the Build Environment
#
# Secondary Targets (used by Primary Targets, but could still be used on their own)
# -----------------
#
#  "makefiles"             -> Generate Makefiles & Build Environment for All Plugins
#  "jars"                  -> Generate Jar Files for every Plugin

# Grab Absolute Path to Plugin Directory...We do this by launching GenerateMakefiles with no arguments,
# which used java.io.File.getCanonicalPath() to do the resolving of symbolic links, path separators etc
pluginsDirectory := $(shell javac GenerateMakefiles.java && java -cp . GenerateMakefiles)

# List of All Plugins Used in C/C++ Deliverable
allPlugins += com.ibm.dstore.extra
allPlugins += com.ibm.dstore.core
allPlugins += com.ibm.cpp.miners.parser
allPlugins += com.ibm.dstore.hosts
allPlugins += com.ibm.dstore.ui
allPlugins += com.ibm.cpp.ui
allPlugins += com.ibm.dstore.miners
allPlugins += com.ibm.cpp.miners
allPlugins += com.ibm.debug.gdbPicl
allPlugins += com.ibm.linux.help	
allPlugins += com.ibm.cpp.docs.user
allPlugins += com.ibm.lpex
allPlugins += com.ibm.debug
allPlugins += com.ibm.debug.doc.user

# Look for list of Plugins that are "build-able", (e.g. that have a 'com' directory).  This is a little complicated,
# but there are really 5 pieces of work going on:
# 1.  Add the dstore.extra and dstore.core directories to the list to ensure they appear first.
# 2.  Using the $(wildcard) function, get all of the plugins that have a 'com' subdirectory
# 3.  Using the $(patsubst) function, remove the plugin Directory from the beginning of each directory
# 4.  Using the $(patsubst) function, remove the /com/ suffix from each directory
# 5.  Using the $(filter-out) funtion, remove the dstore.core and dstore.extra directory from the generated list
#

buildPlugins = com.ibm.dstore.extra com.ibm.dstore.core\
               $(filter-out com.ibm.dstore.core,\
                 $(filter-out com.ibm.dstore.extra,\
                   $(patsubst %/com/,%,\
                      $(patsubst $(pluginsDirectory)/%,%,\
                        $(wildcard $(pluginsDirectory)/*/com/)\
                       )\
                    )\
                  )\
                )


# PUBLIC TARGETS
# --------------
# 
# The following are the targets that are meant to be used by you.  For the most part the actual
# work is done in the private targets below.  For the "public" targets that do some work on each
# plugin, we accomplish this by appending some text like "_buildPlugin" to every plugin name, 
# and then provide the actual rules for these pseudo-prerequisites below.
development     : makefiles all
production      : development jars source-zips createTar
all             : $(patsubst %,%_buildPlugin,$(buildPlugins))
jars            : $(patsubst %,%_createJars,$(buildPlugins))
source-zips     : $(patsubst %,%_createSrcZips,$(buildPlugins))
clean           : $(patsubst %,%_cleanPlugin,$(buildPlugins))
clean-makefiles : $(patsubst %,%_cleanMakefiles,$(buildPlugins))
makefiles       : generateBuildEnvironment
# Helper shortcut targets (for lazy typists)
dev             : development
prod            : production


# PRIVATE TARGETS
# ---------------
# 
# Don't try to use these targets...They are helpers.
%_buildPlugin: 
	@echo Building $(patsubst %_buildPlugin,%,$@)
	@-cd $(pluginsDirectory)/$(patsubst %_buildPlugin,%,$@) && $(MAKE) $(MAKEFLAGS)
	@echo Finished $(patsubst %_buildPlugin,%,$@)

%_createJars:
	@-cd $(pluginsDirectory)/$(patsubst %_createJars,%,$@) && $(MAKE) $(MAKEFLAGS) jar   

%_createSrcZips:
	@-cd $(pluginsDirectory)/$(patsubst %_createSrcZips,%,$@) && $(MAKE) $(MAKEFLAGS) source-zip   

%_cleanPlugin:
	@-cd $(pluginsDirectory)/$(patsubst %_cleanPlugin,%,$@) && $(MAKE) $(MAKEFLAGS) clean

%_cleanMakefiles:
	@-cd $(pluginsDirectory)/$(patsubst %_cleanMakefiles,%,$@) && $(MAKE) $(MAKEFLAGS) clean-makefiles 

generateBuildEnvironment: setUpSpecialPlugins GenerateMakefiles.class
	@java -cp . GenerateMakefiles $(buildPlugins)

createTar: createEmptyTar tarUpPlugins

tarFile=c4eclipse.zip

createEmptyTar: 
	@-cd $(pluginsDirectory) && rm -f $(tarFile)*

tarUpPlugins: $(patsubst %,%_tarPlugin,$(allPlugins))

%_tarPlugin: 
	@-cd $(pluginsDirectory) && zip -qr $(tarFile) $(patsubst %_tarPlugin,%,$@) -x com *com\/* *CVS* &&\
	echo Created Master Zip File -> $(pluginsDirectory)/$(tarFile)

GenerateMakefiles.class: GenerateMakefiles.java
	@javac GenerateMakefiles.java

# The last 3 targets are some hacky stuff to set up the dstore.extra and gdbPicl Plugins...
setUpSpecialPlugins: setUpExtraPlugin setUpGdbPiclPlugin

setUpExtraPlugin:
	@echo Generating Build Environment for All Plugins
	@-cd $(pluginsDirectory)/com.ibm.dstore.extra/com/ibm/dstore/extra/internal;\
	rm -rf extra;\
	cp -r extra.Server extra;\
	cd extra;\
	javac -classpath $(pluginsDirectory)/com.ibm.dstore.core *.java;\
	cd $(pluginsDirectory)/com.ibm.dstore.extra;\
	rm -f dstore_extra_server.jar;\
	touch -f dstore_extra_server.jar;\
	find . -name '*.class' |xargs jar -uf dstore_extra_server.jar;\
	cd $(pluginsDirectory)/com.ibm.dstore.extra/com/ibm/dstore/extra/internal;\
	rm -rf extra;\
	cp -r extra.Client extra;

ifeq (gdbPicl,$(findstring gdbPicl, $(buildPlugins)))
 setUpGdbPiclString=cd $(pluginsDirectory)/com.ibm.debug.gdbPicl/com/ibm/debug;\
 rm -rf util;\
 mkdir util;\
 cd util;\
 java -classpath $(pluginsDirectory)/com.ibm.debug/IBMDebug.jar com.ibm.debug.util.CreateBuildInformation;\
 javac *.java;
endif

setUpGdbPiclPlugin:
	$(setUpGdbPiclString)