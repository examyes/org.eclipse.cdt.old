package com.ibm.dstore.ui.widgets;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.extra.internal.extra.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.*;

import java.util.*;
import java.text.Collator;

public class DataElementSorter extends ViewerSorter
{
  private String _property;

  public DataElementSorter(String property)
  {
    super();
    _property = property;  
  }

  public boolean isSorterProperty(java.lang.Object element, java.lang.Object property)
  {
    String propertyStr = (String)property;
    
    if (propertyStr.equals(DE.P_VALUE) || propertyStr.equals(DE.P_NAME) || propertyStr.equals(DE.P_TYPE))	
      {
	return true;
      }
    else
      {
	return false;
      }
  }

    public int category(Object element) 
    {
	if (element instanceof DataElement)
	    {
		DataElement de = (DataElement)element;
		String type = de.getType();
		if (type.equals("statement") || type.equals("variable"))
		    {
			return 1;
		    }
		else
		    {
			return 0;
		    }
	    }
	else
	    {
		return 0;
	    }
    }

  public int compare(Viewer v, Object e1, Object e2) 
  {
    int cat1 = category(e1);
    int cat2 = category(e2);
    
    if (cat1 < cat2)
      return 1;
    if (cat1 > cat2)
      return -1;

    DataElement element1 = (DataElement)e1;
    if (element1.getType().equals("statement") || 
	element1.getType().equals("type"))
	{
	    return -1;
	}

    DataElement element2 = (DataElement)e2;
    
    String name1 = (String)element1.getElementProperty(_property);    
    String name2 = (String)element2.getElementProperty(_property);

    try
	{
	    return collator.compare(name1, name2);
	}
    catch (Exception e)
	{
	    return 0;
	}
  }
  
}
