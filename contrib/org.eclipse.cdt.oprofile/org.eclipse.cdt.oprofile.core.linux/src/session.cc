/* session - a class which represents an oprofile session.
   All sessions occur as directories of the samples directory. The
   so-called "default" (which uses just the samples directory itself)
   is the exception.
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

#include "session.h"
#include <sstream>
#include <iostream>
#include <map>

#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <dirent.h>

#include <op_config.h>
#include <op_sample_file.h>

#include "samplefile.h"
#include "sfheader.h"
#include "opinfo.h"
#include "xmlfmt.h"
#include "util.h"

using namespace std;

session::session(string name, const opinfo* info)
  : _name (name), _info (info)
{
}

// Return for all counters
samplefile::samplefilelist_t
session::get_samplefiles (int counter)
{
  samplefile::samplefilelist_t samplefiles;

  // Always ensure that the session directory ends in a slash
  string session_dir = _info->get_samples_directory () + _name;
  if (*(--session_dir.end ()) != '/')
    session_dir += "/";

  int start_counter = (counter == -1 ? 0 : counter);
  int max_counter = (counter == -1 ? _info->get_nr_counters () : counter + 1);
  list<string> files;

  // Create samplefiles for all the basic executable sample files
  for (int ctr = start_counter; ctr < max_counter; ++ctr)
    {
      list<string> filelist, separates;
      ostringstream ctr_filter;
      ctr_filter << "*#" << ctr;
      get_sample_file_list (filelist, session_dir, ctr_filter.str ());

      /* Filter out any "separate-libs" kind of files -- they are
	 handled by samplefile.

	 One problem, though... It is possible to have separate sample
	 files for executables which do not have a sample file. This can
	 happen, for example, when using the --separate=kernel flag.
	 You get }bin}bash}}}kernel#0 but no }bin}bash#0.

	 To deal with this, we pass through the file list twice. Once to
	 insert all the "main" samplefiles and once to insert fake/empty
	 "main" samplefiles for any separates that may not have parents. */

      list<string>::iterator i;
      for (i = filelist.begin (); i != filelist.end (); ++i)
	{
	  // First construct the full, disk-image filename of the samplefile
	  ostringstream file;
	  file << (*i) << "#" << ctr;
	  string filename = file.str ();

	  // Now add all "main" samplefiles
	  if (samplefile::is_separate_samplefile (filename))
	    separates.push_back (filename);
	  else
	    {
	      files.push_back ((*i));
	      samplefiles.push_back (new samplefile (ctr, session_dir, (*i)));
	    }
	}

      // Now loop through and add any "fake" samplefiles
      filelist.clear ();
      for (i = separates.begin (); i != separates.end (); ++i)
	{
	  string parent;
	  samplefile::get_parent_name (parent, (*i));

	  if ((find (filelist.begin (), filelist.end (), parent)
	       == filelist.end ())
	      && (find (files.begin (), files.end (), parent)
		  == files.end ()))
	    {
	      filelist.push_back (parent);
	      samplefiles.push_back (new samplefile (ctr, session_dir,
						     parent, true));
	    }
	}
    }

  return samplefiles;
}

int
session::get_event (int counter) const
{
  // Always ensure that the session directory ends in a slash
  string session_dir = _info->get_samples_directory () + _name;
  if (*(--session_dir.end ()) != '/')
    session_dir += "/";

  list<string> filelist;
  ostringstream ctr_filter;
  ctr_filter << "*#" << counter;
  get_sample_file_list (filelist, session_dir, ctr_filter.str ());

  // Grab the first sample file and get the event stored in the header
  if (!filelist.empty ())
    {
      samplefile sfile (counter, session_dir, filelist.front ());
      int event = sfile.get_header ()->get_event ();
      return event;
    }

  return -1;
}

session::sessionlist_t
session::get_sessions (const opinfo& info)
{
  sessionlist_t sessions;

  struct stat sbuf;
  int rc = stat (info.get_samples_directory ().c_str (), &sbuf);
  if (rc < 0)
    {
      cerr << "cannot stat samples directory (" << info.get_samples_directory () << ")"
	   << endl;
      return sessions;
    }
  else if (S_ISDIR (sbuf.st_mode) == 0)
    {
      cerr << "samples directory (" << info.get_samples_directory ()
	   << ") is not a directory" << endl;
      return sessions;
    }

  DIR* dirp = opendir (info.get_samples_directory ().c_str ());
  if (dirp == NULL)
    {
      cerr << "cannot read samples directory (" << info.get_samples_directory () << ")"
	   << endl;
      return sessions;
    }

  struct dirent* dir;
  while ((dir = readdir (dirp)) != NULL)
    {
      if (strcmp (dir->d_name, ".") == 0)
	{
	  // Special case: add one for the "default"
	  sessions.push_back (new session ("", &info));
	}
      else if (strcmp (dir->d_name, "..") != 0)
	{
	  // Okay, we're going to cheat.. Instead of stat'ing every file,
	  // we simply check if it starts with the mangle character...
	  if (dir->d_name[0] != OPD_MANGLE_CHAR)
	    {
	      string name (dir->d_name);
	      sessions.push_back (new session (name, &info));
	    }
	}
    }

  return sessions;
}

/*
 * <session name="foo" counter="0">
 *   <has_samples>1</has_samples>
 *   <event>112</event>
 * </session>
 */
ostream&
operator<< (ostream& os, const session_counter& sc)
{
  samplefile::samplefilelist_t lst = sc._session->get_samplefiles (sc._ctr);
  samplefile::samplefilelist_t::iterator i;

  long count = 0;
  for (i = lst.begin (); i != lst.end (); ++i)
    {
      samplefile* sfile = *i;
      count += sfile->get_count ();

      /* Don't forget to add in separates..
	 You're wondering why samplefile::get_count doesn't do this...
	 Simple: samplefile represents the real, disk image. Getting the
	 count for it simply gets the count specifically for that file.
	 It is in Eclipse where the abstraction layer is raised. Getting
	 the count of a samplefile there _will_ include the separates. */
      samplefile::samplefilelist_t separates
	= sfile->get_separate_sample_files ();
      samplefile::samplefilelist_t::iterator j;
      for (j = separates.begin (); j != separates.end (); ++j)
	count += (*j)->get_count ();
    }

  ostringstream counter;
  counter << sc._ctr;
  os << startt ("session") << attrt ("name", sc._session->_name)
     << attrt ("counter", counter.str ())
     << startt ("count") << count << endt
     << endt;
  samplefile::free_samplefiles (lst);
  return os;
}
