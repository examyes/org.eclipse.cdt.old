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
import java.util.*;
import java.net.*;

/**
 * Processes RemoteHalt Halt Channel command. 
 */

public class CmdRemoteHalt extends Command
{
   public CmdRemoteHalt(DebugSession debugSession, EReqRemoteHalt req)
   {
      super(debugSession);
      _req = req;
   }

   /**
    * Interrupts the debugEngine thread so that debugSession will wake up
    * and halt the execution of the application.
    */
   public boolean execute(EPDC_EngineSession EPDCSession) 
   {
     String message = null;
     int returnCode = EPDC.ExecRc_OK;
     
     DebugEngine _debugEngine = _debugSession.getDebugEngine();
     if (_debugSession.isWaiting())
     {
       _debugEngine.interrupt();
     }

     // We do not reply to this request.
     _rep = null;
     
     return false;
   }

   // Data members

   private EPDC_Request _req;
}
