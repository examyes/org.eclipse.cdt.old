package org.eclipse.cdt.pa.ui.api;
 
/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.extra.internal.extra.*;
import org.eclipse.cdt.cpp.ui.internal.CppPlugin;
import org.eclipse.cdt.cpp.ui.internal.api.*;

import org.eclipse.ui.plugin.*;
import org.eclipse.core.resources.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.swt.widgets.*;
import java.util.*;


public class PAModelInterface implements IDomainListener
{
  
 public class showMessageAction implements Runnable
 {
   private String _title;
   private String _msg;
   
   public showMessageAction(String title, String msg)
   {
     _title = title;
     _msg   = msg;
   }
   
   public void run()
   {
     Shell shell = getShell();
     MessageDialog dialog = new MessageDialog(shell,null,null,null,3,null,0);
	 dialog.openWarning(shell, _title, _msg);   
   }
   
 }
 
 
 private static PAModelInterface _instance; 
 private ModelInterface  		 _cppApi;
 private PATraceNotifier 		 _notifier;
 private CppProjectNotifier		 _cppNotifier;
 private PAProjectAdaptor        _projectAdaptor;
 private DataStore       		 _dataStore; 
 private HashMap 				 _statuses;
 
 private HashMap			 	 _projectsRootMap;
 private HashMap				 _traceFilesRootMap;
 
 private DataElement _selection;
 private DataElement _localProjectsRoot;
 private DataElement _localTraceFilesRoot;
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
  _cppNotifier = _cppApi.getProjectNotifier();
  _cppNotifier.addProjectListener(_projectAdaptor);
  
  dataStore.getDomainNotifier().addDomainListener(this);
  
  _selection 	     = null;
  _statuses		     = new HashMap();
  _projectsRootMap   = new HashMap();
  _traceFilesRootMap = new HashMap();
  
  _localProjectsRoot   = null;
  _localTraceFilesRoot = null;
  _dummyElement        = null;
    
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
  * Return the current selection in the workbench
  */
 public DataElement getSelection() {
   return _selection;
 }
 
 /**
  * Set the current selection object
  */
 public void setSelection(DataElement selection) {
   _selection = selection;
 }
 
 
 /**
  * Return the projects root for a given datastore
  */
 public DataElement getProjectsRoot(DataStore dataStore) {
  
   DataElement projectsRoot = (DataElement)_projectsRootMap.get(dataStore);
   if (projectsRoot == null) {
    DataElement paRoot = dataStore.findMinerInformation("org.eclipse.cdt.cpp.miners.pa.PAMiner");
    projectsRoot = dataStore.find(paRoot, DE.A_NAME, "projects root", 1);
    
    if (projectsRoot != null)
     _projectsRootMap.put(dataStore, projectsRoot);
    else
     System.out.println("Error -- projects root is null for datastore:" + dataStore);
   }
   
   return projectsRoot;
 }
 
 /**
  * Return the trace files root for a given datastore
  */
 public DataElement getTraceFilesRoot(DataStore dataStore) {
   
   DataElement traceFilesRoot = (DataElement)_traceFilesRootMap.get(dataStore);
   if (traceFilesRoot == null) {
    DataElement paRoot = dataStore.findMinerInformation("org.eclipse.cdt.cpp.miners.pa.PAMiner");
    traceFilesRoot = dataStore.find(paRoot, DE.A_NAME, "All Trace Files", 1);
    
    if (traceFilesRoot != null)
     _traceFilesRootMap.put(dataStore, traceFilesRoot);
    else
     System.out.println("Error -- trace files root is null for datastore:" + dataStore);
   }
   
   return traceFilesRoot; 
 }
 
 
 /**
  * Return the projects root in the local dataStore
  */
 public DataElement getLocalProjectsRoot() {
 
   if (_localProjectsRoot == null) {
    _localProjectsRoot = getProjectsRoot(_dataStore);
   }
   
   return _localProjectsRoot;
 }
 
 
 /**
  * Return the trace files root in the local dataStore
  */
 public DataElement getLocalTraceFilesRoot() {
 
   if (_localTraceFilesRoot == null) {
    _localTraceFilesRoot = getTraceFilesRoot(_dataStore);
   }
   
   return _localTraceFilesRoot;
 }
 
 
 /**
  * Return the dummy project element under the local projects root
  */
 public DataElement getDummyElement() {
   
   if (_dummyElement == null) {
    _dummyElement = _dataStore.createObject(getLocalProjectsRoot(), "data", "no input");
   }
   
   return _dummyElement;
 }
 
 /**
  * Extend the schema
  */
 public void extendSchema(DataElement schemaRoot) {
 
  // System.out.println("extend schema");
  DataStore   dataStore 	= schemaRoot.getDataStore();
  DataElement fileD         = dataStore.find(schemaRoot, DE.A_NAME, "file",1);
  DataElement executableD	= dataStore.find(schemaRoot, DE.A_NAME, "executable",1);
  DataElement traceFileD    = dataStore.find(schemaRoot, DE.A_NAME, "trace file", 1);
  DataElement traceProgramD = dataStore.find(schemaRoot, DE.A_NAME, "trace program", 1);
    
  dataStore.createObject(fileD,         DE.T_UI_COMMAND_DESCRIPTOR, "Add trace file", "org.eclipse.cdt.pa.ui.actions.AddTraceFileAction");    
  dataStore.createObject(executableD,   DE.T_UI_COMMAND_DESCRIPTOR, "Add trace program", "org.eclipse.cdt.pa.ui.actions.AddTraceProgramAction");      
  dataStore.createObject(traceFileD,    DE.T_UI_COMMAND_DESCRIPTOR, "Remove", "org.eclipse.cdt.pa.ui.actions.RemoveTraceTargetAction");
  dataStore.createObject(traceProgramD, DE.T_UI_COMMAND_DESCRIPTOR, "Remove", "org.eclipse.cdt.pa.ui.actions.RemoveTraceTargetAction");
  dataStore.createObject(traceProgramD, DE.T_UI_COMMAND_DESCRIPTOR, "Run", "org.eclipse.cdt.pa.ui.actions.RunTraceProgramAction");
  dataStore.createObject(traceProgramD, DE.T_UI_COMMAND_DESCRIPTOR, "Analyze", "org.eclipse.cdt.pa.ui.actions.AnalyzeTraceProgramAction");
  dataStore.createObject(traceProgramD, DE.T_UI_COMMAND_DESCRIPTOR, "Run and Analyze", "org.eclipse.cdt.pa.ui.actions.RunAndAnalyzeTraceProgramAction");
  
  dataStore.getDomainNotifier().addDomainListener(this);
  
 }

 // From IDomainListener
 public boolean listeningTo(DomainEvent ev) {
 
   DataElement parent = (DataElement)ev.getParent();

   if (_statuses.containsKey(parent) && 
       (parent.getName().equals("done") || parent.getName().equals("error")))
    return true;
   else 
    return false;
 }
 
 // From IDomainListener
 public void domainChanged(DomainEvent ev) {

   DataElement object = (DataElement)ev.getParent();
   if ((object != null) && (object.getType().equals("status")))
   {
     DataElement traceElement = (DataElement)_statuses.get(object);
     _statuses.remove(object);

     if (object.getName().equals("done"))
     {
       
       DataElement cmdD = object.getParent();
       String commandValue = cmdD.getName();
             
       if (commandValue.equals("C_QUERY_TRACE_FILE_FORMAT") 
          || commandValue.equals("C_QUERY_TRACE_PROGRAM_FORMAT"))
       {
          // System.out.println("format changed");
	  // System.out.println("format: " + object);
	  
          PATraceEvent traceEvent = new PATraceEvent(PATraceEvent.FORMAT_CHANGED, object, traceElement);
         _notifier.fireTraceChanged(traceEvent);
       }
       else if (commandValue.equals("C_PARSE_TRACE") || commandValue.equals("C_ANALYZE_PROGRAM"))
       {  
         PATraceEvent traceEvent = new PATraceEvent(PATraceEvent.FILE_CREATED, traceElement);
         _notifier.fireTraceChanged(traceEvent);
       }
     }
     else if (object.getName().equals("error")) {
      
      System.out.println("Error parsing trace file or program");
      
      String name = new String(traceElement.getSource());
      if (traceElement.isOfType("trace file")) {
       traceElement.getDataStore().deleteObject(traceElement.getParent(), traceElement);
      }
      
      Display d = getShell().getDisplay();
	  d.asyncExec(new showMessageAction("Invalid Trace File", "Not a valid trace file:\n" + name));
	  
     }
	 
   }
 }

 /**
  * Monitor the command status
  */
 public void monitorStatus(DataElement status, DataElement traceElement)
 {
   if (status != null && !_statuses.containsKey(status))
   {
     _statuses.put(status, traceElement);		
   }
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
  
  DataStore dataStore = element.getDataStore();
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
   attributeElement = dataStore.createObject(null, name, value);
   dataStore.createReference(element, attributeElement, "attributes");
   if (dataStore != _dataStore)
    dataStore.setObject(attributeElement);
    dataStore.setObject(element);
  }

  return attributeElement;
 }
 
 /**
  * Return the corresponding project element under the PA root
  * for a given file resource.
  */
 public DataElement findTraceProjectElement(DataElement fileElement) {
 
  DataStore dataStore = fileElement.getDataStore();
  DataElement projectElement = _cppApi.getProjectFor(fileElement);
  
  if (projectElement != null) {
   String source = projectElement.getSource();
   return dataStore.find(getProjectsRoot(dataStore), DE.A_SOURCE, source);
  }
  else
   return null;
   
 }
 
 /**
  * Return the corresponding project element under the PA root
  * for a given IProject.
  */
 public DataElement findTraceProjectElement(IProject project) {
 
  DataElement projectElement = _cppApi.findProjectElement(project);
  
  if (projectElement != null) {
   DataStore dataStore = projectElement.getDataStore();
   return dataStore.find(getProjectsRoot(dataStore), DE.A_SOURCE, projectElement.getSource());
  }
  else
   return null;
  
 }
 
 
 /**
  * Find or create a project element under the PA root.
  * The trace project element is always created in the local dataStore.
  */
 public DataElement findOrCreateTraceProjectElement(DataElement fileElement) {
 
  DataStore dataStore = fileElement.getDataStore();
  DataElement projectElement = _cppApi.getProjectFor(fileElement);
  
  if (projectElement != null) {
   String name = projectElement.getName();
   String source = projectElement.getSource();
   DataElement result = dataStore.find(getProjectsRoot(dataStore), DE.A_SOURCE, source);
  
   if (result == null) {
    result = dataStore.createObject(getProjectsRoot(dataStore), "trace project", name, source);
    
    if (dataStore != _dataStore) {
     dataStore.setObject(result);
    }
    
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
 
  DataStore dataStore = traceFile.getDataStore();
  return dataStore.find(traceFile, DE.A_VALUE, "trace functions root", 1);
 }

 /**
  * Return the call tree root for a trace file data element
  */
 public DataElement getCallTreeRoot(DataElement traceFile) {

  DataStore dataStore = traceFile.getDataStore();
  return dataStore.find(traceFile, DE.A_TYPE, "call root", 1);
 }
 
 /**
  * Add a new trace file
  */
 public void addTraceFile(DataElement fileElement, String traceFormat) {

   DataStore dataStore = fileElement.getDataStore();
   DataElement traceProject = findOrCreateTraceProjectElement(fileElement);
   
   // Create the trace file element
   DataElement traceFile = dataStore.createObject(traceProject, "trace file", fileElement.getName(), fileElement.getSource());
   
   // Create a reference to the trace file from the local trace files root
   getDataStore().createReference(getLocalTraceFilesRoot(), traceFile);
   
   // Create references to the original file and project
   dataStore.createReference(traceFile, fileElement, "referenced file");
   dataStore.createReference(traceFile, _cppApi.getProjectFor(fileElement), "referenced project");

   setAttribute(traceFile, "trace format", traceFormat);
   
   if (dataStore != getDataStore()) {
    dataStore.setObject(traceFile);
   }
      
   DataElement parseCommand = dataStore.localDescriptorQuery(traceFile.getDescriptor(), "C_PARSE_TRACE");
   DataElement status = dataStore.command(parseCommand, traceFile);
   
   monitorStatus(status, traceFile);
   
 }

 
 /**
  * Remove a trace file
  */
 public void removeTraceFile(DataElement fileElement) {
   
   DataStore dataStore = fileElement.getDataStore();
   
   PATraceEvent traceEvent = new PATraceEvent(PATraceEvent.FILE_DELETED, fileElement);
   _notifier.fireTraceChanged(traceEvent);
   
   dataStore.deleteObject(fileElement.getParent(), fileElement);
   
 }
 
 /**
  * Add a trace program
  */
 public void addTraceProgram(DataElement progElement, String traceFormat) {

   if (!progElement.isOfType("executable")) {
     
     Display d = getShell().getDisplay();
	 d.asyncExec(new showMessageAction("Invalid Trace Program", 
	             "Not a platform executable:\n" + progElement.getSource()));
	 return;
   
   }
   
   DataStore dataStore = progElement.getDataStore();
   
   DataElement traceProject = findOrCreateTraceProjectElement(progElement);
   
   // Figure out the type from the trace format
   String type = null;
   if (traceFormat.indexOf("gprof") >= 0)
    type = "gprof trace program";
   else if (traceFormat.equals("functioncheck"))
    type = "functioncheck trace program";
   else
    type = "unknown trace program";
   
   // Create the trace program element
   DataElement traceProgram = dataStore.createObject(traceProject, type, progElement.getName(), progElement.getSource());
   getDataStore().createReference(getLocalTraceFilesRoot(), traceProgram);
   
   // Create references to the original file and project
   dataStore.createReference(traceProgram, progElement, "referenced file");
   dataStore.createReference(traceProgram, _cppApi.getProjectFor(progElement), "referenced project");

   setAttribute(traceProgram, "trace format", traceFormat);
   
   if (dataStore != getDataStore()) {
     dataStore.setObject(traceProject);
   }
   
   PATraceEvent traceEvent = new PATraceEvent(PATraceEvent.FILE_CREATED, traceProgram);
   _notifier.fireTraceChanged(traceEvent);
   
 }
  
 /**
  * Run a trace program
  */
 public DataElement runTraceProgram(DataElement traceProgram) {
 
   DataElement exeElement = (DataElement)traceProgram.getAssociated("referenced file").get(0);
   DataStore dataStore = exeElement.getDataStore();
   return runCommand(dataStore, exeElement.getParent(), exeElement.getName());
 }
 
 /**
  * Analyze a trace program after the program is run and the profile output file
  * has been generated.
  */
 public void analyzeTraceProgram(DataElement traceProgram) {
    
   DataStore dataStore = traceProgram.getDataStore();        
   DataElement analyzeCommand = dataStore.localDescriptorQuery(traceProgram.getDescriptor(), "C_ANALYZE_PROGRAM");
   DataElement status = dataStore.command(analyzeCommand, traceProgram);
   
   monitorStatus(status, traceProgram);
   
 }
 
 
 /**
  * Query the trace file format
  */
 public void queryTraceFileFormat(DataElement fileElement) {
 
   DataStore dataStore = fileElement.getDataStore();
   DataElement queryTraceCommand = dataStore.localDescriptorQuery(fileElement.getDescriptor(), "C_QUERY_TRACE_FILE_FORMAT");
   DataElement status = dataStore.command(queryTraceCommand, fileElement);
   
   monitorStatus(status, fileElement);
 }
 
 /**
  * Query the trace program format
  */
 public void queryTraceProgramFormat(DataElement progElement) {
 
   if (progElement.isOfType("executable")) {
   
    DataStore dataStore = progElement.getDataStore();
    DataElement queryTraceCommand = dataStore.localDescriptorQuery(progElement.getDescriptor(), "C_QUERY_TRACE_PROGRAM_FORMAT");
    DataElement status = dataStore.command(queryTraceCommand, progElement);
    
    monitorStatus(status, progElement);     
   }
   
 }  
 
}
