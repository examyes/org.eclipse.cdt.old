

all: Formats CommandMiner.class CommandMinerThread.class

Formats: 
	cd formats;gmake;cd ..;

CommandMiner.class: CommandMiner.java ../../extra/IDataElement.java
	javac CommandMiner.java

CommandMinerThread.class: CommandMiner.java 
	javac CommandMiner.java

clean:
	rm *.class
