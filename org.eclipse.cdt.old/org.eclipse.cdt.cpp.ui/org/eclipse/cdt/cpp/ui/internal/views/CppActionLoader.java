package org.eclipse.cdt.cpp.ui.internal.views;

import org.eclipse.cdt.cpp.ui.internal.actions.*;
import org.eclipse.cdt.cpp.ui.internal.api.*;
import org.eclipse.cdt.cpp.ui.internal.views.targets.*;
import org.eclipse.cdt.cpp.ui.internal.*;

import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.views.*;
import org.eclipse.cdt.dstore.ui.actions.*;

import org.eclipse.cdt.dstore.core.*;
import org.eclipse.cdt.dstore.core.client.*;
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.core.util.*;
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
      	 	_iconBundle = ResourceBundle.getBundle("org.eclipse.cdt.cpp.ui.internal.IconResources");
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
		_openPerspectiveAction = loadAction("org.eclipse.cdt.cpp.ui.internal.actions.OpenPerspectiveAction", 
						    "Open Perspective On");
	    }
		return _openPerspectiveAction;
    }

    public IOpenAction getOpenAction()
    {
	if (_openAction == null)
	    {
		_openAction = new org.eclipse.cdt.cpp.ui.internal.actions.OpenEditorAction(null);
	    }
	return _openAction;
    }
    

    public Class forName(String source) throws ClassNotFoundException
    {
	return Class.forName(source);
    }

          
    public void loadCustomActions(IMenuManager menu, DataElement input, DataElement descriptor)
    {
	String type = input.getType();
	if (type.equals("directory") || type.equals("Project"))
	    {
		loadCustomResourceActions(menu, input);
	    }
	else
	    {
		loadBrowseActions(menu, input);
	    }

	// add additions item
	menu.add(new Separator("additions"));

	menu.add(new Separator("Properties"));
	if (input.isOfType("file") || type.equals("Closed Project"))
	    {
		menu.add(new OpenPropertiesAction(input, "Properties...", null, input.getDataStore()));
	    }
    }

    private void loadCustomResourceActions(IMenuManager menu, DataElement input)
    {
	CppPlugin plugin = CppPlugin.getDefault();
	ModelInterface api = plugin.getModelInterface();
	IResource res = api.findResource(input);
	if (res != null)
	    {
		// add command history
		menu.add(new Separator("Command History"));
		MenuManager historyCascade = new MenuManager("Command History", "Command History");
		
		ArrayList cmds = plugin.readProperty(res, "Command History");
		for (int i = 0; i < cmds.size(); i++)
		    {
			String str = (String)cmds.get(i);
			historyCascade.add(new InvocationAction(input, str));				
		    }
		
		menu.add(historyCascade);
		
		
		// add targets
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
		
		// add browse perspective action
		if (input.isOfType("Project"))
		    {
			menu.add(new Separator("Perspectives"));			
			MenuManager browseCascade = new MenuManager("Browse", "Browse");
			browseCascade.add(new BrowseProjectAction("Project", input));			
			menu.add(browseCascade);
		    }
	    }			
    }
    
    private void loadBrowseActions(IMenuManager menu, DataElement input)
    {
	// add browse perspective action
	menu.add(new Separator("Perspectives"));			
	MenuManager browseCascade = new MenuManager("Browse", "Browse");			
	
	DataElement descriptor = input.getDescriptor();
	ArrayList relationships = descriptor.getDataStore().getRelationItems(descriptor, null);
	relationships = Sorter.sort(relationships);

	for (int i = 0; i < relationships.size(); i++)
	    {
		DataElement des = (DataElement)relationships.get(i);
		if (des.depth() > 0)
		    {
			browseCascade.add(new BrowseObjectAction(des, input, 
								 "org.eclipse.cdt.cpp.ui.SuperDetailsViewPart"));
		    }
	    }
	
	menu.add(browseCascade);			
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

 	protected String getPropertyString(String obj)
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
         		_defaultIcon = "org.eclipse.cdt.cpp.ui/icons/full/clcl16/blank_misc.gif";
         	}
         	
         	iconStr = _defaultIcon;
         	
         }
      	}
         return iconStr;
      }
}











