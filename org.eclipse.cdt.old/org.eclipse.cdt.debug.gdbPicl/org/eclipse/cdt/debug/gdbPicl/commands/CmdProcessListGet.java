/*
 * Copyright (c) 1995, 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.commands;
import org.eclipse.cdt.debug.gdbPicl.*;
import org.eclipse.cdt.debug.gdbPicl.objects.*;

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
