/* symbol - A class which represents symbols in executables
   Written by Keith Seitz <keiths@redhat.com>
   Copyright 2003, Red Hat, Inc.

   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 2 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 59 Temple Place - Suite 330,
   Boston, MA 02111-1307, USA.  */

#include <stdlib.h>

#include "symbol.h"
#include "xmlfmt.h"

// From libiberty
#ifndef DMGL_PARAMS
#define DMGL_PARAMS (1 << 0)	// Include function arguments
#endif

#ifndef DMGL_ANSI
#define DMGL_ANSI (1 << 1)	// Include const, volatile, etc
#endif

using namespace std;

extern "C" char* cplus_demangle (char const* mangled_name, int options);

const bfd_vma symbol::UNSET = (bfd_vma) -1;

symbol::symbol (asymbol* sym)
  : _end (UNSET), _asymbol (sym)
{
}

bool
symbol::contains (bfd_vma addr) const
{
  if (addr >= start() && addr < _end)
    return true;
  return false;
}

const char*
symbol::demangled_name (void) const
{
  char* demangled = cplus_demangle (name (), DMGL_PARAMS | DMGL_ANSI);
  if (demangled == NULL)
    return name ();

  return demangled;
}

ostream&
operator<< (ostream& os, const symbol* s)
{
  char buf[65];
  
  sprintf_vma (buf, s->start ());

  // get debug info
  
  return os << startt ("symbol")
	    << startt ("addr") << buf << endt
	    << startt ("name") << s->demangled_name () << endt
	    << endt;
}
