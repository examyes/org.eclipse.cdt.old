/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// com/ibm/debug/gdb/commands/CmdVerifyView.java, gdb, java-dev
// Version 1.1 (last modified 5/24/01 13:45:09)   (based on Jde 12/29/98 1.9)
///////////////////////////////////////////////////////////////////////

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
