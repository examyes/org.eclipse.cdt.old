package com.ibm.cpp.ui.internal.views;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.cpp.ui.internal.api.*;
import com.ibm.cpp.ui.internal.*;

import com.ibm.dstore.core.model.*;

import org.eclipse.swt.widgets.*;

import org.eclipse.jface.viewers.*; 
import org.eclipse.core.resources.*;
import org.eclipse.ui.*;

import com.ibm.dstore.ui.widgets.*;
import com.ibm.dstore.ui.*;

public class ParsedSourceViewPart extends ProjectViewPart
{
    private IProject _input = null;

    public ParsedSourceViewPart()
    {
	super();
    }
    
    
    protected String getF1HelpId()
    {
	return CppHelpContextIds.PARSED_SOURCE_VIEW;
    }
    
    public void doClear()
    {
	_input = null;
	_viewer.setInput(null);
	setTitle("Parsed-Files");
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
		DataElement parsedSource = dataStore.find(projectObj, DE.A_NAME, PM.PARSED_FILES, 1);
		if (parsedSource != null)
		    {
			_viewer.setInput(parsedSource);	    
			setTitle(project.getName() + " Parsed-Files");   
		    }
	    } 	
    } 
}










