package org.eclipse.cdt.pa.ui.api;
 
/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.model.*;

public class PATraceEvent 
{
  // type
  public static final int FILE_CREATED    = 0;
  public static final int FORMAT_CHANGED  = 1; 
  public static final int PROJECT_DELETED = 2;
  public static final int FILE_DELETED    = 3;
  public static final int PROJECT_CHANGED = 4;
  
  private int         _type;  
  private DataElement _object;
  private DataElement _argument;

  public PATraceEvent(int type, DataElement object) 
  {
	_type = type;	
	_object = object;
  }
      
  public PATraceEvent(int type, DataElement object, DataElement argument)
  {
	_type = type;	
	_object = object;
    _argument = argument;
  }
  
  public int getType()
  {
	return _type;    
  }  
    
  public DataElement getObject()
  {
	return _object;
  }
   
  public DataElement getArgument()
  {
    return _argument;
  }
}

