# 
# Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
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
$(filter-out %extra.Client,\
$(filter-out %extra.Server,\
$(filter-out %CVS,\
$(filter-out %icons,\
$(shell find * -type d -maxdepth 0)))))))
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
clean             : doClean
clean-makefiles   : doCleanMakefiles


# Private\Implicit Targets
# ------------------------
#
#addCopyrights:=$(patsubst %, java -cp f:/programs/cygwin/usr/local/javautils/addCopyright.jar addCopyright % "Copyright (c) 2001 International Business Machines Corporation. All rights reserved. This program and the accompanying materials are made available under the terms of the Common Public License which accompanies this distribution.";,$(shell find $(pluginsDirectory)/$(pluginName) -type d))

%.class: %.java
	@echo " Compiling" $(patsubst %.class, %.java, $@)
	@javac -classpath $(subst $(space),$(sep),$(cp)) $(JAVACFLAGS) $<

%_dir:
	@$(MAKE) -C $(patsubst %_dir,%, $@)

createJarFile:
	@cd $(pluginsDirectory)/$(pluginName);\
	rm -f $(jarFile);\
	touch $(jarFile);\
	find . -name '*.class' -or -name '*.properties' | xargs jar -uf $(jarFile)
	@echo " Created" $(pluginsDirectory)/$(pluginName)/$(jarFile) 

createSourceZip:
	@cd $(pluginsDirectory)/$(pluginName);\
	rm -f $(patsubst %.jar,%src.zip,$(jarFile));\
	zip -qr $(patsubst %.jar,%src.zip,$(jarFile)) . -i *.java *.jj *build/makefile *rules.mk
	@echo " Created" $(pluginsDirectory)/$(pluginName)/$(patsubst %.jar,%src.zip,$(jarFile)) 

doClean:
	@cd $(pluginsDirectory)/$(pluginName);\
	find . -name '*.class' |xargs rm -f










