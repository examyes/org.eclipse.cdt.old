//////////////////////////////////////////////////////////////////////////////////////////////////////////
//Copyright (C)  1995, 2001 International Business Machines Corporation and others. All Rights Reserved.
//////////////////////////////////////////////////////////////////////////////////////////////////////////

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
