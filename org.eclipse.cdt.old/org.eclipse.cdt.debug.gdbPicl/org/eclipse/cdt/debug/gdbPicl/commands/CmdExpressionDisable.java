/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.commands;
import org.eclipse.cdt.debug.gdbPicl.*;
import org.eclipse.cdt.debug.gdbPicl.objects.*;

import java.io.*;
import com.ibm.debug.epdc.*;

/**
 * Handles Remote_ExpressionDisable request
 */
public class CmdExpressionDisable extends Command
{
   public CmdExpressionDisable(DebugSession debugSession, EReqExpressionDisable req)
   {
      super(debugSession);
      _exprID = req.exprID();
   }

   /**
    * Disable the variable monitor
    */
   public boolean execute(EPDC_EngineSession EPDCSession)
   {
      _rep = new ERepExpressionDisable();

      _debugSession.getVariableMonitorManager().disableMonitor(_exprID);
      return false;
   }

   // data fields
   private int _exprID;
}
