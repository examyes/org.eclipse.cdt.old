/*
 * Copyright (c) 1995, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.commands;
import org.eclipse.cdt.debug.gdbPicl.*;
import org.eclipse.cdt.debug.gdbPicl.objects.*;

import java.io.*;
import com.ibm.debug.epdc.*;

/**
 * Process location breakpoint request
 */
public class CmdBreakpointEvent extends Command
{
  DebugEngine _debugEngine = null;
  public CmdBreakpointEvent(DebugSession debugSession, EReqBreakpointEvent req)
  {
    super(debugSession);
    _debugEngine = debugSession.getDebugEngine();
    _req = req;
  }

  /**
   * Processes command and updates breakpoints as necessary
   */
  public boolean execute(EPDC_EngineSession EPDCSession)
  {
    BreakpointManager bm = _debugSession.getBreakpointManager();

    _rep = new ERepBreakpointEvent();

    switch (_req.bkpType()) 
    {
      case EPDC.ChangeAddrBkpType:
        ExprEvalInfo evalInfo = null;
        String exprString = null;
        EStdView context = null;

        try
        {
          exprString = _req.bkpVarInfo();
          context = _req.bkpContext();
          String threadName = _debugSession.stopThreadName();
          int du = _debugSession.getThreadManager().getThreadDU(threadName);

          VariableMonitorManager varMonMgr =
              _debugSession.getVariableMonitorManager();

          evalInfo =
              varMonMgr.evaluateExpression(exprString, context, du, true);
        }
        catch (Exception excp)
        {
          _rep.setReturnCode(EPDC.ExecRc_BadExpr);
          _rep.setMessage(
              _debugEngine.getResourceString("EXPRESSION_EVAL_FAILED_MSG"));
          return false;
        }
        if (evalInfo != null && evalInfo.expressionFailed())
        {
          _rep.setReturnCode(EPDC.ExecRc_BadExpr);
          _rep.setMessage(evalInfo.whyFailed(_debugEngine));
          return false;
        }

        int ret = 0;
        boolean enable = (_req.bkpAttr() & EPDC.BkpEnable) == EPDC.BkpEnable;
        if (_req.bkpAction() == EPDC.ReplaceBkp)
        {
           ret = bm.modifyWatchpoint(_req.bkpID(), exprString, evalInfo,
                                    _req.byteCount(), context, enable);
        }
        else
        {
          if (Gdb.traceLogger.DBG) 
              Gdb.traceLogger.dbg(2,"CmdBreakpointEvent setWatchpoint expr="+exprString);
          ret = bm.setWatchpoint(exprString, evalInfo, _req.byteCount(),
                                 context, enable);
        }

        int rc = EPDC.ExecRc_OK;
        String msg = null;
        switch(ret)
        {
          case -1:
            rc = EPDC.ExecRc_BadExpr;
            msg = _debugEngine.getResourceString("EXPRESSION_EVAL_FAILED_MSG");

            if (_req.bkpAction() == EPDC.ReplaceBkp)  // UNIMPLEMENTED modify Watchpoint
            {
               if (Gdb.traceLogger.ERR) 
                  Gdb.traceLogger.err(2,"######## UNIMPLEMENTED modify Watchpoint");
               rc = EPDC.ExecRc_BadBrkType;
               msg = _debugEngine.getResourceString("UNSUPPORTED_BREAKPOINT_TYPE_MSG");
            }

            break;
          case 0:
            rc = EPDC.ExecRc_OK;
            break;
          default:
            rc = EPDC.ExecRc_DupBrkPt;
            msg = _debugEngine.getResourceString("DUPLICATE_BREAKPOINT");
            break;
        }

        _rep.setReturnCode(rc);
        if (msg != null)
          _rep.setMessage(msg);
         
        return false;

      case EPDC.LoadBkpType:
		String dllName = null;
		context = null;

		try {
			dllName = _req.bkpVarInfo();
			context = _req.bkpContext();
			boolean isEnabled =
				((_req.bkpAttr() & EPDC.BkpEnable) == EPDC.BkpEnable) ? true : false;

			ret = ((GdbBreakpointManager) bm).setLoadBreakpoint(dllName, isEnabled);

			rc = EPDC.ExecRc_OK;
			msg = null;
			switch (ret) {
				case 0 :
					rc = EPDC.ExecRc_OK;
					break;
				default :
					rc = EPDC.ExecRc_DupBrkPt;
					msg = _debugEngine.getResourceString("DUPLICATE_BREAKPOINT");
					break;
			}

			_rep.setReturnCode(rc);
			if (msg != null)
				_rep.setMessage(msg);
			return false;				
		} catch (Exception excp) {
			_rep.setReturnCode(EPDC.ExecRc_BadExpr);
			_rep.setMessage(_debugEngine.getResourceString("EXPRESSION_EVAL_FAILED_MSG"));
			return false;
		}
		//	      _rep.setReturnCode(EPDC.ExecRc_BadBrkType);
		//	      _rep.setMessage("Load Breakpoint not supported");

		default:
         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(2,"Unknown breakpoint type="+_req.bkpType() );
        _rep.setReturnCode(EPDC.ExecRc_BadBrkType);
        _rep.setMessage(_debugEngine.getResourceString("UNSUPPORTED_BREAKPOINT_TYPE_MSG"));
        return false;
    }
  }

  // data fields
  private EReqBreakpointEvent _req;
}
