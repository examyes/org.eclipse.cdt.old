package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/HostAddedEvent.java, java-model, eclipse-dev, 20011128
// Version 1.1.1.2 (last modified 11/28/01 16:13:53)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

/**
 * Note that the getSource method for a HostAddedEvent object will return
 * the Class object for com.ibm.debug.model.Model.
 */

public class HostAddedEvent extends HostEvent
{
  HostAddedEvent(Object source, Host host, int requestCode)
  {
    super(source, host, requestCode);
  }

  /**
   * @see ModelEvent#fire(ModelEventListener)
   */

  void fire(ModelEventListener listener)
  {
    ((DebugModelEventListener)listener).hostAdded(this);
  }
}
