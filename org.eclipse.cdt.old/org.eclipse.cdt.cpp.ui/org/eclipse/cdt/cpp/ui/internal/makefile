
all:  CppProjectAttributes.class CppPlugin.class actions dialogs vcm api views builder editor wizards preferences

CppProjectAttributes.class: CppProjectAttributes.java
	javac $(FLAGS) CppProjectAttributes.java

CppPlugin.class: CppPlugin.java
	javac $(FLAGS) CppPlugin.java

preferences:
	cd preferences
	nmake all
	cd ..

builder:
	cd builder
	nmake all
	cd ..

wizards:
	cd wizards
	nmake all
	cd ..

actions:
	cd actions
	nmake all
	cd ..

dialogs:
	cd dialogs
	nmake all
	cd ..

views:
	cd views
	nmake all
	cd ..

api:
	cd api
	nmake all
	cd ..

editor:
	cd editor
	nmake all
	cd ..

vcm:
	cd vcm
	nmake all
	cd ..

dojar:
	cd ..\..\..\..
	jar -cvf cpp_ui.jar com

clean:
	rm *.class

