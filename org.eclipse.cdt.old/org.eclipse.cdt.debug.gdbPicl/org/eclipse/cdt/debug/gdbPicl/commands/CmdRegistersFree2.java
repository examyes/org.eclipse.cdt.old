/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

import com.ibm.debug.epdc.*;

/**
 * Process execute command
 */
public class CmdRegistersFree2 extends Command
{
   public CmdRegistersFree2(DebugSession debugSession, EReqRegistersFree2 req)
   {
      super(debugSession);
      _req = req;
   }

   /**
    * Sets up a registers monitor on the requested thread
    */

   public boolean execute(EPDC_EngineSession EPDCSession)
   {

      _rep = new ERepRegistersFree2();
      int DU = _req.registersDU();
      int groupID = _req.groupID();

      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(1,"Attempting to free registers for thread="+DU+" groupID="+groupID );

      GdbRegisterManager rm = (GdbRegisterManager) _debugSession.getRegisterManager();
      rm.freeRegisters(DU, groupID);

      return false;
   }

   // Class fields
   private EReqRegistersFree2 _req;
}

