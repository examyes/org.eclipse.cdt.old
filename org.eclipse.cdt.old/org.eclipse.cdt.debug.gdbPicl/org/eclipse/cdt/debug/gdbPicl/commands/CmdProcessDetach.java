//////////////////////////////////////////////////////////////////////////////////////////////////////////
//Copyright (C)  1995, 2001 International Business Machines Corporation and others. All Rights Reserved.
//////////////////////////////////////////////////////////////////////////////////////////////////////////

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
