
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
#  "javadocs"              -> Generate Javadocs for every Plugin
#
SHELL=sh
# Grab Absolute Path to Plugin Directory...We do this by launching GenerateMakefiles with no arguments,
# which uses java.io.File.getCanonicalPath() to do the resolving of symbolic links, path separators etc
pluginsDirectory := $(shell cd build && javac GenerateMakefiles.java && cd .. && java -cp . build.GenerateMakefiles)

ifdef ECLIPSE
eclipseLocation := $(ECLIPSE)
else
eclipseLocation := $(shell test -r .eclipse && cat .eclipse)
endif

# List of Plugins that we build
buildPlugins += org.eclipse.cdt.debug.gdbPicl
buildPlugins += org.eclipse.cdt.dstore.extra
buildPlugins += org.eclipse.cdt.dstore.extra.server
buildPlugins += org.eclipse.cdt.dstore.core
buildPlugins += org.eclipse.cdt.linux.help	
buildPlugins += org.eclipse.cdt.cpp.miners.parser
buildPlugins += org.eclipse.cdt.dstore.hosts
buildPlugins += org.eclipse.cdt.dstore.ui
buildPlugins += org.eclipse.cdt.cpp.ui
buildPlugins += org.eclipse.cdt.dstore.miners
buildPlugins += org.eclipse.cdt.cpp.miners



# List of All Plugins Used in C/C++ Deliverable (e.g buildPlugins plus the ones we don't build)
allPlugins = $(buildPlugins)
allPlugins += org.eclipse.cdt.cpp.docs.user
allPlugins += com.ibm.lpex
#allPlugins += com.ibm.lpex.doc.user
#allPlugius += com.ibm.lpex.doc.isv
allPlugins += com.ibm.debug.doc.user
allPlugins += com.ibm.debug
allPlugins += com.ibm.debug.daemon


# List of Plugins needed for the Serverside Zip
serverPlugins =  org.eclipse.cdt.dstore.core
serverPlugins += org.eclipse.cdt.dstore.extra.server
serverPlugins += org.eclipse.cdt.dstore.miners
serverPlugins += org.eclipse.cdt.cpp.miners
serverPlugins += org.eclipse.cdt.cpp.miners.parser
serverPlugins += com.ibm.debug
serverPlugins += com.ibm.debug.daemon
serverPlugins += org.eclipse.cdt.debug.gdbPicl

# PUBLIC TARGETS
# --------------
# 
# The following are the targets that are meant to be used by you.  For the most part the actual
# work is done in the private targets below.  For the "public" targets that do some work on each
# plugin, we accomplish this by appending some text like "_buildPlugin" to every plugin name, 
# and then provide the actual rules for these pseudo-prerequisites below.
all:
	@cd org 2>/dev/null && (${MAKE} ; cd ..) || (echo "";echo "Build Environment needs to be generated...";echo "Run:  make dev[elopment]")

development     : makefiles $(patsubst %,%_buildPlugin,$(buildPlugins))
production      : development jars javadocs source-zips createZip createFinalPackage
jars            : $(patsubst %,%_createJars,$(buildPlugins))
javadocs        : $(patsubst %,%_createJavadocs,$(buildPlugins))
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

%_createJavadocs:
	@-cd $(pluginsDirectory)/$(patsubst %_createJavadocs,%,$@) && $(MAKE) javadoc   

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
	java -cp $(pluginsDirectory)/org.eclipse.cdt.dstore.core -DECLIPSE=$(eclipseLocation) build.GenerateMakefiles $(buildPlugins) || (echo '***' Error: Usage: make ECLIPSE=eclipse_directory targets && false)

unzipBuildPlugins: $(patsubst %,%_unzipPlugin,$(buildPlugins))

%_unzipPlugin:
	@-cd $(pluginsDirectory)/$(patsubst %_unzipPlugin,%,$@);\
	(cd org 2>/dev/null || cd com 2>/dev/null) || (echo Unzipping Source for $(patsubst %_unzipPlugin,%,$@); unzip -qo *src.zip)

zipFile=cdt-eclipse-0.5.0-local.zip
createZip: createEmptyZip zipUpPlugins
	@echo Created $(pluginsDirectory)/$(zipFile)

createEmptyZip: 
	@-cd $(pluginsDirectory) && rm -f $(zipFile) && zip -q $(zipFile) org.eclipse.cdt.dstore.core/makefile

zipUpPlugins: $(patsubst %,%_zipPlugin,$(allPlugins))

%_zipPlugin: 
	@-cd $(pluginsDirectory) && zip -qr $(zipFile) $(patsubst %_zipPlugin,%,$@) -x org *org\/* com *makefile* *setenv.mk* *com\/* *CVS*

tempDir=cdt
finalZip=cdt-eclipse-0.5.0.zip

createFinalPackage: createServerZip createFinalZip
	@echo Created $(pluginsDirectory)/$(finalZip)

createFinalZip:
	@-cd $(pluginsDirectory) && mkdir $(tempDir);\
	cp org.eclipse.cdt.cpp.docs.user/readme.txt $(tempDir);\
	cp org.eclipse.cdt.cpp.docs.user/install.txt $(tempDir);\
	cp org.eclipse.cdt.cpp.docs.user/todo.txt $(tempDir);\
	cp $(zipFile) $(tempDir);\
	cp $(serverZipFile) $(tempDir);\
	zip -qr $(finalZip) $(tempDir);\
	rm -r $(tempDir)

serverZipFile=cdt-eclipse-0.5.0-server.zip

createServerZip: createEmptyServerZip zipServerPlugins
	@echo Created $(pluginsDirectory)/$(serverZipFile)

#Not sure how to create an empty zip (without priming it with something)...so I just add the parser directory by default
createEmptyServerZip:
	@-cd $(pluginsDirectory) && rm -f $(serverZipFile) && zip -q $(serverZipFile) org.eclipse.cdt.dstore.core/makefile

zipServerPlugins: $(patsubst %,%_zipServerPlugin,$(serverPlugins))

%_zipServerPlugin:
	@-cd $(pluginsDirectory) && zip -qr $(serverZipFile) $(patsubst %_zipServerPlugin,%,$@) -x org *org\/* *makefile* *setenv.mk* com *com\/* *CVS*

# Targets specific to org.eclipse.cdt.dstore.core (these are copied from rules.mk...so make sure they stay in synch!!!
jarFile=dstore_core.jar
pluginName=org.eclipse.cdt.dstore.core

jar:
	@cd $(pluginsDirectory)/$(pluginName);\
	rm -f $(jarFile);\
	touch $(jarFile);\
	find . -name '*.class' -or -name '*.properties' | xargs jar uf $(jarFile)
	@echo " Created" $(pluginsDirectory)/$(pluginName)/$(jarFile) 

javadoc:
	@cd $(pluginsDirectory)/$(pluginName);\
	rm -Rf docs;\
	mkdir docs;\
	find . -name '*.java' | xargs javadoc -d docs -classpath $(subst $(space),$(sep),$(cp)) 2>/dev/null
	@echo " Created" $(pluginsDirectory)/$(pluginName)/docs

source-zip:
	@cd $(pluginsDirectory)/$(pluginName);\
	rm -f $(patsubst %.jar,%src.zip,$(jarFile));\
	zip -qr $(patsubst %.jar,%src.zip,$(jarFile)) . -i *.java *.jj *.properties build/*.mk build/*.java makefile 
	@echo " Created" $(pluginsDirectory)/$(pluginName)/$(patsubst %.jar,%src.zip,$(jarFile)) 

clean:
	@cd $(pluginsDirectory)/$(pluginName);\
	find . -name '*.class' |xargs rm -f
