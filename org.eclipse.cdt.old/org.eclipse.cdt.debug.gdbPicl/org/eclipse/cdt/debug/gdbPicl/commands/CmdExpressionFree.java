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
// Version %I% (last modified %G% %U%)   (based on Jde 12/29/98 1.7)
///////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

import java.io.*;
import com.ibm.debug.epdc.*;

/**
 * Handles Remote_ExpressionFree request
 */
public class CmdExpressionFree extends Command
{
   public CmdExpressionFree(DebugSession debugSession, EReqExpressionFree req)
   {
      super(debugSession);
      _exprID = req.exprID();
   }

   /**
    * Remove the variable monitor
    */
   public boolean execute(EPDC_EngineSession EPDCSession)
   {
      _rep = new ERepExpressionFree();

      _debugSession.getVariableMonitorManager().deleteMonitor(_exprID,true);
      return false;
   }

   // data fields
   private int _exprID;
}
