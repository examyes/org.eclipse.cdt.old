package com.ibm.cpp.ui.internal.views;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.cpp.ui.internal.api.*;
import com.ibm.cpp.ui.internal.*;

import com.ibm.dstore.core.model.*;

import org.eclipse.jface.action.*;
import org.eclipse.core.resources.*;
import org.eclipse.swt.widgets.*;

import org.eclipse.jface.viewers.*; 
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.*;

public class CppProjectsViewPart extends ObjectsViewPart implements ISelectionListener, ICppProjectListener
{ 
    private boolean _inputed = false;

    public CppProjectsViewPart()
    {
	super();
    }
    
    public void createPartControl(Composite parent)
    {
	super.createPartControl(parent);
    }
    
    protected String getF1HelpId()
    {
	return "com.ibm.cpp.ui.cpp_projects_view_context";
    }
    
    public void initInput(DataStore dataStore)
    {
	if (!_inputed)
	    {
		
		setTitle("C/C++ Projects");
		dataStore = _plugin.getDataStore();
		DataElement projectMinerData = dataStore.findMinerInformation("com.ibm.cpp.miners.project.ProjectMiner");
		if (projectMinerData != null)
		    {
			DataElement rootElement = null;
			IAdaptable input = getSite().getPage().getInput();
			if (input instanceof IResource)
			    {
				IResource resource = (IResource)input;
				rootElement = _api.findResourceElement(resource);
			    }
			
			if (rootElement != null)
			    {
				_viewer.setInput(rootElement);
				_viewer.setSorter(DE.P_NAME);

			    }
			
			lock(true);
			_inputed = true;
			return;
		    } 
		_viewer.setInput(null);
	    }
	
    }
    



    public void selectionChanged(IWorkbenchPart part, ISelection sel) 
    {
	if (part == this && (sel instanceof IStructuredSelection))
	    {
		IStructuredSelection ssel = (IStructuredSelection)sel;
		Object object = ssel.getFirstElement();
		if (object instanceof DataElement)
		    {
			DataElement theObject = (DataElement)object;
			DataElement theParent = _api.getProjectFor(theObject);
			
			if (theParent != null)
			    {
				IProject project = _api.findProjectResource(theParent);
				if (project != null)
				    {
					DataStore dataStore = theParent.getDataStore();
					_plugin.setCurrentProject(project);
					_plugin.setCurrentDataStore(dataStore);
				    }
			    }			
		    }
		
	    }
    }


    public void projectChanged(CppProjectEvent event)
    {
	int type = event.getType();
	IProject project = event.getProject();
	switch (type)
	    {
	    case CppProjectEvent.OPEN:
		{
		    _viewer.resetView();
		}
		break;
	    case CppProjectEvent.CLOSE:
	    case CppProjectEvent.DELETE:
		{
		    _viewer.resetView();
		}
		break;
		
	    case CppProjectEvent.COMMAND:
		break;
	    default:
		super.projectChanged(event);
		break;
	    }
    }

}










