package org.eclipse.cdt.cpp.ui.internal.api;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.extra.internal.extra.*;
import org.eclipse.cdt.cpp.ui.internal.*;

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;

public class DataElementAdapterFactory implements IAdapterFactory 
{
    private ModelInterface _api; 

    private static Class[] PROPERTIES= new Class[] {
	IResource.class,
	IProject.class,
	IFile.class
    };

    public DataElementAdapterFactory(ModelInterface api)
    {
	super();
	_api = api;
    }

    public Object getAdapter(Object object, Class key) 
    {
	if (object instanceof DataElement)
	    {
		DataElement element = (DataElement)object;
		DataStore dataStore = element.getDataStore();
		if (dataStore == CppPlugin.getDataStore())
		    {
			if (IResource.class.equals(key)) 
			    {
				return getResource(element);
			    } 
			if (IFile.class.equals(key)) 
			    {
				return getFile(element);
			    } 
			if (IProject.class.equals(key)) 
			    {
				return getProject(element);
			    } 
		    }
	    }

	return null;
    }

    public IFile getFile(DataElement element)
    {
	if (element.getType().equals("file"))
	    {
		IFile file = (IFile)_api.findResource(element);
		return file;
	    }
	return null;
    }

    public IProject getProject(DataElement element)
    {
	if (element.getType().equals("Project"))
	    {
		IProject project = _api.findProjectResource(element);
		return project;
	    }
	return null;
    }

    public IResource getResource(DataElement element)
    {
	if (element.isOfType("file"))
	    {
		IResource resource = _api.findResource(element);
		return resource;
	    }
	return null;	
    }

  
    public Class[] getAdapterList() 
    {
	return PROPERTIES;
    }
}
