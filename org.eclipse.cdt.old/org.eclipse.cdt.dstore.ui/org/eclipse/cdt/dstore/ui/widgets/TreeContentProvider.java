package com.ibm.dstore.ui.widgets;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.ui.*;
import com.ibm.dstore.core.model.*;
import com.ibm.dstore.extra.internal.extra.*;

import org.eclipse.core.resources.*;
import org.eclipse.jface.viewers.*;
import java.util.*;

public class TreeContentProvider extends TestContentProvider implements ITreeContentProvider
{
  private DataElement _property;
  private ViewToolBar _toolBar;

  public TreeContentProvider(ViewToolBar  toolBar)
  {
    super();
    _toolBar = toolBar;
    _property = null;
  }

  public TreeContentProvider()
  {
    super();
    _toolBar = null;
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

  public boolean hasChildren(Object object)
  {
    DataElement element = ((DataElement)object).dereference();

    if (element.isDeleted())
	{
	    return false;
	}

    if ((element.depth() < 2) && ((_property == null) || (_property.getName().equals("contents"))))
	{
	    System.out.println(element.getName() + " does not have children");
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
		  IDataElementViewer viewer = _toolBar.getViewer();		  
		  DataElement filter = viewer.getFilter();    
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
 
