//////////////////////////////////////////////////////////////////////////////////////////////////////////
//Copyright (C)  1995, 2001 International Business Machines Corporation and others. All Rights Reserved.
//////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

import com.ibm.debug.epdc.*;

/**
 * Process execute command
 */
public class CmdRegistersDetailsGet extends Command
{
   public CmdRegistersDetailsGet(DebugSession debugSession, EReqRegistersDetailsGet req)
   {
      super(debugSession);
      _req = req;
   }

   /**
    * Gets the requested Register Details
    */

   public boolean execute(EPDC_EngineSession EPDCSession)
   {

      _rep = new ERepRegistersDetailsGet();
      ((ERepRegistersDetailsGet)_rep).addGroup("General");
      ((ERepRegistersDetailsGet)_rep).addGroup("Float");
      int[] iArray = { 1, 2};
      ((ERepRegistersDetailsGet)_rep).setDefaultGroups(iArray);


      return false;
   }

   // Class fields
   private EReqRegistersDetailsGet _req;
}

