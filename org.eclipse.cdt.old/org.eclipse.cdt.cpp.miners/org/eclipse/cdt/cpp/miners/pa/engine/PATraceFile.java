package org.eclipse.cdt.cpp.miners.pa.engine;


import java.util.*;
import java.io.*;

/**
 * PATraceFile is an abstract class that captures the general characteristics in
 * the trace information generated by all profilers.
 * The processLine() method must be implemented by a subclass to provide a line
 * parsing mechanism for a specific trace file format.
 */
public abstract class PATraceFile {

  // The time unit constants.
  // Time units are used in the flat profile for the self/total 
  // time per call information.
  // M: 1E6 	T: 1E9		P: 1E12
  public static double 		TIME_UNIT_M = 1.0e6;
  public static double 		TIME_UNIT_T = 1.0e9;
  public static double 		TIME_UNIT_P = 1.0e12;

  // Trace file name
  protected String 			_traceFileName;
  protected BufferedReader  _reader;
  
  // The table to store all trace functions
  protected HashMap 		_traceFunctionTable;
  
  // The property map
  protected HashMap 		_properties;
  
  // The trace file parse status
  protected PAParseStatus 	_status;
  
  // Trace file attributes
  protected int 			_numberOfCallGraphEntries;  
  private   double 			_timeUnit;
  private   double 			_totalExecutionTime;
  
  // The list of call cycles
  private 	ArrayList 		_callCycles;
  
  
  /**
   * Create an empty PA trace file
   */
  public PATraceFile() {

   _numberOfCallGraphEntries = 0;
   _totalExecutionTime = 0;
   _timeUnit = TIME_UNIT_M;
   
   _traceFunctionTable = new HashMap(10);
   _properties = new HashMap();
   _callCycles = new ArrayList();
   _status = new PAParseStatus();   
  
  }
  
  /**
   * Create a PA trace file from an input file name
   */
  public PATraceFile(String traceFileName) throws PAException {
   
   this(new File(traceFileName));
   
  }
 
   /**
    * Create a PA trace file from a java file
    */
   public PATraceFile(File file) throws PAException {
    
    this();
    
    _traceFileName = file.getAbsolutePath();
    
    try {
     _reader = new BufferedReader(new FileReader(file));
    }
    catch (FileNotFoundException e) {
     throw new PAException("Cannot find the trace file: " + _traceFileName);
    }    
  }
  
  /**
   * Create a PA trace file from a trace reader
   */
  public PATraceFile(BufferedReader reader) {
   
   this();
   
   _reader = reader;
  }
  
  /**
   * Set the trace file name
   */
  public void setTraceFileName(String traceFileName) throws PAException {
  
    _traceFileName = traceFileName;   
    
    try {
     _reader = new BufferedReader(new FileReader(traceFileName));
    }
    catch (FileNotFoundException e) {
     throw new PAException("Cannot find the trace file: " + traceFileName);
    }   
  }

  /**
   * The main parsing method
   */
  public void parse() throws Exception {
  
    String line = null;
    while (!_status.isAllDone() && (line = _reader.readLine()) != null) {
     processLine(line);
    }
    
    _reader.close();
   
  }
  
  // The abstract processLine() method should be implemented by a subclass.
  protected abstract void processLine(String line) throws Exception;
  
  // setter methods
  protected void setTotalExecutionTime(double time) {
   _totalExecutionTime = time;
  }

  public void setTimeUnit(double timeUnit) {
   _timeUnit = timeUnit;
  }
    
  /**
   * Add a trace function to the table
   */
  protected void addTraceFunction(PATraceFunction traceFunction) {
   _traceFunctionTable.put(traceFunction.getName(), traceFunction);
  }
  
  /**
   * Add a call cycle
   */
  public void addCallCycle(PACallCycle cycle) {
   _callCycles.add(cycle);
  }
  
  // getter methods
  public String getTraceFileName() {
   return _traceFileName;
  }
    
  public int getNumberOfCallGraphEntries() {
   return _numberOfCallGraphEntries;
  }
  
  public int getNumberOfTraceFunctions() {
   return _traceFunctionTable.size();
  }
  
  public double getTotalExecutionTime() {
   return _totalExecutionTime;
  }
  
  public double getTimeUnit() {
   return _timeUnit;
  }
  
  public Collection getCallCycles() {
   return _callCycles;
  }
  
  public int getNumberOfCallCycles() {
   return _callCycles.size();
  }
  
  /**
   * Return a PA trace function for the given name
   */
  public PATraceFunction getTraceFunctionByName(String name) {
   return (PATraceFunction)_traceFunctionTable.get(name);
  }
  
  /**
   * Find a PA trace function from the table, or create it
   * if it does not exist.
   */
  public PATraceFunction findOrCreateTraceFunction(String name) {
   
   PATraceFunction traceFunction = getTraceFunctionByName(name);
   if (traceFunction == null) {
    traceFunction = new PATraceFunction(this, name);
    _traceFunctionTable.put(name, traceFunction);
   }
   return traceFunction;
  }
  
  /**
   * Return a collection of trace functions
   */
  public Collection getTraceFunctions() {
   return _traceFunctionTable.values();
  }
  
  /**
   * Set a property for the given name to the given value
   */
  public void setProperty(String name, Object value) {
   _properties.put(name, value);
  }
  
  /**
   * Return the property for a given name
   */
  public Object getProperty(String name) {
   return _properties.get(name);
  }

}