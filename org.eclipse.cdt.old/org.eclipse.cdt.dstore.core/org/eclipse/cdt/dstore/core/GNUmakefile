# makefile for non-win32 server
MAKE=gmake

all: platform extra_dir model_dir util_dir server_dir miners_dir makejar_dir

platform: extra\makefile

extra\makefile:
	cp -R extra.Server extra;

extra_dir:
	cd extra;$(MAKE);cd ..;

model_dir:
	cd model;$(MAKE);cd ..;

util_dir:
	cd util;$(MAKE);cd ..;

server_dir:
	cd server;$(MAKE);cd ..;

miners_dir:
	cd miners;$(MAKE);cd ..;

makejar_dir:
	cd ..\..\..;jar -cvf cpp.jar com;

clean:
	cd extra;$(MAKE) clean;cd ..;
	cd extra;$(MAKE) clean;cd ..;
	cd model;$(MAKE) clean;cd ..;
	cd util;$(MAKE) clean;cd ..;
	cd server;$(MAKE) clean;cd ..;
	cd miners;$(MAKE) clean;cd ..;
