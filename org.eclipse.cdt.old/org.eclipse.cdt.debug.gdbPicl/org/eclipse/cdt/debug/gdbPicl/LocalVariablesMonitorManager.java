/*
 * Copyright (c) 1995, 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl;
import  org.eclipse.cdt.debug.gdbPicl.objects.*;
import  org.eclipse.cdt.debug.gdbPicl.gdbCommands.GetGdbLocals;

import com.ibm.debug.epdc.*;
import java.util.*;
//import sun.tools.debug.*;

/**
 * This class manages variable monitors
 */
public abstract class LocalVariablesMonitorManager extends ComponentManager //extends GdbVariableMonitorManager //HC
{
   int  _exprID = 0;

   public LocalVariablesMonitorManager(DebugSession debugSession)
   {                                     
      super(debugSession);
//      _debugEngine     = debugEngine;
      _monitors        = new Hashtable();
      _changedMonitors = new Vector();
      _threads         = new Vector();
   }

   abstract String[] getCurrentLocals(int DU);

   /*
    * Maintains local variable monitors for all requested threads.  Despite the
    * name, this routine really just adds or deletes monitored variables to
    * the VariableMonitorManager.  The VariableMonitorManager is responsible
    * for updating their values.
    */
   public abstract void updateLocalMonitors();

 
   /**
    *
    */
   void clearLocalVariables()
   {
      _threads.removeAllElements();
   }

   /**
    * Starts monitoring the local variables for the specified thread DU and stack entry
    */
   public abstract void addLocalVariablesMonitor(int DU, int stackEntryNum);

   /**
    * Stops monitoring the local variables for the specified thread DU.  If
    * report is true, send change packets for all monitored variables to
    * the front end.  Otherwise, do not send change packets.
    * AB: for now the stack entry number is ignored,  it is passed in for symmetry
    *     with the addLocalsVariableMonitor method and in the event that it gets
    *     used to allow > 1 locals monitor per thread
    */
   public abstract void removeLocalVariablesMonitor(int DU, int stackEntryNum, boolean report);

   /**
    * Adds change packets for this component to a reply packet
    */
   public void addChangesToReply(EPDC_Reply rep)
   {
//System.out.println("LocalVariablesMonitorManager.addChangesTpReply _changedMonitors.size="+_changedMonitors.size()  );
      for (int i=0; i<_changedMonitors.size(); i++)
      {
         ERepGetNextMonitorExpr changeInfo = ((VariableMonitor)_changedMonitors.elementAt(i)).getMonitorChangeInfo();

         if (changeInfo != null)
            rep.addMonVarChangePacket(changeInfo);
      }

      _changedMonitors.removeAllElements();
   }

   protected  DebugEngine  _debugEngine;

   protected Vector        _threads;            // table of threads for which we are monitoring local variables
   protected Hashtable     _monitors;           // hashtable of variable monitors
   protected Vector        _changedMonitors;    // vector of monitors with changes
}
