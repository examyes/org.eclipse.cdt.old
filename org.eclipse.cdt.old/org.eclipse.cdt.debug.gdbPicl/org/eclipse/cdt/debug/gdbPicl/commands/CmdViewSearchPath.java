/*
 * Copyright (c) 1995, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.commands;
import org.eclipse.cdt.debug.gdbPicl.*;
import org.eclipse.cdt.debug.gdbPicl.objects.*;

import java.io.*;
import java.util.*;
import com.ibm.debug.epdc.*;

/**
 * Process entry search command
 */
public class CmdViewSearchPath extends Command
{

   public CmdViewSearchPath(DebugSession debugSession, EReqViewSearchPath req)
   {
      super(debugSession);
      _req = req;
   }

   /**
    * Find and return a list of entries which fit the criteria given.
    */
   public boolean execute(EPDC_EngineSession EPDCSession)
   {
      _rep = new ERepViewSearchPath();

      // This reply is normally supposed to return a list of additional file
      // paths for the front end to search through to look for source files.
      // However, as apart of the local source hack, it is supposed to return
      // whatever file we looked for if the last part set request had failed.
      if (_debugSession.getLastPartSetFailed())
      {
         ((ERepViewSearchPath)_rep).addFilePath(_debugSession.getLastPartSetSrcFile());

         // Reset the debug session's state
         _debugSession.setLastPartSetFailed(false,"");
      }

      return false;
   }

   // data fields
   private EReqViewSearchPath _req;
}
