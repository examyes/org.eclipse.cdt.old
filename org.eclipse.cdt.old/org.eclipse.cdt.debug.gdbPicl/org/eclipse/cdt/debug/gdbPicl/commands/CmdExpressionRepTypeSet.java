//////////////////////////////////////////////////////////////////////////////////////////////////////////
//Copyright (C)  1995, 2001 International Business Machines Corporation and others. All Rights Reserved.
//////////////////////////////////////////////////////////////////////////////////////////////////////////

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

