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
// Version %I% (last modified %G% %U%)   (based on Jde 12/29/98 1.2)
///////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

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
