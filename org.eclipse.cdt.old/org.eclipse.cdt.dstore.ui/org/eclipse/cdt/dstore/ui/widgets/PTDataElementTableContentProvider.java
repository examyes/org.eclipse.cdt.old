package com.ibm.dstore.ui.widgets;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.ui.*;
import com.ibm.dstore.core.model.*;
import com.ibm.dstore.extra.internal.extra.*;

import java.util.*;

import org.eclipse.core.resources.*;


public class PTDataElementTableContentProvider extends DataElementTableContentProvider
{
    private ArrayList _ptDescriptors;
    private ArrayList _pbDescriptors;

    public PTDataElementTableContentProvider()
    {
	super();

	// pass thru these ones
	_ptDescriptors = new ArrayList();

	// pass by these ones (ignore)
	_pbDescriptors = new ArrayList();
    }

    public void addPTDescriptor(DataElement descriptor)
    {
	if (!_ptDescriptors.contains(descriptor))
	    {
		_ptDescriptors.add(descriptor);
	    }
    }

    public void removePTDescriptor(DataElement descriptor)
    {
	if (!_ptDescriptors.contains(descriptor))
	    {
		_ptDescriptors.remove(descriptor);
	    }
    }

    public void addPBDescriptor(DataElement descriptor)
    {
	if (!_pbDescriptors.contains(descriptor))
	    {
		_pbDescriptors.add(descriptor);
	    }
    }

    public void removePBDescriptor(DataElement descriptor)
    {
	if (!_pbDescriptors.contains(descriptor))
	    {
		_pbDescriptors.remove(descriptor);
	    }
    }

    private ArrayList getPTList(DataElement object)
    {
	ArrayList resultList = new ArrayList();
	ArrayList list = object.getAssociated(_property);

	for (int i = 0; i < list.size(); i++)
	    {
		DataElement result = (DataElement)list.get(i);
		DataElement rDescriptor = result.getDescriptor();
		if (rDescriptor != null)
		    {
			if (_pbDescriptors.contains(rDescriptor))
			    {
				// pass by this descriptor
			    }
			else if (_ptDescriptors.contains(rDescriptor))
			    {
				ArrayList subList = getPTList(result);
				resultList.addAll(subList);
			    }
			else
			    {
				resultList.add(result);
			    }
		    }
	    }
	
	return resultList;
    }

    public ArrayList getList(DataElement object)
    {
	if (_ptDescriptors.size() == 0)
	    {
		return super.getList(object);
	    }
	else
	    {
		return getPTList(object);
	    }
    }
    
}

