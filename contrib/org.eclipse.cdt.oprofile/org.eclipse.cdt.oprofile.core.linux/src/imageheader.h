/* imageheader - a class which represents the "header" info for a given
   image.
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

#ifndef _IMAGEHEADER_H
#define _IMAGEHEADER_H
#include <op_types.h>
#include <op_cpu_type.h>
#include <op_sample_file.h>
#include <ostream>

class imageheader
{
 public:
  // Constructor - pass in the oprofile header
  imageheader (const opd_header* header);

  // Returns the cpu type
  inline op_cpu get_cpu_type (void) const { return static_cast<op_cpu> (_header->cpu_type); };

  // Returns the event collected
  inline u32 get_event (void) const { return _header->ctr_event; };

  // Returns the count
  inline u32 get_count (void) const { return _header->ctr_count; };

  // Returns the unit mask used during collection
  inline u32 get_unit_mask (void) const { return _header->ctr_um; };

  // Returns an approx cpu speed
  inline double get_cpu_speed (void) const { return _header->cpu_speed; };

 private:
  // The oprofile header
  const opd_header* _header;
};

std::ostream& operator<< (std::ostream& os, const imageheader* ihdr);
#endif // !_SFILEHEADER_H
