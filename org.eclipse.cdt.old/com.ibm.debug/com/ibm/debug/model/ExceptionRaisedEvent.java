package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/ExceptionRaisedEvent.java, java-model, eclipse-dev, 20011128
// Version 1.1.1.2 (last modified 11/28/01 16:13:21)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

public class ExceptionRaisedEvent extends ProcessStoppedEvent
{
  ExceptionRaisedEvent(Object source,
                      ProcessStopInfo stopInfo,
                      String exceptionMsg,
                      int requestCode)
  {
    super(source, stopInfo, requestCode);

    _exceptionMsg = exceptionMsg;
  }

  /**
   * Returns the exception message.
   */

  public String exceptionMsg()
  {
    return _exceptionMsg;
  }


  void fire(ModelEventListener listener)
  {
    ((DebuggeeProcessEventListener)listener).exceptionRaised(this);
  }

  private String _exceptionMsg;
}
