package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/ErrorOccurredEngineTerminator.java, java-model, eclipse-dev, 20011128
// Version 1.1.1.2 (last modified 11/28/01 16:14:00)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

class ErrorOccurredEngineTerminator extends DebugEngineEventAdapter
{
   public void errorOccurred(ErrorOccurredEvent event)
   {
     DebugEngine engine;

     try
     {
       engine = (DebugEngine)event.getSource();
     }
     catch (ClassCastException excp)
     {
       return;
     }

     // This shouldn't be necessary but we'll remove this object as an
     // event listener so that we don't end up with infinite recursion:

     engine.removeEventListener(this);

     try
     {
       engine.terminate(DebugEngine.sendReceiveSynchronously);
     }
     catch (java.io.IOException excp)
     {
     }
   }
}
