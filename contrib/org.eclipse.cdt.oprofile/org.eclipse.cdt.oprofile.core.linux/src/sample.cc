/* sample - A class which represents an Oprofile sample
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

#include "sample.h"
#include "xmlfmt.h"
#include "stable.h"

using namespace std;

// Constructor - pass in sample's address, any associated symbol, count,
// and 
sample::sample(bfd_vma addr, symbol* sym, unsigned int count)
  : _addr (addr), _symbol (sym), _count (count)
{
}

ostream&
operator<< (ostream& os, const sample* s)
{
  char buf[65];
  sprintf_vma (buf, s->get_vma ());

  os << startt ("sample")
     << startt ("addr") << buf << endt
     << startt ("count") << s->get_count () << endt;
  
  if (s->has_symbol ())
    os << s->get_symbol ();

#if 0
  if (s->_debuginfo != NULL)
    os << s->_debuginfo;
#endif

  return os << endt;
}
