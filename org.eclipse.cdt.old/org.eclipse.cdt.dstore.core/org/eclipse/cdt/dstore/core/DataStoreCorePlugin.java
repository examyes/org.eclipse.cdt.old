package com.ibm.dstore.core;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

//import com.ibm.dstore.extra.internal.extra.*;
import com.ibm.dstore.core.model.*;
import com.ibm.dstore.core.util.*;
import com.ibm.dstore.core.client.*;

import org.eclipse.ui.plugin.*; 
import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*; 
import org.eclipse.core.internal.plugins.*; 
import org.eclipse.jface.resource.*;
import org.eclipse.swt.graphics.*;

import org.eclipse.core.runtime.*;
import org.eclipse.ui.*;
import java.io.*;
import java.util.*;
import java.net.*;

 

public class DataStoreCorePlugin extends AbstractUIPlugin
{
    private static DataStoreCorePlugin        _instance;
    private static DataStore                  _rootDataStore;
    private static DataStore                  _currentDataStore;
    private static String                     _installLocation;  
    
  public DataStoreCorePlugin(IPluginDescriptor descriptor)
  {
    super(descriptor);
    _instance = this;
    _installLocation = null;
  }

  static public DataStoreCorePlugin getDefault()
  {
    return _instance;
  }

  static public DataStoreCorePlugin getPlugin()
  {
    return _instance;
  }

  static public DataStoreCorePlugin getInstance()
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
          super.startup();
        }
        catch (CoreException e)
        {}
      }

  public static DataStore getRootDataStore()
      {
        return _rootDataStore;
      }

  public static DataStore getCurrentDataStore()
      {
        return _currentDataStore;
      }

  public static void setCurrentDataStore(DataStore ds)
      {
        _currentDataStore = ds;
      }

  public static void setRootDataStore(DataStore ds)
      { 
        _rootDataStore = ds;
        if (_currentDataStore == null)
          _currentDataStore = ds;
      }

  public String getInstallLocation()
  {
      if (_installLocation == null)
	  {
	      String location = ((PluginDescriptor)getDescriptor()).getInstallURLInternal().getPath();
	      File file = new File(location);
	      _installLocation = file.getParentFile().getAbsolutePath() + "/";
	      _installLocation = _installLocation.replace('\\', '/'); 
	  }

      return _installLocation;
  }


  public void shutdown() throws CoreException
  {
    super.shutdown();
  }

 
  public ImageDescriptor getImageDescriptor(String name)
    {
	return getImageDescriptor(name, true); 
    }

  public ImageDescriptor getImageDescriptor(String name, boolean qualify)
      {    
	  String file = name;
	  if (qualify)
	      {
		  String corePath = getInstallLocation() + "com.ibm.dstore.core";   
		  file = corePath + java.io.File.separator + "icons" + java.io.File.separator + name;
	      }
	  
	  if (file != null)
	      {
		  File fileObj = new File(file);
		  if (fileObj.exists())
		      {
			  return ImageDescriptor.createFromFile(null, file);
		      }
		  else
		      {
			  return null;
		      }
	      }
	  return null;
      }

  public Image getImage(String name)
  {
    return getImage(name, false);    
  }
  
  public Image getImage(String name, boolean qualify)
  {
    org.eclipse.jface.resource.ImageRegistry reg = getImageRegistry();
    Image image = reg.get(name);
    if (image == null)
    {
      ImageDescriptor des = getImageDescriptor(name, qualify);
      if (des != null)
	{
	  image = des.createImage();
	  reg.put(name, des);
	}
    }

    return image;
  }

    public static String getLocalizedString(String key)
    {
	if (_currentDataStore != null)
	    return _currentDataStore.getLocalizedString(key);
	else
	    return null;
    }
}











