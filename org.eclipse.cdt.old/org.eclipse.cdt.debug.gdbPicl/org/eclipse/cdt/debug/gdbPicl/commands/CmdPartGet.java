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
// Version %I% (last modified %G% %U%)   (based on Jde 12/29/98 1.8)
///////////////////////////////////////////////////////////////////////

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
