/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

import com.ibm.debug.epdc.*;

/**
 * Handles Remote_ExpressionSubTreeDelete request
 */
public class CmdExpressionSubTreeDelete extends Command
{
   public CmdExpressionSubTreeDelete(DebugSession debugSession, EReqExpressionSubTreeDelete req)
   {
      super(debugSession);
      _req = req;
   }

   /**
    * Collapse a monitor's subtree
    */
   public boolean execute(EPDC_EngineSession EPDCSession)
   {
      _rep = new ERepExpressionSubTreeDelete();
      _debugSession.getVariableMonitorManager().collapseSubTree(_req.exprID(), _req.exprTreeNodeID(),
            _req.exprTreeStartChild(), _req.exprTreeEndChild());
      return false;
   }

   // data fields
   private EReqExpressionSubTreeDelete _req;
}
