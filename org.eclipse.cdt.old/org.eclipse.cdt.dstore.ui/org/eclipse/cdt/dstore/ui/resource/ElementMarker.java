package org.eclipse.cdt.dstore.ui.resource;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.model.*;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.resources.*;

import java.util.*;

public class ElementMarker implements IMarker
{
    private long            _id;
    private IResource       _resource;
    private HashMap         _attributes;
    private String          _type;

    public ElementMarker(IResource element, String type)
    {
	_resource = element;
	_type = type;
	_attributes = new HashMap();
	_id = System.currentTimeMillis();
    }

    public DataElement getElement()
    {
    	if (_resource instanceof IDataElementContainer)
    	{
    		return ((IDataElementContainer)_resource).getElement();
    	}
    	return null;
    }

    public void delete() throws CoreException
    {
    }

    public boolean equals(Object object)
    {
	return (this == object);
    }
    
    public boolean exists()
    {
	return true;
    }
    
    public Object getAttribute(String attributeName)
    {
	return _attributes.get(attributeName);
    }
    
    public int getAttribute(String attributeName, int defaultValue)
    {
	Integer loc = (Integer)getAttribute(attributeName);
	if (loc != null)
	    {
		return loc.intValue();
	    }

	return defaultValue;
    }
    
    public String getAttribute(String attributeName, String defaultValue)
    {
	String result = (String)getAttribute(attributeName);
	if (result != null)
	    {
		return result;
	    }

	return defaultValue;
    }
    
    public boolean getAttribute(String attributeName, boolean defaultValue)
    {
	Boolean result = (Boolean)getAttribute(attributeName);
	if (result != null)
	    {
		return true;
	    }
	return defaultValue;
    }
    
    public Map getAttributes() throws CoreException
    {
	return _attributes;
    }
    
    public Object[] getAttributes(String[] attributeNames) throws CoreException
    {
	ArrayList results = new ArrayList();
	for (int i = 0; i < attributeNames.length; i++)
	    {
		Object attribute = getAttribute(attributeNames[i]);
		if (attribute != null)
		    {
			results.add(attribute);
		    }
	    }

	return results.toArray();
    }
    
    public long getId()
    {
	return _id;
    }
    
    public IResource getResource()
    {
	return _resource;
    }
    
    public String getType() throws CoreException
    {
	return _type;
    }
    
    public boolean isSubtypeOf(String superType) throws CoreException
    {
	return true;
    }
    
    public void setAttribute(String attributeName, Object value) throws CoreException
    {
	_attributes.put(attributeName, value);
    }
    
    public void setAttribute(String attributeName, int value) throws CoreException
    {
	setAttribute(attributeName, new Integer(value));
    }
    
    public void setAttribute(String attributeName, boolean value) throws CoreException
    {
	setAttribute(attributeName, new Boolean(value));
    }
    
    public void setAttributes(String[] attributeNames, Object[] values) throws CoreException
    {	
	for (int i = 0; i < attributeNames.length; i++)
	    {
		_attributes.put(attributeNames[i], values[i]);
	    }
    }

    public void setAttributes(Map attributes) throws CoreException
    {
	_attributes = (HashMap)attributes;
    }

    public Object getAdapter(Class aClass)
    {
	return this;
    }
}
