package org.eclipse.cdt.cpp.miners.parser.invocation;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.util.*;
import java.io.*;

import org.eclipse.cdt.cpp.miners.parser.grammar.*;
import org.eclipse.cdt.cpp.miners.parser.dstore.*;
import org.eclipse.cdt.dstore.core.model.*;

public class ParseWorker extends Thread
{
 private ArrayList             _objectQueue;
 private ArrayList             _immediateObjectQueue;
 private ArrayList             _fileQueue;
 private Parser                _theParser;
 private SimpleCharStream      _theCharStream;
 private ParserTokenManager    _theParserTokenManager;
 private DataStoreSymbolTable  _theSymbolTable;
 private boolean               _enabled;
 private DataElement           _projectObjects;
 private DataStore             _dataStore;
 private DataElement           _masterStatus;
 private boolean			   _fileParsedDone = false;
 private boolean			   _statusDone = false;
 private StringBuffer          _emptyBuffer = null; 
 public ParseWorker()
 {
  //setPriority(getPriority()-1);
  _objectQueue           = new ArrayList();
  _fileQueue             = new ArrayList();
  _immediateObjectQueue  = new ArrayList();
  _theParser             = new Parser(this);
  _theSymbolTable        = new DataStoreSymbolTable();
  _theCharStream         = new SimpleCharStream(new StringReader("default"), 1, 1, 16);
  _theParserTokenManager = new ParserTokenManager(_theCharStream);
  _emptyBuffer           = new StringBuffer("");

 }

 public void closeProjects()
 {
  _objectQueue.clear();
  _fileQueue.clear();
 }
  
 public void setMasterStatus(DataElement status)
 {
  _masterStatus = status;
  _fileParsedDone = false;
  _statusDone = false;
 }
 
 public void parseObject(DataElement theObject, DataElement status)
 {
  _objectQueue.add(theObject);
 }

 public void parseObjectNow(DataElement theObject, DataElement status)
 {
  _immediateObjectQueue.add(theObject);
 }

 public void parseFile(DataElement theFile, DataElement status)
 {
  _fileQueue.add(theFile);
 }

 public void setParsedFiles(DataElement parsedFiles)
 {
  _dataStore = parsedFiles.getDataStore();
  _projectObjects = _dataStore.find(parsedFiles.getParent(), DE.A_NAME, ParserSchema.ProjectObjects,1);
   _theSymbolTable.setParsedFiles(parsedFiles);
  DataElement preferences = _dataStore.find(parsedFiles.getParent(), DE.A_NAME, ParserSchema.Preferences, 1);
  if (preferences == null)
   return;
  DataElement parseQuality = _dataStore.find(preferences, DE.A_NAME, ParserSchema.ParseQuality, 1);
  if (parseQuality == null)
   return;
  _theSymbolTable.setParseQuality(parseQuality);
 }

 public void setEnabled(boolean enabled)
 {
  _enabled = enabled;
 }

 public void run()
 {
  //setPriority(getPriority()+3);
  try
  {
   while (true)
   {
    if (_enabled)
    {
     parseFilesInQueue();
     parseObjectsInQueue();
    }
    sleep(1000);
   }
  }
  catch (InterruptedException e) {}
 }

 public DataStoreSymbolTable getSymbolTable()
 {
  return _theSymbolTable;
 }

 //Start of private methods:
 private void parseObjectsInQueue()
 {
  DataElement theObject = null;
  int threshold = 30;
  int index = 1;
  while ((!_objectQueue.isEmpty()) || (!_immediateObjectQueue.isEmpty()))
  { 
   theObject = getObjectFromQueue();
   if (initializeParser(theObject))
    beginObjectParse(theObject);
   //update(theObject[0]);
   //statusDone(theObject[1]);
   if (index++ > threshold)
   {
    Thread.currentThread().yield();
    index = 1;
   }
  }
  
  if (_fileParsedDone && _masterStatus != null)
  {
  	if (!_masterStatus.getAttribute(DE.A_VALUE).equals("really done"))
  	{
   	_masterStatus.setAttribute(DE.A_VALUE, "really done");
   	_masterStatus.getDataStore().refresh(_masterStatus);
  	}
  }
  if (theObject != null)
   update(_projectObjects);
 }

 private void parseFilesInQueue()
 {
  DataElement theFile = null;
  int threshold = 1;
  int index = 1;
  while(!_fileQueue.isEmpty())
  {
  theFile = getFileFromQueue();
  if (initializeParser(theFile))
    beginFileParse(theFile);
   update(theFile);
   _fileParsedDone = true;
   //statusDone(theFile[1]);
   if (index++ > threshold)
   {
    Thread.currentThread().yield();
    index = 1;
   }
  }
  if (theFile != null)
  { 
   update(_projectObjects);
  }
  if (!_statusDone)
  {
   statusDone(_masterStatus);
   _statusDone = true;
  }
 }

 //These are used by every specific parse method below...
 private boolean done;
 private boolean partiallyDone;
 private int     errorCount;

 private void beginFileParse(DataElement theFile)
 {
  String theType = theFile.getType();
  
  done = false;
  partiallyDone = false;
  errorCount = 0;
 
  //Parse Based on the appropriate type mapping.
  if (theType == null) return;
  else if (theType.equals(ParserSchema.ParsedSource))   parseFile(theType);
  else if (theType.equals(ParserSchema.IncludedSource)) parseFile(theType);
 }

 private void beginObjectParse(DataElement theObject)
 {
  String theType = theObject.getType();
  String theName = theObject.getName();
  done = false;
  partiallyDone = false;
  errorCount = 0;
 
  //Parse Based on the appropriate type mapping.
  if (theType == null) return;
  else if (theType.equals(ParserSchema.Struct))         parseClass(theType, theName);
  else if (theType.equals(ParserSchema.Function))       parseFunction(theType); 
  else if (theType.equals(ParserSchema.Class))          parseClass(theType, theName);
  else if (theType.equals(ParserSchema.Constructor))    parseFunction(theType);
  else if (theType.equals(ParserSchema.Enum))           parseEnum(theType);
  else if (theType.equals(ParserSchema.Namespace))      parseClass(theType, theName);
  else if (theType.equals(ParserSchema.Destructor))     parseFunction(theType);
  else if (theType.equals(ParserSchema.Union))          parseClass(theType, theName);
  else if (theType.equals(ParserSchema.MainFunction))   parseFunction(theType);
  else if (theType.equals(ParserSchema.Namespace))      parseNamespace(theType);
 }

 private void parseFile(String theType)
 {
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
    if (++errorCount > 30)
     done = true;
   } 
  }
 } 

 private void parseClass(String theType, String theName)
 {
  while (!done)
  {
   try 
   { 
    if (partiallyDone)
     _theParser.member_declaration_list(theName);
    else
     _theParser.class_body(theName); 
    done = true;
   }
   catch (Throwable e) 
   {
    createExceptionObject(e);
    done = _theParser.jumpToNextDeclaration(theType);
    partiallyDone = true;
    if (++errorCount > 10)
     done = true;
   }
  }
 }

 private void parseFunction(String theType)
 {
  while (!done)
  {
   try 
   { 
    if (partiallyDone)
     _theParser.statement_list();
    else
     _theParser.function_body(); 
    done = true;
   }
   catch (Throwable e) 
   {
    createExceptionObject(e);
    done = _theParser.jumpToNextDeclaration(theType);
    partiallyDone = true;
    if (++errorCount > 10)
     done = true;
   }
  }
 }

 private void parseEnum(String theType)
 {
  while (!done)
  {
   try 
   { 
    if (partiallyDone)
     _theParser.enumerator_list();
    else
     _theParser.enum_body(); 
    done = true;
   }
   catch (Throwable e) 
   {
    createExceptionObject(e);
    done = _theParser.jumpToNextDeclaration(theType);
    partiallyDone = true;
    if (++errorCount > 10)
     done = true;
   }
  }
 }

 private void parseNamespace(String theType)
 {
  while (!done)
  {
   try 
   { 
    if (partiallyDone)
     _theParser.translation_unit();
    else
     _theParser.namespace_body(); 
    done = true;
   }
   catch (Throwable e) 
   {
    createExceptionObject(e);
    done = _theParser.jumpToNextDeclaration(theType);
    partiallyDone = true;
    if (++errorCount > 10)
     done = true;
   }
  }
 }

 private boolean initializeParser(DataElement theObject)
 { 
  String objectContents = theObject.getBuffer().toString();
  theObject.setBuffer(_emptyBuffer);
  if ((objectContents == null) || (objectContents.length() == 0))
   return false;
  
  //Parse the line number out of the string
  int sep         = objectContents.indexOf(':');
  int startLine   = Integer.parseInt(objectContents.substring(0,sep));
  objectContents  = objectContents.substring(sep+1,objectContents.length());

	if (objectContents.length() == 0)
	{
		return false;	
	}
	
  BufferedReader input = new BufferedReader(new StringReader(objectContents), objectContents.length());
  _theCharStream.ReInit(input, startLine, 1, 4096); 
  _theParserTokenManager.ReInit(_theCharStream);
  _theParserTokenManager.setSymbolTable(_theSymbolTable);  
  _theSymbolTable.setRoot(theObject);
  _theParser.setSymbolTable(_theSymbolTable);
  _theParser.ReInit(_theParserTokenManager);
  return true;
 }

 private void update(DataElement theObject)
 {
  if (( _dataStore != null) && (theObject != null))
   _dataStore.update(theObject); 
 }

 private void statusDone(DataElement theStatus)
 {
  if (( _dataStore == null) || (theStatus == null))
   return;
  
  if (!theStatus.getAttribute(DE.A_NAME).equals("done"))
  {
  	theStatus.setAttribute(DE.A_NAME, "done");
   _dataStore.update(theStatus);
  }
 }

 private DataElement getFileFromQueue()
 {
  DataElement theFile = (DataElement)_fileQueue.get(0);
  _fileQueue.remove(0);
  return theFile;
 }
 
 private DataElement getObjectFromQueue()
 {
  if (!_immediateObjectQueue.isEmpty())
  {
   DataElement theObject = (DataElement)_immediateObjectQueue.get(0);
   _immediateObjectQueue.remove(0);
   return theObject;
  }
  DataElement theObject = (DataElement)_objectQueue.get(0);
  _objectQueue.remove(0);
  return theObject;
 }

 public void createExceptionObject(Throwable e)
 {
  if (e == null)
   return;
  String theError = e.getMessage();
  if (theError == null)
   return;
  _theSymbolTable.addObject(ParserSchema.dError, theError, _theParser.getToken(0).beginLine, false);
 }  
}
