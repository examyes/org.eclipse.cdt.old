/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// %W%
// Version %I% (last modified %G% %U%)   (based on Jde 12/29/98 1.6)
///////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

import java.io.*;
import com.ibm.debug.epdc.*;

/**
 * Process free stack request
 */
public class CmdStackFree extends Command
{
   public CmdStackFree(DebugSession debugSession, EReqStackFree req)
   {
      super(debugSession);
      _stackDU = req.stackDU();
   }

   /**
    * Frees call stack monitor on requested thread
    */
   public boolean execute(EPDC_EngineSession EPDCSession)
   {
      _debugSession.getThreadManager().freeCallStackMonitor(_stackDU);

      _rep = new ERepStackFree();

      return false;
   }

   // data fields
   private int _stackDU;
}
