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

