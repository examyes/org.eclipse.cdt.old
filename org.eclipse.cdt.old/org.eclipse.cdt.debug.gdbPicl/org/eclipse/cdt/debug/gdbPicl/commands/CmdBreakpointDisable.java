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
 * Process disable breakpoint request
 */
public class CmdBreakpointDisable extends Command
{
   public CmdBreakpointDisable(DebugSession debugSession, EReqBreakpointDisable req)
   {
      super(debugSession);
      _bkpID = req.bkpID();
   }

   /**
    * Disable specified breakpoint
    */
   public boolean execute(EPDC_EngineSession EPDCSession)
   {
      BreakpointManager bm = _debugSession.getBreakpointManager();

      if (_bkpID > 0)
      {
         if (Gdb.traceLogger.DBG) 
             Gdb.traceLogger.dbg(1,"Disabling breakpoint " + Integer.toString(_bkpID));
         bm.disableBreakpoint(_bkpID);
      }
      else
      {
         if (Gdb.traceLogger.DBG) 
             Gdb.traceLogger.dbg(1,"Disabling all breakpoints.");
         bm.disableAllBreakpoints();
      }

      _rep = new ERepBreakpointDisable();
      return false;
   }

   // data fields
   private int _bkpID;
}
