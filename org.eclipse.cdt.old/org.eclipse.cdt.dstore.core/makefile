
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
# which uses java.io.File.getCanonicalPath() to do the resolving of symbolic links, path separators etc
pluginsDirectory := $(shell cd build && javac GenerateMakefiles.java && java -cp . GenerateMakefiles)

ifdef ECLIPSE
eclipseLocation := $(ECLIPSE)
else
eclipseLocation := $(shell test -r .eclipse && cat .eclipse)
endif

# List of Plugins that we build
buildPlugins += com.ibm.dstore.extra
buildPlugins += com.ibm.dstore.extra.server
buildPlugins += com.ibm.dstore.core
buildPlugins += com.ibm.debug
buildPlugins += com.ibm.cpp.miners.parser
buildPlugins += com.ibm.dstore.hosts
buildPlugins += com.ibm.dstore.ui
buildPlugins += com.ibm.cpp.ui
buildPlugins += com.ibm.dstore.miners
buildPlugins += com.ibm.cpp.miners
buildPlugins += com.ibm.debug.gdbPicl
buildPlugins += com.ibm.linux.help	

# List of All Plugins Used in C/C++ Deliverable (e.g buildPlugins plus the ones we don't build)
allPlugins = $(buildPlugins)
allPlugins += com.ibm.cpp.docs.user
allPlugins += com.ibm.lpex
allPlugins += com.ibm.debug.doc.user

# List of Plugins needed for the Serverside Zip
serverPlugins =  com.ibm.dstore.core
serverPlugins += com.ibm.dstore.extra.server
serverPlugins += com.ibm.dstore.miners
serverPlugins += com.ibm.cpp.miners
serverPlugins += com.ibm.cpp.miners.parser
serverPlugins += com.ibm.debug
serverPlugins += com.ibm.debug.gdbPicl

# PUBLIC TARGETS
# --------------
# 
# The following are the targets that are meant to be used by you.  For the most part the actual
# work is done in the private targets below.  For the "public" targets that do some work on each
# plugin, we accomplish this by appending some text like "_buildPlugin" to every plugin name, 
# and then provide the actual rules for these pseudo-prerequisites below.
all:
	@cd com 2>/dev/null && (${MAKE} ; cd ..) || (echo "";echo "Build Environment needs to be generated...";echo "Run:  make dev[elopment]")

development     : makefiles $(patsubst %,%_buildPlugin,$(buildPlugins))
production      : development jars source-zips createZip createFinalPackage
jars            : $(patsubst %,%_createJars,$(buildPlugins))
source-zips     : $(patsubst %,%_createSrcZips,$(buildPlugins))
clean           : $(patsubst %,%_cleanPlugin,$(buildPlugins))
clean-makefiles : $(patsubst %,%_cleanMakefiles,$(buildPlugins))
makefiles       : generateBuildEnvironment
# Helper shortcut targets (for lazy typists)
dev             : development
prod            : production

#
# WORKAROUND
# Correct the file modes of executable scripts in case the
# components were acquired by Eclipse CVS
fixup:
	find .. -type f -a \! -name '*.java' \
		-a \! -name '*.class' -a \! -name '*html' \
		-a \! -name '*.gif' | xargs file | grep executable | \
		sed -e 's/\(.*\):.*/\1/' | xargs chmod a+x

# PRIVATE TARGETS
# ---------------
# 
# Don't try to use these targets...They are helpers.
%_buildPlugin: 
	@echo Building $(patsubst %_buildPlugin,%,$@)
	@-cd $(pluginsDirectory)/$(patsubst %_buildPlugin,%,$@) && $(MAKE) 
	@echo Finished $(patsubst %_buildPlugin,%,$@)

%_createJars:
	@-cd $(pluginsDirectory)/$(patsubst %_createJars,%,$@) && $(MAKE) jar   

%_createSrcZips:
	@-cd $(pluginsDirectory)/$(patsubst %_createSrcZips,%,$@) && $(MAKE) source-zip   

%_cleanPlugin:
	@-cd $(pluginsDirectory)/$(patsubst %_cleanPlugin,%,$@) && $(MAKE) clean

%_cleanMakefiles:
	@-cd $(pluginsDirectory)/$(patsubst %_cleanMakefiles,%,$@) && $(MAKE) clean-makefiles 

generateBuildEnvironment: unzipBuildPlugins
ifneq "$(ECLIPSE)" ""
	@echo $(eclipseLocation) >.eclipse
endif
	@cd build && java -cp . -DECLIPSE=$(eclipseLocation) GenerateMakefiles $(buildPlugins) || (echo '***' Error: Usage: make ECLIPSE=eclipse_directory targets && false)

unzipBuildPlugins: $(patsubst %,%_unzipPlugin,$(buildPlugins))

%_unzipPlugin:
	@-cd $(pluginsDirectory)/$(patsubst %_unzipPlugin,%,$@);\
	cd com 2>/dev/null || (echo Unzipping Source for $(patsubst %_unzipPlugin,%,$@); unzip -q *src.zip)

zipFile=c4eclipse.zip
createZip: createEmptyZip zipUpPlugins
	@echo Created $(pluginsDirectory)/$(zipFile)

createEmptyZip: 
	@-cd $(pluginsDirectory) && rm -f $(zipFile) && zip -q $(zipFile) com.ibm.dstore.core/makefile

zipUpPlugins: $(patsubst %,%_zipPlugin,$(allPlugins))

%_zipPlugin: 
	@-cd $(pluginsDirectory) && zip -qr $(zipFile) $(patsubst %_zipPlugin,%,$@) -x com *com\/* *CVS*

tempDir=cdt
finalZip=cdt-eclipse-0.5.0.zip

createFinalPackage: createServerZip createFinalZip
	@echo Created $(pluginsDirectory)/$(finalZip)

createFinalZip:
	@-cd $(pluginsDirectory) && mkdir $(tempDir);\
	cp com.ibm.cpp.docs.user/readme.txt $(tempDir);\
	cp $(zipFile) $(tempDir);\
	cp $(serverZipFile) $(tempDir);\
	zip -qr $(finalZip) $(tempDir);\
	rm -r $(tempDir)

serverZipFile=cdt-eclipse-0.5.0-server.zip

createServerZip: createEmptyServerZip zipServerPlugins
	@echo Created $(pluginsDirectory)/$(serverZipFile)

#Not sure how to create an empty zip (without priming it with something)...so I just add the parser directory by default
createEmptyServerZip:
	@-cd $(pluginsDirectory) && rm -f $(serverZipFile) && zip -q $(serverZipFile) com.ibm.cpp.miners.parser

zipServerPlugins: $(patsubst %,%_zipServerPlugin,$(serverPlugins))

%_zipServerPlugin:
	@-cd $(pluginsDirectory) && zip -qr $(serverZipFile) $(patsubst %_zipServerPlugin,%,$@) -x com *com\/* *CVS*

# Targets specific to com.ibm.dstore.core (these are copied from rules.mk...so make sure they stay in synch!!!
jarFile=dstore_core.jar
pluginName=com.ibm.dstore.core

jar:
	@cd $(pluginsDirectory)/$(pluginName);\
	rm -f $(jarFile);\
	touch $(jarFile);\
	find . -name '*.class' -or -name '*.properties' | xargs jar uf $(jarFile)
	@echo " Created" $(pluginsDirectory)/$(pluginName)/$(jarFile) 

source-zip:
	@cd $(pluginsDirectory)/$(pluginName);\
	rm -f $(patsubst %.jar,%src.zip,$(jarFile));\
	zip -qr $(patsubst %.jar,%src.zip,$(jarFile)) . -i *.java *.jj build/*.mk build/*.java makefile 
	@echo " Created" $(pluginsDirectory)/$(pluginName)/$(patsubst %.jar,%src.zip,$(jarFile)) 

clean:
	@cd $(pluginsDirectory)/$(pluginName);\
	find . -name '*.class' |xargs rm -f
