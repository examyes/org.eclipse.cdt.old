/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.commands;
import org.eclipse.cdt.debug.gdbPicl.*;
import org.eclipse.cdt.debug.gdbPicl.objects.*;

import java.io.*;
import com.ibm.debug.epdc.*;

/**
 * Process get process details request
 */
public class CmdProcessDetailsGet extends Command
{
   public CmdProcessDetailsGet(DebugSession debugSession, EReqProcessDetailsGet req)
   {
      super(debugSession);
   }

   /**
    * Returns stack display column information
    */
   public boolean execute(EPDC_EngineSession EPDCSession)
   {
      ERepProcessDetailsGet rep = new ERepProcessDetailsGet();

      rep.addColumn(_debugSession.getResourceString("TYPE_TEXT"), EPDC.RightJustified, EPDC.LeftJustified);
      rep.addColumn(_debugSession.getResourceString("HOST_TEXT"), EPDC.RightJustified, EPDC.LeftJustified);
      rep.addColumn(_debugSession.getResourceString("PASSWORD_TEXT"), EPDC.LeftJustified, EPDC.LeftJustified);

      _rep = rep;
      return false;
   }
}
