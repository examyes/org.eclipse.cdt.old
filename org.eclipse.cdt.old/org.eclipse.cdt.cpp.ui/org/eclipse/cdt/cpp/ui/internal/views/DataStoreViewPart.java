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

public abstract class DataStoreViewPart extends ObjectsViewPart
{
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
	doInput();
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
		}
		break;
	    case CppProjectEvent.CLOSE:
	    case CppProjectEvent.DELETE:
		{
		    doClear();
		}
		break;
		
	    case CppProjectEvent.COMMAND:
		super.projectChanged(event);
		break;

	    default:
		super.projectChanged(event);
		break;
	    }
    }
  
}










