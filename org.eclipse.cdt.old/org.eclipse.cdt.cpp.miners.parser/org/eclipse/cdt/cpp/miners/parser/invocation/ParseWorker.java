package com.ibm.cpp.miners.parser.invocation;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.util.*;
import java.io.*;

import com.ibm.cpp.miners.parser.grammar.*;
import com.ibm.cpp.miners.parser.dstore.*;
import com.ibm.dstore.core.model.*;

public class ParseWorker extends Thread
{
 private ArrayList             _objectQueue;
 private ArrayList             _fileQueue;
 private Parser                _theParser;
 private SimpleCharStream      _theCharStream;
 private ParserTokenManager    _theParserTokenManager;
 private DataStoreSymbolTable  _theSymbolTable;
 private boolean               _enabled;

 public ParseWorker()
 {
  _objectQueue           = new ArrayList();
  _fileQueue             = new ArrayList();
  _theParser             = new Parser(this);
  _theSymbolTable        = new DataStoreSymbolTable();
  _theCharStream         = new SimpleCharStream(new StringReader("default"), 1, 1, 16);
  _theParserTokenManager = new ParserTokenManager(_theCharStream);
 }
  
 public void parseObject(DataElement theObject)
 {
  _objectQueue.add(theObject);
 }

 public void parseFile(DataElement theFile)
 {
  _fileQueue.add(theFile);
 }

 public void setParsedFiles(DataElement parsedFiles)
 {
  _theSymbolTable.setParsedFiles(parsedFiles);
 }

 public void setEnabled(boolean enabled)
 {
  _enabled = enabled;
 }

 public void run()
 {
  // setPriority(getPriority()+3);
  try
  {
   while (true)
   {
    if (_enabled)
    {
     parseFilesInQueue();
     parseObjectsInQueue();
    }
    sleep(100);
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
  DataElement theObject;
  while (!_objectQueue.isEmpty())
  { 
   theObject = getObjectFromQueue();
   if (initializeParser(theObject))
    beginObjectParse(theObject);
   update(theObject);
  }
 }

 private void parseFilesInQueue()
 {
  DataElement theFile;
  while(!_fileQueue.isEmpty())
  {
   theFile = getFileFromQueue();
   if (initializeParser(theFile))
    beginFileParse(theFile);
   update(theFile);
   yield();
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
  String        theType = theObject.getType();
  
  done = false;
  partiallyDone = false;
  errorCount = 0;
 
  //Parse Based on the appropriate type mapping.
  if (theType == null) return;
  else if (theType.equals(ParserSchema.Struct))         parseClass(theType);
  else if (theType.equals(ParserSchema.Function))       parseFunction(theType); 
  else if (theType.equals(ParserSchema.Class))          parseClass(theType);
  else if (theType.equals(ParserSchema.Constructor))    parseFunction(theType);
  else if (theType.equals(ParserSchema.Enum))           parseEnum(theType);
  else if (theType.equals(ParserSchema.Namespace))      parseClass(theType);
  else if (theType.equals(ParserSchema.Destructor))     parseFunction(theType);
  else if (theType.equals(ParserSchema.Union))          parseClass(theType);
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

 private void parseClass(String theType)
 {
  while (!done)
  {
   try 
   { 
    if (partiallyDone)
     _theParser.member_declaration_list();
    else
     _theParser.class_body(); 
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
  theObject.setBuffer(new StringBuffer(""));
  if ((objectContents == null) || (objectContents.length() == 0))
   return false;
  
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
  return true;
 }

 private void update(DataElement theObject)
 { 
  theObject.getDataStore().update(theObject);
 }

 private DataElement getFileFromQueue()
 {
  DataElement theFile = (DataElement)_fileQueue.get(0);
  _fileQueue.remove(0);
  return theFile;
 }
 
 private DataElement getObjectFromQueue()
 {
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
