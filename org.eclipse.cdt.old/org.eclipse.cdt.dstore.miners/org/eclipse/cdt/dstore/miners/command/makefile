

all: Patterns CommandMiner.class CommandMinerThread.class

Patterns: 
	cd patterns
	nmake
	cd.. 

CommandMiner.class: CommandMiner.java
	javac CommandMiner.java

CommandMinerThread.class: CommandMiner.java 
	javac CommandMiner.java

clean:
	rm *.class


