package org.eclipse.cdt.pa.ui.views;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.widgets.*;
import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.cpp.ui.internal.api.*;
import org.eclipse.cdt.cpp.ui.internal.views.*;

import org.eclipse.cdt.pa.ui.PAPlugin;
import org.eclipse.cdt.pa.ui.api.*;

import org.eclipse.core.resources.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.viewers.*; 
import org.eclipse.ui.*;

import java.util.*;

public class PAActionLoader extends CppActionLoader {

    private static PAActionLoader _instance = new PAActionLoader();
	private ResourceBundle _paIconBundle;
	private IOpenAction _openAction;
	
	
    // Constructor
    public PAActionLoader()
    {
	  super();
	  
	  try 
	  {
      	_paIconBundle = ResourceBundle.getBundle("org.eclipse.cdt.pa.ui.IconResources");
      }
      catch (MissingResourceException mre)
      {
       	_paIconBundle = null;
      }

	  provideExternalLoader(new ExternalLoader(PAPlugin.getDefault().getDescriptor().getPluginClassLoader(),
						   "org.eclipse.cdt.pa.ui.*"));
    }
 
 
    public static IActionLoader getInstance()
    {
	 return _instance;
    }
    
    /*
    public Class forName(String source) throws ClassNotFoundException 
    {
     return Class.forName(source);
    }
    */
    
    
    public IOpenAction getOpenAction()
    {
	  if (_openAction == null)
	  {
		_openAction = new org.eclipse.cdt.pa.ui.actions.PAOpenAction(null);
	  }
	  return _openAction;
    }


 	protected String getPropertyString(String obj)
    {
      String iconStr = "";
      try
      {
        if (_paIconBundle != null && obj != null)
        {
          String key = obj.toLowerCase();
          key = key.replace(' ', '_');
            	
          iconStr = _paIconBundle.getString(key);                         
        }
       }
       catch (MissingResourceException mre)
       {         	
         iconStr = super.getPropertyString(obj);         	
       }
      	
       return iconStr;
    }
      
}
