package com.ibm.cpp.ui.internal.views;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.ui.ConvertUtility;

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.extra.internal.extra.*;

import org.eclipse.core.resources.*;
import org.eclipse.ui.views.navigator.*;

import org.eclipse.jface.viewers.*; 
import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;
import java.util.*;

import org.eclipse.ui.*;

public class SchemaViewPart extends DataStoreViewPart
{
  public SchemaViewPart()
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
	setTitle("Schema - no DataStore selected");
    }
    
    public void doInput()
    {
	DataStore dataStore = _plugin.getCurrentDataStore();
	DataElement schema = dataStore.getDescriptorRoot();
	_viewer.setInput(schema);      
	
	setTitle(_plugin.getLocalizedString("SchemaViewer.Schema_on") + " " + dataStore.getName());    
    } 
  
}










