package com.ibm.cpp.ui.internal.api;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
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
