//////////////////////////////////////////////////////////////////////////////////////////////////////////
//Copyright (C)  1995, 2001 International Business Machines Corporation and others. All Rights Reserved.
//////////////////////////////////////////////////////////////////////////////////////////////////////////

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
