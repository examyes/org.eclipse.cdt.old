package org.eclipse.cdt.dstore.core.server;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.server.*;
import org.eclipse.cdt.dstore.core.util.*;
import org.eclipse.cdt.dstore.core.model.*;

import java.util.*;
import java.lang.*;
import java.net.*;
import java.io.*;

/**
 * ConnectionEstablisher is responsible for managing the server DataStore and 
 * facilitating the communication between client and server DataStores.
 *
 */
public class ConnectionEstablisher 
{
    private ServerSocket          _serverSocket;
    private static boolean        _continue;
    
    private ArrayList             _receivers;
    
    private ServerCommandHandler  _commandHandler;
    private ServerUpdateHandler   _updateHandler;
    
    private ServerAttributes      _serverAttributes = new ServerAttributes();
    private DataStore             _dataStore;
    
    private int                   _maxConnections;
    private int                   _timeout;
    
    /**
     * Creates the default ConnectionEstablisher.  Communication occurs
     * on a default port, there is no timeout and no ticket is required
     * for a client to work with the DataStore.
     * 
     */
    public ConnectionEstablisher()
    {
	String port = _serverAttributes.getAttribute(ServerAttributes.A_HOST_PORT);
	setup(port, null, null);
    }
    
    /**
     * Creates a ConnectionEstablisher.  Communication occurs
     * on the specified port, there is no timeout and no ticket is required
     * for a client to work with the DataStore.
     * 
     * @param port the number of the socket port
     */
    public ConnectionEstablisher(String port)
    {
	setup(port, null, null);
    }
    
    /**
     * Creates a ConnectionEstablisher.  Communication occurs
     * on the specified port, a timeout value indicates the idle wait
     * time before shutting down, and no ticket is required
     * for a client to work with the DataStore.
     * 
     * @param port the number of the socket port
     * @param timeout the idle duration to wait before shutting down
     */
    public ConnectionEstablisher(String port, String timeout)
    {
	setup(port, timeout, null);
    }
  
    /**
     * Creates a ConnectionEstablisher.  Communication occurs
     * on the specified port, a timeout value indicates the idle wait
     * time before shutting down, and ticket specified the required
     * ticket for a client to present in order to work with the DataStore.
     *
     * @param port the number of the socket port
     * @param timeout the idle duration to wait before shutting down
     * @param ticket validation id required by the client to access the DataStore
     */
    public ConnectionEstablisher(String port, String timeout, String ticket)
    {
	setup(port, timeout, ticket);
    }
    
    
    /**
     * Starts the run loop for the ConnectionEstablisher.
     */
    public void start()
    {
	run();
    }
    
    /**
     * Returns the DataStore.
     *
     * @return the DataStore
     */
    public DataStore getDataStore()
    {
	return _dataStore;
    }

    /**
     * Tells the connection establisher to clean up and shutdown
     */
    public void finished(ServerReceiver receiver)
      {
	  System.out.println("finishing");
        _updateHandler.removeSenderWith(receiver.socket());
        _receivers.remove(receiver);
        if (_receivers.size() == 0)
        {
          _continue = false;
	  _commandHandler.finish();
	  _updateHandler.finish();
	  System.out.println(ServerReturnCodes.RC_FINISHED);
	  System.exit(0);
        }
      }

    private void waitForConnections()
    {
      while (_continue == true)
      {
	  System.out.println("waiting for connections");
	  try
	      {
		  // wait for connection
		  Socket newSocket        = _serverSocket.accept();
		  doHandShake(newSocket);
		  
		  ServerReceiver receiver = new ServerReceiver(newSocket, this);
		  Sender sender           = new Sender(newSocket);
		  
		  // add this connection to list of elements
		  _receivers.add(receiver);
		  _updateHandler.addSender(sender);
		  
		  receiver.start();
		  
		  if (_receivers.size() == 1)
		      {
			  _updateHandler.start();
			  _commandHandler.start();
		      }

		  /****	      
		  if (_receivers.size() == _maxConnections)
		      {
			  _continue = false;
			  _serverSocket.close();
			  
		      }
		  ****/
	      }	  
	  catch (IOException ioe)
	      {
		  System.err.println(ServerReturnCodes.RC_CONNECTION_ERROR);
		  System.err.println("Server: error initializing socket: " + ioe);
		  _continue = false;
	      }
      }      
      System.out.println("FINISHED waiting for connections");

    }
    

    /**
     * Create the DataStore and initializes it's handlers and communications.
     *
     * @param portStr the number of the socket port
     * @param timeoutStr the idle duration to wait before shutting down
     * @param ticketStr validation id required by the client to access the DataStore
     */
    private void setup(String portStr, String timeoutStr, String ticketStr)
      {
	  _maxConnections = 1; 
	  
 	  // port	
	  int port = Integer.parseInt(portStr);
	  
	  // timeout
	  if (timeoutStr != null)
	      {
		  _timeout = Integer.parseInt(timeoutStr);
	      }
	  else
	      {
		  _timeout = 120000;
	      }
	  
	  _commandHandler = new ServerCommandHandler(new Loader());
	  _updateHandler = new ServerUpdateHandler();
	  
	  _dataStore = new DataStore(_serverAttributes, _commandHandler, _updateHandler, null);
	  DataElement ticket = _dataStore.getTicket();
	  ticket.setAttribute(DE.A_NAME, ticketStr);
	  
	  _updateHandler.setDataStore(_dataStore);
	  _commandHandler.setDataStore(_dataStore);
	  
	  _receivers = new ArrayList();
	  _continue = true;
	  
	  try
	      {
		  // create server socket from port
		  _serverSocket = new ServerSocket(port);
		  
		  if (_timeout > 0)
		      {
			  _serverSocket.setSoTimeout(_timeout);
		      }
		  
		  System.err.println(ServerReturnCodes.RC_SUCCESS);
		  System.err.println(_serverSocket.getLocalPort());
		  System.out.println("Server running on: " + InetAddress.getLocalHost().getHostName());
	      }
	  catch (UnknownHostException e)
	      {
		  System.err.println(ServerReturnCodes.RC_UNKNOWN_HOST_ERROR);
		  _continue = false;
	      }         
	  catch (BindException e)
	      {
		  System.err.println(ServerReturnCodes.RC_BIND_ERROR);
		  _continue = false;
	      }      
	  catch (IOException e)
	      {
		  System.err.println(ServerReturnCodes.RC_GENERAL_IO_ERROR);
		  _continue = false;
	      }
	  catch (SecurityException e)
	      {
		  System.err.println(ServerReturnCodes.RC_SECURITY_ERROR);
		  _continue = false;
	      }
      }

    private void run()
    {
		waitForConnections();
    }
     
    private void doHandShake(Socket socket)
    { /*
    	try
    	{
    		 PrintWriter writer = new PrintWriter(socket.getOutputStream());
			 writer.println("DataStore");
			 writer.flush();
    	}
    	catch (IOException e)
    	{
    		System.out.println(e);
    	}
    	*/
    }
}

