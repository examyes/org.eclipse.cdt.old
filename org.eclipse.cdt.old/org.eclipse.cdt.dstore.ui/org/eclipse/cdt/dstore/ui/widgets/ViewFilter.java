package com.ibm.dstore.ui.widgets;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.extra.internal.extra.*;

import org.eclipse.jface.viewers.*;
import java.util.*;

public class ViewFilter extends ViewerFilter
{
    static public final String V_DETAILS = "details";
    private DataElement _type;
    private int         _depth;
    private boolean _enableContents = false;

    private ArrayList _notCache;
    private ArrayList _isCache;

    public ViewFilter()
    {
	super();
	_type = null; 
	_notCache = new ArrayList();
	_isCache  = new ArrayList();
	_depth = 5;
    }
    
    public ViewFilter(DataElement type)
    {
        super();
        _type = type;
	_notCache = new ArrayList();
	_isCache = new ArrayList();
	_depth = 5;
    }

  public ViewFilter(ViewFilter oTemplate)
      {
	  super();
	_type = oTemplate.getType();
	_notCache = new ArrayList();
	_isCache = new ArrayList();
	_depth = 5;
      }

    public void setEnableContents(boolean flag)
    {
	_enableContents = flag;
    }  

    public Object[] filter(Viewer viewer, Object parent, Object[] input)
    {
	ArrayList elements = new ArrayList();
	for (int i = 0; i < input.length; i++)
	    {
		DataElement data =  (DataElement)input[i];
		if (select(viewer, data, null))
		    {	
			elements.add(data);
		    }
	    }
	
	return elements.toArray();
    }
    
    public boolean isFilterProperty(Object element, Object property)
    {
	return true;
    }
    
    public boolean select(Viewer viewer, Object element, Object other)
    {
	if (_type == null || _type.getName().equals("all"))
	    {
		return true;
	    }

	DataElement dataElement = (DataElement)element;	
	if (dataElement != null)
	    {	
		if (_type != null)
		    {
			dataElement = dataElement.dereference();
			boolean result =  doesExist(_type, dataElement, _depth);

			if (!result)
			    {
				DataElement notDescriptor = dataElement.getDescriptor();
				addTo(_notCache, notDescriptor);
			    }

			return result;
		    }
		else
		    return true;
	    }
	else
	    {	
		return false;
	    }
    }

  private boolean doesExist(DataElement descriptor, DataElement dataElement, int depth)
  {
      if (depth > 0)
	  {
	      depth--;
	      if (descriptor != null && dataElement != null && !dataElement.isDeleted())
		  {
		      DataElement elementDescriptor = dataElement.getDescriptor();
		      
		      if (_isCache.contains(elementDescriptor))
			  {
			      return true;
			  }
		      else if (!_notCache.contains(elementDescriptor))
			  {	      
			      String dataType  = dataElement.getType();
			      String typeName  = descriptor.getName();
			      String typeType  = descriptor.getType();
			      
			      if (dataType != null && typeName != null)
				  {
				      if (dataType.equals(typeName) || typeName.equals("all"))
					  {
					      addTo(_isCache, elementDescriptor);
					      return true; 
					  }
				      else
					  {
					      if (typeType.equals(DE.T_ABSTRACT_OBJECT_DESCRIPTOR))
						  {
						      if (elementDescriptor != null && 
							  elementDescriptor.isOfType(descriptor, true))
							  {
							      addTo(_isCache, elementDescriptor);
							      return true;
							  }
						      
						      if (_enableContents)
							  {
							      ArrayList containsList = descriptor.getAssociated("contents");
							      for (int i = 0; i < containsList.size(); i++)
								  {
								      DataElement aDes = (DataElement)containsList.get(i);
								      if (aDes != null && 
									  doesExist(aDes, dataElement, depth))
									  {
									      addTo(_isCache, elementDescriptor);
									      return true;
									  }					      
								  }
							  }
						  }
					  }
				  }
			  }	      
		  }
	  }
      
      return false;
  }
    
    private void addTo(ArrayList cache, DataElement descriptor)
    {
	if (descriptor != null && !cache.contains(descriptor))
	    {
		cache.add(descriptor);
	    }
    }

    public void setDepth(int depth)
    {
	_depth = depth;
    }

  public void setType(DataElement type)
  {
      if (_type != type)
	  {
	      _type = type;
	      reset();
	  }
  }
    
    public DataElement getType()
    {
	return _type;
    }


    public void reset()
    {
	_notCache.clear();
	_isCache.clear();
    }
}






