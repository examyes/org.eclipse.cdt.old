OBJS = 	frame.obj process.obj target.obj thread.obj

TARGET = ../lib/win32cdi.dll

INCLUDES =	/I $(JAVA_HOME)/include /I $(JAVA_HOME)/include/win32

%.obj:	%.cpp
	cl /nologo /c /EHsc /DUNICODE $(INCLUDES) $<
	
all:	$(TARGET)

$(TARGET):	$(OBJS)
	link /nologo /dll /out:$(TARGET) $(OBJS) dbghelp.lib user32.lib

clean:
	rm -f $(TARGET) $(OBJS)
