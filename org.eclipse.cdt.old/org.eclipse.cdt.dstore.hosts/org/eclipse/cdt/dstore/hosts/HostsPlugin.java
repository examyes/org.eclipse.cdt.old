package org.eclipse.cdt.dstore.hosts;


/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
 
import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.actions.*;
import org.eclipse.cdt.dstore.ui.connections.*;
import org.eclipse.cdt.dstore.ui.views.*;

import org.eclipse.cdt.dstore.hosts.actions.*;

import org.eclipse.cdt.dstore.extra.internal.extra.*;

import org.eclipse.cdt.dstore.core.*;
import org.eclipse.cdt.dstore.core.miners.miner.*;
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.core.util.*;
import org.eclipse.cdt.dstore.core.client.*;
import org.eclipse.cdt.dstore.core.server.*;

import org.eclipse.ui.plugin.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*; 
import org.eclipse.jface.resource.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.*;
import org.eclipse.ui.*;

import java.io.*;
import java.util.*;
import java.net.*;
import java.lang.reflect.*;
import org.eclipse.jface.action.*;



public class HostsPlugin extends AbstractUIPlugin implements ISchemaProvider
{
    private static HostsPlugin         _instance;
    
    private static ClientConnection    _clientConnection;
    private static ConnectionManager   _connectionManager;

    private static DataStoreCorePlugin _corePlugin;
    private static String              _corePath;

    private static DataStore           _dataStore;
    private static ResourceBundle      _resourceBundle;

    private static IActionLoader       _actionLoader;
    private        SchemaRegistry      _schemaRegistry;
    private        ISchemaExtender     _schemaExtender;

    public HostsPlugin(IPluginDescriptor descriptor)
    {
	super(descriptor);
	_corePlugin = org.eclipse.cdt.dstore.core.DataStoreCorePlugin.getPlugin();
	_corePath = org.eclipse.cdt.dstore.core.DataStoreCorePlugin.getPlugin().getInstallLocation();	

	try
	    {
		_resourceBundle = ResourceBundle.getBundle("org.eclipse.cdt.dstore.hosts.PluginResources");
	    }
	catch (MissingResourceException mre)
	    {
		_resourceBundle = null;
	    }	
    }

  static public HostsPlugin getDefault()
  {
    return _instance;
  }

  static public HostsPlugin getPlugin()
  {
    return _instance;
  }

  static public HostsPlugin getInstance()
  {
    return _instance;
  }

  public static IWorkspace getPluginWorkspace()
  {
    return ResourcesPlugin.getWorkspace();
  }
    
    public static IActionLoader getActionLoader()
    {
	return _actionLoader;
    }    

    public static void setActionLoader(IActionLoader loader)
    {
	_actionLoader = loader;
    }    

    protected void initializeDefaultPreferences()
    {
    }

    public void startup() throws CoreException
      {
        try
        {
          super.startup();
	  initDataStore();
        }
        catch (CoreException e)
        {}
      }

    public void initDataStore()
    {
	if (_instance == null)
	    {	
		ArrayList loadScope = new ArrayList();
		loadScope.add("org.eclipse.cdt.dstore.hosts.*");
		loadScope.add("org.eclipse.cdt.dstore.miners.*");
		
		ExternalLoader hostsLoader = new ExternalLoader(getDescriptor().getPluginClassLoader(), 
								loadScope);
		
		_clientConnection = new ClientConnection("Hosts", 2000); 
		_clientConnection.addLoader(hostsLoader);	
		_dataStore = _clientConnection.getDataStore();
		String install = _corePath;
		
		_dataStore.setAttribute(DataStoreAttributes.A_PLUGIN_PATH, install);
		
		IWorkspace workbench = (IWorkspace)getPluginWorkspace();
		Path rootPath = (Path)Platform.getLocation();
		
		String rootDirectory = rootPath.toString();

		DataElement hostRoot = _dataStore.getHostRoot();
		hostRoot.setAttribute(DE.A_SOURCE, rootDirectory);
		
		_clientConnection.setHostDirectory(rootDirectory);	
		_dataStore.addMinersLocation("org.eclipse.cdt.dstore.miners");
		_clientConnection.localConnect();
	
		_instance = this;
		
		// load schema
		_dataStore.showTicket(_dataStore.getTicket().getName());
		_dataStore.getSchema();
		_dataStore.initMiners();
		
		
		// setup schema
		_schemaRegistry = new SchemaRegistry();
		_schemaExtender = new HostsSchemaExtender(hostsLoader);
		_schemaRegistry.registerSchemaExtender(_schemaExtender);
		_schemaRegistry.extendSchema(_dataStore);
		
		// create hosts action loader
		_actionLoader = new HostsActionLoader(this);
		
		
		// for remote connections
		_connectionManager = new ConnectionManager(_dataStore.getRoot(), _dataStore.getDomainNotifier());
		_connectionManager.readConnections();
	    }
    }
    
    public DataStore getDataStore()
    { 
	return _dataStore; 
    }

    public String getInstallLocation()
    {
	URL baseURL = org.eclipse.core.boot.BootLoader.getInstallURL();
	String path = baseURL.getFile();
	
	String location = path + "/plugins/org.eclipse.cdt.dstore.hosts/";
	return location;
    }
    
  public Image getImage(String name)
    {
	org.eclipse.jface.resource.ImageRegistry reg = getImageRegistry();
	Image image = reg.get(name);
	if (image == null)
	    {
		ImageDescriptor des = ImageDescriptor.createFromFile(null, name);
		image = des.createImage();
		reg.put(name, des);
	    }
	
	return image;
    }

    
     public ImageDescriptor getImageDescriptor(String name)
      {
	    String baseDir = _corePath + java.io.File.separator;
	   	 HostsActionLoader loader = HostsActionLoader.getInstance();
	    String file = loader.getImageString(name);
	    
	return ImageDescriptor.createFromFile(null, file);
      }
  
    
    public static ConnectionManager getConnectionManager()
      {
        return _connectionManager;
      }

    public void shutdown() throws CoreException
    {
	_connectionManager.disconnectAll();
	super.shutdown();
    }
    
      /**
       * Convenience method for NLS enablement of strings
       */
      public static String getLocalizedString(String key)
      {
         try
         {
            if (_instance._resourceBundle != null && key != null)
               return _instance._resourceBundle.getString(key);
         }
         catch (MissingResourceException mre)
         {
         }
         return "";
      }

   public ResourceBundle getResourceBundle()
      {
          return _resourceBundle;
      }

  public static ArrayList readProperty(String preference)
      {
        ArrayList savedProperty = new ArrayList();
	String historyFilePath = _instance.getStateLocation().append(preference).toOSString();
	java.io.File historyFile = new java.io.File(historyFilePath);

	if (historyFile.exists())
	    {
		DataInputStream input = null;
		String historyString = "";
		try
		    {
			input = new DataInputStream(new FileInputStream(historyFile));
			historyString = input.readUTF();
		    }
		catch (IOException e)
		    {
			System.out.println(e);
		    }
		
		if (historyString != null && historyString.length() != 0)
		    {
			StringTokenizer st = new StringTokenizer(historyString, "|", false);
			while (st.hasMoreTokens())
			    {
				savedProperty.add(st.nextToken());
			    }
		    }
	    }
	
	return savedProperty;
      }

  public static void writeProperty(String preference, ArrayList property)
      {
	  String historyFilePath = _instance.getStateLocation().append(preference).toOSString();
	  java.io.File historyFile = new java.io.File(historyFilePath);
	  String historyString = "";
	
	  int size = property.size();
	  for (int i=0; i < size; i++)
	      {
                  historyString = historyString.concat((property.get(i)).toString());
                  historyString = historyString.concat("|");
	      }
	  try
	      {
		  DataOutputStream output = new DataOutputStream(new FileOutputStream(historyFile));
                  output.writeUTF(historyString);
	      }
	  catch (IOException e)
	      {
                  System.out.println(e);
	      }
      }

    public ISchemaRegistry getSchemaRegistry()
    {
	return _schemaRegistry;
    }

    public ISchemaExtender getSchemaExtender()
    {
	return _schemaExtender;
    }


}











