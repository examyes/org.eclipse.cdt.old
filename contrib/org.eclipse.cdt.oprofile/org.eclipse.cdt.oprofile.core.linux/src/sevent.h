/* sevent (sessionevent) - a class which represents an event collected
   within an oprofile session. There will be one sessionevent for every
   event collected within a session. No two sessionevents with the same
   session name may have the same event name.
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

#ifndef _SEVENT_H
#define _SEVENT_H
#include <string>
#include <list>

#include "profileimage.h"

class samplefile;
class session;

class sessionevent
{
 public:
  // Constructor -- pass in session and event name
  sessionevent (const session* session, std::string event);

  // Desctructor
  ~sessionevent ();

  // Returns the event name
  const std::string get_name (void) const { return _event_name; };

  // Returns the session
  const session* get_session (void) const { return _session; };

  // Adds the samplefile to this sessionevent
  void add_sample_file (samplefile* sfile);

  // Returns the count of all samples in this sessionevent
  long get_count (void) const;

  // Returns a list of the images in the sessionevent
  typedef std::list<profileimage*> profileimages_t;
  profileimages_t* get_images (void) const { return _files; };

 private:
  // The session
  const session* _session;

  // The name of the event
  const std::string _event_name;

  // A list of images in this session
  profileimages_t* _files;
};

std::ostream& operator<< (std::ostream& os, const sessionevent* se);
#endif // _SEVENT_H
