//////////////////////////////////////////////////////////////////////////////////////////////////////////
//Copyright (C)  1995, 2001 International Business Machines Corporation and others. All Rights Reserved.
//////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

import java.io.*;
import com.ibm.debug.epdc.*;

/**
 * Process free stack request
 */
public class CmdStackFree extends Command
{
   public CmdStackFree(DebugSession debugSession, EReqStackFree req)
   {
      super(debugSession);
      _stackDU = req.stackDU();
   }

   /**
    * Frees call stack monitor on requested thread
    */
   public boolean execute(EPDC_EngineSession EPDCSession)
   {
      _debugSession.getThreadManager().freeCallStackMonitor(_stackDU);

      _rep = new ERepStackFree();

      return false;
   }

   // data fields
   private int _stackDU;
}
