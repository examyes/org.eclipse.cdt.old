package com.ibm.cpp.miners.parser.invocation;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.cpp.miners.parser.grammar.*;
import com.ibm.cpp.miners.parser.preprocessor.*;
import com.ibm.cpp.miners.parser.dstore.*;
import com.ibm.dstore.core.model.*;

import java.util.*;
import java.io.*;

public class ParseManager
{
 private DataStore            _dataStore = null;
 private PreprocessWorker     _thePreprocessWorker;
 private DataElement          _parsedFiles;

 public ParseManager(DataStore ds)
 {
  _dataStore = ds;
  _thePreprocessWorker = new PreprocessWorker();
  _thePreprocessWorker.start();
 }

 public void setProject(DataElement theProject)
 {
  if (theProject == null)
   return;
  _dataStore      = theProject.getDataStore();
  _parsedFiles    = _dataStore.find(theProject, DE.A_NAME, ParserSchema.ParsedFiles,1);
  UnnamedTypeManager.instance().reset();
 }
 
 public void closeProjects()
 {
  _thePreprocessWorker.closeProjects();
 }
 
 public void parseObject(DataElement theObject, DataElement status)
 {
  _thePreprocessWorker.parseObjectNow(theObject, status);
 }

 public void parseFile(DataElement theFile, DataElement theProject, DataElement status)
 {
  DataElement theParsedFiles = _dataStore.find(theProject,DE.A_NAME, ParserSchema.ParsedFiles,1);
  _thePreprocessWorker.setParsedFiles(theParsedFiles);
  _thePreprocessWorker.preprocessFile(theFile.getSource(), status);
 }

 public void cancelParse()
 {
  _thePreprocessWorker.interrupt();
  _thePreprocessWorker = new PreprocessWorker();
  _thePreprocessWorker.start();
 }
 
 public boolean removeParseInformation(DataElement theFile, DataElement theProject)
 {
  if (theProject == null)
   return false;
  DataElement theParsedFiles = _dataStore.find(theProject,DE.A_NAME, ParserSchema.ParsedFiles,1);
  if (theParsedFiles == null)
   return false;

  ArrayList theFiles = theParsedFiles.getAssociated("contents");
  boolean deletedSomething = false;
  
  if ((theFiles == null) || (theFiles.size() == 0))
   return false;
  
  if (theFiles.contains(theFile))
  {
   _dataStore.deleteObject(_parsedFiles, theFile);
   return true;
  }

  if (theFile.getType().equals("directory"))
  {
   File f = new File (theFile.getSource());
   String thePath = null;
   try
   {
    thePath = f.getCanonicalPath();
   }
   catch (IOException e) {return false;}

   for (int i = theFiles.size()-1; i >= 0; i--)
   {  
    DataElement theElement = (DataElement)theFiles.get(i);
    f = new File (theElement.getSource());
   
    String theElementPath = null; 
    try
     {
      theElementPath = f.getCanonicalPath();
     }
    catch (IOException e) {continue;}
    if (theElementPath.startsWith(thePath))
    {
     _dataStore.deleteObject(theParsedFiles, theElement);
     deletedSomething = true;
    }
   }
  }
  return deletedSomething;
 }
 
 public DataElement nameLookup(String name, String type, DataElement theRoot)
 {
  return _thePreprocessWorker.getSymbolTable().nameLookup().nameLookup(name, type, theRoot);
 }
 
 public DataElement nameLookup(String name, DataElement theRoot)
 {
  return _thePreprocessWorker.getSymbolTable().nameLookup().nameLookup(name, theRoot);
 }
 
 public ArrayList fuzzyNameLookup(String name, DataElement theRoot)
 {
  return _thePreprocessWorker.getSymbolTable().nameLookup().fuzzyNameLookup(name, theRoot);
 }

 public DataElement valueLookup(String name, String type, DataElement theRoot)
 {
  return _thePreprocessWorker.getSymbolTable().nameLookup().valueLookup(name, type, theRoot);
 }
 
 public DataElement valueLookup(String name, DataElement theRoot)
 {
  return _thePreprocessWorker.getSymbolTable().nameLookup().valueLookup(name, theRoot);
 }
 
 public ArrayList fuzzyValueLookup(String name, DataElement theRoot)
 {
  return _thePreprocessWorker.getSymbolTable().nameLookup().fuzzyValueLookup(name, theRoot);
 }

 public DataElement getClosestObject(String src)
 {
  return _thePreprocessWorker.getSymbolTable().nameLookup().getClosestObject(src);
 }
}

