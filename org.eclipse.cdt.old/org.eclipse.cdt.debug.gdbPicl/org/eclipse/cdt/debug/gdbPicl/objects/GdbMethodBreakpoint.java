//////////////////////////////////////////////////////////////////////////////////////////////////////////
//Copyright (C)  1995, 2001 International Business Machines Corporation and others. All Rights Reserved.
//////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.objects;
import  com.ibm.debug.gdbPicl.*;

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
