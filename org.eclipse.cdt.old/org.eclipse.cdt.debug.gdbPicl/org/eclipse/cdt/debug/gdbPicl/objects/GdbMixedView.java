//////////////////////////////////////////////////////////////////////////////////////////////////////////
//Copyright (C)  1995, 2001 International Business Machines Corporation and others. All Rights Reserved.
//////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.objects;
import  com.ibm.debug.gdbPicl.*;

import java.io.*;
import java.util.*;
import com.ibm.debug.epdc.*;

/**
 * This class extends the View class to implement a mixed view.
 */
public class GdbMixedView extends MixedView
{
   GdbMixedView(GdbDebugEngine debugEngine, GdbPart parentPart)
   {
      super(debugEngine,  parentPart);

      // Lets discover some useful info
      DebugSession _debugSession = debugEngine.getDebugSession();
      _moduleManager    = _debugSession.getModuleManager();
      _moduleID        = _parentPart.getModuleID();

      GdbPart part = (GdbPart) ((GdbPart)_parentPart).getPart(); //HC

      // This is a mixed view so _viewFileName will be the full
      // pathname of the source file after we've successfully found it.

      // _viewBaseFilename is composed of the package name (as a path) plus
      // the base filename.   This is done so SUI can do source searches in
      // the same way that Java PICL does.  Thus, DEBUG_PATH (or XXX_DBG_PATH)
      // acts as the classpath does on the backend.  Note that the 
      // _viewBaseFileName will be set to whatever the user types in the
      // source dialog box.  Thus, when the user enters a full path name,
      // _viewBaseFileName will actually be the full path name of the source.

      _viewBaseFileName = "";

      // Discover what the base file name for the source file is
      try
      {
         if (part != null)
         {
            // Only prepend package name as path for non .jsp files
            _viewBaseFileName = part.getSourceFileName();
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(2,"GdbMixedView Part: " + part.getName() + " Source=" + _viewBaseFileName );
         }
      }
      catch (Exception e)
      {
      }

      // Get the executable line table for this class
      try
      {
         if (part != null)
            _executableLines = part.getLineNumbers();
      }
      catch (Exception e)
      {
      }

   }                     

}
