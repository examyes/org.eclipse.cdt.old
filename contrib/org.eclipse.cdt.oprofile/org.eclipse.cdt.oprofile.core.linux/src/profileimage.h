/* profileimage - A class which represents a single image for
   which oprofile has samples (or for which some child dependency
   has samples).
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

#ifndef _PROFILEIMAGE_H
#define _PROFILEIMAGE_H

#include <list>
#include <string>

#include "samplefile.h"

class sample;
class imageheader;

class profileimage
{
 public:
  // Constructor - pass in the samplefile; CANNOT BE NULL.
  profileimage (samplefile* sfile);

  // Destructor
  ~profileimage ();

  // Returns the name of this image; it is the name of the actual binary
  // in which samples were collected.
  std::string get_name (void) const;

  // Returns the image header for this image
  const imageheader* get_header (void);

  // Add the given profileimage as a dependency of this image
  void add_dependency (profileimage* image);

  // Returns a list of all the dependencies of this image
  std::list<profileimage*>* get_dependencies (void) const { return _dependencies; };

  // Returns a list of all the samples in this image, excluding dependencies
  typedef samplefile::samples_t sampleslist_t;
  sampleslist_t get_samples (void) const;

  // Returns the Oprofile samplefile for this image
  samplefile* get_samplefile (void) const { return _samplefile; };

  // Returns the count of all the samples collected in this image, excluding dependencies
  long get_count (void) const;

 private:
  // The samplefile (non-NULL)
  samplefile* _samplefile;

  // List of dependencies
  std::list<profileimage*>* _dependencies;

  // Image header
  imageheader* _header;
};

std::ostream& operator<< (std::ostream& os, profileimage* image);
#endif // !_PROFILEIMAGE_H
