package com.ibm.cpp.miners.parser;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
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
 private CodeAssist   _codeAssist;

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
 }

 public void extendSchema(DataElement schemaRoot)
 {
  new ParserSchema(schemaRoot);
 }

 public DataElement handleCommand (DataElement theElement)
 {
  if (_parseManager == null)
   _parseManager = new ParseManager(theElement.getDataStore());
  if (_codeAssist == null)
  _codeAssist   = new CodeAssist(_parseManager);

  String name         = getCommandName(theElement);
  DataElement status  = getCommandStatus(theElement);
  DataElement subject = getCommandArgument(theElement, 0);
  
  status.setAttribute(DE.A_NAME, "progress");
  _dataStore.update(status);
  
  if (name.equals("C_PARSE"))
  {
   handleFileParse(subject, getCommandArgument(theElement, 1), status);
   return status;
  }
  else if (name.equals("C_QUERY"))
   handleObjectParse(subject, status);
  else if (name.equals("C_CANCEL"))
   handleCancelCommand(subject, getCommandArgument(theElement, 1));
  else if (name.equals("C_REFRESH"))
    handleRefresh(subject, getCommandArgument(theElement, 1), status); 
  else if (name.equals("C_CODE_ASSIST"))
   handleCodeAssist(subject,getCommandArgument(theElement, 1),status);
  else if (name.equals("C_FIND_DECLARATION"))
   handleFindDeclaration(subject, getCommandArgument(theElement, 1), status);
  else if (name.equals("C_REMOVE_PARSE"))
   removeParseInfo(subject);
  else if (name.equals("C_SAVE_PARSE"))
   handleSaveProject(subject); 
  else if (name.equals("C_OPEN") && (subject.getType().equals("Project") || (subject.getType().equals("Closed Project"))))
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
   handleSetPreferences(subject, getCommandArgument(theElement, 1), getCommandArgument(theElement, 2),getCommandArgument(theElement, 3), status);
   status.setAttribute(DE.A_NAME, "done");
   return status;
 }

 private void handleRefresh(DataElement theSubject, DataElement prj, DataElement status)
 { 
  if (!theSubject.getType().equals("Project"))
   return;
  
  DataElement currentPreferences = getProjectElement(getParseProject(theSubject), ParserSchema.Preferences);
  if (currentPreferences == null)
   return;
  
  DataElement autoParse = _dataStore.find(currentPreferences, DE.A_NAME, "autoparse", 1);  
  if ( (autoParse != null) && autoParse.getValue().equals("Yes") )
   handleFileParse(theSubject, prj, status);	
 }

 private DataElement handleOpenProject(DataElement theProject)
 {  
  theProject = theProject.dereference();
  if (theProject.getType().equals("Project") || theProject.getType().equals("Closed Project"))
  {
   ArrayList parseRefs = theProject.getAssociated(ParserSchema.ParseReference);	     
   if (parseRefs.size() == 0)
   {
    _dataStore.update(_minerData);
    DataElement parseProject = _dataStore.createObject(_minerData, ParserSchema.Project, 
								   theProject.getName(), 
								   theProject.getSource(),
								   theProject.getId() + ".parse");
		     
    _dataStore.createObject(parseProject,ParserSchema.ParsedFiles,    
			    ParserSchema.ParsedFiles, "", ParserSchema.ParsedFiles + "ID");
    _dataStore.createObject(parseProject,ParserSchema.ProjectObjects, 
			    ParserSchema.SystemObjects, "", ParserSchema.SystemObjects + "ID");
    _dataStore.createObject(parseProject,ParserSchema.ProjectObjects, 
			    ParserSchema.ProjectObjects, "", ParserSchema.ProjectObjects + "ID");
    _dataStore.createObject(parseProject,ParserSchema.Preferences, 
			    ParserSchema.Preferences, "", ParserSchema.Preferences + "ID");
    _dataStore.createReference(theProject, parseProject, ParserSchema.ParseReference, ParserSchema.ParseReference);
    _dataStore.refresh(theProject);
    _dataStore.refresh(parseProject);
    return parseProject;
   }
   else
   {
    return (DataElement)parseRefs.get(0);
   }
  }
  return null;
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
     for (int i = 0; i < projectsRoot.getNestedSize(); i++)
	 {
	     DataElement project = projectsRoot.get(i);
	     handleCloseProject(project);
	 }
     
     return null;
 }
 
 private DataElement handleCloseProject(DataElement theSubject)
 {
     DataElement theProject = getParseProject(theSubject);
     _parseManager.closeProjects();
     DataElement currentPreferences = getProjectElement(theProject, ParserSchema.Preferences);
     DataElement autoPersist = _dataStore.find(currentPreferences, DE.A_NAME, "autopersist", 1);  
     if ((autoPersist != null) && autoPersist.getValue().equals("Yes") )
	 {
	     saveProject(theProject);
	 }
     
     if (theProject != null)
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
  }
  if (f2.exists() && (projectObjectsElement != null) )
  {
   _dataStore.load(projectObjectsElement, projectObjs);
  }
  if (f3.exists() && (systemObjectsElement != null))
  {
   _dataStore.load(systemObjectsElement, systemObjs);
  }

  _dataStore.refresh(theProject);
  _dataStore.refresh(parsedSourceElement);
  _dataStore.refresh(projectObjectsElement);
  _dataStore.refresh(systemObjectsElement);
 }
  
 private void saveProject(DataElement project)
 {
  DataElement theProject = project.dereference();
  String sourcePath = theProject.getAttribute(DE.A_SOURCE) + "/.metadata/";

  String parsedSource = sourcePath + "parsed_source.xml";
  String systemObjs   = sourcePath + "system_objects.xml";
  String projectObjs  = sourcePath + "project_objects.xml";
    
  _dataStore.saveFile(getProjectElement(project, ParserSchema.ParsedFiles), parsedSource, 10);
  _dataStore.saveFile(getProjectElement(project, ParserSchema.ProjectObjects), projectObjs, 10);
  _dataStore.saveFile(getProjectElement(project, ParserSchema.SystemObjects), systemObjs, 10);
  
 }

 private DataElement handleDeleteProject(DataElement theProject)
 {
  return null;
 }
 
 private DataElement handleSetPreferences(DataElement project, DataElement qualityPref, 
					  DataElement autoParsePref, DataElement autoPersistPref, DataElement status)
 {
  DataElement theProject = getParseProject(project);
  DataElement currentPreferences = getProjectElement(theProject, ParserSchema.Preferences);

  // parse quality
  DataElement parseQuality = _dataStore.find(currentPreferences, DE.A_NAME, ParserSchema.ParseQuality,1);  
  if (parseQuality == null)
   parseQuality = _dataStore.createObject(currentPreferences, ParserSchema.Preferences, ParserSchema.ParseQuality);
  parseQuality.setAttribute(DE.A_VALUE, qualityPref.getName());

  boolean doLoad = false;
  boolean doParse = false;

  // autoPersist
  String autoPersistValue = autoPersistPref.getName();
  DataElement autoPersist = _dataStore.find(currentPreferences, DE.A_NAME, "autopersist", 1);  
  if (autoPersist == null)
  {
   autoPersist = _dataStore.createObject(currentPreferences, ParserSchema.Preferences, "autopersist");
   autoPersist.setAttribute(DE.A_VALUE, autoPersistValue);
   if (autoPersistValue.equals("Yes"))
   {
    doLoad = true;
   } 
  }
  else
  {  
   if (autoPersist.getName().equals("No") && autoPersistValue.equals("Yes"))
   {
    doLoad = true;
   }
   autoPersist.setAttribute(DE.A_VALUE, autoPersistValue);
  }

  // autoParse
  String autoParseValue = autoParsePref.getName();
  DataElement autoParse = _dataStore.find(currentPreferences, DE.A_NAME, "autoparse", 1);  
  if (autoParse == null)
  {
   autoParse = _dataStore.createObject(currentPreferences, ParserSchema.Preferences, "autoparse");	  
   autoParse.setAttribute(DE.A_VALUE, autoParseValue);
   if (autoParseValue.equals("Yes"))
   {
    doParse = true;
   }
  }
  else
  {
   if (autoParse.getName().equals("No") && autoParseValue.equals("Yes"))
   {
    doParse = true;
   }
   autoParse.setAttribute(DE.A_VALUE, autoParseValue);
  }
  if (doLoad)
  {
   loadProject(project);
  }
  else if (doParse)
  {
   handleFileParse(project, project, status);
  }
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


 private void handleObjectParse(DataElement theElement, DataElement status)
 {
  _parseManager.parseObject(theElement, status);
 }

 private void handleFileParse(DataElement theElement, DataElement theProject, DataElement status)
 {
  _parseManager.parseFile(theElement, getParseProject(theProject), status);
 }
 
 private void handleCancelCommand(DataElement hmm, DataElement hmm2)
 {
  _parseManager.closeProjects();
  _parseManager.cancelParse();
 }
 
 private DataElement getProjectElement(DataElement theProject, String name)
 {
  if (theProject == null)
   return null;
  if (theProject.getId().indexOf(".parse") < 0)
   theProject = getParseProject(theProject);
  return _dataStore.find(theProject, DE.A_NAME, name,1);
 }
 
 private DataElement getParseProject(DataElement theProject)
 {
  ArrayList parseRefs = theProject.getAssociated(ParserSchema.ParseReference);
  if (parseRefs.size() == 0)
   return handleOpenProject(theProject);
  return ((DataElement)parseRefs.get(0)).dereference();
 }
}

















