//////////////////////////////////////////////////////////////////////////////////////////////////////////
//Copyright (C)  1995, 2001 International Business Machines Corporation and others. All Rights Reserved.
//////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.objects;
import  com.ibm.debug.gdbPicl.*;

import com.ibm.debug.epdc.*;
//import com.ibm.debug.gdbPicl.expr.*;

/**
 * Stores information for a breakpoint.  This is the superclass for specific
 * types of breakpoints (line breakpoints, method breakpoints)
 */
public class LocationBreakpoint extends Breakpoint
{
   public LocationBreakpoint(DebugSession debugSession, int bkpID, int bkpType,
      int bkpAttr, int partID, int srcFileIndex, int viewNum, EStdExpression2 conditionalExpr)
   {
      super(debugSession, bkpID, bkpType, bkpAttr);


      _partID        = partID;
      _srcFileIndex  = srcFileIndex;
      _viewNum       = viewNum;

      // the conditional expression is not checked at breakpoint creation/modification time
      // because Java delays creating variables until they are referenced.   Otherwise it would be
      // almost impossible to set a breakpoint that referenced local variables until just before
      // the breakpoint location.

      _conditionalExpr = conditionalExpr;

      update();
   }

   // for deferred line breakpoint
   public LocationBreakpoint(DebugSession debugSession, int bkpID, String moduleName,
                      String partName, String fileName, int lineNumber,
                      int bkpType, int bkpAttr, EStdExpression2 conditionalExpr)
   {
     super(debugSession, bkpID, bkpType, bkpAttr);    

     _moduleName = moduleName;
     _partName = partName;
     _fileName = fileName;
     _lineNum = lineNumber;
     _conditionalExpr = conditionalExpr;
   }

   // for deferred method breakpoint
   public LocationBreakpoint(DebugSession debugSession, int bkpID, String moduleName,
                      String partName, String methodName, int bkpType, 
                      int bkpAttr, EStdExpression2 conditionalExpr)
   {
     super(debugSession, bkpID, bkpType, bkpAttr);

     _moduleName = moduleName;
     _partName = partName;
     _entryName = methodName;

     _conditionalExpr = conditionalExpr;
   }

    public void modify(int bkpID, int bkpAttr, int partID, int srcFileIndex,
                int viewNum, EStdExpression2 conditionalExpr)
    {
      modify(bkpID, bkpAttr);

      _partID        = partID;
      _srcFileIndex  = srcFileIndex;
      _viewNum       = viewNum;
      _conditionalExpr = conditionalExpr;

      update();
    }

   /**
    * Helper method to update additional internal info.
    */
    private void update()
    {
      GdbModuleManager moduleManager = (GdbModuleManager)_debugSession.getModuleManager();
      String fullPartName = moduleManager.getFullPartName(_partID);
      GdbPart prt = (GdbPart)moduleManager.getPart(_partID);
      if(prt==null)
      {  if (Gdb.traceLogger.EVT) 
             Gdb.traceLogger.evt(1,"######### LocationBreakpoint.update part==null" );
         return;
      }
      int moduleID = prt.getModuleID();
      String moduleName = moduleManager.getModuleName(moduleID);
      _moduleName = moduleName;
      _partName = fullPartName;
/*
      int index = fullPartName.lastIndexOf(".");
      if (index == -1)
      {
         _moduleName = _debugSession.getResourceString("DEFAULT_PACKAGE_TEXT");
         _partName = fullPartName;
      }
      else
      {
         _moduleName = fullPartName.substring(0, index);
         _partName = fullPartName.substring(index+1);
      }
*/

      Part part = moduleManager.getPart(_partID);
      if (part != null)
      {
         View view = part.getView(_viewNum);
         if (view != null)
         {
            if (!view.isViewVerify())
                view.verifyView();

            _fileName = view.getViewFileName();
         }
      }
    }

  /**
   * check the conditional expression is true
   * @returns LocBkpRet class that contains fields with the result of checking the condition
   */

   public static class LocBkpRet
   {
      public LocBkpRet()
      {
        super();
      }

      public LocBkpRet(LocationBreakpoint breakpoint)
      {
        super();
        bkp = breakpoint;
      }

      public LocationBreakpoint bkp = null;         // the actual breakpoint
      public boolean isLocationBkp = true;          // this is a location breakpoint and the condition was true
      boolean isConditionExprError = false;  // processing the conditional expression caused an error
      String  conditionErrorText = null;     // text from expression evaluator
   }

   public LocBkpRet isConditionalExprTrue()
   {
      LocBkpRet rc = new LocBkpRet(this);

      if (_conditionalExpr == null)
      {
         Gdb.debugOutput("LocationBreakpoint: NO conditional expression to check");

         return rc;
      }

      Gdb.debugOutput("LocationBreakpoint: Checking conditional expression");

      // always evaluate the expression

      if (_vmm == null)
         _vmm = _debugSession.getVariableMonitorManager();

      ExprEvalInfo evalInfo = _vmm.checkConditionalExpr(_conditionalExpr);


      String value = ((String)evalInfo.getValue());

      if (evalInfo.expressionFailed())
      {
         rc.isConditionExprError = true;
         rc.conditionErrorText = _debugSession.getResourceString("BREAKPOINT_CONDITIONAL_EXPRESSION_MSG")
                                 + evalInfo.whyFailed(_debugEngine);
         Gdb.debugOutput(rc.conditionErrorText);
         return rc;
      }


      // Only boolean values are allowed.  The call to checkConditionalExpr()" checks for
      // a boolean return value.

      try
      {
         if (value.equalsIgnoreCase("true")||value.equalsIgnoreCase("false"))
         {
            Gdb.debugOutput("Condition is " +value.equalsIgnoreCase("true"));
            rc.isLocationBkp = value.equalsIgnoreCase("true");
            return rc;
         }
      }
      catch(Exception e) {}

      return rc;
   }


   /**
    * Fill in breakpoint type specific information in the change item.
    */
   void fillBreakpointChangeItem(ERepGetNextBkp bkpChangeItem)
   {

     if (Gdb.traceLogger.EVT) 
         Gdb.traceLogger.evt(1,"<<<<<<<<-------- Location Breakpoint.fillBreakpointChangeItem setDLLName=_moduleName="+_moduleName+" fileName="+_partName+" lineNum="+_lineNum  );

      bkpChangeItem.setDU(0);
      bkpChangeItem.setDLLName(_moduleName);              //module
      bkpChangeItem.setSourceName(_partName);            //part
      bkpChangeItem.setIncludeName(_fileName);           //File
      bkpChangeItem.setEntryReturnType("");
      bkpChangeItem.setAddress("");
      bkpChangeItem.setStatementNum("");

      // Note: We must call setBkpContext for every supported view
      bkpChangeItem.setBkpContext((short) Part.VIEW_SOURCE, (short)_partID, 1, _lineNum);
     if (Gdb.traceLogger.ERR) 
         Gdb.traceLogger.err(1,"######## UNIMPLEMENTED DISASSEMBLY/MIXED VIEW Location Breakpoint.fillBreakpointChangeItem setDLLName=_moduleName="+_moduleName+" fileName="+_partName+" lineNum="+_lineNum  );
      bkpChangeItem.setBkpContext((short) Part.VIEW_DISASSEMBLY, (short)_partID, 1, _lineNum);
      
      if (Part.MIXED_VIEW_ENABLED)
	      bkpChangeItem.setBkpContext((short) Part.VIEW_MIXED, (short)_partID, 1, _lineNum);

      bkpChangeItem.setEntryID(_entryID);
      bkpChangeItem.setVarInfo(_entryName);
      bkpChangeItem.setConditionalExpr(_conditionalExpr);
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

   /**
    * Return breakpoint srcFileIndex
    */
   public int srcFileIndex() {
      return _srcFileIndex;
   }

   /**
    * Return breakpoint view number
    */
   public int viewNum() {
      return _viewNum;
   }

   /**
    * Return the breakpoint entry ID
    */
   public int entryID() {
      return _entryID;
   }

   /**
    * Return the entry name
    */
   public String entryName() {
      return _entryName;
   }

   /**
    * Return the part name
    */
   public String partName() {
      return _partName;
   }

   public String moduleName()
   {
     return _moduleName;
   }

   public String fileName()
   {
     return _fileName;
   }

   /**
    * Return the fully qualified class name if the user provided that when
    * setting the breakpoint. Otherwise return null.
    */
   String getQualifiedPartName()
   {

System.out.println("#### LocationBreakpoint getUnqualifiedPartName UNUSED ################" );

     String partName = partName();
     if (partName == null)
         return null;

     int dotIndex = partName.lastIndexOf('.');
     if (dotIndex > 0)
         return partName;

     return null;
   }

   /**
    * Return true if this is the default package (<default>) and 
    * false otherwise 
    */
   public boolean hasDefaultPackageName()
   {
System.out.println("#### LocationBreakpoint hasDefaultPackageName UNUSED ################" );

     if (_moduleName != null &&
         _moduleName.equals(_debugSession.getResourceString("DEFAULT_PACKAGE_TEXT")))
     {
         return true;
     }

     return false;
   }

   public EStdExpression2 conditionalExpr()
   {
     return _conditionalExpr;
   }

   // Data fields
   private String _moduleName;
   private String _partName;
   private String _fileName;
   private EStdExpression2 _conditionalExpr;
   private VariableMonitorManager _vmm;

   protected int    _lineNum;
   protected int    _partID;
   protected int    _srcFileIndex;
   protected int    _viewNum;
   protected int    _entryID;
   protected String _entryName;
}
