package com.ibm.cpp.ui.internal;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.ui.resource.ResourceElement;
import com.ibm.dstore.hosts.*;
import com.ibm.dstore.core.model.*;
import com.ibm.dstore.core.util.*;
import com.ibm.dstore.core.client.*;
import com.ibm.dstore.core.server.*;
import com.ibm.dstore.core.miners.miner.*;
import com.ibm.dstore.ui.connections.*;
import com.ibm.dstore.core.DataStoreCorePlugin;

import com.ibm.cpp.ui.internal.api.*;
import com.ibm.cpp.ui.internal.views.*;
import com.ibm.cpp.ui.internal.editor.*;
import com.ibm.cpp.ui.internal.wizards.*;
import com.ibm.cpp.ui.internal.vcm.*;

import java.io.*;

import org.eclipse.ui.part.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;
import org.eclipse.jface.resource.*;

import org.eclipse.swt.graphics.*;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.*;
import org.eclipse.ui.plugin.*;

import java.io.*;
 import java.util.*;
import java.util.ResourceBundle;

public class CppPlugin extends org.eclipse.ui.plugin.AbstractUIPlugin
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
		
		return miner;
	    }
    }

    private static CppPlugin              _instance;
    private static DataStoreCorePlugin    _corePlugin;
    
    private static ClientConnection       _clientConnection;
    
    private static ModelInterface         _interface;
    private static CppDocumentProvider    _CppDocumentProvider;
    
    private        String                 _pluginPath;
    private        String                 _corePath;
    
    private static IProject               _currentProject;
    private        ResourceBundle         _resourceBundle;
    
    private static DataStore              _hostDataStore;

    private static final String FN_LOCAL_HISTORY= "proj_local.hist";
    private static final String FN_URL_HISTORY= "proj_url.hist";
    private static final String FN_HOST_NAME_HISTORY= "proj_host_name.hist";
    private static final String FN_HOST_PORT_HISTORY= "proj_host_port.hist";
    private static final String FN_HOST_DIR_HISTORY= "proj_host_dir.hist";
    
  public CppPlugin(IPluginDescriptor descriptor)
  {
    super(descriptor);

    _pluginPath = getInstallLocation();

    _hostDataStore = com.ibm.dstore.hosts.HostsPlugin.getPlugin().getDataStore();
    _corePlugin = com.ibm.dstore.core.DataStoreCorePlugin.getPlugin();
    _corePath = com.ibm.dstore.core.DataStoreCorePlugin.getPlugin().getInstallLocation();

    try
    {
       _resourceBundle = ResourceBundle.getBundle("com.ibm.cpp.ui.internal.PluginResources");
    }
    catch (MissingResourceException mre)
    {
       _resourceBundle = null;
    }
  }

  static public CppPlugin getDefault()
  {
    return _instance;
  }

  static public CppPlugin getPlugin()
  {
    return _instance;
  }


  public static IWorkspace getPluginWorkspace()
  {
      return ResourcesPlugin.getWorkspace();
  }

  protected void initializeDefaultPreferences()
  {
  }

  public void startup() throws CoreException
      {
        try
        {
          // register the default adapter for elements
          IAdapterManager manager = Platform.getAdapterManager();
           manager.registerAdapters(new DataElementAdapterFactory(), IFile.class);

	  initDefaultBuildPreference();
          initDataStore();
          super.startup();
        }
        catch (CoreException e)
        {}
      }

    public void initDefaultBuildPreference()
    {
	ArrayList history = readProperty("DefaultBuildInvocation");
	if ((history == null) || (history.size() == 0))
	    {
		String defaultBuild = "gmake all";
		ArrayList list = new ArrayList();
		list.add(defaultBuild);
		writeProperty("DefaultBuildInvocation", list);
	    }
    }

  public void initDataStore()
    {
    if (_instance == null)
      {	
 	_clientConnection = new ClientConnection("C/C++", 20000);
	_clientConnection.setLoader(new MinerClassLoader());	
        DataStore dataStore = _clientConnection.getDataStore();
	dataStore.setMinersLocation("com.ibm.cpp.miners");
        _corePlugin.setRootDataStore(dataStore);

	String install = _corePath;
	
	dataStore.setAttribute(DataStoreAttributes.A_PLUGIN_PATH, install);

	IWorkspace workbench = (IWorkspace)getPluginWorkspace();
        Path rootPath = (Path)Platform.getLocation();

	DataElement hostRoot = dataStore.getHostRoot();
        hostRoot.setAttribute(DE.A_SOURCE, rootPath.toString());
	_clientConnection.setHostDirectory(rootPath.toString());	
	_clientConnection.localConnect();

	_instance = this;

	_interface = new ModelInterface(dataStore);	
	_interface.getDummyShell();
       	_interface.loadSchema();


	initializeProjects();
      }
  }

    private void initializeProjects()
    {
	DataStore dataStore = getDataStore();
	IWorkspace workbench = (IWorkspace)getPluginWorkspace();

	// init all local projects
	IProject[] projects = workbench.getRoot().getProjects();
	for (int i = 0; i < projects.length; i++)
	  {	
	      IProject project = projects[i];
	      if (isCppProject(project))
		  {
		      if (project.isOpen())
			  {
			      _interface.openProject(project);
			      if (i == 0)
				  {
				      _currentProject = project;			
				  }	
			  }
		      else
			  {
			      // handle closed project
			      _interface.initializeProject(project);
			  }
		  }
	  }	

	// init all remote projects
	PlatformVCMProvider provider = PlatformVCMProvider.getInstance();
	provider.getRepositories();
	IProject[] rmtProjects = provider.getKnownProjects();
	if (rmtProjects != null)
	    {
		for (int i = 0; i < rmtProjects.length; i++)
		    {
			IProject project = rmtProjects[i];
			_interface.initializeProject(project);		
		    }
	    }
	RemoteProjectAdapter adapter = new RemoteProjectAdapter(dataStore.getRoot());
	adapter.setChildren(rmtProjects);	


    }

  public String getInstallLocation()
  {
    return getDescriptor().getInstallURL().getFile();
  }


  public void shutdown() throws CoreException
  {
    getDataStore().getDomainNotifier().enable(false);
    getDataStore().getDomainNotifier().setShell(null);

    // close all local projects
    _interface.shutdown();

    // vcm shutdown
    RemoteProjectAdapter.getInstance().close();
    PlatformVCMProvider.getInstance().shutdown(null);

    _clientConnection.disconnect();

    super.shutdown();
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
    
    public String getPluginPath()
    {
	return _corePath;
    }

  public ImageDescriptor getImageDescriptor(String name)
      {
        String file = _corePath + java.io.File.separator + 
	    "com.ibm.cpp.ui" + java.io.File.separator + 
	    "icons" + java.io.File.separator + name;
	return ImageDescriptor.createFromFile(null, file);
      }
    
    public static DataStore getHostDataStore()
    {
	return _hostDataStore;
    }  

  public static DataStore getDataStore()
  {
    return _corePlugin.getRootDataStore();
  }

  public static DataStore getCurrentDataStore()
  {
    return _corePlugin.getCurrentDataStore();
  }

  public void setCurrentDataStore(DataStore dataStore)
  {
    _corePlugin.setCurrentDataStore(dataStore);
  }

  public boolean setCurrentProject(DataElement obj)
    {
	return setCurrentProject(_interface.findProjectResource(obj));
    }

  public boolean setCurrentProject(Repository obj)
  {
    boolean changed = false;
    
    if (obj.isOpen())
	{
	    DataStore dataStore = ((Repository)obj).getDataStore();	
	    if (_currentProject != obj)
		{	
		    _currentProject = obj;
		    setCurrentDataStore(obj.getDataStore());	
		    changed = true;
		}
	}

    return changed;
  }
    

  public boolean setCurrentProject(IResource obj)
  {
    boolean changed = false;
    DataStore dataStore = null;
    IProject project = null;

    if (obj instanceof Repository)
	{
	    dataStore = ((Repository)obj).getDataStore();		
	    project = (IProject)obj;
	}
    else if (obj instanceof ResourceElement)
	{
	    dataStore = ((ResourceElement)obj).getDataStore();		
	    project = _interface.getProjectFor(obj);	
	}
    else if (obj instanceof IResource)
	{
	    dataStore = getDataStore();	
	    if (obj instanceof IProject)
		{
		    project = (IProject)obj;
		}
	    else
		{
		    project  = _interface.getProjectFor(obj);
		}
	}

    if (project != null && project != _currentProject)
      {	
	_currentProject = project;	
	setCurrentDataStore(dataStore);
	changed = true;	
      }

    return changed;

  }

  public static IProject getCurrentProject()
  {
    return _currentProject;
  }

  public static ModelInterface getModelInterface()
  {
    return _interface;
  }


  public static ClientConnection getClient()
  {
    return _clientConnection;
  }


  public static org.eclipse.ui.IWorkbenchWindow getActiveWorkbenchWindow()
  {
    return _instance.getWorkbench().getActiveWorkbenchWindow();
  }

  public static boolean isCppProject(IProject resource)
    {
	if (resource instanceof Repository)
	    {
		return true;	
	    }	
	else if (resource == null)
	    {
		return false;
	    }
	else
	    {
		if (resource.isOpen())
		    {
			IProjectNature nature = null;
			try
			    {
				nature = resource.getNature("com.ibm.cpp.ui.cppnature");
			    }
			catch (CoreException e)
			    {
				//System.out.println(e);
			    }
			
			if (nature != null)
			    {
				//System.out.println("nature for " + resource.getName() + " is " + nature);
				return true;
			    }
			else
			    {
				return false;
			    }
		    }
		else
		    {
			return true;
		    }
	    }
    }

  public static ArrayList readProperty(IResource resource)
      {
        String property = new String("com.ibm.cpp.ui");
        return readProperty(resource, property);
      }

  public static synchronized ArrayList readProperty(IResource resource, String property)
      {
        IPath newPath = resource.getFullPath();
        QualifiedName propertyQName = new QualifiedName(property, newPath.toString());

        ArrayList savedProperty = new ArrayList();
        String propertyString = "";

        try
        {
          propertyString = resource.getPersistentProperty(propertyQName);

          if (propertyString != null && propertyString.length() != 0)
          {
             StringTokenizer st = new StringTokenizer(propertyString, "|", false);
             while (st.hasMoreTokens())
             {
                savedProperty.add(st.nextToken());
             }
           }
        }
        catch (CoreException e)
        {
        }

        return savedProperty;
      }

  public static void writeProperty(IResource resource, ArrayList property)
      {
        String qualifier = new String("com.ibm.cpp.ui");
        writeProperty(resource, qualifier, property);
      }

  public static void writeProperty(IResource resource, String qualifier, ArrayList property)
      {
        IPath newPath= resource.getFullPath();
        QualifiedName propertyQName = new QualifiedName(qualifier, newPath.toString());

        String propertyString = "";
        int size = property.size();
        for (int i=0; i < size; i++)
        {
           propertyString = propertyString.concat((property.get(i)).toString());
           propertyString = propertyString.concat("|");
        }

        try
        {
           resource.setPersistentProperty(propertyQName, propertyString);
        }
        catch (CoreException e)
        {
        }
      }

  public static ArrayList readProperty(int location)
      {
        ArrayList savedProperty = new ArrayList();

        switch (location)
        {
           case CppProjectAttributes.LOCATION_LOCAL:
              	String historyFilePath = _instance.getStateLocation().append(FN_LOCAL_HISTORY).toOSString();
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
                     System.out.println("CppPlugin:readProperty IOException: " +e);
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
      	  break;
        }
        return savedProperty;
      }

  public static void writeProperty(int location, ArrayList property)
      {
        switch (location)
        {
           case CppProjectAttributes.LOCATION_LOCAL:
              	String historyFilePath = _instance.getStateLocation().append(FN_LOCAL_HISTORY).toOSString();
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
                  System.out.println("CppPlugin:writeProjectLocationHistory IOException: " +e);
         		}
      	  break;
        }

      }


  public static synchronized ArrayList readProperty(String preference)
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
			System.out.println("CppPlugin:readProperty IOException: " +e);
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
                  System.out.println("CppPlugin:writeProjectLocationHistory IOException: " +e);
	      }
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

  public CppDocumentProvider getCppDocumentProvider()
  {
     if (_CppDocumentProvider == null)
     {
        _CppDocumentProvider= new CppDocumentProvider();
     }
     return _CppDocumentProvider;
  }

}











