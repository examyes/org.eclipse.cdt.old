/* xmlfmt - defines several operators and classes for formatting
   an XML stream (see oxmlstream.h)
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

#include "xmlfmt.h"
#include "xmlbuf.h"

using namespace std;

ostream&
operator<< (ostream& os, const startt& s)
{
  xmlbuf* xbuf = dynamic_cast<xmlbuf*> (os.rdbuf ());
  if (xbuf != NULL)
    xbuf->add_tag (s._name);
  return os;
}

ostream&
operator<< (ostream& os, const attrt& a)
{
  xmlbuf* xbuf = dynamic_cast<xmlbuf*> (os.rdbuf ());
  if (xbuf != NULL)
    xbuf->add_attr (a._name, a._value);
  return os;
}

ostream&
endt (ostream& os)
{
  xmlbuf* xbuf = dynamic_cast<xmlbuf*> (os.rdbuf ());
  if (xbuf != NULL)
    xbuf->end_tag ();
  return os;
}

ostream&
endxml (ostream& os)
{
  xmlbuf *xbuf = dynamic_cast<xmlbuf*> (os.rdbuf ());
  if (xbuf != NULL)
    xbuf->dump ();
  return os;
}
