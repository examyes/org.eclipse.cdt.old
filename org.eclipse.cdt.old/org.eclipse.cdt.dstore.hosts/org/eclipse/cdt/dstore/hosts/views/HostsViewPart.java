package com.ibm.dstore.hosts.views;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.hosts.*;
import com.ibm.dstore.hosts.actions.*;

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.ui.views.*;
import com.ibm.dstore.ui.*;
import com.ibm.dstore.ui.actions.*;
import com.ibm.dstore.ui.widgets.*; 
import com.ibm.dstore.ui.connections.*;

import org.eclipse.swt.widgets.*;

import org.eclipse.jface.viewers.*; 
import org.eclipse.jface.action.*; 
import org.eclipse.ui.*;

import java.lang.reflect.*;

public class HostsViewPart extends GenericViewPart
{
    private HostsPlugin _plugin;

    public HostsViewPart()
    {
	super();
	_plugin = HostsPlugin.getInstance();
    }
    
    public ObjectWindow createViewer(Composite parent, IActionLoader loader)
    {
	DataStore dataStore = _plugin.getDataStore();
	return new ObjectWindow(parent, 0, dataStore, _plugin.getImageRegistry(), _plugin.getActionLoader());
    }
    
    public void createPartControl(Composite parent)
    {
	super.createPartControl(parent);    
	fillLocalToolBar();
	initInput(_plugin.getDataStore());
    }

    public void initInput(DataStore dataStore)
    {
	if (dataStore != null)
	    {
		// for this view, the global datastore makes sense
		DataElement root = dataStore.getRoot();
		_viewer.setInput(root);
		_viewer.selectFilter("Hosts");
	    }    
    }
  
  public void fillLocalToolBar() 
    {
	IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
	toolBarManager.add(new ConnectionManager.NewConnectionAction("New Connection", 
			   _plugin.getImageDescriptor("new.gif")));
  }


}

