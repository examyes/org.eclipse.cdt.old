//////////////////////////////////////////////////////////////////////////////////////////////////////////
//Copyright (C)  1995, 2001 International Business Machines Corporation and others. All Rights Reserved.
//////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;
//package com.ibm.debug.gdbPicl.commands.commands;
import  com.ibm.debug.gdbPicl.*;

import com.ibm.debug.epdc.*;

/**
 * Processes terminate debug engine command
 */
public class CmdTerminateDE extends Command
{
   public CmdTerminateDE(DebugSession debugSession, EReqTerminateDE req)
   {
      super(debugSession);
      _req = req;
   }

   /**
    * Any cleaning up should be done here
    */

   public boolean execute(EPDC_EngineSession EPDCSession)
   {
     _rep = new ERepTerminateDE();

     return true;
   }

   // Class fields
   private EReqTerminateDE _req;
}
