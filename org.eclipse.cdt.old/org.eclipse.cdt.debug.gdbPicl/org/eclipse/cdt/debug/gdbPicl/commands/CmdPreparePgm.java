/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// com/ibm/debug/gdb/commands/CmdPreparePgm.java, gdb, java-dev
// Version 1.1 (last modified 5/24/01 13:44:41)   (based on Jde 1.20 2/28/01)
/////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

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
