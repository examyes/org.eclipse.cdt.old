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
   Boston, MA 02111-1307, USA. */

#ifndef _OPROFILEDB_H
#define _OPROFILEDB_H

#include <map>
#include <string>
#include <bfd.h>
#include <odb_hash.h>

class sample;
class symboltable;

// A class which represents an oprofile sample database. This is much
// lower-level stuff than class samplefile.
class oprofile_db
{
 public:
  typedef void (*callback_t)(odb_key_t, odb_value_t, void*);

  // Creates an oprofile_db from the given sample file
  oprofile_db (std::string sample_file);
  ~oprofile_db ();

  // Function object used to compare VMA for sorting
  struct ltvma
  {
    bool operator() (const bfd_vma a, const bfd_vma b) const
    { return (a < b); }
  };

  // The type of the sample database returned be get_samples
  typedef std::map<const bfd_vma, sample*, ltvma> samples_t;

  // The type of one sample in the database
  typedef std::pair<const bfd_vma, sample*> sample_t;

  // Macro to fetch the sample from the sample_t.
  static inline sample* SAMPLE (sample_t sample) { return sample.second; }

  // Retrieves the sample database using STABLE as a symbol table (may be NULL)
  const samples_t& get_samples (symboltable* stable);

  // Set/query whether the db has any samples in it 
  bool has_samples (void);
  void has_samples (bool yesno) { _has_samples = yesno; };

  // Get the total number of samples in this samplefile
  long get_count (void);

  // Retrieves the oprofile header of this sample file.
  const struct opd_header* get_header (void);

  // Walks the samples with the given callback
  void walk_samples (callback_t callback, void* data);

 protected:
  // Callbacks for walking oprofile sample database
  static void _get_samples_callback (odb_key_t key, odb_value_t info, void* data);
  static void _has_samples_callback (odb_key_t key, odb_value_t info, void* data);
  static void _get_count_callback (odb_key_t key, odb_value_t info, void* data);

  // Makes sure the oprofile sample file is open
  void _open_db (void);

  // Closes the oprofile sample file
  void _close_db (void);

  // The sample file
  std::string _filename;

  // The oprofile sample database for the file
  samples_odb_t* _tree;

  // A map of all the samples
  samples_t _samples;

  // The symbol table used to resolve VMA into symbols
  symboltable* _symbol_table;

  bool _has_samples;
  callback_t _callback;
};
#endif // !_OPROFILEDB_H
