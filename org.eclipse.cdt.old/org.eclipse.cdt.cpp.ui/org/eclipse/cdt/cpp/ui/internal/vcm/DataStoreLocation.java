package com.ibm.cpp.ui.internal.vcm;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
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
