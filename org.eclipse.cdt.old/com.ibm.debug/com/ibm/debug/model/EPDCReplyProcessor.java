package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/EPDCReplyProcessor.java, java-model, eclipse-dev, 20011128
// Version 1.104.1.2 (last modified 11/28/01 16:11:01)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;
import java.lang.Thread;
import java.util.Vector;
import com.ibm.debug.epdc.*;
import com.ibm.debug.connection.*;
import com.ibm.debug.util.*;

class EPDCReplyProcessor implements Runnable
{
  EPDCReplyProcessor(DebugEngine debugEngine,
                     Semaphore semaphore,
                     EPDC_EngineSession engineSession)
  {
    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(1, "Creating EPDCReplyProcessor");

    _debugEngine = debugEngine;
    _connection = debugEngine.connection();
    _semaphore = semaphore;
    _engineSession = engineSession;
    _stop = false;
  }

  public void stop()
  {
    _stop = true;
  }

  public void run()
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(1, "In EPDCReplyProcessor.run()");

    while (!_stop)
    {
      _semaphore.countedWait(); // Don't process a reply until we're told by another thread that
                // it's okay to do so. The 'countedWait' method will take into
                // account any notifies that might have been done before we
                // got here i.e. it will not wait if a previous notify has been
                // done. This eliminates race conditions between the threads.
      DebuggeeProcess process = _debugEngine.process();
      try
      {
        EPDC_Reply reply = null;
        if (!_stop)
        {
           reply = getReply();
          _debugEngine.setModelIsBeingUpdated(true);
        }

        // if (reply != null && Model.dumpInfo)
        //  reply.printEPDC(System.out);

        if (!_stop && reply != null)
          processReply(reply);

        _debugEngine.setModelIsBeingUpdated(false);
      }
      catch(IOException excp)
      {
        break;
      }

      if (_debugEngine.hasBeenDeleted())
      {
         // Remove references after when are completely finished.
         if (process != null)
            process.cleanup();
         _debugEngine.cleanup();
         break;
      }
    }
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(1, "Leaving EPDCReplyProcessor.run()");
  }

  byte status()
  {
    return _status;
  }

  void prepareToReceiveReply()
  {
    _status = waitingToReceiveReply;
  }

  EPDC_Reply getReply()
  throws IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(1, "In EPDCReplyProcessor.getReply()");

    // Construct an EPDC reply from the input stream:

    _status = receivingReply;

    EPDC_Reply reply = null;

    try
    {
      reply = EPDC_Reply.decodeReplyStream(_connection, _engineSession);
    }
    catch (java.io.IOException excp)
    {
      _debugEngine.handleLostConnection(excp);
    }

    _debugEngine.setMostRecentReply(reply);

    _status = idle;

    return reply;
  }

  void processReply(EPDC_Reply reply)
       throws IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(1, "In EPDCReplyProcessor.processReply(" + reply + ")");

    int returnCode;

    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(1, "In processing Reply Packet");

    _status = processingReply;

    DebuggeeProcess process = _debugEngine.process();
    String replyMessageText = reply.messageText();

    _debugEngine.clearAllChangeFlags();

    if (process != null)
       process.clearAllChangeFlags();

    if ( ((returnCode = reply.getReturnCode()) != EPDC.ExecRc_OK) )
       _debugEngine.handleError(returnCode, replyMessageText);
    else
    switch (reply.getReplyCode())
    {
      case EPDC.Remote_RepForTypeSet:

           _debugEngine.setDefaultDataRepresentationsHaveChanged(true);

           break;

      case EPDC.Remote_TerminatePgm:
      case EPDC.Remote_ProcessDetach:
           if (Model.TRACE.EVT && Model.traceInfo())
             Model.TRACE.evt(1, "Terminating DebuggeeProcess: " + process);

           _debugEngine.remove(process);

           break;

      case EPDC.Remote_Terminate_Debug_Engine:
           if (Model.TRACE.EVT && Model.traceInfo())
             Model.TRACE.evt(1, "Terminating DebugEngine: " + _debugEngine);

           _debugEngine.host().remove(_debugEngine);

           _status = idle;
           return;

      case EPDC.Remote_Initialize_Debug_Engine:
           if (Model.TRACE.EVT && Model.traceInfo())
             Model.TRACE.evt(1, "Initializing DebugEngine");

           ERepInitializeDE initializeReply = (ERepInitializeDE)reply;

           _engineSession._viewInfo = initializeReply.viewInformation();

           _debugEngine.setHasBeenInitialized(initializeReply);

           break;

      case EPDC.Remote_PreparePgm:
           if (Model.TRACE.EVT && Model.traceInfo())
             Model.TRACE.evt(1, "Preparing a new DebuggeeProcess");

           ERepPreparePgm prepareReply = (ERepPreparePgm)reply;


           _debugEngine.add(process = new DebuggeeProcess(_debugEngine,
                                                prepareReply.ProcessId(),
                                                prepareReply.QualifiedName(),
                                                prepareReply.getProfileName()));

           break;

      case EPDC.Remote_ProcessAttach:
      case EPDC.Remote_ProcessAttach2:
           if (Model.TRACE.EVT && Model.traceInfo())
             Model.TRACE.evt(1, "Creating a new DebuggeeProcess after attach");

           ERepProcessAttach attachReply = (ERepProcessAttach)reply;


           _debugEngine.add(process = new DebuggeeProcess(_debugEngine,
                                                attachReply.ProcessId(),
                                                attachReply.QualifiedName(),
                                                attachReply.getProfileName()));

           break;

      case EPDC.Remote_LocalVariable:
	   DebuggeeThread localMonitorThread = process.getThread( ((EReqLocalVariable)(_debugEngine.getMostRecentRequest())).getDU());
	   localMonitorThread.addLocalExpressionsMonitor();
           break;

      case EPDC.Remote_LocalVariableFree:
	   localMonitorThread = process.getThread( ((EReqLocalVariableFree)(_debugEngine.getMostRecentRequest())).getDU());
	   localMonitorThread.removeLocalExpressionsMonitor();
           break;
    }

    // CHANGE PACKETS:
    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(1, "In processing Change Packets");

    Vector changePackets, changedItems;
    int i, j;

    // FCT bits

    if (reply.isFCTChgd())
    {
       if (Model.TRACE.EVT && Model.traceInfo())
         Model.TRACE.evt(1, "Processing FCT changes");

       changePackets = reply.FCTChangeInfo();

       // It's not likely that we'll see more than one FCT change packet at
       // a time nor more than one change item within that packet, but we'll
       // loop just in case and for consistency with the handling of other
       // change packets:

       for (i = 0; i < changePackets.size(); i++)
       {
           FCTChangeInfo fctChangeInfo = (FCTChangeInfo) changePackets.elementAt(i);
           changedItems = fctChangeInfo.changedItems();

           for (j = 0; j < changedItems.size(); j++)
           {
             EFunctCustTable FCTChangeItem =
                 (EFunctCustTable) ((ERepGetFCT)changedItems.elementAt(j)).getFunctionCustomizationTable();

             _debugEngine.setCapabilities(new EngineCapabilities(_debugEngine,
                                                                 FCTChangeItem));

             if (process != null && FCTChangeItem.postMortemDebugMode())
                process.setIsPostMortem();
           }
       }
    }

    // MODULES

    if (reply.isModuleEntryChgd())
    {
       if (Model.TRACE.EVT && Model.traceInfo())
         Model.TRACE.evt(1, "Processing Module changes");

       changePackets = reply.moduleChangeInfo();

       for (i = 0; i < changePackets.size(); i++)
       {
           ModuleChangeInfo moduleChangeInfo = (ModuleChangeInfo) changePackets.elementAt(i);
           changedItems = moduleChangeInfo.changedItems();

           for (j = 0; j < changedItems.size(); j++)
           {
             ERepNextModuleEntry moduleChangeItem =
                 (ERepNextModuleEntry) changedItems.elementAt(j);

             if (moduleChangeItem.isNewModule())
                process.add(new Module
                                (process,
                                 moduleChangeItem.moduleID(),
                                 moduleChangeItem.moduleName(),
                                 moduleChangeItem.fullPathModuleName(),
                                 moduleChangeItem.hasDebugData()
                                )
                           );
             else
             if (moduleChangeItem.hasBeenDeleted())
                process.removeModule(moduleChangeItem.moduleID());
           }
       }
    }

    // PARTS

    if (reply.isPartChgd())
    {
       if (Model.TRACE.EVT && Model.traceInfo())
         Model.TRACE.evt(1, "Processing Part changes");

       changePackets = reply.partChangeInfo();

       for (i = 0; i < changePackets.size(); i++)
       {
           PartChangeInfo partChangeInfo = (PartChangeInfo) changePackets.elementAt(i);
           changedItems = partChangeInfo.changedItems();

           for (j = 0; j < changedItems.size(); j++)
           {
             ERepNextPart partChangeItem =
                 (ERepNextPart) changedItems.elementAt(j);

             Module owningModule = process.getModule(partChangeItem.owningModuleID());

             if (partChangeItem.isNewPart())
                owningModule.add(new Part
                                     (owningModule,
                                      partChangeItem
                                     )
                                );
             else
             {
                Part part = process.getPart(partChangeItem.id());
                part.change(partChangeItem);
             }
           }
       }
    }

    // THREADS

    if (reply.isPgmStateChgd())
    {
       if (Model.TRACE.EVT && Model.traceInfo())
         Model.TRACE.evt(1, "Processing DebuggeeThread changes");

       changePackets = reply.threadChangeInfo();

       for (i = 0; i < changePackets.size(); i++)
       {
           ThreadChangeInfo threadChangeInfo = (ThreadChangeInfo) changePackets.elementAt(i);
           changedItems = threadChangeInfo.changedItems();

           for (j = 0; j < changedItems.size(); j++)
           {
             ERepGetNextThread threadChangeItem =
                 (ERepGetNextThread) changedItems.elementAt(j);

             int debugEngineAssignedID = threadChangeItem.debugEngineAssignedID();

             // See if thread already exists in model:

             DebuggeeThread thread = process.getThread(debugEngineAssignedID);

             if (thread == null)
             {
                if (!threadChangeItem.hasTerminated())
                   process.add(new DebuggeeThread(process, threadChangeItem));
             }
             else
             if (threadChangeItem.hasTerminated())
                process.remove(thread);
             else
                thread.change(threadChangeItem, false);
           }
       }
    }

    // MONITORED EXPRESSIONS

    if (reply.isMonVariableChgd())
    {
       if (Model.TRACE.EVT && Model.traceInfo())
         Model.TRACE.evt(1, "Processing MonitoredExpression changes");

/*
        if (_debugEngine.getMostRecentRequest().requestCode() ==
                                                EPDC.Remote_LocalVariableFree)
        {
            // thread that local monitor is attached to
            DebuggeeThread localMonitorThread = process.getThread( ((EReqLocalVariableFree)(_debugEngine.getMostRecentRequest())).getDU() );
            LocalMonitoredExpressions localMonitor = localMonitorThread.localExpressionsMonitor();
            localMonitor.prepareToDie();
            localMonitor.setHasBeenDeleted();
        }
*/

        boolean changeCallDone = false;

        changePackets = reply.monitorChangeInfo();
        for (i = 0; i < changePackets.size(); i++)
        {
             MonitorChangeInfo monitorChangeInfo = (MonitorChangeInfo) changePackets.elementAt(i);
             changedItems = monitorChangeInfo.changedItems();

             for (j = 0; j < changedItems.size(); j++)
             {
                  ERepGetNextMonitorExpr monitorChangeItem =
                        (ERepGetNextMonitorExpr) changedItems.elementAt(j);

                  short id = monitorChangeItem.getEPDCAssignedID();

                  short monitorType = monitorChangeItem.type();

                  // Thread that the changed item is attached to

                  DebuggeeThread thread = process.getThread(monitorChangeItem.threadID());

                  // New program monitor expresions will be added to
                  // the process, new local expressions will be added to
                  // the thread.

                  // TF: For now, I'm using popup exprs exclusively in support
                  // of "evaluated" exprs i.e. exprs that are monitored and
                  // then immediately deleted. See DebuggeeThread.evaluateExpression

                  MonitoredExpression monitoredExpression = null;

                  if (monitorChangeItem.isNewMonitor())
                  {
                      monitoredExpression = new MonitoredExpression(process,
                                                               monitorChangeItem);
                      switch(monitorType)
                      {
                        case EPDC.MonTypeLocal:

                             thread.add(monitoredExpression);
                             break;

                        case EPDC.MonTypeProgram:

                             process.add(monitoredExpression);

                             // Tell the process that its program-level monitored exprs
                             // have changed so that they get saved:

                             if (!changeCallDone)
                             {
                                changeCallDone = true;
                                process.setMonitoredExpressionsHaveChanged(true);
                             }

                             break;

                        case EPDC.MonTypePopup:

                             process.setEvaluatedExpression(monitoredExpression);

                             break;
                      }
                  }
                  else
                  if (monitorChangeItem.isDeleted())
                  {
                      switch(monitorType)
                      {
                        case EPDC.MonTypeLocal:

                             LocalMonitoredExpressions localMonitor = thread.localExpressionsMonitor();

                             if (localMonitor != null)
                                localMonitor.removeExpression(id);

                             break;

                        case EPDC.MonTypeProgram:

                             process.removeMonitoredExpression(id);

                             // Tell the process that its program-level monitored exprs
                             // have changed so that they get saved:

                             if (!changeCallDone)
                             {
                                changeCallDone = true;
                                process.setMonitoredExpressionsHaveChanged(true);
                             }

                             break;

                        case EPDC.MonTypePopup:

                             process.removeEvaluatedExpression();

                             break;
                      }
                  }
                  else
                  {
                     switch (monitorType)
                     {
                       case EPDC.MonTypeLocal:

                            monitoredExpression = thread.
                                                  localExpressionsMonitor().
                                                  getLocalMonitoredExpression(id);

                            break;

                       case EPDC.MonTypeProgram:

                            monitoredExpression = process.
                                                  getMonitoredExpression(id);
                            break;

                       case EPDC.MonTypePopup:

                            monitoredExpression = process.
                                                  getEvaluatedExpression();

                            break;
                     }

                     monitoredExpression.change(monitorChangeItem, false);
                  }
             }
        }
    }

    // BREAKPOINTS

    if (reply.isBrkPtChgd())
    {
       if (Model.TRACE.EVT && Model.traceInfo())
         Model.TRACE.evt(1, "Processing Breakpoint changes");

        // Tell the process that its bkps have changed so that they will get
        // saved. TODO: Fine tune this so that we only save when absolutely
        // necessary.

        process.setBreakpointsHaveChanged(true);

        changePackets = reply.breakpointChangeInfo();

        for (i = 0; i < changePackets.size(); i++)
        {
             BreakpointChangeInfo breakpointChangeInfo = (BreakpointChangeInfo) changePackets.elementAt(i);
             changedItems = breakpointChangeInfo.changedItems();

             for (j = 0; j < changedItems.size(); j++)
             {
                  ERepGetNextBkp breakpointChangeItem =
                        (ERepGetNextBkp) changedItems.elementAt(j);

                  if (breakpointChangeItem.isNew())
                  {
                     Breakpoint breakpoint = null;

                     switch(breakpointChangeItem.getType())
                     {
                       case EPDC.LineBkpType:
                            breakpoint = new LineBreakpoint(process,
                                                          breakpointChangeItem);
                            break;

                       case EPDC.EntryBkpType:
                            breakpoint = new EntryBreakpoint(process,
                                                          breakpointChangeItem);
                            break;

                       case EPDC.AddressBkpType:
                            breakpoint = new AddressBreakpoint(process,
                                                         breakpointChangeItem);
                            break;

                       case EPDC.ChangeAddrBkpType:
                            breakpoint = new Watchpoint(process,
                                                        breakpointChangeItem);
                            break;

                       case EPDC.LoadBkpType:
                            breakpoint = new ModuleLoadBreakpoint(process,
                                                        breakpointChangeItem);
                            break;
                     }

                     process.add(breakpoint);
                  }
                  else
                  if (breakpointChangeItem.isDeleted())
                     process.removeBreakpoint(breakpointChangeItem.getID());
                  else
                  {
                     Breakpoint bkp = process.getBreakpoint(breakpointChangeItem.getID());
                     bkp.change(breakpointChangeItem, false);
                  }
             }
        }
    }

    // STORAGE

    if (reply.isMonStorChgd())
    {
       if (Model.TRACE.EVT && Model.traceInfo())
         Model.TRACE.evt(1, "Processing Storage changes");

       // Tell the process that its storage monitors have changed so that they
       // will get saved. TODO: Fine tune this so that we only save when absolutely
       // necessary.

       process.setStorageMonitorsHaveChanged(true);

       changePackets = reply.storageChangeInfo();

       for (i = 0; i < changePackets.size(); i++)
       {
           StorageChangeInfo storageChangeInfo = (StorageChangeInfo) changePackets.elementAt(i);
           changedItems = storageChangeInfo.changedItems();

           for (j = 0; j < changedItems.size(); j++)
           {
               ERepGetNextMonitorStorageId storageChangeItem =
               (ERepGetNextMonitorStorageId) changedItems.elementAt(j);

               if (storageChangeItem.isNew())
                  new Storage(process, storageChangeItem);
               else
               {
                  Storage storage = process.getStorage(storageChangeItem.id());

                  if (storageChangeItem.isDeleted())
                     process.remove(storage);
                  else
                     storage.change(storageChangeItem, false);
               }
           }
       }
    }
    // REGISTERS
    if (reply.isMonRegsChgd())
    {
      if (Model.TRACE.EVT && Model.traceInfo())
        Model.TRACE.evt(1, "Processing MonitoredRegister changes");

      // case when start monitoring a register group/register groups: only group added
      // event fired
      if (_debugEngine.getMostRecentRequest().requestCode() == EPDC.Remote_Registers2)
      {
        EReqRegisters2 request = (EReqRegisters2)_debugEngine.getMostRecentRequest();

        DebuggeeThread owningThread = process.getThread(request.registersDU());
        if (request.groupID() == 0)
          owningThread.addAllMonRegisterGroups();
        else
        {
          RegisterGroup owningGroup = _debugEngine.getRegisterGroup(request.groupID());
          owningThread.add(new MonitoredRegisterGroup(owningThread, owningGroup));
        }

        // now adding each monitored register into the group
        changePackets = reply.regsChangeInfo();

        for (i=0; i<changePackets.size(); i++)
        {
          RegistersChangeInfo regsChangeInfo = (RegistersChangeInfo)changePackets.elementAt(i);

          changedItems = regsChangeInfo.changedItems();
          for (j=0; j<changedItems.size(); j++)
          {
            ERepGetNextRegister chgdRegister = (ERepGetNextRegister)changedItems.elementAt(j);
            MonitoredRegisterGroup monRegGroup = owningThread.getMonRegisterGroup(chgdRegister.getGroupID());
            monRegGroup.add(new MonitoredRegister(monRegGroup,chgdRegister));
          }
        }
      }

      // case when stop monitoring a register group/register groups: only group ended
      // event fired
      else if (_debugEngine.getMostRecentRequest().requestCode() == EPDC.Remote_RegistersFree2)
      {
        EReqRegistersFree2 request = (EReqRegistersFree2)_debugEngine.getMostRecentRequest();

        // get the thread that the monitored register group(s) is/are attached to
        DebuggeeThread owningThread = process.getThread(request.registersDU());
        if (request.groupID() == 0)
          owningThread.removeAllMonRegisterGroups();
        else
          owningThread.remove(owningThread.getMonRegisterGroup(request.groupID()));
      }

      // case when there are only changes to individual registers (e.g. new register
      //added, existing register changed/deleted). For now, I don't think there will
      //be any individual register added/deleted event. But...
      else
      {
        changePackets = reply.regsChangeInfo();

        for (i=0; i<changePackets.size(); i++)
        {
          RegistersChangeInfo regsChangeInfo = (RegistersChangeInfo)changePackets.elementAt(i);

          changedItems = regsChangeInfo.changedItems();
          for (j=0; j<changedItems.size(); j++)
          {
            ERepGetNextRegister chgdRegister = (ERepGetNextRegister)changedItems.elementAt(j);
            DebuggeeThread owningThread =  process.getThread(chgdRegister.getDU());
            MonitoredRegisterGroup monRegGroup = owningThread.getMonRegisterGroup(chgdRegister.getGroupID());

            // See if this register already is monitored:
            MonitoredRegister monReg = monRegGroup.getMonRegister(chgdRegister.getRegisterID());

            if (monReg == null)
              monRegGroup.addNew(new MonitoredRegister(monRegGroup,chgdRegister));
            else
              if (chgdRegister.isDeleted())
                monRegGroup.remove(monReg);
              else
                monReg.change(chgdRegister, false);
          }
        }
      }
    }

    // STACK
    if (reply.isMonStackChgd())
    {
      if (Model.TRACE.EVT && Model.traceInfo())
        Model.TRACE.evt(1, "Processing Stack changes");

      changePackets = reply.stackChangeInfo();

      for (i=0; i<changePackets.size(); i++)
      {
        StackChangeInfo stackChangeInfo = (StackChangeInfo)changePackets.elementAt(i);

        changedItems = stackChangeInfo.changedItems();  //build changedItems of type ERepGetChangedStack
        for (j=0; j<changedItems.size(); j++)
        {
          ERepGetChangedStack stackChangeItem = (ERepGetChangedStack)changedItems.elementAt(j);

          //get owningThread of the stack
          DebuggeeThread owningThread =  process.getThread(stackChangeItem.DU());

          // There is a defect in Java PICL where it sends us a stack change
          // packet even though the thread has been deleted. We will handle
          // this by ignoring the stack change packet if we don't know about
          // the thread. See defect 12037.

          if (owningThread == null)
          {
             if (Model.TRACE.ERR && Model.traceInfo())
                Model.TRACE.err(1, "Engine sent stack change item for a thread that does not exist");

             continue;
          }

          //build a new stack or change the stack contents
          if (stackChangeItem.isNewStack())
            new Stack(owningThread, stackChangeItem);
            //owningThread.add(new Stack(owningThread, stackChangeItem));
          else if  (stackChangeItem.isStackDeleted())
            owningThread.removeStack();
          else
          {
            Stack stack = owningThread.getStack();
            stack.change(stackChangeItem);
          }
        }
      }
    }


    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(1, "In processing Reply Packet again");

    if (returnCode == EPDC.ExecRc_OK)
    {
       switch (reply.getReplyCode())
       {
         // Since the reply to Remote_StartPgm and Remote_Execute contain a
         // a thread id, we need to process these replies after the change
         // packets have been processed (or at least after the Program State
         // change packet) in case the thread was created in the change packet.

         case EPDC.Remote_StartPgm:
              if (Model.TRACE.EVT && Model.traceInfo())
                Model.TRACE.evt(1, "Starting debuggee program");

              ERepStartPgm startProgramReply = (ERepStartPgm)reply;

              process.setHasBeenRunToMainEntryPoint();
              process.hasStopped(startProgramReply.getWhyStop(),
                                 startProgramReply.getThreadID(),
                                 startProgramReply.getExceptionMsg(),
                                 null,
                                 EPDC.Remote_StartPgm);

              break;

         case EPDC.Remote_CommandLogExecute:
              if (Model.TRACE.EVT && Model.traceInfo())
                Model.TRACE.evt(1, "Executing CommandLogExecute");

              ERepCommandLogExecute commandLogExecuteReply = (ERepCommandLogExecute)reply;
              if(process==null)
              {
                 if (Model.TRACE.EVT && Model.traceInfo())
                   Model.TRACE.evt(1, "ERROR: EPDCPReplyProcessor.processReply.Remote_CommandLogExecute process==null" );
              }
              else
              {
                 if(commandLogExecuteReply==null)
                    process.hasStopped((short)1,2,"ERROR: commandLogExecReply==null ExceptionMsg", null, EPDC.Remote_CommandLogExecute);
                 else
                    process.hasStopped(commandLogExecuteReply.getWhyStop(),
                                       commandLogExecuteReply.getThreadID(),
                                       commandLogExecuteReply.getExceptionMsg(),
                                       null,
                                       EPDC.Remote_CommandLogExecute);

                  String[] responseLines = commandLogExecuteReply.getResponseLines();
                  _debugEngine.commandLogResponse( responseLines, commandLogExecuteReply.getReturnCode() );
              }
              break;

         case EPDC.Remote_Execute:
              if (Model.TRACE.EVT && Model.traceInfo())
                Model.TRACE.evt(1, "Executing debuggee program");

              ERepExecute executeReply = (ERepExecute)reply;
              process.hasStopped(executeReply.getWhyStop(),
                                 executeReply.getThreadID(),
                                 executeReply.getExceptionMsg(),
                                 executeReply.getBreakidList(),
                                 EPDC.Remote_Execute);
              break;

         case EPDC.Remote_PreparePgm:

              // Don't call hasStopped if we're going to run to main:

              if (!((DebuggeePrepareOptions)_debugEngine.getDebuggeeStartupOptions()).runToMainEntryPoint())
                 process.hasStopped(EPDC.Why_none,
                                    0,
                                    null,
                                    null,
                                    EPDC.Remote_PreparePgm);
              break;

         case EPDC.Remote_ProcessAttach:
         case EPDC.Remote_ProcessAttach2:

              // Don't call hasStopped if we're going to let the debuggee run:

              if (!((DebuggeeAttachOptions)_debugEngine.getDebuggeeStartupOptions()).executeAfterAttach())
              {
                 ERepProcessAttach attachReply = (ERepProcessAttach)reply;
                 process.hasStopped(attachReply.getWhyStop(),
                                    attachReply.getThreadID(),
                                    attachReply.getExceptionMsg(),
                                    null,
                                    EPDC.Remote_ProcessAttach);
              }

              break;

        // I've noticed that the engine might send a part change packet along
        // with the reply to a Remote_EntrySearch request. Since the entries
        // contained in the Remote_EntrySearch reply refer to parts and files,
        // it is probably wise to process the reply after processing
        // part change packets. That is why this reply is processed here,
        // after the change packets have been processed:

        case EPDC.Remote_EntrySearch:
             if (Model.TRACE.EVT && Model.traceInfo())
               Model.TRACE.evt(1, "Processing reply to Remote_EntrySearch");

             ERepEntrySearch entrySearchReply = (ERepEntrySearch)reply;
             Vector epdcEntries = entrySearchReply.entries();
             int numberOfEntries;

             if (epdcEntries != null && (numberOfEntries = epdcEntries.size()) > 0)
             {
                 for (i = 0; i < numberOfEntries; i++)
                 {
                    ERepEntryGetNext epdcEntry = (ERepEntryGetNext)epdcEntries.elementAt(i);
                    EStdView context = epdcEntry.getEStdView();  //file context

                    if (context.getViewNo() == 0 ||
                        context.getSrcFileIndex() == 0)
                    {
                       // If the entry returned from the EntrySearch has
                       // incomplete location information, we'll add it to the
                       // process as an unresolved function. The location info
                       // for these functions will be obtained by a subsequent
                       // call to DebuggeeProcess.resolveFunctions. See
                       // ViewFile.getFunctions and DebuggeeProcess.getFunctions.

                       process.addUnresolvedFunction(epdcEntry);
                    }
                    else // This call will create a Function object and add it to the
                         // appropriate ViewFile in the Model:
                       process.addFunction(epdcEntry);
                 }
             }

             break;
       }

       if (replyMessageText != null)
          _debugEngine.sendMessage(replyMessageText);
    }

    _status = idle;
  }

  /**
   * Remove references so they can be gc'ed.
   */
  void cleanup()
  {
    _connection = null;
    _debugEngine = null;
    _engineSession = null;
    _semaphore = null;
  }

  private Connection _connection;
  private DebugEngine _debugEngine;
  private EPDC_EngineSession _engineSession;
  private Semaphore _semaphore;
  private boolean _stop;

  private byte _status; // Value will be one of the following:

  static final byte idle = 0;
  static final byte waitingToReceiveReply = 1; // We've been told a reply is
                                               // coming but we haven't started
                                               // receiving it yet.

  static final byte receivingReply = 2;        // We are in the process of
                                               // receiving the reply from the
                                               // debug engine. This includes
                                               // the possibility that we might
                                               // blocked on the reading of the
                                               // input stream (because no
                                               // input is currently available
                                               // i.e. the debug engine hasn't
                                               // sent its reply yet).

  static final byte processingReply = 3;       // A reply has been received and
                                               // is currently being processed.
                                               // This usually implies that
                                               // the model is being updated
                                               // and listeners are
                                               // being notified of events.
}
