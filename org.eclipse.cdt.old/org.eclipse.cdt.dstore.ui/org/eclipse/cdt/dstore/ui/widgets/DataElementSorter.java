package org.eclipse.cdt.dstore.ui.widgets;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.extra.internal.extra.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.*;

import java.util.*;
import java.text.Collator;

public class DataElementSorter extends ViewerSorter
{
    private int         _property;
    private DataElement _attribute;
    private boolean     _reverseSort;

    public DataElementSorter(String property)
    {
	super();
	_attribute = null;
	_reverseSort = false;
	
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
     	
      if (_attribute != null)
	  {
	      element1 = getAttributeInstance(element1, _attribute);
	      element2 = getAttributeInstance(element2, _attribute);	      
	  }

      if (element1 == null || element2 == null)
	  {
	      return 0;
	  }

      String name1 = element1.getAttribute(_property);    
      String name2 = element2.getAttribute(_property);
      
      try
	  {
	      String n1 = name1;
	      String n2 = name2;

	      if (_reverseSort)
		  {
		      n1 = name2;
		      n2 = name1;
		  }

	      // check for integers
	      Integer int1 = null;
	      Integer int2 = null;
	      try
		  {
		      int1 = new Integer(n1);
		      int2 = new Integer(n2);
		  }
	      catch (NumberFormatException e)
		  {
		      int1 = null;
		      int2 = null;
		  }

	      // check for float
	      Float float1 = null;
	      Float float2 = null;
	      try
		  {
		      float1 = new Float(n1);
		      float2 = new Float(n2);
		  }
	      catch (NumberFormatException e)
		  {
		      float1 = null;
		      float2 = null;
		  }

	      if (int1 != null && int2 != null)
		  {
		      return int1.compareTo(int2); 
		  }
	      else if (float1 != null && float2 != null)
		  {
		      return float1.compareTo(float2); 
		  }
	      else
		  {
		      return collator.compare(n1, n2);
		  }
	  }
      catch (Exception e)
	  {
	      return 0;
	  }
  }

    private DataElement getAttributeInstance(DataElement root, DataElement descriptor)
    {
	ArrayList attributes = root.getAssociated("attributes");
	for (int i = 0; i < attributes.size(); i++)
	    {
		DataElement attribute = (DataElement)attributes.get(i);
		if (attribute.getType().equals(descriptor.getName()))
		    {
			return attribute;
		    }
	    }
	
	return null;
    }

    public void setSortAttribute(DataElement attributeDescriptor)
    {
	if (attributeDescriptor == _attribute || attributeDescriptor == null)
	    {
		_reverseSort = !_reverseSort;
	    }
	else
	    {
		_reverseSort = false;
	    }
	_attribute = attributeDescriptor;
    }  
}
