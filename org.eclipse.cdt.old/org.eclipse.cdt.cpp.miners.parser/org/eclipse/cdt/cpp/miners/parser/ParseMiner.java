package com.ibm.cpp.miners.parser;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import java.lang.*;
import java.io.*;
import java.util.*;

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.core.miners.miner.*;
import com.ibm.cpp.miners.parser.dstore.*;
import com.ibm.cpp.miners.parser.invocation.*;
import com.ibm.cpp.miners.parser.codeassist.*;


public class ParseMiner extends Miner
{
 private ParseManager _parseManager;
 private File         _parseExtensionFile;
 private ArrayList    _validCPPFiles;
 private CodeAssist   _codeAssist;
 private String       PARSE_EXTENSIONS = "CPPExtensions.dat"; 
 public ParseMiner ()
 {
  super();
 };

 public void finish()
 {
  super.finish();
 }

 public void load()
 {
  _parseManager    = new ParseManager();
  _parseExtensionFile  = getParseExtensionsFile();
  _codeAssist          = new CodeAssist(_parseManager);
  updateParseExtensions();
 }

 public void extendSchema(DataElement schemaRoot)
 {
  new ParserSchema(schemaRoot);
 }

 public DataElement handleCommand (DataElement theElement)
 {
  String name         = getCommandName(theElement);
  DataElement status  = getCommandStatus(theElement);
  DataElement subject = getCommandArgument(theElement, 0);
   
  
  statusProgress(status);

  if (name.equals("C_PARSE"))
   handleParse(subject, getCommandArgument(theElement, 1));
  else if (name.equals("C_REFRESH"))
    handleRefresh(subject, getCommandArgument(theElement, 1));
  else if (name.equals("C_QUERY"))
   handleObjectParse(subject);
  else if (name.equals("C_CODE_ASSIST"))
   handleCodeAssist(subject,getCommandArgument(theElement, 1),status);
  else if (name.equals("C_FIND_DECLARATION"))
   handleFindDeclaration(subject, getCommandArgument(theElement, 1), status);
  else if (name.equals("C_REMOVE_PARSE"))
   removeParseInfo(subject);
  else if (name.equals("C_SAVE_PARSE"))
   handleSaveProject(subject); 
  else if (name.equals("C_OPEN")/* && subject.getType().equals(ParserSchema.Project)*/)
   handleOpenProject(subject);
  else if (name.equals("C_CLOSE_PROJECTS"))
   handleCloseProjects(subject);
  else if (name.equals("C_CLOSE_PROJECT"))
   handleCloseProject(subject);
  else if (name.equals("C_DELETE_PROJECT"))
   handleDeleteProject(subject);
  else if (name.equals("C_SET_INCLUDE_PATH"))
   handleSetIncludePath(subject, getCommandArgument(theElement, 1));
  else if (name.equals("C_SET_PREFERENCES")) 
   handleSetPreferences(subject, getCommandArgument(theElement, 1));
  return statusDone(status);
 }

  private void handleRefresh(DataElement theSubject, DataElement prj)
    {
	// if we do auto parse
	//***handleParse(theSubject, prj);	
    }

  private DataElement handleOpenProject(DataElement theProject)
 {  
     theProject = theProject.dereference();
     if (theProject.getType().equals("Project"))
	 {
	     _dataStore.update(_minerData);
	     DataElement parseProject = _dataStore.createObject(_minerData, 
								ParserSchema.Project, 
								theProject.getName(), 
								theProject.getSource(),
								theProject.getId() + ".parse");
	     
	     _dataStore.createObject(parseProject,ParserSchema.ParsedFiles,    ParserSchema.ParsedFiles);
	     _dataStore.createObject(parseProject,ParserSchema.ProjectObjects, ParserSchema.SystemObjects);
	     _dataStore.createObject(parseProject,ParserSchema.ProjectObjects, ParserSchema.ProjectObjects);
	     _dataStore.createObject(parseProject,ParserSchema.Preferences, ParserSchema.Preferences);
	     
	     _dataStore.createReference(theProject, parseProject, ParserSchema.ParseReference, ParserSchema.ParseReference);
	     
	     _dataStore.update(theProject);
	     _dataStore.update(parseProject);
	     
	     loadProject(theProject);
	     return parseProject;
	 }
     else
	 {
	     return null;
	 }
 }


 private DataElement handleCodeAssist(DataElement theProject, DataElement thePattern, DataElement status)
 {
  theProject = getParseProject(theProject);
  _codeAssist.setProject(theProject);
  return _codeAssist.doCodeAssist(theProject, thePattern, status);
 }
 
 private DataElement handleFindDeclaration(DataElement theProject, DataElement thePattern, DataElement status)
 {
  theProject = getParseProject(theProject);
  _codeAssist.setProject(theProject);
  return _codeAssist.doFindDeclaration(theProject, thePattern, status);
 }
 

 private DataElement handleCloseProjects(DataElement projectsRoot)
 { 
    //DataElement theFiles = getParsedFiles();
  //
  //while (theFiles.getNestedSize() > 0)
  // handleRemoveParseInfo((DataElement)theFiles.get(0);
  //
  // saveProject(projectsRoot.get(i));
  //_dataStore.deleteObjects(projectsRoot);  
     return null;
 }
 
 private DataElement handleCloseProject(DataElement theProject)
 {
   //  saveProject(theProject);
  _dataStore.deleteObject(theProject.getParent(), theProject);
  return null;
 }

  private DataElement handleSaveProject(DataElement theProject)
 {
  saveProject(theProject);
  return null;
 }

 private void loadProject(DataElement theProject)
 {
  
  String sourcePath = theProject.getAttribute(DE.A_SOURCE) + "/.metadata/";
  String parsedSource = sourcePath + "parsed_source.xml";
  String systemObjs   = sourcePath + "system_objects.xml";
  String projectObjs  = sourcePath + "project_objects.xml";
	
  File f1 = new File(parsedSource);
  File f2 = new File(systemObjs);
  File f3 = new File(projectObjs);
	
  theProject = getParseProject(theProject);
  DataElement parsedSourceElement   = getProjectElement(theProject, ParserSchema.ParsedFiles);
  DataElement projectObjectsElement = getProjectElement(theProject, ParserSchema.ProjectObjects);
  DataElement systemObjectsElement  = getProjectElement(theProject, ParserSchema.SystemObjects);
  
  if (f1.exists() && (parsedSourceElement != null))
  {
   _dataStore.load(parsedSourceElement, parsedSource);		
   _dataStore.update(parsedSourceElement);
  }
  if (f2.exists() && (projectObjectsElement != null) )
  {
   _dataStore.load(projectObjectsElement, projectObjs);
   _dataStore.update(projectObjectsElement);
  }
  if (f3.exists() && (systemObjectsElement != null))
  {
   _dataStore.load(systemObjectsElement, systemObjs);
   _dataStore.update(systemObjectsElement);
  }
 }
  
 private void saveProject(DataElement project)
 {
  DataElement theProject = project.dereference();
  String sourcePath = theProject.getAttribute(DE.A_SOURCE) + "/.metadata/";

  String parsedSource = sourcePath + "parsed_source.xml";
  String systemObjs   = sourcePath + "system_objects.xml";
  String projectObjs  = sourcePath + "project_objects.xml";
  
 
  project = getParseProject(project);
  
  _dataStore.saveFile(getProjectElement(project, ParserSchema.ParsedFiles), parsedSource, 10);
  _dataStore.saveFile(getProjectElement(project, ParserSchema.ProjectObjects), projectObjs, 10);
  _dataStore.saveFile(getProjectElement(project, ParserSchema.SystemObjects), systemObjs, 10);
  
 }

 private DataElement handleDeleteProject(DataElement theProject)
 {
  return null;
 }
 
 private DataElement handleSetPreferences(DataElement theProject, DataElement preferences)
 {
  theProject = getParseProject(theProject);
  
  DataElement currentPreferences = getProjectElement(theProject, ParserSchema.Preferences);
  DataElement parseQuality = _dataStore.find(currentPreferences, DE.A_NAME, ParserSchema.ParseQuality,1);
  
  if (parseQuality == null)
   parseQuality = _dataStore.createObject(currentPreferences, ParserSchema.Preferences, ParserSchema.ParseQuality);
  
  
  if (parseQuality.getNestedSize() == 0)
  { 
   parseQuality.addNestedData(preferences,true);
   preferences.setParent(parseQuality);
  }
  else
   ((DataElement)parseQuality.get(0)).setAttribute(DE.A_NAME, preferences.getName());
  _dataStore.refresh(parseQuality);
  return null;
 }
  
 private DataElement handleSetIncludePath(DataElement theProject, DataElement includePath)
 {
  theProject = getParseProject(theProject);
  DataElement currentPreferences = getProjectElement(theProject, ParserSchema.Preferences);
  DataElement currentIncludePath = _dataStore.find(currentPreferences, DE.A_NAME, ParserSchema.IncludePath,1);
  
  if (currentIncludePath == null)
   currentIncludePath = _dataStore.createObject(currentPreferences, ParserSchema.Preferences, ParserSchema.IncludePath);
  
  currentIncludePath.removeNestedData();
  currentIncludePath.addNestedData(includePath.getNestedData(), true);	
  includePath.setParent(currentIncludePath);
  _dataStore.refresh(currentIncludePath);
   return null;
 }
 
 private DataElement removeParseInfo(DataElement theFile)
 {  
     if (theFile.getType().equals("Project"/*ParserSchema.Project*/))
  {
     
   String sourcePath = theFile.getAttribute(DE.A_SOURCE) + "/" + ".metadata";
   File metadata     = new File (sourcePath);
   sourcePath += "/";
   File parsedSource = new File (sourcePath + "parsed_source.xml");
   File systemObjs   = new File (sourcePath + "system_objects.xml");
   File projectObjs  = new File (sourcePath + "project_objects.xml");
   
   //   DataElement theParent = theFile.getParent();
   DataElement theProject = getParseProject(theFile);
   _dataStore.deleteObjects(getProjectElement(theProject, ParserSchema.ProjectObjects));
   _dataStore.deleteObjects(getProjectElement(theProject, ParserSchema.SystemObjects));
   _dataStore.deleteObjects(getProjectElement(theProject, ParserSchema.ParsedFiles));
   _dataStore.refresh(theProject);

   parsedSource.delete();
   systemObjs.delete();
   projectObjs.delete();
   metadata.delete();
  }
  else
   return _parseManager.removeParseInformation(theFile);
  return theFile;
 }
 
 
 
 private DataElement handleObjectParse(DataElement theElement)
 {
  try 
  {
   _parseManager.parseObject(theElement);
  }
  catch (Throwable e) 
  {
  }
 
  return null;
 }

 private DataElement parseFile(File theFile)
 {
  try
  {
   if (!theFile.exists())
    return null;
 
   if (theFile.isFile())
   {
    if (isCorCPPFile(theFile.getName()) && _parseManager.isInitialized())
     _parseManager.parse(theFile.getCanonicalPath(),false);
   }
   else 
   {
    File[] files = theFile.listFiles();
    for (int i= 0; i < files.length; i++)
     parseFile(files[i]);
   }
  }
  catch (IOException e)
  {
   System.out.println("Parser -> " + e.getMessage());
  }
  return null;
 }
 
 private DataElement handleParse(DataElement theElement, DataElement theProject)
{
  //If theProject is of type status, then this command came from the C++ projects view, as opposed to ModelInterface.
  //Kind of Hacky, but just set theProject to be theElement for now..
  if (theProject.getType().equals("status"))
   theProject = theElement;
 
  try
  { 
   if (theProject == null)
    {
    theProject = _dataStore.createObject(_minerData.get(0), ParserSchema.Project, "fake project");
    _dataStore.createObject(theProject, ParserSchema.dParsedFiles,    ParserSchema.ParsedFiles);
    _dataStore.createObject(theProject, ParserSchema.dProjectObjects,  ParserSchema.SystemObjects);
    _dataStore.createObject(theProject, ParserSchema.dProjectObjects, ParserSchema.ProjectObjects);
   }
   else if (!theProject.getName().equals("fake project")) 
    theProject = getParseProject(theProject);
   if (theProject == null)
    return null;
      
   _parseManager.setProject(theProject);  
   parseFile(new File(theElement.getSource()));
   _dataStore.update(getProjectElement(theProject, ParserSchema.ParsedFiles));
   _dataStore.update(getProjectElement(theProject, ParserSchema.ProjectObjects));
   _dataStore.update(getProjectElement(theProject, ParserSchema.SystemObjects));
  
 }
  
  
  catch (Throwable e) 
  {
   System.out.println("Unrecoverable Parse Error...Parsing of the current source will be halted:");
   e.printStackTrace();
  }
  
  
  //doTest(getParsedFiles(theProject));
  return null;
 }

 private File getParseExtensionsFile()
 {
 
  _validCPPFiles = new ArrayList();
  
  String fileLocation = _dataStore.getAttribute(DataStoreAttributes.A_PLUGIN_PATH);
  if (fileLocation == null)
  {
   System.out.println("Problem reading " + PARSE_EXTENSIONS + "..Set A_PLUGIN_PATH to eclipse\\plugins");
   return null;
  }
   
  String fullFileName = fileLocation + "/com.ibm.cpp.miners.parser/" + PARSE_EXTENSIONS;
  
  File extFile = new File (fullFileName);
  if (!extFile.exists())
  {
   System.out.println("Can't find " + fullFileName);
   return null;
  }
  return extFile;
 }
 
 private void updateParseExtensions()
 {
  _validCPPFiles.clear();
  if (_parseExtensionFile == null)
   return;
  try
  {
   BufferedReader br = new BufferedReader(new FileReader(_parseExtensionFile));
   String nextLine;
   while ( (nextLine = br.readLine()) != null)
   {
    nextLine = nextLine.trim();
    if (nextLine.length() > 0)
     if (nextLine.charAt(0) == '.') 
      _validCPPFiles.add(nextLine);
   }
  }
  catch (IOException e) 
  {
   System.out.println("Problem reading " + PARSE_EXTENSIONS);
  }
 }

 private boolean isCorCPPFile(String objName)
 {
  int dotIndex = objName.lastIndexOf(".");
  if (dotIndex < 0) return false;
  String extension = objName.substring(dotIndex, objName.length()).trim();
  return _validCPPFiles.contains(extension);
 }

 private DataElement getProjectElement(DataElement theProject, String name)
 {
  //DataElement theParseProject = getParseProject(theProject);
  if (theProject == null)
   return null;
  return _dataStore.find(theProject, DE.A_NAME, name,1);
 }
 
 private DataElement getParseProject(DataElement theProject)
 {
  ArrayList parseRefs = theProject.getAssociated(ParserSchema.ParseReference);
  
  if (parseRefs.size() == 0)
   return handleOpenProject(theProject);
  return ((DataElement)parseRefs.get(0)).dereference();
 }
 
 private DataElement statusProgress(DataElement theStatus)
 {
  theStatus.setAttribute(DE.A_NAME, "progress");
  _dataStore.update(theStatus);
  return theStatus;
 }

 private DataElement statusDone(DataElement theStatus)
 { 
  theStatus.setAttribute(DE.A_NAME, "done");
  return theStatus;
 }
}

















