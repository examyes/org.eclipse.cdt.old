/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

import java.io.*;
import java.util.*;
import com.ibm.debug.epdc.*;

/**
 * Process get part request
 */
public class CmdPartGet extends Command
{
   public CmdPartGet(DebugSession debugSession, EReqPartGet req)
   {
      super(debugSession);
      _req = req;
   }

   /**
    * Gets the requested source lines for SUI
    */
   public boolean execute(EPDC_EngineSession EPDCSession)
   {
      _rep = new ERepPartGet();

      Part part = _debugSession.getModuleManager().getPart(_req.partID());

      if (part != null)
      {
         View view = part.getView(_req.viewID());

         if (view != null)
         {
            view.getViewLines((ERepPartGet) _rep, _req.startLine(), _req.numLines());
         }
      }

      return false;
   }

   // Data fields
   private EReqPartGet _req;
}
