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
 * ProfTraceFunction represents a trace function in a prof trace file.
 */
public class ProfTraceFunction extends PATraceFunction {

 String _selfMsPerCall = null;

 // Constructor
  public ProfTraceFunction(PATraceFile traceFile, String name) {
   super(traceFile, name);
  }
 
public void setMsPerCall(String msecCall) {
   _selfMsPerCall = msecCall;
  }

public String getMsPerCall() {
   return _selfMsPerCall;
  }

}
