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

