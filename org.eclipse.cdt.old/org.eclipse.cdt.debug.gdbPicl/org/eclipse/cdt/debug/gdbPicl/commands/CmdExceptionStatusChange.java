/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// com/ibm/debug/gdb/commands/CmdExceptionStatusChange.java, gdb, java-dev
// Version 1.1 (last modified 5/24/01 13:44:26)   (based on Jde 9/28/98 1.8)
///////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

import java.io.*;
import com.ibm.debug.epdc.*;

/**
 * Processes exception status change request
 */
public class CmdExceptionStatusChange extends Command
{
   public CmdExceptionStatusChange(DebugSession debugSession, EReqExceptionStatusChange req)
   {
      super(debugSession);
      _exceptionStatusFlags = req.exceptionStatusFlags();
   }

   /**
    * Sets the exception status flags
    */
   public boolean execute(EPDC_EngineSession EPDCSession)
   {
      _rep = new ERepExceptionStatusChange();
      // update changed exception statuses

      for (int i=0; i<_exceptionStatusFlags.length; i++) 
      {

         if (EPDCSession._exceptionsInfo[i].exceptionStatus() != _exceptionStatusFlags[i]) 
         {
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(3,"CmdExceptionStatusChange EPDCSession._exceptionsInfo[i].exceptionStatus()="+EPDCSession._exceptionsInfo[i].exceptionStatus() 
                  +" _exceptionStatusFlags[i]="+ _exceptionStatusFlags[i] );
            if (_exceptionStatusFlags[i] == 1)
            {
               _debugSession.catchException(i,EPDCSession._exceptionsInfo[i].exceptionName().string());
               EPDCSession._exceptionsInfo[i].setExceptionStatus(1);
            }else
            {
               _debugSession.ignoreException(i,EPDCSession._exceptionsInfo[i].exceptionName().string());
               EPDCSession._exceptionsInfo[i].setExceptionStatus(0);
            }
         }
      }

      return false;
   }

   // data fields
   int[] _exceptionStatusFlags;
}
