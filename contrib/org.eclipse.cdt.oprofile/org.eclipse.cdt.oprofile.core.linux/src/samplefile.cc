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

#include <list>
#include <sstream>

#include "samplefile.h"
#include "sfheader.h"
#include "stable.h"
#include "xmlfmt.h"
#include "sample.h"
#include "util.h"

using namespace std;

samplefile::samplefile (int ctr, string dir, string basename, bool fake)
  : _dir (dir), _basename (basename), _st (NULL), _ctr (ctr),
    _header (NULL), _is_fake (fake)
{
  string name;
  get_sample_file_name (name);
  if (_is_fake)
    _db = NULL;
  else
    _db = new oprofile_db (_dir + name);
}

samplefile::samplefile (string filename)
  : _st (NULL), _header (NULL), _is_fake (false)
{
  string::size_type slash = filename.rfind ('/');
  string::size_type hash = filename.rfind ('#');
  _dir = filename.substr (0, slash+1);
  _basename = filename.substr (slash+1, hash - slash -1);
  _ctr = strtol (filename.substr (hash+1, string::npos).c_str (), NULL, 10);
  _db = new oprofile_db (filename);
}

samplefile::~samplefile (void)
{
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

  if (_header != NULL)
    {
      delete _header;
      _header = NULL;
    }
}

samplefile_header*
samplefile::get_header (void)
{
  if (_header == NULL)
    {
      if (_is_fake)
	{
	  list<samplefile*> addl = get_separate_sample_files ();
	  samplefile* sfile = *(addl.begin ());
	  _header = new samplefile_header (sfile->get_header ());
	  free_samplefiles (addl);
	}
      else
	_header = new samplefile_header (_db->get_header ());      
    }

  return _header;
}

string&
samplefile::get_sample_file_name (string& name) const
{
  ostringstream fname;
  fname << _basename << "#" << _ctr;
  name.assign (fname.str ());
  return name;
}

samplefile::samplefilelist_t
samplefile::get_separate_sample_files (void) const
{
  samplefilelist_t files;

  ostringstream filename;
  filename << _basename << "}}}*#" << _ctr;

  list<string> filelist;
  get_sample_file_list (filelist, _dir, filename.str ());

  list<string>::iterator i;
  for (i = filelist.begin (); i != filelist.end (); ++i)
    files.push_back (new samplefile (_ctr, _dir, (*i)));

  return files;
}

void
samplefile::free_samplefiles (samplefilelist_t& list)
{
  samplefilelist_t::iterator i;
  for (i = list.begin (); i != list.end (); ++i)
    delete *i;
  list.clear ();
}

// DO NOT FREE THE RESULT. ~oprofile_db will do it.
const samplefile::samples_t
samplefile::get_samples (void)
{
  samplefile::samples_t samples;

  if (!_is_fake)
    {
      string executable;
      demangle_sample_filename (_basename, executable);

      if (_st == NULL)
	{
	  _st = new symboltable (executable.c_str ());
	  _st->read_symbols ();
	}

      samples = _db->get_samples (_st);
    }

  return samples;
}

long
samplefile::get_count (void)
{
  return (_is_fake ? 0 : _db->get_count ());
}

bool
samplefile::has_samples (void)
{
  return (_is_fake ? true : _db->has_samples ());
}

bool
samplefile::is_separate_samplefile (void) const
{
  return is_separate_samplefile (_basename);
}

bool
samplefile::is_separate_samplefile (const string& file)
{
  return (file.find ("}}}") != string::npos);
}

void
samplefile::demangle_sample_filename (string sample_file,
				      string& demangled_name)
{
  demangled_name = sample_file;

  // If it's a separate file, erase the parent's name from the filename
  size_t pos = demangled_name.find ("}}}");
  if (pos != string::npos)
      demangled_name.erase (0, pos + 2);

  // Replace all occurrences of the mangling character
  pos = demangled_name.find_first_of (OPD_MANGLE_CHAR);
  if (pos != string::npos)
    {
      demangled_name.erase (0, pos);
      replace (demangled_name.begin (), demangled_name.end (), OPD_MANGLE_CHAR, '/');
    }
  pos = demangled_name.find_last_of ('#');
  if (pos != string::npos)
    demangled_name.erase (pos, string::npos);
}

void
samplefile::get_parent_name (string& result, const string& separate)
{
  result = separate;

  size_t pos;

  pos = result.find("}}}");
  if (pos != string::npos)
    result.erase (pos, result.length ());

  pos = result.find_last_of ('#');
  if (pos != string::npos)
    result.erase (pos, string::npos);
}

bool
samplefile::get_debug_info (bfd_vma vma, const char*& func, const char*& file, unsigned int& line)
{
  return (_st == NULL ? false : _st->get_debug_info (vma, func, file, line));
}

ostream&
operator<< (ostream& os, samplefile* sf)
{
  string demangled_name;
  samplefile::demangle_sample_filename (sf->_basename, demangled_name);

  // output header & demangled name
  string name;
  os << startt ("samplefile")
     << attrt ("name", sf->_dir + sf->get_sample_file_name (name))
     << startt ("demangled-name") << demangled_name << endt
     << sf->get_header ();

  // output list of samples
  samplefile::samples_t samples = sf->get_samples ();
  samplefile::samples_t::iterator s;
  for (s = samples.begin (); s != samples.end (); ++s)
    {
      const sample* smpl = samplefile::SAMPLE (*s);
      os << smpl;
    }

  // output additional samples files
  list<samplefile*>::iterator i;
  list<samplefile*> addl = sf->get_separate_sample_files ();
  for (i = addl.begin (); i != addl.end (); ++i)
    os << (*i);
  samplefile::free_samplefiles (addl);

  return os << endt;
}

