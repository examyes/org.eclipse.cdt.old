/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// com/ibm/debug/gdb/objects/VariableMonitor.java, gdb, java-dev
// Version 1.1 (last modified 5/24/01 13:45:49)   (based on Jde 1.42 3/5/01)
/////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.objects;
import  com.ibm.debug.gdbPicl.*;

import com.ibm.debug.epdc.*;
import java.util.*;
import java.text.*;

/**
 * Represents a variable monitor.  Specific monitor types (LocalMonitor, InstanceMonitor,
 * ClassMonitor) descend from this class.
 */
public abstract class VariableMonitor
{
   public VariableMonitor(DebugSession debugSession, int exprID, int monType,
                   Variable monVar, EStdView context, int DU)
   {
      _debugSession  = debugSession;
      _exprID       = exprID;
      _DU           = DU;
      _context      = context;
      _monType      = monType;
      _stackEntry   = 0;
      _monitoredVar = monVar;

      // set the status flags
      if (context.getPPID() > 0)
        _monDeferred        = false;
      else
        _monDeferred        = true;
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

      ModuleManager moduleManager = _debugSession.getModuleManager();

      int entryID = moduleManager.getEntryID(_context.getPPID(), 
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

      if (_context.getPPID() != 0) {
	Part part = moduleManager.getPart(_context.getPPID());
	partName = part.getPartName();
	moduleName = moduleManager.getModuleName(part.getModuleID());
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
					"", 
					entryID);
   }

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
    * Set the deferred state of this monitor
    * @param state if state is true, the monitor is deferred, if false
    *              it is not deferred
    */
   void setDeferredState(boolean state)
   {
      _monDeferred = state;
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
   Variable getMonitoredVariable()
   {
      return _monitoredVar;
   }

   public abstract void updateVariable();
   
   public abstract ExprEvalInfo setValue(int nodeID, String newValue);

   // data fields
   protected DebugSession _debugSession;
   protected Variable    _monitoredVar;
   protected EStdView    _context;

   protected int _exprID;
   protected int _monType;
   protected int _DU;
   protected int _stackEntry;

//   protected static ExprParser parser =
//       new ExprParser(new com.ibm.debug.gdbPicl.StringInputStream(""));

   // status flags
   protected boolean _monDeferred;
   protected boolean _monDeleted;
   protected boolean _monEnabled;
   protected boolean _monEnablementChanged;
   protected boolean _monNew;
   protected boolean _monValuesChanged;
   protected boolean _monTreeStructChanged;
}
