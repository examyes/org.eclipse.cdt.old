package org.eclipse.cdt.dstore.ui.widgets;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.extra.internal.extra.*;

import java.util.*;

import org.eclipse.core.resources.*;


public class DataElementTableContentProvider extends DataElementContentProvider
{
    protected DataElement _property;
    
    public DataElementTableContentProvider()
    {
	super();
	_property = null;
    }
    
    public Object[] getChildren(Object object)
    {
	return getElements(object);
    }
    
    public Object getParent(Object object)
    {
	DataElement element = (DataElement)object;
	return element.getParent();
    }
    
    public DataElement getProperty()
    {
	return _property;
    }  
    
    public boolean hasChildren(Object object)
    {
	return false;
    }
    
    public void setProperty(DataElement property)
    {
	_property = property;
    }
    
    public Object getElementAt(Object object, int i)
    {
	if (object instanceof DataElement)
	    {
		DataElement element = (DataElement)object;
		return element.get(i); 	
	    }
	return null;	
    }
    
    public ArrayList getList(DataElement object)
    {
	if (object != null)
	    {
		return object.getAssociated(_property);
	    }
	else
	    {
		return new ArrayList(0);
	    }
    }
    
    public Object[] getElements(Object object)
    {
	if (object instanceof DataElement)
	    {		
		ArrayList objs = getList((DataElement)object);      
		return objs.toArray();
	    }
	else
	    {
		return new Vector(0).toArray();
	    }
    }
    
}

