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

public class CmdLocalVariableFree extends Command
{
   public CmdLocalVariableFree(DebugSession debugSession, EReqLocalVariableFree req)
   {
      super(debugSession);
      _req = req;

      _localVarDU  = req.getDU();
      _localVarStackEntryNum = req.getStackEntryNum();
   }

   public boolean execute(EPDC_EngineSession EPDCSession)
   {
      LocalVariablesMonitorManager lvmm = _debugSession.getLocalVariablesMonitorManager();
      lvmm.removeLocalVariablesMonitor(_localVarDU,_localVarStackEntryNum,true);

      _rep = new ERepLocalVariableFree();
      _rep.setReturnCode(EPDC.ExecRc_OK);

      return false;
   }

   // Data members
   private EReqLocalVariableFree _req;
   private int                   _localVarDU;
   private int                   _localVarStackEntryNum;
}
