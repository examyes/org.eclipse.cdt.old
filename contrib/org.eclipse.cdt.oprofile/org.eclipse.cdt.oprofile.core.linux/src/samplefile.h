/* samplefile - A class which represents a samplefile. This class is more
   user-visible functionality. The nitty-gritty details of parsing oprofile
   sample files is handled by oprofile_db.
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

#ifndef _SAMPLEFILE_H
#define _SAMPLEFILE_H
#include <string>
#include <list>
#include <ostream>

#include "oprofiledb.h"

class samplefile_header;
class symboltable;

class samplefile
{
 public:
  // The type of a list of samples. STL container with iterators.
  typedef oprofile_db::samples_t samples_t;
  
  // The type of a sample. Also an STL container. Use SAMPLE to get at
  // actual sample.
  typedef oprofile_db::sample_t sample_t;

  // The type of a list of samplefiles
  typedef std::list<samplefile*> samplefilelist_t;

  // Convenience function to return the sample associated with
  // a sample_t
  static inline sample* SAMPLE (sample_t sample)
    { return oprofile_db::SAMPLE (sample); };

  // Returns the demangled name of the given sample filename.
  // "/foo/bar/baz" for "}foo}bar}baz#0" 
  // "/lib/foo.so" for "}foo}bar}baz}}}lib}foo.so"
  static void demangle_sample_filename (std::string sample_file,
					std::string& demangled_name);

  // Does the given FILE represent a separate sample file?
  static bool is_separate_samplefile (const std::string& file);

  // Get the parent name of a separate sample file
  static void get_parent_name (std::string& result, const std::string& sep_file);

  // Constructor
  samplefile (int ctr, std::string dir, std::string basename, bool fake = false);
  samplefile (std::string filename);

  // Destructor
  ~samplefile (void);

  // Returns the disk filename for this samplefile. Uses FILENAME
  // for storage.
  std::string& get_sample_file_name (std::string& filename) const;

  // Returns the header for this samplefile
  samplefile_header* get_header (void);

  // Returns a list of "separate" sample files (i.e., from oprofile's
  // --separate-{lib,kernel} parameter)
  samplefilelist_t get_separate_sample_files (void) const;

  // Returns a list of all the samples in this samplefile
  const samples_t get_samples (void);

  // Get count of all samples
  long get_count (void);

  // Does this samplefile have any samples?
  bool has_samples (void);

  // Returns the debug info for the given VMA.
  bool get_debug_info (bfd_vma vma, const char*& func, const char*& file, unsigned int& line);

  // Frees the result of get_samples
  static void free_samplefiles (samplefilelist_t& list);

  // Is this samplefile a "separate" samplefile?
  bool is_separate_samplefile (void) const;

  // Insert operator for this class
  friend std::ostream& operator<< (std::ostream& os, samplefile* sf);

 private:
  // The samples directory where this samplefile is located
  std::string _dir;

  // The base filename of this samplefile (no counter)
  std::string _basename;

  // The oprofile_db associated with this samplefile
  oprofile_db* _db;

  // The symbol table opened for the executable represented by this samplefile
  symboltable* _st;

  // The counter this samplefile collected
  int _ctr;

  // The header for this samplefile
  samplefile_header* _header;

  // Is this a "fake" samplefile, i.e., it has no disk-image associated with it
  // (This can happen when executables have separate sample files, but no
  //  main sample file.)
  bool _is_fake;
};
#endif // !_SAMPLEFILE_H
