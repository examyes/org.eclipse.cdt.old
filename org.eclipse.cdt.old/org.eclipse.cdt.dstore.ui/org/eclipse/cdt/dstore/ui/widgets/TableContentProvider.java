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


public class TableContentProvider extends TestContentProvider
{
    private DataElement _property;
    private ViewToolBar _toolBar;

  public TableContentProvider(ViewToolBar toolBar)
  {
    super();
    _property = null;
    _toolBar = toolBar;
  }

  public TableContentProvider()
  {
    super();
    _property = null;
    _toolBar = null;
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
    DataElement element = ((DataElement)object).dereference();

    if (element.isDeleted())
	{
	    return false;
	}

    if ((element.depth() < 2) && ((_property == null) || (_property.getName().equals("contents"))))
	{
	    return false;
	}

    if (element.isExpanded())
    {
      if (element.getNestedSize() > 0)
      {
        return true;
      }
      else
      {
        return false;
      }
    }
    else
    {
      DataElement descriptor = element.getDescriptor();
      
      if (descriptor != null)
      {
        boolean result = false;
        for (int i = 0; i < descriptor.getNestedSize(); i++)
        {
          DataElement subDescriptor = descriptor.get(i).dereference();
          String type = (String)subDescriptor.getElementProperty(DE.P_TYPE);
          if (type.equals(DE.T_OBJECT_DESCRIPTOR))
          {
	    if (_toolBar != null)
	      {		
		DataElement filter = _toolBar.getViewer().getFilter();    
		if (filter != null)
		  {
		      ArrayList checked = new ArrayList();		      
		      result = matchDescriptor(subDescriptor, filter.dereference(), checked);
		      if (result == true)
			  {
			      return true;
			  }
		  }
		else
		  {
		    return true;
		  }
	      }
	    else
	      {
		return true;		
	      }	    
          }
        }
      }
      
      return false;	
    }
  }

  public boolean matchDescriptor(DataElement descriptor, DataElement filter, ArrayList checked)
      {
        if (descriptor == filter)
        {
          return true;
        }
        else if (!checked.contains(filter))
        {
	    checked.add(filter);
	    String filterType = (String)filter.getElementProperty(DE.P_TYPE);
	    if (filterType.equals(DE.T_ABSTRACT_OBJECT_DESCRIPTOR))
		{
		    for (int i = 0; i < filter.getNestedSize(); i++)
			{
			    if (matchDescriptor(descriptor, filter.get(i).dereference(), checked))
				return true;
			}
		}          
        }

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
  
  public Object[] getElements(Object object)
  {
    if (object instanceof DataElement)
    {
      DataElement element = (DataElement)object;
      ArrayList objs = element.getAssociated(_property);      
      return objs.toArray();
    }
    else
    {
      return new Vector(0).toArray();
    }
  }
	
}

