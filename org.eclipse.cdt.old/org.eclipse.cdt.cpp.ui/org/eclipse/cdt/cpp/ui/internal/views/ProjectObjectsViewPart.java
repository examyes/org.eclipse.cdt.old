package com.ibm.cpp.ui.internal.views;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.cpp.ui.internal.api.*;
import com.ibm.cpp.ui.internal.*;

import com.ibm.dstore.core.model.*;

import org.eclipse.jface.action.*;
import org.eclipse.core.resources.*;
import org.eclipse.swt.widgets.*;

import org.eclipse.jface.viewers.*; 
import org.eclipse.ui.*;

public class ProjectObjectsViewPart extends ProjectViewPart
{
    private IProject _input = null;
    
    public ProjectObjectsViewPart()
    {
	super();
    }

    protected String getF1HelpId()
    {
	return "com.ibm.cpp.ui.project_objects_view_context";
    }
    
    public void doClear()
    {
	_input = null;
	_viewer.setInput(null);
	setTitle("Project-Objects");
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
		DataElement declarations = dataStore.find(projectObj, DE.A_NAME, PM.PROJECT_OBJECTS, 1);
		if (declarations != null)
		    {
			_viewer.setInput(declarations);	    
			setTitle(project.getName() + " Project-Objects");   
		    }
	    } 	
    }

}










