OBJS = 	debugClient.obj debugOutputCallbacks.obj

TARGET = ../lib/win32cdi.dll

JAVA_HOME = /c/Eclipse/j2sdk1.4.2_06

INCLUDES =	-I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/win32

%.obj:	%.cpp
	cl -nologo -c -EHsc -DUNICODE $(INCLUDES) $<
	
all:	$(TARGET)

$(TARGET):	$(OBJS)
	link -nologo -dll -out:$(TARGET) $(OBJS) dbgeng.lib dbghelp.lib user32.lib

clean:
	rm -f $(TARGET) $(OBJS)
