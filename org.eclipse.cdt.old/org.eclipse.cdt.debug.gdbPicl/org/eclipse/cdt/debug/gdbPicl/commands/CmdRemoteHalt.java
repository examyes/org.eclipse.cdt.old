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
// Version %I% (last modified %G% %U%)   (based on Jde 12/29/98 1.4)
///////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

import java.io.*;
import com.ibm.debug.epdc.*;
import java.util.*;
import java.net.*;

/**
 * Processes RemoteHalt Halt Channel command. 
 */

public class CmdRemoteHalt extends Command
{
   public CmdRemoteHalt(DebugSession debugSession, EReqRemoteHalt req)
   {
      super(debugSession);
      _req = req;
   }

   /**
    * Interrupts the debugEngine thread so that debugSession will wake up
    * and halt the execution of the application.
    */
   public boolean execute(EPDC_EngineSession EPDCSession) 
   {
     String message = null;
     int returnCode = EPDC.ExecRc_OK;
     
     DebugEngine _debugEngine = _debugSession.getDebugEngine();
     if (_debugSession.isWaiting())
     {
       _debugEngine.interrupt();
     }

     // We do not reply to this request.
     _rep = null;
     
     return false;
   }

   // Data members

   private EPDC_Request _req;
}
