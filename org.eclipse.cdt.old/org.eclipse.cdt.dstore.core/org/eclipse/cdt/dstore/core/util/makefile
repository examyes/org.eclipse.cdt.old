.SUFFIXES: .java .class

.java.class:
       javac %s

all: Sender.class Receiver.class XMLgenerator.class CommandGenerator.class Pattern.class XMLparser.class StringCompare.class ObjectSize.class regex

regex:
	cd regex
	$(MAKE)
	cd ..
clean:
	rm *.class
