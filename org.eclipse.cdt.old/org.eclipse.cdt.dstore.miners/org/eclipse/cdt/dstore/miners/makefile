
MAKE=nmake
 
all: filesystem environment command test settings monitor cvstest dictionary

filesystem:	
	cd filesystem
	$(MAKE)
	cd ..

command:	
	cd command
	$(MAKE)
	cd ..

environment:	
	cd environment
	$(MAKE)
	cd ..

test:	
	cd test
	$(MAKE)
	cd ..

settings:	
	cd settings
	$(MAKE)
	cd ..

monitor:	
	cd monitor
	$(MAKE)
	cd ..

cvstest:	
	cd cvstest
	$(MAKE)
	cd ..

dictionary:
	cd dictionary
	$(MAKE)
	cd ..

dojar:
	cd ..\..\..\..
	jar -cvf dstore_miners.jar com
