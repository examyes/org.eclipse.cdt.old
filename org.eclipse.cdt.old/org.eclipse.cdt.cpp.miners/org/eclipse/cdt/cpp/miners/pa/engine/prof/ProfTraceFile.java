package org.eclipse.cdt.cpp.miners.pa.engine.prof;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.util.*;
import java.io.*;
import org.eclipse.cdt.cpp.miners.pa.engine.*;

/**
 * ProfTraceFile represents a trace output file generated by prof. A prof
 * trace file is composed of flat profile and call graph.
 * This class parses the prof trace file and uses the parsed information to
 * populate the PA engine model.
 */
public class ProfTraceFile extends PATraceFile {

  private double 			  _cumulativeTime = 0;

  /**
   * Create a ProfTraceFile from a given file name
   */
  public ProfTraceFile(String filename) throws PAException {
   super(filename);
   setTraceFormat("prof");
  }

  /**
   * Create a ProfTraceFile from a given Java File
   */
  public ProfTraceFile(File file) throws PAException {
  
   super(file);   
   setTraceFormat("prof");
  }
  
  /**
   * Create a ProfTraceFile from a given BufferedReader
   */
  public ProfTraceFile(BufferedReader reader) {
  
   super(reader);
   setTraceFormat("prof");
  }
  
      
  /**
   * Process  input lines
   */
  public void processLine(String line) throws Exception {
   while ((line = _reader.readLine()) != null) {
     if (!ProfUtility.isFlatProfileHeaderLine(line.trim())) {
	parseFlatProfileEntry(line);
   }
  }
  _status.setFlatProfileStatus(PAParseStatus.DONE);
  _status.setCallGraphStatus(PAParseStatus.DONE);   

 }
  
  /**
   * Parse a line in the flat profile
   */
  private void parseFlatProfileEntry(String line) throws Exception {
   String delimiter = " \t\n\r\f|";
   PATokenizer tokenizer = new PATokenizer(line, delimiter,6);

      
   String FirstToken = tokenizer.getToken(0);
   String functionName = ProfUtility.trimmedFunctionName(FirstToken);
   ProfTraceFunction traceFunction = (ProfTraceFunction)findOrCreateTraceFunction(functionName);
   
   int tokenNumber = tokenizer.getTokenNumber();
   
   try {
   
    for (int i=1; i < tokenNumber; i++) {   
     switch (i) {
      
       case 1:
       
        traceFunction.setTotalPercentage(tokenizer.getTokenAsDouble(i));
        break;
         
       case 2:
       
        traceFunction.setSelfSeconds(tokenizer.getTokenAsDouble(i));
        break;
       
       case 3:
       
        traceFunction.setTotalSeconds(tokenizer.getTokenAsDouble(i));
        break;
       
       case 4:
       
        traceFunction.setCallNumber(tokenizer.getTokenAsInt(i));
        break;
       
       case 5:
        traceFunction.setMsPerCall((String)tokenizer.getLastToken());
        break;
      
       default:
        break;
       
     }     
    }
/*    traceFunction.setTotalSeconds(traceFunction.getCallNumber() * traceFunction.getTotalMsPerCall() / 1.0e6);
    traceFunction.setHasSummary(true); */
   }
   catch (NumberFormatException e) {
    System.out.println(e);
    throw new PAException("Invalid flat profile entry: " + line);
   }
      
  }
  
  /**
   * Find a PA trace function from the table, or create it
   * if it does not exist.
   */
  public PATraceFunction findOrCreateTraceFunction(String name) {

   PATraceFunction traceFunction = getTraceFunctionByName(name);
   if (traceFunction == null) {
    traceFunction = new ProfTraceFunction(this, name);
    addTraceFunction(traceFunction);
   }
   return traceFunction;
  }

}
