.SUFFIXES: .java .class

.java.class:
       javac %s

all:  connections actions dialogs dnd resource IOpenAction.class IActionLoader.class ILinkable.class ISelected.class DataStoreUIPlugin.class ConvertUtility.class  ISelected.class widgets views 

widgets:
	cd widgets
	nmake all
	cd ..

views:
	cd views
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

dnd:
	cd dnd
	nmake all
	cd ..

connections:
	cd connections
	nmake all
	cd ..

resource:
	cd resource
	nmake all
	cd ..

dojar:
	cd ..\..\..\..
	jar -cvf dstore_ui.jar com

clean:
	rm *.class

