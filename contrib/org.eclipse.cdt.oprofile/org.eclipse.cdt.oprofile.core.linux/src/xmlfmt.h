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

#ifndef _XMLFMT_H
#define _XMLFMT_H
#include <ostream>

// Start a new tag with the given NAME
class startt
{
 public:
  startt (const std::string &name) : _name (name) {};
  friend std::ostream& operator<< (std::ostream& os, const startt& s);

 private:
  std::string _name;
};

// Add an attribute with the given NAME and VALUE to the current tag
class attrt
{
 public:
  attrt (const std::string& name, const std::string& value)
    : _name (name), _value (value) {};
  friend std::ostream& operator<< (std::ostream&, const attrt& a);

 private:
  std::string _name;
  std::string _value;

};

// End the current tag
std::ostream& endt (std::ostream& os);

// End the XML document and output it
std::ostream& endxml (std::ostream& os);
#endif // ! _XMLFMT_H
