package com.ibm.dstore.ui.connections;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */
 
import com.ibm.dstore.ui.*;
import com.ibm.dstore.ui.ConvertUtility;
import com.ibm.dstore.ui.dialogs.*;
import com.ibm.dstore.core.client.*;
import com.ibm.dstore.core.model.*;
import com.ibm.dstore.extra.internal.extra.*;

import org.eclipse.jface.action.*;
import org.eclipse.jface.window.*;
import org.eclipse.jface.dialogs.*;

import java.util.*;
import java.io.*;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;

import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.*;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.source.*; 

import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.misc.*;

public class Connection
{
    public String  _name;
    public String  _type;
    public String  _host;
    public String  _port;
    public String  _dir;
    public String  _local;
    public String  _useDaemon;

    public boolean _isLocal;
    public boolean _isUsingDaemon;
    
    public ClientConnection _client;
    
    public DataElement _element;
    public DataElement _parent;
    
    public Connection(String name, ArrayList args, DataElement parent)
    {
        _name = name;
        _type = (String)args.get(0);
        _host = (String)args.get(1);
        _port = (String)args.get(2);
        _dir  = (String)args.get(3);
        _parent = parent;
	
        _local = (String)args.get(4);
        if (_local.equals("true"))
        {
            _isLocal = true;
        } 
        else
        {
          _isLocal = false;
        }

	if (args.size() > 4)
	    {
		_useDaemon = (String)args.get(5);
		if (_useDaemon.equals("true"))
		    {
			_isUsingDaemon = true;
		    }
		else
		    {
			_isUsingDaemon = false;
		    }
	    }
	else
	    {
		_isUsingDaemon = true;
	    }

        _element = parent.getDataStore().createObject(parent, _type, _name, _dir);
        parent.getDataStore().update(parent);
    }        
    
    public Connection(String name,
		      String host, String port, 
		      String type, String dir, boolean isLocal, 
		      boolean isUsingDaemon, 
		      DataElement parent)
    {
	_name = name;
        _host = host;
        _port = port;
        _dir = dir;
	_type = type;
	
        _parent = parent;

        setIsLocal(isLocal);
	setIsUsingDaemon(isUsingDaemon);
	
        _element = parent.getDataStore().createObject(parent, _type, _name, _dir);
        parent.getDataStore().update(parent);
    }
    
    public Connection(Connection connection, DataElement element)
    {
        _host     = connection._host;
        _port     = connection._port;

	setIsLocal(connection._isLocal);
	setIsUsingDaemon(connection._isUsingDaemon);
	
        _parent   = connection._parent;
        _element  = connection._element;
        _client   = connection._client;
        _name     = element.getAttribute(DE.A_NAME);
        _type     = element.getAttribute(DE.A_TYPE);
        _dir      = element.getAttribute(DE.A_SOURCE);
    }
        
    public void setHost(String host)
    {
	_host = host;
    }

    public void setPort(String port)
    {
	_port = port;
    }

    public void setDir(String dir)
    {
	_dir = dir;
    }

    public void setName(String name)
    {
	_name = name;
	_element.setAttribute(DE.A_NAME, _name);
    }

    public void setIsLocal(boolean isLocal)
    {
	_isLocal = isLocal;
        if (_isLocal)
	    {
		_local = new String("true");
	    }
        else
	    {
		_local = new String("false");
	    } 

    }

    public void setIsUsingDaemon(boolean isUsingDaemon)
    {
	_isUsingDaemon = isUsingDaemon;
        if (_isUsingDaemon)
	    {
		_useDaemon = new String("true");
	    }
        else
	    {
		_useDaemon = new String("false");
	    } 
    }
    
    public String getName()
    {
	return _name;
    }

    public String getHost()
    {
	return _host;
    }

    public String getPort()
    {
	return _port;
    }

    public String getDir()
    {
	return _dir;
    }

    public DataElement getRoot()
    {
	return _element;    
    }
    
    public DataElement getParent()
    {
	return _parent;
    }

    public DataStore getDataStore()
    {
	if (_client != null)
	    {
		return _client.getDataStore();    
	    }
	return null;
    }

    public boolean isUsingDaemon()
    {
	return _isUsingDaemon;
    }

    public boolean isLocal()
    {
	return _isLocal;
    }

    public boolean isConnected()
    {
	if (_client != null)
	    {
		return _client.isConnected();	
	    }
	return false;
	
    }
    
    public String toString()
    {
        String result = new String(_name + " " + _type + " " + 
				   _host + " " + _port + " " + 
				   _dir + " " + 
				   _local + " " + _useDaemon);
        return result;
    }
    
    public ConnectionStatus connect(DomainNotifier notifier)
    {
	return connect(notifier, "com.ibm.dstore.miners");
    }

    public ConnectionStatus connect(DomainNotifier notifier, String minersLocation)
    {
	if (_client == null)
	    {	
		_client = new ClientConnection(_name, notifier);
		_client.setLoader(_element.getDataStore().getLoader());
		
	    }
	
	_client.setHost(_host);
	_client.setPort(_port);
	_client.setHostDirectory(_dir);
	
	DataStore parentDS = _element.getDataStore();	
	DataStore newDS =_client.getDataStore();
	newDS.setAttribute(DataStoreAttributes.A_PLUGIN_PATH, parentDS.getAttribute(DataStoreAttributes.A_PLUGIN_PATH));	        
	ConnectionStatus connectStatus = null;
	if (_isLocal) 
	    {
		connectStatus = _client.localConnect();
	    }
	else 
	    {  
		String user = "";
		String password = "";
		if (_isUsingDaemon)
		    {
			LoginDialog ldialog = new LoginDialog();
			ldialog.open();
			if (ldialog.getReturnCode() != ldialog.OK)
			    return null;
			user = ldialog.getUser();
			password = ldialog.getPassword();
		    }
		
		connectStatus = _client.connect(_isUsingDaemon, user, password);
	    }
	
	if (connectStatus.isConnected())
	    {
		if (getSchema(connectStatus, minersLocation))
		    {
			_element.getDataStore().createReferences(_element, _client.getDataStore().getRoot().getNestedData(), "contents");
			_element.getDataStore().refresh(_element);
			return connectStatus;
		    }		
		else
		    {
			return connectStatus;
		    }
	    }
	else
	    {
		return connectStatus;
	    }
    }
    
    public void disconnect()
    {
        if (_client != null && _client.isConnected())
	    {
		_element.removeNestedData();
		_element.setUpdated(false);
		_element.setExpanded(false);	  
		_element.getDataStore().refresh(_element);
		_client.disconnect();
		_client = null;
	    }
    }
    
    public void delete()
    {
        disconnect();
        _parent.removeNestedData(_element);
        DataStore ds = _parent.getDataStore();
        ds.refresh(_parent);
    }
    
    public boolean contains(DataElement element)
    {    
	DataStore ds1 = element.getDataStore();
	if (_client != null)
	    {	
		DataStore ds2 = _client.getDataStore();
		return (ds1 == ds2); 
	    }
	return false;
	
    }
    
    public boolean contains(DataStore ds1)
    {    
	if (_client != null)
	    {	
		DataStore ds2 = _client.getDataStore();
		return (ds1 == ds2);
	    }
	
	return false;
	
    }
    
    public boolean getSchema(ConnectionStatus connectionStatus)
    {
	return getSchema(connectionStatus, "com.ibm.dstore.miners");
    }

    public boolean getSchema(ConnectionStatus connectionStatus, String minersLocation)
    {
	String ticketStr = connectionStatus.getTicket();
	boolean result = false;
        DataStore dataStore = _client.getDataStore();
	DataElement hostRoot = dataStore.getHostRoot();         
	
	// show ticket	
	if (dataStore.showTicket(ticketStr))
	    {
		dataStore.setMinersLocation(minersLocation);
		
		// get schema
		dataStore.getSchema();
		
		// get content
		String host = _client.getHost();
		String hostDirectory = _client.getHostDirectory();
		
		if (!host.equals(hostRoot.getName()) || !hostDirectory.equals(hostRoot.getSource()))
		    {
			hostRoot.setAttribute(DE.A_NAME, host);
			hostRoot.setAttribute(DE.A_SOURCE, hostDirectory);
			dataStore.setHost(hostRoot);
		    }
		
		// initialize miners
		DataElement status = dataStore.initMiners();
		if (!status.getName().equals("done"))
		    {
			disconnect();
			connectionStatus.setConnected(false);
			if (status.get(0) != null)
			    {
				String msg = status.get(0).getName();
				connectionStatus.setMessage(msg);
			    }
			else
			    {
				connectionStatus.setMessage("Couldn't Connect");
			    }
			result = false;
		    }
		else
		    {
			result = true;
			dataStore.getDomainNotifier().enable(true);
		    }

	    }

	return result;
    }
}







