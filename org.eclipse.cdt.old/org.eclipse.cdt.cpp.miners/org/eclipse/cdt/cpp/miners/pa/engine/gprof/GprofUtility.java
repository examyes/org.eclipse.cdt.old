package org.eclipse.cdt.cpp.miners.pa.engine.gprof;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.util.*;
import org.eclipse.cdt.cpp.miners.pa.engine.*;

/**
 * The class GprofUtility contains utility methods which are used to parse
 * a gprof trace file.
 */
public class GprofUtility {

  public static GprofUtility _instance = new GprofUtility();
  
  // Constructor
  public GprofUtility() { }

  /**
   * Is this an empty line?
   */
  public static boolean isEmptyLine(String line) {   
   return (line == null || line.equals(""));
  }
  
  /**
   * Does this line act as a section separator?
   * Gprof uses the Form Feed '\f' character to separate different sections.
   */
  public static boolean isSectionSeparatorLine(String line) {
   return line.equals("\f");
  }
  
  /**
   * Is this a dash line?
   * A dash line is used to separate call graph entries.
   */
  public static boolean isDashLine(String line) {
      
   return (line.startsWith("---") && line.charAt(line.length()-1) == '-');
  }
  
  /**
   * Is this the first header line of the flat profile?
   */
  public static boolean isFlatProfileHeaderLine1(String line) {
   
   int length = line.length();
   int index = 0;
   while (index < length && Character.isWhitespace(line.charAt(index)))
     index++;
     
   if (index >= length)
    return false;
   else
    return (line.charAt(index) == '%' && 
       line.substring(index+1).trim().startsWith("cumulative")); 
       
  }

  /**
   * Is this the second header line of the flat profile?
   */
  public static boolean isFlatProfileHeaderLine2(String line) {

   return (line.startsWith("time") && 
       line.substring(4).trim().startsWith("seconds"));   
  }
  
  /**
   * Return the time unit for self/total time per call.
   * The default time unit is "ms".
   */
  public static double getPerCallTimeUnit(String line) {
  
   int index = line.indexOf("s/call");
   if (index > 0) {
    char ch = Character.toLowerCase(line.charAt(index-1));
    
    if (ch == 'u')
     return PATraceFile.TIME_UNIT_U;
    else if (ch == 't')
     return PATraceFile.TIME_UNIT_T;
    else if (ch == 'p')
     return PATraceFile.TIME_UNIT_P;
    else
     return PATraceFile.TIME_UNIT_M;
   }
   else 
    return PATraceFile.TIME_UNIT_M;
  }

  /**
   * Is this the header line of the call graph?
   */
  public static boolean isCallGraphHeaderLine1(String line) {

   return (line.startsWith("granularity:") || line.startsWith("ngranularity:"));
  }
  
  /**
   * Is this the header line of the call graph?
   */
  public static boolean isCallGraphHeaderLine2(String line) {

   return (line.startsWith("index") && line.substring(5).trim().startsWith("%"));   
  }
  
  /**
   * Is this an entry line of the call graph?
   */
  public static boolean isCallGraphEntryLine(String line) {
   
   if (isEmptyLine(line)) {
    return false;
   }
   else {
    char firstChar = line.charAt(0);
    return ((firstChar == '[' || Character.isDigit(firstChar)) &&
            line.endsWith("]"));
   }
   
  }
  
  /**
   * Is this the primary line of a call graph entry?
   */
  public static boolean isCallGraphPrimaryLine(String line) {
  
   return (line.charAt(0) == '[' && Character.isDigit(line.charAt(1)));
  }
  
  /**
   * Is this the line that tells sampling rate?
   */
  public static boolean isSamplingRateLine(String line) {

   return line.startsWith("Each sample counts");  
  }
  
  /**
   * Return the sampling rate
   */
  public static double getSamplingRate(String line) throws Exception {
  
   int lastIndex = line.indexOf(" seconds");
   String substring = line.substring(0, lastIndex);
   int firstIndex = substring.lastIndexOf(' ');
   
   if (lastIndex > firstIndex && firstIndex > 0) {
    return Double.parseDouble(line.substring(firstIndex+1, lastIndex));
   }
   else
    return 0;
   
  }
  
  /**
   * Is this a cyclic function?
   * A cyclic function name contains the substring "<cycle ".
   */
  public static boolean isCyclicFunction(String functionName) {
   return (functionName.indexOf("<cycle ") > 0);
  }
 
  /**
   * Remove the extra characters that are not part of a function name in
   * the flat profile of BSD gprof output.
   */
  public static String trimmedBsdFlatProfileFunctionName(String name) {
   
    if (name.charAt(0) == '.')
      name = name.substring(1);
     
    if (name.charAt(name.length()-1) == ']') {
      int bracketIndex = name.lastIndexOf('[');
      
      if (bracketIndex > 0)
       name = name.substring(0, bracketIndex).trim();
    }
   
    return name;
  }

  /**
   * Trim the function name for a call graph entry in GNU gprof output.
   */
  public static String trimmedGnuCallGraphFunctionName(String name) {
    
   int cycleIndex = name.indexOf("<cycle ");
   
   if (cycleIndex > 0) {
    return name.substring(0, cycleIndex).trim();
   }
   else {
    int bracketIndex = name.lastIndexOf('[');
   
    if (bracketIndex > 0)
     return name.substring(0, bracketIndex).trim();
    else
     return name;
   }
   
  }
 
  /**
   * Trim the function name for a call graph entry in BSD gprof output.
   */
  public static String trimmedBsdCallGraphFunctionName(String name) {

   if (name.charAt(0) == '.')
    name = name.substring(1);
    
   int bracketIndex = name.lastIndexOf('[');
   
   if (bracketIndex > 0)
     return name.substring(0, bracketIndex).trim();
   else
     return name;
   
  }
  
  /**
   * Return the first number from a "m/n" pair
   */
  public static int getFirstCallNumber(String callString) throws NumberFormatException {
  
   int slashIndex = callString.indexOf('/');
   if (slashIndex > 0) {
    return Integer.parseInt(callString.substring(0, slashIndex));
   }
   else {
    int plusIndex = callString.indexOf('+');
    if (plusIndex > 0)
     return Integer.parseInt(callString.substring(0, plusIndex));
    else
     return Integer.parseInt(callString);
   }
   
  }

  /**
   * Is this a valid call number?
   * A valid call number can be in the form of int, int/int or int+int.
   */
  public static boolean isValidCallNumber(String callString) {
  
   String numString1 = callString;
   String numString2 = null;
   
   int slashIndex = callString.indexOf('/');
   int plusIndex = callString.indexOf('+');
   
   int index = 0;
   if (slashIndex > 0)
    index = slashIndex;
   else if (plusIndex > 0)
    index = plusIndex;
    
   if (index > 0) {   
    numString1 = callString.substring(0, index);
    numString2 = callString.substring(index+1).trim();
   }
   
   try {
    Integer.parseInt(numString1);
    if (numString2 != null) {
     Integer.parseInt(numString2);
    }
   }
   catch (NumberFormatException e) {
    return false;
   }
   
   return true;
  }

}