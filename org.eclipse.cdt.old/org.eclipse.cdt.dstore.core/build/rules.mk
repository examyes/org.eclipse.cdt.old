# 
# Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
# This program and the accompanying materials are made available under the terms of
# the Common Public License which accompanies this distribution.
#
SHELL=sh

# First We Set Up Some Helper Variables
# ----------------
#
# Grab the .java files from the current directory and replace the .java with .class
define get-files
 $(patsubst %.java, %.class, $(wildcard *.java))
endef

# Grab the directories from the current directory, weed out CVS, and append _dir to each
define get-directories
$(patsubst %, %_dir,\
$(filter-out build,\
$(filter-out %CVS,\
$(filter-out %cvs,\
$(filter-out %icons,\
$(filter-out %lib,\
$(shell find * -type d -print -prune)))))))
endef

# Determine the system path separator
ifeq (win, $(findstring win,$(subst W,w,$(OS))))
 sep:= \;
else
 sep:= :
endif

# These are used to help convert the variable cp to a path-separated Classpath
empty:=
space:= $(empty) $(empty)


# Public Targets
# --------------
#
all               : $(get-directories) $(get-files)
jar               : createJarFile 
source-zip        : createSourceZip
javadoc		  : createJavadoc
clean             : doClean

# Private\Implicit Targets
# ------------------------
#
#addCopyrights:=$(patsubst %, java -cp f:/programs/cygwin/usr/local/javautils/addCopyright.jar addCopyright % "Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved. This program and the accompanying materials are made available under the terms of the Common Public License which accompanies this distribution.";,$(shell find $(pluginsDirectory)/$(pluginName) -type d))

helpjarSource=help/helpwebapp/servlet

%.class: %.java
	@echo " Compiling" $(patsubst %.class, %.java, $@)
	@javac -classpath $(subst $(space),$(sep),$(cp)) $(JAVACFLAGS) $<
ifeq ($(helpjarSource),$(findstring $(helpjarSource),$(shell pwd)))
	$(createHelpJar)
	@echo " Created" $(pluginsDirectory)/$(pluginName)/$(helpjarDir)/$(helpjarFile)
endif	

%_dir:
	@$(MAKE) -C $(patsubst %_dir,%, $@)

createJarFile:
	@cd $(pluginsDirectory)/$(pluginName);\
	rm -f $(jarFile);\
	touch $(jarFile);\
	find . -name '*.class' -o -name '*.properties' | xargs jar -uf $(jarFile)
	@echo " Created" $(pluginsDirectory)/$(pluginName)/$(jarFile) 
ifeq ($(pluginName), org.eclipse.cdt.cpp.miners)
	$(createHelpJar)
	@echo " Created" $(pluginsDirectory)/$(pluginName)/$(helpjarDir)/$(helpjarFile)
endif

createJavadoc:
	@cd $(pluginsDirectory)/$(pluginName);\
	rm -Rf docs;\
	mkdir docs;\
	find . -name '*.java' | xargs javadoc -d docs -classpath $(subst $(space),$(sep),$(cp)) 2>/dev/null
	@echo " Created" $(pluginsDirectory)/$(pluginName)/docs

createSourceZip:
	@cd $(pluginsDirectory)/$(pluginName);\
	rm -f $(patsubst %.jar,%src.zip,$(jarFile));\
	zip -qr $(patsubst %.jar,%src.zip,$(jarFile)) . -i *.java *.jj *.properties *build/makefile *rules.mk
	@echo " Created" $(pluginsDirectory)/$(pluginName)/$(patsubst %.jar,%src.zip,$(jarFile)) 

doClean:
	@cd $(pluginsDirectory)/$(pluginName);\
	find . -name '*.class' |xargs rm -f

#This constructs the help webapp jar and puts it in the right place
deployHelpWebApp:
ifeq ($(pluginName),org.eclipse.cdt.cpp.miners)
	$(createHelpJar)
	@echo " Created" $(pluginsDirectory)/$(pluginName)/$(helpjarDir)/$(helpjarFile)
endif

helpjarFile=helpwebapp.jar
helpjarDir=help/helpwebapp/WEB-INF/lib
define createHelpJar
	@cd $(pluginsDirectory)/$(pluginName);\
	rm -Rf $(pluginsDirectory)/$(pluginName)/$(helpjarDir)/$(helpjarFile);\
	mkdir $(pluginsDirectory)/$(pluginName)/$(helpjarDir);\
	find help/helpwebapp/servlet -name '*.class' |xargs jar -cf $(helpjarDir)/$(helpjarFile)
endef








