/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// com/ibm/debug/gdb/commands/CmdBreakpointEnable.java, gdb, java-dev
// Version 1.1 (last modified 5/24/01 13:44:20)   (based on Jde 12/29/98 1.7)
///////////////////////////////////////////////////////////////////////

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
