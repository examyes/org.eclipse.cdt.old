package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/QueuedEvent.java, java-model, eclipse-dev, 20011128
// Version 1.4.1.2 (last modified 11/28/01 16:11:52)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.util.Vector;

/**
 * A simple class to contain an event along with its associated listeners.
 */

class QueuedEvent
{
  QueuedEvent(ModelEvent event, Vector listeners)
  {
    //if (Model.TRACE.EVT && Model.traceInfo())
      //Model.TRACE.evt(4, "Creating QueuedEvent");

    _event = event;
    _listeners = listeners;
  }

  ModelEvent getEvent()
  {
    //if (Model.TRACE.DBG && Model.traceInfo())
      //Model.TRACE.dbg(4, "QueuedEvent.getEvent()");

    return _event;
  }

  Vector getListeners()
  {
    //if (Model.TRACE.DBG && Model.traceInfo())
      //Model.TRACE.dbg(4, "QueuedEvent.getListeners()");

    return _listeners;
  }

  private ModelEvent _event;
  private Vector _listeners;
}
