package org.eclipse.cdt.cpp.miners.pa.engine.functioncheck;


import java.util.*;
import org.eclipse.cdt.cpp.miners.pa.engine.*;

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