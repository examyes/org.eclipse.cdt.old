//////////////////////////////////////////////////////////////////////////////////////////////////////////
//Copyright (C)  1995, 2001 International Business Machines Corporation and others. All Rights Reserved.
//////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

import com.ibm.debug.epdc.*;

/**
 * Process emable breakpoint request
 */
public class CmdBreakpointEnable extends Command
{
   public CmdBreakpointEnable(DebugSession debugSession, EReqBreakpointEnable req)
   {
      super(debugSession);
      _bkpID = req.bkpID();
   }


   /**
    * Enable specified breakpoint
    */
   public boolean execute(EPDC_EngineSession EPDCSession)
   {
      BreakpointManager bm = _debugSession.getBreakpointManager();

      if (_bkpID > 0) 
      {
         if (Gdb.traceLogger.DBG) 
             Gdb.traceLogger.dbg(1,"Enabling breakpoint " + Integer.toString(_bkpID));
         bm.enableBreakpoint(_bkpID);
      }
      else
      {
         if (Gdb.traceLogger.DBG) 
             Gdb.traceLogger.dbg(1,"Enabling all breakpoints.");
         bm.enableAllBreakpoints();
      }

      _rep = new ERepBreakpointEnable();
      return false;
   }

   // data fields
   private int _bkpID;
}
