package org.eclipse.cdt.dstore.extra.internal.extra;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
 

import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.resource.*;

import java.util.*;


public class DesktopElement implements org.eclipse.ui.model.IWorkbenchAdapter
{  
  private IDataElement _element;
  
  public DesktopElement (IDataElement e)
  {
    _element = e;    
  }

  public IDataElement toElement(Object object)
      {
        IDataElement element = null;
        if (object instanceof IDataElement)
        {
          element = (IDataElement)object;
        }        
        else
        {
          element = _element;
        }
        return element;
      }
  
  public Object[] getChildren(Object o) 
  {
    IDataElement element = toElement(o);
    
    element.expandChildren(true);    
    ArrayList objs = element.getAssociated("contents");
    return objs.toArray();
  }

  public ImageDescriptor getImageDescriptor(Object object) 
  {
      return null;
  }

  public String getLabel(Object o) 
  {
    return (String)_element.getElementProperty("value");
  }

  public Object getParent(Object o) 
  {
      return null;
  }
  
  public static boolean matches(Class aClass)
  {
    return (aClass == org.eclipse.ui.model.IWorkbenchAdapter.class);    
  }

    public static Object getPlatformAdapter(Object obj, Class aClass)
    {
	return org.eclipse.core.runtime.Platform.getAdapterManager().getAdapter(obj, aClass);
    }  
}

