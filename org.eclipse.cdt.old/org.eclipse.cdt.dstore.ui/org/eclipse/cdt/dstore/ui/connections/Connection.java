package org.eclipse.cdt.dstore.ui.connections;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
 
import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.ConvertUtility;
import org.eclipse.cdt.dstore.ui.dialogs.*;
import org.eclipse.cdt.dstore.core.client.*;
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.extra.internal.extra.*;

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
import org.eclipse.jface.dialogs.*;

import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.misc.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.*;
import java.lang.reflect.InvocationTargetException;

public class Connection implements IDomainListener
{
    public class ConnectOperation implements IRunnableWithProgress
    {
	private ConnectionStatus _connectStatus;
	private String           _minersLocation;

	public ConnectOperation(String minersLocation)
	{
	    _minersLocation = minersLocation;
	}    

	public void run(IProgressMonitor monitor) throws InvocationTargetException
	{
	    execute(monitor);
	}

	protected void execute(IProgressMonitor pm) 
	{
	    pm.beginTask(_plugin.getLocalizedString("connection.Connecting"), 100);
	    if (_isLocal) 
		{
		    _connectStatus = _client.localConnect();
		}
	    else 
		{  
		    _connectStatus = _client.connect(_isUsingDaemon, _user, _password);
		}

	    pm.worked(100);

	    if (_connectStatus.isConnected())
	    {
		pm.beginTask(_plugin.getLocalizedString("connection.Initializing_DataStore"), 100);

		if (getSchema(_connectStatus, _minersLocation, pm))
		    {
			_element.getDataStore().createReferences(_element, _client.getDataStore().getRoot().getNestedData(), "contents");
			_element.getDataStore().refresh(_element);
		    }		

	    }

	    pm.done();
	}
	
	public boolean getSchema(ConnectionStatus connectionStatus, String minersLocation, IProgressMonitor monitor)
	{
	    String ticketStr = connectionStatus.getTicket();
	    boolean result = false;
	    DataStore dataStore = _client.getDataStore();
	    DataElement hostRoot = dataStore.getHostRoot();         
	    
	    monitor.subTask(_plugin.getLocalizedString("connection.Showing_ticket"));
	    // show ticket	
	    if (dataStore.showTicket(ticketStr))
		{
		    monitor.worked(10);

		    monitor.subTask(_plugin.getLocalizedString("connection.Setting_miners_location"));
		    DataElement smlStatus = dataStore.setMinersLocation(minersLocation);
		    monitor.worked(10);
		    
		    // get schema
		    monitor.subTask(_plugin.getLocalizedString("connection.Getting_schema"));
		    DataElement schemaStatus = dataStore.getSchema();
		    monitor.worked(10);
		    
		    
		    // get content
		    String host = _client.getHost();
		    String hostDirectory = _client.getHostDirectory();
		    
		    monitor.subTask(_plugin.getLocalizedString("connection.Setting_working_directory"));
		    if (!host.equals(hostRoot.getName()) || !hostDirectory.equals(hostRoot.getSource()))
			{
			    hostRoot.setAttribute(DE.A_NAME, host);
			    hostRoot.setAttribute(DE.A_SOURCE, hostDirectory);
			    DataElement shStatus = dataStore.setHost(hostRoot);
			}
		    monitor.worked(10);
		    
		    // initialize miners
		    monitor.subTask(_plugin.getLocalizedString("connection.Initializing_miners"));
		    DataElement status = dataStore.initMiners();
		    
		    DataElement rootDir = dataStore.getHostRoot().get(0);
		    if (rootDir == null)
			{
			    String msg = _plugin.getLocalizedString("connection.Failed_to_find_working_directory");
			    status.setAttribute(DE.A_NAME, "failed");
			    dataStore.createObject(status, "error", msg);			    
			}
		    monitor.worked(20);

		    monitor.subTask(_plugin.getLocalizedString("connection.Checking_status"));
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
				    connectionStatus.setMessage(_plugin.getLocalizedString("connection.Could_not_connect") + "  " + _host + ".");
				}
			    result = false;
			}
		    else
			{
			    result = true;
			}

		    monitor.worked(10);		    
		    monitor.subTask(_plugin.getLocalizedString("connection.Connected_to") + " "  + host);
		    dataStore.getDomainNotifier().enable(true);

		}
	    
	    return result;
	}
	
	public ConnectionStatus getStatus()
	{
	    return _connectStatus;
	}
    }

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
    
    private String _user = null;
    private String _password = null;

    private DomainNotifier _notifier = null;
    private DataStoreUIPlugin _plugin;

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
	_plugin = DataStoreUIPlugin.getDefault();
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
	if (port == null || port.length() == 0)
	    {
		_port = "0";
	    }

        _dir = dir;
	_type = type;
	
        _parent = parent;

        setIsLocal(isLocal);
	setIsUsingDaemon(isUsingDaemon);
	
        _element = parent.getDataStore().createObject(parent, _type, _name, _dir);
        parent.getDataStore().update(parent);
	_plugin = DataStoreUIPlugin.getDefault();
    }
    
    public Connection(Connection connection, DataElement element)
    {
        _host     = connection._host;
        _port     = connection._port;

	if (_port == null || _port.length() == 0)
	    {
		_port = "0";
	    }

	setIsLocal(connection._isLocal);
	setIsUsingDaemon(connection._isUsingDaemon);
	
        _parent   = connection._parent;
        _element  = connection._element;
        _client   = connection._client;
        _name     = element.getAttribute(DE.A_NAME);
        _type     = element.getAttribute(DE.A_TYPE);
        _dir      = element.getAttribute(DE.A_SOURCE);
	_plugin = DataStoreUIPlugin.getDefault();
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
	return connect(notifier, "org.eclipse.cdt.dstore.miners");
    }

    public ConnectionStatus connect(DomainNotifier notifier, String minersLocation)
    {
	_notifier = notifier;
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
	if (!_isLocal) 
	    {  
		if (_isUsingDaemon)
		    {
			Shell shell = notifier.findShell();
			LoginDialog ldialog = new LoginDialog();
			ldialog.open();
			if (ldialog.getReturnCode() != ldialog.OK)
			    return null;
			_user = ldialog.getUser();
			_password = ldialog.getPassword();
		    }
	    }
	
	
	Shell shell = notifier.findShell();
	ConnectOperation op = new ConnectOperation(minersLocation);
	ProgressMonitorDialog progressDlg = new ProgressMonitorDialog(shell);
	try
	    {
		progressDlg.run(true, true, op);
	    }
	catch (InterruptedException e) 
	    {
	    } 
	catch (InvocationTargetException e) 
	    {
	    }
	

	if (_client != null && _client.isConnected())
	    {
		notifier.addDomainListener(this);
	    }


	return op.getStatus();
    }
    
    public void disconnect()
    {
        if (_client != null && _client.isConnected())
	    {
		DataElement dsStatus = _client.getDataStore().getStatus();	
		if (!dsStatus.getName().equals("okay"))
		    {			
			// report an error message
			Shell shell = _notifier.findShell();
			if (shell != null)
			    {
				String msg = dsStatus.getName();
				MessageDialog.openError(shell, _plugin.getLocalizedString("connection.Connection_Error"), msg);   
			    }
		    }
		

		_element.removeNestedData();
		_element.setUpdated(false);
		_element.setExpanded(false);	  
		_element.getDataStore().refresh(_element);
		_client.disconnect();
		_client = null;

		_notifier.removeDomainListener(this);
		_notifier = null;
	    }
    }
    
    public void delete()
    {
        disconnect();
        DataStore ds = _parent.getDataStore();
	ds.deleteObject(_parent, _element);
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


    public boolean listeningTo(DomainEvent e)
    {
	DataElement dsStatus = _client.getDataStore().getStatus();	
	DataElement parent = (DataElement)e.getParent();
	if (dsStatus == parent)
	    {
		return true;
	    }

	return false;
    }
  
    public void domainChanged(DomainEvent e)
    {
	// let disconnect take of this and let others take care of disconnect
    }

    public Shell getShell()
    {
	return null;
    }
    
}







