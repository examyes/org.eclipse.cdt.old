/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// com/ibm/debug/gdb/commands/CmdLocalVariable.java, gdb, java-dev
// Version 1.1 (last modified 5/24/01 13:44:36)   (based on Jde 1.8)
///////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

import com.ibm.debug.epdc.*;
import java.io.*;

/**
 * Local Variable Command
 */

public class CmdLocalVariable extends Command
{
   public CmdLocalVariable(DebugSession debugSession, EReqLocalVariable req)
   {
      super(debugSession);
      _req = req;

      _localVarDU  = req.getDU();
      _localVarStackEntryNum = req.getStackEntryNum();
   }

   public boolean execute(EPDC_EngineSession EPDCSession)
   {
      int DU = _req.getDU();
      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(1,"========>>>>>>>> CmdLocalVariable DU="+DU );

      LocalVariablesMonitorManager lvmm = _debugSession.getLocalVariablesMonitorManager();
      lvmm.addLocalVariablesMonitor(_localVarDU,_localVarStackEntryNum);
      lvmm.updateLocalMonitors();

      _rep = new ERepLocalVariable();
      _rep.setReturnCode(EPDC.ExecRc_OK);

      return false;
   }

   // Data members
   private EReqLocalVariable _req;
   private int               _localVarDU;
   private int               _localVarStackEntryNum;
}
