package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/DebugEngineEventListener.java, java-model, eclipse-dev, 20011128
// Version 1.13.1.2 (last modified 11/28/01 16:10:56)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

public interface DebugEngineEventListener extends ModelEventListener
{

   /**
    * This method will be called whenever a clear text 'CommandLog' message
    * (command) has been sent to a Debug Engine.  Currently 'CommandLog'
    * messages are only recognized and processed by the GdbPicl Engine.
    * The 'CommandLogResponse' contains the text response of the Engine.
    * @param event The event
    */
   public void commandLogResponse(DebugEngineCommandLogResponseEvent event);

   /**
    * This method will be called whenever a debug engine has been
    * terminated. The DebugEngine object will be marked as having been
    * deleted and client code should no longer try and use the object
    * for debugging. A debug engine is typically terminated in response to a
    * DebugEngine.terminate request, but this event can also be fired for
    * other reasons e.g. if, after letting the debuggee run, it runs to
    * completion, the debug engine may tell us that not only has the process
    * been terminated but also that the debug engine itself has been terminated
    * and can no longer be used for debugging. In this case, a
    * DebugEngineTerminatedEvent will be fired. Normally, however, running
    * the debuggee to completion does not cause the debug engine to be
    * terminated.
    * @param event The event
    * @see DebugEngine#terminate
    * @see DebugModelObject#hasBeenDeleted
    */

   public void debugEngineTerminated(DebugEngineTerminatedEvent event);

   /**
    * This method is called whenever a process is brought under debug control,
    * either by attaching to an existing process or loading a program to create
    * a new process.
    * DebugModelEvent.requestCode() can be used to determine whether this
    * event is associated with the creation of a new process or attaching to
    * an existing process. A value of EPDC.Remote_PreparePgm indicates that it
    * is associated with the creation of a new process, while
    * EPDC.Remote_ProcessAttach and EPDC.Remote_ProcessAttach2 indicate that
    * it is due to attaching to an existing process.
    * @param event The event
    */

   public void processAdded(ProcessAddedEvent event);

   /**
    * This event is fired whenever the debug engine returns an error code
    * in response to a request that was sent to it.
    */

   public void errorOccurred(ErrorOccurredEvent event);

   /**
    * This event is fired if a request was sent to the debug engine and
    * was processed successfully (i.e. no error occurred),
    * but the reply from the debug engine contains some message text. Such
    * messages are typically intended to be displayed to the user.
    */

   public void messageReceived(MessageReceivedEvent event);

   public void modelStateChanged(ModelStateChangedEvent event);

   public void engineCapabilitiesChanged(EngineCapabilitiesChangedEvent event);
}
