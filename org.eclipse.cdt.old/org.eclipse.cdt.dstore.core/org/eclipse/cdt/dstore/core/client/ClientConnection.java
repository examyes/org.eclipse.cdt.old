package com.ibm.dstore.core.client;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.core.client.*;
import com.ibm.dstore.core.server.*;
import com.ibm.dstore.core.model.*;
import com.ibm.dstore.core.util.*;
import com.ibm.dstore.extra.internal.extra.*;
 
import java.net.*;
import java.io.*;
import java.lang.*;
import java.util.*;

public class ClientConnection
{
  private ClientAttributes      _clientAttributes;

  private Socket                _theSocket;
  private boolean               _isConnected = false;
  private boolean               _isRemote = false;
  private DataStore             _dataStore;
  private DomainNotifier        _domainNotifier;
  private Sender                _sender;
  private ClientReceiver        _receiver;
  private ClientUpdateHandler   _updateHandler;
  private CommandHandler        _commandHandler;

  private String                _name;
  private String                _host;
  private String                _port;
  private String                _hostDirectory;
  
    private ILoader             _loader;  
    
  public ClientConnection(String name)
      {
        _domainNotifier = new DomainNotifier();
        _name = name;
        init();
        
      }

  public ClientConnection(String name, DomainNotifier notifier)
      {
        _domainNotifier = notifier;
        _name = name;
        init();
      }

  public void init()
      {
        _clientAttributes = new ClientAttributes();
        _clientAttributes.setAttribute(ClientAttributes.A_ROOT_NAME, _name);

        _dataStore = new DataStore(_clientAttributes);
        _dataStore.setDomainNotifier(_domainNotifier);
	_dataStore.createRoot();

        _host = _clientAttributes.getAttribute(ClientAttributes.A_HOST_NAME);
        _hostDirectory = _clientAttributes.getAttribute(ClientAttributes.A_HOST_PATH);
        _port = _clientAttributes.getAttribute(ClientAttributes.A_HOST_PORT);
      }

    public void setLoader(ILoader loader)
    {
	_loader = loader;	
    }

  public void setHost(String host)
      {
        _host = host;
	_clientAttributes.setAttribute(ClientAttributes.A_HOST_NAME, _host);
      }

  public void setPort(String port)
      {
        _port = port;
	_clientAttributes.setAttribute(ClientAttributes.A_HOST_PORT, _port);
      }

  public void setHostDirectory(String directory)
      {
        _hostDirectory = directory;
        _clientAttributes.setAttribute(ClientAttributes.A_HOST_PATH, _hostDirectory);
      }

  public String getHost()
      {
        return _host;
      }

  public String getPort()
      {
        return _port;
      }

  public String getHostDirectory()
      {
        return _hostDirectory;
      }

  public boolean isConnected()
  {
    return _isConnected;
  }

  public void disconnect()
  {
    if (_isConnected)
      {
	if (_isRemote)
	  {
	    _commandHandler.command(_dataStore.find(_dataStore.getRoot(), DE.A_NAME, "Exit"),
				    _dataStore.getHostRoot(),
				    true);
            _receiver.finish();
	  }

	_commandHandler.finish();
	_updateHandler.finish();
	_isConnected = false;
	_dataStore.flush();
      }

    try
    {
      Thread.currentThread().sleep(100);
    }
    catch (InterruptedException e)
    {
      System.out.println(e);
    }
  }

  public ConnectionStatus localConnect()
  {
    _updateHandler = new ClientUpdateHandler();
    _updateHandler.start();

    if (_loader != null)
	{
	    _commandHandler = new ServerCommandHandler(_loader);
	}
    else
	{
	    _commandHandler = new ServerCommandHandler(new Loader());
	}

    _commandHandler.start();

    _dataStore.setCommandHandler(_commandHandler);
    _dataStore.setUpdateHandler(_updateHandler);
    _dataStore.setConnected(true);
    _dataStore.setLoader(_loader);

    _commandHandler.setDataStore(_dataStore);
    _updateHandler.setDataStore(_dataStore);
    ((ServerCommandHandler)_commandHandler).loadMiners();


    _clientAttributes.setAttribute(_clientAttributes.A_LOCAL_NAME,
				   _clientAttributes.getAttribute(ClientAttributes.A_HOST_NAME));
    _clientAttributes.setAttribute(_clientAttributes.A_LOCAL_PATH,
				   _clientAttributes.getAttribute(ClientAttributes.A_HOST_PATH));

    _isConnected = true;

    DataElement ticket = _dataStore.getTicket();
    ticket.setAttribute(DE.A_NAME, "null");

    ConnectionStatus result = new ConnectionStatus(_isConnected);
    result.setTicket(ticket.getName());

    return result; 
  }

  public ConnectionStatus connect(boolean launchServer, String user, String password)
      {
	  ConnectionStatus launchStatus = null;
	  if (launchServer)
	      {
		  launchStatus = launchServer(user, password);
		  if (!launchStatus.isConnected())
		      {
			  return launchStatus;
		      }
	      }
	  else
	      {
		  launchStatus = new ConnectionStatus(true);
		  launchStatus.setTicket("null");
	      }

	  return connect(launchStatus.getTicket());	  
      }

  public ConnectionStatus connect(String ticket)
      { 
	  ConnectionStatus result = null;
        try 
        {
          int port = Integer.parseInt(_port);
          _theSocket = new Socket(_host, port);

          _sender    = new Sender(_theSocket);
          _updateHandler = new ClientUpdateHandler();
          _updateHandler.start(); 

          _commandHandler = new ClientCommandHandler(_sender);
          _commandHandler.start();

          _dataStore.setCommandHandler(_commandHandler);
          _dataStore.setUpdateHandler(_updateHandler);
          _dataStore.setConnected(true);

	  _commandHandler.setDataStore(_dataStore);
	  _updateHandler.setDataStore(_dataStore);

          _receiver  = new ClientReceiver(_theSocket, _dataStore);
          _receiver.start();

          _isConnected = true;
          _isRemote = true;
	  result = new ConnectionStatus(_isConnected);	  
	  result.setTicket(ticket);
        }
        catch (UnknownHostException uhe)
        {
	    _isConnected = false;
	    result = new ConnectionStatus(_isConnected, uhe);	  
        }
        catch (IOException ioe)
        {
	    _isConnected = false;
	    result = new ConnectionStatus(_isConnected, ioe); 
        }

	return result;
      }

  public ConnectionStatus launchServer(String user, String password)
      {
	  ConnectionStatus result = null;
	  try
	      {
		  // connect to known port for server daemon
		  Socket launchSocket = new Socket(_host, 1234);
		  PrintWriter writer = null;
		  BufferedReader reader = null;
		  
		  // create output stream for server launcher
		  try
		      {
			  writer = new PrintWriter(launchSocket.getOutputStream());
			  writer.println(user);
			  writer.println(password);
			  writer.println(_port);
			  writer.flush();
			  
			  reader  = new BufferedReader(new InputStreamReader(launchSocket.getInputStream()));          
			  String status = reader.readLine();
			  
			  if (status != null && !status.equals("connected"))
			      {
				  result = new ConnectionStatus(false, status);
			      }
			  else
			      {
				  result = new ConnectionStatus(true);
				  String ticket = reader.readLine();
				  result.setTicket(ticket);
			      }
		      }
		  catch (java.io.IOException e)
		      {
			  result = new ConnectionStatus(false, e);
		      }

		  reader.close();
		  writer.close();
		  launchSocket.close(); 
	      }
	  catch (UnknownHostException uhe)
	      {
		  result = new ConnectionStatus(false, uhe);
	      }
	  catch (IOException ioe)
	      {
		  result = new ConnectionStatus(false, ioe);
	      }        
	  
	  return result;
      }

  public void flush(DataElement object)
      {
        _dataStore.flush(object);
      }

  public void flush()
      {
        _dataStore.flush(_dataStore.getHostRoot());
        _dataStore.flush(_dataStore.getLogRoot());
        _dataStore.flush(_dataStore.getDescriptorRoot());
        _dataStore.createRoot();
      }

  public DataStore getDataStore()
      {
        return _dataStore;
      }
}




