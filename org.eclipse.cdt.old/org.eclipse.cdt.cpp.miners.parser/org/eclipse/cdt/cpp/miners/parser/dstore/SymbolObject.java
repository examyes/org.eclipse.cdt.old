//This class stores and encapsulates all the information that is found by the Parser while parsing 
//an specific object...This was mainly introduced to clean up the SymbolTable class which was getting 
//pretty messy.
package com.ibm.cpp.miners.parser.dstore;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.core.model.*;
import java.util.*;

public class SymbolObject
{
 private ArrayList   _variableTypes;
 private ArrayList   _uses;
 private ArrayList   _returnTypes;
 private ArrayList   _arguments;
 public  DataElement object;
 
 
 public SymbolObject()
 {
  _variableTypes  = new ArrayList();
  _uses           = new ArrayList();
  _returnTypes    = new ArrayList();
  _arguments      = new ArrayList();
  object          = null;
 }
  
 public void addVariableType(DataElement de)  { if (de!=null) _variableTypes.add(de); }
 public void addUse(DataElement de)           { if (de!=null) _uses.add(de);          }
 public void addReturnType(DataElement de)    { if (de!=null) _returnTypes.add(de);   } 
 public void addArgument(DataElement de)      { if (de!=null) _arguments.add(de);     } 
  
 public ArrayList getVariableTypes() { return _variableTypes;   }
 public ArrayList getUses()          { return _uses;            }
 public ArrayList getReturnTypes()   { return _returnTypes;     }
 public ArrayList getArguments()     { return _variableTypes;   }
  
 public void reset()
 {
  _uses.clear();
  _returnTypes.clear();
  _arguments.clear();
  _variableTypes.clear();
 }
  
 public DataElement removeVariableType()
 {
  int end = _variableTypes.size() - 1;
  DataElement theElement = null;
  
  if (end >= 0)
  {
    theElement = (DataElement)_variableTypes.get(end);
    _variableTypes.remove(end);
  }
  return theElement;
 }
 
 public void createTypeReferences()
 {
  int lastType = _variableTypes.size() - 1;
  if (lastType >= 0)
   createReferences(_variableTypes, ParserSchema.Types, null);
 }

 public void createUseReferences()
 {
  createReferences(_uses, ParserSchema.Uses, ParserSchema.Uses);
 }

 public void createFunctionReferences()
 {
  createReferences(_returnTypes, ParserSchema.ReturnType, null);
  createReferences(_arguments, ParserSchema.Parameters, null);
 }

 //Take any list of references and create relationships...If there is no fromRelationship..use null.
 private void createReferences(ArrayList refList, String toRelationship, String fromRelationship)
 {
  int refs = refList.size();
  if (refs == 0)
   return;
  DataElement currentObject;
  DataStore   dataStore = object.getDataStore();
  
  for (int i=0; i<refs; i++)
  {
   currentObject = (DataElement)refList.get(i);
   if (fromRelationship == null)
    dataStore.createReference(object, currentObject, toRelationship);
   else 
    dataStore.createReference(object, currentObject, toRelationship, fromRelationship);
  }
 }
}








