/* imageheader - a class which represents the "header" information for a given
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

#include "imageheader.h"
#include "xmlfmt.h"

using namespace std;

imageheader::imageheader (const samplefile* sfile)
  : _sfile (sfile)
{
}

ostream&
operator<< (ostream& os, const imageheader* ihdr)
{
  return os << startt ("header")
	    << startt ("cpu_type") << ihdr->get_cpu () << endt
	    << startt ("count") << ihdr->get_count () << endt
            << startt ("event") << ihdr->get_event () << endt
	    << startt ("unit-mask") << ihdr->get_unit_mask () << endt
	    << startt ("cpu-speed") << ihdr->get_cpu_speed () << endt
	    << endt;
}

