/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

import java.io.*;
import com.ibm.debug.epdc.*;

/**
 * Handles Remote_ExpressionEnable request
 */
public class CmdExpressionEnable extends Command
{
   public CmdExpressionEnable(DebugSession debugSession, EReqExpressionEnable req)
   {
      super(debugSession);
      _exprID = req.exprID();
   }

   /**
    * Enable the variable monitor
    */
   public boolean execute(EPDC_EngineSession EPDCSession)
   {
      _rep = new ERepExpressionEnable();

      _debugSession.getVariableMonitorManager().enableMonitor(_exprID);
      return false;
   }

   // data fields
   private int _exprID;
}
