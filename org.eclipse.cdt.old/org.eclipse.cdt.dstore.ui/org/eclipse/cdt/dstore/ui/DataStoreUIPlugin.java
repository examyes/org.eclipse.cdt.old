package com.ibm.dstore.ui;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */
 
import com.ibm.dstore.extra.internal.extra.*;
import com.ibm.dstore.core.model.*;
import com.ibm.dstore.core.util.*;
import com.ibm.dstore.core.client.*;

import org.eclipse.ui.plugin.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*; 
import org.eclipse.jface.resource.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.ui.*;

import java.io.*;
import java.util.*;
import java.net.*;


public class DataStoreUIPlugin extends AbstractUIPlugin
{
  private static DataStoreUIPlugin    _instance;

  public DataStoreUIPlugin(IPluginDescriptor descriptor)
  {
    super(descriptor);
    _instance = this;
    URL url =descriptor.getInstallURL();    
  }

  static public DataStoreUIPlugin getDefault()
  {
    return _instance;
  }

  static public DataStoreUIPlugin getPlugin()
  {
    return _instance;
  }

  static public DataStoreUIPlugin getInstance()
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

  public String getInstallLocation()
  {
    URL baseURL = org.eclipse.core.boot.BootLoader.getInstallURL();
    String path = baseURL.getFile();

    String location = path + "/plugins/com.ibm.dstore.ui/";
    return location;
  }


  public void shutdown() throws CoreException
  {
      IWorkbench desktop = getWorkbench();
      IWorkbenchWindow[] windows = desktop.getWorkbenchWindows();
      for (int a = 0; a < windows.length; a++)
	  {	      
	      IWorkbenchWindow window = windows[a];
	      IWorkbenchPage[] pages = window.getPages();
	      for (int b = 0; b < pages.length; b++)
		  {
		      IWorkbenchPage page = pages[b];
		      IAdaptable input = page.getInput();
		      if (input instanceof DataElement)
			  {
			      page.close();
			  }
		  }
	  }

      super.shutdown();
  }	      
    
    public static String getLocalizedString(String key)
    {
	return null;
    }
}











