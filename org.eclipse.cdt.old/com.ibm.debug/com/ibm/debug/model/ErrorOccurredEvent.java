package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/ErrorOccurredEvent.java, java-model, eclipse-dev, 20011128
// Version 1.2.1.2 (last modified 11/28/01 16:11:30)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

public class ErrorOccurredEvent extends ModelEvent
{
   ErrorOccurredEvent(Object source,
                      int returnCode,
                      String message,
                      int requestCode)
   {
     super(source, requestCode);

     _returnCode = returnCode;
     _message = message;
   }

   /** Values for the return code correspond to constants in
    *  class com.ibm.debug.epdc.EPDC e.g. EPDC.ExecRc_ProgName
    *  @see com.ibm.debug.epdc.EPDC#ExecRc_OK
    */

   public int getReturnCode()
   {
     return _returnCode;
   }

   public String getMessage()
   {
     return _message;
   }

  /**
   * @see ModelEvent#fire(ModelEventListener)
   */

  void fire(ModelEventListener listener)
  {
    ((DebugEngineEventListener)listener).errorOccurred(this);
  }

   private int _returnCode;
   private String _message;
}
