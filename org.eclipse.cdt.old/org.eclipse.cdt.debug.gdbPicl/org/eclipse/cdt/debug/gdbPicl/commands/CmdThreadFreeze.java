/*
 * Copyright (c) 1995, 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.commands;
import org.eclipse.cdt.debug.gdbPicl.*;
import org.eclipse.cdt.debug.gdbPicl.objects.*;

import java.io.*;
import com.ibm.debug.epdc.*;

/**
 * Process ThreadFreeze request
 */
public class CmdThreadFreeze extends Command
{
   public CmdThreadFreeze(DebugSession debugSession, EReqThreadFreeze req)
   {
      super(debugSession);
      _req = req;
   }

   /**
    * Attempts to freeze the thread requested
    */
   public boolean execute(EPDC_EngineSession EPDCSession)
   {
      _rep = new ERepThreadFreeze();

      ThreadManager tm = _debugSession.getThreadManager();
      tm.freezeThread(_req.getDU());

      return false;
   }

   // data fields
   private EReqThreadFreeze _req;
}
