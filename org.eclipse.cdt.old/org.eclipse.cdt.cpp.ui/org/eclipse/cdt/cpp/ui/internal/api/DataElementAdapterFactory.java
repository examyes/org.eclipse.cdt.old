package com.ibm.cpp.ui.internal.api;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.extra.internal.extra.*;

import org.eclipse.core.runtime.*;

public class DataElementAdapterFactory implements IAdapterFactory 
{
public Object getAdapter(Object object, Class adapterType) 
  {
    if (adapterType == DataElementAdapter.class) 
      {
	return DataElementAdapter.getInstance();	
      }
    
    return null;
  }
  
  public Class[] getAdapterList() 
  {
    return new Class[] { DataElementAdapter.class };
  }
}
