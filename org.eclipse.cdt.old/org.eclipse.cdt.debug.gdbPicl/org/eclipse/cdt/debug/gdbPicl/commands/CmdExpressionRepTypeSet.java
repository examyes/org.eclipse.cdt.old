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

