package org.eclipse.cdt.pa.ui.api;
 
/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.extra.internal.extra.*;
import org.eclipse.cdt.cpp.ui.internal.CppPlugin;
import org.eclipse.cdt.cpp.ui.internal.api.*;
import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.cpp.ui.internal.vcm.*;
import org.eclipse.cdt.pa.ui.*;
import org.eclipse.cdt.pa.ui.views.*;
import org.eclipse.cdt.pa.ui.actions.*;

import org.eclipse.ui.*;
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

 public class ShowViewAction implements Runnable
 {

    private String      _id;
    private DataElement _input;

    public ShowViewAction(String id, DataElement input)
    {
      _id = id;
      _input = input;
    }

    public void run()
    {
      IWorkbench desktop = PlatformUI.getWorkbench();
      IWorkbenchWindow win = desktop.getActiveWorkbenchWindow();

      IWorkbenchPage persp= win.getActivePage();
      IViewPart viewPart = persp.findView(_id);

      if (viewPart != null && viewPart instanceof ILinkable)
	  {	
	    persp.bringToTop(viewPart);

	    ILinkable linkablePart = (ILinkable)viewPart;
	    {
		if (_input != null)
		    linkablePart.setInput(_input);	
	    }
	  } 
    }
    
 }
 
 
 private static PAModelInterface _instance;
 
 private PAPlugin				 _plugin;
 private ModelInterface  		 _cppApi;
 private PATraceNotifier 		 _notifier;
 private CppProjectNotifier		 _cppNotifier;
 private DataStore       		 _dataStore; 
 private HashMap 				 _statuses;
 private String					 _outputViewId;
 private HashMap			 	 _projectsRootMap;
 private HashMap				 _traceFilesRootMap;
 
 private DataElement 			 _selection;
 private DataElement 			 _localProjectsRoot;
 private DataElement 			 _localTraceFilesRoot;
 private DataElement 			 _dummyElement;
 
 private boolean     			 _isShowAll;
 
 // Flags for pending operations
 private boolean				 _addTraceFilePending;
 private boolean				 _analyzeOperationPending;
 private boolean				 _performGotoPending;
 private boolean				 _isRunningTraceProgram;
 
 // Constructor
 public PAModelInterface(DataStore dataStore) {
  
  _instance = this;
  
  _dataStore = dataStore;
  _plugin    = PAPlugin.getDefault();
  _notifier  = new PATraceNotifier(this);
  _notifier.enable(true);
  
  _cppApi = CppPlugin.getDefault().getModelInterface();
  
  _cppNotifier = _cppApi.getProjectNotifier();
  _cppNotifier.addProjectListener(PAProjectAdaptor.getInstance());
  
  dataStore.getDomainNotifier().addDomainListener(this);
  
  _selection 	     = null;
  _statuses		     = new HashMap();
  _projectsRootMap   = new HashMap();
  _traceFilesRootMap = new HashMap();
  _outputViewId 	 = "org.eclipse.cdt.cpp.ui.CppOutputViewPart";
  _localProjectsRoot   = null;
  _localTraceFilesRoot = null;
  _dummyElement        = null;
    
  _isShowAll      		 = false;
  _addTraceFilePending 	 = false;
  _performGotoPending	 = false;
  _analyzeOperationPending = false;
  _isRunningTraceProgram = false;
  
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
  * Are we currently running a trace program?
  */
 public boolean isRunningTraceProgram() {
   return _isRunningTraceProgram;
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
    else {
     // System.out.println("Error -- projects root is null for datastore:" + dataStore);
    }
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
    else {
     // System.out.println("Error -- trace files root is null for datastore:" + dataStore);
    }
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
    DataElement projectsRoot = getLocalProjectsRoot();
    if (projectsRoot != null) {
     _dummyElement = _dataStore.createObject(projectsRoot, "data", "No input");
    }
   }
   
   return _dummyElement;
 }
 

 // From IDomainListener
 public boolean listeningTo(DomainEvent ev) {
 
   DataElement parent = (DataElement)ev.getParent();

   if (_statuses.containsKey(parent) && parent.getName().equals("done"))
    return true;
   else 
    return false;
 }
 
 // From IDomainListener
 public void domainChanged(DomainEvent ev) 
 {

   DataElement object = (DataElement)ev.getParent();
   
   // Only do something when the object's type is status.
   if ((object != null) && (object.getType().equals("status")))
   {

     object.getDataStore().setUpdateWaitTime(100);
   
     DataElement traceElement = (DataElement)_statuses.get(object);
     _statuses.remove(object);

     // System.out.println("status in model interface: " + object);
     
     // Handle parse errors from the miner
     if (object.getValue().equals("error"))
     {
      
      System.out.println("Error parsing trace file or program");
      
      String name = new String(traceElement.getSource());
      
      String errorMsg = null;
      
      // Generate error messages for trace files
      if (traceElement.isOfType("trace file")) 
      {
        
        // Fire a notification to delete the trace file 
        // if there is a parse error
        PATraceEvent traceEvent = new PATraceEvent(PATraceEvent.FILE_DELETED, traceElement);
        _notifier.fireTraceChanged(traceEvent);
        
        errorMsg = "Not a valid " + traceElement.getType() + ":\n" + name;
        
        traceElement.getDataStore().deleteObject(traceElement.getParent(), traceElement);
          
      }
      
      // Generate error messages for trace programs
      else if (traceElement.isOfType("trace program")) {
        
        errorMsg = "Error parsing the trace output of:\n" + name;
        
        // If the error code exists, generate an error message from the error code.
        DataStore dataStore = traceElement.getDataStore();
        DataElement errorElement = dataStore.find(traceElement, DE.A_TYPE, "error code", 1);
      
        if (errorElement != null) {      
          errorMsg = getErrorMessage(errorElement, traceElement);        
        }

      }
      
      // Bring up a message dialog to show the error messages.
      Display d = getShell().getDisplay();
	  d.asyncExec(new showMessageAction("Trace Parsing Error", errorMsg));
            
     }
     
     // Hanle the normal case
     else 
     {
       
       DataElement cmdD = object.getParent();
       String commandValue = cmdD.getName();
       
       // If the command is query trace file or program format
       if (commandValue.equals("C_QUERY_TRACE_FILE_FORMAT") 
          || commandValue.equals("C_QUERY_TRACE_PROGRAM_FORMAT"))
       {
         // If there is a pending add trace file operation.
         if (_addTraceFilePending)
         {
            _addTraceFilePending = false;
            
     		String traceFormat = new String(object.getValue());
          
     		// Display a message if it is not a valid trace file.
     		if (traceFormat.equals("invalid trace file")) 
     		{       
       		  Display d = getShell().getDisplay();
	   		  d.asyncExec(new showMessageAction("Invalid Trace File", "Not a valid trace file:\n" + traceElement.getSource()));
       		  return;
     		}
     		else
     		{
     		  addTraceFile(traceElement, traceFormat);
     		}
          }
          else 
          {
            PATraceEvent traceEvent = new PATraceEvent(PATraceEvent.FORMAT_CHANGED, object, traceElement);
            _notifier.fireTraceChanged(traceEvent);
          }
       }
       
       // Fire a FILE_PARSED notification if the command is to parse a trace file 
       // or analyze a trace program.
       else if (commandValue.equals("C_PARSE_TRACE") || commandValue.equals("C_ANALYZE_PROGRAM"))
       {  
         PATraceEvent traceEvent = new PATraceEvent(PATraceEvent.FILE_PARSED, traceElement);
         _notifier.fireTraceChanged(traceEvent);
       }
       
       // Post handling after running a trace program
       else if (commandValue.equals("C_COMMAND"))
       {
         _isRunningTraceProgram = false;
        
         if (_analyzeOperationPending)
         {
           _analyzeOperationPending = false;
           analyzeTraceProgram(traceElement);
         }
       }
       
       // Post handling after querying the source location of a trace function
       else if (commandValue.equals("C_PROVIDE_SOURCE_FOR"))
       {
         if (_performGotoPending)
         {
           _performGotoPending = false;
           
           PAOpenAction openAction = (PAOpenAction)PAActionLoader.getInstance().getOpenAction();
           openAction.gotoSourceLocation();
         }
       }
  	  
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
  * Monitor the command status.
  * This version can display the progress indicator in the status line and
  * set the update wait time.
  */
 public void monitorStatus(DataElement status, DataElement traceElement, boolean updateStatus, int updateInterval)
 {
   if (status != null && !_statuses.containsKey(status))
   {
     status.getDataStore().setUpdateWaitTime(updateInterval);
     _statuses.put(status, traceElement);
     
     // Start a status monitor thread if we want to see the progress monitor.
     if (updateStatus) {
     
	   IProject project = CppPlugin.getDefault().getCurrentProject();
	   MonitorStatusThread monitor = new MonitorStatusThread(status, project);
	   monitor.setWaitTime(400);
	   monitor.start(); 
	 }
			
   }
 }
 
 
 /**
  * Return an error message from the error code data element
  */
 public String getErrorMessage(DataElement errorElement, DataElement traceElement) {
 
   String result = "Unknown error";
   
   String errorCode = errorElement.getName();
   if (errorCode != null) 
   {
    
     if (errorCode.equals("no trace data")) 
     {
                
       result = "Cannot find the trace data file: " + getProfileDataFileName(traceElement) +
       			".\n" + "You should run the trace program before analyzing.";
     }
     else if (errorCode.equals("no file")) 
     {
       result = "Cannot find the trace target file:\n" + traceElement.getSource();
     }
     else if (errorCode.equals("no command")) 
     {
             
       result = "The profile command " + getProfileCommandName(traceElement) + 
       			" does not exist or it is not an executable!";
     }
     else if (errorCode.equals("unsupported option")) 
     {
     
       result = "You might have an incompatible version of " + getProfileCommandName(traceElement) + 
                ". Please upgrade to a newer version.";
     }
     
   }
   
   return result;
 }
 
 
 /**
  * Return the profile command name for a given trace element
  */
 public String getProfileCommandName(DataElement traceElement) {
 
   String profileCommand = "";
   if (traceElement.isOfType("gprof trace program")) {
     profileCommand = "gprof";
   }
   else if (traceElement.isOfType("functioncheck trace program")) {
     profileCommand = "fcdump";
   }
   else {
   
     // The default profile command is the first word of the type.
     String type = traceElement.getType();
     int spaceIndex = 0;
     if (type != null && (spaceIndex = type.indexOf(' ')) > 0) {
      profileCommand = type.substring(0, spaceIndex);
     }
     else {
      profileCommand = type;
     }
   }
   
   return profileCommand;
 }
 
 
 /**
  * Return the profile data file name for a given trace element
  */
 public String getProfileDataFileName(DataElement traceElement) {

   String dataFileName = "";
   if (traceElement.isOfType("gprof trace program"))
     dataFileName = "gmon.out";
   else if (traceElement.isOfType("functioncheck trace program"))
     dataFileName = "functioncheck.fc";
 
   return dataFileName;
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

  String source = project.getLocation().toString();
  DataStore dataStore = _dataStore;
    
  if (project instanceof Repository)
  {	      
	dataStore = ((Repository)project).getDataStore();
  }
  
  DataElement traceProject = dataStore.find(getProjectsRoot(dataStore), DE.A_SOURCE, source);
  return traceProject;
  
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
 public DataElement getReferencedFile(DataElement traceElement) {
 
  ArrayList references = traceElement.getAssociated("referenced file");
  
  if (references.size() > 0)
   return (DataElement)references.get(0);
  else
   return null;
   
 }

 /**
  * Return the referenced project for a given trace element
  */
 public DataElement getReferencedProject(DataElement traceElement) {
 
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
   * Auto detect the trace file format and add it to the trace files view.
   * We will only query the trace file format for now and set the _addTraceFilePending 
   * flag to true. When the query is done, it will detect whether there is a pending add
   * trace file action and call the real addTraceFile() afterwards.
   */
  public void addAutoTraceFile(DataElement fileElement) {

    _addTraceFilePending = true;
    queryTraceFileFormat(fileElement);
  }
  
  
 /**
  * Add a trace file with the given format
  */
 public DataElement addTraceFile(DataElement fileElement, String traceFormat) {

   DataStore dataStore = fileElement.getDataStore();
  
   // Set the type of the trace file
   String type = null;
   if (traceFormat.indexOf("gprof") >= 0)
     type = "gprof trace file (us)";
   else if (traceFormat.indexOf("functioncheck") >= 0)
     type = "functioncheck trace file";
   else
     type = traceFormat + " trace file";
   
   DataElement traceProject = findOrCreateTraceProjectElement(fileElement);
   
   // Create a data element to store the trace format.
   DataElement traceFormatElement = dataStore.createObject(null, "data", traceFormat);
   
   // Synchronize the data element update. This is only needed for remote.
   if (dataStore != getDataStore()) {
    dataStore.setObject(traceFormatElement);    
   }
      
   // Create the trace file element
   DataElement traceFile = dataStore.createObject(traceProject, type, fileElement.getName(), fileElement.getSource());
      
   // Create references to the original file and project
   dataStore.createReference(traceFile, fileElement, "referenced file");
   dataStore.createReference(traceFile, _cppApi.getProjectFor(fileElement), "referenced project");
   dataStore.createReference(traceFile, traceFormatElement, "trace format");

   // Create trace functions root and call tree root
   DataElement traceFunctionsRoot = dataStore.createObject(traceFile, traceFile.getType(), traceFile.getName());
   traceFunctionsRoot.setAttribute(DE.A_VALUE, "trace functions root");
   
   DataElement callTreeRoot = dataStore.createObject(traceFile, "call root", traceFile.getName());
   
   // Update everything under the trace file for remote.
   if (dataStore != getDataStore()) {
    dataStore.setObject(traceFile);
   }

   // Create a reference to the trace file from the local trace files root
   getDataStore().createReference(getLocalTraceFilesRoot(), traceFile);
   
   // Fire the file created event
   PATraceEvent traceEvent = new PATraceEvent(PATraceEvent.FILE_CREATED, traceFile);
   _notifier.fireTraceChanged(traceEvent);
   
   // call the parse trace command
   DataElement parseCommand = dataStore.localDescriptorQuery(traceFile.getDescriptor(), "C_PARSE_TRACE");
   DataElement status = dataStore.command(parseCommand, traceFile);
   
   // Monitor the parse status
   monitorStatus(status, traceFile, true, 500);
   
   return traceFile;
 }

 
 /**
  * Remove a trace file or program.
  */
 public void removeTraceTarget(DataElement traceTarget) {
   
   if (traceTarget.isOfType("trace program") && traceTarget.getValue().equals("trace functions root")) {
    traceTarget = traceTarget.getParent();
   }
      
   DataStore dataStore = traceTarget.getDataStore();
   
   PATraceEvent traceEvent = new PATraceEvent(PATraceEvent.FILE_DELETED, traceTarget);
   _notifier.fireTraceChanged(traceEvent);
   
   DataElement removeCommand = dataStore.localDescriptorQuery(traceTarget.getDescriptor(), "C_REMOVE_TRACE_TARGET");
   dataStore.command(removeCommand, traceTarget);
   
 }
 
 /**
  * Add a trace program with the given trace format and arguments
  */
 public DataElement addTraceProgram(DataElement progElement, String traceFormat, String arguments) {

   // Display an error message if the element is not a binary executable.
   if (!progElement.isOfType("binary executable")) {
     
     Display d = getShell().getDisplay();
	 d.asyncExec(new showMessageAction("Invalid Trace Program", 
	             "Not a platform executable:\n" + progElement.getSource()));
	 return null;
   
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
    type = traceFormat + " trace program";
   
   // Create the trace format element.
   DataElement traceFormatElement = dataStore.createObject(null, "data", traceFormat);
   if (dataStore != getDataStore()) {
     dataStore.setObject(traceFormatElement);   
   }
   
   // Create the trace program element
   DataElement traceProgram = dataStore.createObject(traceProject, type, progElement.getName(), progElement.getSource());
   
   // Store the arguments in the VALUE attribute of the trace program element.
   if (arguments != null && arguments.trim().length() > 0) {
     traceProgram.setAttribute(DE.A_NAME, progElement.getSource() + " " + arguments);
   }
   else {
     traceProgram.setAttribute(DE.A_NAME, progElement.getSource());
   }
   
   traceProgram.setAttribute(DE.A_VALUE, progElement.getName());
  
   // Create references to the original file and project
   dataStore.createReference(traceProgram, progElement, "referenced file");
   dataStore.createReference(traceProgram, _cppApi.getProjectFor(progElement), "referenced project");
   dataStore.createReference(traceProgram, traceFormatElement, "trace format");
   
   // Create trace functions root and call tree root
   DataElement traceFunctionsRoot = dataStore.createObject(traceProgram, traceProgram.getType(), traceProgram.getName());
   traceFunctionsRoot.setAttribute(DE.A_VALUE, "trace functions root");
   
   DataElement callTreeRoot = dataStore.createObject(traceProgram, "call root", traceProgram.getName());
   
   if (dataStore != getDataStore()) {
     dataStore.setObject(traceProgram);
   }
   
   // Create a reference from local trace files root to the trace program
   getDataStore().createReference(getLocalTraceFilesRoot(), traceProgram);
   
   // Fire a file created event
   PATraceEvent traceEvent = new PATraceEvent(PATraceEvent.FILE_CREATED, traceProgram);
   _notifier.fireTraceChanged(traceEvent);
   
   return traceProgram;
 }
  
 /**
  * Run a trace program.
  * The run is done by invoking the command miner.
  */
 public DataElement runTraceProgram(DataElement traceProgram) {
 
   if (traceProgram.getValue().equals("trace functions root")) {
    traceProgram = traceProgram.getParent();
   }
   
   DataElement exeElement = (DataElement)traceProgram.getAssociated("referenced file").get(0);
   DataStore dataStore = exeElement.getDataStore();
   
   _isRunningTraceProgram = true;
   DataElement cmdStatus = runCommand(dataStore, exeElement.getParent(), traceProgram.getName());
   
   monitorStatus(cmdStatus, traceProgram);
   
   Display d = getShell().getDisplay();
   ShowViewAction action = new ShowViewAction(_outputViewId, cmdStatus);
   d.asyncExec(action);		
   
   return cmdStatus;
 }
 
 
 /**
  * Analyze the trace output generated by the run.
  * The actual job is done in the miner.
  */
 public void analyzeTraceProgram(DataElement traceProgram) {

   // This is to work around the problem when you select the analyze action from
   // the popup menu of the empty Function Statstics view.
   if (traceProgram.getValue().equals("trace functions root")) {
    traceProgram = traceProgram.getParent();
   }
   
   // Call the C_ANALYZE_PROGRAM command in PAMiner to do the real job.
   DataStore dataStore = traceProgram.getDataStore();        
   
   DataElement analyzeCommand = dataStore.localDescriptorQuery(traceProgram.getDescriptor(), "C_ANALYZE_PROGRAM");
   DataElement status = dataStore.command(analyzeCommand, traceProgram);
   
   // Monitor the parse status.
   monitorStatus(status, traceProgram, true, 500);
   
 }
 
 
 /**
  * Run the executable to produce the trace output. 
  * Then analyze the trace result.
  */
 public void runAndAnalyzeTraceProgram(DataElement traceProgram) {

   _analyzeOperationPending = true;
   
   DataElement cmdStatus = runTraceProgram(traceProgram);
   
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
 
   if (progElement.isOfType("binary executable")) {
   
    DataStore dataStore = progElement.getDataStore();
    DataElement queryTraceCommand = dataStore.localDescriptorQuery(progElement.getDescriptor(), "C_QUERY_TRACE_PROGRAM_FORMAT");
    DataElement status = dataStore.command(queryTraceCommand, progElement);
    
    monitorStatus(status, progElement);     
   }
   
 }
 
 /**
  * Find the source location of a trace function by querying the parser
  */
 public DataElement findTraceSourceLocation(DataElement traceFunctionElement, boolean hasPendingGoto) {

   DataStore dataStore = traceFunctionElement.getDataStore();
   DataElement cppObjD = dataStore.find(dataStore.getDescriptorRoot(), DE.A_NAME, "Cpp Object", 1);
   if (cppObjD == null) {
    System.out.println("Cannot find the Cpp Object for dataStore: " + dataStore);
    return null;
   }
   
   DataElement result = null;
   DataElement provideSourceForD = dataStore.localDescriptorQuery(cppObjD, "C_PROVIDE_SOURCE_FOR", 1);

   if (provideSourceForD != null) {     

     DataElement traceFile = getContainingTraceFile(traceFunctionElement);
     DataElement projectElement = getReferencedProject(traceFile);
   
     if (projectElement != null) {
       
       ArrayList args = new ArrayList();
       args.add(projectElement);
       result = dataStore.command(provideSourceForD, args, traceFunctionElement, false);
       
       if (hasPendingGoto) {
         _performGotoPending = true;
         monitorStatus(result, traceFunctionElement);
       }
     }
   }
   
   return result;
 
 }
 
 
  /**
   * Open the PA perspective
   */
  public void openPerspective()
  {
     IWorkbench workbench = _plugin.getWorkbench();
	 IWorkbenchWindow dw  = workbench.getActiveWorkbenchWindow();
	
	 try 
	 {
	   workbench.showPerspective("org.eclipse.cdt.pa.ui.PAPerspective", dw);
	 }
     catch (WorkbenchException e)
	 {
	 }
	 
  }
    
}
