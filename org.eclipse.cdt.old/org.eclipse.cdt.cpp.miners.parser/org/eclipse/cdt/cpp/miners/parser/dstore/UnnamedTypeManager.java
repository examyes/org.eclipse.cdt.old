package com.ibm.cpp.miners.parser.dstore;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

/**
 * This calss is responsible for generating unique names
 * for all unnamed datatypes.
 */
public class UnnamedTypeManager
{
  private static UnnamedTypeManager _instance = null;
  
  private int _classId  = 0;
  private int _structId = 0;
  private int _unionId  = 0;
  private int _enumId   = 0;
  private int _namespaceId  = 0;
  
  private UnnamedTypeManager() {}
  
  public static UnnamedTypeManager instance()
  {
   if (_instance == null)
   {
    _instance = new UnnamedTypeManager();
   }
   return _instance;
  }
  
  public String getNextClass()
  {
   return "_Unnamed_class_" + String.valueOf(++_classId);
  }

  public String getNextStruct()
  {
   return "_Unnamed_struct_" + String.valueOf(++_structId);
  }

  public String getNextUnion()
  {
   return "_Unnamed_union_" + String.valueOf(++_unionId);
  }

  public String getNextEnum()
  {
   return "_Unnamed_enum_" + String.valueOf(++_enumId);
  }
  
  public String getNextNamespace()
  {
   return "_Unnamed_namespace_" + String.valueOf(++_namespaceId);
  }

  public String getNextUnnamedType(String type)
  {
   if (type.equals("class"))
    return getNextClass();
   else if (type.equals("struct"))
    return getNextStruct();
   else if (type.equals("union"))
    return getNextUnion();
   else if (type.equals("enum"))
    return getNextEnum();
   else if (type.equals("namespace"))
    return getNextNamespace();
   else
    return "_Unnamed_unknown_type";	
  }
  
  public void reset()
  {
   _classId = 0;
   _structId = 0;
   _unionId = 0;
   _enumId = 0;
   _namespaceId = 0;
  }
  
}
