package com.ibm.dstore.ui.widgets;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.ui.*;
import com.ibm.dstore.core.model.*;
import com.ibm.dstore.extra.internal.extra.*;

import org.eclipse.core.resources.*;
import org.eclipse.jface.viewers.*;
import java.util.*;

public class DataElementTreeContentProvider extends DataElementContentProvider implements ITreeContentProvider
{
    private DataElement _property;
    private DataElement _containerDescriptor = null;

  public DataElementTreeContentProvider()
  {
    super();
    _property = null;
  }
  

  public Object[] getChildren(Object object)
  {
    DataElement theObject = (DataElement)object;

    if (theObject.getNestedSize() > 0)
      return getElements(object);
    else
      return new Vector(0).toArray();
  }

  public Object getParent(Object object)
  {
    DataElement element = (DataElement)object;
    return element.getParent();
  }

    public DataElement getContainerDescriptor(DataElement object)
    {
	if (_containerDescriptor == null)
	    {
		DataStore dataStore = object.getDataStore();
		_containerDescriptor = dataStore.findDescriptor(DE.T_ABSTRACT_OBJECT_DESCRIPTOR, "Container Object");
	    }

	return _containerDescriptor;
    }

  public boolean hasChildren(Object object)
  {
      if (_property == null)
	  {
	      return false;
	  }

      DataElement element = ((DataElement)object).dereference();
      if (element.isDeleted())
	  {
	      return false;
	  }
      
      DataElement descriptor = element.getDescriptor();      
      if (descriptor != null)
	  {	  
	      if (_property.getName().equals("contents"))
		  {
		      if (descriptor.isOfType(getContainerDescriptor(element)))
			  {
			      if (element.isExpanded())
				  {
				      if (element.getNestedSize() > 0)
					  {
					      return true;
					  }
				      else
					  {
					      if (element.depth() > 1)
						  {
						      return true;
						  }
					      else
						  {
						      return false;
						  }
					  }
				  }
			      else
				  {
				      return true;
				  }
			  }
		      else
			  {
			      return false;
			  }
		  }
	      else
		  {
		      return true;
		  }
	  }
      else
	  {
	      return false;
	  }      
  }

  public void setProperty(DataElement property)
  {
    _property = property;
  }

  public DataElement getProperty()
  {
    return _property;    
  }
  
  public Object[] getElements(Object object)
  {
    if (object instanceof DataElement) 
    {
      DataElement element = (DataElement)object;
      if (!element.isDeleted())
	  {
	      ArrayList objs = element.getAssociated(_property);
	      return objs.toArray();
	  }
    }

    return new Vector(0).toArray();
  }
	
}
 
