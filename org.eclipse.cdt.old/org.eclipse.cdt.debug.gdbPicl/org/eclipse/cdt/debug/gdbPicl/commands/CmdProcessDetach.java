/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// com/ibm/debug/gdb/commands/CmdProcessDetach.java, gdb, java-dev
// Version 1.1 (last modified 5/24/01 13:44:43)   (based on Jde 11/11/97 1.12)
///////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

import com.ibm.debug.epdc.*;
import java.io.*;

/**
 * Processes Process Detach request.
 */
public class CmdProcessDetach extends Command
{

   public CmdProcessDetach(DebugSession debugSession, EReqProcessDetach req)
   {
      super(debugSession);
      _req = req;
   }

  public boolean execute(EPDC_EngineSession EPDCSession)
  {
     String[] errorMsg = new String[1];

     if (!_debugSession.remoteDetach(_req.processId(), _req.processDetachAction(), 
        errorMsg))
     {
        _rep = new ERepProcessDetach(EPDC.Why_ProcessChanged, "Detach");
        _rep.setReturnCode(EPDC.ExecRc_ProgName);
        _rep.setMessage(errorMsg[0]);
        return false;
     }

     /* Detach succeeded ... good. */
     _rep = new ERepProcessDetach(EPDC.Why_ProcessChanged, "Detach");

     addChangePackets();
     return false;
  }

  // Data members
  private EReqProcessDetach _req;
}
