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

import com.ibm.debug.epdc.*;

/**
 * Handles Remote_ExpressionSubTree request
 */
public class CmdExpressionSubTree extends Command
{
   public CmdExpressionSubTree(DebugSession debugSession, EReqExpressionSubTree req)
   {
      super(debugSession);
      _req = req;
   }

   /**
    * Expand a monitor's subtree
    */
   public boolean execute(EPDC_EngineSession EPDCSession)
   {
      _rep = new ERepExpressionSubTree();
      _debugSession.getVariableMonitorManager().expandSubTree(_req.exprID(), _req.exprTreeNodeID(),
            _req.exprTreeStartChild(), _req.exprTreeEndChild());
      return false;
   }

   // data fields
   private EReqExpressionSubTree _req;
}
