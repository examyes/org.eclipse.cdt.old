OBJS =	debugCreate.obj \
		debugClient.obj \
		debugControl.obj \
		debugOutputCallbacks.obj \
		debugEventCallbacks.obj

TARGET = ..\os\win32\x86\win32cdi.dll

JAVA_HOME = C:\Java\jdk1.5.0_06
DEBUG_HOME = "C:\Program Files\Debugging Tools for Windows\sdk"

CPPFLAGS =	/I $(JAVA_HOME)\include /I $(JAVA_HOME)\include\win32 /I $(DEBUG_HOME)\inc /DUNICODE

all:	$(TARGET)

$(TARGET):	$(OBJS)
	link /nologo /dll -out:$(TARGET) $(OBJS) user32.lib advapi32.lib

clean:
	rm -f $(TARGET) $(OBJS)
