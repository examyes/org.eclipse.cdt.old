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
 * Processes terminate program command
 */
public class CmdTerminatePgm extends Command
{
   public CmdTerminatePgm(DebugSession debugSession, EReqTerminatePgm req)
   {
      super(debugSession);
      _req = req;
   }

  /**
   * Terminate all program threads and clear all class and thread information
   */
   public boolean execute(EPDC_EngineSession EPDCSession)
   {
     _rep = new ERepTerminatePgm();

     if (!_debugSession.terminateDebuggee())
     {
        _rep.setReturnCode(EPDC.ExecRc_OK);
        _rep.setMessage(_debugSession.getResourceString("DEBUGGEE_GONE_MSG"));
     }

     return false;
   }

   // Class fields
   private EReqTerminatePgm _req;
}
