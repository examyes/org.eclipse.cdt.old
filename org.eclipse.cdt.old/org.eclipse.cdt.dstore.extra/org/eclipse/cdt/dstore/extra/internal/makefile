
MAKE=nmake
RM=del /S

all: extra

extra:
	cd extra
	$(MAKE)
	cd ..

client:	
	$(RM) extra
	xcopy extra.Client extra
	cd extra
	$(MAKE) 
	cd ..	

server: 
	$(RM) extra
	xcopy extra.Server extra
	cd extra
	$(MAKE) 
	cd ..

clean:
	cd extra
	$(MAKE) clean
	cd ..