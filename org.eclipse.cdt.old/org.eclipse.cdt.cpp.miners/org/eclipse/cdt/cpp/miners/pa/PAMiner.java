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
import org.eclipse.cdt.dstore.core.miners.miner.*;
import org.eclipse.cdt.cpp.miners.pa.engine.*;


public class PAMiner extends Miner { 
 
 private DataElement _callArcD;
 private DataElement _callerArcD;
 private DataElement _calleeArcD;
 
 
 // Constructor
 public PAMiner ()
 {
  super();
 }

 public void finish()
 {
  super.finish();
 }

 protected ArrayList getDependencies()
 {
   ArrayList dependencies = new ArrayList();
   dependencies.add("org.eclipse.cdt.dstore.miners.filesystem.FileSystemMiner");
   return dependencies;
 }
 
 public void load()
 {
  // System.out.println("Calling PAMiner.load");
  PADataStoreAdaptor.setMiner(this);
  
  DataElement paRoot = _dataStore.findMinerInformation("org.eclipse.cdt.cpp.miners.pa.PAMiner");

  _dataStore.createObject(paRoot, getLocalizedString("model.data"), "projects root");
  
  _dataStore.createObject(paRoot, getLocalizedString("pa.TraceFile"), "All Trace Files");
  
 }

 // Return the datastore
 public DataStore getDataStore() {
   return _dataStore;
 }
 
 // Extend the schema
 public void extendSchema(DataElement schemaRoot)
 {
 
  // Find the descriptors we are going to use.
  DataElement containerD          = findDescriptor("Container Object");
  DataElement attributesD         = findDescriptor("attributes");
  DataElement integerD			  = findDescriptor("Integer");
  DataElement floatD			  = findDescriptor("Float");
  
  DataElement traceProjectD       = createDerivativeDescriptor(containerD, getLocalizedString("pa.TraceProject"));
  
  // Attributes for trace objects
  //
  // Common trace file attributes (trace format, total execution time, number of trace functions and 
  // number of call graph entries).
  DataElement traceFormatD       = createObjectDescriptor(schemaRoot, getLocalizedString("pa.TraceFormat"));
  DataElement fileTotalTimeD     = createObjectDescriptor(schemaRoot, getLocalizedString("pa.totalExecutionTime"));
  DataElement numTraceFunctionsD = createObjectDescriptor(schemaRoot, getLocalizedString("pa.numTraceFunctions"));
  DataElement numCallEntriesD    = createObjectDescriptor(schemaRoot, getLocalizedString("pa.numCallGraphEntries"));
  
  // Set the types of the attributes (integer or float).
  createReference(fileTotalTimeD, 		floatD, 	attributesD);
  createReference(numTraceFunctionsD, 	integerD, 	attributesD);
  createReference(numCallEntriesD, 		integerD, 	attributesD);
  
  // Additional attribute for gprof trace files (sampling rate).
  DataElement samplingRateD      = createObjectDescriptor(schemaRoot, getLocalizedString("pa.SamplingRate"));
  createReference(samplingRateD, floatD, attributesD);
  
  // Additional attributes for functioncheck trace files (time mode, profile mode, process id and program name).
  DataElement timeModeD          = createObjectDescriptor(schemaRoot, getLocalizedString("pa.TimeMode"));
  DataElement profileModeD       = createObjectDescriptor(schemaRoot, getLocalizedString("pa.ProfileMode"));
  DataElement processIdD         = createObjectDescriptor(schemaRoot, getLocalizedString("pa.ProcessId"));
  DataElement programNameD       = createObjectDescriptor(schemaRoot, getLocalizedString("pa.ProgramName"));
  
  // Common trace function attributes (%time, #calls, total time, self time, children time, 
  // total time/call and self time/call).
  DataElement timePercentD   = createObjectDescriptor(schemaRoot, getLocalizedString("pa.TimePercentage"));  
  DataElement numCallsD      = createObjectDescriptor(schemaRoot, getLocalizedString("pa.numCalls"));
  DataElement selfTimeD      = createObjectDescriptor(schemaRoot, getLocalizedString("pa.SelfTime"));
  DataElement totalTimeD     = createObjectDescriptor(schemaRoot, getLocalizedString("pa.TotalTime"));
  DataElement childrenTimeD  = createObjectDescriptor(schemaRoot, getLocalizedString("pa.ChildrenTime"));
  
  // For gprof, the unit for self and total time per call can be ms, us, ts or ps.
  // We create different descriptors for different time units.
  DataElement selfMsPerCallD = createObjectDescriptor(schemaRoot, getLocalizedString("pa.SelfMsPerCall"));
  DataElement selfUsPerCallD = createObjectDescriptor(schemaRoot, getLocalizedString("pa.SelfUsPerCall"));
  DataElement selfTsPerCallD = createObjectDescriptor(schemaRoot, getLocalizedString("pa.SelfTsPerCall"));
  DataElement selfPsPerCallD = createObjectDescriptor(schemaRoot, getLocalizedString("pa.SelfPsPerCall"));
  
  DataElement totalMsPerCallD  = createObjectDescriptor(schemaRoot, getLocalizedString("pa.TotalMsPerCall"));
  DataElement totalUsPerCallD  = createObjectDescriptor(schemaRoot, getLocalizedString("pa.TotalUsPerCall"));
  DataElement totalTsPerCallD  = createObjectDescriptor(schemaRoot, getLocalizedString("pa.TotalTsPerCall"));
  DataElement totalPsPerCallD  = createObjectDescriptor(schemaRoot, getLocalizedString("pa.TotalPsPerCall"));
  

  // set the types of the attributes
  createReference(timePercentD,  floatD,   attributesD);
  createReference(numCallsD, 	 integerD, attributesD);
  createReference(selfTimeD, 	 floatD,   attributesD);
  createReference(totalTimeD, 	 floatD,   attributesD);
  createReference(childrenTimeD, floatD,   attributesD);
    
  createReference(selfMsPerCallD,  floatD,   attributesD);
  createReference(selfUsPerCallD,  floatD,   attributesD);
  createReference(selfTsPerCallD,  floatD,   attributesD);
  createReference(selfPsPerCallD,  floatD,   attributesD);

  createReference(totalMsPerCallD, floatD,   attributesD);
  createReference(totalUsPerCallD, floatD,   attributesD);
  createReference(totalTsPerCallD, floatD,   attributesD);
  createReference(totalPsPerCallD, floatD,   attributesD);
  
  // Additional attributes for functioncheck trace functions
  DataElement minTotalTimeD  = createObjectDescriptor(schemaRoot, getLocalizedString("pa.MinTotalTime"));
  DataElement maxTotalTimeD  = createObjectDescriptor(schemaRoot, getLocalizedString("pa.MaxTotalTime"));
  DataElement minSelfTimeD   = createObjectDescriptor(schemaRoot, getLocalizedString("pa.MinSelfTime"));
  DataElement maxSelfTimeD   = createObjectDescriptor(schemaRoot, getLocalizedString("pa.MaxSelfTime"));
  
  // set the types of the attributes
  createReference(minTotalTimeD, floatD,   attributesD);
  createReference(maxTotalTimeD, floatD,   attributesD);
  createReference(minSelfTimeD,  floatD,   attributesD);
  createReference(maxSelfTimeD,  floatD,   attributesD);
  
  // Abstract descriptors for trace targets
  
  DataElement traceTargetD   = createAbstractDerivativeDescriptor(containerD, getLocalizedString("pa.TraceTarget"));

  // set the attributes for all trace files
  createReference(traceTargetD, traceFormatD, 		attributesD);
  createReference(traceTargetD, fileTotalTimeD, 	attributesD);
  createReference(traceTargetD, numTraceFunctionsD, attributesD);
  createReference(traceTargetD, numCallEntriesD, 	attributesD);
  
  
  // Create intermediate  object descriptors.
  // The intermediate descriptors are used to model the common characteristics of the more concrete 
  // descriptors. 
  // 
  // Descriptor hierarchy:
  //
  // TraceProgramD --> gprofTraceProgramD  <--- gprofTraceTargetD
  //              |--> fcTraceProgramD     <-----|-----
  //                                             |    |
  // TraceFileD    --> gprofTraceFileD  <--------     |
  //              |--> fcTraceFileD     <------- fcTraceTargetD
  //
  
  DataElement traceProgramD      = createAbstractDerivativeDescriptor(traceTargetD, getLocalizedString("pa.TraceProgram"));
  DataElement traceFileD         = createAbstractDerivativeDescriptor(traceTargetD, getLocalizedString("pa.TraceFile"));
  DataElement gprofTraceTargetD  = createAbstractDerivativeDescriptor(traceTargetD, getLocalizedString("pa.gprofTraceTarget"));
  DataElement fcTraceTargetD     = createAbstractDerivativeDescriptor(traceTargetD, getLocalizedString("pa.fcTraceTarget"));

  // set additional attributes for gprof trace targets
  createReference(gprofTraceTargetD, samplingRateD, attributesD);
  
  // set additional attributes for functioncheck trace targets
  createReference(fcTraceTargetD, timeModeD, 	attributesD);
  createReference(fcTraceTargetD, profileModeD, attributesD);
  createReference(fcTraceTargetD, processIdD, 	attributesD);
  createReference(fcTraceTargetD, programNameD, attributesD);
   
  // Concrete descriptors for trace files.
  // gprofTraceFileD inherits from both gprofTraceTargetD and traceFileD.
  DataElement gprofTraceFileD = createDerivativeDescriptor(gprofTraceTargetD, getLocalizedString("pa.gprofTraceFile"));
  DataElement fcTraceFileD    = createDerivativeDescriptor(fcTraceTargetD,    getLocalizedString("pa.fcTraceFile"));
  createAbstractRelationship(traceFileD, gprofTraceFileD);
  createAbstractRelationship(traceFileD, fcTraceFileD);
  
  // Derivative descriptors from gprof trace file for different time units.
  DataElement gprofTraceFile_msD = createDerivativeDescriptor(gprofTraceFileD, getLocalizedString("pa.gprofTraceFile_ms"));
  DataElement gprofTraceFile_usD = createDerivativeDescriptor(gprofTraceFileD, getLocalizedString("pa.gprofTraceFile_us"));
  DataElement gprofTraceFile_tsD = createDerivativeDescriptor(gprofTraceFileD, getLocalizedString("pa.gprofTraceFile_ts"));
  DataElement gprofTraceFile_psD = createDerivativeDescriptor(gprofTraceFileD, getLocalizedString("pa.gprofTraceFile_ps"));
  
  // Concrete descriptors for trace programs
  // gprofTraceProgramD inherits from both gprofTraceTargetD and traceProgramD.
  DataElement gprofTraceProgramD   = createDerivativeDescriptor(gprofTraceTargetD, getLocalizedString("pa.gprofTraceProgram"));
  DataElement fcTraceProgramD      = createDerivativeDescriptor(fcTraceTargetD,    getLocalizedString("pa.fcTraceProgram"));
  createAbstractRelationship(traceProgramD, gprofTraceProgramD);
  createAbstractRelationship(traceProgramD, fcTraceProgramD); 

  // Derivative descriptors from gprof trace program for different time units.
  DataElement gprofTraceProgram_msD = createDerivativeDescriptor(gprofTraceProgramD, getLocalizedString("pa.gprofTraceProgram_ms"));
  DataElement gprofTraceProgram_usD = createDerivativeDescriptor(gprofTraceProgramD, getLocalizedString("pa.gprofTraceProgram_us"));
  DataElement gprofTraceProgram_tsD = createDerivativeDescriptor(gprofTraceProgramD, getLocalizedString("pa.gprofTraceProgram_ts"));
  DataElement gprofTraceProgram_psD = createDerivativeDescriptor(gprofTraceProgramD, getLocalizedString("pa.gprofTraceProgram_ps"));

  // Descriptors for call arcs
  _callArcD             = createObjectDescriptor(schemaRoot, getLocalizedString("pa.CallArc"));

  // set the attributes for call arcs (#calls, self time and children time).
  createReference(_callArcD, numCallsD, 	attributesD);
  createReference(_callArcD, selfTimeD, 	attributesD);
  createReference(_callArcD, childrenTimeD, attributesD);

  
  // Abstract descriptor for trace functions
  DataElement traceFunctionD       = createAbstractObjectDescriptor(schemaRoot, getLocalizedString("pa.TraceFunction"));
  
  // Relation Descriptors
  DataElement callsD             = createRelationDescriptor(traceFunctionD, getLocalizedString("pa.Calls"));
  DataElement calledByD          = createRelationDescriptor(traceFunctionD, getLocalizedString("pa.CalledBy"));
  DataElement referencedFileD    = createRelationDescriptor(traceTargetD,   getLocalizedString("pa.ReferencedFile"));
  DataElement referencedProjectD = createRelationDescriptor(traceTargetD,   getLocalizedString("pa.ReferencedProject"));
 
  _callerArcD  = createRelationDescriptor(traceFunctionD, getLocalizedString("pa.CallerArc"));
  _calleeArcD  = createRelationDescriptor(traceFunctionD, getLocalizedString("pa.CalleeArc"));

  // Descriptor for gprof trace functions
  DataElement gprofTraceFunctionD  = createDerivativeDescriptor(traceFunctionD, getLocalizedString("pa.gprofTraceFunction"));

  // set the attributes for gprof trace functions
  createReference(gprofTraceFunctionD, timePercentD,  attributesD);  
  createReference(gprofTraceFunctionD, numCallsD, 	  attributesD);
  createReference(gprofTraceFunctionD, selfTimeD, 	  attributesD);
  createReference(gprofTraceFunctionD, totalTimeD, 	  attributesD);

  // Derivative descriptors from gprof trace function for different time units.
  DataElement gprofTraceFunction_msD  = createDerivativeFunctionDescriptor(gprofTraceFunctionD, getLocalizedString("pa.gprofTraceFunction_ms"));
  DataElement gprofTraceFunction_usD  = createDerivativeFunctionDescriptor(gprofTraceFunctionD, getLocalizedString("pa.gprofTraceFunction_us"));
  DataElement gprofTraceFunction_tsD  = createDerivativeFunctionDescriptor(gprofTraceFunctionD, getLocalizedString("pa.gprofTraceFunction_ts"));
  DataElement gprofTraceFunction_psD  = createDerivativeFunctionDescriptor(gprofTraceFunctionD, getLocalizedString("pa.gprofTraceFunction_ps"));

  // Create attributes for self time/call and total time/call.
  createReference(gprofTraceFunction_msD, selfMsPerCallD,  attributesD);
  createReference(gprofTraceFunction_usD, selfUsPerCallD,  attributesD);
  createReference(gprofTraceFunction_tsD, selfTsPerCallD,  attributesD);
  createReference(gprofTraceFunction_psD, selfPsPerCallD,  attributesD);
  
  createReference(gprofTraceFunction_msD, totalMsPerCallD,  attributesD);
  createReference(gprofTraceFunction_usD, totalUsPerCallD,  attributesD);
  createReference(gprofTraceFunction_tsD, totalTsPerCallD,  attributesD);
  createReference(gprofTraceFunction_psD, totalPsPerCallD,  attributesD);

  
  // Descriptor for gprof cyclic trace functions
  DataElement gprofCyclicTrcFuncD  = createDerivativeDescriptor(gprofTraceFunctionD, getLocalizedString("pa.gprofCyclicTraceFunction"));

  // Derivative descriptors for gprof cyclic trace functions
  DataElement gprofCyclicTrcFunc_msD = createDerivativeFunctionDescriptor(gprofTraceFunction_msD, getLocalizedString("pa.gprofCyclicTraceFunction_ms"));
  DataElement gprofCyclicTrcFunc_usD = createDerivativeFunctionDescriptor(gprofTraceFunction_usD, getLocalizedString("pa.gprofCyclicTraceFunction_us"));
  DataElement gprofCyclicTrcFunc_tsD = createDerivativeFunctionDescriptor(gprofTraceFunction_tsD, getLocalizedString("pa.gprofCyclicTraceFunction_ts"));
  DataElement gprofCyclicTrcFunc_psD = createDerivativeFunctionDescriptor(gprofTraceFunction_psD, getLocalizedString("pa.gprofCyclicTraceFunction_ps"));
  
  createAbstractRelationship(gprofCyclicTrcFuncD, gprofCyclicTrcFunc_msD);
  createAbstractRelationship(gprofCyclicTrcFuncD, gprofCyclicTrcFunc_usD);
  createAbstractRelationship(gprofCyclicTrcFuncD, gprofCyclicTrcFunc_tsD);
  createAbstractRelationship(gprofCyclicTrcFuncD, gprofCyclicTrcFunc_psD);
  
  // Descriptor for functioncheck trace functions
  DataElement fcTraceFunctionD     = createDerivativeFunctionDescriptor(traceFunctionD, getLocalizedString("pa.fcTraceFunction"));
  
  // set the attributes for functioncheck trace functions
  createReference(fcTraceFunctionD, timePercentD,  attributesD);  
  createReference(fcTraceFunctionD, numCallsD, 	   attributesD);
  createReference(fcTraceFunctionD, selfTimeD, 	   attributesD);
  createReference(fcTraceFunctionD, totalTimeD,    attributesD);
  createReference(fcTraceFunctionD, selfMsPerCallD,  attributesD);
  createReference(fcTraceFunctionD, totalMsPerCallD, attributesD);
  createReference(fcTraceFunctionD, minSelfTimeD,  attributesD);
  createReference(fcTraceFunctionD, maxSelfTimeD,  attributesD);
  createReference(fcTraceFunctionD, minTotalTimeD, attributesD);
  createReference(fcTraceFunctionD, maxTotalTimeD, attributesD);
  
  // Descriptor for functioncheck cyclic trace functions
  DataElement fcCyclicTrcFuncD     = createDerivativeFunctionDescriptor(fcTraceFunctionD, getLocalizedString("pa.fcCyclicTraceFunction"));
        
  createRelationDescriptor(gprofCyclicTrcFuncD, getLocalizedString("pa.Cycles"));
  createRelationDescriptor(fcCyclicTrcFuncD, getLocalizedString("pa.Cycles"));
  
  // make the caller/callee arc relations invisible
  _callerArcD.setDepth(0);
  _calleeArcD.setDepth(0);
  referencedFileD.setDepth(0);
  referencedProjectD.setDepth(0);
  
  callsD.setDepth(200);
  calledByD.setDepth(100);
  
  // Set up the relation between trace targets and trace functions.
  // This models the fact that a gprof trace file or program can contain 
  // a list of gprof trace functions.
  createReference(fcTraceTargetD, fcTraceFunctionD);
  
  createReference(gprofTraceFile_msD, gprofTraceFunction_msD);
  createReference(gprofTraceFile_usD, gprofTraceFunction_usD);
  createReference(gprofTraceFile_tsD, gprofTraceFunction_tsD);
  createReference(gprofTraceFile_psD, gprofTraceFunction_psD);

  createReference(gprofTraceProgram_msD, gprofTraceFunction_msD);
  createReference(gprofTraceProgram_usD, gprofTraceFunction_usD);
  createReference(gprofTraceProgram_tsD, gprofTraceFunction_tsD);
  createReference(gprofTraceProgram_psD, gprofTraceFunction_psD);
  
  // set up the calls and called by relations
  createReference(gprofTraceFunctionD,  gprofTraceFunctionD, callsD);
  createReference(gprofTraceFunctionD,  gprofTraceFunctionD, calledByD);
  createReference(fcTraceFunctionD, 	fcTraceFunctionD, 	 callsD);
  createReference(fcTraceFunctionD, 	fcTraceFunctionD, 	 calledByD);
    
  // Descriptor for call root
  DataElement callRootD = createObjectDescriptor(schemaRoot, getLocalizedString("pa.CallRoot"));
  createReference(callRootD, callsD);
  
  // Find the descriptors for file and executable.
  DataElement fileD = findDescriptor("file");
  DataElement executableD = findDescriptor("executable");
  
  // Create command descriptors
  createCommandDescriptor(traceFileD,    getLocalizedString("pa.Parse"), "C_PARSE_TRACE").setDepth(0);
  createCommandDescriptor(traceProgramD, getLocalizedString("pa.Analyze"), "C_ANALYZE_PROGRAM").setDepth(0);
  createCommandDescriptor(traceTargetD,  getLocalizedString("pa.Remove"), "C_REMOVE_TRACE_TARGET").setDepth(0);
  createCommandDescriptor(fileD, "quey trace format", "C_QUERY_TRACE_FILE_FORMAT").setDepth(0);
  createCommandDescriptor(executableD, "query trace format", "C_QUERY_TRACE_PROGRAM_FORMAT").setDepth(0);
  
  makeTransient(gprofTraceFunctionD);
  makeTransient(fcTraceFunctionD);
    
 }

 // Commmand Handler
 public DataElement handleCommand (DataElement theElement)
 {

  String name         = getCommandName(theElement);
  DataElement status  = getCommandStatus(theElement);
  DataElement subject = getCommandArgument(theElement, 0);
    
  if (name.equals("C_PARSE_TRACE"))
  {
   handleTraceFileParse(subject, status);
  }
  else if (name.equals("C_ANALYZE_PROGRAM"))
  {
   handleTraceProgramAnalyze(subject, status);
  }
  else if (name.equals("C_QUERY_TRACE_FILE_FORMAT"))
  {
   handleQueryTraceFileFormat(subject, status);
  }
  else if (name.equals("C_QUERY_TRACE_PROGRAM_FORMAT"))
  {
   handleQueryTraceProgramFormat(subject, status);
  }
  else if (name.equals("C_REMOVE_TRACE_TARGET"))
  {
   handleRemoveTraceTarget(subject, status);
  }
  
  return status;
   
 }
 
 
 /**
  * Remove the given trace target
  */
 public void handleRemoveTraceTarget(DataElement traceTarget, DataElement status) {
 
   _dataStore.deleteObject(traceTarget.getParent(), traceTarget);
   status.setAttribute(DE.A_NAME, "done");
 }
 
 
 /**
  * Query the trace file format
  */
 public void handleQueryTraceFileFormat(DataElement fileElement, DataElement status) {
 
   File file = fileElement.getFileObject(false);
   int traceFormat = -1;
   try {
    traceFormat = PAAdaptor.queryTraceFileFormat(file);
   }
   catch (Exception e) { }
   
   String formatStr = null;
   switch (traceFormat) {
   
     case PAAdaptor.GPROF_GNU:
      formatStr = "gprof_gnu";
      break;
      
     case PAAdaptor.GPROF_BSD:
      formatStr = "gprof_bsd";
      break;
     
     case PAAdaptor.FUNCTIONCHECK:
      formatStr = "functioncheck";
      break;
     
     default:
      formatStr = "invalid trace file";
      break;
     
   }
   
   status.setAttribute(DE.A_NAME, "done");
   status.setAttribute(DE.A_VALUE, formatStr);
   _dataStore.refresh(status, true);
 }
 

 /**
  * Query the trace program format
  */
 public void handleQueryTraceProgramFormat(DataElement fileElement, DataElement status) {
 
   File file = new File(fileElement.getSource());
         
   String formatStr = null;
   String queryCommand = "nm" + " " + file.getName() + "|grep \"mcount\\|cyg_profile_func_enter\"";
   
   String theOS = System.getProperty("os.name");
   if (theOS.toLowerCase().equals("aix"))
     queryCommand = "nm" + " " + file.getName() + "|egrep \"mcount|cyg_profile_func_enter\"";
      
   DataElement cmdStatus = PADataStoreAdaptor.runCommand(fileElement.getParent(), queryCommand);
   
   QueryTraceFormatThread queryThread = new QueryTraceFormatThread();
   queryThread.init(cmdStatus, status);
   queryThread.start();
   
 }
 

 /**
  * Parse the given trace file and use the parsed result to populate the datastore 
  */
 public void handleTraceFileParse(DataElement traceElement, DataElement status) {
   
   // System.out.println("parsing " + traceElement);
   
   ArrayList references = traceElement.getAssociated(getLocalizedString("pa.ReferencedFile"));
   DataElement fileElement = (DataElement)references.get(0);
   
   File file = fileElement.getFileObject(false);
   
   // System.out.println("file: " + file.getAbsolutePath());
   
   PADataStoreAdaptor adaptor = new PADataStoreAdaptor(traceElement);
   
   ArrayList formats = traceElement.getAssociated(getLocalizedString("pa.TraceFormat"));
   String traceFormat = ((DataElement)formats.get(0)).getName();
      
   // System.out.println("trace format: " + traceFormat);
   
   PATraceFile traceFile = null;
   try {
    traceFile = PAAdaptor.createTraceFile(file, traceFormat);
   }
   catch (Exception e) {
    e.printStackTrace();
    status.setAttribute(DE.A_NAME, "done");
    status.setAttribute(DE.A_VALUE, "error");
    _dataStore.refresh(status, false);
    return;
   }
   
   // System.out.println("parse done");
   
   // Reset the type of the trace element after the parsing
   traceElement.setAttribute(DE.A_TYPE, PADataStoreAdaptor.getTraceFileFormat(traceFile));
   DataElement traceFunctionsRoot = _dataStore.find(traceElement, DE.A_VALUE, getLocalizedString("pa.TraceFuncRoot"), 1);
   traceFunctionsRoot.setAttribute(DE.A_TYPE, traceElement.getType());
   _dataStore.refresh(traceFunctionsRoot, false);
   
   adaptor.populateDataStore(traceElement, traceFile);
   status.setAttribute(DE.A_NAME, "done");
   _dataStore.refresh(status, false);
 }
 
 /**
  * Run the profiler command and analyze the result
  */
 public void handleTraceProgramAnalyze(DataElement traceElement, DataElement status) {
  
  // System.out.println("traceElement: " + traceElement);
  
  ArrayList formats = traceElement.getAssociated(getLocalizedString("pa.TraceFormat"));
  String traceFormat = ((DataElement)formats.get(0)).getName();
  
  // System.out.println("trace format: " + traceFormat);
  
  // Create profile command according to the trace format
  String profileCommand = null;
  if (traceFormat != null && traceFormat.indexOf("gprof") >= 0) {
   profileCommand = "gprof -b";
  }
  else if (traceFormat != null && traceFormat.equals("functioncheck")) {
   profileCommand = "fcdump -demangle-params";
  }
  else {
   System.out.println("Invalid trace format: " + traceFormat);
   status.setAttribute(DE.A_NAME, "done");
   status.setAttribute(DE.A_VALUE, "error");
   _dataStore.refresh(status, false);
   return;
  }
  
  // Test whether the trace program exists
  File file = new File(traceElement.getSource());
  if (!file.exists()) {
   System.out.println("Trace program does not exist: " + traceElement.getSource());
   status.setAttribute(DE.A_NAME, "done");
   status.setAttribute(DE.A_VALUE, "error");
   _dataStore.refresh(status, false);
   return;   
  }
  
  profileCommand = profileCommand + " " + file.getName();
  
  // System.out.println("running profile command: " + profileCommand);
  // System.out.println("current dir: " + file.getParentFile().getAbsolutePath());
  
  // Run the profile command
  Process process = null;
  try {
   process = Runtime.getRuntime().exec(profileCommand, null, file.getParentFile());
  }
  catch (Exception e) {
   System.out.println("Error running the profile command: " + profileCommand);
   _dataStore.createObject(traceElement, "error code", getLocalizedString("pa.NoCommand"));
   status.setAttribute(DE.A_NAME, "done");
   status.setAttribute(DE.A_VALUE, "error");
   _dataStore.refresh(status, false);
   return;
  }
  
  PAMinerParseErrorThread errorThread = new PAMinerParseErrorThread(this, traceElement, status, process.getErrorStream());
  PAMinerParseOutputThread outputThread = new PAMinerParseOutputThread(this, traceElement, status, traceFormat, process.getInputStream());
  errorThread.setOutputThread(outputThread);
  outputThread.setErrorThread(errorThread);
  
  errorThread.start();
  outputThread.start();
      
 }
 
 
 /**
  * Create a derivative function descriptor from a given base descriptor.
  * We need to create the callerArc and calleeArc relations for function descriptors.
  */
 private DataElement createDerivativeFunctionDescriptor(DataElement base, String derivedName)
 {
   DataElement descriptor = createDerivativeDescriptor(base, derivedName);
   createReference(descriptor, 	_callArcD, 	_callerArcD);
   createReference(descriptor, 	_callArcD, 	_calleeArcD);
   return descriptor;
 }
 
 
 /**
  * Create a derivative descriptor from a given base descriptor
  */
 private DataElement createDerivativeDescriptor(DataElement base, String derivedName)
 {
  DataElement theDerivative = createObjectDescriptor(base.getDataStore().getDescriptorRoot(), derivedName);
  createAbstractRelationship(base, theDerivative);
  
  DataElement attributesD = findDescriptor(getLocalizedString("pa.Attributes"));
  ArrayList relations = base.getAssociated(attributesD);
  for (int i=0; i < relations.size(); i++) {
   createReference(theDerivative, (DataElement)relations.get(i), attributesD);
  }
  
  return theDerivative;
 }

 /**
  * Create an abstract derivative descriptor from a given base descriptor
  */
 private DataElement createAbstractDerivativeDescriptor(DataElement base, String derivedName)
 {
  DataElement theDerivative = createAbstractObjectDescriptor(base.getDataStore().getDescriptorRoot(), derivedName);
  createAbstractRelationship(base, theDerivative);
  
  DataElement attributesD = findDescriptor(getLocalizedString("pa.Attributes"));
  ArrayList relations = base.getAssociated(attributesD);
  for (int i=0; i < relations.size(); i++) {
   createReference(theDerivative, (DataElement)relations.get(i), attributesD);
  }
   
  return theDerivative;
 }
 
 /**
  * Create a reference between two data elements
  */
 private DataElement createReference(DataElement from, DataElement to, DataElement relationType)
 {
  return from.getDataStore().createReference(from, to, relationType);
 }

 /**
  * Overwrite the getResourceBundle() method in Miner
  */
 public ResourceBundle getResourceBundle()  
 {
  try {
   return ResourceBundle.getBundle(getName()); 
  }
  catch (MissingResourceException mre) {}	  	
  return null;
 }
 
}
