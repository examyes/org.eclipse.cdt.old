/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.commands;
import org.eclipse.cdt.debug.gdbPicl.*;
import org.eclipse.cdt.debug.gdbPicl.objects.*;
import org.eclipse.cdt.debug.gdbPicl.objects.Part;
import org.eclipse.cdt.debug.gdbPicl.objects.View;

import java.io.*;
import com.ibm.debug.epdc.*;

/**
 * Processes set part request
 */
public class CmdPartSet extends Command
{
   public CmdPartSet(DebugSession debugSession, EReqPartSet req)
   {
      super(debugSession);
      _req = req;
   }

   /**
    * Sets the source file to the given file name, if it exists
    */
   public boolean execute(EPDC_EngineSession EPDCSession)
   {
     _rep = new ERepPartSet();
     
     int partID       = _req.partID();
     int viewID       = _req.viewID();
     int srcFileIndex = _req.srcFileIndex();

     ModuleManager classManager = _debugSession.getModuleManager();

     String srcFileName = null;
     
     try
     {
        srcFileName = _req.partFileName();
        
        Part part = classManager.getPart(partID);
 
        if (part != null)
        {
           View view = part.getView(viewID);
 
           if (view != null)
           {
              // Remember these in case the verify fails.
              boolean viewVerify = view.isViewVerify();
              boolean viewVerifyAttemptedFE = view.isViewVerifyAttemptedFE();
              boolean viewVerifyLocal = view.isViewVerifyLocal();

              // Reset some flags
              view.setViewVerify(false);
              view.setViewVerifyAttemptedFE(false);
              view.setViewVerifyLocal(false);

              boolean verified = view.verifyView(srcFileName);

              // int fileIndex        = srcFileName.lastIndexOf(_fileSeparator);
              // String pathString    = srcFileName.substring(0, fileIndex+1);

              // Set the last info for the last CmdPartSet we processed.
              // (Used by CmdViewSearchPath)
              if (!verified)
              {
                 // Reset back to known state since filename isn't valid.
                 view.setViewVerify(viewVerify);
                 view.setViewVerifyAttemptedFE(viewVerifyAttemptedFE);
                 view.setViewVerifyLocal(viewVerifyLocal);
                 _rep.setMessage(_debugSession.getResourceString("FILE_NOT_FOUND_MSG")+srcFileName);
                 _rep.setReturnCode(EPDC.ExecRc_FileNotFound);
                 _debugSession.setLastPartSetFailed(true, srcFileName);
              }
              else
              {
                 _debugSession.setLastPartSetFailed(false, "");
              }
           }
        }
     } 
     catch (IOException ioe)
     {
       Gdb.handleException(ioe);
     }
  
     return false;
   }

   // Data fields
   private EReqPartSet _req;
}
