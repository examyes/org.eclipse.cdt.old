package org.eclipse.cdt.cpp.miners.pa;


import java.util.*;
import java.io.*;
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.core.client.*;

import org.eclipse.cdt.cpp.miners.pa.engine.*;
import org.eclipse.cdt.cpp.miners.pa.engine.gprof.*;
import org.eclipse.cdt.cpp.miners.pa.engine.functioncheck.*;


public class PADataStoreAdaptor {

 // attributes
 private static PAMiner		_miner;
 private static DataStore   _dataStore;
 private static DataElement _provideSourceForD;
 	
 private DataElement _traceElement;
 private DataElement _referencedProject;
 private DataElement _traceFunctionsRoot;
 private DataElement _callTreeRoot;
 private DataElement _attributesRoot;
 private DataElement _callArcsRoot;
 
 // maps between PA trace functions and their corresponding data elements
 private HashMap _elementToTraceFuncMap;
 private HashMap _traceFuncToElementMap;
 
 
 // Constructors
 public PADataStoreAdaptor(DataElement traceElement) {
  
   _traceElement = traceElement;
   _elementToTraceFuncMap = new HashMap();
   _traceFuncToElementMap = new HashMap();

   ArrayList projects = traceElement.getAssociated(getLocalizedString("pa.ReferencedProject"));
     
   if (projects.size() > 0) {
    _referencedProject = (DataElement)projects.get(0);
   }
   else {
    _referencedProject = null;
    System.out.println("Cannot find the containing project for trace element: " + traceElement.getName());
   }
 } 
 
 /**
  * Set the PA miner
  */
 public static void setMiner(PAMiner miner) {
 
   _miner = miner;
   _dataStore = miner.getDataStore();
   
   // Find the provideSourceFor command descriptor
   DataElement cppObjD = _dataStore.find(_dataStore.getDescriptorRoot(), DE.A_NAME, "Cpp Object", 1);
   if (cppObjD == null) {
    System.out.println("Cannot find the Cpp Object");
    return;
   }
   
   _provideSourceForD = _dataStore.localDescriptorQuery(cppObjD, "C_PROVIDE_SOURCE_FOR", 1);    
 }
 
 /**
  * Return the localized string from a given key
  */
 public static String getLocalizedString(String key) {
   return _miner.getLocalizedString(key);
 }
 
 /**
  * Run the given command and retrieve the command output
  */
 public static ArrayList getCommandResult(File workingDir, String command) {
 
  // Run the given command
  Process process = null;
  String theShell = getShell();
  try {
  
   if (theShell.equals("sh")) {
     String args[] = new String[3];
     args[0] = theShell;
     args[1] = "-c";
     args[2] = command;
     process = Runtime.getRuntime().exec(args, null, workingDir);
   }
   else {
     process = Runtime.getRuntime().exec(theShell + command, null, workingDir);
   }
  }
  catch (Exception e) {
   System.out.println("Error running the command: " + command);
   return null;
  }
    
  // Parse the command output
  BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
  
  ArrayList results = new ArrayList();
  String line = null;
  try {
    while ((line = reader.readLine()) != null) {
     results.add(line);
    }
    reader.close();
  }
  catch (IOException e) {
   System.out.println(e);
  }
  
  return results;
 }
 
 /**
  * Return the shell command for a particular OS
  */
 private static String getShell() {
 
   String theOS = System.getProperty("os.name");
   String theShell = null;
   
   if (!theOS.toLowerCase().startsWith("win")) {
    theShell = "sh";
   }
   else {
   
    if ((theOS.indexOf("95")>=0) ||
        (theOS.indexOf("98")>=0) ||
        (theOS.indexOf("ME")>=0)) 
    {
	   theShell = "start /B ";
    }
    else
    {
	  theShell = "cmd /C ";
    }
   }
   
   return theShell;
 }
 
 
 /**
  * Return the first non-empty output line for a command
  */
 public static String getFirstCommandOutputLine(File workingDir, String command) {
   
  // System.out.println("command: " + command);
  
  // Run the given command
  Process process = null;
  String theShell = getShell();
  try {
  
   if (theShell.equals("sh")) {
     String args[] = new String[3];
     args[0] = theShell;
     args[1] = "-c";
     args[2] = command;
     process = Runtime.getRuntime().exec(args, null, workingDir);
   }
   else {
     process = Runtime.getRuntime().exec(theShell + command, null, workingDir);
   }
  }
  catch (Exception e) {
   System.out.println("Error running the command: " + command);
   return null;
  }
     
  // Parse the command output
  BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
  
  return getFirstLine(reader);
 }
 
 /**
  * Retrieve the first non-empty line from a buffered reader
  */
 public static String getFirstLine(BufferedReader reader) {
 
   String result = null;
   try {
     
     Thread.sleep(100);
     while (reader.ready() && (result = reader.readLine()) != null) {
       if (result.trim().length() > 0)
        break;
     }
     
     reader.close();
   }
   catch (Exception e) {
     System.out.println(e);
   }
   
   return result;
 }
 
 
 /**
  * Return an error code from the error stream
  */
 public static String getErrorCode(BufferedReader stderr) {
 
   String firstLine = getFirstLine(stderr);
   if (firstLine != null) {
   
     if (firstLine.indexOf("gmon.out:") >= 0)
      return getLocalizedString("pa.NoTraceData");
     
     else if (firstLine.indexOf("ommand not found") >= 0)
      return getLocalizedString("pa.NoCommand");
     
     else if (firstLine.indexOf("o such file or directory") >= 0 ||
     		  firstLine.indexOf("does not exist") >= 0)
      return getLocalizedString("pa.NoFile");
     
     else if (firstLine.indexOf("he specified flag is not valid") >= 0 ||
     		  firstLine.indexOf("unrecognized option") >= 0)
      return getLocalizedString("pa.NoOption");
     
     else
      return null;
   }
   else
     return null;
     
 }
 
 /**
  * Return the attribute value associated with a given data element
  */
 public static String getAttribute(DataElement element, String name) {
  
  ArrayList attributes = element.getAssociated(getLocalizedString("pa.Attributes"));
  for (int i=0; i < attributes.size(); i++) {
   DataElement anAttr = (DataElement)attributes.get(i);
   if (!anAttr.isDeleted() && anAttr.getType().equals(name)) {
    return anAttr.getName();
   }
  }
  
  return null;
 }

 /**
  * Set the attribute to a given value for a data element
  */
 public static DataElement setAttribute(DataElement element, String name, String value) {
  
  DataElement attributeElement = null;
  ArrayList attributes = element.getAssociated(getLocalizedString("pa.Attributes"));
  for (int i=0; i < attributes.size(); i++) {
   DataElement anAttr = (DataElement)attributes.get(i);
   if (anAttr.getType().equals(name))
    attributeElement = anAttr;
  }
  
  if (attributeElement != null) {
   attributeElement.setAttribute(DE.A_NAME, value);
  }
  else {
   DataStore dataStore = element.getDataStore();
   attributeElement = dataStore.createObject(null, name, value);
   dataStore.createReference(element, attributeElement, getLocalizedString("pa.Attributes"));
  }

  return attributeElement;  
 }
 
 /**
  * Return the trace file format as a String
  */
 public static String getTraceFileFormat(PATraceFile traceFile) {
 
   if (traceFile instanceof GprofTraceFile)
    return getLocalizedString("pa.gprofTraceFile");
   else if (traceFile instanceof FunctionCheckTraceFile)
    return getLocalizedString("pa.fcTraceFile");
   else
    return getLocalizedString("pa.UnknownTraceFile");
    
 }
 
 /**
  * Return the trace file format as a String
  */
 public static String getTraceFunctionFormat(PATraceFunction traceFunction) {
 
  String type = null;
  
  if (traceFunction instanceof FunctionCheckTraceFunction) {
   if (traceFunction.isCyclic())
    type = getLocalizedString("pa.fcCyclicTraceFunction");
   else
    type = getLocalizedString("pa.fcTraceFunction");
  }
  else {
   if (traceFunction.isCyclic()) {
    type = getLocalizedString("pa.gprofCyclicTraceFunction");
   }
   else
    type = getLocalizedString("pa.gprofTraceFunction");   
  }
  
  return type;
  
 } 

 /**
  * Find out the source location for a trace function
  */
 public void getSourceLocation(DataElement traceFuncElement) {
       
   if (_provideSourceForD != null && _referencedProject != null) {     
     ArrayList args = new ArrayList();
     args.add(_referencedProject);
     _dataStore.command(_provideSourceForD, args, traceFuncElement, false);
   }
   
 }
 
 /**
  * Find out the source location for a trace function
  */
 public static void provideSourceFor(DataElement traceFuncElement) {

   if (_provideSourceForD != null) {     

     DataElement traceFile = traceFuncElement.getParent().getParent();
     ArrayList projects = traceFile.getAssociated(getLocalizedString("pa.ReferencedProject"));
   
     DataElement projectElement = null; 
     if (projects.size() > 0) {
       
       projectElement = (DataElement)projects.get(0);
       
       ArrayList args = new ArrayList();
       args.add(projectElement);
       _dataStore.command(_provideSourceForD, args, traceFuncElement, false);
     }
     else {
       System.out.println("Cannot find the containing project for trace function: " + traceFuncElement.getName());
     }   
   }
 
 }
 
 
 /**
  * Remove the trace information for the given trace element
  */
 public static void cleanTraceInformation(DataElement traceElement) {
 
   // System.out.println("Calling cleanTraceInformation");
   DataStore dataStore = traceElement.getDataStore();
   
   DataElement traceFunctionsRoot = _dataStore.find(traceElement, DE.A_VALUE, getLocalizedString("pa.TraceFuncRoot"));
   DataElement callTreeRoot 	  = _dataStore.find(traceElement, DE.A_TYPE,  getLocalizedString("pa.CallRoot"));
   DataElement attributesRoot 	  = _dataStore.find(traceElement, DE.A_NAME,  getLocalizedString("pa.AttributesRoot"));
   DataElement callArcsRoot 	  = _dataStore.find(traceElement, DE.A_NAME,  getLocalizedString("pa.CallArcsRoot"));
   
   deleteChildren(traceFunctionsRoot);
   deleteChildren(callTreeRoot);
   dataStore.deleteObject(traceElement, attributesRoot);
   dataStore.deleteObject(traceElement, callArcsRoot);
     
 }
  
 /**
  * Delete all the children under a given data element
  */
 private static void deleteChildren(DataElement element) {
 
   DataStore dataStore = element.getDataStore();
   
   int size = element.getNestedSize();
   for (int i=0; i < size; i++) {
     DataElement child = element.get(i);
     dataStore.deleteObject(element, child);
   }
 }
 
 
 /**
  * Populate the datastore using the information from the PA trace file.
  */
 public void populateDataStore(DataElement fileElement, PATraceFile traceFile) {
   
   if (_dataStore.find(fileElement, DE.A_NAME, getLocalizedString("pa.AttributesRoot")) != null) {
     cleanTraceInformation(fileElement);
   }
                    
   // Find the trace functions root and call tee root  
   _traceFunctionsRoot = _dataStore.find(fileElement, DE.A_VALUE, getLocalizedString("pa.TraceFuncRoot"), 1);
   _callTreeRoot   = _dataStore.find(fileElement, DE.A_TYPE, getLocalizedString("pa.CallRoot"), 1);
   
   // Create the attributes root and call arcs root 
   _attributesRoot = _dataStore.createObject(fileElement, getLocalizedString("pa.data"), getLocalizedString("pa.AttributesRoot"));
   _callArcsRoot   = _dataStore.createObject(fileElement, getLocalizedString("pa.data"), getLocalizedString("pa.CallArcsRoot"));

   // set the trace file attributes
   createTraceFileSummary(fileElement, traceFile);
      
   // Process each trace function
   Iterator it = traceFile.getTraceFunctions().iterator();
   
   while (it.hasNext()) {
    PATraceFunction trcFunc = (PATraceFunction)it.next();
    DataElement trcFuncElement = createTraceFunction(_traceFunctionsRoot, trcFunc);
    
    // set trace function attributes
    createTraceFunctionSummary(trcFuncElement, trcFunc);
    
    _elementToTraceFuncMap.put(trcFuncElement, trcFunc);
    _traceFuncToElementMap.put(trcFunc, trcFuncElement);
    
    // Create a "calls" reference between the call root and the top level
    // trace functions.
    if (trcFunc.isTopLevelFunction()) {
     _dataStore.createReference(_callTreeRoot, trcFuncElement, getLocalizedString("pa.Calls"));
    }
    
   }
  
   createCallNestingRelations(_traceFunctionsRoot);
 }
 
 /**
  * create an attribute for a DataElement
  */
 private void createAttribute(DataElement parent, String name, String value) {
 
  DataElement anAttribute = _dataStore.createObject(_attributesRoot, name, value);
  _dataStore.createReference(parent, anAttribute, getLocalizedString("pa.Attributes"));
 }
 
 /**
  * Create summary information for a trace file element
  */
 private void createTraceFileSummary(DataElement fileElement, PATraceFile traceFile) {
 
  
  createAttribute(fileElement, getLocalizedString("pa.TraceFormat"),  		 traceFile.getTraceFormat());
  createAttribute(fileElement, getLocalizedString("pa.totalExecutionTime"),  String.valueOf(traceFile.getTotalExecutionTime()));
  createAttribute(fileElement, getLocalizedString("pa.numTraceFunctions"),   String.valueOf(traceFile.getNumberOfTraceFunctions()));
  createAttribute(fileElement, getLocalizedString("pa.numCallGraphEntries"), String.valueOf(traceFile.getNumberOfCallGraphEntries()));
 
  // create additional attributes for functioncheck trace files
  if (traceFile instanceof FunctionCheckTraceFile) {
   FunctionCheckTraceFile fcTraceFile = (FunctionCheckTraceFile)traceFile;
   createAttribute(fileElement, getLocalizedString("pa.TimeMode"),    fcTraceFile.getTimeMode());
   createAttribute(fileElement, getLocalizedString("pa.ProfileMode"), fcTraceFile.getProfileMode());
   createAttribute(fileElement, getLocalizedString("pa.ProcessId"),   String.valueOf(fcTraceFile.getProcessId()));
   createAttribute(fileElement, getLocalizedString("pa.ProgramName"), fcTraceFile.getProgramName());   
  }
  else if (traceFile instanceof GprofTraceFile) {
   GprofTraceFile gprofTraceFile = (GprofTraceFile)traceFile;
   createAttribute(fileElement, getLocalizedString("pa.SamplingRate"), String.valueOf(gprofTraceFile.getSamplingRate()));
  }
  
 }
 
 /**
  * Create summary information for a trace function element
  */
 private void createTraceFunctionSummary(DataElement funcElement, PATraceFunction trcFunc) {
 
  createAttribute(funcElement, getLocalizedString("pa.TotalPercentage"),  String.valueOf(trcFunc.getTotalPercentage()));
  createAttribute(funcElement, getLocalizedString("pa.numCalls"), 		  String.valueOf(trcFunc.getCallNumber()));
  createAttribute(funcElement, getLocalizedString("pa.TotalTime"), 		  String.valueOf(trcFunc.getTotalSeconds()));
  createAttribute(funcElement, getLocalizedString("pa.SelfTime"), 		  String.valueOf(trcFunc.getSelfSeconds()));
  createAttribute(funcElement, getLocalizedString("pa.TotalTimePerCall"), String.valueOf(trcFunc.getTotalMsPerCall()));
  createAttribute(funcElement, getLocalizedString("pa.SelfTimePerCall"),  String.valueOf(trcFunc.getSelfMsPerCall()));
    
  // create additional attributes for functioncheck trace functions
  if (trcFunc instanceof FunctionCheckTraceFunction) {
   FunctionCheckTraceFunction fcTrcFunc = (FunctionCheckTraceFunction)trcFunc;
   createAttribute(funcElement, getLocalizedString("pa.MinTotalTime"), String.valueOf(fcTrcFunc.getMinTotalTime())); 
   createAttribute(funcElement, getLocalizedString("pa.MaxTotalTime"), String.valueOf(fcTrcFunc.getMaxTotalTime()));
   createAttribute(funcElement, getLocalizedString("pa.MinSelfTime"),  String.valueOf(fcTrcFunc.getMinLocalTime()));
   createAttribute(funcElement, getLocalizedString("pa.MaxSelfTime"),  String.valueOf(fcTrcFunc.getMaxLocalTime()));
  }
  
 }
 
  /**
   * Create a trace function object in the datastore
   */
  public DataElement createTraceFunction(DataElement parent, PATraceFunction traceFunction) {
  
   String type = getTraceFunctionFormat(traceFunction);
   
   DataElement traceFuncElement = _dataStore.createObject(parent, type, traceFunction.getName());
   //traceFuncElement.expandChildren();
   getSourceLocation(traceFuncElement);
   return traceFuncElement;
  }


 /**
  * Create the call nesting relations for all trace functions
  */
 private void createCallNestingRelations(DataElement funcRoot) {
 
  int numTraceFuncs = funcRoot.getNestedSize();
  for (int i=0; i < numTraceFuncs; i++) {
   DataElement funcElement = funcRoot.get(i);
   PATraceFunction trcFunc = (PATraceFunction)_elementToTraceFuncMap.get(funcElement);
   createCallMap(funcElement, trcFunc);
  }
  
 }
 
 /**
  * Create the caller/callee relations for a given trace function
  */
 private void createCallMap(DataElement funcElement, PATraceFunction trcFunc) {
   
  ArrayList callees = trcFunc.getCallees();
  
  int numCallees = callees.size();
  for (int i=0; i < numCallees; i++) {
   PACallArc callArc = (PACallArc)callees.get(i);
   PATraceFunction callee = callArc.getCallee();
   DataElement calleeElement = (DataElement)_traceFuncToElementMap.get(callee);
   
   // create caller/callee relations
   _dataStore.createReference(funcElement, calleeElement, getLocalizedString("pa.Calls"));
   _dataStore.createReference(calleeElement, funcElement, getLocalizedString("pa.CalledBy"));
   
   // create a caller arc and a callee arc
   DataElement calleeArc    = _dataStore.createObject(_callArcsRoot, getLocalizedString("pa.CallArc"), callee.getName());
   DataElement callerArc    = _dataStore.createObject(_callArcsRoot, getLocalizedString("pa.CallArc"), trcFunc.getName());
   
   DataElement numCalls     = _dataStore.createObject(_attributesRoot, getLocalizedString("pa.numCalls"), String.valueOf(callArc.getCallNumber()));
   DataElement selfTime     = _dataStore.createObject(_attributesRoot, getLocalizedString("pa.SelfTime"), String.valueOf(callArc.getSelfTime()));
   DataElement childrenTime = _dataStore.createObject(_attributesRoot, getLocalizedString("pa.ChildrenTime"), String.valueOf(callArc.getChildrenTime()));
   
   // create the relations between trace functions and call arcs
   _dataStore.createReference(funcElement,   calleeArc, getLocalizedString("pa.CalleeArc"));
   _dataStore.createReference(calleeElement, callerArc, getLocalizedString("pa.CallerArc"));
   
   _dataStore.createReference(calleeArc, numCalls,     getLocalizedString("pa.Attributes"));
   _dataStore.createReference(calleeArc, selfTime,     getLocalizedString("pa.Attributes"));
   _dataStore.createReference(calleeArc, childrenTime, getLocalizedString("pa.Attributes"));
   
   _dataStore.createReference(callerArc, numCalls,     getLocalizedString("pa.Attributes"));
   _dataStore.createReference(callerArc, selfTime,     getLocalizedString("pa.Attributes"));
   _dataStore.createReference(callerArc, childrenTime, getLocalizedString("pa.Attributes"));
   
  }
  
 }
   
}
