/*
 * Copyright (c) 1995, 2002 International Business Machines Corporation. All rights reserved.
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
public class GdbLocalVariablesMonitorManager extends LocalVariablesMonitorManager
{
   GdbThreadManager   tm = null;
   GdbModuleManager   cm = null;
   GdbVariableMonitorManager  vmm = null;
   GetGdbLocals _getGdbLocals = null;
   int  _exprID = 0;

   public GdbLocalVariablesMonitorManager(GdbDebugSession debugSession)
   {
      super(debugSession);
      _getGdbLocals    = new GetGdbLocals(debugSession); 
   }

   String[] getCurrentLocals(int DU)
   {
      String [] _gdbLocals = _getGdbLocals.getLocals(DU);
      return _gdbLocals;
   }

   /*
    * Maintains local variable monitors for all requested threads.  Despite the
    * name, this routine really just adds or deletes monitored variables to
    * the VariableMonitorManager.  The VariableMonitorManager is responsible
    * for updating their values.
    */
   public void updateLocalMonitors()
   {
      int i,j;
      int                numLocals;

      GdbThreadComponent    tc;
      GdbStackFrame[]    stack;
      GdbStackFrame      stackFrame;
      ThreadStackInfo    stackInfo;
      tm = (GdbThreadManager)_debugSession.getThreadManager();
      cm = (GdbModuleManager)_debugSession.getModuleManager();
      vmm = (GdbVariableMonitorManager)_debugSession.getVariableMonitorManager();

      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(2,"GdbLocalVariablesMonitorManager.updateLocalMonitors is updating local variables...");
      // For each thread in our list
      try
      {
         for (i=0;i<_threads.size();i++)
         {
            stackInfo    = (ThreadStackInfo) _threads.elementAt(i);
            tc           = (GdbThreadComponent)tm.getThreadComponent(stackInfo.getDU());
            GdbThread t = ((GdbThreadComponent)tc).getGdbThread();

            if (tc == null)
            {
               // This thread has died, remove it from our list and continue
               // We don't report change packets since the thread is dead
               removeLocalVariablesMonitor(stackInfo.getDU(),stackInfo.getStackEntryNum(), false);     //HC
               continue;
            }
            stack        = ((GdbThreadComponent)tc).getCallStack();  //HC

            // special case where stackentry number == 0.  This means get the
            // current execution entry in the stack
            // when stackentry >0 then get that specific entry.

            int stackEntry = stackInfo.getStackEntryNum();
            stackEntry = stackEntry > 0 ? stackEntry-- : 0;
            Gdb.debugOutput("Requesting locals for stackentry :" + stackEntry);

            if (stack != null && stack.length > 0 && stackEntry < stack.length)
            {
               stackFrame   = stack[stackEntry];
            }

            GdbThread gt = ((GdbThreadComponent)tc).getGdbThread();
            int gdbTID = gt.getIntThreadID();
            if (Gdb.traceLogger.DBG) 
                Gdb.traceLogger.dbg(2,"GdbLocalVariablesMonitorManager.updateLocalMonitor #### GETTING LOCAL_VARIABLES for gdbTID="+gdbTID );
            String[] locals = getCurrentLocals( gdbTID );
            numLocals = locals.length;
/*
            // Determine the number of local variables that are in scope.
            numLocals = 0;
System.out.println("GdbLocalVariablesMonitorManager.updateLocalMonitor #### SHOULD **SCOPE** LOCAL_VARIABLES HERE #### " );
            for (j=0; j< remoteVariables.length; j++)
            {
               if (remoteVariables[j].inScope())
                  numLocals++;
            }
*/
            // We have to be a little intelligent about when we add/remove
            // local variables.  We do a refresh if we are in a different
            // class or  method or the number of local variables has changed
            // (variables come in and out of scope as we step)

            int moduleID = tc.moduleID(0);
            short currentPartID  = (short) cm.getPartID(moduleID,tc.fileName(0));
//System.out.println("\nGdbLocalVariablesMonitorManager.updateLocalMonitor moduleID="+moduleID+" tc.fileName(0)="+tc.fileName(0)+" currentPartID="+currentPartID  );

Module m = cm.getModule(moduleID);
if(m==null)
{
   if (Gdb.traceLogger.ERR) 
       Gdb.traceLogger.err(2,"######## GdbLocalVariablesMonitorManager.updateLocalMonitor moduleID="+moduleID+" module==NULL,  BYPASSING" );
   return;
}
String mName = m.getModuleName();
String pName = tc.fileName(0);
// HACK ##############################################################################
if(mName.equals("<Default>") && pName.equals("TestJava.java"))
{
   if (Gdb.traceLogger.ERR) 
       Gdb.traceLogger.err(2,"\n\n######## HACK: GdbLocalVariablesMonitorManager.updateLocalMonitor changing TestJava.java into TestC.C" );
   moduleID=0;
   currentPartID=0;
}
// HACK ##############################################################################


            int   currentEntryID = cm.getEntryID(currentPartID,tc.lineNumber(0));

//            if (currentPartID  != stackInfo.getpartID() ||
//                currentEntryID != stackInfo.getentryID() ||
//                numLocals      != stackInfo.getnumLocals())
            {

               // !!! Check this EStdView construction
               EStdView context = new EStdView(currentPartID,(short)1,1,tc.lineNumber(0));
               if (Gdb.traceLogger.DBG) 
                   Gdb.traceLogger.dbg(1,"local variables have changed...");

               Integer exprID;

               // If we've stepped into a different method and class, wipe
               // our list clean.
//               if (currentPartID != stackInfo.getpartID() ||
//                   currentEntryID != stackInfo.getentryID())					//HC
               {
                  // Remove all variables being monitored for this thread
                  Enumeration elements = stackInfo.getexprIDs().elements();
                  while (elements.hasMoreElements())
                  {
                     exprID = (Integer) elements.nextElement();
                     if (Gdb.traceLogger.DBG) 
                         Gdb.traceLogger.dbg(1,"Removing local variable "+exprID);
                     vmm.deleteMonitor(exprID.intValue(),true);
                  }
                  stackInfo.getexprIDs().clear();
               }
/*
               // We only have to remove the variables that are no longer
               // in scope.
               else
               {
                  Enumeration keys = stackInfo.getexprIDs().keys();
                  while (keys.hasMoreElements())
                  {
                     String varName = (String) keys.nextElement();
System.out.println("GdbLocalVariablesMonitorManager.updateLocalMonitor #### SHOULD REMOVE LOCAL_VARIABLES HERE #### " );
                     RemoteStackVariable remoteVar = null;
                     for (j=0;j<remoteVariables.length;j++)
                     {
                        String getName = new String(remoteVariables[j].getName().getBytes("8859_1"),"UTF8");
                        if (getName.equals(varName))
                        {
                           remoteVar = remoteVariables[j];
                           break;
                        }
                     }
                     if (remoteVar != null && !remoteVar.inScope())
                     {
                        exprID = (Integer) stackInfo.getexprIDs().get(varName);
                        Gdb.debugOutput("Removing local variable "+exprID);
                        vmm.deleteMonitor(exprID.intValue(),true);
                        stackInfo.getexprIDs().remove(varName);
                     }
                  }
               }
*/

               // Remember where we are now.
               stackInfo.setpartID(currentPartID);     //HC
               stackInfo.setentryID(currentEntryID);   //HC
               stackInfo.setnumLocals(numLocals);	   //HC

               // Add them as Local monitored variables
               for (j=0;j<locals.length;j++)
               {
                  String varValue = "??unknownValue??";
                  String varName = locals[j];
                  if(varName==null)
                     continue; 

                  int x = varName.indexOf(" = ");
                  if(x>0)
                  {    
                       varValue = varName.substring(x+3);
                       varName = varName.substring(0,x);
                       if (Gdb.traceLogger.DBG) 
                           Gdb.traceLogger.dbg(2,"GdbLocalVariablesMonitorManager.updateLocalMonitor varName="+varName+" varValue="+varValue );
                  }
                  if (!stackInfo.getexprIDs().containsKey(varName))
                  {
                     int nodeID = 1;
                     String varType = GdbVariableMonitor.getExpressionType((GdbDebugSession)_debugSession, varName);                     
                     GdbVariable monVar = GdbVariable.createVariable(_debugSession,varName, varType,varValue, nodeID);
                     exprID = new Integer(vmm.addVariableMonitor((short)EPDC.MonTypeLocal, monVar, context, stackInfo.getDU()));
                     stackInfo.getexprIDs().put(varName, exprID);
                     if (Gdb.traceLogger.DBG) 
                         Gdb.traceLogger.dbg(1,"Adding local variable "+varName+" exprID="+exprID );
                  }
               }
 
            }
         }
      }
      catch (Exception e)
      {
         Gdb.handleException(e);
      }
   }

 
   /**
    * Starts monitoring the local variables for the specified thread DU and stack entry
    */
   public void addLocalVariablesMonitor(int DU, int stackEntryNum)
   {
      int i;
      ThreadStackInfo stackInfo;
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(2,"GdbLocalVariablesMonitorManager.ADD_LocalVariablesMonitor DU="+DU +" stackEntryNum="+stackEntryNum  );

      // If this thread is not in our list then add it
      for (i=0;i<_threads.size();i++)
      {
         stackInfo = (ThreadStackInfo) _threads.elementAt(i);

         // AB: check to see if this thread already has a locals monitor.
         // NOTE: current limitation of only one local's monitor per thread
         //       To allow multiple monitors for each stack entry will require one of the
         //       following:
         //       1) add to the locals reply the thread and stack number so that the model
         //          can match up the request with the variables in the reply
         //       2) have the model search each of the monitored variable entries to determine
         //          the thread DU and the stack number.  This can then be used to match
         //          the variable with the original locals monitor request.

         if (stackInfo.getDU() == DU)
         {
             return;
         }
      }
      _threads.addElement(new ThreadStackInfo(DU,(short)-1,-1,0,stackEntryNum));
   }

   /**
    * Stops monitoring the local variables for the specified thread DU.  If
    * report is true, send change packets for all monitored variables to
    * the front end.  Otherwise, do not send change packets.
    * AB: for now the stack entry number is ignored,  it is passed in for symmetry
    *     with the addLocalsVariableMonitor method and in the event that it gets
    *     used to allow > 1 locals monitor per thread
    */
   public void removeLocalVariablesMonitor(int DU, int stackEntryNum, boolean report)
   {
      int i,j;
      ThreadStackInfo stackInfo;
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(2,"GdbLocalVariablesMonitorManager.REMOVE_LocalVariablesMonitor DU="+DU +" stackEntryNum="+stackEntryNum+" report="+report  );

      // If this thread is in our list then remove it
      for (i=0;i<_threads.size();i++)
      {
         stackInfo = (ThreadStackInfo) _threads.elementAt(i);
         if (stackInfo.getDU() == DU)
         {
             // Remove all variables being monitored for this thread
             Enumeration elements = stackInfo.getexprIDs().elements();
             while (elements.hasMoreElements())
             {
                Integer exprID = (Integer) elements.nextElement();
                vmm.deleteMonitor(exprID.intValue(),report);
             }
             _threads.removeElement(stackInfo);
             return;
         }
      }
   }

}
