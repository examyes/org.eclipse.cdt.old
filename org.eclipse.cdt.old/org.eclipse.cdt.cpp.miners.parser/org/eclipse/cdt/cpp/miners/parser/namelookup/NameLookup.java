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
  
 //This method and the few that follow, take a fully qualified function name such as 
 //abc::def::foo(int bar) and searches for it in the parse information.
 public DataElement provideSourceFor(DataElement theElement, DataElement parsedFiles, DataElement status)
 {
  if ((theElement == null) || (parsedFiles == null) || (status == null))
   return null;
 
  String fullyQualifiedName = theElement.getValue();
  ArrayList names = parseQualifiedName(fullyQualifiedName);
  int lastIndex = names.size()-1;
  if (lastIndex < 0)
   return null;

  String functionName = (String)names.get(lastIndex);
  names.remove(lastIndex);
 
  //First try to find the fully qualified name directly under a parsed file. 
  DataElement foundFunction = findFunction(parsedFiles.getAssociated("contents"), getFunctionName(fullyQualifiedName), countCommas(fullyQualifiedName));
  
  if (foundFunction == null)
  {
   //If we get here, we try to navigate the namespaces first 
   DataElement containingNamespace = navigateNamespaces(parsedFiles, names);
   if (containingNamespace == null)
    return null;
   //If we get here, we have found the right container, so just find the function.
   ArrayList container = new ArrayList();
   container.add(containingNamespace);
   foundFunction = findFunction(container, getFunctionName(functionName), countCommas(functionName));
   if (foundFunction == null)
    return null;
  }
  theElement.setAttribute(DE.A_SOURCE, foundFunction.getSource());
  theElement.getDataStore().update(theElement);
  return status;
 }
 
 //Break a fully qualified name apart based on ::'s
 public ArrayList parseQualifiedName(String fullyQualifiedName)
 {
  ArrayList theNames = new ArrayList();
  StringTokenizer st = new StringTokenizer(fullyQualifiedName, "::");
  while (st.hasMoreTokens())
   theNames.add(st.nextToken());
  return theNames;
 }

 //Simply try to find each of the names...
 public DataElement navigateNamespaces(DataElement parsedFiles, ArrayList namespaces)
 {
  ArrayList files = parsedFiles.getAssociated("contents");
  DataElement currentRoot = null;
  for (int i = 0; i<namespaces.size(); i++)
  {
   currentRoot = null;
   String theName = (String)namespaces.get(i);
   for (int j = files.size()-1; j>=0; j--)
   {
    DataElement aFile = (DataElement)files.get(j);
    currentRoot = findDataElement(aFile, theName);
    if (currentRoot != null)
     break;
   }
   if (currentRoot == null)
    return null;
  }
  return currentRoot;
 }
 
 public DataElement findDataElement(DataElement root, String theName)
 {
  ArrayList contents = root.getAssociated("contents");
  for (int i=contents.size()-1; i>=0; i--)
  {
   DataElement de = (DataElement)contents.get(i);
   if (de.getValue().equals(theName))
    return de;
  }
  return null;
 }

 public DataElement findFunction(ArrayList roots, String functionName, int commas)
 { 
 
  for (int j=roots.size()-1; j>=0; j--)
  {
   ArrayList contents = ((DataElement)roots.get(j)).getAssociated("contents");
   for (int i=contents.size()-1; i>=0; i--)
   {
    DataElement theElement = (DataElement)contents.get(i);
    String theType = theElement.getType();
    if (theType.equals("function")||theType.equals("mainfunction")||theType.equals("constructor")||theType.equals("destructor"))
    if ( getFunctionName(theElement.getValue()).equals(functionName) && (countCommas(theElement.getValue()) == commas))
     return theElement;   
   }
  }
  return null;
 }

 public String getFunctionName(String functionSignature)
 {
  int firstParen = functionSignature.indexOf("(");
  if (firstParen < 0)
   return functionSignature;
  return functionSignature.substring(0, firstParen);
 }
 
 public int countCommas(String functionName)
 {
  int commas = 0;
  char[] letters = functionName.toCharArray();
  for (int i=letters.length-1; i>=0; i--)
   if (letters[i] == ',')
    commas++;
  return commas;
 }

 /*
 public DataElement findFunction(DataElement root, String functionName, int commas)
 {
  ArrayList contents = root.getAssociated("contents");
  for (int i = contents.size()-1; i>=0; i--)
  {
   DataElement theElement = (DataElement)contents.get(i);
   String theType = theElement.getType();
   if (  theType.equals("function") || theType.equals("constructor") || theType.equals("destructor") )
   {
    if ( (getFunctionName(theElement.getValue()).equals(functionName)) && 
         (countCommas(theElement.getValue()) == commas))
     return theElement;
   }
   else if ( theType.equals("Parsed Source") || theType.equals("Included Source") || theType.equals("class") || theType.equals("namespace") || theType.equals("struct") )
   {
    DataElement result = findFunction(theElement, functionName, commas);
    if (result != null)
     return result;
   }  
  }
  return null;
 } 


 
 */


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

