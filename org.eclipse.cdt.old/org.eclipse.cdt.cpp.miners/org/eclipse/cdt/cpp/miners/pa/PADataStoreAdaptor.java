package org.eclipse.cdt.cpp.miners.pa;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

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
 
 private static String _attributesD;
 private static String _callsD;
 private static String _calledByD;
 private static String _callArcD;
 private static String _callerArcD;
 private static String _calleeArcD;
 private static String _numCallsD;
 private static String _selfTimeD;
 private static String _childrenTimeD;
 private static String _totalTimeD;
 private static String _timePercentD;
 private static String _selfMsPerCallD;
 private static String _totalMsPerCallD;
 private static String _minSelfTimeD;
 private static String _maxSelfTimeD;
 private static String _minTotalTimeD;
 private static String _maxTotalTimeD;
 
 private DataElement _traceElement;
 private DataElement _referencedProject;
 private DataElement _traceFunctionsRoot;
 private DataElement _callTreeRoot;
 private DataElement _attributesRoot;
 private DataElement _callArcsRoot;
 
 private String		 _traceFunctionFormat;
 private String		 _cyclicTraceFunctionFormat;
 private String		 _selfTimeString;
 private String		 _totalTimeString;
 private boolean	 _hasParsedSource;
  
 // This map stores the correspondence between PA trace functions and their 
 // corresponding data elements.
 private HashMap _traceFunctionsMap;
 
 
 // Create a PADataStoreAdaptor from a given trace element
 public PADataStoreAdaptor(DataElement traceElement) {
  
   _traceElement = traceElement;
   _traceFunctionsMap = new HashMap();
   
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
   
   // Initialize the relation strings
   _attributesD = getLocalizedString("pa.Attributes");
   _callsD		= getLocalizedString("pa.Calls");
   _calledByD   = getLocalizedString("pa.CalledBy");
   _callArcD	= getLocalizedString("pa.CallArc");
   _calleeArcD  = getLocalizedString("pa.CalleeArc");
   _callerArcD	= getLocalizedString("pa.CallerArc");
   _numCallsD	= getLocalizedString("pa.numCalls");
   _selfTimeD	= getLocalizedString("pa.SelfTime");
   _totalTimeD	= getLocalizedString("pa.TotalTime");

   _childrenTimeD 	= getLocalizedString("pa.ChildrenTime");   
   _timePercentD	= getLocalizedString("pa.TimePercentage");
   _selfMsPerCallD  = getLocalizedString("pa.SelfMsPerCall");
   _totalMsPerCallD = getLocalizedString("pa.TotalMsPerCall");
   _minSelfTimeD 	= getLocalizedString("pa.MinSelfTime");
   _maxSelfTimeD	= getLocalizedString("pa.MaxSelfTime");
   _minTotalTimeD	= getLocalizedString("pa.MinTotalTime");
   _maxTotalTimeD	= getLocalizedString("pa.MaxTotalTime");
 }
 
 
 /**
  * Return the localized string from a given key
  */
 public static String getLocalizedString(String key) {
   return _miner.getLocalizedString(key);
 }
 
 
 /**
  * Run a given command and return the status element
  */
 public static DataElement runCommand(DataElement workingDir, String command) {
   
   DataElement cmdD = _dataStore.localDescriptorQuery(workingDir.getDescriptor(), "C_COMMAND");
   ArrayList args = new ArrayList();
  			
   DataElement invocationObj = _dataStore.createObject(null, "invocation", command, "");
   args.add(invocationObj);
  
   DataElement cmdStatus = _dataStore.command(cmdD, args, workingDir);
   return cmdStatus;
 
 }
  
  
 /**
  * Return the trace file format as a String
  */
 public static String getTraceFileFormat(PATraceFile traceFile) {
 
   if (traceFile instanceof GprofTraceFile) {
    if (traceFile.getTimeUnit() == PATraceFile.TIME_UNIT_M)
      return getLocalizedString("pa.gprofTraceFile_ms");
    else if (traceFile.getTimeUnit() == PATraceFile.TIME_UNIT_U)
      return getLocalizedString("pa.gprofTraceFile_us");
    else if (traceFile.getTimeUnit() == PATraceFile.TIME_UNIT_T)
      return getLocalizedString("pa.gprofTraceFile_ts");
    else if (traceFile.getTimeUnit() == PATraceFile.TIME_UNIT_P)
      return getLocalizedString("pa.gprofTraceFile_ps");
    else
      return getLocalizedString("pa.gprofTraceFile");
    
   }
   else if (traceFile instanceof FunctionCheckTraceFile)
    return getLocalizedString("pa.fcTraceFile");
   else
    return traceFile.getClass().getName();
    
 }
 

 /**
  * Return the trace program format as a String
  */
 public static String getTraceProgramFormat(PATraceFile traceFile) {
 
   if (traceFile instanceof GprofTraceFile) {
    if (traceFile.getTimeUnit() == PATraceFile.TIME_UNIT_M)
      return getLocalizedString("pa.gprofTraceProgram_ms");
    else if (traceFile.getTimeUnit() == PATraceFile.TIME_UNIT_U)
      return getLocalizedString("pa.gprofTraceProgram_us");
    else if (traceFile.getTimeUnit() == PATraceFile.TIME_UNIT_T)
      return getLocalizedString("pa.gprofTraceProgram_ts");
    else if (traceFile.getTimeUnit() == PATraceFile.TIME_UNIT_P)
      return getLocalizedString("pa.gprofTraceProgram_ps");
    else
      return getLocalizedString("pa.gprofTraceProgram");
   }
   else if (traceFile instanceof FunctionCheckTraceFile)
    return getLocalizedString("pa.fcTraceProgram");
   else
    return getLocalizedString("pa.TraceProgram");
    
 }

 /**
  * Set the trace function format strings
  */
 private void setTraceFunctionFormat(PATraceFile traceFile) {
 
   _selfTimeString  = getLocalizedString("pa.SelfMsPerCall");
   _totalTimeString = getLocalizedString("pa.TotalMsPerCall");
   
   if (traceFile instanceof GprofTraceFile) {
   
     String timeUnit = traceFile.getTimeUnitString();
     _traceFunctionFormat = "gprof trace function (" + timeUnit + ")";
     _cyclicTraceFunctionFormat = "gprof cyclic trace function (" + timeUnit + ")";     
     _selfTimeString = "self " + timeUnit + "/call";
     _totalTimeString = "total " + timeUnit + "/call";
   }
   else if (traceFile instanceof FunctionCheckTraceFile) {
   
     _traceFunctionFormat = getLocalizedString("pa.fcTraceFunction");
     _cyclicTraceFunctionFormat = getLocalizedString("pa.fcCyclicTraceFunction");   
   }
   else {
   
     _traceFunctionFormat = getLocalizedString("pa.TraceFunction");
     _cyclicTraceFunctionFormat = getLocalizedString("pa.cyclicTraceFunction");
   }
 }
 
 
 /**
  * Round a double to an approximate value with 3 digits after the dot.
  */
 public static String roundDouble(double d) {
 
   String result = String.valueOf(d);
   int dotIndex = result.indexOf('.');
   int eIndex = result.indexOf('E');
   if (eIndex < 0)
    eIndex = result.indexOf('e');
         
   if (dotIndex >= 0) {
   
     if (eIndex > dotIndex) {
     
       if (eIndex > dotIndex + 4) {
         
         double base = d;
         try {
          base = Double.parseDouble(result.substring(0, eIndex)) + 0.0005;
         }
         catch (NumberFormatException e)
         {     
         }
         
         String baseStr = String.valueOf(base);
         result = baseStr.substring(0, dotIndex + 4) + result.substring(eIndex);
       }
     }
     else {
       
       if (result.length() - dotIndex > 3) {
         String baseStr = String.valueOf(d + 0.0005);
         result = baseStr.substring(0, dotIndex + 4);
       }
     }
     
   }
   
   return result;
 }
 
 
 /**
  * Has the referenced project been parsed?
  */
 private boolean hasParsedSource() {
   
   if (_referencedProject != null) {
   
     DataElement parsedProject = ((DataElement)(_referencedProject.getAssociated("Parse Reference").get(0))).dereference();
     DataElement parsedFiles = _dataStore.find(parsedProject, DE.A_NAME, "Parsed Files", 2);
     if (parsedFiles != null) {
      
      ArrayList files = parsedFiles.getAssociated("contents");
      
      if (files.size() > 0)
       return true;
     }
   }
   
   return false;
 }
 
 
 /**
  * Find out the source location for a trace function
  */
 public void getSourceLocation(DataElement traceFuncElement) {
   
   // System.out.println("Calling getSourceLocation");
   
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
   
   DataElement traceFunctionsRoot = _dataStore.find(traceElement, DE.A_VALUE, getLocalizedString("pa.TraceFuncRoot"));
   DataElement callTreeRoot 	  = _dataStore.find(traceElement, DE.A_TYPE,  getLocalizedString("pa.CallRoot"));
   DataElement attributesRoot 	  = _dataStore.find(traceElement, DE.A_NAME,  getLocalizedString("pa.AttributesRoot"));
   DataElement callArcsRoot 	  = _dataStore.find(traceElement, DE.A_NAME,  getLocalizedString("pa.CallArcsRoot"));
   
   _dataStore.deleteObjects(traceFunctionsRoot);
   _dataStore.deleteObjects(callTreeRoot);
   _dataStore.deleteObject(traceElement, attributesRoot);
   _dataStore.deleteObject(traceElement, callArcsRoot);
   
   _dataStore.refresh(traceElement, false);
 }
   
 
 /**
  * Populate the datastore using the information from the PA trace file.
  */
 public void populateDataStore(DataElement fileElement, PATraceFile traceFile) {
   
   // Clean the old trace information if we are re-parsing a trace target
   if (_dataStore.find(fileElement, DE.A_NAME, getLocalizedString("pa.AttributesRoot")) != null) {
     cleanTraceInformation(fileElement);
   }
   
   setTraceFunctionFormat(traceFile);
   
   // Detect whether the project has been parsed
   _hasParsedSource = hasParsedSource();
   
   // Find the trace functions root and call tee root  
   _traceFunctionsRoot = _dataStore.find(fileElement, DE.A_VALUE, getLocalizedString("pa.TraceFuncRoot"), 1);
   _callTreeRoot   = _dataStore.find(fileElement, DE.A_TYPE, getLocalizedString("pa.CallRoot"), 1);
   
   // Create the attributes root and call arcs root 
   _attributesRoot = _dataStore.createObject(fileElement, getLocalizedString("pa.data"), getLocalizedString("pa.AttributesRoot"));
   _callArcsRoot   = _dataStore.createObject(fileElement, getLocalizedString("pa.data"), getLocalizedString("pa.CallArcsRoot"));

   // Set the trace file attributes
   createTraceFileAttributes(fileElement, traceFile);
      
   // Process each trace function
   Iterator it = traceFile.getTraceFunctions().iterator();
   
   int i = 0;
   while (it.hasNext()) {
    PATraceFunction trcFunc = (PATraceFunction)it.next();
    DataElement funcElement = findOrCreateTraceFunctionElement(trcFunc);
    createCallNestingRelations(trcFunc, funcElement);
        
    // Create a "calls" reference between the call root and the top level
    // trace functions.
    if (trcFunc.isTopLevelFunction()) {
     _dataStore.createReference(_callTreeRoot, funcElement, _callsD);
    }
    
   }
   
   _dataStore.refresh(_attributesRoot, false);

 }
 
 
 /**
  * Find or create a data element for a PATraceFunction
  */
 private DataElement findOrCreateTraceFunctionElement(PATraceFunction traceFunction) {
 
   // First try to find the trace function element from the hash map.
   DataElement funcElement = (DataElement)_traceFunctionsMap.get(traceFunction);
   
   // Create a trace function element if it does not already exist.
   if (funcElement == null) {
   
     funcElement = createTraceFunction(traceFunction);
     
     // set the attributes for the trace function     
     createTraceFunctionAttributes(funcElement, traceFunction);
     
     // Store the created trace function into the map
     _traceFunctionsMap.put(traceFunction, funcElement);
   }
   
   return funcElement;
 }
 
 
  /**
   * Create a DataElement for a trace function object in the datastore
   */
  private DataElement createTraceFunction(PATraceFunction traceFunction) {
  
   String type = _traceFunctionFormat;
   if (traceFunction.isCyclic())
    type = _cyclicTraceFunctionFormat;
   
   DataElement traceFuncElement = _dataStore.createObject(_traceFunctionsRoot, type, traceFunction.getName());
   
   // Get the source location from the parser if we have parsed source information.
   if (_hasParsedSource) {
     getSourceLocation(traceFuncElement);
   }
   
   return traceFuncElement;
  }

 
 /**
  * create an attribute for a DataElement
  */
 private void createAttribute(DataElement parent, String name, String value) {
 
  DataElement anAttribute = _dataStore.createObject(_attributesRoot, name, value);
  _dataStore.createReference(parent, anAttribute, _attributesD);
 }
 
 
 /**
  * create a float number attribute for a DataElement.
  * We only create the DataElement when the float number is not zero.
  */
 private void createFloatAttribute(DataElement parent, String name, double value) {
 
   if (value > 0)
    createAttribute(parent, name, String.valueOf(value));
    
 }
 
 /**
  * create a float rounded attribute for a DataElement
  */
 private void createFloatAttribute(DataElement parent, String name, double value, boolean round) {
 
   if (value > 0) {
    
     if (round)
       createAttribute(parent, name, roundDouble(value));
     else
       createAttribute(parent, name, String.valueOf(value));
   }
    
 }
 
 
 /**
  * Create attributes for a trace file element
  */
 private void createTraceFileAttributes(DataElement fileElement, PATraceFile traceFile) {
 
  // Create common trace file attributes
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
  * Create attributes for a trace function element
  */
 private void createTraceFunctionAttributes(DataElement funcElement, PATraceFunction trcFunc) {
 
  if (trcFunc instanceof FunctionCheckTraceFunction)
   createFunctionCheckTraceFunctionAttributes(funcElement, (FunctionCheckTraceFunction)trcFunc);
  else 
   createGprofTraceFunctionAttributes(funcElement, trcFunc);
 }
 
 
 /**
  * Create attributes for a gprof trace function
  */
 private void createGprofTraceFunctionAttributes(DataElement funcElement, PATraceFunction trcFunc) {

  createFloatAttribute(funcElement, _timePercentD,    trcFunc.getTotalPercentage());
  createAttribute(funcElement, 		_numCallsD, 	  String.valueOf(trcFunc.getCallNumber()));
  createFloatAttribute(funcElement, _selfTimeD, 	  trcFunc.getSelfSeconds());
  createFloatAttribute(funcElement, _totalTimeD, 	  trcFunc.getTotalSeconds(), true);  
  createFloatAttribute(funcElement, _selfTimeString,  trcFunc.getSelfTimePerCall());
  createFloatAttribute(funcElement, _totalTimeString, trcFunc.getTotalTimePerCall());
 
 }
 
 
 /**
  * Create attributes for a functioncheck trace function
  */
 private void createFunctionCheckTraceFunctionAttributes(DataElement funcElement, FunctionCheckTraceFunction trcFunc) {

  createFloatAttribute(funcElement, _timePercentD,    trcFunc.getTotalPercentage());
  createAttribute(funcElement, 		_numCallsD, 	  String.valueOf(trcFunc.getCallNumber()));
  createFloatAttribute(funcElement, _selfTimeD, 	  trcFunc.getSelfSeconds());
  createFloatAttribute(funcElement, _totalTimeD, 	  trcFunc.getTotalSeconds());  
  createFloatAttribute(funcElement, _selfMsPerCallD,  trcFunc.getSelfTimePerCall(),  true);  
  createFloatAttribute(funcElement, _totalMsPerCallD, trcFunc.getTotalTimePerCall(), true);
  createFloatAttribute(funcElement, _minSelfTimeD,    trcFunc.getMinLocalTime());
  createFloatAttribute(funcElement, _maxSelfTimeD,    trcFunc.getMaxLocalTime());
  createFloatAttribute(funcElement, _minTotalTimeD,   trcFunc.getMinTotalTime()); 
  createFloatAttribute(funcElement, _maxTotalTimeD,   trcFunc.getMaxTotalTime());
  
 }


 /**
  * Create the call nesting relations for a trace function
  */
 private void createCallNestingRelations(PATraceFunction traceFunction, DataElement funcElement) 
 {
 
  ArrayList callees = traceFunction.getCallees();
  
  int numCallees = callees.size();
  for (int i=0; i < numCallees; i++) {
   
   PACallArc callArc = (PACallArc)callees.get(i);
   PATraceFunction callee = callArc.getCallee();
   DataElement calleeElement = findOrCreateTraceFunctionElement(callee);
      
   // create caller/callee relations
   _dataStore.createReference(funcElement, calleeElement, _callsD);
   _dataStore.createReference(calleeElement, funcElement, _calledByD);
   
   // create a DataElement to represent the call arc
   DataElement callArcElement    = _dataStore.createObject(_callArcsRoot, _callArcD, traceFunction.getName());
   callArcElement.setAttribute(DE.A_VALUE, callee.getName());
   
   createAttribute(callArcElement, 		_numCallsD, 	String.valueOf(callArc.getCallNumber()));
   createFloatAttribute(callArcElement, _selfTimeD, 	callArc.getSelfTime());
   createFloatAttribute(callArcElement, _childrenTimeD, callArc.getChildrenTime());
   
   // create the relations between trace functions and call arcs
   _dataStore.createReference(funcElement,   callArcElement, _calleeArcD);
   _dataStore.createReference(calleeElement, callArcElement, _callerArcD);
      
  }
    
 }
  
}
