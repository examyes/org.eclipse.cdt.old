package org.eclipse.cdt.cpp.miners.pa.engine.functioncheck;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.util.*;
import java.io.*;
import org.eclipse.cdt.cpp.miners.pa.engine.*;

/**
 * FunctionCheckTraceFile represents a trace file in FunctionCheck format.
 * It extends PATraceFile and overwrites the processLine() interface.
 */
public class FunctionCheckTraceFile extends PATraceFile {

  // Time mode constants
  public static int CLOCK_TIME = 0;
  public static int CPU_TIME = 1;
  public static int UNKNOWN_TIME_MODE = -1;
  
  // Profile mode constants
  public static int SINGLE_PROCESS = 0;
  public static int FORK = 1;
  public static int THREAD = 2;
  public static int UNKNOWN_PROFILE_MODE = -1;
  
  // Attributes
  private int _timeMode = CLOCK_TIME;
  private int _profileMode = SINGLE_PROCESS;
  private int _processId = 0;
  private String _programName = null;
  
  // Status flags
  private boolean _foundFlatProfileHeader1 = false;
  private boolean _foundFlatProfileHeader2 = false;
  private PATraceFunction _currentCaller = null;
  
  /**
   * Create a FunctionCheckTraceFile from a given file name
   */
  public FunctionCheckTraceFile(String name) throws PAException {
   
   super(name);   
   setTraceFormat("FunctionCheck");
  }
  
  /**
   * Create a FunctionCheckTraceFile from a given Java File object
   */
  public FunctionCheckTraceFile(File file) throws PAException {
   
   super(file);   
   setTraceFormat("FunctionCheck");
  }
  
  /**
   * Create a FunctionCheckTraceFile from a BufferedReader.
   * The trace contents are read from the stream associated with the BufferedReader.
   */
  public FunctionCheckTraceFile(BufferedReader reader) {
  
   super(reader);
   setTraceFormat("FunctionCheck");
  }
  
  /**
   * Return the time mode (cpu time or real clock time).
   */
  public String getTimeMode() {
   
   if (_timeMode == CPU_TIME)
    return "cpu time";
   else if (_timeMode == CLOCK_TIME)
    return "real clock time";
   else
    return "unknown";
    
  }
  
  /**
   * Return the profile mode (single process, fork or thread).
   */
  public String getProfileMode() {
  
   if (_profileMode == SINGLE_PROCESS)
    return "single process";
   else if (_profileMode == FORK)
    return "fork";
   else if (_profileMode == THREAD)
    return "thread";
   else
    return "unknown";
  }
  
  /**
   * Return the process id.
   */
  public int getProcessId() {
   return _processId;
  }
  
  /**
   * Return the program name.
   */
  public String getProgramName() {
   return _programName;
  }
  
  /**
   * Process a line from the input. The input can be a file or
   * a stream reader.
   */
  public void processLine(String line) throws Exception {

   if (_status.isParsingFlatProfile()) {
    if (FunctionCheckUtility.isEmptyLine(line)) {
     _status.setFlatProfileStatus(PAParseStatus.DONE);
    }
    else
     parseFlatProfileEntry(line);
   }
   else if (_status.isParsingCallGraph()) {
    
    if (FunctionCheckUtility.isCycleHeader(line)) {
     _status.setCallGraphStatus(PAParseStatus.DONE);
    }
    else
     parseCallGraphEntry(line);
   }
   else if (FunctionCheckUtility.isFlatProfileHeader1(line)) {
    _foundFlatProfileHeader1 = true;
   }
   else if (_foundFlatProfileHeader1 &&
            FunctionCheckUtility.isFlatProfileHeader2(line)) {
    _foundFlatProfileHeader2 = true;
   }
   else if (_foundFlatProfileHeader2 &&
   	    FunctionCheckUtility.isFlatProfileHeader3(line)) {
    _status.setFlatProfileStatus(PAParseStatus.PARSING);
   }
   else if (FunctionCheckUtility.isCallGraphHeader(line)) {
    _status.setCallGraphStatus(PAParseStatus.PARSING);
   }
   else if (FunctionCheckUtility.isProgramLine(line)) {
    _programName = FunctionCheckUtility.getProgramName(line);
   }
   else if (FunctionCheckUtility.isTimeModeLine(line)) {
    _timeMode = FunctionCheckUtility.getTimeMode(line);
   }
   else if (FunctionCheckUtility.isProfileModeLine(line)) {
    _profileMode = FunctionCheckUtility.getProfileMode(line);
   }
   else if (FunctionCheckUtility.isProcessIdLine(line)) {
    _processId = FunctionCheckUtility.getProcessId(line);
   }
   else if (FunctionCheckUtility.isTotalTimeLine(line)) {
    setTotalExecutionTime(FunctionCheckUtility.getTotalTime(line));
   }
   else {
    // Ignore other lines
   }
  
  }
    
  /**
   * Parse a flat profile line
   */
  private void parseFlatProfileEntry(String line) throws Exception {
  
   String delimiter = " \t\n\r\f|";
   PATokenizer tokenizer = new PATokenizer(line, delimiter, 10);
   
   if (tokenizer.getTokenNumber() < 10) {
    throw new PAException("Invalid flat profile entry: " + line);
   }
   
   String lastToken = tokenizer.getLastToken();
   FunctionCheckTraceFunction traceFunction = 
          (FunctionCheckTraceFunction)findOrCreateTraceFunction(lastToken);
   
   try {
    traceFunction.setCallNumber(tokenizer.getTokenAsInt(8));
    traceFunction.setTotalSeconds(tokenizer.getTokenAsDouble(0));
    traceFunction.setTotalPercentage(tokenizer.getTokenAsDouble(1));
    traceFunction.setSelfSeconds(tokenizer.getTokenAsDouble(2));
    traceFunction.setSelfPercentage(tokenizer.getTokenAsDouble(3));
    traceFunction.setMinTotalTime(tokenizer.getTokenAsDouble(4));
    traceFunction.setMaxTotalTime(tokenizer.getTokenAsDouble(5));
    traceFunction.setMinLocalTime(tokenizer.getTokenAsDouble(6));
    traceFunction.setMaxLocalTime(tokenizer.getTokenAsDouble(7));
    
    if (traceFunction.getCallNumber() > 0) {
     traceFunction.setSelfMsPerCall (traceFunction.getSelfSeconds()  * 1.0e6 / traceFunction.getCallNumber());
     traceFunction.setTotalMsPerCall(traceFunction.getTotalSeconds() * 1.0e6 / traceFunction.getCallNumber());
    }
   }
   catch (NumberFormatException e) {
    System.out.println(e);
    throw new PAException("Invalid flat profile entry: " + line);
   }
   
  }
  
  /**
   * Parse a line in the call graph
   */
  private void parseCallGraphEntry(String line) throws PAException {
  
   if (FunctionCheckUtility.isEmptyLine(line)) {
    _currentCaller = null;
    return;
   }
   
   if (FunctionCheckUtility.isCallerLine(line)) {
    String callerName = FunctionCheckUtility.getCallerName(line);
    _currentCaller = findOrCreateTraceFunction(callerName);
    _numberOfCallGraphEntries++;
   }
   else {
    parseCalleeLine(line);
   }
   
  }

  /**
   * Parse a callee line
   */
  private void parseCalleeLine(String line) throws PAException {
  
   FunctionCheckCalleeTokenizer tokenizer = new FunctionCheckCalleeTokenizer(line);
   
   while (tokenizer.hasMoreTokens()) {
    FunctionCheckCalleeToken token = tokenizer.nextToken();
    
    if (token.getName().equals("nobody"))
     return;
    
    PATraceFunction callee = findOrCreateTraceFunction(token.getName());
    if (_currentCaller != null) {
     PACallArc anArc = new PACallArc(_currentCaller, callee);
     
     String numberStr = token.getNumber();
     if (numberStr != null) {
       try {
        int callNumber = Integer.parseInt(numberStr);
        anArc.setCallNumber(callNumber);
       }
       catch (NumberFormatException e) {
        System.out.println(e);
        throw new PAException("Invalid call number at line: " + line);
       }
     }
     
     _currentCaller.addCallee(anArc);
     callee.addCaller(anArc);
    }
    
   }
   
  }
  
  /**
   * Find a PA trace function from the table, or create it
   * if it does not exist.
   */
  public PATraceFunction findOrCreateTraceFunction(String name) {
   
   PATraceFunction traceFunction = getTraceFunctionByName(name);
   if (traceFunction == null) {
    traceFunction = new FunctionCheckTraceFunction(this, name);
    addTraceFunction(traceFunction);
   }
   return traceFunction;
  }
  
}
