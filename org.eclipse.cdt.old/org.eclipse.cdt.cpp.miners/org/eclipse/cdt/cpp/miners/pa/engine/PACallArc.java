package org.eclipse.cdt.cpp.miners.pa.engine;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */


/**
 * PACallArc represents a call arc in the call graph.
 * The call arc contains information about which caller calls which callee, the
 * number of calls and time information.
 */
public class PACallArc {

  private PATraceFunction _caller;
  private PATraceFunction _callee;
  private int _callNumber;
  private double _selfTime;
  private double _childrenTime;
  
  // constructor
  public PACallArc(PATraceFunction caller, PATraceFunction callee) {
   _caller = caller;
   _callee = callee;
  }

  public PACallArc(PATraceFunction caller, PATraceFunction callee, int callNumber, double selfTime, double childrenTime) {
   _caller = caller;
   _callee = callee;
   _callNumber = callNumber;
   _selfTime = selfTime;
   _childrenTime = childrenTime;
  }
  
  // setter methods
  public void setCallNumber(int callNumber) {
   _callNumber = callNumber;
  }
  
  public void setSelfTime(double selfTime) {
   _selfTime = selfTime;
  }
  
  public void setChildrenTime(double childrenTime) {
   _childrenTime = childrenTime;
  }
  
  public void setCaller(PATraceFunction caller) {
   _caller = caller;
  }
  
  public void setCallee(PATraceFunction callee) {
   _callee = callee;
  }
  
  // getter methods
  public PATraceFunction getCaller() {
   return _caller;
  }

  public PATraceFunction getCallee() {
   return _callee;
  }

  public int getCallNumber() {
   return _callNumber;
  }
  
  public double getSelfTime() {
   return _selfTime;
  }

  public double getChildrenTime() {
   return _childrenTime;
  } 
}