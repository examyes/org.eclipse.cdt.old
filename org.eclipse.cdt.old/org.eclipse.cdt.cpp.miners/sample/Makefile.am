//Makefile.am
SUBDIRS util

customdir = /home/test/mystuff
custom_PROGRAMS  = hello goodbye

hello_SOURCES      = hello.cpp aux.cpp aux.h
hello_LDFLAGS      = -all-static
hello_LDADD        = util.a
hello_DEPENDENCIES = goodbye

goodbye_SOURCES    = bye.c bye.h
