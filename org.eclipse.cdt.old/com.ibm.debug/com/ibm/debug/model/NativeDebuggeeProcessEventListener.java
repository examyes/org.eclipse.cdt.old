package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/NativeDebuggeeProcessEventListener.java, java-model, eclipse-dev, 20011128
// Version 1.9.1.2 (last modified 11/28/01 16:11:10)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

class NativeDebuggeeProcessEventListener extends DebuggeeProcessEventAdapter
{
   /**
    * This method will be called whenever a module is added to the process.
    * @param event The event
    */

   public native void moduleAdded(ModuleAddedEvent event);

   /**
    * This method will be called whenever a thread is added to the process.
    * @param event The event
    */

   public native void threadAdded(ThreadAddedEvent event);

   /**
    * This method will be called whenever a monitored expression is added to
    * the process.
    * @param event The event
    */

   public native void monitoredExpressionAdded(MonitoredExpressionAddedEvent event);

   public native void breakpointAdded(BreakpointAddedEvent event);

   /**
    *  This method will be called whenever the process stops.
    *  @param event The event
    */

   public native void processStopped(ProcessStoppedEvent event);


   public native void processEnded(ProcessEndedEvent event);
}
