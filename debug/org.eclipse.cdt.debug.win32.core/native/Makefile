OBJS =	debugCreate.obj \
		debugClient.obj \
		debugControl.obj \
		debugOutputCallbacks.obj \
		debugEventCallbacks.obj

TARGET = ../lib/win32cdi.dll

JAVA_HOME = /d/Java/jdk1.5.0_04

INCLUDES =	-I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/win32

%.obj:	%.cpp
	cl -nologo -c -EHsc -DUNICODE $(INCLUDES) $<
	
all:	$(TARGET)

$(TARGET):	$(OBJS)
	link -nologo -dll -out:$(TARGET) $(OBJS) user32.lib advapi32.lib

clean:
	rm -f $(TARGET) $(OBJS)
