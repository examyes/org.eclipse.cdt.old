/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.objects;
import  org.eclipse.cdt.debug.gdbPicl.*;

import com.ibm.debug.epdc.*;

/**
 * Class corresponding to a method breakpoint
 */
class GdbMethodBreakpoint extends LocationBreakpoint
{
   GdbMethodBreakpoint(GdbDebugSession debugSession, int bkpID, int bkpAttr, int partID, int srcFileIndex, int viewNum, int entryID, int lineNum, EStdExpression2 conditionalExpr)
   {
      super(debugSession, bkpID, EPDC.EntryBkpType, bkpAttr, partID, srcFileIndex, viewNum, conditionalExpr);

      _entryID = entryID;
      _lineNum = lineNum;
      update();
   }

   // Constructor for deferred method breakpoints
   GdbMethodBreakpoint(DebugSession debugSession, int bkpID, String moduleName,
                    String partName, String methodName, int bkpAttr, 
                    EStdExpression2 conditionalExpr)
   {
      super(debugSession, bkpID, moduleName, partName, methodName, 
            EPDC.EntryBkpType, bkpAttr, conditionalExpr);
   }

   /**
    * Modify this breakpoint.
    */
    void modify(int bkpID, int bkpAttr, int partID, int srcFileIndex, int viewNum, int entryID, int lineNum, EStdExpression2 conditionalExpr)
    {
      super.modify(bkpID, bkpAttr, partID, srcFileIndex, viewNum, conditionalExpr);
      _entryID = entryID;
      _lineNum = lineNum;
      update();
    }

   /**
    * Private helper to update additional internal information.
    */
    private void update()
    {
      ModuleManager classManager = _debugSession.getModuleManager();
      _entryName = classManager.getEntryName(_entryID);
    }
}
