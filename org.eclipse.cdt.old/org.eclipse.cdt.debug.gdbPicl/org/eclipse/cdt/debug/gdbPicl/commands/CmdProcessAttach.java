/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

import com.ibm.debug.epdc.*;
import java.io.*;

/**
 * Processes Process Attach request.
 */
public class CmdProcessAttach extends Command
{

   public CmdProcessAttach(DebugSession debugSession, EReqProcessAttach req)
   {
      super(debugSession);
      _req = req;
   }

  public boolean execute(EPDC_EngineSession EPDCSession)
  {
     String[] errorMsg = new String[1];
     ThreadManager tm = _debugSession.getThreadManager();

     // PLEASE NOTE: We are treating the processId as a process index here.
     // Our DebugSession object maintains an artificial process list.  Since
     // this process id is specified from the command line invocation of
     // the front end, when debugging java we simply pass in a value of 0
     // to indicate we wish to attach to the first (and probably only)
     // remote JVM we specified when we started up the backend.

     int processIndex = _req.processId();
     String progName = "";
     try
     {
        progName = _req.processPath();
     }
     catch (IOException ioe)
     {
     	System.out.println("CmdProcessAttach.execute() IOException = " + ioe);
     }

     if (Gdb.traceLogger.DBG)
        Gdb.traceLogger.dbg(2,"CmdProcessAttach.execute() calling remoteAttach() with processIndex = " + processIndex + "  progName = " + progName);

     if (!_debugSession.remoteAttach(processIndex, progName, errorMsg))
     {
        _rep = new ERepProcessAttach(EPDCSession,
                                  new EStdTime(0,0,0),
                                  new EStdDate(0,0,0),
                                  progName,
                                  processIndex,
                                  1,
                                  EPDC.Why_ProcessChanged,
                                  null,
                                  progName);
        // !!! Anything other than ExecRc_ProgName will kill SUI.  Why?
        _rep.setReturnCode(EPDC.ExecRc_ProgName);
        _rep.setMessage(errorMsg[0]);
        return false;
     }

     /* Attach succeeded ... good. */
     _rep = new ERepProcessAttach(EPDCSession,
                               new EStdTime(0,0,0),
                               new EStdDate(0,0,0),
                               progName,
                               processIndex,
                               tm.getThreadDU(_debugSession.stopThreadName()),
                               EPDC.Why_ProcessChanged,
                               null,
                               progName);

     // Set watchpoint FCT bits if supported
     if (_debugSession.modificationWatchpointsSupported())
     {
        int options =
           EPDCSession._functCustomTable.getBreakpointCapabilities();
        if (EPDCSession._negotiatedEPDCVersion >= 307)
        {
           options = options | EPDC.FCT_CHANGE_ADDRESS_BREAKPOINT;
        }
        else
        {
           options = options | EPDC.FCT_CHANGE_ADDRESS_BREAKPOINT |
                     EPDC.FCT_BREAKPOINT_MONITOR_1BYTES |
                     EPDC.FCT_BREAKPOINT_MONITOR_2BYTES |
                     EPDC.FCT_BREAKPOINT_MONITOR_4BYTES |
                     EPDC.FCT_BREAKPOINT_MONITOR_8BYTES;
        }
        EPDCSession._functCustomTable.setBreakpointCapabilities(options);
     }

     // Turn off process attach support. NOTE: This is a temporary fix for
     // Component Broker (see defect 8787 in the debugger family). Ultimately,
     // we probably don't want to be disabling process attach after doing
     // an attach i.e. under normal circumstances it should be possible for
     // the user to choose another process to attach to. TF

     EPDCSession._functCustomTable.setProcessAttachSupported(false);
     _rep.addFCTChangePacket(new ERepGetFCT(EPDCSession._functCustomTable));

     return true;
  }

  // Data members
  private EReqProcessAttach _req;
}
