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
 * Handles set expression representation type request
 */
public class CmdExpressionRepTypeSet extends Command
{
   public CmdExpressionRepTypeSet(DebugSession debugSession, EReqExpressionRepTypeSet req)
   {
      super(debugSession);
      _req = req;
   }

   /**
    * Set the new representation type
    */
   public boolean execute(EPDC_EngineSession EPDCSession)
   {
      _rep = new ERepExpressionRepTypeSet();

      VariableMonitorManager vmm = _debugSession.getVariableMonitorManager();
      // Note: The repType here is an index based at 1
      vmm.setRepresentation(_req.exprID(), _req.nodeID(), _req.newRepType());

      return false;
   }

   // data fields
   private EReqExpressionRepTypeSet _req;
}

