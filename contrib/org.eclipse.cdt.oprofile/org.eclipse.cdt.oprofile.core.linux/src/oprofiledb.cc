/* oprofile_db - An Oprofile sample file database wrapper.
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

#include <iostream>
#include <op_sample_file.h>

#include "oprofiledb.h"
#include "stable.h"
#include "sample.h"

oprofile_db::oprofile_db (std::string filename)
  : _filename (filename), _tree (NULL), _symbol_table (NULL)
{
}

oprofile_db::~oprofile_db ()
{
  _close_db ();
  samples_t::iterator i = _samples.begin ();
  while (i != _samples.end ())
    {
      delete SAMPLE (*i);
      ++i;
    }
  _samples.clear ();
}

void
oprofile_db::_open_db (void)
{
  if (_tree == NULL)
    {
      int rc;

      _tree = new samples_odb_t;
      rc = odb_open (_tree, _filename.c_str (), ODB_RDONLY, sizeof (opd_header));
      if (rc != 0)
	{
	  // This shouldn't happen, but let's at least print something out.
	  std::cerr << "Error opening oprofile database: " << strerror (rc)
		    << std::endl;
	}
    }
}

void
oprofile_db::_close_db (void)
{
  if (_tree != NULL)
    {
      odb_close (_tree);
      delete _tree;
    }

  _tree = NULL;
}

static void
samples_odb_travel (samples_odb_t* hash, int start, int end, oprofile_db::callback_t callback, void* data)
{
  odb_node_nr_t node_nr, pos;
  odb_node_t* node = odb_get_iterator (hash, &node_nr);
  for (pos = 0; pos < node_nr; ++pos)
    {
      if (node[pos].key)
	callback (node[pos].key, node[pos].value, data);
    }
}

void
oprofile_db::walk_samples (callback_t callback, void* data)
{
  _open_db ();
  samples_odb_travel (_tree, 0, ~0, callback, data);
  _close_db ();
}

const oprofile_db::samples_t&
oprofile_db::get_samples (symboltable* stable)
{
  _symbol_table = stable;
  walk_samples (_get_samples_callback, this);
  _symbol_table = NULL;
  return _samples;
}

bool
oprofile_db::has_samples (void)
{
  walk_samples (_has_samples_callback, this);
  return _has_samples;
}

long
oprofile_db::get_count (void)
{
  long count = 0;
  walk_samples (_get_count_callback, &count);
  return count;
}

// This is a callback from oprofile when traveling the samples in the sample file.
void
oprofile_db::_get_samples_callback (odb_key_t key, odb_value_t info, void* data)
{
  oprofile_db* odb = static_cast<oprofile_db*> (data);

  symbol* symbol = NULL;
  bfd_vma real_addr;
  if (odb->_symbol_table != NULL)
    symbol = odb->_symbol_table->lookup_vma ((bfd_vma) key, real_addr);

  // Oprofile can have multiple samples for the same VMA, so look in the
  // our map/database and see if the given VMA exists. If it does not exist,
  // add a new Sample. If it does exist, just increment the count of the Sample
  // by INFO.
  samples_t::iterator i = odb->_samples.find ((bfd_vma) key);
  if (i == odb->_samples.end ())
    {
      // new sample
      sample* s = new sample (real_addr, symbol, info);
      odb->_samples.insert (sample_t (key, s));
    }
  else
    {
      // existing sample
      SAMPLE (*i)->incr_count (info);
    }
}

void
oprofile_db::_has_samples_callback (odb_key_t key, odb_value_t info, void* data)
{
  oprofile_db* odb = static_cast<oprofile_db*> (data);
  if (info > 0)
    odb->has_samples (true);
}

void
oprofile_db::_get_count_callback (odb_key_t key, odb_value_t info, void* data)
{
  long* count = static_cast<long*> (data);
  *count += info;
}

const struct opd_header*
oprofile_db::get_header (void)
{
  _open_db ();
  struct opd_header* header = static_cast<struct opd_header*> (_tree->base_memory);

  // We can't do this, but fortunately, it doesn't matter. Opxml
  // immediately destroys it. If this ever changes, we must make a copy!
  //_close_db ();
  return header;
}
