/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// com/ibm/debug/gdb/RegisterManager.java, gdb, java-dev
// Version 1.1 (last modified 5/24/01 13:42:00)   (based on Jde 1.26 1/23/01)
/////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl;
import  com.ibm.debug.gdbPicl.objects.*;

import com.ibm.debug.epdc.*;
import java.util.*;

/**
 * This class manages variable registers
 */
public class RegisterManager extends ComponentManager      //HC
{
   public RegisterManager(DebugSession debugSession)
   {
      super(debugSession);
      _registersMonitors = new Hashtable(100);
      _changedRegisters  = new Vector();
   }

   /**
    * Adds change packets for this component to a reply packet
    */
   public void addChangesToReply(EPDC_Reply rep)
   { 
      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(2,"................ RegisterManager.addChangesToReply _changedRegisters.size="+_changedRegisters.size() );

      for (int i=0; i<_changedRegisters.size(); i++)
      {
         RegistersMonitor monitor = (RegistersMonitor)_changedRegisters.elementAt(i);
         ERepGetNextRegister changeInfo = monitor.getRegisterChangeInfo(rep);

         if (changeInfo != null)
         {   rep.addRegisterChangePacket(changeInfo);
             if (Gdb.traceLogger.DBG) 
                 Gdb.traceLogger.dbg(2,"................ RegisterManager.addChangesToReply name="+changeInfo.getName()+" value="+changeInfo.getValue()+" isNew="+changeInfo.isNew() );
         }
      }

      _changedRegisters.removeAllElements();
   }

  /**
   * Monitor the indicated thread's registers and send register change 
   * packets to SUI when necessary
   * @param DU the unique thread identification number
   */
   public void monitorRegisters(int DU, int groupID)
   {
      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(2,"RegisterManager.monitorRegisters DU="+DU+" groupID="+groupID );

      GdbRegistersMonitor monitor = (GdbRegistersMonitor)_registersMonitors.get(new Integer(DU));
      if(monitor==null)
         monitor = new GdbRegistersMonitor(_debugSession, DU);
      _registersMonitors.put(new Integer(DU), monitor);
      monitor.trackGroupID(groupID);
      monitor.updateRegisters();
      if (monitor.hasChanged() && !_changedRegisters.contains(monitor))
          _changedRegisters.addElement(monitor);

      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(1,"RegisterManager.monitorRegisters _registersMonitors.size()="+_registersMonitors.size()  );

   }

  /**
   * Stop monitoring the indicated thread's registers
   * @param DU the unique thread identification number
   */
   public void freeRegisters(int DU, int groupID)
   {
      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(2,"RegisterManager.freeRegisters DU="+DU+" groupID="+groupID );
          
      GdbRegistersMonitor monitor = (GdbRegistersMonitor)_registersMonitors.get(new Integer(DU));
      if(monitor==null)
         return; // happens when ui collapses thread and sends free of all groups (even if not expanded)
      monitor.freeGroupID(groupID);
      if (monitor.hasChanged() && !_changedRegisters.contains(monitor))
          _changedRegisters.addElement(monitor);
      if(monitor.isEmpty())
         _registersMonitors.remove(new Integer(DU));
   }

  /**
   * Clear list of registers
   */
   void clearRegisters()
   {
      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(2,"RegisterManager.clearRegisters ");
      _registersMonitors.clear();
   }

   /**
    * Update registers
    */
   public void updateRegisters()
   {
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(2,"RegisterManager.updateRegisters _registersMonitors.size="+_registersMonitors.size() );

      // Now cycle through all known registersMonitors and update their values
      Enumeration elements = _registersMonitors.elements();
      while (elements.hasMoreElements())
      {
         RegistersMonitor monitor = (RegistersMonitor)elements.nextElement(); 
         if (monitor != null)
         {
            monitor.updateRegisters();

            // Only add this registerMonitor if it has changed since last update
            if (monitor.hasChanged() && !_changedRegisters.contains(monitor))
               _changedRegisters.addElement(monitor);
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
/*
   public void setRepresentation(int exprID, int nodeID, int newRep)
   {
      RegisterMonitor monitor = (RegisterMonitor) _registers.get(new Integer(exprID));

      if (monitor != null)
      {
         monitor.setRepresentation(nodeID, newRep);
         if (!_changedRegisters.contains(monitor))
            _changedRegisters.addElement(monitor);
      }
   }
*/

   /**
    * Replace a monitors's value.
    * @param exprID the expression ID of the register
    * @param rootNodeID the root node ID to set
    * @param newValue the new value
    */
   ExprEvalInfo setValue(int exprID, int rootNodeID, String newValue)
   {
      if (Gdb.traceLogger.ERR) 
          Gdb.traceLogger.err(1,"######## RegisterManager.setValue UNIMPLEMENTED, IGNORED " );
      return null;

/*
      RegistersMonitor monitor = (RegistersMonitor) _registers.get(new Integer(exprID));
      ExprEvalInfo result = null;

      if (monitor != null)
      {
         result = monitor.setValue(rootNodeID, newValue);
      }
      return result;
*/
   }

   // data fields
   protected Hashtable    _registersMonitors;   // hashtable of thread registersMonitors
   protected Vector      _changedRegisters;
}
