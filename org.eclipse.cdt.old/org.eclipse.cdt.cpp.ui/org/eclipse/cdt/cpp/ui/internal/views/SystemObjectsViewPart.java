package com.ibm.cpp.ui.internal.views;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.cpp.ui.internal.api.*;
import com.ibm.cpp.ui.internal.*;

import com.ibm.dstore.core.model.*;

import org.eclipse.swt.widgets.*;
import org.eclipse.core.resources.*;

import org.eclipse.jface.viewers.*; 
import org.eclipse.ui.*;

public class SystemObjectsViewPart extends ProjectViewPart
{
    private IProject _input = null;

  public SystemObjectsViewPart()
  {
    super();
  }
 
  protected String getF1HelpId()
  {
   return CppHelpContextIds.SYSTEM_OBJECTS_VIEW;
  }

    
    public void doClear()
    {
	_input = null;
	_viewer.setInput(null);
	setTitle("System-Objects");
    }

    public void doInput(IProject project)
    {
	DataStore dataStore = _plugin.getCurrentDataStore();
	_input = project;
	ModelInterface api = _plugin.getModelInterface();
	DataElement projectObj = api.findProjectElement(project);
	
	if (projectObj != null)
	    {
		dataStore = projectObj.getDataStore();
		DataElement declarations = dataStore.find(projectObj, DE.A_NAME, PM.SYSTEM_OBJECTS, 1);
		if (declarations != null)
		    {
			_viewer.setInput(declarations);	    
			setTitle(project.getName() + " System-Objects");   
		    }
	    } 	
    }
}










