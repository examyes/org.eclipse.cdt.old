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
 * Local Variable Command
 */

public class CmdLocalVariable extends Command
{
   public CmdLocalVariable(DebugSession debugSession, EReqLocalVariable req)
   {
      super(debugSession);
      _req = req;

      _localVarDU  = req.getDU();
      _localVarStackEntryNum = req.getStackEntryNum();
   }

   public boolean execute(EPDC_EngineSession EPDCSession)
   {
      int DU = _req.getDU();
      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(1,"========>>>>>>>> CmdLocalVariable DU="+DU );

      LocalVariablesMonitorManager lvmm = _debugSession.getLocalVariablesMonitorManager();
      lvmm.addLocalVariablesMonitor(_localVarDU,_localVarStackEntryNum);
      lvmm.updateLocalMonitors();

      _rep = new ERepLocalVariable();
      _rep.setReturnCode(EPDC.ExecRc_OK);

      return false;
   }

   // Data members
   private EReqLocalVariable _req;
   private int               _localVarDU;
   private int               _localVarStackEntryNum;
}
