//////////////////////////////////////////////////////////////////////////////////////////////////////////
//Copyright (C)  1995, 2001 International Business Machines Corporation and others. All Rights Reserved.
//////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

import com.ibm.debug.epdc.*;

/**
 * Process clear breakpoint request
 */
public class CmdBreakpointClear extends Command
{
   public CmdBreakpointClear(DebugSession debugSession, EReqBreakpointClear req)
   {
      super(debugSession);
      _bkpID = req.bkpID();
   }

   /**
    * Clear specified breakpoint
    */
   public boolean execute(EPDC_EngineSession EPDCSession)
   {
      BreakpointManager bm = _debugSession.getBreakpointManager();

      if (_bkpID > 0) {
         if (Gdb.traceLogger.DBG) 
             Gdb.traceLogger.dbg(1,"Clearing breakpoint " + Integer.toString(_bkpID));
         bm.clearBreakpoint(_bkpID);
      }
      else {
         if (Gdb.traceLogger.DBG) 
             Gdb.traceLogger.dbg(1,"Clearing all breakponts");
         bm.clearAllBreakpoints();
      }

      _rep = new ERepBreakpointClear();
      return false;
   }

   // data fields
   private int _bkpID;
}
