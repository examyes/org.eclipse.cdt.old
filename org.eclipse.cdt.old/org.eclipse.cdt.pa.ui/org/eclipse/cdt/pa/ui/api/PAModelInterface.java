package org.eclipse.cdt.pa.ui.api;
 
/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.cpp.ui.internal.CppPlugin;
import org.eclipse.cdt.cpp.ui.internal.api.*;

import org.eclipse.ui.plugin.*;
import org.eclipse.core.resources.*;
import org.eclipse.swt.widgets.*;
import java.util.*;


public class PAModelInterface {

 private static PAModelInterface _instance; 
 private ModelInterface  		 _cppApi;
 private PATraceNotifier 		 _notifier;
 private PAProjectAdaptor        _projectAdaptor;
 private DataStore       		 _dataStore;
 
 private DataElement _paRoot;
 private DataElement _projectsRoot;
 private DataElement _traceFilesRoot;
 private DataElement _dummyElement;
 private boolean     _isShowAll;
 
 // Constructor
 public PAModelInterface(DataStore dataStore) {
  
  _dataStore = dataStore;
  _notifier = new PATraceNotifier(this);
  _notifier.enable(true);
  _instance = this;
  _cppApi = CppPlugin.getDefault().getModelInterface();
  
  _projectAdaptor = PAProjectAdaptor.getInstance();
  CppProjectNotifier cppNotifier = _cppApi.getProjectNotifier();
  cppNotifier.addProjectListener(_projectAdaptor);
  
  _paRoot         = dataStore.findMinerInformation("org.eclipse.cdt.cpp.miners.pa.PAMiner");

  _projectsRoot   = dataStore.createObject(_paRoot, "data", "projects root");
  
  _traceFilesRoot = dataStore.createObject(_paRoot, "data", "trace files root");
  
  _dummyElement   = dataStore.createObject(_projectsRoot, "data", "no input");
  
  _isShowAll      = false;
  
  _instance = this;
 }
 
 /**
  * Return the single static instance
  */
 public static PAModelInterface getInstance() {
  return _instance;
 }
 
 /**
  * Return the trace notifier
  */
 public PATraceNotifier getTraceNotifier() {
  return _notifier;
 }
 
 /**
  * Return the datastore
  */
 public DataStore getDataStore() {
  return _dataStore;
 }
 
 public Shell getShell() {
  return _cppApi.getDummyShell();
 }
 
 /**
  * Return the show all flag
  */
 public boolean isShowAll() {
  return _isShowAll;
 }
 
 /**
  * Set the show all flag
  */
 public void setShowAll(boolean isShowAll) {
  _isShowAll = isShowAll;
 }
 
 /**
  * Return the projects root under the PA miner
  */
 public DataElement getProjectsRoot() {
  return _projectsRoot;
 }
 
 /**
  * Return the trace files root under the PA miner
  */
 public DataElement getTraceFilesRoot() {
  return _traceFilesRoot;
 }
 
 /**
  * Return the dummy project element under the PA miner
  */
 public DataElement getDummyElement() {
  return _dummyElement;
 }
 
 /**
  * Extend the schema
  */
 public void extendSchema() {
 
  // System.out.println("extend schema");
  DataElement schemaRoot    = _dataStore.getDescriptorRoot();
  DataElement fileD         = _dataStore.find(schemaRoot, DE.A_NAME, "file",1);	
  DataElement traceFileD    = _dataStore.find(schemaRoot, DE.A_NAME, "trace file", 1);
  DataElement traceProgramD = _dataStore.find(schemaRoot, DE.A_NAME, "trace program", 1);
  
  _dataStore.createObject(fileD,         DE.T_UI_COMMAND_DESCRIPTOR, "Add trace file", "org.eclipse.cdt.pa.ui.actions.AddTraceFileAction");    
  _dataStore.createObject(fileD,         DE.T_UI_COMMAND_DESCRIPTOR, "Add trace program", "org.eclipse.cdt.pa.ui.actions.AddTraceProgramAction");      
  _dataStore.createObject(traceFileD,    DE.T_UI_COMMAND_DESCRIPTOR, "Remove", "org.eclipse.cdt.pa.ui.actions.RemoveTraceTargetAction");
  _dataStore.createObject(traceProgramD, DE.T_UI_COMMAND_DESCRIPTOR, "Remove", "org.eclipse.cdt.pa.ui.actions.RemoveTraceTargetAction");
  _dataStore.createObject(traceProgramD, DE.T_UI_COMMAND_DESCRIPTOR, "Run", "org.eclipse.cdt.pa.ui.actions.RunTraceProgramAction");
  _dataStore.createObject(traceProgramD, DE.T_UI_COMMAND_DESCRIPTOR, "Analyze", "org.eclipse.cdt.pa.ui.actions.AnalyzeTraceProgramAction");
  _dataStore.createObject(traceProgramD, DE.T_UI_COMMAND_DESCRIPTOR, "Run and Analyze", "org.eclipse.cdt.pa.ui.actions.RunAndAnalyzeTraceProgramAction");
  
 }
 
 /**
  * Run a given command and return the status element
  */
 public DataElement runCommand(DataStore dataStore, DataElement workingDir, String command) {
   
   DataElement cmdD = dataStore.localDescriptorQuery(workingDir.getDescriptor(), "C_COMMAND");
   ArrayList args = new ArrayList();
  			
   DataElement invocationObj = dataStore.createObject(null, "invocation", command, "");
   args.add(invocationObj);
  		 
   DataElement cmdStatus = dataStore.command(cmdD, args, workingDir);
   return cmdStatus;
 
 }
 
 /**
  * Run the command util it is done
  */
 public DataElement runSynchronizedCommand(DataStore dataStore, DataElement workingDir, String command) {
  
   DataElement status = runCommand(dataStore, workingDir, command);
   while (!status.getName().equals("done")) {
     try {
       Thread.sleep(20);
     }
     catch (InterruptedException e) 
     {
       break;
     }
   }
   return status;
 }
  

 /**
  * Run the command represented by the file element
  */
 public DataElement runCommand(DataElement fileElement) {
  return runCommand(fileElement.getDataStore(), fileElement.getParent(), fileElement.getName());
 }
  
 
 /**
  * Run a given command and return the result as an array of Strings
  */
 public ArrayList getCommandResult(DataStore dataStore, DataElement workingDir, String command) {
 
  DataElement status = runCommand(dataStore, workingDir, command);
  while (!status.getAttribute(DE.A_VALUE).equals("done")) {
   try { Thread.sleep(20); } 
   catch (InterruptedException e) { break; }
  }
  
  ArrayList outputList = status.getAssociated("contents");
  ArrayList result = new ArrayList();
  
  for (int i=0; i < outputList.size(); i++) {
   String line = ((DataElement)outputList.get(i)).getName();
   if (!line.startsWith("> ") && line.trim().length() > 0)
    result.add(line);
  }
     
  return result;
 }

 /**
  * Return the attribute value associated with a given data element
  */
 public String getAttribute(DataElement element, String name) {
  
  ArrayList attributes = element.getAssociated("attributes");
  for (int i=0; i < attributes.size(); i++) {
   DataElement anAttr = (DataElement)attributes.get(i);
   if (anAttr.getType().equals(name))
    return anAttr.getName();
  }
  
  return null;
 }

 /**
  * Set the attribute to a given value for a data element
  */
 public DataElement setAttribute(DataElement element, String name, String value) {
  
  DataElement attributeElement = null;
  ArrayList attributes = element.getAssociated("attributes");
  for (int i=0; i < attributes.size(); i++) {
   DataElement anAttr = (DataElement)attributes.get(i);
   if (anAttr.getType().equals(name))
    attributeElement = anAttr;
  }
  
  if (attributeElement != null) {
   attributeElement.setAttribute(DE.A_NAME, value);
  }
  else {
   attributeElement = _dataStore.createObject(null, name, value);
   _dataStore.createReference(element, attributeElement, "attributes");
  }

  return attributeElement;
 }
 
 /**
  * Return the corresponding project element under the PA root
  * for a given file resource.
  */
 public DataElement findTraceProjectElement(DataElement fileElement) {
 
  DataElement projectElement = _cppApi.getProjectFor(fileElement);
  String source = projectElement.getSource();
  return _dataStore.find(getProjectsRoot(), DE.A_NAME, source);
 }
 
 /**
  * Return the corresponding project element under the PA root
  * for a given name.
  */
 public DataElement findTraceProjectElement(String projectName) {
 
  return _dataStore.find(getProjectsRoot(), DE.A_NAME, projectName);
  
 }
 
 /**
  * Find or create a project element under the PA root
  */
 public DataElement findOrCreateTraceProjectElement(DataElement fileElement) {
 
  DataElement projectElement = _cppApi.getProjectFor(fileElement);
    
  if (projectElement != null) {
   String source = projectElement.getSource();
   DataElement result = _dataStore.find(_projectsRoot, DE.A_NAME, source);
  
   if (result == null) {
    result = _dataStore.createObject(_projectsRoot, "trace project", source, source);
   }
  
   return result;
  }
  else
   return null;
   
 }
 
 /**
  * Return the referenced file element for a given trace element
  */
 public DataElement findReferencedFile(DataElement traceElement) {
 
  ArrayList references = traceElement.getAssociated("referenced file");
  
  if (references.size() > 0)
   return (DataElement)references.get(0);
  else
   return null;
   
 }

 /**
  * Return the referenced project for a given trace element
  */
 public DataElement findReferencedProject(DataElement traceElement) {
 
  ArrayList references = traceElement.getAssociated("referenced project");
  
  if (references.size() > 0)
   return (DataElement)references.get(0);
  else
   return null;
   
 }
 
 /**
  * Return the list of trace files under a trace project
  */
 public ArrayList getTraceFiles(DataElement traceProject) {
 
   return traceProject.getAssociated("contents");
 }
 
 /**
  * Return the containing trace file for a trace function
  */
 public DataElement getContainingTraceFile(DataElement traceFunction) {
 
   return traceFunction.getParent().getParent();
 }
 
 /**
  * Return the trace functions root for a trace file data element
  */
 public DataElement getTraceFuctionsRoot(DataElement traceFile) {
 
  return _dataStore.find(traceFile, DE.A_VALUE, "trace functions root", 1);
 }

 /**
  * Return the call trace root for a trace file data element
  */
 public DataElement getCallTraceRoot(DataElement traceFile) {

  return _dataStore.find(traceFile, DE.A_TYPE, "call root", 1);
 }
 
 /**
  * Add a new trace file
  */
 public boolean addTraceFile(DataElement fileElement, String traceFormat) {

   DataElement paProject = findOrCreateTraceProjectElement(fileElement);
   DataElement traceFile = _dataStore.createObject(paProject, "trace file", fileElement.getName(), fileElement.getSource());
   _dataStore.createReference(getTraceFilesRoot(), traceFile);
   _dataStore.createReference(traceFile, fileElement, "referenced file");
   _dataStore.createReference(traceFile, _cppApi.getProjectFor(fileElement), "referenced project");

   setAttribute(traceFile, "trace format", traceFormat);
   
   DataElement parseCommand = _dataStore.localDescriptorQuery(traceFile.getDescriptor(), "C_PARSE_TRACE");
   DataElement status = _dataStore.command(parseCommand, traceFile);
   
   while (!status.getName().equals("done") && !status.getName().equals("error")) {
    try { Thread.sleep(20); } catch (InterruptedException e) { break; }
   }
   
   if (status.getName().equals("done")) {
       
    PATraceEvent traceEvent = new PATraceEvent(PATraceEvent.FILE_CREATED, traceFile);
    _notifier.fireTraceChanged(traceEvent);
    return true;
   }
   else {
    _dataStore.deleteObject(paProject, traceFile);
    System.out.println("trace file parse error");
    return false;
   }
   
 }

 
 /**
  * Remove a trace file
  */
 public void removeTraceFile(DataElement fileElement) {
 
   DataElement projectElement = fileElement.getParent();
   DataElement traceFilesRoot = getTraceFilesRoot();
   
   PATraceEvent traceEvent = new PATraceEvent(PATraceEvent.FILE_DELETED, fileElement);
   _notifier.fireTraceChanged(traceEvent);
   
   _dataStore.deleteObject(projectElement, fileElement);
   
 }
 
 /**
  * Add a trace program
  */
 public void addTraceProgram(DataElement progElement, String traceFormat) {

   DataElement paProject = findOrCreateTraceProjectElement(progElement);
   
   String type = null;
   if (traceFormat.indexOf("gprof") >= 0)
    type = "gprof trace program";
   else if (traceFormat.equals("functioncheck"))
    type = "functioncheck trace program";
   else
    type = "unknown trace program";
    
   DataElement traceProgram = _dataStore.createObject(paProject, type, progElement.getName(), progElement.getSource());
   _dataStore.createReference(getTraceFilesRoot(), traceProgram);
   _dataStore.createReference(traceProgram, progElement, "referenced file");
   _dataStore.createReference(traceProgram, _cppApi.getProjectFor(progElement), "referenced project");

   setAttribute(traceProgram, "trace format", traceFormat);

   PATraceEvent traceEvent = new PATraceEvent(PATraceEvent.FILE_CREATED, traceProgram);
   _notifier.fireTraceChanged(traceEvent);
   
 }
 
 /**
  * Return the working directory for a file element
  */
 public DataElement getWorkingDirectory(DataElement fileElement) {
 
   DataElement parent = fileElement.getParent();
   return parent;
   /*
   if (parent.getType().equals("directory"))
    return parent;
   else if (parent.getType().equals("Project"))
    return _dataStore.createObject(null, "directory", parent.getSource(), parent.getSource());
   else
    return null;
   */
   
 }
 
 /**
  * Run a trace program
  */
 public DataElement runTraceProgram(DataElement traceProgram) {
 
   DataElement exeElement = (DataElement)traceProgram.getAssociated("referenced file").get(0);
   DataElement workingDir = getWorkingDirectory(exeElement);
   DataStore dataStore = exeElement.getDataStore();
   return runCommand(dataStore, workingDir, exeElement.getName());
 }
 
 /**
  * Analyze a trace program after the program is run and the profile output file
  * has been generated.
  */
 public boolean analyzeTraceProgram(DataElement traceProgram) {
 
   DataElement exeElement = (DataElement)traceProgram.getAssociated("referenced file").get(0);
   DataElement workingDir = getWorkingDirectory(exeElement);
   DataStore dataStore = exeElement.getDataStore();
   
   String gprofCommand = "gprof -b " + exeElement.getName();
   String functionCheckCommand = "fcdump -demangle-params " + exeElement.getName();
   
   String traceFormat = getAttribute(traceProgram, "trace format");
   // System.out.println("trace format: " + traceFormat);
   
   String command = null;
   if (traceFormat.indexOf("gprof") >= 0)
    command = gprofCommand;
   else if (traceFormat.equals("functioncheck"))
    command = functionCheckCommand;
    
   DataElement status = runSynchronizedCommand(dataStore, workingDir, command);
   
   /*
   ArrayList results = status.getAssociated("contents");
   for (int i=0; i < results.size(); i++) {
    System.out.println(((DataElement)results.get(i)).getName());
   }
   */
        
   DataElement analyzeCommand = _dataStore.localDescriptorQuery(traceProgram.getDescriptor(), "C_ANALYZE_PROGRAM");
   ArrayList args = new ArrayList();
   args.add(status);
   status = _dataStore.command(analyzeCommand, args, traceProgram);
   
   while (!status.getName().equals("done") && !status.getName().equals("error")) {
    try { Thread.sleep(20); } catch (InterruptedException e) { break; }
   }
   
   if (status.getName().equals("done")) {
       
    PATraceEvent traceEvent = new PATraceEvent(PATraceEvent.FILE_CREATED, traceProgram);
    _notifier.fireTraceChanged(traceEvent);
    return true;
   }
   else {
    System.out.println("trace program parse error");
    return false;
   }

 }
 
 /**
  * Query the trace file format
  */
 public int queryTraceFileFormat(DataElement fileElement) {
 
   DataElement queryTraceCommand = _dataStore.localDescriptorQuery(fileElement.getDescriptor(), "C_QUERY_TRACE_FORMAT");
   DataElement status = _dataStore.command(queryTraceCommand, fileElement);
   
   while (!status.getName().equals("done")) {
    try { Thread.sleep(20); } catch (InterruptedException e) { break; }
   }
   
   String value = status.getValue();
   if (value.equals("gprof_gnu")) {
    return PAResource.GPROF_GNU;
   }
   else if (value.equals("gprof_bsd")) {
    return PAResource.GPROF_BSD;
   }
   else if (value.equals("functioncheck")) {
    return PAResource.FUNCTIONCHECK;
   }
   else {
    return PAResource.INVALID;
   }
   
 }
 
 /**
  * Query the trace program format
  */
 public int queryTraceProgramFormat(DataElement progElement) {
 
   if (isPlatformExecutable(progElement)) {
   
    if (isGprofExecutable(progElement))
     return PAResource.GPROF_ALL;
    else if (isFunctionCheckExecutable(progElement))
     return PAResource.FUNCTIONCHECK;
    else
     return PAResource.INVALID;
     
   }
   else {
    // System.out.println("Not a platform executable");
    return PAResource.NOT_EXECUTABLE;
   }
   
 }
 
 
 /**
  * Check whether the given file is a platform executable.
  */
 public boolean isPlatformExecutable(DataElement fileElement) {
 
   String fileCommand = "file" + " " + fileElement.getSource();
   DataStore dataStore = fileElement.getDataStore();
   DataElement workingDir = getWorkingDirectory(fileElement);
   Object[] results = getCommandResult(dataStore,  workingDir, fileCommand).toArray();
      
   if (results.length > 0 && ((String)results[0]).indexOf("executable") >= 0)
    return true;
   else
    return false;
 }
 
 
 /**
  * Check whether the given file is built with gprof trace hook.
  */
 public boolean isGprofExecutable(DataElement fileElement) {
 
   String nmCommand = "nm" + " " + fileElement.getSource() + "| grep mcount";
   DataStore dataStore = fileElement.getDataStore();
   DataElement workingDir = getWorkingDirectory(fileElement);
   Object[] results = getCommandResult(dataStore,  workingDir, nmCommand).toArray();
     
   if (results.length > 0 && ((String)results[0]).indexOf("mcount") >= 0)
    return true;
   else
    return false;
    
 }
 
 
 /**
  * Check whether the given file is built with functioncheck trace hook.
  */
 public boolean isFunctionCheckExecutable(DataElement fileElement) {
 
   String nmCommand = "nm" + " " + fileElement.getSource() + "|grep cyg_profile_func_enter";
   DataStore dataStore = fileElement.getDataStore();
   DataElement workingDir = getWorkingDirectory(fileElement);
   Object[] results = getCommandResult(dataStore,  workingDir, nmCommand).toArray();
   
   if (results.length > 0 && ((String)results[0]).indexOf("cyg_profile_func_enter") >= 0)
    return true;
   else
    return false;
   
 }
 
 
}
