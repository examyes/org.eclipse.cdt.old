package org.eclipse.cdt.dstore.ui.widgets;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
 
import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.widgets.*;
import org.eclipse.cdt.dstore.ui.dialogs.*;
import org.eclipse.cdt.dstore.ui.actions.*;
import org.eclipse.cdt.dstore.ui.dnd.*; 

import org.eclipse.cdt.dstore.core.client.*;
import org.eclipse.cdt.dstore.core.model.*;

import org.eclipse.cdt.dstore.extra.internal.extra.*;

import org.eclipse.jface.resource.*;

import org.eclipse.core.runtime.IAdaptable;
import java.util.*;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.dnd.*;

import org.eclipse.jface.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.*;

import org.eclipse.core.resources.*;
import org.eclipse.ui.part.*;

public class MenuHandler
{
    private IActionLoader  _loader;
    private ResourceBundle _resourceBundle;
    private CustomAction   _openPerspectiveAction;

    public MenuHandler(IActionLoader loader)
    {
	_loader = loader;

	// setup resource bundle
	try
	    {
		_resourceBundle = ResourceBundle.getBundle("org.eclipse.cdt.dstore.ui.UIResources");
	    }
	catch (MissingResourceException mre)
	    {
		_resourceBundle = null;
	    }
    }

    public void setActionLoader(IActionLoader loader)
    {
	_loader = loader;
    }

    public String getLocalizedString(String key)
    {
	try
	    {
		if (_resourceBundle != null && key != null)
		    {
			return _resourceBundle.getString(key);
		    }
	    }
	catch (MissingResourceException mre)
	    {
	    }
	
	return "";
    }

    public void multiFillContextMenu(IMenuManager menu, IStructuredSelection es)
    {
	// test for union of commands       
	java.util.List list = es.toList();

	DataElement first = (DataElement)es.getFirstElement();

	DataElement mergeDescriptor = first.dereference().getDescriptor();
	
	for (int i = 1; i < list.size(); i++)
	    {
		DataElement selectedElement = ((DataElement)list.get(i)).dereference();		
		DataElement descriptor = selectedElement.getDescriptor();
		mergeDescriptor = mergeDescriptors(mergeDescriptor, descriptor);
	    }
	
	fillContextMenuHelper(menu, list, mergeDescriptor);		  		  
    }  

    private DataElement mergeDescriptors(DataElement merge,  DataElement descriptor)
    {	
	if (merge == null)
	    {
		return merge;
	    }
	else if ((descriptor == null) || (merge == descriptor))
	    {
		return merge;
	    }
	else 
	    {
		DataStore dataStore = merge.getDataStore();

		if (descriptor.isOfType(merge, true))
		    {
			return merge;
		    }
		else if (merge.isOfType(descriptor, true))
		    {
			merge = descriptor;
		    }
		else 
		    {
			ArrayList abstractsMerge = merge.getAssociated(dataStore.getLocalizedString("model.abstracted_by"));
			for (int i = 0; i < abstractsMerge.size(); i++)
			    {
				DataElement superDescriptor = ((DataElement)abstractsMerge.get(i)).dereference();
				if (descriptor.isOfType(superDescriptor, true))
				    {
					merge = superDescriptor;
					return merge;
				    }
			    }


			ArrayList abstractsDescriptor = descriptor.getAssociated(dataStore.getLocalizedString("model.abstracted_by"));
			for (int i = 0; i < abstractsDescriptor.size(); i++)
			    {
				DataElement superDescriptor = ((DataElement)abstractsDescriptor.get(i)).dereference();
				if (merge.isOfType(superDescriptor, true))
				    {
					merge = superDescriptor;
					return merge;
				    }
			    }

			merge = null;
		    }
	    }	

	return merge;
    }


  public void fillContextMenu(IMenuManager menu, DataElement input, DataElement selected)
      {
        if (selected == null)
        {
          selected = input;
        }

	if (selected != null)
	  {	    
	
	    menu.add(new Separator(getLocalizedString("ui.Object_Actions")));	    
	    DataElement descriptor = selected.getDescriptor();

	    ArrayList list = new ArrayList();
	    list.add(selected);


	    fillContextMenuHelper(menu, list, descriptor);

	    if (_loader != null)
		{
		    _loader.loadCustomActions(menu, selected, descriptor); 
		    	
		   	menu.add(new Separator("additions"));

		    /*** DKM - no need for this anymore
		    if (_openPerspectiveAction == null)
			{
				_openPerspectiveAction = _loader.getOpenPerspectiveAction();
		  	}
	      
	      	if (_openPerspectiveAction != null)
		  	{
		      _openPerspectiveAction.setSubject(selected);
		      
		      menu.add(new Separator("Perspective Actions"));
		      menu.add(_openPerspectiveAction);
		  	}
		    ****/
		}
	  }
	
      }

  public void fillContextMenuHelper(IMenuManager menu, List objects, DataElement descriptor)
      {
	if (objects.size() > 0)
	  {    	
	    if (descriptor != null)
	      {	
		  DataStore dataStore = descriptor.getDataStore();
		  
		  // add actions for contained command descriptors
		  ArrayList subDescriptors = descriptor.getAssociated(dataStore.getLocalizedString("model.contents"));		  
		  for (int i = 0; i < subDescriptors.size(); i++)
		  { 
		      DataElement subDescriptor = (DataElement)subDescriptors.get(i);

		      String type = subDescriptor.getType();		      

		      if (type.equals(DE.T_COMMAND_DESCRIPTOR) && (subDescriptor.depth() > 0))
			  {
			      String name = subDescriptor.getName();
			      menu.add(new UICommandAction(objects, name, subDescriptor, descriptor.getDataStore()));
			  }
		      else if (type.equals(DE.T_ABSTRACT_COMMAND_DESCRIPTOR))
			  {
			      String name = subDescriptor.getName();
			      MenuManager cascade = new MenuManager(name, name);
			      fillContextMenuHelper(cascade, objects, subDescriptor);
			      menu.add(cascade);
			  }
		      else if (type.equals(DE.T_UI_COMMAND_DESCRIPTOR))
			  {
			      if (_loader != null)
				  {
				  		CustomAction action = null;
				  		int size = objects.size();
				  		if (size == 1)
				  		{
				  		  action = _loader.loadAction((DataElement)objects.get(0), subDescriptor);
				  		}
				  		else
				  		{
				      	 action = _loader.loadAction(objects, subDescriptor);
				  		}
				      if (action != null)
				      {
				      	if (size > 1 && !action.supportsMultiple())
				      	{
				      		// don't add action
				      	}
				      	else
				      	{
					  		menu.add(action);
				      	}
				      }
				  }
			  }
		  }

		  // inherit actions from abstract object descriptors
		  menu.add(new Separator(dataStore.getLocalizedString("model.abstracted_by")));

		  ArrayList baseDescriptors = descriptor.getAssociated(dataStore.getLocalizedString("model.abstracted_by"));
		  for (int j = 0; j < baseDescriptors.size(); j++)
		      {
			  DataElement baseDescriptor = (DataElement)baseDescriptors.get(j);
			  fillContextMenuHelper(menu, objects, baseDescriptor);			  
		      }
	      }
	  }
      }
}

