/*
 * Copyright (c) 1995, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.commands;
import org.eclipse.cdt.debug.gdbPicl.*;
import org.eclipse.cdt.debug.gdbPicl.objects.*;

import java.io.*;
import com.ibm.debug.epdc.*;

/**
 * Process free stack request
 */
public class CmdStackFree extends Command
{
   public CmdStackFree(DebugSession debugSession, EReqStackFree req)
   {
      super(debugSession);
      _stackDU = req.stackDU();
   }

   /**
    * Frees call stack monitor on requested thread
    */
   public boolean execute(EPDC_EngineSession EPDCSession)
   {
      _debugSession.getThreadManager().freeCallStackMonitor(_stackDU);

      _rep = new ERepStackFree();

      return false;
   }

   // data fields
   private int _stackDU;
}
