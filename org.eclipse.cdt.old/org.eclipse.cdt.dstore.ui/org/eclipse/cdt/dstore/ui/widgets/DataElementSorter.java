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
    
    private int         STRING  = 0;
    private int         INTEGER = 1;
    private int         FLOAT   = 2;
    
    private int         _sortFormat;
    

    public DataElementSorter(String property)
    {
	super();
	_sortFormat = STRING;
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

      // handle null cases
      if (element1 == null)
	  {
	      if (element2 == null)
		  {
		      return 0;
		  }
	      else
		  {
		      return _reverseSort ? 1 : -1;
		  }
	  }
      else if (element2 == null)
	  {
	      if (element1 == null)
		  {
		      return 0;
		  }
	      else
		  {
		      return _reverseSort ? -1 : 1;
		  }
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
	      if (_sortFormat == INTEGER)
		  {
		      return compareIntegers(n1, n2);
		  }
		
	      else if (_sortFormat == FLOAT)
		  {		 
		      return compareFloats(n1, n2);
		  }
	    
	      else if (_sortFormat == STRING)
		  {
		      return compareStrings(n1, n2);
		  }
	      else
		  {
		      if (isInteger(n1) && isInteger(n2))
			  {
			      return compareIntegers(n1, n2);
			  }
		      else
			  {
			      return compareStrings(n1, n2);
			  }
		  }
		  
		
	  }
      catch (Exception e)
	  {
	      return 0;
	  }

  }

    private boolean hasFormat(DataElement descriptor, String format)
    {
     if (descriptor != null)
     {
		ArrayList attributes = descriptor.getAssociated("attributes");
		if (attributes.size() > 0)
	    {
			DataElement attribute = (DataElement)attributes.get(0);
			return (attribute.getName().equals(format));
	    }
	 }
	 return false;
    }


    private int compareStrings(String str1, String str2)
    {
	return collator.compare(str1, str2);
    }

    private int compareDates(String date1, String date2)
    {
	return compareIntegers(date1, date2);
    }

    private int compareIntegers(String n1, String n2)
    {
	Integer int1 = null;
	Integer int2 = null;
	try
	    {
		int1 = new Integer(n1);
		int2 = new Integer(n2);
		return int1.compareTo(int2); 
	    }
	catch (NumberFormatException e)
	    {
	    }

	return 0;
    }

    private int compareFloats(String n1, String n2)
    {
	// check for float
	Float float1 = null;
	Float float2 = null;
	try
	    {
		float1 = new Float(n1);
		float2 = new Float(n2);
		return float1.compareTo(float2); 
	    }
	catch (NumberFormatException e)
	    {
	    }

	return 0;
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
	ArrayList format = attributeDescriptor.getAssociated("attributes");
	if (format.size() > 0)
	    {
		DataElement formatDescriptor = (DataElement)format.get(0);
		String formatStr = formatDescriptor.getName();

		if (formatStr.equals("Integer") || formatStr.equals("Date"))
		    {
			_sortFormat = INTEGER;
		    }
		else if (formatStr.equals("Float"))
		    {
			_sortFormat = FLOAT;
		    }
		else
		    {
			_sortFormat = STRING;
		    }		
	    }	
	else
	    {
		_sortFormat = STRING;
	    }
    }  


    private boolean isInteger(String str)
    {
	for (int i = 0; i < str.length(); i++)
	    {
		char c = str.charAt(i);
		if (!Character.isDigit(c))
		    {
			if (i == 0 && c == '-')
			    {
			    }
			else
			    {
				return false;
			    }
		    }
	    }

	return true;
    }

    private boolean isFloat(String str)
    {
	for (int i = 0; i < str.length(); i++)
	    {
		char c = str.charAt(i);
		if (!Character.isDigit(c))
		    {
			if (i == 0 && c == '-')
			    {
			    }
			else if (c == '.')
			    {
			    }
			else
			    {
				return false;
			    }
		    }
	    }

	return true;
    }

}
