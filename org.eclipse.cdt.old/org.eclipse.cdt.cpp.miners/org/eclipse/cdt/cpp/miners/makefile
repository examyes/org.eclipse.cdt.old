
MAKE=nmake
 
all: project managedproject search parser 

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

parser:
	cd parser
	$(MAKE)
	cd ..

dojar:
	cd ..\..\..\..
	jar -cvf cpp_miners.jar com
