package com.ibm.dstore.ui.widgets;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.extra.internal.extra.*;

import org.eclipse.jface.viewers.*;
import java.util.*;

public class ViewFilter extends ViewerFilter
{
  static public final String V_DETAILS = "details";
  private DataElement _type;

  public ViewFilter()
  {
    super();
    _type = null; 
  }

  public ViewFilter(DataElement type)
      {
        super();
        _type = type;
      }

  public ViewFilter(ViewFilter oTemplate)
      {
	_type = oTemplate.getType();
      }

  public Object[] filter(Viewer viewer,
		       Object parent,
		       Object[] input)
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
    DataElement dataElement = (DataElement)element;	
    if (dataElement != null)
      {	
        if (_type != null)
        {
	    dataElement = dataElement.dereference();
	    ArrayList checked = new ArrayList();
            boolean result =  doesExist(_type, dataElement, checked);
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

  public static boolean doesExist(DataElement descriptor, DataElement dataElement, ArrayList checked)
  {
      if (!checked.contains(descriptor))
	  {
	      checked.add(descriptor);
	      if (descriptor != null && dataElement != null && !dataElement.isDeleted())
		  {
		      String dataType  = (String)dataElement.getElementProperty(DE.P_TYPE);
		      String typeName  = (String)descriptor.getElementProperty(DE.P_NAME);
		      String typeType  = (String)descriptor.getElementProperty(DE.P_TYPE);
		      
		      if (dataType != null && typeName != null)
			  {
			      if (dataType.equals(typeName) || typeName.equals("all"))
				  {
				      return true; 
				  }
			      else if (typeType.equals(DE.T_ABSTRACT_OBJECT_DESCRIPTOR))
				  {         
				      for (int i = 0; i < descriptor.getNestedSize(); i++)
					  {
					      DataElement subObject = descriptor.get(i).dereference();
					      if (subObject != null && doesExist(subObject, dataElement, checked))
						  {
						      return true;
						  }
					  }
				  }
			  }
		  }
	  }
      
      return false;
  }


  public void setType(DataElement type)
  {
    _type = type;
  }

  public DataElement getType()
  {
    return _type;
  }

}






