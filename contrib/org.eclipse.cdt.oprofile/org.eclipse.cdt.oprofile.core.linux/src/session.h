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

#ifndef _SESSION_H
#define _SESSION_H
#include <string>
#include <list>
#include <op_cpu_type.h>
#include "samplefile.h"

class opinfo;
class session_counter;

class session
{
 public:
  // Constructor - pass in the name of the session (or "" for the default)
  // and cpu/config information
  session (std::string name, const opinfo* info);

  // Returns a list of the samplefiles in the session with the given counter (-1 = all counters)
  samplefile::samplefilelist_t get_samplefiles (int ctr = -1);

  // Returns a list of all sessions
  typedef std::list<session*> sessionlist_t;
  static sessionlist_t get_sessions (const opinfo& info);

  // Returns the name of this session
  const std::string& get_name (void) const { return _name; };

  // Returns the event that was collected on the given COUNTER or -1 if unknown/error.
  int get_event (int counter) const;

  friend std::ostream& operator<< (std::ostream& os, const session_counter& sc);

 private:
  // The name of this session
  std::string _name;

  // The cpu info
  const opinfo* _info;
};

class session_counter
{
 public:
  session_counter (session* s, int ctr) : _session (s), _ctr (ctr) {};
  friend std::ostream& operator<< (std::ostream& os, const session_counter& sc);

 private:
  session* _session;
  int _ctr;
};
#endif // !_SESSION_H
