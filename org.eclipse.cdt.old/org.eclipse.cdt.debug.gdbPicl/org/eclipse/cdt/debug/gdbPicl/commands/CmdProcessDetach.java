/*
 * Copyright (c) 1995, 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.commands;
import org.eclipse.cdt.debug.gdbPicl.*;
import org.eclipse.cdt.debug.gdbPicl.objects.*;

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
