//////////////////////////////////////////////////////////////////////////////////////////////////////////
//Copyright (C)  1995, 2001 International Business Machines Corporation and others. All Rights Reserved.
//////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

import com.ibm.debug.epdc.*;

/**
 * Handles Remote_ExpressionSubTree request
 */
public class CmdExpressionSubTree extends Command
{
   public CmdExpressionSubTree(DebugSession debugSession, EReqExpressionSubTree req)
   {
      super(debugSession);
      _req = req;
   }

   /**
    * Expand a monitor's subtree
    */
   public boolean execute(EPDC_EngineSession EPDCSession)
   {
      _rep = new ERepExpressionSubTree();
      _debugSession.getVariableMonitorManager().expandSubTree(_req.exprID(), _req.exprTreeNodeID(),
            _req.exprTreeStartChild(), _req.exprTreeEndChild());
      return false;
   }

   // data fields
   private EReqExpressionSubTree _req;
}
