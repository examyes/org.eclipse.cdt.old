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
// Version %I% (last modified %G% %U%)   (based on Jde 12/29/98 1.6)
///////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;
//package com.ibm.debug.gdbPicl.commands.commands;
import  com.ibm.debug.gdbPicl.*;

import com.ibm.debug.epdc.*;

/**
 * Processes terminate debug engine command
 */
public class CmdTerminateDE extends Command
{
   public CmdTerminateDE(DebugSession debugSession, EReqTerminateDE req)
   {
      super(debugSession);
      _req = req;
   }

   /**
    * Any cleaning up should be done here
    */

   public boolean execute(EPDC_EngineSession EPDCSession)
   {
     _rep = new ERepTerminateDE();

     return true;
   }

   // Class fields
   private EReqTerminateDE _req;
}
