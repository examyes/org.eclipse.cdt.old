/*
 * Copyright (c) 1995, 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.objects;
import  org.eclipse.cdt.debug.gdbPicl.*;

import com.ibm.debug.epdc.*;

/**
 * Class corresponding to a line breakpoint
 */
public class LineBreakpoint extends LocationBreakpoint
{
   public LineBreakpoint(DebugSession debugSession, int bkpID, int gdbBkID,
                  int bkpAttr, int partID, int srcFileIndex, int viewNum, 
                  int lineNum, EStdExpression2 conditionalExpr)
   {
      super(debugSession, bkpID, gdbBkID, EPDC.LineBkpType, bkpAttr, partID,
            srcFileIndex, viewNum, conditionalExpr);
      _lineNum = lineNum;
      update();
   }

   // Constructor for deferred line breakpoints
   public LineBreakpoint(DebugSession debugSession, int bkpID, int gdbBkID,
                  String moduleName, String partName, String fileName, int attr, 
                  int lineNumber, EStdExpression2 conditionalExpr)
   {
     super(debugSession, bkpID, gdbBkID, moduleName, partName, fileName, 
           lineNumber, EPDC.LineBkpType, attr, conditionalExpr);
   }

   /**
    * Modify this breakpoint.
    */
    public void modify(int bkpID, int bkpAttr, int partID, int srcFileIndex,
                int viewNum, int lineNum, EStdExpression2 conditionalExpr)
    {
      super.modify(bkpID, bkpAttr, partID, srcFileIndex, viewNum, conditionalExpr);
      _lineNum       = lineNum;
      update();
    }

   /**
    * Private helper to update additional internal information.
    */
    private void update()
    {
      ModuleManager mm = _debugSession.getModuleManager();
      _entryID = mm.getEntryID(_partID, _lineNum);
      _entryName = mm.getEntryName(_entryID);
    }

    /**
     * If the source name is fully qualified, returns the name of the source
     * excluding the path and .java. Otherwise, it return the name of the
     * source excluding .java.
     */
    String getFileName()
    {
	  if (Gdb.traceLogger.DBG) 
              Gdb.traceLogger.dbg(1,"#### LineBreakpoint getFileName UNUSED ################" );

      String name = fileName();
      int dotJavaIndex = name.lastIndexOf(".java");

      // The source can be fully qualified
      int slashIndex = name.lastIndexOf('/');
      int backSlashIndex = name.lastIndexOf('\\');

      int srcPathIndex = 0;
      if (slashIndex > 0 || backSlashIndex > 0)
      {
          if (slashIndex >= backSlashIndex)
              srcPathIndex = slashIndex;
          else
              srcPathIndex = backSlashIndex;
          srcPathIndex++;
      }
      String fileName = name.substring(srcPathIndex, dotJavaIndex);

      return fileName;
    }

}
