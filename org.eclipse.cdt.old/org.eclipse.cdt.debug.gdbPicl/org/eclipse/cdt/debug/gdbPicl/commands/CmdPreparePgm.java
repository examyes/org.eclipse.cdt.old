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
 * Processes Prepare Program request.
 */

public class CmdPreparePgm extends Command
{
   public CmdPreparePgm(DebugSession debugSession, EReqPreparePgm req)
   {
      super(debugSession);
      _req = req;
   }

   public boolean execute(EPDC_EngineSession EPDCSession)
   {
     String programName;

     try {
           programName = _req.reqPgmName();
        
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(1,"Attempting to find program: " + programName);

	String[] errorMsg = new String[1];
        if (!_debugSession.setStartProgramName(programName, _req.reqPgmParms(), errorMsg)) {
           _rep = new ERepPreparePgm(EPDCSession, 
				     new EStdTime(0,0,0), 
				     new EStdDate(0,0,0),
				     null, 
				     null, 
				     1, 
				     null);
           _rep.setReturnCode(EPDC.ExecRc_ProgName);
           _rep.setMessage(errorMsg[0]);
           _debugSession.clearManagers();
           return false;
        }

        /* Program was found... good. */
        _rep = new ERepPreparePgm(EPDCSession, 
				  new EStdTime(0,0,0), 
				  new EStdDate(0,0,0),
				  "localhost", 
				  programName, 
				  1, 
				  programName);

        /* Set watchpoint FCT bits if supported */
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
           _rep.addFCTChangePacket(new ERepGetFCT(EPDCSession._functCustomTable));
        }

     } catch (IOException ioe) {
        Gdb.handleException(ioe);
     }
     return false;
  }

  // Data members
  private EReqPreparePgm _req;
}
