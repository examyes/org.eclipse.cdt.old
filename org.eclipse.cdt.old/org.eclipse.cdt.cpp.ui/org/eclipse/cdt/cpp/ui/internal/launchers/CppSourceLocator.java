package org.eclipse.cdt.cpp.ui.internal.launchers;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.api.*;
import org.eclipse.cdt.dstore.ui.resource.*;
import org.eclipse.cdt.dstore.core.model.*;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IStackFrame;

import com.ibm.debug.internal.picl.PICLDebugTarget;
import com.ibm.debug.internal.picl.PICLStackFrame;
import com.ibm.debug.model.ViewInformation;

import com.ibm.debug.*;

public class CppSourceLocator extends WorkspaceSourceLocator 
{
    private static ModelInterface _api;
    private DataElement _projectElement = null;
    private IProject    _projectResource = null;

    public CppSourceLocator(DataElement projectElement) 
    {
	super();  
	_api = ModelInterface.getInstance();
	_projectElement = projectElement;
	_projectResource = _api.findProjectResource(projectElement);
	setHomeProject(_projectResource);
    }
	   	

    public IFile findFile(String fileName) 
    {
	IFile file = null;
	DataStore dataStore = _projectElement.getDataStore();		
	DataElement found = dataStore.find(_projectElement, DE.A_NAME, fileName);
	if (found != null)
	    {
		file = (IFile)_api.findFile(found.getSource());
		if (file == null)
		    {
			file = new FileResourceElement(found, _projectResource);
			_api.addNewFile(file);
			
		    }
	    }

	return file;
    }
    

}
