//////////////////////////////////////////////////////////////////////////////////////////////////////////
//Copyright (C)  1995, 2001 International Business Machines Corporation and others. All Rights Reserved.
//////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

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
