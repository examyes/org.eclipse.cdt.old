package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/StackEventListener.java, java-model, eclipse-dev, 20011128
// Version 1.3.1.2 (last modified 11/28/01 16:12:29)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

/** The methods in this interface correspond to events that can occur
 *  within a stack at the stack level. Client code should implement
 *  this interface if it wants to be informed when these events occur.
 *  An object of this type can be registered as an interested listener
 *  by calling the <b>Stack.addEventListener(StackEventListener)</b>
 *  method. Whenever an event occurs which corresponds to one of the methods
 *  in the interface, the Stack object will call the appropriate
 *  method for every registered event listener.
 *  @see Stack
 */

public interface StackEventListener extends ModelEventListener
{
   /**
    * This method will be called when a stack has ended and is just about
    * to be removed from its owning thread object.
    * @param event The event
    */

   public void stackEnded(StackEndedEvent event);

   /**
    * This method will be called when a new stack frame is added to the stack.
    * @param event The event
    */

   public void stackFrameAdded(StackFrameAddedEvent event);

}
