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
		
		if (args.length > 0)
		    {
			if (args.length > 1)
			{
			    theServer = new Server(args[0], args[1]);	      
			}
			else
			    {
				theServer = new Server(args[0]);	      	      			    }
		    }	
		else
		    {
			theServer = new Server();
		    }
		
		theServer.run();
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
    
    public Server(String port, String ticket)
    {
	_establisher = new ConnectionEstablisher(port, ticket);
    }
  

  public void run()
    {
      _establisher.start();
    }
}
