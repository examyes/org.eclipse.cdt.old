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
 private PAMiner     _miner;
 private DataStore   _dataStore;
 private DataElement _traceFunctionsRoot;
 private DataElement _callTreeRoot;
 private DataElement _attributesRoot;
 private DataElement _callArcsRoot;
 private DataElement _provideSourceForD;
 
 // maps between PA trace functions and their corresponding data elements
 private HashMap _elementToTraceFuncMap;
 private HashMap _traceFuncToElementMap;
 
 
 // Constructors
 public PADataStoreAdaptor(PAMiner miner) {
  
   _miner = miner;
   _dataStore = miner.getDataStore();
   _elementToTraceFuncMap = new HashMap();
   _traceFuncToElementMap = new HashMap();

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
 public String getLocalizedString(String key) {
   return _miner.getLocalizedString(key);
 }
 
 /**
  * Return the attribute value associated with a given data element
  */
 public String getAttribute(DataElement element, String name) {
  
  ArrayList attributes = element.getAssociated(getLocalizedString("pa.Attributes"));
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
 public String getTraceFileFormat(PATraceFile traceFile) {
 
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
 public String getTraceFunctionFormat(PATraceFunction traceFunction) {
 
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
 public void provideSourceFor(DataElement traceFuncElement) {
       
   if (_provideSourceForD != null) {
     DataElement traceFileElement = traceFuncElement.getParent().getParent();
     ArrayList projects = traceFileElement.getAssociated(getLocalizedString("pa.ReferencedProject"));
     DataElement projectElement = null;
     
     if (projects.size() > 0) {
      projectElement = (DataElement)projects.get(0);
     }
     else {
      System.out.println("Cannot find the containing project for trace function: " + traceFuncElement.getName());
     }
     
     ArrayList args = new ArrayList();
     args.add(projectElement);
     _dataStore.command(_provideSourceForD, args, traceFuncElement, false);
   }
   
 }
  
 /**
  * Create a trace function object in the datastore
  */
 public DataElement createTraceFunction(DataElement parent, PATraceFunction traceFunction) {
 
  String type = getTraceFunctionFormat(traceFunction);
  
  DataElement traceFuncElement = _dataStore.createObject(parent, type, traceFunction.getName());
  //traceFuncElement.expandChildren();
  provideSourceFor(traceFuncElement);
  return traceFuncElement;
 }
 
 /**
  * Populate the datastore using the information from the PA trace file.
  */
 public void populateDataStore(DataElement fileElement, PATraceFile traceFile) {
                   
   // set the trace file attributes
   createTraceFileSummary(fileElement, traceFile);
   
   // Create trace functions root, call trace root, attributes root and call arc root
   _traceFunctionsRoot = _dataStore.createObject(fileElement, fileElement.getType(), fileElement.getName());
   _traceFunctionsRoot.setAttribute(DE.A_VALUE, getLocalizedString("pa.TraceFuncRoot"));
   
   _callTreeRoot   = _dataStore.createObject(fileElement, getLocalizedString("pa.CallRoot"), fileElement.getName());
   _attributesRoot = _dataStore.createObject(fileElement, getLocalizedString("pa.data"), getLocalizedString("pa.AttributesRoot"));
   _callArcsRoot   = _dataStore.createObject(fileElement, getLocalizedString("pa.data"), getLocalizedString("pa.CallArcsRoot"));
   
   // setAttribute(fileElement, getLocalizedString("pa.TraceFormat"), getTraceFileFormat(traceFile));
   
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
