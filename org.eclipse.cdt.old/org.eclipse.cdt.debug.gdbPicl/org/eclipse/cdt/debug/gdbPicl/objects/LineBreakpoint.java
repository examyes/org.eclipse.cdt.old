/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// com/ibm/debug/gdb/objects/LineBreakpoint.java, gdb, java-dev
// Version 1.1 (last modified 5/24/01 13:45:38)   (based on Jde 1.13 12/7/00)
/////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.objects;
import  com.ibm.debug.gdbPicl.*;

import com.ibm.debug.epdc.*;

/**
 * Class corresponding to a line breakpoint
 */
public class LineBreakpoint extends LocationBreakpoint
{
   public LineBreakpoint(DebugSession debugSession, int bkpID, 
                  int bkpAttr, int partID, int srcFileIndex, int viewNum, 
                  int lineNum, EStdExpression2 conditionalExpr)
   {
      super(debugSession, bkpID, EPDC.LineBkpType, bkpAttr, partID,
            srcFileIndex, viewNum, conditionalExpr);
      _lineNum = lineNum;
      update();
   }

   // Constructor for deferred line breakpoints
   public LineBreakpoint(DebugSession debugSession, int bkpID, 
                  String moduleName, String partName, String fileName, int attr, 
                  int lineNumber, EStdExpression2 conditionalExpr)
   {
     super(debugSession, bkpID, moduleName, partName, fileName, 
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
System.out.println("#### LineBreakpoint getFileName UNUSED ################" );

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
