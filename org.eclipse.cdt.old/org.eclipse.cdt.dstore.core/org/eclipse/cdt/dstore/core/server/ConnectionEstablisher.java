package com.ibm.dstore.core.server;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.core.server.*;
import com.ibm.dstore.core.util.*;
import com.ibm.dstore.core.model.*;

import java.util.*;
import java.lang.*;
import java.net.*;
import java.io.*;

public class ConnectionEstablisher 
    //extends Thread
{
  private ServerSocket          _serverSocket;
  private static boolean        _continue;

  private ArrayList             _receivers;

  private ServerCommandHandler  _commandHandler;
  private ServerUpdateHandler   _updateHandler;
 
  private ServerAttributes      _serverAttributes;
  private DataStore             _dataStore;

  private int                   _maxConnections;
  
  public ConnectionEstablisher()
  {
    _maxConnections = 1;
    _serverAttributes = new ServerAttributes();
    String port = _serverAttributes.getAttribute(ServerAttributes.A_HOST_PORT);
    setup(port, null);
  }
  
  public ConnectionEstablisher(String port)
  {
    _maxConnections = 1;
    _serverAttributes = new ServerAttributes();
    setup(port, null);
  }
  
  public ConnectionEstablisher(String port, String ticket)
  {
    _maxConnections = 1; 
    _serverAttributes = new ServerAttributes();
    setup(port, ticket);
  }
  

  public void setup(String portStr, String ticketStr)
      {
      int port = Integer.parseInt(portStr);

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

    public void start()
    {
	run();
    }

    public void run()
    {
      waitForConnections();
    }

  public DataStore dataStore()
      {
        return _dataStore;
      }



  public void finished(ServerReceiver receiver)
      {
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

  public void waitForConnections()
    {
      while (_continue == true)
      {
        try
            {
              // wait for connection
              Socket newSocket        = _serverSocket.accept();
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

	      if (_receivers.size() == _maxConnections)
		  {
		    _continue = false;
 			_serverSocket.close();
		  
		  }
            }

        catch (IOException ioe)
            {
		System.err.println(ServerReturnCodes.RC_CONNECTION_ERROR);
		System.err.println("Server: error initializing socket: " + ioe);
		_continue = false;
            }
      }
    }
}

