package com.ibm.dstore.core.server;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
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
