package com.ibm.cpp.miners.parser.codeassist;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.core.model.*;
import com.ibm.cpp.miners.parser.dstore.*;
import com.ibm.cpp.miners.parser.namelookup.*;
import com.ibm.cpp.miners.parser.invocation.*;

import java.util.*;

public class CodeAssist
{
 private DataStore    _dataStore;
 private NameLookup   _nameLookup;
 private ParseManager _parseManager;
 
 public CodeAssist(ParseManager pm)
 {
  _parseManager = pm;
  _dataStore  = null;
  _nameLookup = new NameLookup();
 }

 public void setProject(DataElement theProject)
 {
  if (theProject == null) 
   return;
  _dataStore  = theProject.getDataStore();
  _nameLookup.setProject(theProject);
 }
 
 public DataElement doFindDeclaration(DataElement theProject, DataElement patternElement, DataElement status)
 {
  ArrayList results = _nameLookup.getClosestObjects(patternElement.getSource());
  if ((results == null) || results.isEmpty())
   return status;
 
  //Should really look at all matches from the line...but for now we'll just do the first.
  DataElement theElement = (DataElement)results.get(0);
  ArrayList uses = theElement.getAssociated("Uses");
  if ((uses == null) || uses.isEmpty())
   return status;
  
  for (int i=0; i<uses.size(); i++)
  {
   DataElement theUse = ((DataElement)uses.get(i)).dereference();
   if (patternElement.getValue() == theUse.getValue())
   {
    status.addNestedData(theUse,true);
    return status;
   }
  }
  
  status.addNestedData(((DataElement)uses.get(0)).dereference(),true);
  return status;
 }
 

 public DataElement doCodeAssist(DataElement theProject, DataElement patternElement,DataElement status)
 { 
  ArrayList results = new ArrayList();
   
  String      path           = (String)patternElement.getElementProperty(DE.P_SOURCE_NAME);
  int         line           = ((Integer)patternElement.getElementProperty(DE.P_SOURCE_LOCATION)).intValue();
  String      sourceLocation = patternElement.getAttribute(DE.A_SOURCE);
  DataElement fileElement    = _dataStore.find(theProject, DE.A_NAME, path, 2);
  String      pattern        = patternElement.getName();
  DataElement startElement   = _nameLookup.getClosestObject(path + ":" + line);
  
 
  ArrayList identifiers = splitPattern(pattern);
 
   //For now, we only handle a.b or a->b  not a.b.c
  if ( (identifiers == null) || (startElement == null) || (identifiers.size() == 0) || (identifiers.size() > 2))
  {
   status.addNestedData(results,true);
   return status;
  }
  
  
  if (identifiers.size() == 1)
  {
   results = _nameLookup.fuzzyValueLookup((String)identifiers.get(0), startElement);
   status.addNestedData(filterResults(results), true);
   return status;
  }
  
  
  String baseName = (String)identifiers.get(0);
  String fuzzyName = (String)identifiers.get(1);
  
  DataElement baseObj = _nameLookup.valueLookup(baseName, ParserSchema.Variable, startElement);
  
  //If the base Object isn't a variable it could be a struct, class, or union also.
  if (baseObj == null)
  {
   baseObj = _nameLookup.valueLookup(baseName, ParserSchema.Struct, startElement);
   if (baseObj == null)
    baseObj = _nameLookup.valueLookup(baseName, ParserSchema.Class, startElement);
   if (baseObj == null)
    baseObj = _nameLookup.valueLookup(baseName, ParserSchema.Union, startElement);
   if (baseObj == null)
   {
    status.addNestedData(results, true);
    return status;
   }
  }
  else //If the base Object is a variable, we need to get its Base Type, and dereference it.
  {
   ArrayList baseTypes = baseObj.getAssociated(ParserSchema.Types);
    
   baseObj = null;
  
   DataElement currentBaseType = null;
   
   for (int i=0; i< baseTypes.size(); i++)
   {
    currentBaseType = (DataElement)baseTypes.get(i);
    if (currentBaseType != null)
    {
     baseObj = currentBaseType.dereference();
     if (baseObj.getType().equals(ParserSchema.Struct) || baseObj.getType().equals(ParserSchema.Class))
      break;
     else
     baseObj = null;
    }
   }
   
   if (baseObj == null)
   {
    status.addNestedData(results, true);
    return status;
   }
  }
  
  //baseObj.expandChildren();
  _parseManager.parseObject(baseObj);
  
  ArrayList tempresults = baseObj.getAssociated("contents");
  for (int i=tempresults.size()-1; i>=0; i--)
  {
   DataElement curObj = (DataElement)tempresults.get(i);
   String theValue = curObj.getValue();
   if (theValue != null)
      if (theValue.startsWith(fuzzyName))
      {
        results.add(curObj);
      }
  }
  
  status.addNestedData(filterResults(results),true);
  return status;
 }
 
 private ArrayList filterResults(ArrayList res)
 {
  ArrayList newResults = new ArrayList();
  DataElement curObj;
  String theType;
  
  for (int i=0; i < res.size(); i++)
  {
   curObj = (DataElement)res.get(i);
   theType = curObj.getType();
   if  (theType.equals(ParserSchema.Class) ||
            theType.equals(ParserSchema.Struct) ||
	    theType.equals(ParserSchema.Union) ||
	    theType.equals(ParserSchema.Namespace) ||
	    theType.equals(ParserSchema.Function) ||
	    theType.equals(ParserSchema.Constructor) ||
	    theType.equals(ParserSchema.Destructor) ||
	    theType.equals(ParserSchema.Macro) ||
	    theType.equals(ParserSchema.Variable) ||
	    theType.equals(ParserSchema.Enum))
	   
    newResults.add(curObj);
  }
  return newResults;
 }
 
 public void doTest(DataElement theProject)
 {
  for (int i = 0; i < 25; i++)
  {
   for (int j = 0; j < 1000; j++)
    _dataStore.createObject(theProject, "Parsed Source", "class ZZZZZZZZZZZZZZ", "hpppppppppppppppppppppppppppp", "" + i);
   _dataStore.update(theProject);
  }
 }
 
 //Return the list of ids (split by . or ->)
 private ArrayList splitPattern(String pattern)
 {
  int          patternLength = pattern.length();
  StringBuffer currentName   = new StringBuffer();
  ArrayList    ids           = new ArrayList();
  char         curChar;
  boolean      inID          = false;
  
  try
  {
   for (int i=0; i < patternLength; i++)
   {
    curChar = pattern.charAt(i);
    if ( (curChar == '.') || ( (curChar == '-') && (pattern.charAt(++i) == '>')))
    {
     if (inID)
     {
      ids.add(currentName.toString());
      currentName.delete(0, currentName.length());
      inID = false;
     }
    }
    else 
    {
     currentName.append(curChar);
     inID = true;
    }
   }
   
    ids.add(currentName.toString());
  }
  catch (IndexOutOfBoundsException e)
  {
   if (inID)
    ids.add(currentName.toString());
  }
  return ids;
 }
}









