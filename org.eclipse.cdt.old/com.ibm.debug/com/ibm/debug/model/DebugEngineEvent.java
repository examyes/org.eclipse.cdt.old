package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/DebugEngineEvent.java, java-model, eclipse-dev, 20011128
// Version 1.1.1.2 (last modified 11/28/01 16:11:31)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

/**
 * The super class for all debug engine events.
 */

public abstract class DebugEngineEvent extends ModelEvent
{
  DebugEngineEvent(Object source, DebugEngine debugEngine, int requestCode)
  {
    super(source, requestCode);
    _debugEngine = debugEngine;
  }

  public DebugEngine getDebugEngine()
  {
    return _debugEngine;
  }

  private DebugEngine _debugEngine;
}
