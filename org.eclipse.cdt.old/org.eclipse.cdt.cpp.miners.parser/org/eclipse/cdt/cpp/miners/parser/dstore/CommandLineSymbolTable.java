package com.ibm.cpp.miners.parser.dstore;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.core.model.*;
import com.ibm.cpp.miners.parser.namelookup.*;
import java.lang.*;
import java.util.*;

public class CommandLineSymbolTable implements SymbolTable
{
 private String        _indent;
 private DataElement   _dummy;
 
 public CommandLineSymbolTable()
 {
  _indent    = "";
  _dummy     = new DataElement(); 
  _dummy.reInit(null, "B", "C", "D", "E", false);
 }
 
 public DataElement addObject(DataElement a, String name, int line, boolean hasChildred)
 {
  return _dummy;
  
 }
 
 public DataElement addObject(DataElement a, String name, String value, int line, boolean hasChildred)
 {
  return _dummy;
 }
 
 //Methods that actually create or deal with DataElement in the dataStore.
 public DataElement addObject(String type, String name, int line, boolean hasChildren)
 {
  emit(type + " " + name);
  if (hasChildren)
  {
   _indent += "|  ";
  }
  return _dummy;
 }
   
 public void closeScope() 
 {
  if (_indent.length() == 0)
   return;
  _indent = _indent.substring(0,_indent.length()-3);
  emit("");
 }
 
 public void gotoGlobalScope(){};
   
 //Methods that add reference information
 public void addObjectUse(String name) {};
 public void addFunctionParameter(String name) {};
 public void addFunctionReturnTypes(String name) {};
 public void addFunctionCall(String name, String params) {};
 public void addClassBase(String name) {};
 public void addClassMember(String name) {};
 public void addIncludeFile(String name) {};

 public DataElement getRoot()  { return null; }
  
 //Get rid of this...it is currently need in the declaration() production..but it is a crappy way of doing it!!!!!
 public boolean doBodies()  { return false; }
 public void setObjectTypedef(boolean isTypedef) {}
 public boolean isObjectTypedef() {return false;}
 public void setCurrentDeclaration(DataElement type) {}
 
 private void emit(String s)
 {
  System.out.println(_indent + s);
 }
}


