/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

import com.ibm.debug.epdc.*;

/**
 * Process Get Context Entry Point command
 */
public class CmdContextQualGet extends Command
{
   public CmdContextQualGet(DebugSession debugSession, EReqContextQualGet req)
   {
      super(debugSession);
      _req = req;
   }

   /**
    * Use the given Context to look up the entryID and return it to the FE.
    */
   public boolean execute(EPDC_EngineSession EPDCSession)
   {
     ModuleManager cm       = _debugSession.getModuleManager();
     int          PPID     = _req.context().getPPID();
     int          lineNum  = _req.context().getLineNum();
     int[]        entryIDs = new int[1];

     entryIDs[0] = cm.getEntryID(PPID, lineNum);
    
     _rep = new ERepContextQualGet(entryIDs);

     return false;
   }

   // data fields
   private EReqContextQualGet _req;
}
