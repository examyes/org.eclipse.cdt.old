package org.eclipse.cdt.cpp.miners.pa.engine.functioncheck;


import java.util.*;
import java.io.*;
import org.eclipse.cdt.cpp.miners.pa.engine.*;

public class FunctionCheckUtility {

  private static FunctionCheckUtility _instance = new FunctionCheckUtility();
  
  // Constructor
  private FunctionCheckUtility() { }

  /**
   * Is this an empty line?
   */
  public static boolean isEmptyLine(String line) {   
   return (line == null || line.equals(""));
  }
  
  /**
   * Does this line tell you the program name?
   */
  public static boolean isProgramLine(String line) {
   return line.startsWith("Execution profile for program");
  }
  
  /**
   * Return the program name for the program line
   */
  public static String getProgramName(String line) {
  
    int firstIndex = line.indexOf("'");
    int lastIndex = line.lastIndexOf("'");
    if (firstIndex > 0 && lastIndex > firstIndex) {
     return line.substring(firstIndex+1, lastIndex);
    }
    else
     return null;
  }
  
  /**
   * Does this line tell you the time mode?
   */
  public static boolean isTimeModeLine(String line) {
   return line.startsWith("Time mode used is");
  }
  
  /**
   * Return the time mode from the time mode line
   */
  public static int getTimeMode(String line) {
  
   int colonIndex = line.indexOf(':');
   if (colonIndex > 0) {
    String timeModeString = line.substring(colonIndex+1).trim();
    
    if (timeModeString.startsWith("clock"))
     return FunctionCheckTraceFile.CLOCK_TIME;
    else if (timeModeString.startsWith("cpu"))
     return FunctionCheckTraceFile.CPU_TIME;
    else
     return FunctionCheckTraceFile.UNKNOWN_TIME_MODE;
     
   }
   else
    return FunctionCheckTraceFile.UNKNOWN_TIME_MODE;
  }
  
  /**
   * Does this line tell you the profile mode?
   */
  public static boolean isProfileModeLine(String line) {
   return line.startsWith("Profile mode is");
  }
  
  /**
   * Return the profile mode from the profile mode line
   */
  public static int getProfileMode(String line) {

   int colonIndex = line.indexOf(':');
   if (colonIndex > 0) {
    String profileModeString = line.substring(colonIndex+1).trim();
    
    if (profileModeString.startsWith("single"))
     return FunctionCheckTraceFile.SINGLE_PROCESS;
    else if (profileModeString.startsWith("fork"))
     return FunctionCheckTraceFile.FORK;
    else if (profileModeString.startsWith("thread"))
     return FunctionCheckTraceFile.THREAD;
    else
     return FunctionCheckTraceFile.UNKNOWN_PROFILE_MODE;
     
   }
   else
    return FunctionCheckTraceFile.UNKNOWN_PROFILE_MODE;
  
  }

  /**
   * Does this line tell you the process Id?
   */
  public static boolean isProcessIdLine(String line) {
   return line.startsWith("ID of this process");
  }
  
  /**
   * Return the process ID
   */
  public static int getProcessId(String line) throws Exception {
  
   int lastSpaceIndex = line.lastIndexOf(' ');
   if (lastSpaceIndex > 0) {
    String processIdString = line.substring(lastSpaceIndex+1).trim();
    return Integer.parseInt(processIdString);
   }
   else
    throw new PAException("Cannot get process ID");
  }
  
  /**
   * Does this line tell you the total time spent in this process?
   */
  public static boolean isTotalTimeLine(String line) {
   return line.startsWith("Total time spend in");
  }
  
  /**
   * Return the the total time spent in this process
   */
  public static double getTotalTime(String line) throws Exception {
  
   int lastSpaceIndex = line.lastIndexOf(' ');
   if (lastSpaceIndex > 0) {
    String totalTimeString = line.substring(lastSpaceIndex+1).trim();
    return Double.parseDouble(totalTimeString);
   }
   else
    throw new PAException("Cannot get total time");;
   
  }
  
  /**
   * Does this line tell you the summary of functions and call arcs?
   */
  public static boolean isSummaryLine(String line) {  
   return (Character.isDigit(line.charAt(0)) && line.endsWith("library(ies)"));
  }
  
  /**
   * Is this the first flat profile header line?
   */
  public static boolean isFlatProfileHeader1(String line) {
   line = line.trim();
   return (line.startsWith("total") && line.endsWith("function") && line.indexOf('|') > 0);
  }
  
  /**
   * Is this the second flat profile header line?
   */
  public static boolean isFlatProfileHeader2(String line) {
   line = line.trim();
   return (line.startsWith("time") && line.endsWith("name") && line.indexOf('|') > 0);
  }
  
  /**
   * Is this the third flat profile header line?
   */
  public static boolean isFlatProfileHeader3(String line) {
   line = line.trim();
   return (line.startsWith("--") && line.endsWith("--") && line.indexOf('|') > 0);
  }
  
  /**
   * Is this the call graph header line?
   */
  public static boolean isCallGraphHeader(String line) {
   return (line.equals("Call-graph:"));
  }
  
  /**
   * Is this the cycle header line?
   */
  public static boolean isCycleHeader(String line) {
   return line.startsWith("Detected cycle");
  }
  
  /**
   * Is this a nobody line?
   */
  public static boolean isNobodyLine(String line) {
   return line.trim().equals("nobody");
  }
  
  /**
   * Is this a caller line of the call graph?
   */
  public static boolean isCallerLine(String line) {
   return (line.startsWith("'") && line.endsWith("calls:"));
  }
  
  /**
   * Return the name of the caller function
   */
  public static String getCallerName(String line) {
   
   int firstIndex = line.indexOf("'");
   int lastIndex = line.lastIndexOf("'");
   if (firstIndex >= 0 && lastIndex > firstIndex) {
    return line.substring(firstIndex+1, lastIndex).trim();
   }
   else
    return null;
  }
  
}