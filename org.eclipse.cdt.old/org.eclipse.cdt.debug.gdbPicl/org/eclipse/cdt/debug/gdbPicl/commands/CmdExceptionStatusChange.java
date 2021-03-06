/*
 * Copyright (c) 1995, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.commands;
import org.eclipse.cdt.debug.gdbPicl.*;
import org.eclipse.cdt.debug.gdbPicl.objects.*;

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
               _debugSession.catchException(i,EPDCSession._exceptionsInfo[i].exceptionName());
               EPDCSession._exceptionsInfo[i].setExceptionStatus(1);
            }else
            {
               _debugSession.ignoreException(i,EPDCSession._exceptionsInfo[i].exceptionName());
               EPDCSession._exceptionsInfo[i].setExceptionStatus(0);
            }
         }
      }

      return false;
   }

   // data fields
   int[] _exceptionStatusFlags;
}
