package org.eclipse.cdt.pa.ui.api;
 
/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.pa.ui.PAPlugin;

public class PAResource {

  private static PAResource _instance = new PAResource();
  
  // Trace type constants
  public static int TRACE_FILE = 0;
  public static int TRACE_PROGRAM = 1;
  
  // Trace format constants
  public static final int AUTO = 0;
  public static final int GPROF_GNU = 1;
  public static final int GPROF_BSD = 2;
  public static final int GPROF_ALL = 3;
  public static final int FUNCTIONCHECK = 4;
  public static final int INVALID = -1;
  
  // constructor
  private PAResource() { }
  
  public static PAResource getInstance() {
    return _instance;
  }
  
  
  public static String traceFormatToString(int format) {
  
    String formatStr = "";
    
    switch (format) {
           
      case GPROF_GNU:
       formatStr = "gprof_gnu";
       break;
      
      case GPROF_BSD:
       formatStr = "gprof_bsd";
       break;
       
      case FUNCTIONCHECK:
       formatStr = "functioncheck";
       break;
       
      default:
       formatStr = "auto";
       break;
    }
    
    return formatStr;
  }
  
  
  public static String getLocalizedString(String key) {
    return PAPlugin.getDefault().getLocalizedString(key);
  }
  
}