/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// com/ibm/debug/gdb/commands/CmdStorageEnablementSet.java, gdb, java-dev
// Version 1.1 (last modified 5/24/01 13:44:57)   (based on Jde 11/2/97 1.12)
///////////////////////////////////////////////////////////////////////

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

