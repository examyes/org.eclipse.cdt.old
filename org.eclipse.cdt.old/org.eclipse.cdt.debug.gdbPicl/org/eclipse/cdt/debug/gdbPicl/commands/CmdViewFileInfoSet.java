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

import java.io.*;
import java.util.*;
import com.ibm.debug.epdc.*;

/**
 * Process entry search command
 */
public class CmdViewFileInfoSet extends Command
{

   public CmdViewFileInfoSet(DebugSession debugSession, EReqViewFileInfoSet req)
   {
      super(debugSession);
      _req = req;
   }

   /**
    * Find and return a list of entries which fit the criteria given.
    */
   public boolean execute(EPDC_EngineSession EPDCSession)
   {
      _rep = new ERepViewFileInfoSet();

      Part part = _debugSession.getModuleManager().getPart(_req.partID());
 
      if (part != null)
      {
          View view = part.getView(_req.viewNum());

          if (view != null)
          {
              view.setViewInfo(_req.srcFileIndex(), _req.getEView());
              part.setPartChanged(true);
          }
      }

      return false;
   }

   // data fields
   private EReqViewFileInfoSet _req;
}
