/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.commands;
import org.eclipse.cdt.debug.gdbPicl.*;
import org.eclipse.cdt.debug.gdbPicl.objects.*;

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
