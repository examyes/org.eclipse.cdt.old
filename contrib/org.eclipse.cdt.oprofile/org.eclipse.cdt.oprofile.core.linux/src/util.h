/* Utilities and miscellany
   This file written by Keith Seitz <keiths@redhat.com>, but almost
   entirely taken from op_mangling.* in Oprofile, written by John Levon
   and Philippe Elie.
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

#ifndef _UTIL_H
#define _UTIL_H
#include <string>
#include <list>

/* See comments in op_mangling.h. This does pretty much what you
   might expect it to do: get the list of files from the base_dir,
   using the supplied filter. */
void get_sample_file_list (std::list<std::string>& file_list,
			   std::string const& base_dir,
			   std::string const& filter);
#endif // !_UTIL_H
