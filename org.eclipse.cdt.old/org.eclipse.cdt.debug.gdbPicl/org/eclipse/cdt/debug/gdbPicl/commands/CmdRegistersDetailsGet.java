/*
 * Copyright (c) 1995, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.commands;
import org.eclipse.cdt.debug.gdbPicl.*;
import org.eclipse.cdt.debug.gdbPicl.objects.*;

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

