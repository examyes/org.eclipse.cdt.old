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

#include "sfheader.h"
#include "xmlfmt.h"

using namespace std;

samplefile_header::samplefile_header (const struct opd_header* header)
  : _header (const_cast<struct opd_header*> (header)), _alloced (false)
{
}

samplefile_header::samplefile_header (const samplefile_header* hdr)
{
  struct opd_header* header = hdr->_header;
  _header = new struct opd_header;
  _header->cpu_type = header->cpu_type;
  _header->ctr = header->ctr;
  _header->ctr_event = header->ctr_event;
  _header->ctr_count = header->ctr_count;
  _header->ctr_um = header->ctr_um;
  _header->cpu_speed = header->cpu_speed;
  _header->separate_lib_samples = header->separate_lib_samples;
  _header->separate_kernel_samples = header->separate_kernel_samples;
  _alloced = true;
}

samplefile_header::~samplefile_header ()
{
  if (_alloced)
    delete _header;
}

ostream&
operator<< (ostream& os, const samplefile_header* sfh)
{
  return os << startt ("header")
	    << startt ("cpu_type") << op_get_cpu_type_str (sfh->get_cpu_type ()) << endt
	    << startt ("counter") << sfh->get_counter () << endt
	    << startt ("count") << sfh->get_count () << endt
            << startt ("event") << sfh->get_event () << endt
	    << startt ("unit-mask") << sfh->get_unit_mask () << endt
	    << startt ("cpu-speed") << sfh->get_cpu_speed () << endt
	    << startt ("separate-lib") << sfh->get_separate_lib () << endt
	    << startt ("separate-kernel") << sfh->get_separate_kernel () << endt
	    << endt;
}

