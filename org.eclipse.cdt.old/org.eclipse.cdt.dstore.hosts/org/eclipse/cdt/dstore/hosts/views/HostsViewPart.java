package org.eclipse.cdt.dstore.hosts.views;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.hosts.*;
import org.eclipse.cdt.dstore.hosts.actions.*;

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.ui.views.*;
import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.actions.*;
import org.eclipse.cdt.dstore.ui.widgets.*; 
import org.eclipse.cdt.dstore.ui.connections.*;

import org.eclipse.swt.widgets.*;

import org.eclipse.jface.viewers.*; 
import org.eclipse.jface.action.*; 
import org.eclipse.core.resources.*;
import org.eclipse.ui.*;
import org.eclipse.core.runtime.*; 

import java.lang.reflect.*;

public class HostsViewPart extends GenericViewPart
{
    private HostsPlugin _plugin;
    private DataElement _perspective_input = null;

    public HostsViewPart()
    {
	super();
	_plugin = HostsPlugin.getInstance();
    }
    
    public ObjectWindow createViewer(Composite parent, IActionLoader loader)
    {
	DataStore dataStore = _plugin.getDataStore();
	return new ObjectWindow(parent, ObjectWindow.TREE, dataStore, _plugin.getImageRegistry(), loader);
    }
    
    public IActionLoader getActionLoader()
    {
	IActionLoader loader = _plugin.getActionLoader();
	return loader; 
    }

    
    public void createPartControl(Composite parent)
    {
	super.createPartControl(parent);    
	initInput(_plugin.getDataStore());
    }

    public void initInput(DataStore dataStore)
    {
    	IAdaptable input = getSite().getPage().getInput();
    	
    	if (input != null && input instanceof DataElement)
	    {
    		_perspective_input = (DataElement)input;
    		_viewer.setInput(input);	
	    }
    	else
    	{
	    if (dataStore != null)
		{		    
		    // for this view, the global datastore makes sense
		    DataElement root = dataStore.getRoot();
		    DataElement descriptor = root.getDescriptor();
		    DataElement property = dataStore.findDescriptor(DE.T_RELATION_DESCRIPTOR, "abstracted by");
		    DataElement schema = dataStore.getDescriptorRoot();

		    _viewer.setInput(root);
		    _viewer.selectFilter("Hosts");
		}
    	}    
	 
    }
  
  public void fillLocalToolBar() 
    {
    	if (_perspective_input == null)
    	{
		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
		toolBarManager.add(new ConnectionManager.NewConnectionAction("New Connection", 
			   _plugin.getImageDescriptor("newhost")));
    	}
  }


}

