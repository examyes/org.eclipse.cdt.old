/*
 * Copyright (c) 1995, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.commands;
import org.eclipse.cdt.debug.gdbPicl.*;
import org.eclipse.cdt.debug.gdbPicl.objects.*;

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
