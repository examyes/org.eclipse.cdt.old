package org.eclipse.cdt.cpp.ui.internal.api;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.extra.internal.extra.*;

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;

public class ResourceAdapterFactory implements IAdapterFactory 
{
    private static Class[] PROPERTIES= new Class[] 
    {
	DataElementAdapter.class
    };


    private ModelInterface _api; 
    public ResourceAdapterFactory(ModelInterface api)
    {
	super();
	_api = api;
    }

    public Object getAdapter(Object object, Class key) 
    {
	if (DataElementAdapter.class.equals(key)) 
	    {
		return DataElementAdapter.getInstance();	
	    }		

	return null;
    }
  
    public Class[] getAdapterList() 
    {
	return PROPERTIES;
    }
}
