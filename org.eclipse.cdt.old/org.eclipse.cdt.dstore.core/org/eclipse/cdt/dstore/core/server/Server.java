package org.eclipse.cdt.dstore.core.server;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.server.*;

/**
 * Server is the standard way of instantiating and controlling a remote DataStore.
 * The server runs a ConnectionEstablisher which manages client connections to
 * the DataStore.
 *
 */
public class Server
{
    private ConnectionEstablisher  _establisher;
    
    /**
     * The startup interface to run the Server.
     *
     * @param args a list of arguments for running the server.  These consist of 
     * the socket port to wait on, the timeout value, and the the ticket
     */
    public static void main(String[] args)
    {
	try 
	    {
		Server theServer = null;
		switch (args.length)
		    {
			case 0:
			  theServer = new Server();
			  break;
			case 1:
			  theServer = new Server(args[0]);
			  break;
			case 2:
			  theServer = new Server(args[0], args[1]);
			  break;
			case 3:
			  theServer = new Server(args[0], args[1], args[2]);
			  break;
			default:
			  break;  
		}
		
	    if (theServer != null)
		{
			theServer.run();
		}
	  }
	catch (SecurityException e) 
	    {
		System.err.println(ServerReturnCodes.RC_SECURITY_ERROR);
		throw e;   // Optional
	    }
    }
    
    /**
     * Creates a new Server with default DataStore and connection attributes.
     *
     */
    public Server() 
    {
        _establisher = new ConnectionEstablisher();
    }
    
    /**
     * Creates a new Server that waits on the specified socket port.
     *
     * @param port the number of the socket port to wait on
     */
    public Server(String port)
    {
        _establisher = new ConnectionEstablisher(port);
    } 
    
    /**
     * Creates a new Server that waits on the specified socket port for
     * the specified time interval before shutting down.
     *
     * @param port the number of the socket port to wait on
     * @param timeout the idle time to wait before shutting down 
     */
    public Server(String port, String timeout)
    {
    	_establisher = new ConnectionEstablisher(port, timeout);	
    }
    
    /**
     * Creates a new Server that waits on the specified socket port for
     * the specified time interval before shutting down.  
     *
     * @param port the number of the socket port to wait on
     * @param timeout the idle time to wait before shutting down 
     * @param ticket the ticket that the client needs to interact with the DataStore 
     */
    public Server(String port, String timeout, String ticket)
    {
	  _establisher = new ConnectionEstablisher(port, timeout, ticket);
    }
  
    /**
     * Runs the server by starting the ConnectionEstablisher  
     */
    public void run()
    {
	_establisher.start();
    }
}
