package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/EngineCapabilitiesChangedEvent.java, java-model, eclipse-dev, 20011128
// Version 1.1.1.2 (last modified 11/28/01 16:12:23)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

/**
 * This kind of event is fired whenever the capabilities of a debug engine
 * change.
 * @see DebugEngineEventListener#engineCapabilitiesChanged
 * @see DebugEngine#getCapabilities
 * @see EngineCapabilities
 */

public class EngineCapabilitiesChangedEvent extends DebugEngineEvent
{
  EngineCapabilitiesChangedEvent(Object source,
                        DebugEngine debugEngine,
                        EngineCapabilities oldCapabilities,
                        EngineCapabilities newCapabilities,
                        int requestCode
                       )
  {
    super(source, debugEngine, requestCode);

    _oldCapabilities = oldCapabilities;
    _newCapabilities = newCapabilities;
  }

  public EngineCapabilities getOldCapabilities()
  {
    return _oldCapabilities;
  }

  public EngineCapabilities getNewCapabilities()
  {
    return _newCapabilities;
  }

  void fire(ModelEventListener listener)
  {
    ((DebugEngineEventListener)listener).engineCapabilitiesChanged(this);
  }

  private EngineCapabilities _oldCapabilities;
  private EngineCapabilities _newCapabilities;
}
