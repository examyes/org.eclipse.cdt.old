package com.ibm.dstore.core.model;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */


// constants for dataelements
public class DE 	     
{
  public static final String P_CHILDREN = "children";
  public static final String P_IMAGE = "org.eclipse.jface.icon";
  public static final String P_LABEL = "label";
  public static final String P_NOTIFIER = "notifier";

  public static final String P_SOURCE_NAME     = "source";
  public static final String P_SOURCE          = "sourcefile";
  public static final String P_SOURCE_LOCATION = "sourcelocation";
  public static final String P_SOURCE_LOCATION_COLUMN = "sourcelocationcolumn";
  public static final String P_NESTED          = "nested";
  public static final String P_BUFFER          = "buffer";
  public static final String P_TYPE            = "type";
  public static final String P_ID              = "id";
  public static final String P_NAME            = "name";
  public static final String P_VALUE           = "value";
  public static final String P_ISREF           = "isRef";
  public static final String P_DEPTH           = "depth";
  public static final String P_ATTRIBUTES      = "attribute";
  public static final String P_FILE            = "file";

  // a reference to another element
  public static final String T_REFERENCE          = "reference";
  public static final String T_COMMAND            = "command";
  public static final String T_UI_COMMAND_DESCRIPTOR  = "ui_commanddescriptor";

  public static final String T_OBJECT_DESCRIPTOR  = "objectdescriptor";
  public static final String T_COMMAND_DESCRIPTOR = "commanddescriptor";
  public static final String T_RELATION_DESCRIPTOR = "relationdescriptor";

  public static final String T_ABSTRACT_OBJECT_DESCRIPTOR = "abstractobjectdescriptor";
  public static final String T_ABSTRACT_COMMAND_DESCRIPTOR = "abstractcommanddescriptor";
  public static final String T_ABSTRACT_RELATION_DESCRIPTOR = "abstractrelationdescriptor";

  // indexes of element attributes
  public static final int    A_TYPE       = 0;
  public static final int    A_ID         = 1;
  public static final int    A_NAME       = 2;
  public static final int    A_VALUE      = 3;
  public static final int    A_SOURCE     = 4;
  public static final int    A_ISREF      = 5;
  public static final int    A_DEPTH      = 6;
  public static final int    A_SIZE       = 7;
}
