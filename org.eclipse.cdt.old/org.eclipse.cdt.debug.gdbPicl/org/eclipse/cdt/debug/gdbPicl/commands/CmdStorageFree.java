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
 * Process execute command
 */
public class CmdStorageFree extends Command
{
   public CmdStorageFree(DebugSession debugSession, EReqStorageFree req)
   {
      super(debugSession);
      _req = req;
   }

   /**
    * Execute program as specified
    */

   public boolean execute(EPDC_EngineSession EPDCSession)
   {
      short id               = _req.getID();
      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(2,"CmdStorageFree.execute id="+id );
      GdbStorageManager sm = (GdbStorageManager) _debugSession.getStorageManager();
      sm.freeStorage(id);

      _rep = new ERepStorageFree();

      return false;
   }

   // Class fields
   private EReqStorageFree _req;
}

