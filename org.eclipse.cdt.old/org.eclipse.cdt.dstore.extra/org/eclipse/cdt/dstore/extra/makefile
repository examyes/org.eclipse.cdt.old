
MAKE=nmake

all: client

internal: client server

client:
	cd internal
	$(MAKE) client
	cd ..	
	cd ..\..\..\..
	jar -cvf dstore_extra_client.jar com

server:
	cd internal
	$(MAKE) server
	cd ..
	cd ..\..\..\..
	jar -cvf dstore_extra_server.jar com

clean:
	cd internal
	$(MAKE) clean
	cd ..