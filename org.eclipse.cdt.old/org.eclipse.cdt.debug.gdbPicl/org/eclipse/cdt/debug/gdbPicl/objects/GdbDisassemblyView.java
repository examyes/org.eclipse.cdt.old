/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// com/ibm/debug/gdb/objects/GdbDisassemblyView.java, gdb, java-dev
// Version 1.1 (last modified 5/24/01 13:45:29)   (based on Jde 02/08/99)
///////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.objects;
import  com.ibm.debug.gdbPicl.*;

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
