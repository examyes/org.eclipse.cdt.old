OBJS = 	frame.obj process.obj target.obj thread.obj \
		os.obj context.obj stackframe64.obj symbol_info.obj

TARGET = ../lib/win32cdi.dll

INCLUDES =	/I $(JAVA_HOME)/include /I $(JAVA_HOME)/include/win32

%.obj:	%.cpp
	cl /nologo /c /EHsc /DUNICODE $(INCLUDES) $<
	
all:	$(TARGET)

$(TARGET):	$(OBJS)
	link /nologo /dll /out:$(TARGET) $(OBJS) dbghelp.lib user32.lib

clean:
	rm -f $(TARGET) $(OBJS)

os.obj:			os.cpp context.h stackframe64.h symbol_info.h
context.obj:	context.cpp context.h
stackframe64.obj:	stackframe64.cpp stackframe64.h
symbol_info.obj:	symbol_info.cpp symbol_info.h
