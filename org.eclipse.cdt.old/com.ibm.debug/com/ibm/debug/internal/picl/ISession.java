package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/ISession.java, eclipse, eclipse-dev, 20011128
// Version 1.4 (last modified 11/28/01 15:57:56)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IDebugTarget;

/**
 * An <code>ISession</code> is used to initiate a debug session
 * using the PICL debug model. <code>ISession</code> itself, is abstract,
 * and defines the functionality common among all PICL debug sessions.
 * There are two kinds of debug sessions:<ul>
 * <li>Launching debug sessions. A launching session launches a debug engine,
 * 		creates a debug target that connects with the engine, and launches
 *		a program in the debug target</li>
 * <li>Attaching debug sessions. An attaching debug session creates a debug
 *		target that is waiting for a connection for a debug engine. When a
 *		connection is received, the debug target attaches to the debug engine.</li>
 * </ul>
 *
 * @see IAttachingSession
 * @see ILaunchingSession
 */
public interface ISession {
	/**
	 * Returns the <code>IDebugTarget</code> resulting
	 * from starting this debug session, or <code>null</code>
	 * if no debug target was created.
	 */
	IDebugTarget getDebugTarget();
	/**
	 * Returns the collection of <code>IProcess</code>es resulting
	 * from starting this debug session, or <code>null</code> if no
	 * proceses were generated.
	 */
	IProcess[] getProcesses();
	/**
	 * Starts the debug session, returning <code>true</code> if successful.
	 * If successful, the restuling target and system processes
	 * are available via <code>getDebugTarget()</code> and
	 * <code>getProcesses()</code>.
	 *
	 * @exception DebugException if an exception occurrs while starting the session
	 * (REQUEST_FAILED)
	 */
	boolean startDebugSession() throws DebugException;
}
