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
    private boolean _enableContents = false;

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

    public void setEnableContents(boolean flag)
    {
	_enableContents = flag;
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

  public boolean doesExist(DataElement descriptor, DataElement dataElement, ArrayList checked)
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
				      ArrayList abstractedList = descriptor.getAssociated("abstracts");
				      for (int i = 0; i < abstractedList.size(); i++)
					  {
					      DataElement aDes = (DataElement)abstractedList.get(i);
					      if (aDes != null && doesExist(aDes, dataElement, checked))
						  {
						      return true;
						  }
					      
					  }
				      
				      if (_enableContents)
					  {
					      ArrayList containsList = descriptor.getAssociated("contents");
					      for (int i = 0; i < containsList.size(); i++)
						  {
						      DataElement aDes = (DataElement)containsList.get(i);
						      if (aDes != null && 
							  doesExist(aDes, dataElement, checked))
							  {
							      return true;
							  }					      
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






