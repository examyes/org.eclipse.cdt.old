/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// %W%
// Version %I% (last modified %G% %U%)   (based on Jde 12/29/98 1.3)
///////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

import com.ibm.debug.epdc.*;

/**
 * Process Get Context Entry Point command
 */
public class CmdContextQualGet extends Command
{
   public CmdContextQualGet(DebugSession debugSession, EReqContextQualGet req)
   {
      super(debugSession);
      _req = req;
   }

   /**
    * Use the given Context to look up the entryID and return it to the FE.
    */
   public boolean execute(EPDC_EngineSession EPDCSession)
   {
     ModuleManager cm       = _debugSession.getModuleManager();
     int          PPID     = _req.context().getPPID();
     int          lineNum  = _req.context().getLineNum();
     int[]        entryIDs = new int[1];

     entryIDs[0] = cm.getEntryID(PPID, lineNum);
    
     _rep = new ERepContextQualGet(entryIDs);

     return false;
   }

   // data fields
   private EReqContextQualGet _req;
}
