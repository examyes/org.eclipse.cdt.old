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
	return true;
    }
    
    public int category(Object element) 
    {
	return 0;
    }

  public int compare(Viewer v, Object e1, Object e2) 
  {
      DataElement element1 = (DataElement)e1;
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
