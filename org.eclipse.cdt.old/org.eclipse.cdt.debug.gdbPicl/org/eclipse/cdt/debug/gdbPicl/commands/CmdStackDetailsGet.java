/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

import java.io.*;
import com.ibm.debug.epdc.*;

/**
 * Process get stack details request
 */
public class CmdStackDetailsGet extends Command
{
   public CmdStackDetailsGet(DebugSession debugSession, EReqStackDetailsGet req)
   {
      super(debugSession);
   }

   /**
    * Returns stack display column information
    */
   public boolean execute(EPDC_EngineSession EPDCSession)
   {

      ERepStackDetailsGet rep = new ERepStackDetailsGet();

      rep.addColumn(_debugSession.getResourceString("ENTRY_TEXT"), EPDC.RightJustified, EPDC.RightJustified);
      rep.addColumn(_debugSession.getResourceString("METHOD_TEXT"), EPDC.LeftJustified, EPDC.LeftJustified);
//GDB      rep.addColumn(_debugSession.getResourceString("CLASS_TEXT"), EPDC.LeftJustified, EPDC.LeftJustified);
      rep.addColumn("File", EPDC.LeftJustified, EPDC.LeftJustified);
//GDB      rep.addColumn(_debugSession.getResourceString("Address"), EPDC.LeftJustified, EPDC.LeftJustified);
      rep.addColumn("Address", EPDC.LeftJustified, EPDC.LeftJustified);

      int[] defCols = {2,3,4};

      rep.setDefaultColumns(defCols);
      _rep = rep;
      return false;
   }
}
