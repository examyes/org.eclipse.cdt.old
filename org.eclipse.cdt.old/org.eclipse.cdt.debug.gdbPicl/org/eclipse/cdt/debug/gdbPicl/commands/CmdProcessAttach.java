/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.commands;
import org.eclipse.cdt.debug.gdbPicl.*;
import org.eclipse.cdt.debug.gdbPicl.objects.*;

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

     int processIndex = _req.processId();
     String filename = "";
     try
     {
        filename = _req.processPath();
     }
     catch (IOException ioe)
     {
     	System.out.println("CmdProcessAttach.execute() IOException = " + ioe);
     }

     if (Gdb.traceLogger.DBG)
        Gdb.traceLogger.dbg(2,"CmdProcessAttach.execute() processIndex = " + processIndex + "  filename = " + filename);
    
     
     if (!_debugSession.remoteAttach(processIndex, filename, errorMsg))
     {
        _rep = new ERepProcessAttach(EPDCSession,
                                  new EStdTime(0,0,0),
                                  new EStdDate(0,0,0),
                                  filename,
                                  processIndex,
                                  1,
                                  EPDC.Why_ProcessChanged,
                                  null,
                                  filename);
        // !!! Anything other than ExecRc_ProgName will kill SUI.  Why?
        _rep.setReturnCode(EPDC.ExecRc_ProgName);
        _rep.setMessage(errorMsg[0]);
        return false;
     }

     /* Attach succeeded ... good. */
     _rep = new ERepProcessAttach(EPDCSession,
                               new EStdTime(0,0,0),
                               new EStdDate(0,0,0),
                               filename,
                               processIndex,
                               tm.getThreadDU(_debugSession.stopThreadName()),
                               EPDC.Why_ProcessChanged,
                               null,
                               filename);

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

     _rep.addFCTChangePacket(new ERepGetFCT(EPDCSession._functCustomTable));

     return true;
  }

  // Data members
  private EReqProcessAttach _req;
}
