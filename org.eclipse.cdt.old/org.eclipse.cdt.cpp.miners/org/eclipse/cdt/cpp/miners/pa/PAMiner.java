package org.eclipse.cdt.cpp.miners.pa;

import java.util.*;
import java.io.*;

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.core.client.*;
import org.eclipse.cdt.dstore.core.miners.miner.*;

import org.eclipse.cdt.cpp.miners.pa.engine.*;


public class PAMiner extends Miner { 
 
 
 private PADataStoreAdaptor _adaptor;
 
 
 // Constructor
 public PAMiner ()
 {
  super();
 }

 public void finish()
 {
  super.finish();
 }

 public void load()
 {
  // System.out.println("Calling PAMiner.load");
  _adaptor = new PADataStoreAdaptor(this);  
 }

 // Return the datastore
 public DataStore getDataStore() {
   return _dataStore;
 }
 
 // Extend the schema
 public void extendSchema(DataElement schemaRoot)
 {
 
  DataElement containerD          = findDescriptor("Container Object");
  DataElement attributesD         = findDescriptor("attributes");
  
  DataElement traceProjectD       = createDerivativeDescriptor(containerD, getLocalizedString("pa.TraceProject"));
  
  // Attributes for trace objects
  //
  // general trace file attributes
  DataElement traceFormatD       = createObjectDescriptor(schemaRoot, getLocalizedString("pa.TraceFormat"));
  DataElement fileTotalTimeD     = createObjectDescriptor(schemaRoot, getLocalizedString("pa.totalExecutionTime"));
  DataElement numTraceFunctionsD = createObjectDescriptor(schemaRoot, getLocalizedString("pa.numTraceFunctions"));
  DataElement numCallEntriesD    = createObjectDescriptor(schemaRoot, getLocalizedString("pa.numCallGraphEntries"));
  
  // additional attribute for gprof trace files
  DataElement samplingRateD      = createObjectDescriptor(schemaRoot, getLocalizedString("pa.SamplingRate"));
  
  // additional attributes for functioncheck trace files
  DataElement timeModeD          = createObjectDescriptor(schemaRoot, getLocalizedString("pa.TimeMode"));
  DataElement profileModeD       = createObjectDescriptor(schemaRoot, getLocalizedString("pa.ProfileMode"));
  DataElement processIdD         = createObjectDescriptor(schemaRoot, getLocalizedString("pa.ProcessId"));
  DataElement programNameD       = createObjectDescriptor(schemaRoot, getLocalizedString("pa.ProgramName"));
  
  // general trace function attributes
  DataElement totalPercentD  = createObjectDescriptor(schemaRoot, getLocalizedString("pa.TotalPercentage"));
  DataElement numCallsD      = createObjectDescriptor(schemaRoot, getLocalizedString("pa.numCalls"));
  DataElement totalTimeD     = createObjectDescriptor(schemaRoot, getLocalizedString("pa.TotalTime"));
  DataElement selfTimeD      = createObjectDescriptor(schemaRoot, getLocalizedString("pa.SelfTime"));
  DataElement childrenTimeD  = createObjectDescriptor(schemaRoot, getLocalizedString("pa.ChildrenTime"));
  DataElement totalPerCallD  = createObjectDescriptor(schemaRoot, getLocalizedString("pa.TotalTimePerCall"));
  DataElement selfPerCallD   = createObjectDescriptor(schemaRoot, getLocalizedString("pa.SelfTimePerCall"));
  
  // additional attributes for functioncheck trace functions
  DataElement minTotalTimeD  = createObjectDescriptor(schemaRoot, getLocalizedString("pa.MinTotalTime"));
  DataElement maxTotalTimeD  = createObjectDescriptor(schemaRoot, getLocalizedString("pa.MaxTotalTime"));
  DataElement minSelfTimeD   = createObjectDescriptor(schemaRoot, getLocalizedString("pa.MinSelfTime"));
  DataElement maxSelfTimeD   = createObjectDescriptor(schemaRoot, getLocalizedString("pa.MaxSelfTime"));
  
  // Abstract descriptors for trace targets
  
  DataElement traceTargetD   = createAbstractDerivativeDescriptor(containerD, getLocalizedString("pa.TraceTarget"));

  // set up the attributes for all trace files
  createReference(traceTargetD, traceFormatD, 		attributesD);
  createReference(traceTargetD, fileTotalTimeD, 	attributesD);
  createReference(traceTargetD, numTraceFunctionsD, attributesD);
  createReference(traceTargetD, numCallEntriesD, 	attributesD);
  
  // Create object descriptors for trace file and trace program
  DataElement traceProgramD      = createAbstractDerivativeDescriptor(traceTargetD, getLocalizedString("pa.TraceProgram"));
  DataElement traceFileD         = createAbstractDerivativeDescriptor(traceTargetD, getLocalizedString("pa.TraceFile"));
  DataElement gprofTraceTargetD  = createAbstractDerivativeDescriptor(traceTargetD, getLocalizedString("pa.gprofTraceTarget"));
  DataElement fcTraceTargetD     = createAbstractDerivativeDescriptor(traceTargetD, getLocalizedString("pa.fcTraceTarget"));

  // set up additional attributes for gprof trace files
  createReference(gprofTraceTargetD, samplingRateD, attributesD);
  
  // set up additional attributes for functioncheck trace files
  createReference(fcTraceTargetD, timeModeD, 	attributesD);
  createReference(fcTraceTargetD, profileModeD, attributesD);
  createReference(fcTraceTargetD, processIdD, 	attributesD);
  createReference(fcTraceTargetD, programNameD, attributesD);
   
  // Descriptors for trace files
  DataElement gprofTraceFileD = createDerivativeDescriptor(gprofTraceTargetD, getLocalizedString("pa.gprofTraceFile"));
  DataElement fcTraceFileD    = createDerivativeDescriptor(fcTraceTargetD,    getLocalizedString("pa.fcTraceFile"));
  createAbstractRelationship(traceFileD, gprofTraceFileD);
  createAbstractRelationship(traceFileD, fcTraceFileD);
  
  // Descriptors for trace programs
  DataElement gprofTraceProgramD   = createDerivativeDescriptor(gprofTraceTargetD, getLocalizedString("pa.gprofTraceProgram"));
  DataElement fcTraceProgramD      = createDerivativeDescriptor(fcTraceTargetD,    getLocalizedString("pa.fcTraceProgram"));
  createAbstractRelationship(traceProgramD, gprofTraceProgramD);
  createAbstractRelationship(traceProgramD, fcTraceProgramD); 
  
  // Descriptors for trace functions
  DataElement traceFunctionD       = createAbstractObjectDescriptor(schemaRoot, getLocalizedString("pa.TraceFunction"));
  
  // set up the attributes for trace functions
  createReference(traceFunctionD, totalPercentD, attributesD);
  createReference(traceFunctionD, numCallsD, 	 attributesD);
  createReference(traceFunctionD, totalTimeD, 	 attributesD);
  createReference(traceFunctionD, selfTimeD, 	 attributesD);
  createReference(traceFunctionD, totalPerCallD, attributesD);
  createReference(traceFunctionD, selfPerCallD,  attributesD);

  // Descriptors for gprof trace functions
  DataElement gprofTraceFunctionD  = createDerivativeDescriptor(traceFunctionD, 	 getLocalizedString("pa.gprofTraceFunction"));
  DataElement gprofCyclicTrcFuncD  = createDerivativeDescriptor(gprofTraceFunctionD, getLocalizedString("pa.gprofCyclicTraceFunction"));
  
  // Descriptors for functioncheck trace functions
  DataElement fcTraceFunctionD     = createDerivativeDescriptor(traceFunctionD, getLocalizedString("pa.fcTraceFunction"));
  
  // set up the additional attributes for functioncheck trace functions
  createReference(fcTraceFunctionD, minTotalTimeD, attributesD);
  createReference(fcTraceFunctionD, maxTotalTimeD, attributesD);
  createReference(fcTraceFunctionD, minSelfTimeD,  attributesD);
  createReference(fcTraceFunctionD, maxSelfTimeD,  attributesD);
  
  DataElement fcCyclicTrcFuncD     = createDerivativeDescriptor(fcTraceFunctionD, getLocalizedString("pa.fcCyclicTraceFunction"));
  
  // Descriptors for call arcs
  DataElement callArcD             = createObjectDescriptor(schemaRoot, getLocalizedString("pa.CallArc"));

  // set up the attributes for call arcs
  createReference(callArcD, numCallsD, 		attributesD);
  createReference(callArcD, selfTimeD, 		attributesD);
  createReference(callArcD, childrenTimeD,  attributesD);
    
  // Relation Descriptors
  DataElement callsD             = createRelationDescriptor(traceFunctionD, getLocalizedString("pa.Calls"));
  DataElement calledByD          = createRelationDescriptor(traceFunctionD, getLocalizedString("pa.CalledBy"));
  DataElement callerArcD         = createRelationDescriptor(traceFunctionD, getLocalizedString("pa.CallerArc"));
  DataElement calleeArcD         = createRelationDescriptor(traceFunctionD, getLocalizedString("pa.CalleeArc"));
  DataElement referencedFileD    = createRelationDescriptor(traceTargetD,   getLocalizedString("pa.ReferencedFile"));
  DataElement referencedProjectD = createRelationDescriptor(traceTargetD,   getLocalizedString("pa.ReferencedProject"));
  
  createRelationDescriptor(gprofCyclicTrcFuncD, getLocalizedString("pa.Cycles"));
  createRelationDescriptor(fcCyclicTrcFuncD, getLocalizedString("pa.Cycles"));
  
  // make the caller/callee arc relations invisible
  callerArcD.setDepth(0);
  calleeArcD.setDepth(0);
  referencedFileD.setDepth(0);
  referencedProjectD.setDepth(0);
  callsD.setDepth(120);
  calledByD.setDepth(110);
    
  // Set up the relation between trace targets and trace functions
  createReference(gprofTraceTargetD, gprofTraceFunctionD);
  createReference(fcTraceTargetD, fcTraceFunctionD);
  
  // set up the calls and called by relations
  createReference(gprofTraceFunctionD,  gprofTraceFunctionD, callsD);
  createReference(gprofTraceFunctionD,  gprofTraceFunctionD, calledByD);
  createReference(fcTraceFunctionD, 	fcTraceFunctionD, 	 callsD);
  createReference(fcTraceFunctionD, 	fcTraceFunctionD, 	 calledByD);
  createReference(gprofTraceFunctionD,  callArcD, 			 callerArcD);
  createReference(gprofTraceFunctionD,  callArcD, 			 calleeArcD);
  createReference(fcTraceFunctionD, 	callArcD, 			 callerArcD);
  createReference(fcTraceFunctionD, 	callArcD, 			 calleeArcD);
    
  DataElement callRootD = createObjectDescriptor(schemaRoot, getLocalizedString("pa.CallRoot"));
  createReference(callRootD, callsD);
  
  DataElement fileD = findDescriptor("file");
  
  // command descriptors
  createCommandDescriptor(traceFileD,    getLocalizedString("pa.Parse"), "C_PARSE_TRACE").setDepth(0);
  createCommandDescriptor(traceProgramD, getLocalizedString("pa.Analyze"), "C_ANALYZE_PROGRAM").setDepth(0);
  createCommandDescriptor(traceFunctionD, "Query", "C_QUERY");
  createCommandDescriptor(fileD, "quey trace", "C_QUERY_TRACE_FORMAT").setDepth(0);
  
  makeTransient(gprofTraceFunctionD);
  makeTransient(fcTraceFunctionD);
    
 }

 // Commmand Handler
 public DataElement handleCommand (DataElement theElement)
 {

  String name         = getCommandName(theElement);
  DataElement status  = getCommandStatus(theElement);
  DataElement subject = getCommandArgument(theElement, 0);
    
  if (name.equals("C_QUERY") && subject.isOfType(getLocalizedString("pa.TraceFunction")))
  {
   provideSourceFor(subject);
  }
  else if (name.equals("C_PARSE_TRACE"))
  {
   handleTraceFileParse(subject, status);
  }
  else if (name.equals("C_ANALYZE_PROGRAM"))
  {
   handleTraceProgramAnalyze(subject, getCommandArgument(theElement, 1), status);
  }
  else if (name.equals("C_QUERY_TRACE_FORMAT"))
  {
   handleQueryTraceFormat(subject, status);
  }

  return status;
   
 }
 

 /**
  * Find the source location for a trace function
  */
 public void provideSourceFor(DataElement traceFuncElement) {

   if (traceFuncElement.getSource() == null || traceFuncElement.getSource().length() == 0) {
     _adaptor.provideSourceFor(traceFuncElement);
   }
   
 }
 
 /**
  * Query the trace file format
  */
 public void handleQueryTraceFormat(DataElement fileElement, DataElement status) {
 
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
      formatStr = "error";
      break;
     
   }
   
   status.setAttribute(DE.A_NAME, "done");
   status.setAttribute(DE.A_VALUE, formatStr);
 }
 
 
 /**
  * Parse the given trace file and use the parsed result to populate the datastore 
  */
 public void handleTraceFileParse(DataElement traceElement, DataElement status) {
   
   // System.out.println("parsing " + traceElement);
   
   ArrayList references = traceElement.getAssociated(getLocalizedString("pa.ReferencedFile"));
   DataElement fileElement = (DataElement)references.get(0);
   
   File file = fileElement.getFileObject(false);
   
   String traceFormat = _adaptor.getAttribute(traceElement, getLocalizedString("pa.TraceFormat"));
   
   // System.out.println("trace format: " + traceFormat);
   
   PATraceFile traceFile = null;
   try {
    traceFile = PAAdaptor.createTraceFile(file, traceFormat);
   }
   catch (Exception e) {
    e.printStackTrace();
    status.setAttribute(DE.A_NAME, "error");
    return;
   }
   
   traceElement.setAttribute(DE.A_TYPE, _adaptor.getTraceFileFormat(traceFile));
   _adaptor.populateDataStore(traceElement, traceFile);
   status.setAttribute(DE.A_NAME, "done");
 }
 
 /**
  * Run the trace program, collect trace
  */
 public void handleTraceProgramAnalyze(DataElement traceElement, DataElement result, DataElement status) {

  // System.out.println("analyze result: " + result);
  
  String traceFormat = _adaptor.getAttribute(traceElement, getLocalizedString("pa.TraceFormat"));
  
  // System.out.println("trace format: " + traceFormat);
  
  ITraceReader reader = new PACommandOutputReader(result);
  PATraceFile traceFile = null;
  
  try {
    traceFile = PAAdaptor.createTraceFile(reader, traceFormat);
  }
  catch (Exception e) {
    e.printStackTrace();
    status.setAttribute(DE.A_NAME, "error");
    return;
  }
  
  _adaptor.populateDataStore(traceElement, traceFile);
  status.setAttribute(DE.A_NAME, "done");
  
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
