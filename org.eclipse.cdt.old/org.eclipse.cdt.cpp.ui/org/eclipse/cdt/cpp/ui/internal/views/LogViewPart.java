package com.ibm.cpp.ui.internal.views;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
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
	_viewer.setSorter((String)null);
	_viewer.setInput(log);
	setTitle("Log on " + dataStore.getName());
    } 
}










