package org.eclipse.cdt.cpp.miners.parser.invocation;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.util.*;
import java.io.*;

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.cpp.miners.parser.dstore.*;
import org.eclipse.cdt.cpp.miners.parser.preprocessor.*;

public class PreprocessWorker extends Thread
{
 private ArrayList      _fileQueue;
 private ArrayList      _cppExtensions;
 private Preprocessor   _thePreprocessor;
 private ParseWorker    _theParseWorker;
 private DataStore      _dataStore;
 private DataElement    _parsedFiles;
 private DataElement    _projectObjects;
 private DataElement    _systemObjects;
 private String         _projectCanonicalPath;
 
 private static String  extFileComment = "#This file tells the parser what files to consider as C or C++ Source";
 private static String  default_extensions[] = {".c", ".C", ".cpp", ".CPP", ".h", ".H", ".hpp",
 											   ".HPP", ".cxx", ".cc", ".CC"};

 public PreprocessWorker()
 {
  setPriority(getPriority() - 1);
  _theParseWorker  = new ParseWorker();
  _theParseWorker.setEnabled(false);
  _fileQueue       = new ArrayList();
  _thePreprocessor = new Preprocessor(this);
 }
 
 public void setParsedFiles(DataElement theParsedFiles)
 {
  if (theParsedFiles == null)
   return;
  _dataStore   = theParsedFiles.getDataStore();
  _parsedFiles = theParsedFiles;
  _theParseWorker.setParsedFiles(_parsedFiles);
  _projectObjects = _dataStore.find(_parsedFiles.getParent(), DE.A_NAME, ParserSchema.ProjectObjects, 1);
  _systemObjects  = _dataStore.find(_parsedFiles.getParent(), DE.A_NAME, ParserSchema.SystemObjects, 1);
  try{_projectCanonicalPath = (new File (_parsedFiles.getParent().getSource())).getCanonicalPath();} catch (IOException e){}
  updateParseExtensions(); 
 }
 
 public void preprocessFile(String fileName, DataElement status)
 {
  if (status != null) {
   _theParseWorker.setMasterStatus(status);
  }
  File theFile = new File(fileName);
  if (theFile.exists() && (theFile.isDirectory() || isCorCPPFile(fileName)))
   _fileQueue.add(new File(fileName));
 }

 public void closeProjects()
 {
  _fileQueue.clear();
  _theParseWorker.closeProjects();
 }
 
 public void parseObjectNow(DataElement theObject, DataElement status)
 { 
  _theParseWorker.parseObjectNow(theObject, status);
 }

 public void run()
 {
  _theParseWorker.start(); 
  //setPriority(getPriority()+3);
  try
  {
   while(true)
   {
    preprocessFilesFromQueue();
    sleep(600);
   }
  }
  catch (InterruptedException e) {_theParseWorker.interrupt();}
 }


 //This is a callback from the preprocessor telling us to stop what we're doing and parse this file...
 public void preprocessIncludeFile(File theFile)
 {
  DataElement theFileElement = createFileElement(theFile);
  if (theFileElement != null)
  {
   _thePreprocessor.pushState();
   _thePreprocessor.setFile(theFile);
   beginPreprocess(theFileElement);
   _thePreprocessor.popState();
  }
 }

 public DataStoreSymbolTable getSymbolTable()
 {
  return _theParseWorker.getSymbolTable();
 }

 //Start of Private Methods
 private void preprocessFilesFromQueue()
 {
  File        theFile;
  DataElement theFileElement;
  if (!_fileQueue.isEmpty())
  {
   _theParseWorker.setEnabled(false);
   while ((theFile = getFileFromQueue()) != null)
   {
    theFileElement = createFileElement(theFile);
    if (theFileElement != null)
    {
     initializePreprocessor(theFile);
      beginPreprocess(theFileElement);
    }
   } 
   _theParseWorker.setEnabled(true);
  }
 }

 //We return null here, if the file doesn't need to be parsed.
 private DataElement createFileElement(File theFile)
 {
  try
  {
   if (!theFile.exists()) return null;
   
   String thePath = theFile.getCanonicalPath();
   DataElement theFileElement = _dataStore.find(_parsedFiles, DE.A_VALUE, thePath, 1);

   if (theFileElement != null)
   {
    ArrayList theTimeStamps = theFileElement.getAssociated(ParserSchema.dTimeStamp);
    DataElement savedTimeStampElement = null;
    if (theTimeStamps.size() == 1)
     savedTimeStampElement = ((DataElement)theTimeStamps.get(0)).dereference();
    if ((savedTimeStampElement != null)&&(Long.parseLong(savedTimeStampElement.getName())==theFile.lastModified()))
     return null; 
    _dataStore.deleteObject(_parsedFiles, theFileElement); 
   }

   //If we get here, the file needs to be parsed and has been removed (if it existed) from _parsedFiles.
   String theName = theFile.getName();
   if (thePath.startsWith(_projectCanonicalPath))
   {
    theFileElement = _dataStore.createObject(_parsedFiles, ParserSchema.ParsedSource, theName, thePath);
    _dataStore.createReference(_projectObjects, theFileElement);
   }
   else
   {
    theFileElement = _dataStore.createObject(_parsedFiles, ParserSchema.IncludedSource, theName, thePath);
    _dataStore.createReference(_systemObjects, theFileElement);
   }
   
   theFileElement.setAttribute(DE.A_VALUE, thePath);
   DataElement theNewTimeStamp = _dataStore.createObject(null, ParserSchema.dTypes, Long.toString(theFile.lastModified()));
   _dataStore.createReference(theFileElement, theNewTimeStamp, ParserSchema.dTimeStamp); 
  
   _dataStore.refresh(_parsedFiles);
   _dataStore.refresh(_projectObjects);
   _dataStore.refresh(_systemObjects);
  return theFileElement;
  }
  catch (IOException e) {}
  return null;
 }
 
 private void beginPreprocess(DataElement theFileElement)
 {
  theFileElement.setBuffer(_thePreprocessor.preprocess().insert(0,"1:"));
  _dataStore.update(theFileElement);
  _theParseWorker.parseFile(theFileElement, null);
 }
 
 private void initializePreprocessor(File theFile)
 {
  _thePreprocessor.reset();
  _thePreprocessor.setFile(theFile);
  DataElement prefs = _dataStore.find(_parsedFiles.getParent(), DE.A_NAME, ParserSchema.Preferences, 1);
  _thePreprocessor.setIncludes(_dataStore.find(prefs, DE.A_NAME, ParserSchema.IncludePath, 1));
 }

 private File getFileFromQueue()
 { 
  while (!_fileQueue.isEmpty())
  {  
   File theFile = (File)_fileQueue.get(0);
   _fileQueue.remove(0);
 
   if (theFile.isFile())
    return theFile;
   else if (theFile.isDirectory())
   {
    File[] theFiles = theFile.listFiles();
    for (int i=theFiles.length-1; i>=0; i--)
    {
     try
     {
      preprocessFile(theFiles[i].getCanonicalPath(), null);
     }
     catch (IOException e) {}
    } 
   }
  }
  return null;
 }

 private boolean isCorCPPFile(String objName)
 {
  int dotIndex = objName.lastIndexOf(".");
  if (dotIndex < 0) return false;
  return _cppExtensions.contains(objName.substring(dotIndex, objName.length()).trim());
 }
 
 private void updateParseExtensions()
 {
  _cppExtensions = new ArrayList();
  File parseExtensionFile = getParseExtensionsFile();
  
  BufferedReader br = null;
  try
  {
   br = new BufferedReader(new FileReader(parseExtensionFile));
   String nextLine;
   while ( (nextLine = br.readLine()) != null)
   {
    nextLine = nextLine.trim();
    if (nextLine.length() > 0)
     if (nextLine.charAt(0) == '.') 
      _cppExtensions.add(nextLine);
   }
  }
  catch (IOException e) 
  {
   System.out.println("Problem reading Parse Extensions (CppExtensions.dat)");
  }
  finally
  {
   try {br.close();} catch (IOException e) {}
  }

 }

 private File getParseExtensionsFile()
 {
  String fileName = _dataStore.getAttribute(DataStoreAttributes.A_PLUGIN_PATH) +  _dataStore.getMinersLocation() + "/CPPExtensions.dat";  
  File extFile = new File (fileName);
  if (!extFile.exists())
  {   
   BufferedWriter bw = null;
   try
   {
    extFile.createNewFile();
    bw = new BufferedWriter(new FileWriter(extFile));
    bw.write(extFileComment + "\r\n");
    for (int i=0; i<default_extensions.length; i++)
     bw.write(default_extensions[i] + "\r\n");
   }
   catch (IOException e) 
   {
    System.out.println("Problem writing Parse Extensions (CppExtensions.dat)");
   }
   finally
   {
    try {bw.close();} catch (IOException e) {}
   }
  }
  return extFile;
 }
 
}

