//////////////////////////////////////////////////////////////////////////////////////////////////////////
//Copyright (C)  1995, 2001 International Business Machines Corporation and others. All Rights Reserved.
//////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

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
