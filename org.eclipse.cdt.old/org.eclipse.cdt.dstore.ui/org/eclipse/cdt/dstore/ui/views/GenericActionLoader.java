package com.ibm.dstore.ui.views;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.ui.*;
import com.ibm.dstore.ui.widgets.*;
import com.ibm.dstore.ui.actions.*;

import com.ibm.dstore.core.*;
import com.ibm.dstore.core.client.*;
import com.ibm.dstore.core.model.*;
import com.ibm.dstore.extra.internal.extra.*; 
import com.ibm.dstore.ui.resource.*;

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

    public void loadCustomActions(IMenuManager menu, DataElement input, DataElement descriptor)
    {
    }
 
    public String getImageString(DataElement object)
    {
    		DataStore dataStore   = object.getDataStore();

	String baseDir        = dataStore.getAttribute(DataStoreAttributes.A_PLUGIN_PATH); 			
	String type           = object.getType();

	StringBuffer iconPath = new StringBuffer(baseDir);

	if (type.equals(DE.T_OBJECT_DESCRIPTOR) || 
	    type.equals(DE.T_RELATION_DESCRIPTOR) ||
	    type.equals(DE.T_ABSTRACT_OBJECT_DESCRIPTOR) ||
	    type.equals(DE.T_ABSTRACT_RELATION_DESCRIPTOR))
            {
		type = object.getName();
		String subDir = object.getSource();
		if (subDir.length() > 0)
		    {
			iconPath.append(subDir);
		    }
		else
		    {
			iconPath.append("com.ibm.dstore.core");
		    }
            }
	else
            {
		DataElement descriptor = object.getDescriptor();
		if (descriptor != null)
		    {
			String subDir = descriptor.getSource(); 
			if (subDir.length() > 0)
			    {
				iconPath.append(subDir);
			    }
			else
			    {
				iconPath.append("com.ibm.dstore.core");
			    }
		    }
		else
		    {
			iconPath.append("com.ibm.dstore.ui");
		    }
            }

	iconPath.append(java.io.File.separator);	
	iconPath.append("icons");
	iconPath.append(java.io.File.separator);
	iconPath.append(type);
	iconPath.append(".gif");        

	return iconPath.toString();

    }
        
}











