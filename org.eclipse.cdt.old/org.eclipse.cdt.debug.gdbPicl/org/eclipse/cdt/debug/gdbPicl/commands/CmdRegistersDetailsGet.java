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
// Version %I% (last modified %G% %U%)   (based on Jde 11/2/97 1.12)
///////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

import com.ibm.debug.epdc.*;

/**
 * Process execute command
 */
public class CmdRegistersDetailsGet extends Command
{
   public CmdRegistersDetailsGet(DebugSession debugSession, EReqRegistersDetailsGet req)
   {
      super(debugSession);
      _req = req;
   }

   /**
    * Gets the requested Register Details
    */

   public boolean execute(EPDC_EngineSession EPDCSession)
   {

      _rep = new ERepRegistersDetailsGet();
      ((ERepRegistersDetailsGet)_rep).addGroup("General");
      ((ERepRegistersDetailsGet)_rep).addGroup("Float");
      int[] iArray = { 1, 2};
      ((ERepRegistersDetailsGet)_rep).setDefaultGroups(iArray);


      return false;
   }

   // Class fields
   private EReqRegistersDetailsGet _req;
}

