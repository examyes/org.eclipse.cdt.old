/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

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
