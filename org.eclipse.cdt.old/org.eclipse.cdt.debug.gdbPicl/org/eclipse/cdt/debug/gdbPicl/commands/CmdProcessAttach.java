/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// com/ibm/debug/gdb/commands/CmdProcessAttach.java, gdb, java-dev
// Version 1.1 (last modified 5/24/01 13:44:42)   (based on Jde 1.10 2/28/01)
/////////////////////////////////////////////////////////////////////////

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

     if (!_debugSession.remoteAttach(processIndex, errorMsg))
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
                               processIndex,
                               tm.getThreadDU(_debugSession.stopThreadName()),
                               EPDC.Why_ProcessChanged,
                               "Remote JVM",
                               "Remote JVM");

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

     return false;
  }

  // Data members
  private EReqProcessAttach _req;
}
