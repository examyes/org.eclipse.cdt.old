/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// com/ibm/debug/gdb/commands/CmdStorageRangeSet2.java, gdb, java-dev
// Version 1.2 (last modified 5/24/01 16:40:12)   (based on Jde 11/2/97 1.12)
///////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

import com.ibm.debug.epdc.*;

/**
 * Process execute command
 */
public class CmdStorageRangeSet2 extends Command
{
   public CmdStorageRangeSet2(DebugSession debugSession, EReqStorageRangeSet2 req)
   {
      super(debugSession);
      _req = req;
   }

   /**
    * Execute program as specified
    */

   public boolean execute(EPDC_EngineSession EPDCSession)
   {
      int id             = _req.getID();
      int rangeEnd       = _req.getRangeEnd();
      int rangeStart     = _req.getRangeStart();
      String expr        = _req.getAddressExpr();

      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(2,"CmdStorageRangeSet2.execute id="+id+" rangeStart="+rangeStart
                    +" rangeEnd="+rangeEnd+" expr="+expr );

      GdbStorageManager sm = (GdbStorageManager) _debugSession.getStorageManager();
      sm.storageRangeSet(id, rangeStart, rangeEnd);

      _rep = new ERepStorageRangeSet2();

      return false;
   }

   // Class fields
   private EReqStorageRangeSet2 _req;
}

