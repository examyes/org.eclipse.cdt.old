package com.ibm.cpp.ui.internal.views;

import com.ibm.dstore.ui.*;
import com.ibm.dstore.ui.views.*;
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
 

public class CppActionLoader extends GenericActionLoader
{
    private static CppActionLoader _instance = new CppActionLoader();

    public CppActionLoader()
    {
    }
    
    public static CppActionLoader getInstance()
    {
	return _instance;
    }

    public IOpenAction getOpenAction()
    {
	if (_openAction == null)
	    {
		_openAction = new com.ibm.cpp.ui.internal.actions.OpenEditorAction(null);
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











