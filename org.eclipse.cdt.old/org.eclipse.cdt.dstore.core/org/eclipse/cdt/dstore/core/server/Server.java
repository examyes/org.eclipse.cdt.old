package com.ibm.dstore.core.server;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
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
