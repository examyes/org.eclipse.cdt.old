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
 * Handles Remote_ExpressionDisable request
 */
public class CmdExpressionDisable extends Command
{
   public CmdExpressionDisable(DebugSession debugSession, EReqExpressionDisable req)
   {
      super(debugSession);
      _exprID = req.exprID();
   }

   /**
    * Disable the variable monitor
    */
   public boolean execute(EPDC_EngineSession EPDCSession)
   {
      _rep = new ERepExpressionDisable();

      _debugSession.getVariableMonitorManager().disableMonitor(_exprID);
      return false;
   }

   // data fields
   private int _exprID;
}
