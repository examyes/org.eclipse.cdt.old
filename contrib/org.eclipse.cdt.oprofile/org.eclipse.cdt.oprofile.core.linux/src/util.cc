/* Utilities and miscellany
   This file written by Keith Seitz <keiths@redhat.com>, but almost
   entirely taken from Oprofile, written by John Levon, Philippe Elie,
   and others.
   Copyright 2004 Red Hat, Inc.

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
#include <vector>

using namespace std;

// From libutil++
extern bool create_file_list (list<string>& file_list, string const& base_dir,
			      string const& filter = "*", bool recursive = true);
extern vector<string> separate_token (string const & str, char sep);

void
get_sample_file_list (list<string>& file_list, string const& base_dir)
{
  file_list.clear ();

  list<string> files;
  bool ok = create_file_list (files, base_dir);
  if (ok)
    {
      list<string>::iterator i;
      for (i = files.begin (); i != files.end (); ++i)
	{
	  // Only allow unique filenames into the final list.
	  // (This can happen because we can have multiple counters
	  // for any given sample file.)
	  if (find (file_list.begin (), file_list.end (), *i)
	      == file_list.end ())
	    file_list.push_back (*i);
	}
    }
}

/* Stolen from parse_filename.cpp in oprofile */
// PP:3.19 event_name.count.unitmask.tgid.tid.cpu
parsed_filename* parse_event_spec(string const & event_spec)
{
  typedef vector<string> parts_type;
  typedef parts_type::size_type size_type;

  size_type const nr_parts = 6;
                                                                                       
  parts_type parts = separate_token(event_spec, '.');
                                                                                       
  if (parts.size() != nr_parts) {
    return NULL;
  }
  
  for (size_type i = 0; i < nr_parts ; ++i) {
    if (parts[i].empty()) {
      return NULL;
    }
  }
                                                                                         
  size_type i = 0;
  parsed_filename* result = new parsed_filename;
  result->event = parts[i++];
  result->count = parts[i++];
  result->unitmask = parts[i++];
  result->tgid = parts[i++];
  result->tid = parts[i++];
  result->cpu = parts[i++];
  
  return result;
}

/**
 * @param component  path component
 *
 * remove from path_component all directory left to {root} or {kern}
 */
static void
remove_base_dir(vector<string> & path)
{
        vector<string>::iterator it;
        for (it = path.begin(); it != path.end(); ++it) {
                if (*it == "{root}" || *it == "{kern}")
                        break;
        }
                                                                                       
        path.erase(path.begin(), it);
}

/*
 *  valid filename are:
 *
 * {kern}/name/event_spec
 * {root}/path/to/bin/event_spec
 * {root}/path/to/bin/{dep}/{root}/path/to/bin/event_spec
 * {root}/path/to/bin/{dep}/{kern}/name/event_spec
 *
 * where /name/ denote a unique path component
 */
// MUST BE FREE'D!
parsed_filename* parse_filename(string const & filename)
{
  string::size_type pos = filename.find_last_of('/');
  if (pos == string::npos) {
    return NULL;
  }
  string event_spec = filename.substr(pos + 1);
  string filename_spec = filename.substr(0, pos);
                                                                                       
  parsed_filename* result = parse_event_spec(event_spec);
  if (result == NULL)
    return NULL;

  result->filename = filename;
                                                                                       
  vector<string> path = separate_token(filename_spec, '/');
                                                                                       
  remove_base_dir(path);

  // pp_interface PP:3.19 to PP:3.23 path must start either with {root}
  // or {kern} and we must found at least 2 component, remove_base_dir()
  // return an empty path if {root} or {kern} are not found
  if (path.size() < 2) {
    delete result;
    return NULL;
  }
                                                                                       
  size_t i;
  for (i = 1 ; i < path.size() ; ++i) {
    if (path[i] == "{dep}")
      break;
                                                                                       
    result->image += "/" + path[i];
  }
                                                                                       
  if (i == path.size()) {
    delete result;
    return NULL;
  }
                                                                                       
  // skip "{dep}"
  ++i;
                                                                                       
  // PP:3.19 {dep}/ must be followed by {kern}/ or {root}/
  if (path[i] != "{kern}" && path[i] != "{root}") {
    delete result;
    return NULL;
  }
                                                                                       
  // skip "{root}" or "{kern}"
  ++i;
                                                                                       
  for (; i < path.size(); ++i) {
    if (path[i] == "{cg}")
      break;
    result->lib_image += "/" + path[i];
  }
                                                                                       
  if (i != path.size()) {
    // skip "{cg}"
    ++i;
    if (i == path.size() ||
	(path[i] != "{kern}" && path[i] != "{root}")) {
      delete result;
      return NULL;
    }
    // skip "{root}" or "{kern}"
    ++i;
  }
  for (; i < path.size(); ++i) {
    result->cg_image += "/" + path[i];
  }
                                                                                       
  return result;
}
