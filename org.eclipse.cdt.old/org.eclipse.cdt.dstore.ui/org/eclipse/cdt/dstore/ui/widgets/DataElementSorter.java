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
  private int _property;

    public DataElementSorter(String property)
    {
	super();
	
	 if (property.equals(DE.P_NAME))
	 {
	 	_property = DE.A_NAME;
	 }
	 else if (property.equals(DE.P_VALUE))
	 {
	 	_property = DE.A_VALUE;
	 }	
	 else if (property.equals(DE.P_TYPE))
	 {
	 	_property = DE.A_TYPE;
	 }
	 else if (property.equals(DE.P_SOURCE))
	 {
	 	_property = DE.A_SOURCE;
	 }
	 else
	 {
		_property = DE.A_NAME;  
	 }
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
     	
      String name1 = element1.getAttribute(_property);    
      String name2 = element2.getAttribute(_property);
      
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
