package org.eclipse.cdt.cpp.ui.internal.vcm;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.lang.reflect.InvocationTargetException;
import org.eclipse.vcm.internal.core.base.IRepositoryLocation;
import java.lang.reflect.Constructor;

public class DataStoreLocation implements IRepositoryLocation 
{
  private String location;
  private String type;
  
  public DataStoreLocation(String type, String location) 
  {
    this.type = type;
    this.location = location;
  }

  public String getLocation() 
  {
    return location;
  }

  public String getType() 
  {
    return type;
  }
}
