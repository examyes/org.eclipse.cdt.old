/*
 * Copyright (c) 1995, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.commands;
import org.eclipse.cdt.debug.gdbPicl.*;
import org.eclipse.cdt.debug.gdbPicl.objects.*;

import java.io.*;
import com.ibm.debug.epdc.*;

/**
 * Process verify view request
 */
public class CmdVerifyView extends Command
{
   public CmdVerifyView(DebugSession debugSession, EReqVerifyViews req)
   {
      super(debugSession);
      _req = req;
   }

   /**
    * Attempts to verify the requested part
    */
   public boolean execute(EPDC_EngineSession EPDCSession)
   {
      _rep = new ERepVerifyViews();

      ModuleManager moduleManager = _debugSession.getModuleManager();

      Part part = moduleManager.getPart(_req.partID());

      if (part != null)
      {
         part.verifyViews();
      }

      return false;
   }

   // data fields
   private EReqVerifyViews _req;
}
