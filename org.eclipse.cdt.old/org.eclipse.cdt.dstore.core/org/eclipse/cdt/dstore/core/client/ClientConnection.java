package org.eclipse.cdt.dstore.core.client;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.server.*;
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.core.util.*;
import org.eclipse.cdt.dstore.extra.internal.extra.*;
 
import java.net.*;
import java.io.*;
import java.util.*;



/**
 * ClientConnection is the standard means of creating a new connection to
 * a DataStore.  
 *
 * <li>
 * If a connection is local, then a DataStore is instantiated
 * in the same process to be used by the client to communicate with miners (tools).
 * </li>
 * 
 * <li>
 * If a connection is not local, then a virtual DataStore is instantiated in the 
 * current process to communicate with the remote DataStore.  If the client wishes
 * to instantiate a remote DataStore through a daemon, then ClientConnection first connects
 * to the daemon requesting a DataStore at a given port, and then connects to the
 * newly launched DataStore.  Otherwise, a DataStore is expected to be running on 
 * the remote machine under the same port that the client tries to connect to.
 * </li>
 * 
 *
 */
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
    
    /**
     * Creates a new ClientConnection instance
     *
     * @param name an identifier for this connection
     */
    public ClientConnection(String name)
    {
        _domainNotifier = new DomainNotifier();
        _name = name;
        init();
    }
    
    /**
     * Creates a new ClientConnection instance
     *
     * @param name an identifier for this connection
     * @param initialSize the number of elements to preallocate in the DataStore
     */
    public ClientConnection(String name, int initialSize)
    {
        _domainNotifier = new DomainNotifier();
        _name = name;
        init(initialSize);        
    }

    /**
     * Creates a new ClientConnection instance
     *
     * @param name an identifier for this connection
     * @param notifier the notifier used to keep the user interface in synch with the DataStore
     */
    public ClientConnection(String name, DomainNotifier notifier)
    {
        _domainNotifier = notifier;
        _name = name;
        init();
    }

    /**
     * Creates a new ClientConnection instance
     *
     * @param name an identifier for this connection
     * @param notifier the notifier used to keep the user interface in synch with the DataStore
     * @param initialSize the number of elements to preallocate in the DataStore
     */
    public ClientConnection(String name, DomainNotifier notifier, int initialSize)
    {
        _domainNotifier = notifier;
        _name = name;
        init(initialSize);
    }

    
    /**
     * Specifies the loader used to instantiate the miners
     *
     * @param loader the loader 
     */
    public void setLoader(ILoader loader)
    {
	_loader = loader;	
    }

    /**
     * Specifies the hostname or IP of the host to connect to 
     *
     * @param host the hostname or IP of the machine to connect to
     */
    public void setHost(String host)
    {
        _host = host;
	_clientAttributes.setAttribute(ClientAttributes.A_HOST_NAME, _host);
    }
    
    /**
     * Specifies the number of the socket port to connect to 
     *
     * @param port the number of the socket port to connect to
     */
    public void setPort(String port)
    {
	if (port == null || port.length() == 0)
	    {
		port = "0";
	    }
	
	_port = port;
	_clientAttributes.setAttribute(ClientAttributes.A_HOST_PORT, _port);
    }
    
    /**
     * Specifies the default working directory on the remote machine 
     *
     * @param directory the remote working directory
     */
    public void setHostDirectory(String directory)
    {
        _hostDirectory = directory;
        _clientAttributes.setAttribute(ClientAttributes.A_HOST_PATH, _hostDirectory);
    }
    
    /**
     * Returns the hostname/IP of the host to connect to 
     *
     * @return the hostname/IP
     */
    public String getHost()
    {
        return _host;
    }

    /**
     * Returns the number of the socket port to connect to 
     *
     * @return the number of the socket port to connect to
     */
    public String getPort()
    {
        return _port;
    }

    /**
     * Returns the default working directory on the host machine 
     *
     * @return the working directory on the host
     */
    public String getHostDirectory()
    {
        return _hostDirectory;
    }
    
    /**
     * Indicates whether the client is connected to the DataStore 
     *
     * @return whether the client is connected
     */
    public boolean isConnected()
    {
	return _isConnected;
    }
    
    /**
     * Disconnects from the DataStore and cleans up DataStore meta-information 
     */
    public void disconnect()
    {
	if (_isConnected)
	    {
		_dataStore.setConnected(false);
		
		if (_isRemote)
		    {
			_commandHandler.command(_dataStore.find(_dataStore.getRoot(), DE.A_NAME, "Exit"),
						_dataStore.getHostRoot(),
						false);
			_receiver.finish();
		    }
		
		_commandHandler.finish();
		
		try
		    {
			Thread.currentThread().sleep(200);
		    }
		catch (InterruptedException e)
		    {
			System.out.println(e);
		    }
		
		_updateHandler.finish();
		_dataStore.finish();
		
		_isConnected = false;
	    }
    }
    
    /**
     * Creates and connects to a local DataStore to work with in the current process.
     *
     * @return the status of the DataStore connection
     */
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
	_dataStore.getDomainNotifier().enable(true);
	
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

    /**
     * Connects to a remote DataStore by first communicating with a remote daemon and then
     * connecting to the DataStore.
     *
     * @param launchServer an indication of whether to launch a DataStore on the daemon on not
     * @param user the user ID of the current user on the remote machine
     * @param password the password of the current user on the remote machine
     * @return the status of the connection
     */    
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

    /**
     * Connects to a remote DataStore.   
     * A socket is created and the virtual DataStore is initialized with an update handler, command handler, 
     * socket sender and socket receiver. 
     *
     * @param ticket the ticket required to be granted access to the remote DataStore
     * @return the status of the connection
     */    
    public ConnectionStatus connect(String ticket)
    { 
	ConnectionStatus result = null;
        try 
	    {
		int port = 0;
		if (_port != null && _port.length() > 0)
		    {
			port = Integer.parseInt(_port);
		    }
		
		_theSocket = new Socket(_host, port);
		
		if (doHandShake())
		{		
			_sender    = new Sender(_theSocket, _dataStore);
			_updateHandler = new ClientUpdateHandler();
			_updateHandler.start(); 
		
			_commandHandler = new ClientCommandHandler(_sender);
			_commandHandler.start();
		
			_dataStore.setCommandHandler(_commandHandler);
			_dataStore.setUpdateHandler(_updateHandler);
			_dataStore.setConnected(true);
			_dataStore.getDomainNotifier().enable(true);
		
			_commandHandler.setDataStore(_dataStore);
			_updateHandler.setDataStore(_dataStore);
		
			_receiver  = new ClientReceiver(_theSocket, _dataStore);
			_receiver.start();
		
			_isConnected = true;
			_isRemote = true;
			result = new ConnectionStatus(_isConnected);	  
			result.setTicket(ticket);		
	    }
	    else
	    {
	    	String msg = "Invalid Protocol.";
	    	msg += "\nThe server running on " + _host + " under port " + _port + " is not a valid DataStore server.";
	    	result = new ConnectionStatus(false, msg);	
	    }
	    }
	catch (java.net.ConnectException e)
	    {
		String msg = "Connection Refused.";		
		msg += "\nMake sure that the DataStore server is running on " + _host + " under port " + _port + ".";
		result = new ConnectionStatus(false, msg);				  
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
    
    /**
     * Connects to a remote daemon and tells the daemon to launch
     * a DataStore server.   
     *
     * @param user the user ID of the current user on the remote machine
     * @param password the password of the current user on the remote machine
     * @return the status of the connection
     */    
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

				  _port = reader.readLine();
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
	  catch (java.net.ConnectException e)
	    {
		String msg = "Connection Refused.";		
		msg += "\nMake sure that the DataStore daemon is running on " + _host + ".";
		result = new ConnectionStatus(false, msg);				  
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

    /**
     * Returns the DataStore that the client is connected to.
     * @return the DataStore
     */    
    public DataStore getDataStore()
    {
        return _dataStore;
    }

    private void init()
    {
		init(10000);
    }

    private void init(int initialSize)
    {
        _clientAttributes = new ClientAttributes();
        _clientAttributes.setAttribute(ClientAttributes.A_ROOT_NAME, _name);
	
        _dataStore = new DataStore(_clientAttributes, initialSize);
        _dataStore.setDomainNotifier(_domainNotifier);
		_dataStore.createRoot();
	
        _host = _clientAttributes.getAttribute(ClientAttributes.A_HOST_NAME);
        _hostDirectory = _clientAttributes.getAttribute(ClientAttributes.A_HOST_PATH);
        _port = _clientAttributes.getAttribute(ClientAttributes.A_HOST_PORT);
    }

    private void flush(DataElement object)
    {
        _dataStore.flush(object);
    }
    
    private void flush()
      {
        _dataStore.flush(_dataStore.getHostRoot());
        _dataStore.flush(_dataStore.getLogRoot());
        _dataStore.flush(_dataStore.getDescriptorRoot());
        _dataStore.createRoot();
      }
      
    private boolean doHandShake()
    {
    	/*
    	try
    	{
   			BufferedReader reader  = new BufferedReader(new InputStreamReader(_theSocket.getInputStream()));          

	
			try
		    {
				Thread.currentThread().sleep(200);
		    }
			catch (InterruptedException e)
		    {
				System.out.println(e);
		    }
		
			
			if (reader.ready())
			{
				String handshake = reader.readLine(); 

				if  (handshake.equals("DataStore"))
				{
					return true;	
				}		
				else
				{
					return false;	
				}
    		}
    		else
    		{
    		}
    	}
   	 	catch (Exception e)
    	{
    		return false;	
    	}

    		
    	return false;
    	*/
    	return true;
    }

}




