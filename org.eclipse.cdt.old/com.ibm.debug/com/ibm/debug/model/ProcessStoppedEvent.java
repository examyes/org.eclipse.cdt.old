package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/ProcessStoppedEvent.java, java-model, eclipse-dev, 20011128
// Version 1.1.1.2 (last modified 11/28/01 16:11:43)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

public class ProcessStoppedEvent extends ProcessEvent
{
  ProcessStoppedEvent(Object source,
                      ProcessStopInfo stopInfo,
                      int requestCode)
  {
    super(source, stopInfo.getProcess(), requestCode);

    _stopInfo = stopInfo;
  }

  /**
   * Determine which thread caused the process to stop and why.
   */

  public ProcessStopInfo getProcessStopInfo()
  {
    return _stopInfo;
  }


  void fire(ModelEventListener listener)
  {
    ((DebuggeeProcessEventListener)listener).processStopped(this);
  }

  private ProcessStopInfo _stopInfo;
}
