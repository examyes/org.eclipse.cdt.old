package com.ibm.cpp.ui.internal.editor.contentoutliner;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.ui.widgets.*;
import com.ibm.dstore.ui.*;
import com.ibm.dstore.ui.views.*;

import com.ibm.cpp.ui.internal.api.*;
import com.ibm.cpp.ui.internal.*;

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.ui.ILinkable;

import org.eclipse.core.resources.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.viewers.*; 

import org.eclipse.ui.*;

public class SelectedObjectViewPart extends GenericViewPart
{
    private CppPlugin _plugin;
    public SelectedObjectViewPart()
    {
	super();
	_plugin = CppPlugin.getDefault();
    }
    
 
    public ObjectWindow createViewer(Composite parent, IActionLoader loader)
    {
	DataStore dataStore = _plugin.getCurrentDataStore();
	return new ObjectWindow(parent, 0, dataStore, _plugin.getImageRegistry(), this, false);
    }
    
   
  public void initInput(DataStore dataStore)
  {
  }

  public void setFocus()
  {  
      _viewer.setFocus();
  }

}










