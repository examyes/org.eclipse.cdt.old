/*
 * Copyright (c) 1995, 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.objects;
import  org.eclipse.cdt.debug.gdbPicl.*;

import com.ibm.debug.epdc.*;
import java.util.*;
import java.text.*;

/**
 * Represents a variable monitor.  Specific monitor types (LocalMonitor, InstanceMonitor,
 * ClassMonitor) descend from this class.
 */
abstract class Gdb_VariableMonitor
{
   Gdb_VariableMonitor(GdbDebugSession debugSession, int exprID, int monType, GdbVariable monVar, EStdView context, int DU)
   {
      _debugSession  = debugSession;
      _exprID       = exprID;
      _DU           = DU;
      _context      = context;
      _monType      = monType;
      _stackEntry   = 0;
      _monitoredVar = monVar;

      // set the status flags
      _monDeleted           = false;
      _monEnabled           = true;
      _monNew               = true;
      _monValuesChanged     = true;
      _monEnablementChanged = false;
      _monTreeStructChanged = false;
   }

   /**
    * Return whether this monitor's variable values have changed
    */
   public boolean hasChanged()
   {
      return _monValuesChanged | _monEnablementChanged | _monTreeStructChanged;
   }
   
   /**
    * Return this monitor's type
    */
   public int getMonitorType()
   {
      return _monType;
   }
   
   /**
    * Set the representation for this monitored variable.
    * @param nodeID the variable's tree node ID.
    * @param newRep the new representation
    */
   public void setRepresentation(int nodeID, int newRep)
   {
      _monitoredVar.setRepresentation(nodeID, newRep);
      _monValuesChanged = true;
   }

   /**
    * Get the REpGetNextMonitorExpr item for this monitor.  If the thread 
    * associated with this monitor is no longer active, null is returned.
    */
   public ERepGetNextMonitorExpr getMonitorChangeInfo()
   {
      ThreadComponent tc = 
	_debugSession.getThreadManager().getThreadComponent(_DU);

      GdbModuleManager cm = (GdbModuleManager)_debugSession.getModuleManager();

      int entryID = cm.getEntryID(_context.getPPID(), 
				  _context.getLineNum());

      short flags = _monEnabled ? (short) EPDC.MonEnabled : 0;
      flags |= _monEnablementChanged ? (short) EPDC.MonEnablementChanged : 0;
      flags |= (_monValuesChanged && !_monDeleted) ? (short) EPDC.MonValuesChanged : 0;
      flags |= _monDeleted ? (short) EPDC.MonDeleted : 0;
      flags |= _monNew ? (short) (EPDC.MonNew | EPDC.MonNameChanged) : 0;
      flags |= _monTreeStructChanged ? (short) (EPDC.MonTreeStructChanged) : 0;

      _monNew = 
	_monValuesChanged = 
	_monEnablementChanged = 
	_monTreeStructChanged = false;

      String partName = "";
      String moduleName = "";
      String viewFileName = "";

      if (_context.getPPID() != 0) {
	Part part = cm.getPart(_context.getPPID());
   if (Gdb.traceLogger.DBG) 
       Gdb.traceLogger.dbg(2,"Gdb_VariableMonitor.getMonitorChangeInfo _context.getPPID="+_context.getPPID()+" part="+part );
   if(part!=null)
	{  //partName = part.getPartName();
      partName = part.getFullPartName();
	   moduleName = cm.getModuleName(part.getModuleID());
      View vw = part.getView(_context.getViewNo());
      viewFileName = vw.getViewFileName();
      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(3,"Gdb_VariableMonitor.getMonitorChangeInfo partName="+partName+" moduleName="+moduleName +" viewFileName="+viewFileName );
	}
      }

      // !!! Use partID to fill in module, part and file for constructor
      // !!! For now, we use empty strings
      return new ERepGetNextMonitorExpr((short) _exprID, 
					_monitoredVar.getTreeNode(), 
					_context, 
					"",
					_monitoredVar.getName(), 
					_DU, 
					(short) _stackEntry, 
					flags, 
					_monType, 
					moduleName,
					partName,
					viewFileName, 
					entryID);
   }

   public abstract void updateVariable();

   /**
    * Enable this monitor
    */
   public void enableMonitor()
   {
      _monEnabled = true;
      _monEnablementChanged = true;
   }

   /**
    * Disable this monitor
    */
   public void disableMonitor()
   {
      _monEnabled = false;
      _monEnablementChanged = true;
   }

   /**
    * Mark this monitor as deleted
    */
   public void deleteMonitor()
   {
      _monDeleted = true;
   }

   /**
    * Expand the subtree of this variable monitor
    */
   public void expandSubTree(int rootNodeID, int startChild, int endChild) {
      _monitoredVar.expandSubTree(rootNodeID, startChild, endChild);
   }

   /**
    * Expand the subtree of this variable monitor
    */
   public void collapseSubTree(int rootNodeID, int startChild, int endChild) {
      _monitoredVar.collapseSubTree(rootNodeID, startChild, endChild);
   }

   /**
    * Get the Variable being monitored.
    */
   public GdbVariable getMonitoredVariable()
   {
      return _monitoredVar;
   }

   // data fields
   protected GdbDebugSession _debugSession;
   protected GdbVariable    _monitoredVar;
   protected EStdView    _context;

   protected int _exprID;
   protected int _monType;
   protected int _DU;
   protected int _stackEntry;

   // status flags
   public boolean isMonDeleted()
   {   return _monDeleted;  }
   protected boolean _monDeleted;
   public boolean isMonEnabled()
   {   return _monEnabled;  }
   protected boolean _monEnabled;
   protected boolean _monEnablementChanged;
   protected boolean _monNew;
   protected boolean _monValuesChanged;
   protected boolean _monTreeStructChanged;
}
