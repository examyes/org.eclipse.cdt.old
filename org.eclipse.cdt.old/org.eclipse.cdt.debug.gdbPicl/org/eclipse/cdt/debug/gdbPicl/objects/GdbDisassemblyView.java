/*
 * Copyright (c) 1995, 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.objects;
import  org.eclipse.cdt.debug.gdbPicl.*;

import java.io.*;
import java.util.*;
import com.ibm.debug.epdc.*;

/**
 * This class extends the View class to implement a disassembly view.
 */
public class GdbDisassemblyView extends DisassemblyView
{
   GdbDisassemblyView(GdbDebugEngine debugEngine, GdbPart parentPart)
   {
      super(debugEngine,  parentPart);

      // Lets discover some useful info
      DebugSession _debugSession = debugEngine.getDebugSession();
      _moduleManager    = _debugSession.getModuleManager();
      _moduleID        = _parentPart.getModuleID();

      GdbPart part = (GdbPart) ((GdbPart)_parentPart).getPart();

      _viewBaseFileName = "";
      // Discover what the base file name for the source file is
      try
      {
         if (part != null)
         {
            // Only prepend package name as path for non .jsp files
            _viewBaseFileName = part.getSourceFileName();
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(2,"GdbDisassemblyView Part: " + part.getName() + " Source=" + _viewBaseFileName );
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
