/*
 * Copyright (c) 1995, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl;
import  org.eclipse.cdt.debug.gdbPicl.objects.*;

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
	 * Get address of the breakpoint
	 * @return address of the breakpoint if successful
	 * @return null if unsuccessful
	 * 
	 */
	protected String getBreakpointAddress(int gdbBkpId)
	{
		((GdbDebugSession)_debugSession).executeGdbCommand("info breakpoint " + gdbBkpId);
		String[] str = ((GdbDebugSession)_debugSession).getTextResponseLines();        

		int start = str[1].indexOf(" 0x");
		String address=null;
		if (start > 0)
		{
			int end = str[1].indexOf(" in");
			if (end > 0 && end>start)
				address = str[1].substring(start, end);
		}   
		return address; 
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
      
      int gdbBkpID = _debugSession.setLineBreakpoint(partID, lineNum);
      if (gdbBkpID < 0)
         return -1;     

	  String address = getBreakpointAddress(gdbBkpID);

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

      LineBreakpoint lineBkp = new LineBreakpoint(_debugSession, bkpID, gdbBkpID, 0, partID, srcFileIndex, viewNum, lineNum, conditionalExpr );
      lineBkp.setBkpAddress(address);
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(1,"Line breakpoint set");
/*
       String placeHolder = "";
      while (_breakpoints.size() < bkpID)
          _breakpoints.addElement(placeHolder);
*/          
      _breakpoints.addElement(lineBkp);
     
      // if breakpoint should be disabled, then disable it.
      if (!enable)
         disableBreakpoint(bkpID);
      else
         _changedBreakpoints.addElement(lineBkp);
      return 0;
   }

   /**
    * Add an address breakpoint
    * @return 0 if succesful
    * @return -1 if failed
    * @return breakpoint ID of a duplicate breakpoint if this line already has a line breakpoint set.
    */
	public int setAddressBreakpoint(String address,boolean enable,EStdExpression2 conditionalExpr) 
	{
		int partID = 0;
		int srcFileIndex = 1;
		int viewNum = Part.VIEW_SOURCE;
		String lineNum = "1";
		String filename = "";				
		int num=1;
		ModuleManager cm = _debugSession.getModuleManager();

		
		// first make sure there are no other line breakpoints at the same location
		for (int i = 0; i < _breakpoints.size(); i++) {
			Object obj = _breakpoints.elementAt(i);
			if (!(obj instanceof LineBreakpoint))
				continue;
	
			LineBreakpoint bkp = (LineBreakpoint) obj;
			if (bkp != null) {
				if (bkp.getBkpAddress() != null)
				{
					if (bkp.getBkpAddress().equals(address))
					{
						if (Gdb.traceLogger.DBG)
							Gdb.traceLogger.dbg(1, "Duplicate breakpoint");
							return bkp.bkpID();
					}
				}
			}
		}
			
		address = address.trim();
		
		if (!address.startsWith("0x"))
		{
			// this is a line number in mixed view
			// but we have no info about part or filename, so cannot set breakpoint
			// in mixed view, source lines are shown as non-executable line
			// so it's ok if we don't set breakpoint here.
			return -1;
		}
	
		int gdbBkpID = ((GdbDebugSession) _debugSession).setAddressBreakpoint(address);
		if (gdbBkpID < 0)
			return -1;
	
		int bkpID = _breakpoints.size() + 1;
	
		if (Gdb.traceLogger.DBG)
				Gdb.traceLogger.dbg(1,"Address breakpoint set: "+ address);
				
		// get info about this breakpoint filename and line number
		((GdbDebugSession)_debugSession).executeGdbCommand("info breakpoint " + gdbBkpID);
		String[] lines = ((GdbDebugSession)_debugSession).getTextResponseLines();
		
		if (lines.length > 0)
		{
			String line = lines[1];
			String keyword = " at ";
			int x = line.indexOf(keyword);
			if (x > 0)
			{
				line = line.substring(x+keyword.length());
				x = line.indexOf(" ");
				if (x > 0)
				{
					line = line.substring(0, x);
				}
				
				x = line.indexOf(":");
				filename = line.substring(0, x);
				lineNum = line.substring(x+1);
			}
		}
		
		partID = cm.getPartID(filename);
		
		// add part if it does not exist
		if (partID == 0)
		{
			cm.checkPart(1, filename);
			partID = cm.getPartID(filename);
		}
		
		try
		{
			num = Integer.parseInt(lineNum);
		}
		catch (java.lang.NumberFormatException e)
		{
			num = 1;
		}
	
		LineBreakpoint lineBkp = new LineBreakpoint(_debugSession,	bkpID,
				gdbBkpID,0,partID,srcFileIndex,viewNum,num,conditionalExpr);
	
		lineBkp.setBkpAddress(address);
	
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

     // Try setting a regular breakpoint vs deferred
     // if successful, don't have to set deferred breakpoint
	int gdbBkId = super.setLineBreakpoint(fileName,lineNumber,true,conditionalExpr);
	
	if (gdbBkId == 0)
	{
		return 0;
	}

    int bkpID = _breakpoints.size()+1;

     LineBreakpoint bkp = new LineBreakpoint(_debugSession, bkpID, 0, pkgName,
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

	  _numDeferredBkpt++;
	  ((GdbDebugSession)session).getGdbProcess().setStopOnSharedLibEvents(true);

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
	  bkpID--;   	
   	
      if (bkpID < 1 || bkpID > _breakpoints.size())
        return -1;

      LineBreakpoint bkp = (LineBreakpoint) _breakpoints.elementAt(bkpID);

      if (bkp == null)
        return -1;

      ModuleManager cm = _debugSession.getModuleManager();

      // set the new breakpoint
      if (_debugEngine.getDebugSession().setLineBreakpoint(partID, lineNum) < 0)
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

     Breakpoint bkp = (Breakpoint)_breakpoints.elementAt(bkpID);

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

     int bkpID = _breakpoints.size()+1;  //  RW revisit

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

      MethodBreakpoint bkp = (MethodBreakpoint) _breakpoints.elementAt(bkpID);

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

     Breakpoint bkp = (Breakpoint)_breakpoints.elementAt(bkpID);

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
      int bkpID = _debugSession.setWatchpoint(exprString);
      if ( bkpID < 0)
          return -1;

      //int bkpID = _breakpoints.size()+1;

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
   	
   	bkpID--;
   	
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(1,"GdbBreakpointManager.disableBreakpoint ID="+bkpID );

      GdbDebugSession _debugSession = (GdbDebugSession)_debugEngine.getDebugSession();
      Breakpoint bkp = (Breakpoint) _breakpoints.elementAt(bkpID);
//      String ID = String.valueOf(bkpID);
      String ID = String.valueOf(bkp.getGdbBkID());
      String cmd = "disable breakpoint "+ID;
      boolean ok = _debugSession.executeGdbCommand(cmd);
      if( ok )
      {
//         _debugSession.getGdbResponseLines();
         _debugSession.addCmdResponsesToUiMessages();
      }

	  // don't need to remove breakpoint after a disable
//      if (!bkp.isDeferred())
//          removeBreakpoint(bkp);

      bkp.disableBreakpoint();
      _changedBreakpoints.addElement(bkp);

   }

   /**
    * Enable a breakpoint
    */
   public void enableBreakpoint(int bkpID)  
   {
   	
   	   bkpID--;
   	
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(1,"GdbBreakpointManager.enableBreakpoint ID="+bkpID );
  
      GdbDebugSession _debugSession = (GdbDebugSession)_debugEngine.getDebugSession();
      Breakpoint bkp = (Breakpoint) _breakpoints.elementAt(bkpID);
//      String ID = String.valueOf(bkpID);
      String ID = String.valueOf(bkp.getGdbBkID());
      String cmd = "enable breakpoint "+ID;
      boolean ok = _debugSession.executeGdbCommand(cmd);
      if( ok )
      {
//         _debugSession.getGdbResponseLines();
         _debugSession.addCmdResponsesToUiMessages();
      }

//      Breakpoint bkp = (Breakpoint) _breakpoints.elementAt(bkpID);

		// don't need to add breakpoint after a enable
//      if (!bkp.isDeferred())
//          addBreakpoint(bkp);

      bkp.enableBreakpoint();

      _changedBreakpoints.addElement(bkp);
   }

   /**
    * Delete a breakpoint
    */
   public void clearBreakpoint(int bkpID) {

	  bkpID--;

      boolean removeBkp = true;

      Breakpoint bkp = (Breakpoint) _breakpoints.elementAt(bkpID);
      
      if (bkp==null)
      	return;

      if (!bkp.isDeferred())
          removeBreakpoint(bkp);
      else
      	  _numDeferredBkpt--;
          
	  if (((GdbDebugSession)_debugSession).resetStopOnSharedLibEvents())
	  {
   		((GdbDebugSession)_debugSession).getGdbProcess().setStopOnSharedLibEvents(false);
	  }

      bkp.deleteBreakpoint();
      _changedBreakpoints.addElement(bkp);
      _breakpoints.setElementAt(null, bkpID);
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
    * Remove a location breakpoint 
    */
   protected void removeLocationBreakpoint(LocationBreakpoint bkp) {

     int gdbBkID = bkp.getGdbBkID();
      ((GdbDebugSession)_debugEngine.getDebugSession()).clearBreakpoint(gdbBkID);

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
   void enableDeferredBreakpoint(Breakpoint bkp)
   {
     int bkpID = bkp.bkpID();
     int attr = bkp.attribute();
     attr ^= EPDC.BkpDefer;
     int srcFileIndex = 1;
     int viewNumber = Part.VIEW_SOURCE;
     int lineNumber = 0;
     int partID = 0;

     if (bkp.isLineBreakpoint())
     {
         LineBreakpoint lineBkp = (LineBreakpoint)bkp;
         ModuleManager cm = _debugSession.getModuleManager();
         
         cm.checkPart(1, lineBkp.fileName());
         partID = cm.getPartID(1, lineBkp.fileName());

         /************************************************************
         // TODO: Generic defered line breakpoint
         // Do no reuse the bkpID because this breakpoint must be set for
         // every class in every package that matches the source name and
         // line number
         ************************************************************/

         // modify the deferred line breakpoint information
         lineNumber = lineBkp.lineNum();
/*         
         // set the deferred breakpoint
         setLineBreakpoint(lineBkp.fileName(), lineNumber, true, null);
         
         lineBkp.deleteBreakpoint();
      	 _changedBreakpoints.addElement(lineBkp);
	     _breakpoints.setElementAt(null, bkpID-1);
*/
       int gdbBkpID = (((GdbDebugSession)_debugSession).setLineBreakpoint(lineBkp.fileName(), lineNumber));
       
       if (gdbBkpID < 0)
			return;
			
		String address = getBreakpointAddress(gdbBkpID);			

		lineBkp.modify(bkpID, attr, partID, srcFileIndex, viewNumber,
                        lineNumber, lineBkp.conditionalExpr());
        
        lineBkp.setGdbBkID(gdbBkpID);
        lineBkp.setBkpAddress(address);

	    if (!lineBkp.isEnabled())
	    {
	         disableBreakpoint(bkpID);                        
	    }
	    else
	    {
        	 _changedBreakpoints.addElement(bkp);
	    }

		_numDeferredBkpt--;         
     }
     /*
      * Method breakpoints are not fixed at the moment.  Need to re-visit this code
      */
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
   
   /*
    * Enable all deferred breakpoints there were set before running to main.
    * All dlls should be loaded once we have run to main.  Therefore we can try to
    * set the deferred breakpoints after run to main is completed.
    */
   public void enableDeferredBreakpoints()
   { 	
		int num = _breakpoints.size();
   	
		for (int i=0; i<num; i++)
	   	{
	   		Object obj = _breakpoints.elementAt(i);
           if (!(obj instanceof LineBreakpoint))
	            continue;
			if (((Breakpoint)_breakpoints.elementAt(i)).isDeferred())
			{
   				enableDeferredBreakpoint((Breakpoint)_breakpoints.elementAt(i));
	   		}
   		}
   }
   
   /**
	 * Gets the numPendingBkpt.
	 * @return Returns a int
	 */
	public int getNumDeferredBkpt() {
		return _numDeferredBkpt;
	}
	
	/**
	 * Sets the numPendingBkpt.
	 * @param numPendingBkpt The numPendingBkpt to set
	 */
	public void setNumDeferredBkpt(int numDeferredBkpt) {
		_numDeferredBkpt = numDeferredBkpt;
	}
	
	// return 0 if ok
	// otherwise, duplicate breakpoint
	public int setLoadBreakpoint(String dllName, boolean enable)
	{
		int gdbBkId = ((GdbDebugSession) _debugSession).setLoadBreakpoint(dllName);
		/*
			public LoadBreakpoint(
		DebugSession debugSession,
		int bkpID,
		int gdbBkID,
		int bkpType,
		int bkpAttr,
		String dllName) {
		*/

		int bkpID = _breakpoints.size() + 1;
		LoadBreakpoint bkp =
			new LoadBreakpoint(_debugSession, bkpID, gdbBkId, EPDC.LoadBkpType, 0, dllName);

		_breakpoints.addElement(bkp);

		// if breakpoint should be disabled, then disable it.
		if (!enable)
			disableBreakpoint(bkpID);
		else
			_changedBreakpoints.addElement(bkp);
 
		return 0;		
	}
   
   private int _numDeferredBkpt = 0;



}
