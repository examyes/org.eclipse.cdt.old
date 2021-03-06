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
public class CmdStorageEnablementSet extends Command
{
   public CmdStorageEnablementSet(DebugSession debugSession, EReqStorageEnablementSet req)
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
          Gdb.traceLogger.evt(2,"CmdStorageEnablement.execute id="+id );

      int e = _req.getEnablementFlags();
      if(e != 0) // EPDC.StorageEnabled ||  EPDC.StorageExprEnabled
      {
         GdbStorageManager sm = (GdbStorageManager) _debugSession.getStorageManager();
         sm.updateStorage(id);
      }

      _rep = new ERepStorageEnablementSet();

      return false;
   }

   // Class fields
   private EReqStorageEnablementSet _req;
}

