package org.eclipse.cdt.cpp.miners.pa.engine.prof;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.util.*;
import org.eclipse.cdt.cpp.miners.pa.engine.*;

/**
 * The class ProfUtility contains utility methods which are used to parse
 * a prof trace file.
 */
public class ProfUtility {

  public static ProfUtility _instance = new ProfUtility();
  
  // Constructor
  public ProfUtility() { }

  /**
   * Is this an empty line?
   */
  public static boolean isEmptyLine(String line) {   
   return (line == null || line.equals(""));
  }
  
  
  /**
   * Is this the first header line of the flat profile?
   */
  public static boolean isFlatProfileHeaderLine(String line) {
        
   if (isEmptyLine(line))
    return false;
   else
    return (line.indexOf("%") > 0 && 
       line.startsWith("Name")); 
       
  }

  
  /**
   * Return the time unit for self/total time per call
   */
  public static double getPerCallTimeUnit(String line) {
  
   int index = line.indexOf("s/call");
   if (index > 0) {
    char ch = Character.toLowerCase(line.charAt(index-1));
    if (ch == 't')
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
   * Remove the extra characters that are not part of a function name
   */
  public static String trimmedFunctionName(String name) {

   if (name.charAt(0) == '.')
    name = name.substring(1);
    
   return name;
   
  }
  
  /**
   * Return the first number from a "m/n" pair
   */
  public static int getFirstCallNumber(String callString) throws NumberFormatException {
  
     return Integer.parseInt(callString);
  }

  /**
   * Is this a valid call number?
   * A valid call number can be in the form of int, int/int or int+int.
   */
  public static boolean isValidCallNumber(String callString) {
  
   String numString1 = callString;
   
   try {
    Integer.parseInt(numString1);
   }
   catch (NumberFormatException e) {
    return false;
   }
   
   return true;
  }

}
