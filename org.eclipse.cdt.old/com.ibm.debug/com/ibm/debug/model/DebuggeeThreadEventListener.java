package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/DebuggeeThreadEventListener.java, java-model, eclipse-dev, 20011128
// Version 1.9.1.2 (last modified 11/28/01 16:11:00)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

/** The methods in this interface correspond to events that can occur
 *  within a debuggee at the thread level. Client code should implement
 *  this interface if it wants to be informed when these events occur.
 *  An object of this type can be registered as an interested listener
 *  by calling the <b>DebuggeeThread.addEventListener(DebuggeeThreadEventListener)</b>
 *  method. Whenever an event occurs which corresponds to one of the methods
 *  in the interface, the DebuggeeThread object will call the appropriate
 *  method for every registered event listener.
 *  @see DebuggeeThread
 */

public interface DebuggeeThreadEventListener extends ModelEventListener
{
   /**
    * This method will be called when a thread has ended and is just about
    * to be removed from its owning process object.
    * @param event The event
    */

   public void threadEnded(ThreadEndedEvent event);

   /**
    * This method will be called when a thread's attributes have changed.
    * This includes changes in the thread's state, debug state, priority,
    * and/or execution point.
    * @param event The event
    */

   public void threadChanged(ThreadChangedEvent event);

   /**
    * This method will be called whenever a stack is added to the thread.
    * @param event The event
    */

   public void stackAdded (StackAddedEvent event);

   /**
    * This method will be called when a new local monitor is added to the
    * thread.
    * @param event The event for adding the local monitor
    */
   public void localExpressionsMonitorAdded(LocalMonitoredExpressionsAddedEvent event);

   /**
    * This method will be called when a new monitored register group is added to the
    * thread.
    * @param event The event for adding a new monitored register group
    */
   public void monitoredRegisterGroupAdded (MonitoredRegisterGroupAddedEvent event);
}
