/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// com/ibm/debug/gdb/GdbBreakpointManager.java, gdb, java-dev
// Version 1.1 (last modified 5/24/01 13:41:49)   (based on Jde 12/29/98 1.21)
///////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl;
import  com.ibm.debug.gdbPicl.objects.*;

import com.ibm.debug.epdc.*;
import java.util.*;

/**
 * Manages the breakpoint table
 */

public class GdbBreakpointManager extends BreakpointManager//extends ComponentManager
{

   public GdbBreakpointManager(GdbDebugSession debugSession)
   {
      super(debugSession);
      _breakpoints = new Vector();
      _changedBreakpoints = new Vector();
   }

   /**
    * Add a line breakpoint
    * @return 0 if succesful
    * @return -1 if failed 
    * @return breakpoint ID of a duplicate breakpoint if this line already has a line breakpoint set.
    */
   public int setLineBreakpoint(int partID, int srcFileIndex, int viewNum, 
                         int lineNum, boolean enable,
                         EStdExpression2 conditionalExpr)
   {
      if (Gdb.traceLogger.ERR) 
          Gdb.traceLogger.err(2,"######## GdbBreakpointManager.setLineBreakpoint -- why not use BASE BreakpointManager" );
      // first make sure there are no other line breakpoints at the same location
      for (int i=0; i<_breakpoints.size(); i++)
      {
         Object obj = _breakpoints.elementAt(i);
         if (!(obj instanceof LineBreakpoint))
            continue;

         LineBreakpoint bkp = (LineBreakpoint) _breakpoints.elementAt(i);
         if ((bkp != null) && (bkp.bkpType() == EPDC.LineBkpType) && 
             (bkp.partID() == partID) && (bkp.lineNum() == lineNum))
         {
            if (Gdb.traceLogger.DBG) 
                Gdb.traceLogger.dbg(1,"Duplicate breakpoint");
            return bkp.bkpID();
         }
      }

      // now try to set the breakpoint
      ModuleManager cm = _debugSession.getModuleManager();

      if (!_debugSession.setLineBreakpoint(partID, lineNum))
         return -1;

      int bkpID = _breakpoints.size()+1;

      if (Gdb.traceLogger.DBG) 
      {
         try
         {
             Gdb.traceLogger.dbg(1,"Line breakpoint set: line number " + lineNum +
                        (conditionalExpr == null ? " No condition" : " Conditional expr = >" + conditionalExpr.getExprString() + "<") );
         } catch(Exception e) {
            if (Gdb.traceLogger.ERR) 
                Gdb.traceLogger.err(3,"Error printing out Line breakpoint info");
         }
      }

      LineBreakpoint lineBkp = new LineBreakpoint(_debugSession, bkpID, 0, partID, srcFileIndex, viewNum, lineNum, conditionalExpr );
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(1,"Line breakpoint set");

      _breakpoints.addElement(lineBkp);

      // if breakpoint should be disabled, then disable it.
      if (!enable)
         disableBreakpoint(bkpID);
      else
         _changedBreakpoints.addElement(lineBkp);
      return 0;
   }

   /**
    * Add a deferred line breakpoint
    * @return 0 if succesful
    * @return -1 if failed
    * @return breakpoint ID of a duplicate breakpoint if this line already 
    * has a deferred line breakpoint set.
    */
   public int setDeferredLineBreakpoint(String pkgName, String partName,
                                 String fileName, int attr, int lineNumber,
                                 boolean enabled, 
                                 EStdExpression2 conditionalExpr)
   {
      if (Gdb.traceLogger.ERR) 
          Gdb.traceLogger.err(2,"######## GdbBreakpointManager.setDeferredLineBreakpoint -- why not use BASE BreakpointManager instead of override ??????" );

     for (int i=0; i<_breakpoints.size(); i++)
     {
          Breakpoint bkp = (Breakpoint) _breakpoints.elementAt(i);

          if (!(bkp instanceof LineBreakpoint))
              continue;

          LineBreakpoint lineBkp = (LineBreakpoint)bkp;
          if (lineBkp != null && lineBkp.bkpType() == EPDC.LineBkpType &&
              lineBkp.fileName() != null && fileName != null &&
              lineBkp.fileName().equals(fileName) && 
              lineBkp.lineNum() == lineNumber)
          {
              if ( (partName != null && pkgName != null &&
                    lineBkp.partName() != null && lineBkp.moduleName() != null &&
                    lineBkp.partName().equals(partName) &&
                    lineBkp.moduleName().equals(pkgName)) ||
                   (partName == null && pkgName == null) )
              {
                   if (Gdb.traceLogger.DBG) 
                       Gdb.traceLogger.dbg(1,"Duplicate breakpoint");
                   return lineBkp.bkpID();
              }
          }
     }

     DebugSession session = _debugEngine.getDebugSession();

     // If the part for this line breakpoint request is already loaded,
     // then set this as a regular line breakpoint vs. a deferred
     // line breakpoint.
     if (partName != null)
     {
         String[] partsList = session.getPartsList(partName);

        if (partsList != null && partsList.length != 0)
        {
            int partID = 0;
            for (int i = 0; i < partsList.length; i++)
            {
                 partID = _debugSession.getModuleManager().getPartID(partsList[i]);
                 // If the class name includes the package name of this
                 // breakpoint request, then just set the line breakpoint
                 // for the class of this package only.
                 if (pkgName != null && 
                     (partsList[i].startsWith(pkgName) ||
                      pkgName.equals(_debugSession.getResourceString("DEFAULT_PACKAGE_TEXT"))))
                 {
                     setLineBreakpoint(partID, 1, Part.VIEW_SOURCE,
                                       lineNumber, enabled, conditionalExpr);
                     break;
                 }

                 setLineBreakpoint(partID, 1, Part.VIEW_SOURCE, lineNumber,
                                   enabled, conditionalExpr);
            }
            return 0;
        }
     }

     int bkpID = _breakpoints.size()+1;

     LineBreakpoint bkp = new LineBreakpoint(_debugSession, bkpID, pkgName,
                                             partName, fileName, attr,
                                             lineNumber, conditionalExpr);
     _breakpoints.addElement(bkp);

     if (Gdb.traceLogger.DBG) 
     {
        try
        {
            Gdb.traceLogger.dbg(1,"Deferred line breakpoint set: line number " + lineNumber +
                    (conditionalExpr == null ? " No condition" : " Conditional expr = >" + conditionalExpr.getExprString() + "<") );
        }
        catch(Exception e) {
            if(Gdb.traceLogger.ERR) 
               Gdb.traceLogger.err(3,"Error printing out Line breakpoint info");
        }
    }

     if (!enabled)
         disableBreakpoint(bkpID);
     else
         _changedBreakpoints.addElement(bkp);

     // Set up the method load filter pattern based on the
     // breakpoint information
     session.prepareDeferredBreakpointMethodFilter(bkp);

     return 0;
   }


   /**
    * Modify a line breakpoint
    * @return 0 if succesful
    * @return -1 if failed
    * @return breakpoint ID of a duplicate breakpoint if this line already has a method breakpoint set.
    */
   public int modifyLineBreakpoint(int bkpID, int partID, int srcFileIndex, int viewNum, 
                            int lineNum, boolean enable,
                            EStdExpression2 conditionalExpr)
   {
      if (bkpID < 1 || bkpID > _breakpoints.size())
        return -1;

      LineBreakpoint bkp = (LineBreakpoint) _breakpoints.elementAt(bkpID-1);

      if (bkp == null)
        return -1;

      ModuleManager cm = _debugSession.getModuleManager();

      // set the new breakpoint
      if (!_debugEngine.getDebugSession().setLineBreakpoint(partID, lineNum))
         return -1;

      int old_partID = bkp.partID();
      int old_lineNum = bkp.lineNum();

      bkp.modify(bkpID, 0, partID, srcFileIndex, viewNum, lineNum, conditionalExpr);

      // clear the old breakpoint
      if (!isLocationBreakpoint(old_partID, old_lineNum))
        _debugEngine.getDebugSession().clearBreakpoint(old_partID, old_lineNum);
      
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(1,"Line breakpoint modified");

      // if breakpoint should be disabled, then disable it.
      if (!enable)
         disableBreakpoint(bkpID);
      else
         _changedBreakpoints.addElement(bkp);
      return 0;
   }

   /**
    * Modify a deferred line breakpoint
    * @return 0 if succesful and -1 otherwise.
    */
   public int modifyDeferredLineBreakpoint(int bkpID, String moduleName,
                                    String partName, String fileName,
                                    int attr, int lineNum, EStdExpression2 expr)
   {
     if (bkpID < 1 || bkpID > _breakpoints.size())
         return -1;

     Breakpoint bkp = (Breakpoint)_breakpoints.elementAt(bkpID-1);

     if (!(bkp instanceof LineBreakpoint))
         return -1;

     // Because we cannot modify the ClassPrepareRequest filter pattern
     // of a deferred line breakpoint, we cannot modify the existing
     // deferred line breakpoint. Instead we delete this breakpoint and
     // create a new one, so that we can create a new ClassPrepareRequest
     // with the new filter information. Therefore, we need to remember all
     // current deferrd line breakpoint information to pass it to the new one.
     boolean isEnabled = bkp.isEnabled();
     int bkpAttr = (bkp.attribute() | attr);
     EStdExpression2 conditionalExpr = (expr == null) ? ((LineBreakpoint)bkp).conditionalExpr() : expr;

     clearBreakpoint(bkpID);

     setDeferredLineBreakpoint(moduleName, partName, fileName, attr, lineNum, isEnabled, conditionalExpr);

     return 0;
   }

   /**
    * Add a method breakpoint
    * @return 0 if succesful
    * @return -1 if method invalid
    * @return breakpoint ID of a duplicate breakpoint if this line already has a method breakpoint set.
    */
/*
   public int setMethodBreakpoint(int partID, int srcFileIndex, int viewNum, 
                           int entryID, boolean enable, EStdExpression2 conditionalExpr)
   {
      // first make sure there are no other method breakpoints at the same location
      for (int i=0; i<_breakpoints.size(); i++)
      {
         Object obj = _breakpoints.elementAt(i);
         if (!(obj instanceof MethodBreakpoint))
            continue;

         MethodBreakpoint bkp = (MethodBreakpoint) _breakpoints.elementAt(i);
         if ((bkp != null) && (bkp.bkpType() == EPDC.EntryBkpType) && 
             (bkp.partID() == partID) && (bkp.entryID() == entryID))
         {
            if (Gdb.traceLogger.DBG) 
                Gdb.traceLogger.dbg(1"Duplicate breakpoint");
            return bkp.bkpID();
         }
      }

      // now try to set the breakpoint
      ModuleManager moduleManager = _debugSession.getModuleManager();
      int methodIndex = moduleManager.getMethodIndex(entryID);
      int lineNum = _debugSession.setMethodBreakpoint(partID, methodIndex);

      if (lineNum <0)
         return -1;

      int bkpID = _breakpoints.size()+1;
      
      try
      {
          if (Gdb.traceLogger.DBG) 
              Gdb.traceLogger.dbg(1,"Method breakpoint set, " +
                        (conditionalExpr == null ? " No condition" : " Conditional expr = >" + conditionalExpr.getExprString() + "<") );
      } catch(Exception e) {
         Gdb.debugOutput("Error printing out Method breakpoint info");
      }

      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(2,"---------------- GdbBreakpointManager.setMethodBreakpoint creating MethodBreakpoint bkpID="+bkpID+ " bkpAttr="+0
             +" partID="+partID+" srcFileIndex="+srcFileIndex+" viewNum="+ viewNum+ "entryID="+entryID+" lineNum="+lineNum );
      MethodBreakpoint methBkp = new MethodBreakpoint(_debugSession, bkpID, 0, partID, srcFileIndex, viewNum, entryID, lineNum, conditionalExpr);
      Gdb.debugOutput("Method breakpoint set partID="+partID+" lineNum="+lineNum );

      _breakpoints.addElement(methBkp);

      // if breakpoint should be disabled, then disable it.
      if (!enable)
         disableBreakpoint(bkpID);
      else
         _changedBreakpoints.addElement(methBkp);
      return 0;
   }
*/

   /**
    * Add a method breakpoint
    * @return 0 if succesful
    * @return -1 if method invalid
    * @return breakpoint ID of a duplicate breakpoint if there is already a
    * deferred method breakpoint set in this class.
    */
   public int setDeferredMethodBreakpoint(String pkgName, String partName,
                                   String entryName, int attr, boolean enabled,
                                   EStdExpression2 conditionalExpr)
   {
     String methodName = entryName;

     // We need to get the method name excluding the signature information
     // in order to compare it with any other method that matches to the
     // same name and is in the same package and class of an already deferred
     // method breakpoint.
     int startSignatureIndex = entryName.indexOf('(');
     if (startSignatureIndex > 0)
         methodName = entryName.substring(0, startSignatureIndex);

     // first make sure there are no other deferred method breakpoints for
     // the same method.
     for (int i=0; i<_breakpoints.size(); i++)
     {
          Breakpoint bkp = (Breakpoint) _breakpoints.elementAt(i);

          if (bkp == null || !(bkp instanceof MethodBreakpoint))
              continue;

          MethodBreakpoint methodBkp = (MethodBreakpoint)bkp;
          String bkpModuleName = methodBkp.moduleName();
          String bkpPartName = methodBkp.partName();
          if (methodBkp != null && methodBkp.bkpType() == EPDC.EntryBkpType &&
              methodBkp.isDeferred() &&
              methodBkp.entryName().equals(methodName) &&
              (bkpModuleName != null && 
               (methodBkp.hasDefaultPackageName() || bkpModuleName.equals(pkgName))) &&
              (bkpPartName != null && bkpPartName.equals(partName)))
          {
              if (Gdb.traceLogger.DBG) 
                  Gdb.traceLogger.dbg(1,"Duplicate breakpoint");
              return methodBkp.bkpID();
          }
     }

     DebugSession session = _debugEngine.getDebugSession();

     // If the class for this method breakpoint request is already loaded,
     // then set this as a regular method breakpoint vs. a deferred
     // method breakpoint.
     if (partName != null)
     {
         String[] partsList = session.getPartsList(partName);

        if (partsList != null && partsList.length != 0)
        {
            int partID = 0;
            ModuleManager moduleManager = _debugSession.getModuleManager();
            for (int i = 0; i < partsList.length; i++)
            {
                 partID = moduleManager.getPartID(partsList[i])
;
                 int moduleID = moduleManager.getPart(partID).getModuleID();
                 String moduleName = moduleManager.getModuleName(moduleID);
                 int[] entryID = moduleManager.getEntryIDs(partID, entryName, true);

                 // If the package name is known and the class is in this
                 // package, only set the breakpoint for methods matching
                 // the entryName in this package.
                 if (pkgName != null &&
                     pkgName.equals(moduleName))
                 {
                     for (int j = 0; j < entryID.length; j++)
                          setMethodBreakpoint(partID, 1, Part.VIEW_SOURCE,
                                              entryID[j], enabled, conditionalExpr);
                     return 0;
                 }

                 // If we do not have the package information, set the
                 // breakpoint for all the methods matching entryName in
                 // all classes matching partName in this debug session.
                 for (int j = 0; j < entryID.length; j++)
                      setMethodBreakpoint(partID, 1, Part.VIEW_SOURCE,
                                          entryID[j], enabled, conditionalExpr);
            }

            return 0;
        }
     }

     int bkpID = _breakpoints.size()+1;

     if (Gdb.traceLogger.DBG) 
     {
        try
        {
            Gdb.traceLogger.dbg(1,"Deferred method breakpoint set, " +
              (conditionalExpr == null ? " No condition" : " Conditional expr = >" + conditionalExpr.getExprString() + "<") );
        }
        catch(Exception e) {
           if (Gdb.traceLogger.ERR) 
               Gdb.traceLogger.err(3,"Error printing out Method breakpoint info");
        }
     }

     MethodBreakpoint bkp = new MethodBreakpoint(_debugSession, bkpID, pkgName, partName, methodName, attr, conditionalExpr);

     _breakpoints.addElement(bkp);

     if (!enabled)
         disableBreakpoint(bkpID);
     else
         _changedBreakpoints.addElement(bkp);

     _debugSession.prepareDeferredBreakpointMethodFilter(bkp);

     return 0;
   }


   /**
    * Modify a method breakpoint
    * @return 0 if succesful
    * @return -1 if failed
    * @return breakpoint ID of a duplicate breakpoint if this line already has a method breakpoint set.
    */
   public int modifyMethodBreakpoint(int bkpID, int partID, int srcFileIndex, int viewNum, 
                              int entryID, boolean enable,
                              EStdExpression2 conditionalExpr)
   {
      if (bkpID < 1 || bkpID > _breakpoints.size())
        return -1;

      MethodBreakpoint bkp = (MethodBreakpoint) _breakpoints.elementAt(bkpID-1);

      if (bkp == null)
        return -1;

      ModuleManager cm = _debugSession.getModuleManager();

      // set the new breakpoint
      int lineNum = _debugSession.setMethodBreakpoint(partID, cm.getMethodIndex(entryID));

      if (lineNum < 0)
      {
         // Special flag to indicate we _probably_ tried to set a method
         // breakpoint where one already existed.  If so, don't fail.  Assume
         // that lineNum is the old breakpoint's line num.  We have to go
         // through this due to the fact that we can't tell where a method
         // breakpoint was set until after we set it.  See setMethodBreakpoint
         // for more info.
         if (lineNum == -2)
           lineNum = bkp.lineNum();
         else
           return -1;
      }

      int old_partID = bkp.partID();
      int old_lineNum = bkp.lineNum();

      if (Gdb.traceLogger.DBG) 
      {
         try
         {
             Gdb.traceLogger.dbg(1,"Method breakpoint modified, " +
                        (conditionalExpr == null ? " No condition" : " Conditional expr = >" + conditionalExpr.getExprString() + "<") );
         } catch(Exception e) {
            if (Gdb.traceLogger.ERR) 
               Gdb.traceLogger.err(3,"Error printing out Method breakpoint info");
         }
      }

      bkp.modify(bkpID, 0, partID, srcFileIndex, viewNum, entryID, lineNum, conditionalExpr);

      // clear the old breakpoint
      if (!isLocationBreakpoint(old_partID, old_lineNum))
        _debugEngine.getDebugSession().clearBreakpoint(old_partID, old_lineNum);
      
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(1,"Method breakpoint modified");

      // if breakpoint should be disabled, then disable it.
      if (!enable)
         disableBreakpoint(bkpID);
      else
         _changedBreakpoints.addElement(bkp);
      return 0;
   }

   /**
    * Modify a deferred method breakpoint
    * @return 0 if succesful and -1 otherwise.
    */
   public int modifyDeferredMethodBreakpoint(int bkpID, String moduleName,
                                      String partName, String methodName,
                                      int attr, EStdExpression2 expr)
   {
     if (bkpID < 1 || bkpID > _breakpoints.size())
         return -1;

     Breakpoint bkp = (Breakpoint)_breakpoints.elementAt(bkpID-1);

     if (!(bkp instanceof MethodBreakpoint))
         return -1;
     
     MethodBreakpoint methodBkp = (MethodBreakpoint)bkp;

     // Because we cannot modify the ClassPrepareRequest filter pattern
     // of a deferred method breakpoint, we cannot modify the existing
     // deferred method breakpoint. Instead we delete this breakpoint and
     // create a new one, so that we can create a new ClassPrepareRequest
     // with the new filter information. Therefore, we need to remember all
     // current deferrd method breakpoint information to pass it to the new one.
     boolean isEnabled = methodBkp.isEnabled();
     int bkpAttr = (methodBkp.attribute() | attr);
     EStdExpression2 conditionalExpr = (expr == null) ? methodBkp.conditionalExpr() : expr;
     clearBreakpoint(bkpID);

     setDeferredMethodBreakpoint(moduleName, partName, methodName, bkpAttr, isEnabled, conditionalExpr);


     return 0;
   }

   /**
    * Add a watchpoint
    * @return 0 if succesful
    * @return -1 if failed
    * @return breakpoint ID of a duplicate breakpoint if this expression
    *         is already being watched.
    */
   public int setWatchpoint(String exprString, ExprEvalInfo evalInfo, int byteCount,
                     EStdView context, boolean enable)
   {
      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(1,"GdbBreakpointManager.setWatchpoint expr="+exprString );
      if ( !_debugSession.setWatchpoint(exprString) )
          return -1;

      int bkpID = _breakpoints.size()+1;

      int attrib = 0;
      WatchBreakpoint watchBkp = new WatchBreakpoint(_debugSession, bkpID, EPDC.ChangeAddrBkpType, attrib, context, exprString, byteCount );

      if (Gdb.traceLogger.DBG) 
         Gdb.traceLogger.dbg(1,"Watchpoint set: expr " + exprString );

      _breakpoints.addElement(watchBkp);

      // if breakpoint should be disabled, then disable it.
      if (!enable)
         disableBreakpoint(bkpID);
      else
         _changedBreakpoints.addElement(watchBkp);
      return 0;


   }




   /**
    * Modify a watchpoint
    * @return 0 if succesful
    * @return -1 if failed
    * @return breakpoint ID of a duplicate breakpoint if this expression
    *         is already being watched.
    */
   public int modifyWatchpoint(int bkpID, String exprString, ExprEvalInfo evalInfo,
                        int byteCount, EStdView location, boolean enable)
   {
      if (Gdb.traceLogger.ERR) 
          Gdb.traceLogger.err(2,"UNIMPLEMENTED GdbBreakpointManager.modiftWatchpoint");
      return -1;
   }


   /**
    * Returns whether an *ENABLED* breakpoint exists at the specified location
    */
   public boolean isLocationBreakpoint(int partID, int lineNum)
   {
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(1,"isLocationBreakpoint: checking if valid breakpoint");
      for (int i=0; i<_breakpoints.size(); i++)
      {
         Object obj = _breakpoints.elementAt(i);
         if (!(obj instanceof LocationBreakpoint))
            continue;

         LocationBreakpoint bkp = (LocationBreakpoint) _breakpoints.elementAt(i);
         if (bkp != null)
            if ((bkp.partID() == partID ) && (bkp.lineNum() == lineNum) && (bkp.isEnabled()))
               return true;
      }
      return false;
   }

  /**
   * check if valid location breakpoint and check its conditional expression
   * @param partID  - part ID
   * @param lineNum - line number for breakpoint
   * @return LocationBreakpoint.LocBkpRet - used to return info when there is an error trying the
   *         conditional expression
   */

   LocationBreakpoint.LocBkpRet isLocationBreakpoint(int partID, int lineNum, boolean chkConditionalExpr)
   {
      LocationBreakpoint.LocBkpRet rc = new LocationBreakpoint.LocBkpRet();

      if(Gdb.traceLogger.DBG) 
         Gdb.traceLogger.dbg(1,"isLocationBreakpoint: checking if valid breakpoint and its conditional expr");
      for (int i=0; i<_breakpoints.size(); i++)
      {
         Object obj = _breakpoints.elementAt(i);
         if (!(obj instanceof LocationBreakpoint))
            continue;

         LocationBreakpoint bkp = (LocationBreakpoint)obj;
         if (bkp != null)
            if ((bkp.partID() == partID ) &&
                (bkp.lineNum() == lineNum) &&
                (bkp.isEnabled()))
               if (chkConditionalExpr)
                  return bkp.isConditionalExprTrue();
               else
               {
                  rc.bkp = bkp;
                  return rc;
               }

      }
      rc.isLocationBkp = false;
      return rc;
   }

   /**
    * Disable a breakpoint
    */
   public void disableBreakpoint(int bkpID) 
   {
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(1,"GdbBreakpointManager.disableBreakpoint ID="+bkpID );

      GdbDebugSession _debugSession = (GdbDebugSession)_debugEngine.getDebugSession();
      String ID = String.valueOf(bkpID+1);
      String cmd = "disable breakpoint "+ID;
      boolean ok = _debugSession.executeGdbCommand(cmd);
      if( ok )
      {
//         _debugSession.getGdbResponseLines();
         _debugSession.addCmdResponsesToUiMessages();
      }

      Breakpoint bkp = (Breakpoint) _breakpoints.elementAt(bkpID-1);

      if (!bkp.isDeferred())
          removeBreakpoint(bkp);

      bkp.disableBreakpoint();
      _changedBreakpoints.addElement(bkp);

   }

   /**
    * Enable a breakpoint
    */
   public void enableBreakpoint(int bkpID) 
   {
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(1,"GdbBreakpointManager.enableBreakpoint ID="+bkpID );
  
      GdbDebugSession _debugSession = (GdbDebugSession)_debugEngine.getDebugSession();
      String ID = String.valueOf(bkpID+1);
      String cmd = "enable breakpoint "+ID;
      boolean ok = _debugSession.executeGdbCommand(cmd);
      if( ok )
      {
//         _debugSession.getGdbResponseLines();
         _debugSession.addCmdResponsesToUiMessages();
      }

      Breakpoint bkp = (Breakpoint) _breakpoints.elementAt(bkpID-1);

      if (!bkp.isDeferred())
          addBreakpoint(bkp);

      bkp.enableBreakpoint();

      _changedBreakpoints.addElement(bkp);
   }

   /**
    * Delete a breakpoint
    */
   public void clearBreakpoint(int bkpID) {
      boolean removeBkp = true;

      Breakpoint bkp = (Breakpoint) _breakpoints.elementAt(bkpID-1);

      if (!bkp.isDeferred())
          removeBreakpoint(bkp);

      bkp.deleteBreakpoint();
      _changedBreakpoints.addElement(bkp);
      _breakpoints.setElementAt(null, bkpID-1);
   }

   /*
    * Add a breakpoint.
    */
   protected void addBreakpoint(Breakpoint bkp)
   {
      if (bkp instanceof LocationBreakpoint)
         addLocationBreakpoint((LocationBreakpoint)bkp);
      else
      {  if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(2,"GdbBreakpointManager.addBreakpoint *NOT* of type LocationBreakpoint" );
      }
   }

   /*
    * Add a location breakpoint.
    */
   protected void addLocationBreakpoint(LocationBreakpoint bkp)
   {
      int partID = bkp.partID();
      int lineNum = bkp.lineNum();

      _debugEngine.getDebugSession().setLineBreakpoint(partID, lineNum);
   }

   /*
    * Remove a breakpoint.
    *
    */
   protected void removeBreakpoint(Breakpoint bkp) {
      if (bkp instanceof LocationBreakpoint)
         removeLocationBreakpoint((LocationBreakpoint)bkp);
      else
      {  if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(2,"GdbBreakpointManager.removeBreakpoint *NOT* of type LocationBreakpoint" );
      }
   }

  /*
    * Remove a location breakpoint if it is safe to do so (there
    * are no other breakpoints at the same location).
    */
   protected void removeLocationBreakpoint(LocationBreakpoint bkp) {
      boolean removeBkp = true;
      int bkpID = bkp.bkpID();
      int partID = bkp.partID();
      int lineNum = bkp.lineNum();

      int i = 0;
      while ((i < _breakpoints.size()) && removeBkp == true) {
         Object obj = _breakpoints.elementAt(i);
         if (obj instanceof LocationBreakpoint)
         {
             LocationBreakpoint b = (LocationBreakpoint)obj;
             if ((i != bkpID-1) && (b.partID() == partID) &&
                 (b.lineNum() == lineNum) && b.isEnabled())
                 removeBkp = false;
         }
         i++;
      }

      if (removeBkp)
         _debugEngine.getDebugSession().clearBreakpoint(partID, lineNum);
   }


   /**
    * Disable all breakpoints
    */
   public void disableAllBreakpoints() {
      for (int i=0; i<_breakpoints.size(); i++) {
         if (_breakpoints.elementAt(i) != null)
            disableBreakpoint(i+1);
      }
   }

   /**
    * Enable all breakpoints
    */
   public void enableAllBreakpoints() {
      for (int i=0; i<_breakpoints.size(); i++) {
         if (_breakpoints.elementAt(i) != null)
            enableBreakpoint(i+1);
      }
   }

   /**
    * Delete all breakpoints
    */
   public void clearAllBreakpoints() {
      for (int i=0; i<_breakpoints.size(); i++) {
         if (_breakpoints.elementAt(i) != null)
            clearBreakpoint(i+1);
      }
   }

   /** Adds the breakpoint change packets to the reply packet */
   public void addChangesToReply(EPDC_Reply rep) {
      if (_changedBreakpoints.size() > 0) 
      {    if (Gdb.traceLogger.EVT) 
               Gdb.traceLogger.evt(2,"................ GdbBreakpointManager.addChangesToReply _changedBreakpoints.size()="+_changedBreakpoints.size());
      }

      for (int i=0; i<_changedBreakpoints.size(); i++)
         rep.addBkpChangePacket(((Breakpoint) _changedBreakpoints.elementAt(i)).getBreakpointChangeItem());

      _changedBreakpoints.removeAllElements();
   }

   /**
    * Clear all breakpoint data -- called for clean up when the debuggee exits
    */
   void clearBreakpointInfo() {
      _breakpoints.removeAllElements();
      _changedBreakpoints.removeAllElements();
   }

   /**
    * return breakpoint corresponding to specified breakpoint ID
    */
   Breakpoint getBreakpoint(int ID)
   {
     return (Breakpoint) _breakpoints.elementAt(ID);
   }

   /**
    * return breakpoint vector
    */
   Vector getBreakpoints() {
      return _breakpoints;
   }

   /**
    * If the deferred breakpoint to be resolved is a line breakpoint,
    * modify the deferred line breakpoint to a real breakpoint
    * If the deferred breakpoint to be resolved is a method breakpoint,
    * find out if it maps to more than one method in the part and if so
    * create new breakpoints for them.
    */
   void enableDeferredBreakpoint(Breakpoint bkp, int partID)
   {
     int bkpID = bkp.bkpID();
     int attr = bkp.attribute();
     attr ^= EPDC.BkpDefer;
     int srcFileIndex = 1;
     int viewNumber = Part.VIEW_SOURCE;
     int lineNumber = 0;

     if (bkp.isLineBreakpoint())
     {
         LineBreakpoint lineBkp = (LineBreakpoint)bkp;

         /************************************************************
         // TODO: Generic defered line breakpoint
         // Do no reuse the bkpID because this breakpoint must be set for
         // every class in every package that matches the source name and
         // line number
         ************************************************************/

         // modify the deferred line breakpoint information
         lineNumber = lineBkp.lineNum();
         lineBkp.modify(bkpID, attr, partID, srcFileIndex, viewNumber,
                        lineNumber, lineBkp.conditionalExpr());
         _changedBreakpoints.addElement(bkp);
     }
     else
     if (bkp.isMethodBreakpoint())
     {
         MethodBreakpoint methodBkp = (MethodBreakpoint)bkp;
         String methodName = methodBkp.entryName();
         String bkpPartName = methodBkp.partName();
         GdbModuleManager moduleManager = (GdbModuleManager)_debugSession.getModuleManager();
         String partName = moduleManager.getFullPartName(partID);
         String partNameFromMethod = methodBkp.getClassFromMethodName();
         boolean isEnabled = methodBkp.isEnabled();

         // If the method name the user provided is the same as the class name
         // the method must be a constructor. Change the name to <init>,
         // otherwise, when finding the list of methods associated with the
         // class the user's method name will not be in the list of methods.
         if (partName != null &&
             (partName.equals(methodName) ||
              partName.endsWith("."+methodName)) ||
             (partNameFromMethod != null &&
              partName.equals(partNameFromMethod)))
         {
             methodName = _debugSession.getResourceString("CONSTRUCTOR");
         }

         // Get the list of methods matching the name in this part
         // For now assume case insensitive
         int[] entryIDs = moduleManager.getEntryIDs(partID, methodName, false);

         if (entryIDs == null || entryIDs.length == 0)
             return;

         /************************************************************
         // TODO: Generic deferred method breakpoint
         // Set method breakpoint for all generic method breakpoints
         *************************************************************/

         lineNumber = _debugSession.setMethodBreakpoint(partID, moduleManager.getMethodIndex(entryIDs[0]));

         // If the deferred breakpoint matched a duplicate deferred
         // breakpoint in the part, remove it
         if (lineNumber < 0)
         {
             clearBreakpoint(bkpID);
             return;
         }

         if (lineNumber > 0)
         {
             methodBkp.modify(bkpID, attr, partID, srcFileIndex,
                              viewNumber, entryIDs[0], lineNumber,
                              methodBkp.conditionalExpr());

             _changedBreakpoints.addElement(bkp);
         }

         int numberOfEntries = entryIDs.length;

         // More than one method matches the deferred method breakpoint name
         // Therefore, set a new breakpoint for all of them.
         if (numberOfEntries > 1)
         {
             for (int i = 1; i < numberOfEntries; i++)
             {
                  setMethodBreakpoint(partID, srcFileIndex, viewNumber,
                                      entryIDs[i] , isEnabled,
                                      methodBkp.conditionalExpr());
             }
         }
     }
   }

}
