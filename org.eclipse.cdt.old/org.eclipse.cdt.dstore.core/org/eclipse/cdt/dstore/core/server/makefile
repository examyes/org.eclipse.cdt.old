.SUFFIXES: .java .class

.java.class:
       javac %s

all: Server.class ServerAttributes.class ConnectionEstablisher.class ServerUpdateHandler.class ServerCommandHandler.class ServerReceiver.class ServerLauncher.class ServerReturnCodes.class MinerLoader.class ILoader.class Loader.class

clean:
	rm *.class 
