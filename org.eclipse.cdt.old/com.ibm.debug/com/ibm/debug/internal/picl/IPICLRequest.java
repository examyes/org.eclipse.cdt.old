package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/IPICLRequest.java, eclipse, eclipse-dev, 20011128
// Version 1.4 (last modified 11/28/01 15:57:55)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.IOException;

/**
 * An <code>IPICLRequest</code> sends a request to the PICL debug
 * model, when sent the message <code>performRequest</code>.
 * A request identifies itself as synchronous, asynchronous,
 * immediate, or idle. An immediate request does not wait for the
 * engine to be in an "accepting state". For example, the halt
 * request can be sent at any time. An idle request waits for the
 * engine to not be busy.
 */
public interface IPICLRequest {
	/**
	 * Number of milliseconds a request should block waiting for the
	 * engine to become ready to accept a request.
	 */
	public static final int PICL_REQUEST_TIME_OUT= 10000;

	/**
	 * Constant indicating synchronous mode.
	 */
	public static final int SYNCHRONOUS= 0x0001;

	/**
	 * Constant indicating asynchronous mode.
	 */
	public static final int ASYNCHRONOUS= 0x0002;

	/**
	 * Constant indicating immediate mode.
	 */
	public static final int IMMEDIATE= 0x0004;

	/**
	 * Constant indicating idle mode
	 */
	public static final int IDLE= 0x0008;
	/**
	 * Returns a user readable message indicating what went
	 * wrong during a failed request.
	 */
	public String getErrorMessage();
	/**
	 * Returns the request type of this mode - a bit mask of the
	 * mode constants defined by <ocde>IPICLRequest</code>.
	 */
	int getMode();
	/**
	 * Performs the specific request, returning <code>true</code> if the
	 * request was accepted, otherwise <code>false</code>.
	 *
	 * @exception IOException if the request fails.
	 */
	boolean performRequest() throws IOException;
}
