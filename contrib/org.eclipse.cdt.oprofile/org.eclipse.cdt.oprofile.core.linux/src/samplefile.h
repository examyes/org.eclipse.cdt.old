/* samplefile - A class which represents a samplefile. This class either
   represents a real disk file or a "fake" one (needed in cases where
   Oprofile only collected samples in a dependency, like a library).
   Written by Keith Seitz <keiths@redhat.com>
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

#ifndef _SAMPLEFILE_H
#define _SAMPLEFILE_H
#include <string>
#include <list>
#include <ostream>

#include "oprofiledb.h"
#include "util.h"

class opd_header;
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

  // Constructor -- pass in the filename (must be valid)
  samplefile (std::string filename);

  // Constructor -- pass in the parsed filename (may be NULL when there
  // were no samples collected for the profileimage, i.e., "fake").
  // Memory will be free'd by this class's destructor.
  samplefile (parsed_filename* pfname);

  // Destructor
  ~samplefile (void);

  // Does this sample have a samplefile? This happens when Oprofile has
  // collected samples for an image, but all those samples were collected
  // in libraries and other dependencies.
  bool has_samplefile (void) const { return _parsed_filename != NULL; };

  // Is this samplefile a dependency?
  bool is_dependency (void) const;

  // Returns the filename of this samplefile (or "" if it is "fake")
  std::string get_sample_file_name (void) const
    { return has_samplefile () ? _parsed_filename->filename : ""; };

  // Returns the image name (see parsed_image.image_name)
  std::string get_image (void) const;

  // Returns the library image name (see parsed_image.lib_image)
  std::string get_lib_image (void) const;

  // Returns the logical name of the image in this samplefile, i.e.,
  // the lib_image if this is a dependency or image_name if not
  std::string get_name (void) const;

  // Get count of all samples
  long get_count (void);

  // Returns the event name that was collected in this samplefile
  std::string get_event (void);

  // Returns a list of all the samples in this samplefile
  const samples_t get_samples (void);

  // Returns the header for this samplefile
  const opd_header* get_header (void) const;

  // Returns the debug info for the given VMA.
  bool get_debug_info (bfd_vma vma, const char*& func, const char*& file, unsigned int& line);

 private:
  // The oprofile_db associated with this samplefile
  oprofile_db* _db;

  // The symbol table opened for the executable represented by this samplefile
  symboltable* _st;

  // The fully parsed filanme for this samplefile. May be NULL.
  parsed_filename* _parsed_filename;
};

std::ostream& operator<< (std::ostream& os, samplefile* sf);
#endif // !_SAMPLEFILE_H
