
.SUFFIXES: .java .class

.java.class:
       javac %s

all:  AmParser ManagedProjectMiner.class AutoconfManager.class

AmParser: 
	cd amparser
	nmake
	cd ..


clean:
	rm *.class


