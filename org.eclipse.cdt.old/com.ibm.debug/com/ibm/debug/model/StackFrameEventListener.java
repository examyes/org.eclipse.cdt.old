package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/StackFrameEventListener.java, java-model, eclipse-dev, 20011128
// Version 1.1.1.2 (last modified 11/28/01 16:12:53)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

/** The methods in this interface correspond to events that can occur
 *  within a stack frame at the stack frame level. Client code should implement
 *  this interface if it wants to be informed when these events occur.
 *  An object of this type can be registered as an interested listener
 *  by calling the <b>StackFrame.addEventListener(StackFrameEventListener)</b>
 *  method. Whenever an event occurs which corresponds to one of the methods
 *  in the interface, the StackFrame object will call the appropriate
 *  method for every registered event listener.
 *  @see StackFrame
 */

public interface StackFrameEventListener extends ModelEventListener
{
   /**
    * This method will be called when a stack frame has ended and is just about
    * to be removed from its owning stack object.
    * @param event The event
    */

   public void stackFrameEnded(StackFrameEndedEvent event);

   /**
    * This method will be called when a stack frame's attributes have changed.
    * @param event The event
    */

   public void stackFrameChanged(StackFrameChangedEvent event);

}
