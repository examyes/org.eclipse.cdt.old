/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// com/ibm/debug/gdb/commands/CmdThreadInfoGet.java, gdb, java-dev
// Version 1.1 (last modified 5/24/01 13:45:06)   (based on Jde 3/9/01 1.0)
///////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

import java.io.*;
import com.ibm.debug.epdc.*;

/**
 *
 */
public class CmdThreadInfoGet extends Command
{
   public CmdThreadInfoGet(DebugSession debugSession, EReqThreadInfoGet req)
   {
      super(debugSession);
      _req = req;
   }

   /**
    * Sets the source file to the given file name, if it exists
    */
   public boolean execute(EPDC_EngineSession EPDCSession)
   {
     _rep = new ERepThreadInfoGet();

     int du = _req.getDU();
       if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(1,"========>>>>>>>> CmdThreadInfoGet DU="+du );
    _debugSession.getThreadManager().updateThread(du);
 
     return false;
   }

   // Data fields
   private EReqThreadInfoGet _req;
}
