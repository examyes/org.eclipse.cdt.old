.SUFFIXES: .java .class

.java.class:
       javac %s

all:  views actions dialogs HostsPlugin.class

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

dojar:
	cd ..\..\..\..
	jar -cvf dstore_hosts.jar com

clean:
	rm *.class

