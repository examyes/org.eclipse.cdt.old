//////////////////////////////////////////////////////////////////////////////////////////////////////////
//Copyright (C)  1995, 2001 International Business Machines Corporation and others. All Rights Reserved.
//////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

import java.io.*;
import com.ibm.debug.epdc.*;

/**
 * Process ThreadThaw request
 */
public class CmdThreadThaw extends Command
{
   public CmdThreadThaw(DebugSession debugSession, EReqThreadThaw req)
   {
      super(debugSession);
      _req = req;
   }

   /**
    * Attempts to freeze the thread requested
    */
   public boolean execute(EPDC_EngineSession EPDCSession)
   {
      _rep = new ERepThreadThaw();

      ThreadManager tm = _debugSession.getThreadManager();
      tm.thawThread(_req.getDU());

      return false;
   }

   // data fields
   private EReqThreadThaw _req;
}
