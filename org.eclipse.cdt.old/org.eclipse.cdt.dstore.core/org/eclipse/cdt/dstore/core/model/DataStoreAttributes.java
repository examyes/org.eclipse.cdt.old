package com.ibm.dstore.core.model;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import java.lang.*;
import java.util.*;

//
// we'll use this to store any information that a user can change
// related to the model layer
//
public class DataStoreAttributes
{
  public static final int    A_PLUGIN_PATH  = 0;
  public static final int    A_ROOT_NAME    = 1;
  public static final int    A_ROOT_PATH    = 2;
  public static final int    A_HOST_NAME    = 3;
  public static final int    A_HOST_PATH    = 4;
  public static final int    A_HOST_PORT    = 5;
  public static final int    A_LOCAL_NAME   = 6;
  public static final int    A_LOCAL_PATH   = 7;
  public static final int    A_LOG_NAME     = 8;
  public static final int    A_LOG_PATH     = 9;
  public static final int    A_SIZE         = 10;

  private String _attributes[];
 
  public DataStoreAttributes()
      { 
        _attributes = new String[A_SIZE];

        // root
        _attributes[A_ROOT_NAME] = new String("Local");
        _attributes[A_ROOT_PATH] = new String("");

        // log
        _attributes[A_LOG_NAME] = new String("log");
        _attributes[A_LOG_PATH] = new String("log.xml");

	// host
	_attributes[A_HOST_NAME] = new String("");
	_attributes[A_HOST_PATH] = new String("");
	_attributes[A_HOST_PORT] = new String("4033");

	// local
	_attributes[A_LOCAL_NAME] = new String("");
	_attributes[A_LOCAL_PATH] = new String("");	
      }

  public String getAttribute(int attributeIndex)
      {
        return _attributes[attributeIndex];
      }

  public void setAttribute(int attributeIndex, String attribute)
      {
        _attributes[attributeIndex] = new String(attribute);
      }

}
