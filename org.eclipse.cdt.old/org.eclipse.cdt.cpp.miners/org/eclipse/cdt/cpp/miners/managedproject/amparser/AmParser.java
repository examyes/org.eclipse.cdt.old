package com.ibm.cpp.miners.managedproject.amparser;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import java.io.*;
import java.util.*;
import com.ibm.dstore.core.model.*;

public class AmParser
{
 private DataElement     _project;
 private DataStore       _dataStore;
 private BufferedReader  _theFileReader;
 private String          _curFile;
 private int             _curLine;
 private int             _curCol;
 
 public AmParser (DataElement theUnmanagedProject)
 {
  _dataStore = theUnmanagedProject.getDataStore();
  
  DataElement workspace = theUnmanagedProject.getParent();
  DataElement project = findExistingManagedProject(workspace, theUnmanagedProject.getName() );
  if (project != null)
  {
  	//_dataStore.deleteObject(workspace, _project);
  	workspace.removeNestedData(project);
  }
  
  _project   = _dataStore.createObject(workspace, Am.MANAGED_PROJECT, theUnmanagedProject.getName(), theUnmanagedProject.getSource());
  _curFile   = theUnmanagedProject.getSource() + "/" + "Makefile.am";
  try 
  {
   _theFileReader = new BufferedReader(new FileReader(new File(_curFile)));
  }
  catch (Throwable e)
  {
   System.out.println("Problem opening " + _curFile);
  }
 }
 
 public AmParser (DataElement root, String subdir)
 {
  _dataStore = root.getDataStore();
  
  DataElement project = findExistingManagedProject(root, subdir);
  if (project != null)
  {
  	//_dataStore.deleteObject(root, _project);	
	root.removeNestedData(project);
  }
  
  
  _project   = _dataStore.createObject(root, Am.MANAGED_PROJECT, subdir, root.getSource());
  _curFile   = root.getSource() + "/" + subdir + "/" + "Makefile.am";
  _project.setAttribute(DE.A_SOURCE, root.getSource() + "/" + subdir + "/");
  try 
  {
   _theFileReader = new BufferedReader(new FileReader(new File(_curFile)));
  }
  catch (Throwable e)
  {
   System.out.println("Problem opening " + _curFile);
  }
 }
 
 private DataElement findExistingManagedProject(DataElement workspace, String name)
 {
 	for (int i = 0; i < workspace.getNestedSize(); i++)
 	{
 		DataElement child = workspace.get(i);
 		if (child.getType().equals(Am.MANAGED_PROJECT))
 		{
 			if (child.getName().equals(name))
 			{
 				return child;
 			}
 			
 		}	
 	}
 	return null;
 }

 public DataElement parse()
 {  
  String nextLine;
  while ((nextLine = readLine()) != null)
   processLine(nextLine);
  return _project;
 }

 //Just return the next line from the BufferedReader.
 private String readLine()
 {
  if (_theFileReader == null)
   return null;
 
  try
  {
   String theLine = _theFileReader.readLine();
   _curLine++;
   _curCol=1;
   return theLine;
  }
  catch (IOException e)
  {}
  return null;
 }
 
 private void processLine(String theLine)
 {
  int type;
  
  if ( (type = targetDefinition(theLine)) >= 0)
   handleTargetDefinition(theLine, type);
  else if ( (type = attributeDefinition(theLine)) >= 0)
   handleAttributeDefinition(theLine, type);
  else if (subdirsDefinition(theLine))
   handleSubdirsDefinition(theLine);
 }

 //Return the target type if one is found...-1 otherwise
 private int targetDefinition(String theLine)
 {
  for (int i = Am.TARGETTYPE_START; i <= Am.TARGETTYPE_END; i++)
  {
   if (theLine.indexOf("_" + Am.getString(i)) >= 0)
    return i;
  }
  return -1;
 }

 //Return the attribute type if one is found...-1 otherwise
 private int attributeDefinition(String theLine)
 {
  for (int i = Am.ATTRIBUTE_START; i <= Am.ATTRIBUTE_END; i++)
  {
   if (theLine.indexOf("_" + Am.getString(i)) >= 0)
    return i;
  }
  return -1;
 }

 private boolean subdirsDefinition(String theLine)
 {
  return (theLine.indexOf(Am.SUBDIRS) >= 0);
 }
 

 
 //Sample line:
 //   bin_PROGRAMS    =    hello goodbye
 private void handleTargetDefinition(String theLine, int theType)
 {
  int index = theLine.indexOf("_" + Am.getString(theType));
  if (index < 0)
   return;
  int startOfTargetNames = theLine.indexOf("=", index) + 1;
  if ( (startOfTargetNames < 0) || (startOfTargetNames == theLine.length()))
   return;
  String targetNames = theLine.substring(startOfTargetNames,theLine.length()).trim();
  for (int i=0; i<targetNames.length(); i++)
  {
   int nextSpace = targetNames.indexOf(" ", i);
   if (nextSpace < 0)
    nextSpace = targetNames.length();
   addTarget(targetNames.substring(i,nextSpace), theType);
   i = nextSpace;
  }
 }
 
 private DataElement addTarget(String name, int type)
 {
  if ( (type < Am.TARGETTYPE_START) || (type > Am.TARGETTYPE_END))
   type = Am.PROGRAMS; //Default to PROGRAMS if no type was specified
  
  DataElement theTarget = findCanonicalName(_project, name);
  if (theTarget == null)
   theTarget = _dataStore.createObject(_project, Am.PROJECT_TARGET, name, getSourceLocation());
  return theTarget;
 }
 

 
 private void handleAttributeDefinition(String theLine, int theType)
 {
  int index = theLine.indexOf("_" + Am.getString(theType));
  if (index < 0)
   return;
  String targetName = theLine.substring(0,index).trim();
  int startOfAttributes = theLine.indexOf("=", index) + 1;
  if ( (startOfAttributes < 0) || (startOfAttributes == theLine.length()))
   return;
  String attributes = theLine.substring(startOfAttributes,theLine.length()).trim();
  for (int i=0; i<attributes.length(); i++)
  {
   int nextSpace = attributes.indexOf(" ", i);
   if (nextSpace < 0)
    nextSpace = attributes.length();
   addTargetAttribute(targetName, theType, attributes.substring(i,nextSpace));
   i = nextSpace;
  }
 }

 private void handleSubdirsDefinition(String theLine)
 {
  int startOfDirs = theLine.indexOf("=") + 1;
  if ( (startOfDirs < 0) || (startOfDirs >= theLine.length())) 
   return;
  String theDirs = theLine.substring(startOfDirs, theLine.length()).trim();
   
  for (int i =0; i<theDirs.length(); i++)
  {
   int nextSpace = theDirs.indexOf(" ", i);
   if (nextSpace < 0)
    nextSpace = theDirs.length();
   parseSubMakefile(theDirs.substring(i, nextSpace).trim());
   i = nextSpace;
  }
 }
 
 private void parseSubMakefile(String theDir)
 {
  AmParser theParser = new AmParser(_project, theDir);
  theParser.parse();
 }
 
 private DataElement addTargetAttribute(String targetName, int attType, String attValue)
 {
  //Find the target that this attribute applies to
  DataElement theTarget = findCanonicalName(_project,targetName);
  if (theTarget == null)
   theTarget = addTarget(targetName, -1);
  
  //Check to see if the AttributeType already exists under the target
  DataElement theAttributeType = _dataStore.find(theTarget, DE.A_NAME, Am.getString(attType), 1);
  if (theAttributeType == null)
   theAttributeType = _dataStore.createObject(theTarget, Am.TARGET_ATTRIBUTE_TYPE, Am.getString(attType), getSourceLocation());
  
  //If the AttributeType is DEPENDENCIES, this is a special case since we are creating a reference, not an object
  if (Am.getString(attType).equals(Am.getString(Am.DEPENDENCIES)))
  {
   DataElement theRefTarget = findCanonicalName(_project, attValue);
   if (theRefTarget != null)
    return _dataStore.createReference(theAttributeType, theRefTarget);
  }
  return _dataStore.createObject(theAttributeType, Am.getType(attType), attValue, getSourceLocation());
 }

 private String getSourceLocation()
 {
  return _curFile + ":" + _curLine;
 }

 private DataElement findCanonicalName(DataElement root, String name)
 {
  ArrayList children = root.getNestedData();
  for (int i = 0; i < children.size(); i++)
  {
   DataElement child = (DataElement)children.get(i);
   String childName = child.getName();
   if (makeCanonical(childName).equals(name))
    return child;
  }
  return null;
 }

 private String makeCanonical(String theString)
 {
  StringBuffer theCanonicalString = new StringBuffer();
  for (int i = 0; i < theString.length(); i++)
  {
   char c = theString.charAt(i);
   if (Character.isLetterOrDigit(c))
    theCanonicalString.append(c);
   else
    theCanonicalString.append("_");
  }
  return theCanonicalString.toString();
 }
 
}





