/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
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
public class CmdStorageUpdate extends Command
{
   public CmdStorageUpdate(DebugSession debugSession, EReqStorageUpdate req)
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
          Gdb.traceLogger.evt(2,"CmdStorageUpdate.execute id="+id+" address="+_req.getBaseAddress()+" lineOffset="+_req.getLineOffset()+" unitOffset="+_req.getUnitOffset()+" value="+_req.getValue() );

      GdbStorageManager sm = (GdbStorageManager) _debugSession.getStorageManager();
      sm.modifyStorage(id, _req.getBaseAddress(), _req.getLineOffset(), _req.getUnitOffset(), _req.getValue());

      _rep = new ERepStorageUpdate();

      return false;
   }

   // Class fields
   private EReqStorageUpdate _req;
}

