/* xmlbuf - A class for XML output on an ostream.
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

#ifndef _XMLBUF_H
#define _XMLBUF_H
#include <streambuf>
#include <ostream>
#include <string>
#include <stack>

class xmltag;

class xmlbuf : public std::streambuf
{
 public:
  // Constructor - pass in the ostream to which to dump the whole
  // XML tree.
  xmlbuf (std::ostream& outstream);
  ~xmlbuf ();

  // Adds an XML tag of the given name
  // Invoked via "addt" operator (defined in xmlfmt.h)
  void add_tag (const std::string& tag_name);

  // Ends the current tag
  // Invoked via "endt" operator (defined in xmlfmt.h)
  void end_tag (void);

  // Adds the given attribute NAME with VALUE to the current tag
  // Invoked via "attrt" operator (defined in xmlfmt.h)
  void add_attr (const std::string& name, const std::string& value);

  // Dumps the whole tree to the (real) output stream
  // Invoked via "endxml" operator (defined in xmlfmt.h)
  void dump (void);

 protected:
  int overflow (int ch);

 private:
  // The ostream to dump the tree
  std::ostream& _os;

  // The top of the XML document tree
  xmltag* _top;

  // The current node in the tree being constructed
  xmltag* _current;

  // A stack of tags being constructed
  std::stack<xmltag*>* _tag_stack;
};
#endif // !_XMLBUF_H
