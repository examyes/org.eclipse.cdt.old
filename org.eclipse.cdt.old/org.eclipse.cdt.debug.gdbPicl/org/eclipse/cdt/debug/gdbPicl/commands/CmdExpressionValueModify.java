//////////////////////////////////////////////////////////////////////////////////////////////////////////
//Copyright (C)  1995, 2001 International Business Machines Corporation and others. All Rights Reserved.
//////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

import com.ibm.debug.epdc.*;

/**
 * Handles Remote_ExpressionValueModify request
 */
public class CmdExpressionValueModify extends Command
{
   public CmdExpressionValueModify(DebugSession debugSession, EReqExpressionValueModify req)
   {
      super(debugSession);
      _req = req;
   }

   /**
    * Collapse a monitor's subtree
    */
   public boolean execute(EPDC_EngineSession EPDCSession)
   {
      ExprEvalInfo evalInfo;

      _rep = new ERepExpressionValueModify();

//      VariableMonitorManager vmm = _debugEngine.getVariableMonitorManager();
      GdbVariableMonitorManager vmm = (GdbVariableMonitorManager)_debugSession.getVariableMonitorManager(); //GDB

      try {
          if (Gdb.traceLogger.DBG) 
              Gdb.traceLogger.dbg(1,"CmdExpressionValueModify.execute exprID="+_req.exprID()
                 +" exprTreeNode="+_req.exprTreeNode() +" newValue="+_req.reqNewNodeValue() );
          vmm.modifyMonitor(_req.exprID(), _req.reqNewNodeValue() );
/*
          evalInfo = vmm.setValue(_req.exprID(), 
                                  _req.exprTreeNode(), 
                                  _req.reqNewNodeValue());
          if(evalInfo == null) {
              _rep.setReturnCode(EPDC.ExecRc_Error);
              _rep.setMessage(_debugEngine.getResourceString("EXPRESSION_EVAL_FAILED_MSG"));
          } else if(evalInfo.expressionFailed()) {
              _rep.setReturnCode(EPDC.ExecRc_BadExpr);
              _rep.setMessage(evalInfo.whyFailed(_debugEngine));
          }
*/
      } catch (Exception e) {
          _rep.setReturnCode(EPDC.ExecRc_Error);
          String msg = e.getMessage();
          if (msg != null && msg.length() > 0)
          {   _rep.setMessage(msg);
              if (Gdb.traceLogger.EVT) 
                  Gdb.traceLogger.evt(1,"CmdExpressionValueModify.execute exception msg="+msg );
          }
      }
      return false;
   }

   // data fields
   private EReqExpressionValueModify _req;
}
