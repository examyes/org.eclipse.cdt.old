package com.ibm.dstore.extra.internal.extra;
 
/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.ui.views.properties.*;


import java.util.*; 

public class PropertySource implements IPropertySource
{
  private IDataElement   _dataElement;
  private static ArrayList _descriptors;
  
  public PropertySource(IDataElement element)
  {
    _dataElement = element;
    
    _descriptors = new ArrayList();
    _descriptors.add(new TextPropertyDescriptor("type", "type"));
    _descriptors.add(new TextPropertyDescriptor("name", "name"));
    _descriptors.add(new TextPropertyDescriptor("value", "value"));
    _descriptors.add(new TextPropertyDescriptor("id", "id"));
       _descriptors.add(new TextPropertyDescriptor("source", "source"));
  }

  public static boolean matches(Class aClass)
  {
    return (aClass == org.eclipse.ui.views.properties.IPropertySource.class);    
  }
  

  public Object getEditableValue()
  {
    return this;
  }

  public IPropertyDescriptor[] getPropertyDescriptors()
  {
      if (_descriptors.size() > 0)
	  {
	      IPropertyDescriptor [] results = new IPropertyDescriptor[_descriptors.size()];
	      for (int i = 0; i < _descriptors.size(); i++)
		  {
		      results[i] = (IPropertyDescriptor)_descriptors.get(i);
		  }
	      return results;
	  }
      else
	  return null;
  }

  public Object getPropertyValue(Object name)
    {
	return getPropertyValue((String)name);
    }

  public Object getPropertyValue(String name)
  {
    return _dataElement.getElementProperty(name);
  }

  public boolean isPropertySet(Object property)
  {
      return isPropertySet((String)property);
  }

  public boolean isPropertySet(String property)
  {
    return false;
  }

  public void resetPropertyValue(Object property)
    {
    }

  public void resetPropertyValue(String property)
  {
  }

  public void setPropertyValue(Object name, Object value)
    {
	setPropertyValue((String)name, value);
    }

  public void setPropertyValue(String name, Object value)
  {
  }

}
