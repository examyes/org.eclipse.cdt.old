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

public abstract class DataStoreViewPart extends ObjectsViewPart
{
    private DataStore _inputDataStore;

    public DataStoreViewPart()
    {
	super();
    }

    public void createPartControl(Composite parent)
    {
	super.createPartControl(parent);    
    }
    
    public void initInput(DataStore dataStore)
    {
	if (dataStore != _inputDataStore)
	    {
		doInput();
		_inputDataStore = dataStore;
	    }
    }

    public abstract void doClear();
    public abstract void doInput();
    
    public void projectChanged(CppProjectEvent event)
    {
	int type = event.getType();
	IProject project = event.getProject();
	switch (type)
	    {
	    case CppProjectEvent.OPEN:
		{
		    doInput();
		    _viewer.resetView();
		}
		break;
	    case CppProjectEvent.CLOSE:
	    case CppProjectEvent.DELETE:
		{
		    doClear();
		    _viewer.resetView();
		}
		break;
		
	    default:
		super.projectChanged(event);
		break;
	    }
    }
  
}










