//////////////////////////////////////////////////////////////////////////////////////////////////////////
//Copyright (C)  1995, 2001 International Business Machines Corporation and others. All Rights Reserved.
//////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

import java.io.*;
import com.ibm.debug.epdc.*;

/**
 * Process monitor call stack request
 */
public class CmdStack extends Command
{
   public CmdStack(DebugSession debugSession, EReqStack req)
   {
      super(debugSession);
      _DU = req.stackDU();
   }

   /**
    * Sets up a call stack monitor on the requested thread
    */
   public boolean execute(EPDC_EngineSession EPDCSession) 
   {
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(1,"Attempting to monitor call stack for thread "+_DU);

      //_debugEngine.getThreadManager().monitorCallStack(_DU);
      GdbThreadManager tm = (GdbThreadManager) _debugSession.getThreadManager();

      tm.monitorCallStack(_DU);

      _rep = new ERepStack();

      return false;
   }

   // data fields
   private int _DU;
}
