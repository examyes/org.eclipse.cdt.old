package com.ibm.cpp.ui.internal.views;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.core.model.*;
import com.ibm.cpp.ui.internal.api.*;

import org.eclipse.core.resources.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.viewers.*; 
import org.eclipse.ui.*;

public class LogViewPart extends DataStoreViewPart
{
    public LogViewPart()
    {
	super();
    }
    
    public void createPartControl(Composite parent)
    {
	super.createPartControl(parent);    
    }
    
    public void doClear()
    {
	_viewer.setInput(null);
	setTitle("Log - no DataStore selected");
    }
    
    public void doInput()
    {
	DataStore dataStore = _plugin.getCurrentDataStore();
	DataElement log = dataStore.getLogRoot();
	_viewer.setSorter("null");
	_viewer.setInput(log);

	_viewer.selectRelationship("contents");
	_viewer.selectFilter("all");
	setTitle("Log on " + dataStore.getName());
    } 


}










