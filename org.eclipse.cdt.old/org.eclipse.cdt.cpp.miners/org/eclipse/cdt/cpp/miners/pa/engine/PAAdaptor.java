package org.eclipse.cdt.cpp.miners.pa.engine;

import java.io.*;
import java.util.*;

import org.eclipse.cdt.cpp.miners.pa.engine.gprof.*;
import org.eclipse.cdt.cpp.miners.pa.engine.functioncheck.*;


public class PAAdaptor {
 
 private static PAAdaptor _instance = new PAAdaptor();
 
 // Trace file format constants
 public static final int AUTO = 0;
 public static final int GPROF_GNU = 1;
 public static final int GPROF_BSD = 2;
 public static final int GPROF_ALL = 3;
 public static final int FUNCTIONCHECK = 4;
 public static final int INVALID = -1;
 
 // The default trace file format is AUTO
 private static int _traceFileFormat = AUTO;
 
 /**
  * Constructor
  */
 private PAAdaptor() { }
 
 /**
  * Create a PA trace file from a given java file
  */
 public static PATraceFile createTraceFile(File file) throws Exception {
  return createTraceFile(file, _traceFileFormat);
 }

 /**
  * Create a PA trace file from a given file name
  */
 public static PATraceFile createTraceFile(String fileName) throws Exception {
  return createTraceFile(new File(fileName));
 }

 /**
  * Create a PA trace file from a given file name in the given format
  */
 public static PATraceFile createTraceFile(String fileName, int format) throws Exception {
  return createTraceFile(new File(fileName), format);
 }
 
 /**
  * Create a PA trace file from a given java file in the given format
  */
 public static PATraceFile createTraceFile(File file, int format) throws Exception {

  if (format == AUTO)
   format = queryTraceFileFormat(file);
   
  PATraceFile traceFile = null;
  if (format == GPROF_GNU || format == GPROF_ALL) {
   traceFile = new GprofTraceFile(file);
   traceFile.parse();   
  }
  else if (format == GPROF_BSD) {
   traceFile = new GprofTraceFile(file);
   traceFile.parse();   
   markCyclicFunctions(traceFile.getTraceFunctions());  
  }
  else if (format == FUNCTIONCHECK) {
   traceFile = new FunctionCheckTraceFile(file);
   traceFile.parse();
  }
  else
   throw new PAException("Illegal trace file format:" + file.getAbsolutePath());
  
  return traceFile;
 }
 
 
 /**
  * Create a PA trace file in the given format
  */
 public static PATraceFile createTraceFile(File file, String formatString) throws Exception {
 
  if (formatString == null)
   return createTraceFile(file); 
  else if (formatString.toLowerCase().equals("gprof_gnu"))
   return createTraceFile(file, GPROF_GNU);
  else if (formatString.toLowerCase().equals("gprof_bsd"))
   return createTraceFile(file, GPROF_BSD);
  else if (formatString.toLowerCase().equals("gprof"))
   return createTraceFile(file, GPROF_ALL);  
  else if (formatString.toLowerCase().equals("functioncheck"))
   return createTraceFile(file, FUNCTIONCHECK);
  else
   return createTraceFile(file, AUTO);
   
 }
 
 /**
  * Create a PA trace file from a given trace reader
  */
 public static PATraceFile createTraceFile(ITraceReader reader, String formatString) throws Exception {
 
  if (formatString == null || formatString.toLowerCase().equals("gprof_gnu"))
   return createTraceFile(reader, GPROF_GNU);
  else if (formatString.toLowerCase().equals("gprof_bsd"))
   return createTraceFile(reader, GPROF_BSD);
  else if (formatString.toLowerCase().equals("gprof"))
   return createTraceFile(reader, GPROF_ALL);  
  else if (formatString.toLowerCase().equals("functioncheck"))
   return createTraceFile(reader, FUNCTIONCHECK);
  else
   throw new PAException("Illegal trace file format.");
   
 }
 
  /**
   * Create a PA trace file from a given trace reader
   */
  public static PATraceFile createTraceFile(ITraceReader reader, int format) throws Exception {
     
   PATraceFile traceFile = null;
   if (format == GPROF_GNU || format == GPROF_ALL) {
    traceFile = new GprofTraceFile(reader);
    traceFile.parse();   
   }
   else if (format == GPROF_BSD) {
    traceFile = new GprofTraceFile(reader);
    traceFile.parse();   
    markCyclicFunctions(traceFile.getTraceFunctions());  
   }
   else if (format == FUNCTIONCHECK) {
    traceFile = new FunctionCheckTraceFile(reader);
    traceFile.parse();
   }
   else
    throw new PAException("Illegal trace file format.");
   
   return traceFile;
  }

 /**
  * Set the trace file format
  */
 public static void setTraceFileFormat(int format) {
  _traceFileFormat = format;
 }
 
 /**
  * Return the current trace file format
  */
 public static int getTraceFileFormat() {
  return _traceFileFormat;
 }
 
 /**
  * Query the given trace file and return its format
  */
 public static int queryTraceFileFormat(File file) throws Exception {
  
  BufferedReader reader = null;
  String firstLine = null;
  try {
   reader = new BufferedReader(new FileReader(file));
   while ((firstLine = reader.readLine()) != null) {
    if (!firstLine.trim().equals(""))
     break;
   }
  }
  catch (IOException e) {
   throw new PAException("Error reading file: " + file.getAbsolutePath());
  }
  finally {
   try { reader.close(); } catch (IOException e) {}
  }
  
  firstLine = firstLine.trim();
  if (firstLine.startsWith("Flat profile:"))
   return GPROF_GNU;
  else if (firstLine.startsWith("ngranularity:") || firstLine.startsWith("granularity:"))
   return GPROF_BSD;
  else if (firstLine.startsWith("FunctionCheck"))
   return FUNCTIONCHECK;
  else
   return INVALID;
  
 }
 
 /**
  * Find out which function is cyclic and put a mark on it.
  */
 public static void markCyclicFunctions(Collection functions) {
 
  Iterator it = functions.iterator();
  while (it.hasNext()) {
   PATraceFunction traceFunction = (PATraceFunction)it.next();
   boolean isCyclic = isCyclicFunction(traceFunction);
   traceFunction.setCyclic(isCyclic);
  }
  
 }
 
 /**
  * Find out whether the given function is cyclic
  */
 public static boolean isCyclicFunction(PATraceFunction traceFunction) {
 
  if (traceFunction.getNumberOfCallers() == 0 || 
      traceFunction.getNumberOfCallees() == 0 )
  {
   return false;
  }
  else {
   return isElementInTree(traceFunction, traceFunction);
  }
  
 }
 
 /**
  * Find out whether a PA trace function is contained in a caller tree.
  */
 public static boolean isElementInTree(PATraceFunction element, PATraceFunction treeRoot) {
  
  //System.out.println("element: " + element.getName());
  //System.out.println("treeRoot: " + treeRoot.getName());
  
  int numCallers = treeRoot.getNumberOfCallers();
  
  if (numCallers == 0)
   return false;
   
  for (int i=0; i < numCallers; i++) {
   if (treeRoot.getCaller(i) == element)
    return true;
  }
  
  boolean isContained = false;
  for (int i=0; i < numCallers; i++) {
   
   if (isElementInTree(element, treeRoot.getCaller(i))) {
    isContained = true;
    break;
   }
   
  }
  
  return isContained;
 }
 
}
