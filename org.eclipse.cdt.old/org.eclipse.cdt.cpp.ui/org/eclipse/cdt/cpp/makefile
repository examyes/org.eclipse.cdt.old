
MAKE=nmake

all: ui

ui:
	cd ui
	$(MAKE)
	cd ..

makejar:
	cd ..\..\..
	jar -cvf cppui.jar com

clean:
	del *.class /S