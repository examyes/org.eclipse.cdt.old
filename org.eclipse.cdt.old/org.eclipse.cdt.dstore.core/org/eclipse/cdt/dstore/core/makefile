# Makefile for WIN32
MAKE=nmake

all: DataStoreCorePlugin.class model util client server miners 

DataStoreCorePlugin.class: DataStoreCorePlugin.java
	javac DataStoreCorePlugin.java 


model:
	cd model
	$(MAKE)
	cd ..

util:
	cd util
	$(MAKE)
	cd ..

client:
	cd client
	$(MAKE)
	cd ..

server:
	cd server
	$(MAKE)
	cd ..

miners:
	cd miners
	$(MAKE)
	cd ..

web:
	cd web
	$(MAKE)
	cd ..

dojar:
	cd ..\..\..\..
	jar -cvf dstore_core.jar com

clean:
	del *.class /S
