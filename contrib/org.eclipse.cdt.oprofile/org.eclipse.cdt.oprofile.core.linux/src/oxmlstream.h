/* oxmlstream.h - A convenience class for outputting XML.
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

#ifndef _OXMLSTREAM_H
#define _OXMLSTREAM_H
#include <ostream>
#include "xmlbuf.h"
#include "xmlfmt.h"

// An ostream which outputs in XML. See xmlfmt.h for XML operators.
class oxmlstream : public std::ostream
{
 public:
  // Constructor - pass ostream onto which XML should be output.
  oxmlstream (std::ostream& os) : std::ostream (new xmlbuf (os)) {}
  ~oxmlstream () { delete rdbuf (); }
};
#endif // ! _OXMLSTREAM_H
