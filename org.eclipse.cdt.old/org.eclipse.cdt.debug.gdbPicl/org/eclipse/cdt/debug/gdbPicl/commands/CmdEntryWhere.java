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
 * Process entry where command
 */
public class CmdEntryWhere extends Command
{
   public CmdEntryWhere(DebugSession debugSession, EReqEntryWhere req)
   {
      super(debugSession);
      _req = req;
   }

   /**
    * Creates reply packet containing entry information
    */
   public boolean execute(EPDC_EngineSession EPDCSession)
   {
      _rep = new ERepEntryWhere();
      ModuleManager cm = _debugSession.getModuleManager();

      int entryID = _req.entryID();
      int pid = entryID >>> 16;  // get upper two bytes as part id

      // NOTE: We must call addContextInfo for each supported view
      ((ERepEntryWhere)_rep).addContextInfo((short)pid, 
               (short)Part.VIEW_SOURCE, 1, cm.getEntryLineNumber(entryID));
      if (Gdb.traceLogger.ERR) 
          Gdb.traceLogger.err(2,"######## UNIMPLEMENTED DISASSEMBLY VIEW CmdEntryWhere partID="+pid
                              +" lineNum="+cm.getEntryLineNumber(entryID) );
      ((ERepEntryWhere)_rep).addContextInfo((short)pid, 
               (short)Part.VIEW_DISASSEMBLY, 1, cm.getEntryLineNumber(entryID));
               
	  if (Part.MIXED_VIEW_ENABLED)
	  {               
    	  ((ERepEntryWhere)_rep).addContextInfo((short)pid, 
	           (short)Part.VIEW_MIXED, 1, cm.getEntryLineNumber(entryID));
	  }

      return false;
   }

   // data fields
   private EReqEntryWhere _req;
}
