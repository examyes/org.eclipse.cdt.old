/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// com/ibm/debug/gdb/objects/GdbMethodBreakpoint.java, gdb, java-dev
// Version 1.1 (last modified 5/24/01 13:45:25)   (based on Jde 12/29/98 1.9)
///////////////////////////////////////////////////////////////////////

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
