/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// com/ibm/debug/gdb/commands/CmdStorageUpdate.java, gdb, java-dev
// Version 1.2 (last modified 5/24/01 16:40:13)   (based on Jde 11/2/97 1.12)
///////////////////////////////////////////////////////////////////////

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

