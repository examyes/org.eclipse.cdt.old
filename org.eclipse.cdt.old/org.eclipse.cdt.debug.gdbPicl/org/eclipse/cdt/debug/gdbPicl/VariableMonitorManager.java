/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// com/ibm/debug/gdb/VariableMonitorManager.java, gdb, java-dev
// Version 1.1 (last modified 5/24/01 13:42:03)   (based on Jde 1.26 1/23/01)
/////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl;
import  com.ibm.debug.gdbPicl.objects.*;

import com.ibm.debug.epdc.*;
import java.util.*;

/**
 * This class manages variable monitors
 */
public abstract class VariableMonitorManager extends ComponentManager      //HC
{
	
	public VariableMonitorManager()
	{
	}
		
   public VariableMonitorManager(DebugSession debugSession)
   {
      super(debugSession);
      _monitors        = new Hashtable();
      _changedMonitors = new Vector();
      _exprID          = 0;
   }

  /**
   *
   */
   void clearVariables()
   {
      _monitors.clear();
      _changedMonitors.removeAllElements();
   }

   /**
    * Adds change packets for this component to a reply packet
    */
   public void addChangesToReply(EPDC_Reply rep)
   {
      for (int i=0; i<_changedMonitors.size(); i++)
      {
         ERepGetNextMonitorExpr changeInfo = ((VariableMonitor)_changedMonitors.elementAt(i)).getMonitorChangeInfo();

         if (changeInfo != null)
            rep.addMonVarChangePacket(changeInfo);
      }

      _changedMonitors.removeAllElements();
   }

   /**
    * Enable a variable monitor
    */
   VariableMonitor getMonitor(int exprID)
   {
      VariableMonitor vm = null;
      Object o = _monitors.get(new Integer(exprID));
      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(1,"VariableMonitorManager.getMonitor exprID="+exprID +" object="+o );
      if(o!=null)
         vm = (VariableMonitor) o;
      return vm;
   }

   /**
    * Enable a variable monitor
    */
   public void enableMonitor(int exprID)
   {
      VariableMonitor monitor = (VariableMonitor) _monitors.get(new Integer(exprID));
      if (monitor != null)
      {
         monitor.enableMonitor();
         monitor.updateVariable();
         if (!_changedMonitors.contains(monitor))
            _changedMonitors.addElement(monitor);
      }
   }

   /**
    * Disable a variable monitor
    */
   public void disableMonitor(int exprID)
   {
      VariableMonitor monitor = (VariableMonitor) _monitors.get(new Integer(exprID));
      if (monitor != null)
      {
         monitor.disableMonitor();
         if (!_changedMonitors.contains(monitor))
            _changedMonitors.addElement(monitor);
      }
   }

   /**
    * Delete a variable monitor.  If report is true, the deletion is reported
    * via change packet to the front end.  Otherwise, no change packet is
    * sent.
    */
   public void deleteMonitor(int exprID, boolean report)
   {
      VariableMonitor monitor = (VariableMonitor) _monitors.get(new Integer(exprID));
      if (monitor != null)
      {
         monitor.deleteMonitor();
         _monitors.remove(new Integer(exprID));
         if (!_changedMonitors.contains(monitor) && report)
            _changedMonitors.addElement(monitor);
      }
   }

   /**
    * Update monitors
    */
   public void updateMonitors()
   {
      // First tell the LocalVariablesMonitorManager to update all local vars
      _debugSession.getLocalVariablesMonitorManager().updateLocalMonitors();

      // Now cycle through all known monitors and update their values
      Enumeration elements = _monitors.elements();
      while (elements.hasMoreElements())
      {
         VariableMonitor monitor = (VariableMonitor) elements.nextElement();
         if (monitor != null)
         {
            monitor.updateVariable();

            // Only add this variable if it has changed since last update
            if (monitor.hasChanged() && !_changedMonitors.contains(monitor))
               _changedMonitors.addElement(monitor);
         }
      }
   }

   /**
    * Set the representation type for this monitor
    * @param exprID the expression ID
    * @param nodeID the variable's tree node ID.  This is currently not used
    * because only simple variables can be monitored.
    * @param newRep the new representation
    */
   public void setRepresentation(int exprID, int nodeID, int newRep)
   {
      VariableMonitor monitor = (VariableMonitor) _monitors.get(new Integer(exprID));

      if (monitor != null)
      {
         monitor.setRepresentation(nodeID, newRep);
         if (!_changedMonitors.contains(monitor))
            _changedMonitors.addElement(monitor);
      }
   }

   /**
    * Expand a variable monitor's subtree
    * @param exprID the expression ID of the monitor to expand
    * @param rootNodeID the root node ID to expand
    * @param startChild the start child of the subtree
    * @param endChild the end child of the subtree
    */
   public void expandSubTree(int exprID, int rootNodeID, int startChild, int endChild)
   {
      VariableMonitor monitor = (VariableMonitor) _monitors.get(new Integer(exprID));

      if (monitor != null)
      {
         monitor.expandSubTree(rootNodeID, startChild, endChild);
         // update the variable monitor
         monitor.updateVariable();
         if (!_changedMonitors.contains(monitor))
            _changedMonitors.addElement(monitor);
      }
   }

   /**
    * Collapse a variable monitor's subtree
    * @param exprID the expression ID of the monitor to collapse
    * @param rootNodeID the root node ID to collapse
    * @param startChild the start child of the subtree
    * @param endChild the end child of the subtree
    */
   public void collapseSubTree(int exprID, int rootNodeID, int startChild, int endChild)
   {
      VariableMonitor monitor = (VariableMonitor) _monitors.get(new Integer(exprID));

      if (monitor != null)
      {
         monitor.collapseSubTree(rootNodeID, startChild, endChild);
         // update the variable monitor
         monitor.updateVariable();
         if (!_changedMonitors.contains(monitor))
            _changedMonitors.addElement(monitor);
      }
   }

   /**
    * Replace a variable's value.
    * @param exprID the expression ID of the monitor
    * @param rootNodeID the root node ID to set
    * @param newValue the new value
    */
   ExprEvalInfo setValue(int exprID, int rootNodeID, String newValue)
   {
      VariableMonitor monitor = (VariableMonitor) _monitors.get(new Integer(exprID));
      ExprEvalInfo result = null;

      if (monitor != null)
      {
         result = monitor.setValue(rootNodeID, newValue);

         if(!result.expressionFailed()) {
             /*
              * Since we don't know what monitors depend on the
              * value we changed, we must update them all.
              */
             updateMonitors();
         }
      }
      return result;
   }

  /**
   * Attempts to add the expression to this variable monitor manager. Returns
   * an instance of ExprEvalInfo for this expression.  The expression is
   * added only if the evaluation succeeded or it is deferred.
   */
   public abstract ExprEvalInfo addExpression(short monType, String exprString,
                                       EStdView context, int du,
                                       boolean isDeferred);

  /**
   * Add the expression as a deferred expression.  This is called when the
   * part associated with the expression has not yet been loaded (i.e. the
   * context is not complete) so the expression must be deferred.
   */
   public abstract void addDeferredExpression(short monType, String exprString,
                                       String fullPartName, EStdView context,
                                       int du);

   /**
   * Add the variable as a variable monitor of the given type
   * @return the monitor ID or 0 if a variable for the given name cannot be
   * found.
   * Assumes evalInfo is a validated expression.
   */
   abstract int addVariableMonitor(short monType, Variable monVar, EStdView context, int du);

   /**
    * Evaluate and expression - do not add as a monitored expression.
    */
   public abstract ExprEvalInfo evaluateExpression(String exprString, EStdView context, int du, boolean evalToField);

   public abstract ExprEvalInfo checkConditionalExpr(EStdExpression2 conditionalExpr);

   // data fields
   protected int _exprID;

   protected Hashtable _monitors;           // hashtable of variable monitors
   protected Vector    _changedMonitors;    // vector of monitors with changes
                                            // since the last change packet
}
