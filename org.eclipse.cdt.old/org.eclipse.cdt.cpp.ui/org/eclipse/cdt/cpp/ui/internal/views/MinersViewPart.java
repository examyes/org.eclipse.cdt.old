package com.ibm.cpp.ui.internal.views;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.core.model.*;

import org.eclipse.swt.widgets.*;
import org.eclipse.jface.viewers.*; 


public class MinersViewPart extends DataStoreViewPart
{
    public MinersViewPart()
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
	setTitle("Miners - no DataStore selected");
    }
    
    public void doInput(DataStore dataStore)
    {
	DataElement minerRoot = dataStore.getMinerRoot();
	_viewer.fixateOnRelationType(dataStore.getLocalizedString("model.contents"));
	_viewer.setInput(minerRoot);      
	
	setTitle(_plugin.getLocalizedString("MinersViewer.Miners_on") + " " + dataStore.getName());    
    }
  
}










