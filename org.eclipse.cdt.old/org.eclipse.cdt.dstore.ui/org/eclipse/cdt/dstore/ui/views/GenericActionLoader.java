package org.eclipse.cdt.dstore.ui.views;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.widgets.*;
import org.eclipse.cdt.dstore.ui.actions.*;

import org.eclipse.cdt.dstore.core.*;
import org.eclipse.cdt.dstore.core.client.*;
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.extra.internal.extra.*; 
import org.eclipse.cdt.dstore.ui.resource.*;

import org.eclipse.ui.*;
import org.eclipse.ui.part.*;

import org.eclipse.jface.viewers.*; 
import org.eclipse.jface.action.*; 
import org.eclipse.jface.resource.ImageRegistry;


import org.eclipse.core.runtime.*; 
import org.eclipse.core.resources.*;

import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.resource.*;

import java.util.*;
import java.lang.reflect.*;
 

public class GenericActionLoader implements IActionLoader
{
    protected   IOpenAction            _openAction;
    protected   CustomAction           _openPerspectiveAction;
    

    public GenericActionLoader()	
    {	
    }
   
    public CustomAction getOpenPerspectiveAction()
    {
	if (_openPerspectiveAction == null)
	    {
		_openPerspectiveAction = loadAction("org.eclipse.cdt.dstore.ui.actions.OpenPerspectiveAction", 
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
   
 
    public void loadCustomActions(IMenuManager menu, DataElement input, DataElement descriptor)
    {
    }
 
 	public String getImageString(String name)
 	{
 		return "";
 	}
 
    public String getImageString(DataElement object)
    {
 		return "";
    }
        
}











