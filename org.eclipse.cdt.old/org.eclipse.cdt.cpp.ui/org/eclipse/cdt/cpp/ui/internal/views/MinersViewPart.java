package com.ibm.cpp.ui.internal.views;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
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
    
    public void doInput()
    {
	DataStore dataStore = _plugin.getCurrentDataStore();

	DataElement minerRoot = dataStore.getMinerRoot();
	_viewer.fixateOnRelationType(dataStore.getLocalizedString("model.contents"));
	_viewer.fixateOnObjectType(dataStore.getLocalizedString("model.all"));
	_viewer.setInput(minerRoot);      
	
	setTitle(_plugin.getLocalizedString("MinersViewer.Miners_on") + " " + dataStore.getName());    
    } 
  
}










