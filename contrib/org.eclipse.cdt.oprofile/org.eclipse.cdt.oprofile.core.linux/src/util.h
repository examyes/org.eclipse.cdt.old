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

#ifndef _UTIL_H
#define _UTIL_H
#include <string>
#include <list>

/* See comments in op_mangling.h. This does pretty much what you
   might expect it to do: get the list of files from the base_dir. */
void get_sample_file_list (std::list<std::string>& file_list,
			   std::string const& base_dir);

/* Stolen from parse_filename.h in oprofile */
/**
 * a convenience class to store result of parse_filename()
 */
struct parsed_filename
{
        std::string image;
        std::string lib_image;
        /// destination image for call graph file, empty if this sample
        /// file is not a callgraph file.
        std::string cg_image;
        std::string event;
        std::string count;
        std::string unitmask;
        std::string tgid;
        std::string tid;
        std::string cpu;
                                                                                       
        /**
         * the original sample filename from which the
         * above components are built
         */
        std::string filename;
};

/**
 * parse a sample filename
 * @param filename in: a sample filename
 *
 * filename is split into constituent parts, the lib_image is optional
 * and can be empty on successfull call. All other error are fatal.
 * Filenames are encoded as according to PP:3.19 to PP:3.25
 *
 * all errors throw an std::invalid_argument exception
 */
// MUST BE FREE'D!
parsed_filename* parse_filename(std::string const & filename);
#endif // !_UTIL_H
