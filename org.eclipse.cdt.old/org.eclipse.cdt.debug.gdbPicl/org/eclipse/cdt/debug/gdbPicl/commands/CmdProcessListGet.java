/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// com/ibm/debug/gdb/commands/CmdProcessListGet.java, gdb, java-dev
// Version 1.1 (last modified 5/24/01 13:44:45)   (based on Jde 3/9/01 1.2)
////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

import java.io.*;
import com.ibm.debug.epdc.*;

/**
 * Process get process details request
 */
public class CmdProcessListGet extends Command
{
   public CmdProcessListGet(DebugSession debugSession, EReqProcessListGet req)
   {
      super(debugSession);
   }

   /**
    * Returns stack display column information
    */
   public boolean execute(EPDC_EngineSession EPDCSession)
   {
      // 3 Columns
      ERepProcessListGet rep = new ERepProcessListGet(3);

      DebugEngine _debugEngine = _debugSession.getDebugEngine();
/*
      for (int i=0;i < _debugEngine.numHosts(); i++)
      {
         String[] process = new String[3];
         process[0] = _debugSession.getResourceString("REMOTE_JVM_TEXT");
         process[1] = _debugEngine.getHost(i);
         process[2] = _debugEngine.getPassword(i);
         rep.addProcess(process);
      }
*/
      _rep = rep;
      return false;
   }
}
