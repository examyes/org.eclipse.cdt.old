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
// Version %I% (last modified %G% %U%)   (based on Jde 10/27/97 1.0)
///////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

import java.io.*;
import com.ibm.debug.epdc.*;

/**
 * Process ThreadFreeze request
 */
public class CmdThreadFreeze extends Command
{
   public CmdThreadFreeze(DebugSession debugSession, EReqThreadFreeze req)
   {
      super(debugSession);
      _req = req;
   }

   /**
    * Attempts to freeze the thread requested
    */
   public boolean execute(EPDC_EngineSession EPDCSession)
   {
      _rep = new ERepThreadFreeze();

      ThreadManager tm = _debugSession.getThreadManager();
      tm.freezeThread(_req.getDU());

      return false;
   }

   // data fields
   private EReqThreadFreeze _req;
}
