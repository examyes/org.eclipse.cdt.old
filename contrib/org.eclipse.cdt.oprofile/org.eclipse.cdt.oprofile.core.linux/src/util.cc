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

#include "util.h"

#include <op_sample_file.h>

using namespace std;

// Forward declaration
static void strip_counter_suffix (string& name);

// From libutil++
extern bool create_file_list (list<string>& file_list, string const& base_dir,
			      string const& filter = "*", bool recursive = false);
static void
strip_counter_suffix (string& name)
{
  size_t pos = name.find_last_of ('#');
  name = name.substr (0, pos);
}

void
get_sample_file_list (list<string>& file_list, string const& base_dir,
		      string const& filter)
{
  file_list.clear ();

  list<string> files;
  bool ok = create_file_list (files, base_dir, filter);
  if (ok)
    {
      list<string>::iterator i;
      for (i = files.begin (); i != files.end (); ++i)
	{
	  // Paranoia? Avoid any non-sample file files.
	  if (i->find_first_of (OPD_MANGLE_CHAR) != string::npos)
	    {
	      string filename = *i;
	      strip_counter_suffix (filename);
	      
	      // Only allow unique filenames into the final list.
	      // (This can happen because we can have multiple counters
	      // for any given sample file.)
	      if (find (file_list.begin (), file_list.end (), filename)
		  == file_list.end ())
		file_list.push_back (filename);
	    }
	}
    }
}
