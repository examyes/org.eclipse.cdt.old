package com.ibm.cpp.ui.internal.views;

import com.ibm.cpp.ui.internal.actions.*;
import com.ibm.cpp.ui.internal.api.*;
import com.ibm.cpp.ui.internal.views.targets.*;
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
	private ResourceBundle _iconBundle;
	private String _defaultIcon;
	private HashMap _hashMap; 
	private String _baseDir;

    public CppActionLoader()
    {
		super(); 

		try
    	{
      	 	_iconBundle = ResourceBundle.getBundle("com.ibm.cpp.ui.internal.IconResources");
    	}
    	catch (MissingResourceException mre)
    	{
       		_iconBundle = null;
    	}
    	
    	_hashMap = new HashMap();
    	
    	CppPlugin plugin = CppPlugin.getDefault();
    	_baseDir = plugin.getPluginPath();
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
	     System.out.println(e);
	    }
        catch (IllegalAccessException e)
	    {
		System.out.println(e);
	    }
        catch (InvocationTargetException e)
	    {
		System.out.println(e);
	    }
	
        return newAction;
    }
   

          
    public void loadCustomActions(IMenuManager menu, DataElement input, DataElement descriptor)
    {
	String type = input.getType();
	if (type.equals("directory") || type.equals("Project"))
	    {
		// add command history
		CppPlugin plugin = CppPlugin.getDefault();
		ModelInterface api = plugin.getModelInterface();
		IResource res = api.findResource(input);
		if (res != null)
		    {
			menu.add(new Separator("Command History"));
			MenuManager historyCascade = new MenuManager("Command History", "Command History");

			ArrayList cmds = plugin.readProperty(res, "Command History");
			for (int i = 0; i < cmds.size(); i++)
			    {
				String str = (String)cmds.get(i);
				historyCascade.add(new InvocationAction(input, str));				
			    }

			menu.add(historyCascade);
		    }

		// add targets
		if (res != null)
		    {
			IProject project = res.getProject();
			if (project != null)
			    {
				menu.add(new Separator("Targets"));
				MenuManager targetsCascade = new MenuManager("Command Specifications", 
									     "Command Specifications");
				
				TargetsStore targetsStore = TargetsStore.getInstance();
				
				Vector projectList = targetsStore.getProjectList();
				for(int i = 0; i < projectList.size(); i++)
				    {
					RootElement root = (RootElement)projectList.elementAt(i);
					IProject rProject = root.getRoot();
					
					if(project.getName().equals(rProject.getName()))
					    {
						// we found matching root for this project
						Vector targets = root.getTargets();
						for (int t = 0; t < targets.size(); t++)
						    {
							TargetElement target = (TargetElement)targets.get(t);
							String name       = (String)target.getTargetName();
							String workingDir = (String)target.getWorkingDirectory();
							String invocation = (String)target.getMakeInvocation();
					
							if (workingDir.equals(input.getSource()))
							    {
								targetsCascade.add(new InvocationAction(input, name, invocation));
							    }		

						    }

					    }
				    }

				menu.add(targetsCascade);
			    }
		    }
		
	    }
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
         		_defaultIcon = "com.ibm.cpp.ui/icons/full/clcl16/blank_misc.gif";
         	}
         	
         	iconStr = _defaultIcon;
         	
         }
      	}
         return iconStr;
      }
}











