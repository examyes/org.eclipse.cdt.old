//////////////////////////////////////////////////////////////////////////////////////////////////////////
//Copyright (C)  1995, 2001 International Business Machines Corporation and others. All Rights Reserved.
//////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

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
     _debugSession.runToMain();
     ThreadManager tm =_debugSession.getThreadManager();
     

     int DU = tm.getThreadDU(_debugSession.stopThreadName());
     int whyStop = 0;

     switch (_debugSession.whyStop()) {
        case DebugSession.WS_BkptHit:
           whyStop = EPDC.Why_none;
           break;

        case DebugSession.WS_PgmQuit:
           whyStop=EPDC.Why_done;
           returnCode = EPDC.ExecRc_Error;
           break;

        case DebugSession.WS_ExceptionThrown:
           whyStop=EPDC.Why_done;
           returnCode = EPDC.ExecRc_Error;
           break;

        case DebugSession.WS_ThreadDeath:
           whyStop = EPDC.Why_Other;
           returnCode = EPDC.ExecRc_Error;
           break;
     }

     _rep = new ERepStartPgm(DU, whyStop, null);
     _rep.setReturnCode(returnCode);

     return false;
   }

   // Class fields
   private EReqStartPgm _req;
}
