/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.objects;
import  org.eclipse.cdt.debug.gdbPicl.*;

import com.ibm.debug.epdc.*;
//import org.eclipse.cdt.debug.gdbPicl.expr.*;

/**
 * Stores information for a breakpoint.  This is the superclass for specific
 * types of breakpoints (line breakpoints, method breakpoints)
 */
public class WatchBreakpoint extends Breakpoint
{
   public WatchBreakpoint(DebugSession debugSession, int bkpID, int bkpType, int bkpAttr,  EStdView context
                          , String expression, int byteCount )
   {
      super(debugSession, bkpID, bkpType, bkpAttr);
      _context = context;
      _expression = expression;
      _partID = context.getPPID();
      _lineNum = context.getLineNum();
      _byteCount = byteCount;
      if(_byteCount<=0) 
         _byteCount = 4;
   }


   /**
    * Fill in breakpoint type specific information in the change item.
    */
   void fillBreakpointChangeItem(ERepGetNextBkp bkpChangeItem)
   {

     if (Gdb.traceLogger.EVT) 
         Gdb.traceLogger.evt(1,"<<<<<<<<-------- WatchBreakpoint.fillBreakpointChangeItem"  );

         
 
//      bkpChangeItem.setDU(0);
//      bkpChangeItem.setDLLName("");
//      bkpChangeItem.setSourceName("");
//      bkpChangeItem.setIncludeName("");
//      bkpChangeItem.setEntryReturnType("");
      bkpChangeItem.setAddress(_expression);
      bkpChangeItem.setByteCount(_byteCount);
//      bkpChangeItem.setStatementNum("");

      // Note: We must call setBkpContext for every supported view
      bkpChangeItem.setBkpContext((short) Part.VIEW_SOURCE, _context.getPPID(), _context.getSrcFileIndex(), _lineNum);
      if (Gdb.traceLogger.ERR) 
          Gdb.traceLogger.err(2,"######## UNIMPLEMENTED DISASSEMBLY VIEW WatchBreakpoint lineNum="+_lineNum );
      bkpChangeItem.setBkpContext((short) Part.VIEW_DISASSEMBLY, _context.getPPID(), _context.getSrcFileIndex(), _lineNum);
      
      if (Part.MIXED_VIEW_ENABLED)
   	      bkpChangeItem.setBkpContext((short) Part.VIEW_MIXED, _context.getPPID(), _context.getSrcFileIndex(), 1);
//	      bkpChangeItem.setBkpContext((short) Part.VIEW_MIXED, _context.getPPID(), _context.getSrcFileIndex(), _lineNum);

//      bkpChangeItem.setEntryID(0);
//      bkpChangeItem.setVarInfo("");
//      bkpChangeItem.setConditionalExpr(null);
 
   }


   /**
    * Return the expression
    */
   public String expression() {
      return _expression;
   }
 
   /**
    * Return the breakpoint part ID
    */
   public int partID() {
      return _partID;
   }

   /**
    * Return breakpoint line number
    */
   public int lineNum() {
      return _lineNum;
   }


   // Data fields
   protected String   _expression;
   protected int      _lineNum;
   protected int      _partID;
   protected EStdView _context;
   public void setByteCount(int i) { _byteCount = i; }
   protected int      _byteCount = 4;
//   protected String _fileName;
//   public void setFileName(String f) { _fileName = f; }
//   public void setLineNum(int i) { _lineNum = i; }

}
