/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// com/ibm/debug/gdb/commands/CmdProcessAttach2.java, gdb, java-dev
// Version 1.1 (last modified 5/24/01 13:44:42)   (based on Jde 1.9 2/28/01)
/////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

import com.ibm.debug.epdc.*;
import java.io.*;

/**
 * Processes Process Attach2 request.
 */
public class CmdProcessAttach2 extends Command
{

   public CmdProcessAttach2(DebugSession debugSession, EReqProcessAttach2 req)
   {
      super(debugSession);
      _req = req;
   }

   public boolean execute(EPDC_EngineSession EPDCSession)
   {
     String[] errorMsg = new String[1];
     ThreadManager tm = _debugSession.getThreadManager();

     // Note: The processIndices returned from SUI are based at 1. Our internal
     // array is based at 0

     if (!_debugSession.remoteAttach(_req.processIndex()-1, errorMsg))
     {
        _rep = new ERepProcessAttach(EPDCSession,
                                  new EStdTime(0,0,0),
                                  new EStdDate(0,0,0),
                                  _debugSession.getResourceString("REMOTE_JVM_TEXT"),
                                  1,
                                  1,
                                  EPDC.Why_ProcessChanged,
                                  "Remote JVM",
                                  "Remote JVM");
        // !!! Anything other than ExecRc_ProgName will kill SUI.  Why?
        _rep.setReturnCode(EPDC.ExecRc_ProgName);
        _rep.setMessage(errorMsg[0]);
        return false;
     }

     /* Attach succeeded ... good. */
     _rep = new ERepProcessAttach(EPDCSession,
                               new EStdTime(0,0,0),
                               new EStdDate(0,0,0),
                               _debugSession.getResourceString("REMOTE_JVM_TEXT"),
                               _req.processIndex()-1,
                               tm.getThreadDU(_debugSession.stopThreadName()),
                               EPDC.Why_ProcessChanged,
                               "Remote JVM",
                               "Remote JVM");

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

     return false;
  }

  // Data members
  private EReqProcessAttach2 _req;
}
