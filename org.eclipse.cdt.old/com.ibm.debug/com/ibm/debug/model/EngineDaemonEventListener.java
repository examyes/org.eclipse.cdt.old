package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/EngineDaemonEventListener.java, java-model, eclipse-dev, 20011128
// Version 1.1.1.2 (last modified 11/28/01 16:14:16)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

public interface EngineDaemonEventListener extends ModelEventListener
{
/**
 * This event indicates that a remote engine has contacted the EngineDaemon and
 * is waiting to be connected to.
 * @param event - DebugEngineWaitingEvent contains information about the waiting
 * engine
 * Creation date: (3/21/00 3:09:52 PM)
 */
void debugEngineWaiting(DebugEngineWaitingEvent event);
}
