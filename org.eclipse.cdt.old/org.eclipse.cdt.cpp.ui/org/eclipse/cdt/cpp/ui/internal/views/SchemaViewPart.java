package org.eclipse.cdt.cpp.ui.internal.views;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.ui.ConvertUtility;

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
    
    public void doInput(DataStore dataStore)
    {
	DataElement schema = dataStore.getDescriptorRoot();
	_viewer.setInput(schema);      
	
	setTitle(_plugin.getLocalizedString("SchemaViewer.Schema_on") + " " + dataStore.getName());    
    } 
  
}










