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
 * Handles Remote_ExpressionFree request
 */
public class CmdExpressionFree extends Command
{
   public CmdExpressionFree(DebugSession debugSession, EReqExpressionFree req)
   {
      super(debugSession);
      _exprID = req.exprID();
   }

   /**
    * Remove the variable monitor
    */
   public boolean execute(EPDC_EngineSession EPDCSession)
   {
      _rep = new ERepExpressionFree();

      _debugSession.getVariableMonitorManager().deleteMonitor(_exprID,true);
      return false;
   }

   // data fields
   private int _exprID;
}
