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
import com.ibm.debug.epdc.*;

/**
 * Process get process details request
 */
public class CmdProcessDetailsGet extends Command
{
   public CmdProcessDetailsGet(DebugSession debugSession, EReqProcessDetailsGet req)
   {
      super(debugSession);
   }

   /**
    * Returns stack display column information
    */
   public boolean execute(EPDC_EngineSession EPDCSession)
   {
      ERepProcessDetailsGet rep = new ERepProcessDetailsGet();

      rep.addColumn(_debugSession.getResourceString("TYPE_TEXT"), EPDC.RightJustified, EPDC.LeftJustified);
      rep.addColumn(_debugSession.getResourceString("HOST_TEXT"), EPDC.RightJustified, EPDC.LeftJustified);
      rep.addColumn(_debugSession.getResourceString("PASSWORD_TEXT"), EPDC.LeftJustified, EPDC.LeftJustified);

      _rep = rep;
      return false;
   }
}
