/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

import java.io.*;
import com.ibm.debug.epdc.*;

/**
 * Process build stack view request
 */
public class CmdStackBuildView extends Command
{
   public CmdStackBuildView(DebugSession debugSession, EReqStackBuildView req)
   {
      super(debugSession);

      _stackDU = req.stackDU();
      _stackEntryID = req.stackEntryID();
   }

   /**
    * Return the view information for a stack entry
    */
   public boolean execute(EPDC_EngineSession EPDCSession)
   {
      ThreadManager threadManager = _debugSession.getThreadManager();
      ModuleManager  classManager  = _debugSession.getModuleManager();

      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(1,"Getting view information for thread " + Integer.toString(_stackDU) +
              ", stack entry " + Integer.toString(_stackEntryID));
      ThreadComponent tc = threadManager.getThreadComponent(_stackDU);

      // We always return view information for the source view.  Hopefully, 
      // this is what the front end was looking for.  If not, the front
      // end will do a context convert.
      int stackEntry = tc.getCallStackSize()-_stackEntryID;
      int moduleID = ((GdbThreadComponent)tc).moduleID(stackEntry);
      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(2,"---------------- CmdStackView Getting viewInformation for thread " + Integer.toString(_stackDU) +
              ", stack entry " + Integer.toString(_stackEntryID) +" >>>> fileName="+tc.fileName(stackEntry)+" lineNumber="+tc.lineNumber(stackEntry) );

	  short partID = (short)classManager.getPartID(moduleID, tc.fileName(stackEntry));
	  
	  if (partID <= 0)
	  	partID = 1;
	  
      _rep = new ERepStackBuildView( (short)Part.VIEW_SOURCE, 
         partID, 1,
         tc.lineNumber(stackEntry) );

      return false;
   }

   // data fields
   private int _stackDU;
   private int _stackEntryID;
}
