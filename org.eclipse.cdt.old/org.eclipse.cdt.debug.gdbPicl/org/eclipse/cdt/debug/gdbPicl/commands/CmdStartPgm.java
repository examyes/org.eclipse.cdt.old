/*
 * Copyright (c) 1995, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.commands;
import org.eclipse.cdt.debug.gdbPicl.*;
import org.eclipse.cdt.debug.gdbPicl.objects.*;

import com.ibm.debug.epdc.*;

/**
 * Processes start program command
 */
public class CmdStartPgm extends Command
{
   public CmdStartPgm(DebugSession debugSession, EReqStartPgm req)
   {
      super(debugSession);
      _req = req;
   }

   /**
    * Start program and run to main
    */

   public boolean execute(EPDC_EngineSession EPDCSession)
   {

       int returnCode = EPDC.ExecRc_OK;
       String[] errorMsg = new String[1];

     _debugSession.runToMain(errorMsg);
     ThreadManager tm =_debugSession.getThreadManager();


     int DU = tm.getThreadDU(_debugSession.stopThreadName());
     int whyStop = 0;

     String msg = errorMsg[0];

     switch (_debugSession.whyStop()) {
        case DebugSession.WS_BkptHit:
           whyStop = EPDC.Why_none;
           break;

        case DebugSession.WS_PgmQuit:
           whyStop=EPDC.Why_done;
//           returnCode = EPDC.ExecRc_TeError;
           returnCode = EPDC.ExecRc_TerminateDebugger;
           break;

        case DebugSession.WS_ExceptionThrown:
           whyStop=EPDC.Why_done;
           returnCode = EPDC.ExecRc_TerminateDebugger;
           break;

        case DebugSession.WS_ThreadDeath:
           whyStop = EPDC.Why_Other;
  //         returnCode = EPDC.ExecRc_Error;
           returnCode = EPDC.ExecRc_TerminateDebugger;
           break;
     }

     _rep = new ERepStartPgm(DU, whyStop, null);
     
     if (msg != null)
     {
     	if (msg.length() > 0)
		     _rep.setMessage(msg);
     }
     _rep.setReturnCode(returnCode);

     return false;
   }

   // Class fields
   private EReqStartPgm _req;
}
