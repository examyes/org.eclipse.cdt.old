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
 private Parser               _theParser;
 private Preprocessor         _thePreprocessor;
 private DataStoreSymbolTable _theSymbolTable;
 private SimpleCharStream     _theCharStream;
 private ParserTokenManager   _theParserTokenManager;
 
 private DataStore          _dataStore = null;
 private DataElement        _projectRoot = null;
 private DataElement        _projectObjects;
 private DataElement        _systemObjects;
 private DataElement        _parsedFiles;
 
 public ParseManager()
 {
  _theParser             = new Parser();
  _theSymbolTable        = new DataStoreSymbolTable();
  _thePreprocessor       = new Preprocessor(this);
  _theCharStream         = new SimpleCharStream(new StringReader("default"), 1, 1, 16);
  _theParserTokenManager = new ParserTokenManager(_theCharStream);
 }
 
 public void setProject(DataElement theProject)
 {
  if (theProject == null)
   return;
  
  _projectRoot    = theProject;
  _dataStore      = theProject.getDataStore();
  _projectObjects = _dataStore.find(_projectRoot, DE.A_NAME, ParserSchema.ProjectObjects,1);
  _systemObjects  = _dataStore.find(_projectRoot, DE.A_NAME, ParserSchema.SystemObjects,1);
  _parsedFiles    = _dataStore.find(_projectRoot, DE.A_NAME, ParserSchema.ParsedFiles,1);
  if (_parsedFiles != null)
   _theSymbolTable.setParsedFiles(_parsedFiles);
  UnnamedTypeManager.instance().reset();
  
  DataElement preferences   = _dataStore.find(_projectRoot, DE.A_NAME, ParserSchema.Preferences, 1);
  if (preferences != null)
  {
   DataElement includePath  = _dataStore.find(preferences, DE.A_NAME, ParserSchema.IncludePath ,1);
   DataElement parseQuality = _dataStore.find(preferences, DE.A_NAME, ParserSchema.ParseQuality,1);
   setPreferences(parseQuality,includePath);
  }
  
  
 } 
 
 public void setPreferences(DataElement parseQuality, DataElement includes) 
 {
  if (parseQuality != null) _theSymbolTable.setParseQuality(parseQuality);
  if (includes     != null) _thePreprocessor.setIncludes(includes);
 } 
 
 public boolean isInitialized()
 {
  return (_theSymbolTable.isInitialized() && (_projectRoot != null) && (_dataStore != null));
 }
 
 public DataElement parseObject(DataElement theObject)
 {
  String objectContents = theObject.getBuffer().toString();
  theObject.setBuffer(new StringBuffer(""));
  if ((objectContents == null) || (objectContents.length() == 0))
  {
   _dataStore.refresh(theObject);
   return theObject;
  }
  
 
  
  //Parse the line number out of the string
  int sep         = objectContents.indexOf(':');
  int startLine   = Integer.parseInt(objectContents.substring(0,sep));
  objectContents  = objectContents.substring(sep+1,objectContents.length());

  BufferedReader input = new BufferedReader(new StringReader(objectContents), objectContents.length());
  _theCharStream.ReInit(input, startLine, 1, 4096); 
  _theParserTokenManager.ReInit(_theCharStream);
  _theParserTokenManager.setSymbolTable(_theSymbolTable);  
  _theSymbolTable.setRoot(theObject);
  _theParser.setSymbolTable(_theSymbolTable);
  _theParser.ReInit(_theParserTokenManager);

  String theType = theObject.getType();
  boolean done = false;
  int errorCount = 0;
  
  boolean partiallyDone = false;
  
  //Map theType to the appropriate type:
  if (theType.equals(ParserSchema.Struct) || theType.equals(ParserSchema.Union) || theType.equals(ParserSchema.Namespace))
   theType = ParserSchema.Class;
  else if (theType.equals(ParserSchema.Constructor) || theType.equals(ParserSchema.Destructor))
   theType = ParserSchema.Function;

 
  
  
  while (!done)
  {
   try 
   { 
    if (theType.equals(ParserSchema.Class))
    {
     if (partiallyDone)
      _theParser.member_declaration_list();
     else
      _theParser.class_body(); 
    }
    
    else if (theType.equals(ParserSchema.Function))
    {
     if (partiallyDone)
      _theParser.statement_list();
     else
     _theParser.function_body();
    }
    
    else if (theType.equals(ParserSchema.Enum))
    {
     if (partiallyDone)
      _theParser.enumerator_list();
     else
      _theParser.enum_body();
    }
  
    done = true;
   }
   catch (Throwable e) 
   {
    createExceptionObject(e);
    done = _theParser.jumpToNextDeclaration(theType);
    partiallyDone = true;
    if (++errorCount > 15)
     done = true;
   }
  }
  return theObject;
 }


 private DataElement getElementForFile(String fileName)
 {
  //If the file doesn't exist on disk, just return null
  File theFile = new File (fileName);
  if (!theFile.exists()) 
   return null;
 
  long theTimeStamp = theFile.lastModified();
  
  //Look for the file DataElement, and check to make sure the time stamps are not equal before deleting it.
  DataElement theFileElement = _dataStore.find(_parsedFiles, DE.A_NAME, fileName, 1);
  if (theFileElement != null)
  {
   //If the current Time Stamp is the same as the saved one, return null (which means don't bother parsing)
   ArrayList theTimeStamps = theFileElement.getAssociated(ParserSchema.dTimeStamp);
   DataElement savedTimeStampElement = null;
   if (theTimeStamps.size() == 1)
    savedTimeStampElement = ((DataElement)theTimeStamps.get(0)).dereference();
   if ((savedTimeStampElement != null) && (Long.parseLong(savedTimeStampElement.getName()) == theTimeStamp))
    return null; 
   removeParseInformation(theFileElement); 
  }

  //Create a fresh DataElement, and add the Time Stamp object.
  theFileElement = _dataStore.createObject(_parsedFiles, ParserSchema.dParsedSource, fileName, fileName);
  DataElement theNewTimeStamp = _dataStore.createObject(null, ParserSchema.dTypes, Long.toString(theTimeStamp), fileName);
  _dataStore.createReference(theFileElement, theNewTimeStamp, ParserSchema.dTimeStamp);
  
  theFileElement.setDepth(1);
  _dataStore.refresh(_parsedFiles);
  return theFileElement;
 }
 
 public DataElement parse(String fileName, boolean isIncludeFile)
 {
  DataElement theFileElement = getElementForFile(fileName);
  if (theFileElement == null)
   return null;

  //If we get here, theFileElement should be a "fresh" DataElement containing just the Time Stamp.
  _theSymbolTable.pushRoot();
  _theSymbolTable.setRoot(theFileElement);
   
  if (isIncludeFile)
   _thePreprocessor.pushState();
  else
   _thePreprocessor.reset();
  
 
  //String huge = _thePreprocessor.preprocess(fileName);
 
  // if (huge.length() == 0)
  // return fileElement;
  
  BufferedReader input = new BufferedReader(new StringReader(_thePreprocessor.preprocess(fileName)), 16000);
  _theCharStream.ReInit(input,1, 1, 32768);
  _theParserTokenManager.ReInit(_theCharStream);
  _theParserTokenManager.setSymbolTable(_theSymbolTable);
  
  if (isIncludeFile)
   _thePreprocessor.popState();
  
  _theParser.setSymbolTable(_theSymbolTable);
  _theParser.ReInit(_theParserTokenManager);
  
  boolean done = false;
  int errorCount = 0;
  
  while (!done)
  {
   try
   {
    _theParser.translation_unit();
    done = true;
   }
   catch (Throwable e)  
   { 
    createExceptionObject(e);
    done = _theParser.jumpToClosingCurly();
    if (++errorCount > 100)
     done = true;
   } 
  }
  String projectPath = null;
  
  try
   {
    projectPath = (new File (_projectRoot.getSource())).getCanonicalPath();
   }
  catch (IOException e)
   {
    System.out.println("Project has an invalid path => " + _projectRoot.getSource());
    return theFileElement;
   }
  
  if (theFileElement.getSource().indexOf(projectPath) < 0)
  {
   theFileElement.setAttribute(DE.A_TYPE, ParserSchema.IncludedSource);
   _dataStore.createReferences(_systemObjects, theFileElement.getAssociated("contents"), "contents");
   //_dataStore.update(_systemObjects);
  }
  else
  {
   _dataStore.createReferences(_projectObjects, theFileElement.getAssociated("contents"), "contents");
   //_dataStore.update(_projectObjects);
  }
  _theSymbolTable.popRoot();
   
  //System.out.println((System.currentTimeMillis() - startTime) + "ms " + fileName);    
  return theFileElement;
 }
 
 public DataElement removeParseInformation(DataElement theFile)
 {
  String fileName = theFile.getName();
  DataElement obj;
  
  //Remove Project Objects from the given file 
  for (int i=_projectObjects.getNestedSize()-1; i>=0; i--)
  {
   if ( (obj = _projectObjects.get(i)) != null)
   {
    String source = (String)obj.getElementProperty(DE.P_SOURCE_NAME);
    if (source != null)
     if (source.equals(fileName))
     _dataStore.deleteObject(_projectObjects,obj);
   }
  }
  _dataStore.update(_projectObjects);

  //Remove System Objects from the given file 
  for (int i=_systemObjects.getNestedSize()-1; i>=0; i--)
  {
   if ( (obj = _systemObjects.get(i)) != null)
   {
    String source = (String)obj.getElementProperty(DE.P_SOURCE_NAME);
    if (source != null)
     if (source.equals(fileName))
     _dataStore.deleteObject(_systemObjects,obj);
   }
  }
  _dataStore.update(_systemObjects);

  //Remove the file from Parsed Files
  _dataStore.deleteObject(_parsedFiles,theFile);
  _dataStore.update(_parsedFiles);
  return null;
 }
 
 public DataElement nameLookup(String name, String type, DataElement theRoot)
 {
  return _theSymbolTable.nameLookup().nameLookup(name, type, theRoot);
 }
 
 public DataElement nameLookup(String name, DataElement theRoot)
 {
  return _theSymbolTable.nameLookup().nameLookup(name, theRoot);
 }
 
 public ArrayList fuzzyNameLookup(String name, DataElement theRoot)
 {
  return _theSymbolTable.nameLookup().fuzzyNameLookup(name, theRoot);
 }

 public DataElement valueLookup(String name, String type, DataElement theRoot)
 {
  return _theSymbolTable.nameLookup().valueLookup(name, type, theRoot);
 }
 
 public DataElement valueLookup(String name, DataElement theRoot)
 {
  return _theSymbolTable.nameLookup().valueLookup(name, theRoot);
 }
 
 public ArrayList fuzzyValueLookup(String name, DataElement theRoot)
 {
  return _theSymbolTable.nameLookup().fuzzyValueLookup(name, theRoot);
 }

 public DataElement getClosestObject(String src)
 {
  return _theSymbolTable.nameLookup().getClosestObject(src);
 }
 

 public void createExceptionObject(Throwable e)
 {
  if (e == null)
   return;
  //e.printStackTrace();   
  String theError = e.getMessage();
  if (theError == null)
   return;
 
  //int     endLine = theError.indexOf("\n");
  //String  theText;
  
  //if (endLine < 0)
  // theText = theError;
  //else 
  // theText = theError.substring(0,endLine).trim();
 
  //System.out.println(e.getMessage());
  
  _theSymbolTable.addObject(ParserSchema.dError, theError, _theParser.getToken(0).beginLine, false);
 }
}













