/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.commands;
import org.eclipse.cdt.debug.gdbPicl.*;
import org.eclipse.cdt.debug.gdbPicl.objects.*;

import java.io.*;
import com.ibm.debug.epdc.*;

/**
 * Process ThreadThaw request
 */
public class CmdThreadThaw extends Command
{
   public CmdThreadThaw(DebugSession debugSession, EReqThreadThaw req)
   {
      super(debugSession);
      _req = req;
   }

   /**
    * Attempts to freeze the thread requested
    */
   public boolean execute(EPDC_EngineSession EPDCSession)
   {
      _rep = new ERepThreadThaw();

      ThreadManager tm = _debugSession.getThreadManager();
      tm.thawThread(_req.getDU());

      return false;
   }

   // data fields
   private EReqThreadThaw _req;
}
