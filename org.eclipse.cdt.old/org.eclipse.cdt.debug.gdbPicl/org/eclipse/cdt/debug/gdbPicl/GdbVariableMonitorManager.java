/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package com.ibm.debug.gdbPicl;
import  com.ibm.debug.gdbPicl.objects.*;

import com.ibm.debug.epdc.*;
import java.util.*;

/**
 * This class manages variable monitors
 */
public class GdbVariableMonitorManager extends VariableMonitorManager
{
   GdbDebugSession _debugSession = null;
   public GdbVariableMonitorManager(GdbDebugSession debugSession)    {
     super (debugSession);
      _debugSession     = debugSession;
     // _monitors        = new Hashtable();
     // _changedMonitors = new Vector();
     // _exprID          = 0;
   }

   GdbVariableMonitorManager(short monType, String exprString,
                                       String fullPartName, EStdView context,
                                       int du)
   {
       if (Gdb.traceLogger.ERR)
           Gdb.traceLogger.err(2,"######## BYPASSING GdbVariableMonitorManager.GdbVariableMonitorManager exprString="+exprString +" fullPartName="+fullPartName  );
   }

   int addVariableMonitor(short monType, GdbVariable monVar, EStdView context, int du)
   {
      GdbVariableMonitor vm = null;

      _exprID = _exprID + 1;
      if (Gdb.traceLogger.DBG)
          Gdb.traceLogger.dbg(2,"GdbVariableMonitorManager.addVariableMonitor monVar="+monVar.getName()+" du="+du+" _exprID="+_exprID    );

      vm = new GdbVariableMonitor(_debugSession, _exprID, monType, monVar, context, du);

      _monitors.put(new Integer(_exprID),vm);
      if (!_changedMonitors.contains(vm))
         _changedMonitors.addElement(vm);

      return _exprID;
   }
   int addVariableMonitor(short monType, Variable monVar, EStdView context, int du)
   {
     if(monVar instanceof GdbVariable)
        return addVariableMonitor( monType, (GdbVariable)monVar, context, du);
     else
     {
        if (Gdb.traceLogger.ERR)
            Gdb.traceLogger.err(2,"######## UNIMPLEMENTED GdbVariableMonitorManager.addVariableMonitor monVar.getName="+monVar.getName()  );
        return 0;
     }
   }
   public ExprEvalInfo evaluateExpression(String exprString, EStdView context, int du, boolean evalToField)
   {
      if (Gdb.traceLogger.DBG)
          Gdb.traceLogger.dbg(2,"GdbVariableMonitorManager.evaluateExpression exprString="+exprString +" du="+du );

      GdbExprEvalInfo evalInfo = null;
      evalInfo = GdbVariableMonitor.evaluateExpression(_debugSession, exprString, context, du);

      if (evalInfo.expressionFailed())
          if (Gdb.traceLogger.ERR)
              Gdb.traceLogger.err(2,"$$$$$$$$$$$$$$$$ GdbVariableMonitorManager.addExpression evalInfo.expressionFailed || isDeferred");

      return evalInfo;
   }
   public ExprEvalInfo checkConditionalExpr(EStdExpression2 conditionalExpr)
   {
      if (Gdb.traceLogger.ERR)
          Gdb.traceLogger.err(2,"######## UNIMPLEMENTED GdbVariableMonitorManager.checkConditionalExpr conditionalExpr. getExprString="+conditionalExpr );
      return null;
   }
   public void addDeferredExpression(short monType, String exprString,
                                       String fullPartName, EStdView context,
                                       int du)
   {
      if (Gdb.traceLogger.ERR)
          Gdb.traceLogger.err(2,"######## UNIMPLEMENTED (treated as non-deferred) - GdbVariableMonitorManager.addDeferredExpression exprString="+exprString
                    +" fullPartName="+fullPartName
                    +" context.getPPID()="+context.getPPID()
                    +" context.getSrcFileIndex()="+context.getSrcFileIndex()
                    +" context.getViewNo()="+context.getViewNo()
                    +" context.getLineNum()="+context.getLineNum()
                    );

      //View v = part.getView(int viewNum);
      //String pn = part.getPartName();
      //String n = v.getViewFileName()
      //String b = v.getBaseViewFileName()

      GdbModuleManager mm = (GdbModuleManager) _debugSession.getModuleManager();
      int[] partIDs = mm.getPartIDs(1);
      for(int x=0; x<partIDs.length; x++)
      {
          GdbPart p = (GdbPart) mm.getPart(partIDs[x]);
          String n = p.getName();
          String s = p.getSourceFileName();
          View v = p.getView(context.getViewNo());
//          int sfi = v.getSrcFileIndex();
      }

      boolean isDeferred = false;  //  true;  //???????????????????
      ExprEvalInfo evalInfo = addExpression(monType, exprString, context, du, isDeferred);
      if (evalInfo.expressionFailed() )// ????????? && !_req.isDeferred())
      {
        if (Gdb.traceLogger.ERR)
            Gdb.traceLogger.err(2,_debugEngine.getResourceString("ADD_DEFERRED_EXPRESSION_EVALUATION_FAILED")+exprString);
        String msg = evalInfo.whyFailed(_debugEngine);
//	_rep.setMessage(msg);
//	_rep.setReturnCode(EPDC.ExecRc_BadExpr);
      return;
      }

//      addChangesToReply(_rep);
      return;
  }


   /**
    * Modify a variable monitor
    */
   public void modifyMonitor(short exprID, String exprValue)
   {
      GdbVariableMonitor monitor = (GdbVariableMonitor) _monitors.get(new Integer(exprID));
      if (monitor != null)
      {
         if (monitor.isMonDeleted() || !monitor.isMonEnabled())
            return;

         ERepGetNextMonitorExpr e = monitor.getMonitorChangeInfo();
         String ID = String.valueOf(exprID);
         String exprName = monitor.getMonitoredVariable().getName();
         String cmd = "set variable "+exprName+"="+exprValue;
         boolean ok = _debugSession.executeGdbCommand(cmd);
         if( ok )
         {
//            _debugSession.getGdbResponseLines();
            _debugSession.addCmdResponsesToUiMessages();
            _debugSession.cmdResponses.removeAllElements();
            cmd = "display ";
            ok = _debugSession.executeGdbCommand(cmd);
            if( ok )
            {
//               _debugSession.getGdbResponseLines();
               _debugSession.addCmdResponsesToUiMessages();
               _debugSession.cmdResponses.removeAllElements();
            }
         }
         updateMonitors();
/*
         monitor.getMonitoredVariable().setScalarValue(exprValue);
         monitor.modifyMonitorValue();
         monitor.updateVariable();
         if (!_changedMonitors.contains(monitor))
            _changedMonitors.addElement(monitor);
*/
      }
   }



   /**
    * Enable a variable monitor
    */
   public void enableMonitor(int exprID)
   {
      GdbVariableMonitor monitor = (GdbVariableMonitor) _monitors.get(new Integer(exprID));
      if (monitor != null)
      {
         String ID = String.valueOf(exprID);
         String cmd = "enable display "+ID;
         boolean ok = _debugSession.executeGdbCommand(cmd);
         if( ok )
         {
//            _debugSession.getGdbResponseLines();
            _debugSession.addCmdResponsesToUiMessages();
         }
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
      GdbVariableMonitor monitor = (GdbVariableMonitor) _monitors.get(new Integer(exprID));
      if (monitor != null)
      {
         String ID = String.valueOf(exprID);
         String cmd = "disable display "+ID;
         boolean ok = _debugSession.executeGdbCommand(cmd);
         if( ok )
         {
//            _debugSession.getGdbResponseLines();
            _debugSession.addCmdResponsesToUiMessages();
         }

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
      GdbVariableMonitor monitor = (GdbVariableMonitor) _monitors.get(new Integer(exprID));
      if (monitor != null)
      {
         monitor.deleteMonitor();
         _monitors.remove(new Integer(exprID));
         if (!_changedMonitors.contains(monitor) && report)
         {
             _changedMonitors.addElement(monitor);
         }
      }
   }

   /**
    * Update monitors
    */
   public void updateMonitors()
   {
      // First tell the LocalVariablesMonitorManager to update all local vars
      _debugSession.getLocalVariablesMonitorManager().updateLocalMonitors();

      int length = _debugSession.monitorChangedID.size();
      
      if (length !=0 )
      {
         Enumeration elements = _monitors.elements();
         // Now cycle through all known monitors and update their values
         while (elements.hasMoreElements())
         {
            GdbVariableMonitor monitor = (GdbVariableMonitor) elements.nextElement();
            if ((monitor != null) && (monitor.getMonitorType() == EPDC.MonTypeProgram))
            {
               GdbVariable monVar = monitor.getMonitoredVariable();
               if (Gdb.traceLogger.DBG)
                   Gdb.traceLogger.dbg(3,"GdbVariableMonitorManager.updateMonitors nodeID="+monVar.getNodeID()+" name="+monVar.getName() );

               for(int i=0; i<length; i++)
               {
                  String exprNumbr = (String)_debugSession.monitorChangedID.elementAt(i);
                  int nmbr = -1;
                  if(exprNumbr!=null)
                     nmbr = Integer.parseInt(exprNumbr);
                  if(nmbr==monVar.getNodeID() )
                  {
                     String exprName = (String)_debugSession.monitorChangedName.elementAt(i);
                     String exprValue = (String)_debugSession.monitorChangedValue.elementAt(i);
                     if (Gdb.traceLogger.EVT)
                         Gdb.traceLogger.evt(2,"GdbVariableMonitorManager.updateMonitors i="+i+" exprNumbr="+exprNumbr+" exprName="+exprName+" exprValue="+exprValue   );

                     monitor.getMonitoredVariable().setScalarValue(exprValue);
                     monitor.updateVariable();

                     _debugSession.monitorChangedID.setElementAt(null, i);

                     // Only add this variable if it has changed since last update
                     if (monitor.hasChanged() && !_changedMonitors.contains(monitor))
                        _changedMonitors.addElement(monitor);
                  }
               }
            }
         }
      }
      
      if (Gdb.traceLogger.DBG)
      {
         for(int i=0; i<length; i++)
         {
            String exprNumbr = (String)_debugSession.monitorChangedID.elementAt(i);
            if(exprNumbr!=null)
            {
               String exprName = (String)_debugSession.monitorChangedName.elementAt(i);
               String exprValue = (String)_debugSession.monitorChangedValue.elementAt(i);
               Gdb.traceLogger.dbg(2,"GdbVariableMonitorManager.updateMonitors GdbMonitor NOT FOUND i="+i+" exprNumbr="+exprNumbr+" exprName="+exprName+" exprValue="+exprValue   );
            }
         }
      }

     _debugSession.monitorChangedID.removeAllElements();
     _debugSession.monitorChangedName.removeAllElements();
     _debugSession.monitorChangedValue.removeAllElements();
   }

   /** Adds change packets for this component to a reply packet */
   public void addChangesToReply(EPDC_Reply rep)
   {
      if( _changedMonitors.size()<=0 )
          return;

      // HACK ##################################################################
      if(_debugSession.getProgramName().equals("TestJava") )
      {
          if (Gdb.traceLogger.ERR)
              Gdb.traceLogger.err(2,"######## HACK: GdbVariableMonitorManager.addChangesToReply BYPASSING MONITORS programName="+_debugSession.getProgramName() );
      _changedMonitors.removeAllElements();
      }
      // HACK ##################################################################

      if(_changedMonitors.size()>0)
         if (Gdb.traceLogger.EVT)
             Gdb.traceLogger.evt(1,"................ GdbVariableMonitorManager.addChangesToReply _changedMonitors.size="+_changedMonitors.size() );
      for (int i=0; i<_changedMonitors.size(); i++)
      {
         ERepGetNextMonitorExpr changeInfo = ((GdbVariableMonitor)_changedMonitors.elementAt(i)).getMonitorChangeInfo();

         if (changeInfo != null)
         {   rep.addMonVarChangePacket(changeInfo);
             if (Gdb.traceLogger.EVT)
                 Gdb.traceLogger.evt(2,"GdbVariableMonitorManager.addChangesToReply changeInfo.getExpressionString()="+changeInfo.getExpressionString() );
         }
      }

      _changedMonitors.removeAllElements();
   }

  /**
   * Attempts to add the expression to this variable monitor manager. Returns
   * an instance of ExprEvalInfo for this expression.  The expression is
   * added only if the evaluation succeeded.
   */
   public ExprEvalInfo addExpression(short monType, String exprString, EStdView context, int du, boolean isDeferred)
   {
      if (Gdb.traceLogger.DBG)
          Gdb.traceLogger.dbg(2,"GdbVariableMonitorManager.addExpression exprString="+exprString +" monType="+monType );

      GdbExprEvalInfo evalInfo = null;
      evalInfo = GdbVariableMonitor.evaluateExpression(_debugSession, exprString, context, du);

      if (!evalInfo.expressionFailed() || isDeferred)
      {
         String value = evalInfo.getInitialValue();
         if(value==null)
         {   value = "unknown_value";
         }
         else
         {
            String type = evalInfo.getType();
            GdbVariable monVar = GdbVariable.createVariable(_debugSession, exprString, type, value, evalInfo.getNodeID() );

            if (isDeferred && evalInfo.expressionFailed())
              monVar.setScope(false);

            addVariableMonitor(monType, monVar, context, du);
         }
      }
      else
      {
          if (Gdb.traceLogger.ERR)
              Gdb.traceLogger.err(2,"$$$$$$$$$$$$$$$$ GdbVariableMonitorManager.addExpression evalInfo.expressionFailed || isDeferred");
      }

      return evalInfo;
   }

   /**
    * Set the representation type for this monitor
    * @param exprID the expression ID
    * @param nodeID the variable's tree node ID.  This is currently not used because only simple variables
    * can be monitored.
    * @param newRep the new representation
    */
   public void setRepresentation(int exprID, int nodeID, int newRep)
   {
      GdbVariableMonitor monitor = (GdbVariableMonitor) _monitors.get(new Integer(exprID));

      if (monitor != null)
      {
         monitor.setRepresentation(nodeID, newRep);
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
      if (Gdb.traceLogger.ERR)
          Gdb.traceLogger.err(2,"GdbVariableMonitorManager setValue called (WHY NOT MODIFYMONITOR????) ");
      modifyMonitor((short)exprID, newValue);
      return null;
/*
      GdbVariableMonitor monitor = (GdbVariableMonitor) _monitors.get(new Integer(exprID));
      ExprEvalInfo result = null;

      if (monitor != null)
      {
         result = monitor.setValue(rootNodeID, newValue);

         if(!result.expressionFailed()) {
             //
             // Since we don't know what monitors depend on the
             // value we changed, we must update them all.
             //
             updateMonitors();
         }
      }
      return result;
*/
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
      GdbVariableMonitor monitor = (GdbVariableMonitor) _monitors.get(new Integer(exprID));

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
      GdbVariableMonitor monitor = (GdbVariableMonitor) _monitors.get(new Integer(exprID));

      if (monitor != null)
      {
         monitor.collapseSubTree(rootNodeID, startChild, endChild);
         // update the variable monitor
         monitor.updateVariable();
         if (!_changedMonitors.contains(monitor))
            _changedMonitors.addElement(monitor);
      }
   }

}

