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
public class CmdTypesNumGet extends Command
{
   public CmdTypesNumGet(DebugSession debugSession, EReqTypesNumGet req)
   {
      super(debugSession);
      _req = req;
   }

   /**
    * Execute program as specified
    */

   public boolean execute(EPDC_EngineSession EPDCSession)
   {
      // !!! Do we need to check the language id?  We only support one.
      // _req.languageId();

      _rep = EPDCSession._repInfo;
      return false;
   }

   // Class fields
   private EReqTypesNumGet _req;
}

