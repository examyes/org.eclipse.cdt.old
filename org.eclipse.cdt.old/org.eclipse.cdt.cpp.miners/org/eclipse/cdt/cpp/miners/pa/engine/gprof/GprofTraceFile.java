package org.eclipse.cdt.cpp.miners.pa.engine.gprof;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.util.*;
import java.io.*;
import org.eclipse.cdt.cpp.miners.pa.engine.*;

/**
 * GprofTraceFile represents a trace output file generated by gprof. A gprof
 * trace file is composed of flat profile and call graph.
 * This class parses the gprof trace file and uses the parsed information to
 * populate the PA engine model.
 */
public class GprofTraceFile extends PATraceFile {

  // There are two variations of gprof (GNU and BSD).
  public final static int	  GNU_GPROF = 0;
  public final static int	  BSD_GPROF = 1;

  private GprofCallGraphEntry _currentCallGraphEntry = null;
  private double 			  _samplingRate = 0;
  private double 			  _cumulativeTime = 0;
  private boolean 			  _foundCallGraphPrimaryLine = false;
  
  // The default variation is set to GNU.
  private int				  _gprofVariation = GNU_GPROF;

  /**
   * Create a GprofTraceFile from a given file name
   */
  public GprofTraceFile(String filename) throws PAException {
  
   super(filename);
   setTraceFormat("gprof");
  }

  /**
   * Create a GprofTraceFile from a given Java File
   */
  public GprofTraceFile(File file) throws PAException {
  
   super(file);   
   setTraceFormat("gprof");
  }
  
  /**
   * Create a GprofTraceFile from a given BufferedReader
   */
  public GprofTraceFile(BufferedReader reader) {
  
   super(reader);
   setTraceFormat("gprof");
  }
  

  /**
   * Return the sampling rate
   */
  public double getSamplingRate() {
   return _samplingRate;
  }
   
  /**
   * Return the gprof variation (GNU or BSD).
   */
  public int getGprofVariation() {
   return _gprofVariation;
  }
  
  /**
   * Process an input line
   */
  public void processLine(String line) throws Exception {
   
   // Are we parsing the flat profile?
   if (_status.isParsingFlatProfile())
   {
   
    if (GprofUtility.isSectionSeparatorLine(line)) {
     _status.setFlatProfileStatus(PAParseStatus.DONE);
     setTotalExecutionTime(_cumulativeTime);
    }
    else {
     line = line.trim();
     if (!GprofUtility.isEmptyLine(line)) {
      parseFlatProfileEntry(line);     
     }
    }
    
   }
   
   // Are we parsing the call graph?
   else if (_status.isParsingCallGraph()) 
   {
    
    if (GprofUtility.isSectionSeparatorLine(line)) {
     _status.setCallGraphStatus(PAParseStatus.DONE);
    }
    else {
     line = line.trim();
     if (GprofUtility.isDashLine(line)) {
      _foundCallGraphPrimaryLine = false;
      _currentCallGraphEntry = null;
     }
     else if (GprofUtility.isCallGraphEntryLine(line)) {
      parseCallGraphEntry(line);
     }
    }
         
   }
   
   // Check whether this is the second flat profile header line.
   else if (_status.getFlatProfileStatus() == PAParseStatus.HEADER) 
   {
   
    if (GprofUtility.isFlatProfileHeaderLine2(line.trim())) {
     _status.setFlatProfileStatus(PAParseStatus.PARSING);
     setTimeUnit(GprofUtility.getPerCallTimeUnit(line));
    }
    
   }
   
   // Check whether this is the second call graph header line.
   else if (_status.getCallGraphStatus() == PAParseStatus.HEADER) 
   {
   
    if (GprofUtility.isCallGraphHeaderLine2(line)) {
     _status.setCallGraphStatus(PAParseStatus.PARSING);
     
     // If the call graph comes before the flat profile, then we are
     // using BSD gprof. Otherwise we are using GNU gprof.
     if (_status.getFlatProfileStatus() == PAParseStatus.NOTYET) {
      _gprofVariation = BSD_GPROF;
     }
     
    }
     
   }
   
   // Check whether this is the first flat profile header line.
   else if (GprofUtility.isFlatProfileHeaderLine1(line)) 
   {
    _status.setFlatProfileStatus(PAParseStatus.HEADER);
   }
   
   // Check whether this is the first call graph header line.
   else if (_status.getCallGraphStatus() != PAParseStatus.DONE &&
            GprofUtility.isCallGraphHeaderLine1(line)) 
   {
    _status.setCallGraphStatus(PAParseStatus.HEADER);
   }
   
   // Check whether this is the sampling rate line. 
   // The sampling rate line should come before the flat profile and call graph headers.
   else if (_status.getFlatProfileStatus() == PAParseStatus.NOTYET &&
   		    _status.getCallGraphStatus() == PAParseStatus.NOTYET) 
   {
   		    
    if (GprofUtility.isSamplingRateLine(line)) {
     _samplingRate = GprofUtility.getSamplingRate(line);
    }
    
   }
   else 
   {
    // other lines are ignored.
   }
   
  }
  
  /**
   * Parse a line in the flat profile
   */
  private void parseFlatProfileEntry(String line) throws Exception {
  
   PATokenizer tokenizer = new PATokenizer(line, 7);
      
   String functionName = tokenizer.getLastToken();
   
   // If we are using BSD gprof, we need to remove the leading '.' and trailing
   // "[]" in the function name.
   if (_gprofVariation == BSD_GPROF) {
    functionName = GprofUtility.trimmedBsdFlatProfileFunctionName(functionName);
   }
    
   PATraceFunction traceFunction = findOrCreateTraceFunction(functionName);
   
   int tokenNumber = tokenizer.getTokenNumber();
   
   try {
   
    for (int i=0; i < tokenNumber-1; i++) {   
     switch (i) {
      
       case 0:
       
        traceFunction.setTotalPercentage(tokenizer.getTokenAsDouble(i));
        break;
         
       case 1:
       
        _cumulativeTime = tokenizer.getTokenAsDouble(i);
        break;
       
       case 2:
       
        traceFunction.setSelfSeconds(tokenizer.getTokenAsDouble(i));
        break;
       
       case 3:
       
        traceFunction.setCallNumber(tokenizer.getTokenAsInt(i));
        break;
       
       case 4:
       
        traceFunction.setSelfTimePerCall(tokenizer.getTokenAsDouble(i));
        break;
       
       case 5:
       
        traceFunction.setTotalTimePerCall(tokenizer.getTokenAsDouble(i));
        break;
       
       default:
        break;
       
     }     
    }
    traceFunction.setTotalSeconds(traceFunction.getCallNumber() * traceFunction.getTotalTimePerCall() * getTimeUnit());
    traceFunction.setHasSummary(true);
   }
   catch (NumberFormatException e) {
    System.out.println(e);
    throw new PAException("Invalid flat profile entry: " + line);
   }
      
  }
  
  /**
   * Parse a line from the call graph
   */
  private void parseCallGraphEntry(String line) throws Exception {
     
   if (_currentCallGraphEntry == null) {
    _currentCallGraphEntry = new GprofCallGraphEntry(this);
    _numberOfCallGraphEntries++;
    
    if (GprofUtility.isCallGraphPrimaryLine(line)) {
     _currentCallGraphEntry.addPrimaryLine(line);
     _foundCallGraphPrimaryLine = true;
    }
    else
     _currentCallGraphEntry.addCallerLine(line);
   }
   else {
   
    if (_foundCallGraphPrimaryLine) {
     _currentCallGraphEntry.addSubroutineLine(line);
    }
    else if (GprofUtility.isCallGraphPrimaryLine(line)) {
     _currentCallGraphEntry.addPrimaryLine(line);
     _foundCallGraphPrimaryLine = true;    
    }
    else {
     _currentCallGraphEntry.addCallerLine(line);
    }
    
   }   
  }
  
}