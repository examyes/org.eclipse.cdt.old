package org.eclipse.cdt.pa.ui;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */


import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.ui.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.ui.*;
import org.eclipse.ui.plugin.*;

import org.eclipse.cdt.cpp.ui.internal.CppPlugin;
import org.eclipse.cdt.pa.ui.api.*;
import org.eclipse.cdt.pa.ui.views.PAActionLoader;

import java.util.*;


public class PAPlugin extends AbstractUIPlugin {
    
  // Plugin attributes  
  private static PAPlugin               _instance;
  private static CppPlugin              _cppPlugin;    
  private static PAModelInterface       _interface;
    
  private        String                 _pluginPath;
  private        ResourceBundle         _resourceBundle;
    

  // Constructor  
  public PAPlugin(IPluginDescriptor descriptor)
  {
    super(descriptor);

    _pluginPath = getInstallLocation();

    _cppPlugin = org.eclipse.cdt.cpp.ui.internal.CppPlugin.getPlugin();

    try
    {
       _resourceBundle = ResourceBundle.getBundle("org.eclipse.cdt.pa.ui.PluginResources");
    }
    catch (MissingResourceException mre)
    {
       _resourceBundle = null;
    }
  }

  static public PAPlugin getDefault()
  {
    return _instance;
  }

  static public PAPlugin getPlugin()
  {
    return _instance;
  }

  public PAModelInterface getModelInterface() {
   return _interface;
  }

  public static IWorkspace getPluginWorkspace()
  {
      return ResourcesPlugin.getWorkspace();
  }

  protected void initializeDefaultPreferences()
  {
  }


  public void startup() throws CoreException {
  
	if (_instance == null) {	
	 _instance = this;
	}
	
	super.startup();
	
	_interface = new PAModelInterface(getDataStore());
	_interface.extendSchema(getDataStore().getDescriptorRoot());
	CppPlugin.getDefault().provideExternalLoader(new ExternalLoader(getDescriptor().getPluginClassLoader(),
							   "org.eclipse.cdt.pa.ui.*"));
	
  }


  public String getInstallLocation()
  {
    return getDescriptor().getInstallURL().getFile();
  }

  public String getPluginPath()
  {
    return _cppPlugin.getPluginPath();
  }
  

  public void shutdown() throws CoreException
  {
    super.shutdown();
  }


  public String getLocalizedString(String key)
  {
    try {
      if (_resourceBundle != null && key != null)
       return _resourceBundle.getString(key);
    }
    catch (MissingResourceException e) {
    }
    
    return "";
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
	PAActionLoader loader = (PAActionLoader)PAActionLoader.getInstance();
	String file = loader.getImageString(name);
	
	return ImageDescriptor.createFromFile(null, file);
  }
    
  
  public static DataStore getHostDataStore()
  {
   return _cppPlugin.getHostDataStore();
  }  

  public static DataStore getDataStore()
  {
    return _cppPlugin.getDataStore();
  }

  public static DataStore getCurrentDataStore()
  {
    return _cppPlugin.getCurrentDataStore();
  }

  public void setCurrentDataStore(DataStore dataStore)
  {
    _cppPlugin.setCurrentDataStore(dataStore);
  }


  public IWorkbench getWorkbench()
  {
    return _cppPlugin.getWorkbench();
  }
    
  
  public static org.eclipse.ui.IWorkbenchWindow getActiveWorkbenchWindow()
  {
    return _instance.getWorkbench().getActiveWorkbenchWindow();
  }

 
  public ResourceBundle getResourceBundle()
  {
   return _resourceBundle;
  }

}











