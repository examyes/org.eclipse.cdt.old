
MAKE=nmake
 
all: project managedproject search parser debug

project:
	cd project
	$(MAKE)
	cd ..

managedproject:
	cd managedproject
	$(MAKE)
	cd ..

search:
	cd search
	$(MAKE)
	cd ..

debug:
	cd debug
	$(MAKE)
	cd ..

parser:
	cd parser
	$(MAKE)
	cd ..

dojar:
	cd ..\..\..\..
	jar -cvf cpp_miners.jar com
