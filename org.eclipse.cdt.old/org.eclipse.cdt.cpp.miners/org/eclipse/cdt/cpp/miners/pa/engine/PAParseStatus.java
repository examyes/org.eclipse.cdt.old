package org.eclipse.cdt.cpp.miners.pa.engine;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */


/**
 * PAParseStatus describes the current status in the parsing process.
 * From an object of this class, we will know whether we are currently
 * parsing the flat profile or call graph, or whether the parse job is done.
 */
public class PAParseStatus {

  public static int NOTYET = 0;
  public static int HEADER = 1;
  public static int PARSING = 2;
  public static int DONE = 3;
  
  private int _flatProfileStatus;
  private int _callGraphStatus;
  
  /**
   * Create a PAParseStatus object
   */
  public PAParseStatus() {
   _flatProfileStatus = NOTYET;
   _callGraphStatus = NOTYET;
  }
  
  /**
   * Return the flat profile parsing status
   */
  public int getFlatProfileStatus() {
   return _flatProfileStatus;
  }
  
  /**
   * Set the flat profile parsing status
   */
  public void setFlatProfileStatus(int flatProfileStatus) {
   _flatProfileStatus = flatProfileStatus;
  }
  
  /**
   * Return the call graph parsing status
   */
  public int getCallGraphStatus() {
   return _callGraphStatus;
  }
  
  /**
   * Set the call graph parsing status
   */
  public void setCallGraphStatus(int callGraphStatus) {
   _callGraphStatus = callGraphStatus;
  }
  
  /**
   * Is the parse done?
   * The parse is done after the flat profile and call graph are both parsed.
   */
  public boolean isAllDone() {
   return (_flatProfileStatus == DONE && _callGraphStatus == DONE);
  }
  
  /**
   * Are we parsing the flat profile?
   */
  public boolean isParsingFlatProfile() {
   return (_flatProfileStatus == PARSING);
  }

  /**
   * Are we parsing the call graph?
   */
  public boolean isParsingCallGraph() {
   return (_callGraphStatus == PARSING);
  }

}