package org.eclipse.cdt.cpp.miners.parser.namelookup;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.model.*;
import java.util.*;
import java.io.*;

public class NameLookup
{
 private DataStore    _dataStore;
 private ArrayList    _searchedFiles;
 private DataElement  _parsedFiles;
 
 public NameLookup()
 {
  _dataStore     = null;
  _searchedFiles = new ArrayList();
  _parsedFiles   = null;
 }

 public void setProject(DataElement theProject)
 {
  if (theProject == null)
   return;
  _dataStore   = theProject.getDataStore();
  _parsedFiles = _dataStore.find(theProject, DE.A_NAME, "Parsed Files", 1);
 }
 
  
 public DataElement nameLookup(String objName, String type, DataElement start)
 {
  _searchedFiles.clear();
  ArrayList results = findObject(objName, type, start, 5, false, DE.A_VALUE);
  if (results.size() == 1)
   return (DataElement)results.get(0);
  return null;
 }
 
 public ArrayList  fuzzyNameLookup(String objName, String type, DataElement start)
 {
  _searchedFiles.clear();
  return findObject(objName, type, start, 5, true, DE.A_VALUE);
 } 

 public DataElement nameLookup(String objName, DataElement start)
 {
  return nameLookup(objName, null, start);
 }
 
 public ArrayList fuzzyNameLookup(String objName, DataElement start)
 {
  return fuzzyNameLookup(objName, null, start);
 }

 public DataElement valueLookup(String objName, String type, DataElement start)
 {
  _searchedFiles.clear();
  ArrayList results = findObject(objName, type, start, 5, false, DE.A_VALUE);
  if (results.size() == 1)
   return (DataElement)results.get(0);
  return null;
 }
 
 public ArrayList  fuzzyValueLookup(String objName, String type, DataElement start)
 {
  _searchedFiles.clear();
  return findObject(objName, type, start, 5, true, DE.A_VALUE);
 } 

 public DataElement valueLookup(String objName, DataElement start)
 {
  return valueLookup(objName, null, start);
 }
 
 public ArrayList fuzzyValueLookup(String objName, DataElement start)
 {
  return fuzzyValueLookup(objName, null, start);
 }
 
 public ArrayList findObject(String objName, String type, DataElement startElement, int incdepth, boolean isFuzzy, int att)
 {
  ArrayList  results = new ArrayList();
  if ((incdepth == 0) || (startElement == null)) return results;
  
  //Here we'll assume that rootElement is underneath the parsed source...and 
  //we'll walk up the tree, stepping into any include files recursively. 
  DataElement rootElement      = startElement.getParent();
  ArrayList   children         = rootElement.getNestedData();
  int         curChild         = children.indexOf(startElement);
  DataElement curObject        = null;
  String      curName          = null;
  ArrayList   recursiveResults = null;
  
  int count=0;  
  //Now curChild should be pointing to the startElement
  while ((rootElement != null) && !rootElement.getType().equals("Parsed Files"))
  {
      if (count++ > 10)
	  return results;
   for (; curChild >=0; curChild--)
   {
    curObject = (DataElement)children.get(curChild);
       
    if (!curObject.isReference())    
    {
    	if ( (type == null) || (curObject.getType().equals(type)) )
    	{
    	 String currAtt = curObject.getAttribute(att);
     
     	 if (currAtt != null) 
     	 {
     	 	if (isFuzzy && currAtt.startsWith(objName))
     	 	{
				results.add(curObject);    	 		
     	 	} 
    	    else if (currAtt.equals(objName)) //We have an exact match
    	    { 
    	   		results.add(curObject);
    	   		return results;
    	    }	
    	 }
    	}
    }      
    else if (curObject.getType().equals("includes"))             
    {
     DataElement incObj = curObject.dereference();
     String incName = incObj.getName();
     if (!_searchedFiles.contains(incName))   //Only step into the include file if we haven't been there yet
     {
      _searchedFiles.add(incName);
      int incSize = incObj.getNestedSize();
      if (incSize > 0)
      {
       DataElement newStartObject = incObj.get(incSize - 1);
       if (newStartObject != null)
       {
        recursiveResults=findObject(objName, type, newStartObject, incdepth-1, isFuzzy, att);
        if (recursiveResults.size() > 0) 
        {
         if (isFuzzy)
          results.addAll(recursiveResults);
         else
          return recursiveResults;
        }
       }
      }
     }
    }
   }
   rootElement = rootElement.getParent();

   if (rootElement == null)
   {  
    return results;
   }
 
    
   children    = rootElement.getNestedData();
   if (children == null)
    return results;
   curChild    = children.size()-1;
   if (curChild == -1)
    return results;
   
  }
  return results;
 }
 
 public DataElement getClosestObject(String src)
 {
  int    col  = src.lastIndexOf(":");
  String path = src.substring(0,col);
  String foo  = src.substring(col+1, src.length());
  int    line = Integer.parseInt(foo);

  //Make sure we canonize the path before searching the Parsed files
  try
  { 
   File theActualPath = new File(path);
   if (!theActualPath.exists())
    return null;
   path = theActualPath.getCanonicalPath(); 
  }
  catch (IOException e) {return null;}

  DataElement theFile = _dataStore.find(_parsedFiles, DE.A_SOURCE, path,1);
 

  if (theFile == null)
   return null;
 
  DataElement theElement; 
  while (line >=0)
  {
   theElement = _dataStore.find(theFile, DE.A_SOURCE, (path + ":" + line),3);
   if (theElement!=null)
    return theElement;
   line--;
  }
  return null;
 }

 public ArrayList getClosestObjects(String src)
 {
  int    col  = src.lastIndexOf(":");
  String path = src.substring(0,col);
  DataElement theFile = _dataStore.find(_parsedFiles, DE.A_SOURCE, path,1);
  return findAllElements(theFile, DE.A_SOURCE, src);
 }
 
 private ArrayList findAllElements(DataElement root, int attribute, String pattern)
 {
  if ( (root == null) || (pattern == null) )
   return null;
  ArrayList children = root.getAssociated("contents");
  ArrayList results = new ArrayList();
  for (int i = 0; i < children.size(); i++)
  {
   DataElement theChild = (DataElement)children.get(i);
   if (theChild!=null)
   {
   
    
    if (theChild.getSource().toLowerCase().equals(pattern.toLowerCase()))
     results.add(theChild);
    results.addAll(findAllElements(theChild, attribute, pattern));
   }
  }
  return results;
 }
}

