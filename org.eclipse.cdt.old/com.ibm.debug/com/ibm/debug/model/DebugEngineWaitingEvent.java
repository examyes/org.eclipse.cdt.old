package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/DebugEngineWaitingEvent.java, java-model, eclipse-dev, 20011129
// Version 1.2.1.3 (last modified 11/29/01 14:15:34)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.connection.DebugDaemon;


/**
 * Event generated when a remote debug engine is waiting for a connection
 */
public class DebugEngineWaitingEvent extends ModelEvent {
	EngineDaemon.EngineConnected _engineConnected = null;
/**
 * DebugEngineWaitingEvent
 * @param source java.lang.Object
 * @param engineConnected EngineDaemon.EngineConnected - info passed by remote engine
 * @param requestCode int
 */
DebugEngineWaitingEvent(Object source, EngineDaemon.EngineConnected engineConnected, int requestCode) {
	super(source, requestCode);
	_engineConnected = engineConnected;

}
/**
 * fire method
 */
void fire(ModelEventListener listener) {
	((EngineDaemonEventListener) listener).debugEngineWaiting(this);
}
/**
 * Returns the arguments passed by the remote debug engine to the EngineDaemon
 * @return java.lang.String
 */
public String getArguments() {
	return _engineConnected.getArguments();
}
/**
 * Returns the ConnectionInfo object required to connect to the remote
 * debug engine that is waiting for a connection
 * @return com.ibm.debug.connection.ConnectionInfo
 */
public com.ibm.debug.connection.ConnectionInfo getConnectionInfo() {
	return _engineConnected.getConnectionInfo();
}
/**
 * Returns the title passed to the engine daemon by the remote debug engine
 *  This title can be used by the model client for windows associated with this
 *  engine connection
 * @return java.lang.String
 */
public String getTitle() {
	return _engineConnected.getTitle();
}
/**
 * Returns the engine parameters passed to the engine daemon by the remote
 * debug engine.
 */
public DebugDaemon.EngineParameters getEngineParameters() {
        return _engineConnected.getEngineParameters();
}
}
