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

#include "samplefile.h"
#include "sample.h"
#include "stable.h"
#include "xmlfmt.h"

using namespace std;

samplefile::samplefile (string filename)
{
  _st = NULL;
  _db = new oprofile_db (filename);
  _parsed_filename = parse_filename (filename);
}

samplefile::samplefile (parsed_filename* pfname)
  : _parsed_filename (pfname)
{
  _st = NULL;
  if (_parsed_filename != NULL)
    _db = new oprofile_db (_parsed_filename->filename);
}

bool
samplefile::is_dependency (void) const
{
  // If _parsed_filename == NULL, this object is a toplevel image
  // with no samples (all samples are in the dependencies)
  return ((_parsed_filename == NULL)
	  || (_parsed_filename->image != _parsed_filename->lib_image));
}

string
samplefile::get_image (void) const
{
  return _parsed_filename->image;
}

string
samplefile::get_lib_image (void) const
{
  return _parsed_filename->lib_image;
}

string
samplefile::get_name (void) const
{
  return (is_dependency () ? get_lib_image () : get_image ());
}

long
samplefile::get_count (void)
{
  return (has_samplefile () ? _db->get_count () : 0);
}

string
samplefile::get_event (void)
{
  return _parsed_filename->event;
}

const opd_header*
samplefile::get_header (void) const
{
  return (has_samplefile () ? _db->get_header () : NULL);
}

samplefile::~samplefile (void)
{
  if (_parsed_filename != NULL)
    {
      delete _parsed_filename;
      _parsed_filename = NULL;
    }

  if (_db != NULL)
    {
      delete _db;
      _db = NULL;
    }

  if (_st != NULL)
    {
      delete _st;
      _st = NULL;
    }
}


// DO NOT FREE THE RESULT. ~oprofile_db will do it.
const samplefile::samples_t
samplefile::get_samples (void)
{
  samplefile::samples_t samples;

  if (has_samplefile ())
    {
      if (_st == NULL)
	{
	  _st = new symboltable (get_name ().c_str ());
	  _st->read_symbols ();
	}

      samples = _db->get_samples (_st);
    }

  return samples;
}

bool
samplefile::get_debug_info (bfd_vma vma, const char*& func, const char*& file, unsigned int& line)
{
  return (_st == NULL ? false : _st->get_debug_info (vma, func, file, line));
}

// Output header & list of samples
/*
 * <samplefile>/var/lib/oprofile/samples/current/blah/blah/blah</samplefile>
 * SAMPLE (handled by class sample)
 */
ostream&
operator<< (ostream& os, samplefile* sf)
{
  // output the sfile's full pathname (used for fetching debug info)
  os << startt ("samplefile") << sf->get_sample_file_name () << endt;

  // output list of samples
  samplefile::samples_t samples = sf->get_samples ();
  samplefile::samples_t::iterator s;
  for (s = samples.begin (); s != samples.end (); ++s)
    {
      const sample* smpl = samplefile::SAMPLE (*s);
      os << smpl;
    }

  return os;
}
