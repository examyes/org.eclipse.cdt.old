//////////////////////////////////////////////////////////////////////////////////////////////////////////
//Copyright (C)  1995, 2001 International Business Machines Corporation and others. All Rights Reserved.
//////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

import java.io.*;
import com.ibm.debug.epdc.*;

/**
 * Process ThreadFreeze request
 */
public class CmdThreadFreeze extends Command
{
   public CmdThreadFreeze(DebugSession debugSession, EReqThreadFreeze req)
   {
      super(debugSession);
      _req = req;
   }

   /**
    * Attempts to freeze the thread requested
    */
   public boolean execute(EPDC_EngineSession EPDCSession)
   {
      _rep = new ERepThreadFreeze();

      ThreadManager tm = _debugSession.getThreadManager();
      tm.freezeThread(_req.getDU());

      return false;
   }

   // data fields
   private EReqThreadFreeze _req;
}
