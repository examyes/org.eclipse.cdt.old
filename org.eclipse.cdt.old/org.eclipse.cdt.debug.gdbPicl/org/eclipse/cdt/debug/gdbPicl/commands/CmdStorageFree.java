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

