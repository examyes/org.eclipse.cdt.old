/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// %W%
// Version %I% (last modified %G% %U%)   (based on Jde 1.13 2/14/01)
/////////////////////////////////////////////////////////////////////////

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
