package com.ibm.cpp.ui.internal.views;

import com.ibm.cpp.ui.internal.actions.*;
import com.ibm.cpp.ui.internal.api.*;
import com.ibm.cpp.ui.internal.*;

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
	super(); 
    }
 
    public static IActionLoader getInstance()
    {
	return _instance;
    }
    
    public CustomAction getOpenPerspectiveAction()
    {
	if (_openPerspectiveAction == null)
	    {
		_openPerspectiveAction = loadAction("com.ibm.cpp.ui.internal.actions.OpenPerspectiveAction", 
						    "Open Perspective On");
	    }
	return _openPerspectiveAction;
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
    
        
    public void loadCustomActions(IMenuManager menu, DataElement input, DataElement descriptor)
    {
	String type = input.getType();
	if (type.equals("directory") || type.equals("Project"))
	    {
		// inherit actions from abstract object descriptors
		menu.add(new Separator("Custom Actions"));
	
		MenuManager cascade = new MenuManager("Commands", "Commands");

		CppPlugin plugin = CppPlugin.getDefault();
		ModelInterface api = plugin.getModelInterface();
		IResource res = api.findResource(input);
		if (res != null)
		    {
			ArrayList cmds = plugin.readProperty(res, "Command History");
			for (int i = 0; i < cmds.size(); i++)
			    {
				String str = (String)cmds.get(i);
				cascade.add(new InvocationAction(input, str));				
			    }
		    }

		menu.add(cascade);
	    }
    }
 
    public String getImageString(DataElement object)
    {
	return null;
    }

}











