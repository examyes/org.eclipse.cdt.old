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


public class HostsActionLoader extends GenericActionLoader
{
   	private static HostsActionLoader _instance;
    	
	private IOpenAction   _openAction;
	private CustomAction  _openPerspectiveAction;
	
	private ResourceBundle _iconBundle;
	private String _defaultIcon;
	private HashMap _hashMap;
	private String _baseDir;

    public HostsActionLoader()
    {
		super(); 

		try
    	{
      	 	_iconBundle = ResourceBundle.getBundle("org.eclipse.cdt.dstore.hosts.IconResources");
    	}
    	catch (MissingResourceException mre)
    	{
       		_iconBundle = null;
    	}
    	
    	_hashMap = new HashMap();
    	_instance = this;    	
      }
      
    public static HostsActionLoader getInstance()
    {
    	return _instance;
    }  

 	public CustomAction getOpenPerspectiveAction()
    {
		if (_openPerspectiveAction == null)
	    {
		_openPerspectiveAction = loadAction("org.eclipse.cdt.dstore.hosts.actions.OpenPerspectiveAction", 
						    "Open Perspective On");
	    }
		return _openPerspectiveAction;
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
	    String name = descriptor.getName();
	    String source = descriptor.getSource();
	    
	    CustomAction newAction = null; 
	    try
		{         
		    Class actionClass = Class.forName(source);

		    Object[] args = {objects, 
		        					name, 
		        					descriptor, 
		        					descriptor.getDataStore()};

		    Class[] parameterTypes = { java.util.List.class, 
		        							name.getClass(), 
		        							descriptor.getClass(),
		        							descriptor.getDataStore().getClass()};

		    Constructor constructor = null;
		    
		    try
		    {
			    constructor = actionClass.getConstructor(parameterTypes);
		    }
		    catch (Exception e)
		    {
		    }
		    
			if (constructor != null) 
			{		
		    	newAction = (CustomAction)constructor.newInstance(args);
		    }
		    else
		    {
		    	return loadAction((DataElement)objects.get(0), descriptor);
	   	    }
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
	
	
       public CustomAction loadAction(DataElement object, DataElement descriptor)
    {
        String name = descriptor.getName();
        String source = descriptor.getSource();
        
        CustomAction newAction = null; 
        try
	    {         
      		Class actionClass = Class.forName(source);
      		Object[] args = {object, name, descriptor, object.getDataStore()};
	
		    Class[] parameterTypes = { object.getClass(), 
		        							name.getClass(), 
		        							descriptor.getClass(),
		        							descriptor.getDataStore().getClass()};

		    Constructor constructor = null;
		    
		    try
		    {
			    constructor = actionClass.getConstructor(parameterTypes);
		    }
		    catch (Exception e)
		    {
		    }
		  
   			if (constructor != null)
   			{
	            newAction = (CustomAction)constructor.newInstance(args);
   			}
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
   

  public String getImageString(DataElement object)
    {   	
  		String type           = object.getType();
		
		if (type.equals(DE.T_OBJECT_DESCRIPTOR) || 
	    	type.equals(DE.T_RELATION_DESCRIPTOR) ||
	    	type.equals(DE.T_ABSTRACT_OBJECT_DESCRIPTOR) ||
	    	type.equals(DE.T_ABSTRACT_RELATION_DESCRIPTOR))
            {
				type = object.getName();
            }
   
   		return getImageString(type);	
    }   
	
    public String getImageString(String type)
    {		                      
    	String result = (String)_hashMap.get(type);
    	
    	if (result == null)
    	{    	
    		if (_baseDir == null)
    		{	   	    
    			_baseDir = org.eclipse.cdt.dstore.core.DataStoreCorePlugin.getPlugin().getInstallLocation();
   
    		}
		
 			result = _baseDir + getPropertyString(type);
			_hashMap.put(type, result);		
    	}
    	
    	return result;
    }

 	private String getPropertyString(String obj)
      {
      	String iconStr = "";
         {
         try
         {
            if (_iconBundle != null && obj != null)
            {
            	String key = obj.toLowerCase();
            	key = key.replace(' ', '_');
            	
               iconStr = _iconBundle.getString(key);          
               
            }
         }
         catch (MissingResourceException mre)
         {
         	// use default
         	if (_defaultIcon == null)
         	{
         		_defaultIcon = "org.eclipse.cdt.dstore.hosts/icons/full/clcl16/blank_misc.gif";
         	}
         	
         	iconStr = _defaultIcon;
         	
         }
      	}
         return iconStr;
      }


	public void loadCustomActions(IMenuManager menu, DataElement input, DataElement descriptor)
	{
	}
     }
    