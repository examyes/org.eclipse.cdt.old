package com.ibm.dstore.hosts;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
 
import com.ibm.dstore.ui.*;
import com.ibm.dstore.ui.actions.*;
import com.ibm.dstore.ui.connections.*;

import com.ibm.dstore.hosts.actions.*;

import com.ibm.dstore.extra.internal.extra.*;

import com.ibm.dstore.core.*;
import com.ibm.dstore.core.miners.miner.*;
import com.ibm.dstore.core.model.*;
import com.ibm.dstore.core.util.*;
import com.ibm.dstore.core.client.*;
import com.ibm.dstore.core.server.*;

import org.eclipse.ui.plugin.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*; 
import org.eclipse.jface.resource.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.ui.*;

import java.io.*;
import java.util.*;
import java.net.*;
import java.lang.reflect.*;


public class HostsPlugin extends AbstractUIPlugin
{
    

    public class MinerClassLoader implements ILoader
    {
	public Miner loadMiner(String name)
	    {
		Miner miner = null;
		try
		    {					
			miner = (Miner)Class.forName(name).newInstance();
		    }
		catch (ClassNotFoundException e)
		    {
			System.out.println(e);
		    }
		catch (InstantiationException e)
		    {
			System.out.println(e);
		    }
		catch (IllegalAccessException e)
		    {
			System.out.println(e);
		    }
		catch (Exception e)
		    {
			System.out.println(e);
		    }

		return miner;
	    }
    }

    public class HostsActionLoader implements IActionLoader
    {
	private IOpenAction   _openAction;
	private CustomAction  _openPerspectiveAction;

	public CustomAction getOpenPerspectiveAction()
	{
	    if (_openPerspectiveAction == null)
		{
		    _openPerspectiveAction = loadAction("com.ibm.dstore.ui.actions.OpenPerspectiveAction", 
							"Open Perspective On");
		}
	    return _openPerspectiveAction;
	}

	public IOpenAction getOpenAction()
	{
	    if (_openAction == null)
		{
		    _openAction = new OpenEditorAction(null);
		}
	    return _openAction;
	}

	public CustomAction loadAction(String source, String name)
	{
	    CustomAction newAction = null;
	    try
		{
		    Object[] args = { name};
		    Class actionClass = Class.forName(source);
		    Constructor constructor = actionClass.getConstructors()[0];
		    newAction = (CustomAction)constructor.newInstance(args);
		}
	    catch (ClassNotFoundException e)
		{
		    //System.out.println(e);
		}
	    catch (InstantiationException e)
		{ 
		    //System.out.println(e);
		}
	    catch (IllegalAccessException e)
		{
		    //System.out.println(e);	
		}
	    catch (InvocationTargetException e)
		{
		    //System.out.println(e);
		}
	    
	    return newAction;
	}

	public CustomAction loadAction(java.util.List objects, DataElement descriptor)
	{
	    return loadAction((DataElement)objects.get(0), descriptor);
	}

	public CustomAction loadAction(DataElement object, DataElement descriptor)
	{
	    String name = descriptor.getName();
	    String source = descriptor.getSource();
	    
	    CustomAction newAction = null; 
	    try
		{         
		    Object[] args = {object, name, descriptor, object.getDataStore()};
		    Class actionClass = Class.forName(source);
		    Constructor constructor = actionClass.getConstructors()[0];
		    newAction = (CustomAction)constructor.newInstance(args);
		}
	    catch (ClassNotFoundException e)
		{
		    //System.out.println(e);
		}
	    catch (InstantiationException e)
		{
		    //System.out.println(e);
		}
	    catch (IllegalAccessException e)
		{
		    //System.out.println(e);
		}
	    catch (InvocationTargetException e)
		{
		    //System.out.println(e);
		}
	    
	    return newAction;
	}
    }
    
    public class DialogActionLoader extends HostsActionLoader
    {
	public IOpenAction getOpenAction()
	{
	    return null;
	}	
    }

    private static HostsPlugin         _instance;
    
    private static ClientConnection    _clientConnection;
    private static ConnectionManager   _connectionManager;

    private static DataStoreCorePlugin _corePlugin;
    private        String              _corePath;

    private static DataStore           _dataStore;
    private        ResourceBundle      _resourceBundle;

    private        IActionLoader       _actionLoader;
    private        IActionLoader       _dialogActionLoader;

    public HostsPlugin(IPluginDescriptor descriptor)
    {
	super(descriptor);
	_corePlugin = com.ibm.dstore.core.DataStoreCorePlugin.getPlugin();
	_corePath = com.ibm.dstore.core.DataStoreCorePlugin.getPlugin().getInstallLocation();	
	_actionLoader = new HostsActionLoader();
	_dialogActionLoader = new DialogActionLoader();
	try
	    {
		_resourceBundle = ResourceBundle.getBundle("com.ibm.dstore.hosts.PluginResources");
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

    public IActionLoader getActionLoader()
    {
	return _actionLoader;
    }    

    public IActionLoader getDialogActionLoader()
    {
	return _dialogActionLoader;
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
	_clientConnection = new ClientConnection("Hosts", 2000);
      	_clientConnection.setLoader(new MinerClassLoader());	
        _dataStore = _clientConnection.getDataStore();
	String install = _corePath;
	
	_dataStore.setAttribute(DataStoreAttributes.A_PLUGIN_PATH, install);

	IWorkspace workbench = (IWorkspace)getPluginWorkspace();
        Path rootPath = (Path)Platform.getLocation();

       	String rootDirectory = rootPath.toString();

	DataElement hostRoot = _dataStore.getHostRoot();
        hostRoot.setAttribute(DE.A_SOURCE, rootDirectory);

       	_clientConnection.setHostDirectory(rootDirectory);	
	_dataStore.setMinersLocation("com.ibm.dstore.miners");
	_clientConnection.localConnect();

	// load schema
	_dataStore.showTicket(_dataStore.getTicket().getName());
	_dataStore.getSchema();
	_dataStore.initMiners();

	extendSchema(_dataStore.getDescriptorRoot());
	
	_instance = this;

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
	
	String location = path + "/plugins/com.ibm.dstore.hosts/";
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
        String file = _corePath + java.io.File.separator + 
	    "com.ibm.dstore.hosts" + java.io.File.separator + 
	    "icons" + java.io.File.separator + name;
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


    // temporary place for extending schema from UI side
    public void extendSchema(DataElement schemaRoot)
    {
	DataStore   dataStore = schemaRoot.getDataStore();
	DataElement dirD      = dataStore.find(schemaRoot, DE.A_NAME, "directory", 1);
	DataElement fsD       = dataStore.find(schemaRoot, DE.A_NAME, "Filesystem Objects", 1);
	DataElement rootD     = dataStore.find(schemaRoot, DE.A_NAME, "root", 1);
	DataElement hostD     = dataStore.find(schemaRoot, DE.A_NAME, "host", 1);
		
	DataElement connect = dataStore.createObject(rootD, DE.T_UI_COMMAND_DESCRIPTOR, 
						     dataStore.getLocalizedString("model.Connect_to"), 
						     "com.ibm.dstore.hosts.actions.HostConnectAction");
        connect.setAttribute(DE.A_VALUE, "C_CONNECT");
	
	DataElement disconnect = dataStore.createObject(rootD, DE.T_UI_COMMAND_DESCRIPTOR, 
							dataStore.getLocalizedString("model.Disconnect_from"), 
							"com.ibm.dstore.ui.connections.DisconnectAction");	 
        disconnect.setAttribute(DE.A_VALUE, "C_DISCONNECT");
	
	DataElement editConnection = dataStore.createObject(rootD, DE.T_UI_COMMAND_DESCRIPTOR, 
						  "Edit Connection", 
						  "com.ibm.dstore.ui.connections.EditConnectionAction");	 
        editConnection.setAttribute(DE.A_VALUE, "C_EDIT");

	DataElement removeConnection = dataStore.createObject(rootD, DE.T_UI_COMMAND_DESCRIPTOR, 
						    dataStore.getLocalizedString("model.Delete_Connection"), 
						    "com.ibm.dstore.ui.connections.DeleteAction");	 
        removeConnection.setAttribute(DE.A_VALUE, "C_DELETE");	

	DataElement selectFile = dataStore.createObject(hostD, DE.T_UI_COMMAND_DESCRIPTOR,
							"File Dialog", 
							"com.ibm.dstore.hosts.actions.SelectFileAction");
	selectFile.setAttribute(DE.A_VALUE, "C_SELECT");

	DataElement fileTransfer = dataStore.createObject(hostD, DE.T_UI_COMMAND_DESCRIPTOR,
							  "File Transfer", 
							  "com.ibm.dstore.hosts.actions.FileTransferAction");

	DataElement findFiles = dataStore.createObject(fsD, DE.T_UI_COMMAND_DESCRIPTOR,
						       "Find Files", 
						       "com.ibm.dstore.hosts.actions.FindFileAction");
	findFiles.setAttribute(DE.A_VALUE, "C_FIND_FILES_ACTION");

	DataElement dictionarySearch = dataStore.createObject(hostD, DE.T_UI_COMMAND_DESCRIPTOR,
							      "Dictionary Search", 
							      "com.ibm.dstore.hosts.actions.SearchDictionaryAction");
	dictionarySearch.setAttribute(DE.A_VALUE, "C_DICTIONARY_SEARCH_ACTION");
    }

}











