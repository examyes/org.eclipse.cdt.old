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
 
 public void parseObject(DataElement theObject)
 {
  _thePreprocessWorker.parseObjectNow(theObject);
 }

 public void parseFile(DataElement theFile, DataElement theProject, DataElement status)
 {
  DataElement theParsedFiles = _dataStore.find(theProject,DE.A_NAME, ParserSchema.ParsedFiles,1);
  _thePreprocessWorker.setParsedFiles(theParsedFiles, status);
  _thePreprocessWorker.preprocessFile(theFile.getSource());
 }

 public void cancelParse()
 {
  _thePreprocessWorker.interrupt();
  _thePreprocessWorker = new PreprocessWorker();
  _thePreprocessWorker.start();
 }
 
 public DataElement removeParseInformation(DataElement theFile)
 {
  _dataStore.deleteObject(_parsedFiles,theFile);
  _dataStore.update(_parsedFiles);
  return null;
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

