//////////////////////////////////////////////////////////////////////////////////////////////////////////
//Copyright (C)  1995, 2001 International Business Machines Corporation and others. All Rights Reserved.
//////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

import com.ibm.debug.epdc.*;

/**
 * Process execute command
 */
public class CmdRegisters2 extends Command
{
   public CmdRegisters2(DebugSession debugSession, EReqRegisters2 req)
   {
      super(debugSession);
      _req = req;
   }

   /**
    * Sets up a registers monitor on the requested thread
    */

   public boolean execute(EPDC_EngineSession EPDCSession)
   {

      _rep = new ERepRegisters2();
      int DU = _req.registersDU();
      int groupID = _req.groupID();

      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(1,"Attempting to monitor registers for thread="+DU+" groupID="+groupID );

      GdbRegisterManager rm = (GdbRegisterManager) _debugSession.getRegisterManager();
      rm.monitorRegisters(DU, groupID);

      return false;
   }

   // Class fields
   private EReqRegisters2 _req;
}

