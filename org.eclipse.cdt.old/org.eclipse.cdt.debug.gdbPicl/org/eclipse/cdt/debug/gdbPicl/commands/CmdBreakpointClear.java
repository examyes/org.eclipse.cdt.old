/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// com/ibm/debug/gdb/commands/CmdBreakpointClear.java, gdb, java-dev
// Version 1.1 (last modified 5/24/01 13:44:18)   (based on Jde 12/29/98 1.8)
///////////////////////////////////////////////////////////////////////

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
