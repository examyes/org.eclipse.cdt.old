package org.eclipse.cdt.cpp.miners.pa.engine.functioncheck;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.util.*;
import org.eclipse.cdt.cpp.miners.pa.engine.*;

/**
 * FunctionCheckTraceFunction represents a trace function in a FunctionCheck trace file.
 * In addition to the attributes in PATraceFunction, a FunctionCheckTraceFunction has
 * the following additional attributes:
 *
 * - Min total time
 * - Max total time
 * - Min local time
 * - Max local time
 */
public class FunctionCheckTraceFunction extends PATraceFunction {

  private double _selfPercentage = 0;
  private double _minTotalTime = 0;
  private double _maxTotalTime = 0;
  private double _minLocalTime = 0;
  private double _maxLocalTime = 0;
  
  // Constructor
  public FunctionCheckTraceFunction(PATraceFile traceFile, String name) {
   super(traceFile, name);
  }
  
  public FunctionCheckTraceFunction(PATraceFile traceFile, String name, int callNumber, double selfSeconds) {
   super(traceFile, name, callNumber, selfSeconds);
  }
  
  // getter methods
  public double getSelfPercentage() {
   return _selfPercentage;
  }
  
  public double getMinTotalTime() {
   return _minTotalTime;
  }
  
  public double getMaxTotalTime() {
   return _maxTotalTime;
  }
  
  public double getMinLocalTime() {
   return _minLocalTime;
  }

  public double getMaxLocalTime() {
   return _maxLocalTime;
  }
  
  // setter methods
  public void setSelfPercentage(double selfPercentage) {
   _selfPercentage = selfPercentage;
  }
  
  public void setMinTotalTime(double minTotalTime) {
   _minTotalTime = minTotalTime;
  }
  
  public void setMaxTotalTime(double maxTotalTime) {
   _maxTotalTime = maxTotalTime;
  }
  
  public void setMinLocalTime(double minLocalTime) {
   _minLocalTime = minLocalTime;
  }
  
  public void setMaxLocalTime(double maxLocalTime) {
   _maxLocalTime = maxLocalTime;
  }
  
}