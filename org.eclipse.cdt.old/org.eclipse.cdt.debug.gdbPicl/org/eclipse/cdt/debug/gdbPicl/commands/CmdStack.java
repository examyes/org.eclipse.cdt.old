/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// com/ibm/debug/gdb/commands/CmdStack.java, gdb, java-dev
// Version 1.1 (last modified 5/24/01 13:44:51)   (based on Jde 12/29/98 1.7)
///////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

import java.io.*;
import com.ibm.debug.epdc.*;

/**
 * Process monitor call stack request
 */
public class CmdStack extends Command
{
   public CmdStack(DebugSession debugSession, EReqStack req)
   {
      super(debugSession);
      _DU = req.stackDU();
   }

   /**
    * Sets up a call stack monitor on the requested thread
    */
   public boolean execute(EPDC_EngineSession EPDCSession) 
   {
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(1,"Attempting to monitor call stack for thread "+_DU);

      //_debugEngine.getThreadManager().monitorCallStack(_DU);
      GdbThreadManager tm = (GdbThreadManager) _debugSession.getThreadManager();

      tm.monitorCallStack(_DU);

      _rep = new ERepStack();

      return false;
   }

   // data fields
   private int _DU;
}
