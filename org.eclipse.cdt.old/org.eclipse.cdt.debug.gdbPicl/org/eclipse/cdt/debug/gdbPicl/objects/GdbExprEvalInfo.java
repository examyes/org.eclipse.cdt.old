/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package com.ibm.debug.gdbPicl.objects;
import  com.ibm.debug.gdbPicl.*;

//import sun.tools.debug.*;

/**
 * Holds expression evaluation status code and diagnostic information.
 */
public class GdbExprEvalInfo extends ExprEvalInfo            //HC
{
  public GdbExprEvalInfo(int status)
  {  this(status, null, -1, WF_Unknown);        }
  public GdbExprEvalInfo(int status, String diagInfo)
  {  this(status, diagInfo, -1, WF_Unknown);    }
  public GdbExprEvalInfo(int status, String diagInfo, int stackEntry, int whereFound)
  {
    super(status, diagInfo, stackEntry, whereFound);	  //HC
  }

  public boolean expressionFailed()
  {
     if( _status != exprVARIABLEFOUND)
     if (Gdb.traceLogger.EVT) 
         Gdb.traceLogger.evt(1,"GdbExprEvalInfo: expressionFailed _status="+_status );
     return _status != exprVARIABLEFOUND;
  }

  public String whyFailed(DebugEngine debugEngine)
  {
	  Gdb.debugOutput("GdbExprEvalInfo: whyFailed()");
     String message = "EXPRESSION_EVAL_FAILED_MSG";

     String msg;

     msg = debugEngine.getResourceString(message);

     if (_diagInfo != null)
        msg += " " + _diagInfo;

     return msg;
  }
  public void setInitialValue(String s)
  {  _initialValue = s;    }
  public String getInitialValue()
  {  return _initialValue; }
  String _initialValue                  = null;

  public void setType(String s)
  {  _type = s;    }
  public String getType()
  {  return _type; }
  String _type                          = null;

  public void setNodeID(int i)
  {  _nodeID = i;    }
  public int getNodeID()
  {  return _nodeID; }
  int _nodeID                           = -1;

  public final static int exprVARIABLEFOUND    = 1;
  public final static int exprNOTFOUND         = 0;
  public final static int exprNULLVALUE        = -1;
  public final static int exprINVALID          = -4;
  public final static int exprFAILED           = -6;
 
  // "Where Found" constants
  final static int WF_Unknown     = 0;
}
