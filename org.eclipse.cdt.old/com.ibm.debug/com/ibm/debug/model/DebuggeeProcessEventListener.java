package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/DebuggeeProcessEventListener.java, java-model, eclipse-dev, 20011128
// Version 1.12.1.2 (last modified 11/28/01 16:10:58)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

/** The methods in this interface correspond to events that can occur
 *  within a debuggee at the process level. Client code should implement
 *  this interface if it wants to be informed when these events occur.
 *  An object of this type can be registered as an interested listener
 *  by calling the <b>DebuggeeProcess.add(DebuggeeProcessEventListener)</b>
 *  method. Whenever an event occurs which corresponds to one of the methods
 *  in the interface, the DebuggeeProcess object will call the appropriate
 *  method for every registered event listener.
 *  @see DebuggeeProcess
 *  @see DebuggeeThread
 *  @see Module
 */

public interface DebuggeeProcessEventListener extends ModelEventListener
{
   /**
    * This method will be called whenever a module is added to the process.
    * @param event The event
    */

   public void moduleAdded(ModuleAddedEvent event);

   /**
    * This method will be called whenever a thread is added to the process.
    * @param event The event
    */

   public void threadAdded(ThreadAddedEvent event);

   /**
    * This method will be called whenever a monitored expression is added to
    * the process.
    * @param event The event
    */

   public void monitoredExpressionAdded(MonitoredExpressionAddedEvent event);

   /**
    * This method will be called whenever a breakpoint is added to the process.
    * @param event The event
    */

   public void breakpointAdded(BreakpointAddedEvent event);

   /**
    * This method will be called whenever monitored storage is added to the process.
    * @param event The event
    */

   public void storageAdded(StorageAddedEvent event);

   /**
    *  This method will be called whenever the process stops.
    *  @param event The event
    */

   public void processStopped(ProcessStoppedEvent event);

   public void processEnded(ProcessEndedEvent event);

   /**
    *  This method will be called whenever an exception occurs.
    *  @param event The event
    */

   public void exceptionRaised(ExceptionRaisedEvent event);
}
