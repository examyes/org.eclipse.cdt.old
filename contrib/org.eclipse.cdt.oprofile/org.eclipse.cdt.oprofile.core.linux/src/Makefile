# Makefile for Eclipse Oprofile wrapper library
# Written by Keith Seitz  <keiths@redhat.com>
#
# Copyright (c) 2003, Red Hat, Inc.

CFLAGS += -g -Wall
CXXFLAGS = $(CFLAGS)

OPROFILE_LIBS = -lop -lodb -loputil++ -loputil
BFD_LIBRARY = -lbfd
LIBIBERTY = -liberty
OTHER_LIBS = $(BFD_LIBRARY)
EXTRA_LIBS = $(OPROFILE_LIBS) $(OTHER_LIBS) $(LIBIBERTY)

OPXML = 		opxml
OPXML_OBJS =		opxml.o
OPROFILE_LIB_OBJS =	stable.o oprofiledb.o symbol.o sample.o \
			session.o samplefile.o sfheader.o opinfo.o \
			xmlfmt.o xmlbuf.o xmltag.o util.o


all: $(OPXML)

$(OPXML): $(OPROFILE_LIB_OBJS) $(OPXML_OBJS)
		$(CXX) -o $(OPXML) $(OPXML_OBJS) $(OPROFILE_LIB_OBJS) \
		$(EXTRA_LIBS)

clean:
	$(RM) $(OPROFILE_LIB_OBJS) $(OPXML) $(OPXML_OBJS) *~



# Dependencies
tst.o:  session.h samplefile.h tst.cc

opxml.o: oxmlstream.h opinfo.h session.h samplefile.h opxml.cc

oprofiledb.o: oprofiledb.h sample.h stable.h oprofiledb.cc

stable.o: stable.h symbol.h stable.cc

symbol.o: symbol.h symbol.cc

sample.o: symbol.h sample.h sample.cc

session.o: session.h samplefile.h util.h session.cc

samplefile.o: samplefile.h sfheader.h stable.h xmlfmt.h sample.h oprofiledb.h util.h samplefile.cc

sfheader.o: xmlfmt.h sfheader.h sfheader.cc

opinfo.o: opinfo.h opinfo.cc

xmlfmt.o: xmlbuf.h xmlfmt.h xmlfmt.cc

xmlbuf.o: xmltag.h xmlbuf.h xmlbuf.cc

xmltag.o: xmltag.h xmltag.cc

