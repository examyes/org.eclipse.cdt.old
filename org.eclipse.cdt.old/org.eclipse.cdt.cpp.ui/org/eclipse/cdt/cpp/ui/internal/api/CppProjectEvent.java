package com.ibm.cpp.ui.internal.api;
 
/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.core.model.*;
import org.eclipse.core.resources.*;

import java.util.*;

public class CppProjectEvent 
{
    // type
    public static final int CREATE   = 0;
    public static final int OPEN     = 1; 
    public static final int CLOSE    = 2;
    public static final int DELETE   = 3;
    public static final int COMMAND  = 4;
    public static final int VIEW_CHANGE = 5;
    
    public static final int START    = 0;
    public static final int WORKING  = 1;
    public static final int DONE     = 2;

    private IProject    _project;
    private int         _type;  
    private int         _status;
    private DataElement _object;

  public CppProjectEvent(int type, int status, DataElement object, IProject project) 
      {
	_type = type;	
	_status = status;
        _project = project;
	_object = object;
      }

  public CppProjectEvent(int type, int status, IProject project) 
      {
	_type = type;	
	_status = status;
        _project = project;
	_object = null;
      }

  public CppProjectEvent(int type, IProject project) 
      {
	_type = type;	
	_status = DONE;
        _project = project;
	_object = null;
      }

    public IProject getProject()
    {
	return _project;    
    }
    
    public int getType()
    {
	return _type;    
    }  
    
    public int getStatus()
    {
	return _status;
    }

    public DataElement getObject()
    {
	return _object;
    }
}

