
all: AmParser ManagedProjectMiner.class

AmParser: 
	cd amparser
	nmake
	cd ..

ManagedProjectMiner.class: ManagedProjectMiner.java
	javac ManagedProjectMiner.java

clean:
	rm *.class


