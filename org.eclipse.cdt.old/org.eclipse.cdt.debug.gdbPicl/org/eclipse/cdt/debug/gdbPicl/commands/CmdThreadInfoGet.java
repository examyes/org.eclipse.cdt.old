//////////////////////////////////////////////////////////////////////////////////////////////////////////
//Copyright (C)  1995, 2001 International Business Machines Corporation and others. All Rights Reserved.
//////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

import java.io.*;
import com.ibm.debug.epdc.*;

/**
 *
 */
public class CmdThreadInfoGet extends Command
{
   public CmdThreadInfoGet(DebugSession debugSession, EReqThreadInfoGet req)
   {
      super(debugSession);
      _req = req;
   }

   /**
    * Sets the source file to the given file name, if it exists
    */
   public boolean execute(EPDC_EngineSession EPDCSession)
   {
     _rep = new ERepThreadInfoGet();

     int du = _req.getDU();
       if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(1,"========>>>>>>>> CmdThreadInfoGet DU="+du );
    _debugSession.getThreadManager().updateThread(du);
 
     return false;
   }

   // Data fields
   private EReqThreadInfoGet _req;
}
