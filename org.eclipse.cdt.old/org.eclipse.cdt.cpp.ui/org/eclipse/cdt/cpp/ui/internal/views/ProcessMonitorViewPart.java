package org.eclipse.cdt.cpp.ui.internal.views;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.ui.ConvertUtility;

import org.eclipse.cdt.dstore.ui.widgets.*;
import org.eclipse.cdt.dstore.ui.*; 


import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.extra.internal.extra.*;

import org.eclipse.core.resources.*;
import org.eclipse.ui.views.navigator.*;

import org.eclipse.jface.viewers.*; 
import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;
import java.util.*;

import org.eclipse.ui.*;

public class ProcessMonitorViewPart extends DataStoreViewPart
{
  public ProcessMonitorViewPart()
  {
    super();
  }

    public ObjectWindow createViewer(Composite parent, IActionLoader loader)
    {
	DataStore dataStore = _plugin.getCurrentDataStore();
	return new ObjectWindow(parent, ObjectWindow.TABLE, dataStore, _plugin.getImageRegistry(), loader);
    }

    public void createPartControl(Composite parent)
    {
	super.createPartControl(parent);    
    }

    public void doClear()
    {
	_viewer.setInput(null);
    }

    
    public void doInput(DataStore dataStore)
    {
	DataElement hostRoot = dataStore.getHostRoot();
	DataElement processes = dataStore.find(hostRoot, DE.A_TYPE, "Processes", 1);
	_viewer.setInput(processes);      
    } 
  
}










