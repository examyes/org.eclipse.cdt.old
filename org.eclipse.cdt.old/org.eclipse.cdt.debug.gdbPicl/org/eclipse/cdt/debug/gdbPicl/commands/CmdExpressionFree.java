//////////////////////////////////////////////////////////////////////////////////////////////////////////
//Copyright (C)  1995, 2001 International Business Machines Corporation and others. All Rights Reserved.
//////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

import java.io.*;
import com.ibm.debug.epdc.*;

/**
 * Handles Remote_ExpressionFree request
 */
public class CmdExpressionFree extends Command
{
   public CmdExpressionFree(DebugSession debugSession, EReqExpressionFree req)
   {
      super(debugSession);
      _exprID = req.exprID();
   }

   /**
    * Remove the variable monitor
    */
   public boolean execute(EPDC_EngineSession EPDCSession)
   {
      _rep = new ERepExpressionFree();

      _debugSession.getVariableMonitorManager().deleteMonitor(_exprID,true);
      return false;
   }

   // data fields
   private int _exprID;
}
