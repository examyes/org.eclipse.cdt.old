/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// com/ibm/debug/gdb/commands/CmdBreakpointDisable.java, gdb, java-dev
// Version 1.1 (last modified 5/24/01 13:44:19)   (based on Jde 12/29/98 1.7)
///////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

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
