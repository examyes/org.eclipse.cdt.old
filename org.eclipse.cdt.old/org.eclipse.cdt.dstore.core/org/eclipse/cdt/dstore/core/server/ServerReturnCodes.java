package org.eclipse.cdt.dstore.core.server;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

/**
 * Insert the type's description here.
 * Creation date: (3/1/01 8:41:20 AM)
 * 
 */
public class ServerReturnCodes 
{
	public static final String RC_SUCCESS            = "Server Started Successfully";
	
	public static final String RC_UNKNOWN_HOST_ERROR = "Unknown host error";
	public static final String RC_BIND_ERROR	 = "Error binding socket";
	public static final String RC_GENERAL_IO_ERROR	 = "General IO error creating socket";
	public static final String RC_CONNECTION_ERROR   = "Connection error";
	
	public static final String RC_SECURITY_ERROR     = "Security error creating socket";

	public static final String RC_FINISHED           = "Server Finished";
}
