/*
 * Copyright (c) 1995, 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.objects;
import  org.eclipse.cdt.debug.gdbPicl.*;

import com.ibm.debug.epdc.*;

/**
 * Class corresponding to a method breakpoint
 */
public class MethodBreakpoint extends LocationBreakpoint
{
   public MethodBreakpoint(DebugSession debugSession, int bkpID, int bkpAttr, int partID, int srcFileIndex, int viewNum, int entryID, int lineNum, EStdExpression2 conditionalExpr)
   {
      super(debugSession, bkpID, 0, EPDC.EntryBkpType, bkpAttr, partID, srcFileIndex, viewNum, conditionalExpr);

      _entryID = entryID;
      _lineNum = lineNum;
      update();
   }

   // Constructor for deferred method breakpoints
   public MethodBreakpoint(DebugSession debugSession, int bkpID, String moduleName,
                    String partName, String methodName, int bkpAttr, 
                    EStdExpression2 conditionalExpr)
   {
      super(debugSession, bkpID, 0, moduleName, partName, methodName, 
            EPDC.EntryBkpType, bkpAttr, conditionalExpr);
   }

   /**
    * Modify this breakpoint.
    */
    public void modify(int bkpID, int bkpAttr, int partID, int srcFileIndex, int viewNum, int entryID, int lineNum, EStdExpression2 conditionalExpr)
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
      ModuleManager mm = _debugSession.getModuleManager();
      _entryName = mm.getEntryName(_entryID);
    }

    /**
     * If the method name supplied is qualified, return the qualified
     * class portion. Otherwise return null.
     */
    public String getClassFromMethodName()
    {
      String name = entryName();
      int dotIndex = name.lastIndexOf('.');
      if (dotIndex > 0)
          return name.substring(0, dotIndex);

      return null;
    }

    /**
     * strip the class qualifier and signature information of the method
     * if available and return that method name.
     */
    String getUnqualifiedMethodName()
    {
      String name = entryName();

      // remove the class qualifier
      int dotIndex = name.lastIndexOf('.');
      if (dotIndex < 0)
          dotIndex = 0;
      else
          dotIndex++;

      String newName = name.substring(dotIndex, name.length());

      // remove the signature information
      int openBracketIndex = newName.indexOf('(');
      if (openBracketIndex < 0)
          return newName;

      return newName.substring(0,openBracketIndex);
    }
}
