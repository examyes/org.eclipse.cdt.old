package org.eclipse.cdt.cpp.miners.pa.engine;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.util.*;

/**
 * PATraceFunction represents a trace function in the profile. It has
 * attributes that describe the time information (e.g. total execution
 * time, self execution time). 
 * A trace function knows its callers and callees.
 */
public class PATraceFunction {
  
  // Attributes for a trace function
  private String  _name;
  private String  _mangledName;
  private String  _sourceLocation;
  private int     _callNumber;
  private double  _selfSeconds;
  private double  _totalSeconds;
  private double  _selfMsPerCall;
  private double  _totalMsPerCall;
  private double  _totalPercentage;
  private boolean _isRecursive;
  private boolean _isCyclic;
  private boolean _hasSummary;
  
  // The trace file this function belongs to
  private PATraceFile _traceFile;
  
  // Caller and callee lists
  private ArrayList _callers;
  private ArrayList _callees;
  
  
  // constructors
  public PATraceFunction(PATraceFile traceFile) {
   _traceFile = traceFile;
   _selfSeconds = 0;
   _totalSeconds = 0;
   _callNumber = 0;
   _selfMsPerCall = 0;
   _totalMsPerCall = 0;
   _totalPercentage = 0;
   _isRecursive = false;
   _isCyclic = false;
   _hasSummary = false;
   
   _callers = new ArrayList();
   _callees = new ArrayList();   
  }
  
  public PATraceFunction(PATraceFile traceFile, String name) {
   this(traceFile);   
   _name = name;
  }
  
  public PATraceFunction(PATraceFile traceFile, String name, int callNumber, double selfSeconds) {
   this(traceFile, name);
   _callNumber = callNumber;
   _selfSeconds = selfSeconds;
  }
  
  // Add a caller
  public void addCaller(PACallArc callArc) {   
    _callers.add(callArc);    
  }
  
  // Add a callee
  public void addCallee(PACallArc callArc) {
  
    _callees.add(callArc);
    if (this == callArc.getCallee()) {
     _isRecursive = true;
     _isCyclic = true;
    }
  }
  
  // Return the number of callers
  public int getNumberOfCallers() {
   return _callers.size();
  }
  
  // Return the number of callees
  public int getNumberOfCallees() {
   return _callees.size();
  }
  
  // Return the caller for a given index
  public PATraceFunction getCaller(int index) {
   
   if (index < _callers.size())
    return ((PACallArc)_callers.get(index)).getCaller();
   else
    return null;
    
  }
  
  // Return the callee for a given index
  public PATraceFunction getCallee(int index) {
  
   if (index < _callees.size())
    return ((PACallArc)_callees.get(index)).getCallee();
   else
    return null;
   
  }
  
  // Is this trace function a caller of the given trace function?
  public boolean isCallerOf(PATraceFunction traceFunc) {
   
   int numberOfCallees = _callees.size();
   for (int i=0; i < numberOfCallees; i++) {
    if (getCallee(i) == traceFunc)
     return true;
   }
   return false;
  }
  
  // Is this trace function a callee of the given trace function?
  public boolean isCalleeOf(PATraceFunction traceFunc) {

   int numberOfCallers = _callers.size();
   for (int i=0; i < numberOfCallers; i++) {
    if (getCaller(i) == traceFunc)
     return true;
   }
   return false;
  
  }
  
  /**
   * Is this a top level trace function?
   * A top level function does not have any caller.
   */
  public boolean isTopLevelFunction() {
   return _callers.isEmpty();
  }
  
  /**
   * Is this a leaf trace function?
   * A leaf function does not have any callee.
   */
  public boolean isLeafFunction() {
   return _callees.isEmpty();
  }
  
  // setter methods
  public void setName(String name) {
    _name = name;
  }

  public void setMangledName(String mangledName) {
   _mangledName = mangledName;
  }
    
  public void setCallNumber(int callNumber) {
   _callNumber = callNumber;
  }
  
  public void setSelfSeconds(double selfSeconds) {
   _selfSeconds = selfSeconds;
  }
  
  public void setSelfMsPerCall(double selfTime) {    
   _selfMsPerCall = selfTime * (1.0e6 / _traceFile.getTimeUnit());   
  }
  
  public void setTotalSeconds(double totalSeconds) {
   _totalSeconds = totalSeconds;
  }
  
  public void setTotalMsPerCall(double totalTime) {
   _totalMsPerCall = totalTime * (1.0e6 / _traceFile.getTimeUnit());
  }
  
  public void setTotalPercentage(double totalPercentage) {
   _totalPercentage = totalPercentage;
  }
    
  public void setSourceLocation(String sourceLocation) {
   _sourceLocation = sourceLocation;
  }
  
  public void setRecursive(boolean isRecursive) {
   _isRecursive = isRecursive;
  }

  public void setCyclic(boolean isCyclic) {
   _isCyclic = isCyclic;
  }
  
  public void setHasSummary(boolean hasSummary) {
   _hasSummary = hasSummary;
  }
  
  // getter methods
  public String getName() {
   return _name;
  }
  
  public String getMangledName() {
   return _mangledName;
  }
  
  public PATraceFile getTraceFile() {
   return _traceFile;
  }
  
  public double getSelfSeconds() {
   return _selfSeconds;
  }

  public double getTotalSeconds() {
   return _totalSeconds;
  }
  
  public int getCallNumber() {
   return _callNumber;
  }
  
  public double getSelfMsPerCall() {
   return _selfMsPerCall;
  }

  public double getTotalMsPerCall() {
   return _totalMsPerCall;
  }

  public double getTotalPercentage() {
   return _totalPercentage;
  }
    
  public String getSourceLocation() {
   return _sourceLocation;
  }
  
  public boolean isRecursive() {
   return _isRecursive;
  }
  
  public boolean isCyclic() {
   return _isCyclic;
  }
  
  public boolean hasSummary() {
   return _hasSummary;
  }
  
  public ArrayList getCallers() {
   return _callers;
  }
  
  public ArrayList getCallees() {
   return _callees;
  }

}