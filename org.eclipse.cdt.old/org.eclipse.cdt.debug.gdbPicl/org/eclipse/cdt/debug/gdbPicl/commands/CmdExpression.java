/*
 * Copyright (c) 1995, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.commands;
import org.eclipse.cdt.debug.gdbPicl.*;
import org.eclipse.cdt.debug.gdbPicl.objects.*;

import java.io.*;
import java.util.*;
import com.ibm.debug.epdc.*;

/**
 * Handles Remote_Expression request
 */
public class CmdExpression extends Command
{
  DebugEngine _debugEngine = null;
  public CmdExpression(DebugSession debugSession, EReqExpression req)
  {
    super(debugSession);
    _debugEngine = debugSession.getDebugEngine();
    _req = req;
  }

  /**
   * Attempt to create a variable monitor
   */
  public boolean execute(EPDC_EngineSession EPDCSession) 
  {
    // Get expression to be evaluated.
    EStdExpression2 expr;
    String          exprString;
    short           monType;
    int             du;
    EStdView        context;
    
    ExprEvalInfo    evalInfo;
    String          msg;
    
    _rep = new ERepExpression();
    
    try {
      expr       = _req.getExpression();
      monType    = _req.getMonType();
      exprString = expr.getExprString();
      context    = expr.getContext();
      du         = expr.getExprDU();
    }
    catch (Exception excp) {
      evalInfo = new GdbExprEvalInfo(GdbExprEvalInfo.exprFAILED, "");
      msg = evalInfo.whyFailed(_debugEngine);
      _rep.setMessage(msg);
      _rep.setReturnCode(EPDC.ExecRc_BadExpr);
      if (Gdb.traceLogger.ERR) 
          Gdb.traceLogger.err(3,"%%%%%%%%%%%%%%%% CmdExpression exception="+excp );
      return false;
    }
    try {
      VariableMonitorManager varMonMgr = 
	_debugSession.getVariableMonitorManager();

      // For deferred expressions, build context information
      if (_req.isDeferred()) {
      	
 		// do not support deferred expression for now
      	msg = "Deferred Expression not supported";
		_rep.setMessage(msg);
		_rep.setReturnCode(EPDC.ExecRc_BadExpr);
		return false;     
      }
      
      if (_req.isDeferred())
      {
	// Extract these separately as they may be null
	String partNameES = _req.getPartName();
	String moduleNameES = _req.getModuleName();
	
	// Determine part ID by looking up module and part name
        String partName = "";

	if (moduleNameES != null)
        { 
           String moduleName = moduleNameES;

//           if (moduleName != null && moduleName.length() != 0 &&
//               !moduleName.equals(_debugEngine.getResourceString(
//               "DEFAULT_PACKAGE_TEXT")))
//	      partName = moduleName + ".";
        }

	if (partNameES != null) {
	  partName = partName + partNameES;

    int moduleID = ((GdbModuleManager)_debugSession.getModuleManager()).getModuleID(moduleNameES); //GDB

	  short ppid = 
//	    (short) _debugEngine.getClassManager().getPartID(partName);
	    (short) ((GdbModuleManager)_debugSession.getModuleManager()).getPartID(moduleID,partName); //GDB

     if(ppid<=0)
     {   Part part = _debugSession.isPartInModule(partName, moduleNameES);
         if(part!=null)
         {    ppid = (short) ((GdbModuleManager)_debugSession.getModuleManager()).getPartID(moduleID,partName);
              if (Gdb.traceLogger.EVT) 
                  Gdb.traceLogger.evt(2,"CmdExpression.execute added part for monitor, moduleName="+moduleNameES+" partName="+partName+" partID="+ppid+", expr="+exprString  );
              if(context!=null)
              {
                 context.setPPID(ppid);
              }
         }
         else
         {    if (Gdb.traceLogger.ERR) 
                  Gdb.traceLogger.err(2,"CmdExpression.execute FAILED to find part for monitor(MUST ADD DEFER CAPABILITY), moduleName="+moduleNameES+" partName="+partName+" partID="+ppid+", expr="+exprString  );
         }
     }

	  context = new EStdView(ppid, 
				 (short) 1,          // SrcFileIndex
				 Part.VIEW_SOURCE,   // View 
				 context.getLineNum());
          
          if (ppid <= 0)
          {
            varMonMgr.addDeferredExpression(monType, exprString, partName,
                                            context, du);
            varMonMgr.addChangesToReply(_rep);
            return false;
          }
	}
      }
      
	evalInfo =varMonMgr.addExpression(monType,exprString,context,du,_req.isDeferred());
      
	if (evalInfo.expressionFailed() && !_req.isDeferred()) {

		msg = evalInfo.whyFailed(_debugEngine);
		
		exprString = "$" + exprString;

		// if failed, try registers
		evalInfo =
			varMonMgr.addExpression(
				monType,
				exprString,
				context,
				du,
				_req.isDeferred());

		if (evalInfo.expressionFailed() && !_req.isDeferred()) {
			_rep.setMessage(msg);
			_rep.setReturnCode(EPDC.ExecRc_BadExpr);
			return false;
		}
	}       
      varMonMgr.addChangesToReply(_rep);
      
      return false;
    }
    catch (Exception excp) {
      Gdb.handleException(excp);
      evalInfo =  
	new GdbExprEvalInfo(GdbExprEvalInfo.exprFAILED, 
			     "Invalid Expression");
      msg = evalInfo.whyFailed(_debugEngine);
      _rep.setMessage(msg);
      _rep.setReturnCode(EPDC.ExecRc_BadExpr);
      return false;
    }
  }

  // data fields
  private EReqExpression _req;
}
