/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// com/ibm/debug/gdb/commands/CmdEntryWhere.java, gdb, java-dev
// Version 1.1 (last modified 5/24/01 13:44:25)   (based on Jde 12/29/98 1.7)
///////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

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
          Gdb.traceLogger.err(1,"######## UNIMPLEMENTED DISASSEMBLY VIEW CmdEntryWhere partID="+pid
                              +" lineNum="+cm.getEntryLineNumber(entryID) );
      ((ERepEntryWhere)_rep).addContextInfo((short)pid, 
               (short)Part.VIEW_DISASSEMBLY, 1, cm.getEntryLineNumber(entryID));
      ((ERepEntryWhere)_rep).addContextInfo((short)pid, 
               (short)Part.VIEW_MIXED, 1, cm.getEntryLineNumber(entryID));

      return false;
   }

   // data fields
   private EReqEntryWhere _req;
}
