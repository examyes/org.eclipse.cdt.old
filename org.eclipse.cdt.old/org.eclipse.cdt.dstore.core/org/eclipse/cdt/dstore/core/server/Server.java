package com.ibm.dstore.core.server;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.core.server.*;

public class Server
{
  private ConnectionEstablisher  _establisher;
    
    public static void main(String[] args)
    {
	try 
	    {
		Server theServer = null;
		
		// args:
		// 1 = port
		// 2 = timeout
		// 3 = ticket
		
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
    
    public Server() 
    {
        _establisher = new ConnectionEstablisher();
    }
    
    public Server(String port)
    {
        _establisher = new ConnectionEstablisher(port);
    } 
    
    public Server(String port, String timeout)
    {
    	_establisher = new ConnectionEstablisher(port, timeout);	
    }
    
    public Server(String port, String timeout, String ticket)
    {
	  _establisher = new ConnectionEstablisher(port, timeout, ticket);
    }
  

  public void run()
    {
      _establisher.start();
    }
}
