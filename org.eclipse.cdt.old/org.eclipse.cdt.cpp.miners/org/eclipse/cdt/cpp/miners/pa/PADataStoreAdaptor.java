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
 	
 private DataElement _traceElement;
 private DataElement _referencedProject;
 private DataElement _traceFunctionsRoot;
 private DataElement _callTreeRoot;
 private DataElement _attributesRoot;
 private DataElement _callArcsRoot;
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
    createCallNestingRelations(trcFunc);
        
    // Create a "calls" reference between the call root and the top level
    // trace functions.
    if (trcFunc.isTopLevelFunction()) {
     _dataStore.createReference(_callTreeRoot, funcElement, getLocalizedString("pa.Calls"));
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
  
   String type = getTraceFunctionFormat(traceFunction);
   
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
  _dataStore.createReference(parent, anAttribute, getLocalizedString("pa.Attributes"));
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

  createAttribute(funcElement, getLocalizedString("pa.TimePercentage"),   String.valueOf(trcFunc.getTotalPercentage()));
  createAttribute(funcElement, getLocalizedString("pa.numCalls"), 		  String.valueOf(trcFunc.getCallNumber()));
  createAttribute(funcElement, getLocalizedString("pa.SelfTime"), 		  String.valueOf(trcFunc.getSelfSeconds()));
  createAttribute(funcElement, getLocalizedString("pa.TotalTime"), 	      roundDouble(trcFunc.getTotalSeconds()));  
  createAttribute(funcElement, getLocalizedString("pa.SelfTimePerCall"),  String.valueOf(trcFunc.getSelfMsPerCall()));
  createAttribute(funcElement, getLocalizedString("pa.TotalTimePerCall"), String.valueOf(trcFunc.getTotalMsPerCall()));
 
 }
 
 
 /**
  * Create attributes for a functioncheck trace function
  */
 private void createFunctionCheckTraceFunctionAttributes(DataElement funcElement, FunctionCheckTraceFunction trcFunc) {

  createAttribute(funcElement, getLocalizedString("pa.TimePercentage"),   String.valueOf(trcFunc.getTotalPercentage()));
  createAttribute(funcElement, getLocalizedString("pa.numCalls"), 		  String.valueOf(trcFunc.getCallNumber()));
  createAttribute(funcElement, getLocalizedString("pa.SelfTime"), 		  String.valueOf(trcFunc.getSelfSeconds()));
  createAttribute(funcElement, getLocalizedString("pa.TotalTime"), 	      String.valueOf(trcFunc.getTotalSeconds()));  
  createAttribute(funcElement, getLocalizedString("pa.SelfTimePerCall"),  roundDouble(trcFunc.getSelfMsPerCall()));  
  createAttribute(funcElement, getLocalizedString("pa.TotalTimePerCall"), roundDouble(trcFunc.getTotalMsPerCall()));
  createAttribute(funcElement, getLocalizedString("pa.MinSelfTime"),  	  String.valueOf(trcFunc.getMinLocalTime()));
  createAttribute(funcElement, getLocalizedString("pa.MaxSelfTime"),  	  String.valueOf(trcFunc.getMaxLocalTime()));
  createAttribute(funcElement, getLocalizedString("pa.MinTotalTime"), 	  String.valueOf(trcFunc.getMinTotalTime())); 
  createAttribute(funcElement, getLocalizedString("pa.MaxTotalTime"), 	  String.valueOf(trcFunc.getMaxTotalTime()));
  
 }


 /**
  * Create the call nesting relations for a trace function
  */
 private void createCallNestingRelations(PATraceFunction traceFunction) {
 
  DataElement funcElement = findOrCreateTraceFunctionElement(traceFunction);
  
  ArrayList callees = traceFunction.getCallees();
  
  int numCallees = callees.size();
  for (int i=0; i < numCallees; i++) {
   
   PACallArc callArc = (PACallArc)callees.get(i);
   PATraceFunction callee = callArc.getCallee();
   DataElement calleeElement = findOrCreateTraceFunctionElement(callee);
   
   if (calleeElement != null) {
   
     // create caller/callee relations
     _dataStore.createReference(funcElement, calleeElement, getLocalizedString("pa.Calls"));
     _dataStore.createReference(calleeElement, funcElement, getLocalizedString("pa.CalledBy"));
   
     // create a caller arc and a callee arc
     DataElement calleeArc    = _dataStore.createObject(_callArcsRoot, getLocalizedString("pa.CallArc"), callee.getName());
     DataElement callerArc    = _dataStore.createObject(_callArcsRoot, getLocalizedString("pa.CallArc"), traceFunction.getName());
   
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
  
}
