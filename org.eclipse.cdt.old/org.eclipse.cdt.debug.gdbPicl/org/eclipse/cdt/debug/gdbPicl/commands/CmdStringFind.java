/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.commands;
import org.eclipse.cdt.debug.gdbPicl.*;
import org.eclipse.cdt.debug.gdbPicl.objects.*;

import java.io.*;
import com.ibm.debug.epdc.*;

/**
 * Processes find string request
 */
public class CmdStringFind extends Command
{
  public CmdStringFind(DebugSession debugSession, EReqStringFind req)
  {
    super(debugSession);
    _req = req;
  }
  
  /**
   * 
   */
  public boolean execute(EPDC_EngineSession EPDCSession)
  {
    short partID     = _req.getPartID();
    short viewNum    = _req.getViewNumber();
    int srcFileIndex = _req.getSrcFileIndex();

    String searchString  = _req.getSearchString();
    int startLine        = _req.getStartLine();
    int startColumn      = _req.getStartColumn();
    int numLinesToSearch = _req.getNumLinesToSearch();
    int searchFlags      = _req.getSearchFlags();

    int line = 0;
    int column = 0;

    int position[] = null;

    Part part = _debugSession.getModuleManager().getPart(partID);

    if (part != null)
    {
       View view = part.getView(viewNum);

       if (view != null)
       {
          position = view.stringSearch(srcFileIndex, startLine,startColumn,numLinesToSearch,searchString,searchFlags);
       }
    }

    if (position != null)
    {
       line   = position[0];
       column = position[1];
    }

    _rep = new ERepStringFind(line,column);

    if (position == null)
    {
       _rep.setReturnCode(EPDC.ExecRc_FindFailed);
       _rep.setMessage(_debugSession.getResourceString("TEXT_NOT_FOUND_MSG"));
    }

    return false;
  }
  
  // Data fields
  private EReqStringFind _req;
}
