/* samplefile_header - a class which represents the header of a samplefile.
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

#ifndef _SFILEHEADER_H
#define _SFILEHEADER_H
#include <op_types.h>
#include <op_cpu_type.h>
#include <op_sample_file.h>
#include <iostream>

class samplefile_header
{
 public:
  // Constructor - pass in the oprofile header
  samplefile_header (const struct opd_header* header);
  samplefile_header (const samplefile_header* header);

  // Destructor
  ~samplefile_header ();

  // Returns the cpu type
  inline op_cpu get_cpu_type (void) const { return static_cast<op_cpu> (_header->cpu_type); };

  // Returns the counter that this samplefile collected
  inline u32 get_counter (void) const { return _header->ctr; };

  // Returns the event collected
  inline u32 get_event (void) const { return _header->ctr_event; };

  // Returns the count
  inline u32 get_count (void) const { return _header->ctr_count; };

  // Returns the unit mask used during collection
  inline u32 get_unit_mask (void) const { return _header->ctr_um; };

  // Returns an approx cpu speed
  inline double get_cpu_speed (void) const { return _header->cpu_speed; };

  // Was "--separate-lib" used? (Returns 0 or 1)
  inline int get_separate_lib (void) const { return _header->separate_lib_samples; };

  // Was "--separate-kernel" used? (Returns 0 or 1)
  inline int get_separate_kernel (void) const { return _header->separate_kernel_samples; };

  // inserter for this class
  friend std::ostream& operator<< (std::ostream& os, const samplefile_header* sfh);

 private:
  // The oprofile header
  struct opd_header* _header;

  // Did we allocate memory (which needs to be freed) for this header?
  bool _alloced;
};
#endif // !_SFILEHEADER_H
