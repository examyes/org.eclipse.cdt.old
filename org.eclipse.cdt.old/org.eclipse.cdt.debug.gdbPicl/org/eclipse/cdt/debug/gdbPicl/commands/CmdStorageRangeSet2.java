/*
 * Copyright (c) 1995, 2001, 2002 International Business Machines Corporation. All rights reserved.
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

