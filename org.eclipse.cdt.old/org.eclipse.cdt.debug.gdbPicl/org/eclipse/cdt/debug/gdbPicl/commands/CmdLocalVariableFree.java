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
// Version %I% (last modified %G% %U%)   (based on Jde 1.7)
///////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

import com.ibm.debug.epdc.*;
import java.io.*;

/**
 * Local Variable Command
 */

public class CmdLocalVariableFree extends Command
{
   public CmdLocalVariableFree(DebugSession debugSession, EReqLocalVariableFree req)
   {
      super(debugSession);
      _req = req;

      _localVarDU  = req.getDU();
      _localVarStackEntryNum = req.getStackEntryNum();
   }

   public boolean execute(EPDC_EngineSession EPDCSession)
   {
      LocalVariablesMonitorManager lvmm = _debugSession.getLocalVariablesMonitorManager();
      lvmm.removeLocalVariablesMonitor(_localVarDU,_localVarStackEntryNum,true);

      _rep = new ERepLocalVariableFree();
      _rep.setReturnCode(EPDC.ExecRc_OK);

      return false;
   }

   // Data members
   private EReqLocalVariableFree _req;
   private int                   _localVarDU;
   private int                   _localVarStackEntryNum;
}
