package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1995, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/DebuggeeThread.java, java-model, eclipse-dev, 20011128
// Version 1.79.1.2 (last modified 11/28/01 16:11:00)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.epdc.*;
import java.util.Vector;
import java.io.*;

/**
 *  This class represents threads within the process being debugged.
 *  DebuggeeThread objects are contained within DebuggeeProcess objects to
 *  reflect the relationship between a process and its threads.
 *  <p>Debuggeethread objects will come and go throughout a debug session as
 *  the threads they represent are started and ended.
 *  Note: This class is named DebuggeeThread instead of just Thread in order to
 *  avoid name collision with java.lang.Thread i.e. so users of this class
 *  don't have to qualify it with the package name.
 *  @see DebuggeeProcess#threads()
 */

public class DebuggeeThread extends DebugModelObject
{
  DebuggeeThread(DebuggeeProcess owningProcess, ERepGetNextThread epdcThread)
  {
    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(1, "Creating DebuggeeThread : OwningProcess=DebuggeeProcess[" + owningProcess.processID() + "] ID=" + epdcThread.debugEngineAssignedID());

    _owningProcess = owningProcess;

    change(epdcThread, true);
  }

  public void addEventListener(DebuggeeThreadEventListener eventListener)
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(2, "DebuggeeThread[" + debugEngineAssignedID() + "].addEventListener(" + eventListener + ")");

    _eventListeners.addElement(eventListener);
  }

  public void removeEventListener(DebuggeeThreadEventListener eventListener)
  {
    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(2, "DebuggeeThread[" + debugEngineAssignedID() + "].removeEventListener(" + eventListener + ")");

    int index = _eventListeners.indexOf(eventListener);

    if (index != -1)
    {
        try
        {
          _eventListeners.setElementAt(null, index);
        }
        catch(ArrayIndexOutOfBoundsException excp)
        {
        }
    }
  }

  /** Notify listeners that thread has ended.
   */

  void prepareToDie()
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "DebuggeeThread[" + debugEngineAssignedID() + "].prepareToDie()");

    DebugEngine debugEngine = _owningProcess.debugEngine();

    int requestCode = debugEngine.getMostRecentReply().getReplyCode();

    debugEngine.getEventManager().addEvent(new ThreadEndedEvent(this,
                                                                this,
                                                                requestCode
                                                               ),
                                           _eventListeners
                                          );
  }

   /**
    * Call this method to freeze the current thread.
    * @see DebuggeeThread#thaw
    */

   public boolean freeze(int sendReceiveControlFlags)
      throws java.io.IOException
   {
      if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(2, "DebuggeeThread[" + debugEngineAssignedID() + "].freeze(" + sendReceiveControlFlags + ")");

      DebugEngine debugEngine = _owningProcess.debugEngine();

      if (debugState() != EPDC.StdThdFrozen)
      {
         if (!debugEngine.prepareForEPDCRequest(EPDC.Remote_ThreadFreeze,
                                                sendReceiveControlFlags))
            return false;

         if (Model.TRACE.EVT && Model.traceInfo())
           Model.TRACE.evt(2, "Sending EPDC request: Remote_ThreadFreeze");

         if (!debugEngine.processEPDCRequest(new EReqThreadFreeze(debugEngineAssignedID()),
                                             sendReceiveControlFlags))
            return false;
         else
            return true;
      }
      else
         return true;
   }

   /**
    * Call this method to thaw the current thread.
    * @see DebuggeeThread#freeze
    */

   public boolean thaw(int sendReceiveControlFlags)
      throws java.io.IOException
   {
      if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(2, "DebuggeeThread[" + debugEngineAssignedID() + "].thaw(" + sendReceiveControlFlags + ")");

      DebugEngine debugEngine = _owningProcess.debugEngine();

      if (debugState() != EPDC.StdThdThawed)
      {
         if (!debugEngine.prepareForEPDCRequest(EPDC.Remote_ThreadThaw,
                                                sendReceiveControlFlags))
            return false;

         if (Model.TRACE.EVT && Model.traceInfo())
           Model.TRACE.evt(2, "Sending EPDC request: Remote_ThreadThaw");

         if (!debugEngine.processEPDCRequest(new EReqThreadThaw(debugEngineAssignedID()),
                                             sendReceiveControlFlags))
            return false;
         else
            return true;
      }
      else
         return true;
   }

  void change(ERepGetNextThread epdcThread, boolean isNew)
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "DebuggeeThread[" + epdcThread.debugEngineAssignedID() + "].change(" + epdcThread + ", " + isNew + ")");

    _currentLocation = null;
    _epdcThread = epdcThread;

    // Get the part in which this thread is currently stopped:

    EStdView[] epdcLocation = epdcThread.whereStopped();

    short currentPartID;
    _currentPart = null;

    for (int i = 0; i < epdcLocation.length; i++)
        if ((currentPartID = epdcLocation[i].getPPID()) != 0)
        {
           _currentPart = _owningProcess.getPart(currentPartID);
           break;
        }

    processAttributes();

    if (isNew)
       return;

    DebugEngine debugEngine = _owningProcess.debugEngine();

    int requestCode = debugEngine.getMostRecentReply().getReplyCode();

    debugEngine.getEventManager().addEvent(new ThreadChangedEvent(this,
                                                                  this,
                                                                  requestCode
                                                                 ),
                                           _eventListeners
                                          );
  }


  private void processAttributes()
  {
    int EPDCVersion = _owningProcess.debugEngine().getEPDCVersion();

    EStdAttribute[] epdcAttributes;

    if (EPDCVersion < 307) // Create attributes from old fields
    {
       epdcAttributes = new EStdAttribute[3];

       epdcAttributes[0] = new EStdAttribute(EPDC.ThreadNameOrTID, // type
                                               null,                 // name
                                               Integer.toString(systemAssignedID()) //value
                                              );

       String threadState = Model.getResourceString("Thread.State." + state());

       epdcAttributes[1] = new EStdAttribute(EPDC.ThreadState, // type
                                               null,             // name
                                               threadState       // value
                                              );

       epdcAttributes[2] = new EStdAttribute(EPDC.ThreadPriority, // type
                                               null,                // name
                                               Integer.toString(priority()) //value
                                              );
    }
    else
       if ((epdcAttributes = _epdcThread.getAttributes()) == null)
          epdcAttributes = new EStdAttribute[0];

    _attributes = new ThreadAttribute[epdcAttributes.length]; // discard old ones

    _nameOrTID = _state = _priority = _group = _blockingThread = null;

    for (int i = 0; i < epdcAttributes.length; i++)
    {
        _attributes[i] = new ThreadAttribute(epdcAttributes[i]);

        switch(epdcAttributes[i].getType())
        {
          case EPDC.ThreadNameOrTID:
               _nameOrTID = _attributes[i];
               break;

          case EPDC.ThreadState:
               _state = _attributes[i];
               break;

          case EPDC.ThreadPriority:
               _priority = _attributes[i];
               break;

          case EPDC.ThreadGroup:
               _group = _attributes[i];
               break;

          case EPDC.ThreadBlockingThread:
               _blockingThread = _attributes[i];
               break;
        }
    }
  }

  /**
   * Returns an array of all attributes for this thread. This method can
   * be used no matter what level of EPDC the engine uses (307 or earlier).
   * <p>
   * Note that the returned array can be empty in which case the engine did
   * not send any attributes for the thread.
   * <p>
   * Note also that there are convenience methods for retrieving certain
   * well-known attributes such as thread name, thread state, etc. but
   * those convenience methods don't necessarily represent the entire set
   * of attributes sent by the engine. For the entire set of attributes, call
   * this method instead.
   */

  public ThreadAttribute[] getAttributes()
  {
    return (ThreadAttribute[])_attributes.clone();
  }

  /**
   * A convience method for getting the name (or TID) of this thread.
   * This method can be used no matter what level of EPDC the engine uses
   * (307 or earlier). To get all of the thread's attributes (including
   * this one) use getAttributes instead.
   *
   * @return The ThreadAttribute containing the thread's name or TID, or
   *         null if there is no such attribute.
   */

  public ThreadAttribute getNameOrTID()
  {
    return _nameOrTID;
  }

  /**
   * A convience method for getting the state of this thread.
   * This method can be used no matter what level of EPDC the engine uses
   * (307 or earlier). To get all of the thread's attributes (including
   * this one) use getAttributes instead.
   *
   * @return The ThreadAttribute containing the thread's state, or
   *         null if there is no such attribute.
   */

  public ThreadAttribute getState()
  {
    return _state;
  }

  /**
   * A convience method for getting the priority of this thread.
   * This method can be used no matter what level of EPDC the engine uses
   * (307 or earlier). To get all of the thread's attributes (including
   * this one) use getAttributes instead.
   *
   * @return The ThreadAttribute containing the thread's priority, or
   *         null if there is no such attribute.
   */

  public ThreadAttribute getPriority()
  {
    return _priority;
  }

  /**
   * A convience method for getting the thread group containing this thread.
   * This method can be used no matter what level of EPDC the engine uses
   * (307 or earlier). To get all of the thread's attributes (including
   * this one) use getAttributes instead.
   *
   * @return The ThreadAttribute containing the thread group to which this
   * thread belongs, or null if there is no such attribute.
   */

  public ThreadAttribute getGroup()
  {
    return _group;
  }

  /**
   * A convience method for getting the name of the thread that is blocking
   * the execution of this thread (because it holds a lock on an object,
   * for example).
   * This method can be used no matter what level of EPDC the engine uses
   * (307 or earlier). To get all of the thread's attributes (including
   * this one) use getAttributes instead.
   *
   * @return The ThreadAttribute containing the name of the thread blocking
   * the execution of this thread, or null if there is no such attribute.
   */

  public ThreadAttribute getBlockingThread()
  {
    return _blockingThread;
  }

  /**
   *  Get the current state of this thread (e.g. running, suspended, blocked).
   *  Values returned from this method correspond to the constants in
   *  class com.ibm.debug.epdc.EPDC for thread state.
   *
   * @see com.ibm.debug.epdc.EPDC#StdThdRunnable
   *
   * @deprecated This method is only guaranteed to work with engines at
   *             EPDC level 306 and earlier. Use getState or getAttributes
   *             instead.
   */

  public short state()
  {
    return _epdcThread.state();
  }

  /**
   *  Get this thread's current debug state (e.g. thawed, frozen).
   *  Values returned from this method correspond to the constants in
   *  class com.ibm.debug.epdc.EPDC for thread debug state.
   * @see com.ibm.debug.epdc.EPDC#StdThdThawed
   *
   */

  public short debugState()
  {
    return _epdcThread.debugState();
  }

  /**
   *  Get the current priority of this thread. The meaning of this number is
   *  operating system-dependent.
   *
   * @deprecated This method is only guaranteed to work with engines at
   *             EPDC level 306 and earlier. Use getPriority or getAttributes
   *             instead.
   */

  public int priority()
  {
    return _epdcThread.priority();
  }

  /**
   * Get the thread ID (TID) assigned to this thread by the operating system
   * on which it is running.
   *
   * @deprecated This method is only guaranteed to work with engines at
   *             EPDC level 306 and earlier. Use getNameOrTID or getAttributes
   *             instead.
   */

  public int systemAssignedID()
  {
    return _epdcThread.systemAssignedID();
  }

  /**
   * Get the debug engine-generated ID for this thread. This thread ID will be
   * unique within the process. Threads are usually numbered by the debug engine
   * starting with ID 1.
   */

  public int debugEngineAssignedID()
  {
    return _epdcThread.debugEngineAssignedID();
  }

  /**
   * Get the process which owns this thread.
   */

  public DebuggeeProcess owningProcess()
  {
    return _owningProcess;
  }

  /**
   * Returns the Part object which represents the part (compilation unit)
   * in which this thread is currently stopped.
   */

  public Part partInWhichThreadIsCurrentlyStopped()
  throws java.io.IOException
  {
    if (_currentPart == null)
    {
        DebugEngine debugEngine = _owningProcess.debugEngine();

        if (debugEngine.prepareForEPDCRequest(EPDC.Remote_ThreadInfoGet,
                                          DebugEngine.sendReceiveSynchronously))
        {
            if (Model.TRACE.EVT && Model.traceInfo())
                Model.TRACE.evt(2, "Sending EPDC request: Remote_ThreadInfoGet");

            EReqThreadInfoGet request = new EReqThreadInfoGet(debugEngineAssignedID());

            // The request should cause a thread change packet to be sent
            // by the engine and in processing that change packet the
            // _currentPart member will be updated.
            debugEngine.processEPDCRequest(request, DebugEngine.sendReceiveSynchronously);
        }
    }

    return _currentPart;
  }

  /**
   * Return the current location (file and line number) of this thread.
   * @param viewInformation The view information object for a particular kind
   * of view e.g. source, disassembly, etc. The location returned will be
   * a location within a view of this type. Will return null if the part in
   * which the thread is stopped has no view of this type.
   * @see DebugEngine#isBusy
   * @see DebugEngine#modelIsBeingUpdated
   *  @exception java.io.IOException If there is a problem communicating
   *  with the debug engine.
   */

  public Location currentLocationWithinView(ViewInformation viewInformation)
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "DebuggeeThread[" + debugEngineAssignedID() + "].currentLocationWithinView(" + viewInformation.name() + ")");

    // Make sure we know what the current part is:

    if (partInWhichThreadIsCurrentlyStopped() == null)
       return null;

    DebugEngine debugEngine = _owningProcess.debugEngine();

    EStdView epdcLocation = _epdcThread.whereStopped()[viewInformation.index()-1];
    // We know the part in which this thread is currently stopped (_currentPart)
    // so we'll ask that part for the view which corresponds to the given
    // ViewInformation object:

    View view = _currentPart.view(viewInformation);

    if (view == null)
    {
      if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(4, "No view of this type: " + viewInformation.name());

      return null;
    }

    ViewFile file = view.file(epdcLocation.getSrcFileIndex());

    int numberOfViews = _currentPart.views().length;
    int viewIndex = viewInformation.index();

    // view index starts from one and not zero
    if (_currentLocation == null)
        _currentLocation = new Location[numberOfViews+1];

    if (_currentLocation[viewIndex] == null)
        _currentLocation[viewIndex] = new Location(file, epdcLocation.getLineNum());

    return _currentLocation[viewIndex];
  }

  /**
   * Send a request to monitor an expression. The expression is represented
   * as a string. With this request expressions will be viewed in the program
   * monitor window.
   * @param location the location of the expression in the source
   * @param expression the string representing the expression to be monitored
   * @param attribute the attribute associated with the monitored expression.
   * This attribute can be for an enabled, disabled, or deferred monitored
   * expression
   * @param sendReceiveControlFlags the flag indicating the state in which the
   * request is to be sent(synchronized, asynchronized).
   * @return 'true' if the request to view monitored expressions in the program
   * monitor was sent successfully, and 'false' otherwise.
   * @exception java.io.IOException if there is a communication problem
   * with the debug engine, this exception occurs.
   */
  public boolean monitorExpression(Location location, String expression,
                                   byte attribute, int sendReceiveControlFlags)
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "DebuggeeThread[" + debugEngineAssignedID() + "].monitorExpression(" + expression + ", " + attribute + ")");

    return _owningProcess.monitorExpression(location.getEStdView(),
                                            _epdcThread.debugEngineAssignedID(),
                                            expression,
                                            attribute,
                                            EPDC.MonTypeProgram,
                                            null,
                                            null,
                                            null,
                                            sendReceiveControlFlags);
  }

  /**
   * This is the default method to request to monitor an expression with its
   * attribute set as enabled. This method will call the overriding
   * monitorExpression method with the attribute parameter set to
   * EPDC.MonEnable.
   * @param location the location of the expression in the source
   * @param expression the string representing the expression to be monitored
   * @param sendReceiveControlFlags the flag indicating the state in which the
   * request is to be sent(synchronized, asynchronized).
   * @return 'true' if the request to view monitored expressions in the program
   * monitor was sent successfully, and 'false' otherwise.
   * @exception java.io.IOException if there is a communication problem
   * with the debug engine, this exception occurs.
   */
  public boolean monitorExpression(Location location, String expression,
                                int sendReceiveControlFlags)
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "DebuggeeThread[" + debugEngineAssignedID() + "].monitorExpression(" + expression + ")");

    return monitorExpression(location, expression,
                             (byte)EPDC.MonEnable, sendReceiveControlFlags);
  }

  /**
   * Do a once-only evaluation of the given expression. This method is very
   * similar to the "monitorExpression" method - the main difference is that
   * <i>the expression does not remain monitored after it has been
   * evaluated</i>. This has several implications:
   * <ul>
   * <li>No events will ever be fired regarding this expression. In particular,
   * there will be no "added", "changed", or "deleted" events for this expr.
   * <li>No further requests can be sent to the debug engine regarding this
   * monitored expression (e.g. expand, enable/disable, etc.) - as far as the
   * Model and the engine are concerned, this monitored expression no longer
   * exists.
   * <li>Unlike truly "monitored" expressions, this expression is not saved
   * within the Model (nor by the engine) and there is therefore no method
   * that client code can call to retrieve it again later on.
   * <li>The MonitoredExpression object will be marked as "deleted".
   * </ul>
   * @param location A Location object which determines the context for the
   * evaluation of the expression. The expression evaluator needs to know the
   * context of the evaluation in order to do the proper look-up of names
   * contained within the expression.
   * @param expression The expression to be evaluated.
   * @param expansionLevel Not currently used
   * @param maximumNumberOfChildren It will have the value of zero if the
   * expression is not an aggregate. Otherwise, it will be the number of
   * children allowed by the request.
   * @return A MonitoredExpression object representing the result of
   * evaluating the expression, or null if the expression could not be
   * evaluated.
   * @exception java.io.IOException If there is a problem communicating with
   * the debug engine.
   */

  public MonitoredExpression evaluateExpression(Location location,
                                                String expression,
                                                int expansionLevel,
                                                int maximumNumberOfChildren
                                               )
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "DebuggeeThread[" + debugEngineAssignedID() + "].evaluateExpression(" + expression + ")");

    if (!_owningProcess.monitorExpression(location.getEStdView(),
                                            _epdcThread.debugEngineAssignedID(),
                                            expression,
                                            EPDC.MonEnable,
                                            EPDC.MonTypePopup,
                                            null,
                                            null,
                                            null,
                                            DebugEngine.sendReceiveSynchronously)
       )
       return null;

    DebugEngine debugEngine = _owningProcess.debugEngine();

    EPDC_Reply reply = debugEngine.getMostRecentReply();

    if (reply == null ||
        reply.getReplyCode() != EPDC.Remote_Expression ||
        reply.getReturnCode() != EPDC.ExecRc_OK)
       return null;

    MonitoredExpression evaluatedExpression = _owningProcess.getEvaluatedExpression();

    // Expand children if requested and if the expr result is an aggregate.
    // Expand will be done for one level only.

    MonitoredExpressionTreeNode rootNode = evaluatedExpression.getValue();
    if (rootNode instanceof AggregateMonitoredExpressionTreeNode)
    {
        // There has to a minimum number of one child
        int minimumNumberOfChildren = 1;

        ((AggregateMonitoredExpressionTreeNode)rootNode).expand(
                                         minimumNumberOfChildren,
                                         maximumNumberOfChildren,
                                         DebugEngine.sendReceiveSynchronously);
    }

    // We prefer to remove it asynchronously, if possible, but we may not be
    // able to if we were called during event listener callbacks - the Model
    // does not accept asynch requests during the callback phase:

/*
    if (debugEngine.isAcceptingAsynchronousRequests())
       evaluatedExpression.remove(DebugEngine.sendReceiveDefault);
    else
*/
       evaluatedExpression.remove(DebugEngine.sendReceiveSynchronously);

    return evaluatedExpression;
  }

  /**
   * CAUTION - this method was created for a special case and should
   * be used with caution.
   * This method is similar to the evaluateExpression method in that it
   * it is a once-only modification of an expression.  As for
   * the evaluateExpression method, the expression does not remain monitored,
   * no events will ever be fired regarding this expression, no further
   * requests can be sent to the debug engine regarding this expression,
   * the model does not save the expression and thus it cannot be
   * retrieved, the expression returned is marked as deleted.
   * @param location A location object which determines the context for the
   * evaluation of the expression. The expression evaluator needs to know the
   * context of the evaluation in order to do the proper look-up of names
   * contained within the expression.
   * @param expression The expression to be evaluated.
   * @param value The new value to assign to the expression.  The value
   * will be assigned to the expression root node.
   * @return A MonitoredExpression object representing the result of
   * modifying the expression, or null if the expression could not be
   * modified.
   * @exception java.io.IOException If there is a problem communicating with
   * the debug engine.
   */
  public MonitoredExpression modifyExpression(Location location,
                                              String expression,
                                              String value
                                              )
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "DebuggeeThread[" + debugEngineAssignedID() + "].evaluateExpression(" + expression + ")");

    if (!_owningProcess.monitorExpression(location.getEStdView(),
                                            _epdcThread.debugEngineAssignedID(),
                                            expression,
                                            EPDC.MonEnable,
                                            EPDC.MonTypePopup,
                                            null,
                                            null,
                                            null,
                                            DebugEngine.sendReceiveSynchronously)
       )
       return null;

    DebugEngine debugEngine = _owningProcess.debugEngine();

    EPDC_Reply reply = debugEngine.getMostRecentReply();

    if (reply == null ||
        reply.getReplyCode() != EPDC.Remote_Expression ||
        reply.getReturnCode() != EPDC.ExecRc_OK)
       return null;

    MonitoredExpression evaluatedExpression = _owningProcess.getEvaluatedExpression();

    MonitoredExpressionTreeNode rootNode = evaluatedExpression.getValue();
    if (!rootNode.modifyValue(value, DebugEngine.sendReceiveSynchronously))
       return null;

    // We prefer to remove it asynchronously, if possible, but we may not be
    // able to if we were called during event listener callbacks - the Model
    // does not accept asynch requests during the callback phase:

/*
    if (debugEngine.isAcceptingAsynchronousRequests())
       evaluatedExpression.remove(DebugEngine.sendReceiveDefault);
    else
*/
       evaluatedExpression.remove(DebugEngine.sendReceiveSynchronously);

    return evaluatedExpression;
  }


  /**
   * This is the method to request to monitor local expressions. Upon success
   * of this request a local monitor view with the local expressions can
   * be displayed.  Locals will be monitored at the current execution point in the thread's
   * stack.
   * @param sendReceiveControlFlags the flag indicating the state in which the
   * request is to be sent(synchronized, asynchronized).
   * @return 'true' if the request to monitor local expressions
   * was sent successfully, and 'false' otherwise.
   * @exception java.io.IOException if there is a communication problem
   * with the debug engine, this exception occurs.
   */
   public boolean monitorLocalVariables(int sendReceiveControlFlags)
   throws java.io.IOException
   {
      return monitorLocalVariables(sendReceiveControlFlags,0);
   }

  /**
   * Request to monitor local variables at a stack entry number.
   * @param stackEntryNum specifies to monitor locals at this stack entry
   * @param sendReceiveControlFlags indicates state in which request is to be
   * sent (synchronized, asynchronized)
   * @return boolean true if successful
   * @exception java.io.IOException if a communication problem.
   */

   public boolean monitorLocalVariables(int sendReceiveControlFlags, int stackEntryNum)
   throws java.io.IOException
   {
      if (Model.TRACE.DBG && Model.traceInfo())
         Model.TRACE.dbg(3, "DebuggeeThread[" + debugEngineAssignedID() + "].monitorLocalVariables(" + sendReceiveControlFlags + ")");

      int threadID = _epdcThread.debugEngineAssignedID();

      // return the status of the request
      DebugEngine debugEngine = _owningProcess.debugEngine();

      if ( !debugEngine.prepareForEPDCRequest(EPDC.Remote_LocalVariable,
                                            sendReceiveControlFlags) )
         return false;

    // Check to see if the current engine supports local variable monitors.
    // If not the request will be canceled.
    // Cancel the request if we already have a local monitor

      if (Model.checkFCTBit)
      {
         if (!debugEngine.getCapabilities().getWindowCapabilities().monitorLocalVariablesSupported())
         {
            debugEngine.cancelEPDCRequest(EPDC.Remote_LocalVariable);
            return false;
         }
      }

      if (_localExpressionsMonitor != null)
      {
         debugEngine.cancelEPDCRequest(EPDC.Remote_LocalVariable);
         return false;
      }

    // The second argument, stack entry number is passed to the engine to indicate
    // at what point in the stack the locals should be monitored.
      EReqLocalVariable request = new EReqLocalVariable(threadID, stackEntryNum);

      if (Model.TRACE.EVT && Model.traceInfo())
         Model.TRACE.evt(2, "Sending EPDC request: Remote_LocalVariable for stack entry :" + stackEntryNum);

      if (!debugEngine.processEPDCRequest(request, sendReceiveControlFlags))
         return false;
      else
         return true;
   }

  /**
   * Add a new expression to the list of monitored expressions in the
   * local monitor.
   */
  synchronized void add(MonitoredExpression expr)
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(4, "DebuggeeThread[" + debugEngineAssignedID() + "].add(MonitoredExpression<" + expr.getMonitoredExpressionAssignedID() + ">)");

    _localExpressionsMonitor.addLocalMonitoredExpression(expr);
  }

  void removeLocalExpressionsMonitor()
  {
    _localExpressionsMonitor.prepareToDie();
    _localExpressionsMonitor.setHasBeenDeleted();

    _localExpressionsMonitor = null;
  }

  void addLocalExpressionsMonitor()
  {
    _localExpressionsMonitor = new LocalMonitoredExpressions(this);

    DebugEngine debugEngine = _owningProcess.debugEngine();

    int requestCode = debugEngine.getMostRecentReply().getReplyCode();

    debugEngine.getEventManager().addEvent(new
				           LocalMonitoredExpressionsAddedEvent(
						   this,
						   _localExpressionsMonitor,
						   requestCode),
					   _eventListeners);
  }

  /**
   * Return the local monitor attached to the thread
   */
  public LocalMonitoredExpressions localExpressionsMonitor()
  {
    return _localExpressionsMonitor;
  }


  /**
   * Run to the given location. This is equivalent to setting a breakpoint at
   * that location, letting the debuggee run, and then removing the breakpoint.
   * @param location A Location object which identifies the target location to
   * run to.
    *  @param sendReceiveControlFlags A set of flags which specify the mode
    *  in which this request is to be performed. There is a set of constants
    *  in DebugEngine which define the possible values for this argument.
    *  For example, a value of DebugEngine.sendReceiveDefault means that the
    *  request is to be done asynchronously, while a value of DebugEngine.sendReceiveSynchronously
    *  means that the request is to be performed synchronously.
    *  <p>When done asynchronously,
    *  this method will return immediately after sending the request to the
    *  debug engine without waiting for a response from
    *  the debug engine. The response to the request will be
    *  received on a separate thread and client code will be notified of the
    *  the response via the event listener mechanism.
   *
   *  @exception java.io.IOException If there is a problem communicating
   *  with the debug engine.
   */

  public boolean runToLocation(Location location, int sendReceiveControlFlags)
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(2, "DebuggeeThread[" + debugEngineAssignedID() + "].runToLocation(Location<" + location.file().baseFileName() + ", " + location.lineNumber() + ">)");

    DebugEngine debugEngine = _owningProcess.debugEngine();

    if (!debugEngine.prepareForEPDCRequest(EPDC.Remote_Execute, sendReceiveControlFlags))
       return false;

    if (Model.checkFCTBit)
    {
        if (!debugEngine.getCapabilities().getRunCapabilities().runToLocationSupported())
        {
            debugEngine.cancelEPDCRequest(EPDC.Remote_Execute);
            return false;
        }
    }

    // TODO: Should we call currentLocationWithinView and check for null to
    // make sure that this is a valid view for the part in which the thread is
    // currently stopped before making the request?

    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(2, "Sending EPDC request: Remote_Execute");

    return debugEngine.processEPDCRequest(new EReqExecuteRunToLocation(debugEngineAssignedID(),
                                                                       location.getEStdView()
                                                                      ),
                                           sendReceiveControlFlags
                                          );
  }

  /**
   * Jump to the given location.
   * @param location A Location object which identifies the target location to
   * jump to.
    *  @param sendReceiveControlFlags A set of flags which specify the mode
    *  in which this request is to be performed. There is a set of constants
    *  in DebugEngine which define the possible values for this argument.
    *  For example, a value of DebugEngine.sendReceiveDefault means that the
    *  request is to be done asynchronously, while a value of DebugEngine.sendReceiveSynchronously
    *  means that the request is to be performed synchronously.
    *  <p>When done asynchronously,
    *  this method will return immediately after sending the request to the
    *  debug engine without waiting for a response from
    *  the debug engine. The response to the request will be
    *  received on a separate thread and client code will be notified of the
    *  the response via the event listener mechanism.
   *
   *  @exception java.io.IOException If there is a problem communicating
   *  with the debug engine.
   */

  public boolean jumpToLocation(Location location, int sendReceiveControlFlags)
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(2, "DebuggeeThread[" + debugEngineAssignedID() + "].jumpToLocation(Location<" + location.file().baseFileName() + ", " + location.lineNumber() + ">)");

    DebugEngine debugEngine = _owningProcess.debugEngine();

    if (!debugEngine.prepareForEPDCRequest(EPDC.Remote_Execute, sendReceiveControlFlags))
       return false;

    if (Model.checkFCTBit)
    {
        if (!debugEngine.getCapabilities().getRunCapabilities().jumpToLocationSupported())
        {
            debugEngine.cancelEPDCRequest(EPDC.Remote_Execute);
            return false;
        }
    }

    // TODO: Should we call currentLocationWithinView and check for null to
    // make sure that this is a valid view for the part in which the thread is
    // currently stopped before making the request?

    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(2, "Sending EPDC request: Remote_Execute");

    return debugEngine.processEPDCRequest(new EReqExecuteJumpToLocation(debugEngineAssignedID(),
                                                                        location.getEStdView()
                                                                       ),
                                           sendReceiveControlFlags
                                          );
  }

  /**
   * Do a step debug.
   * @param viewInformation EPDC dictates that a step must be performed w.r.t.
   * a particular kind of view. The debug engine uses this info to determine
   * the granularity of the step
   * (e.g. step 1 statement vs. step 1 machine instruction), among other things.
    *  @param sendReceiveControlFlags A set of flags which specify the mode
    *  in which this request is to be performed. There is a set of constants
    *  in DebugEngine which define the possible values for this argument.
    *  For example, a value of DebugEngine.sendReceiveDefault means that the
    *  request is to be done asynchronously, while a value of DebugEngine.sendReceiveSynchronously
    *  means that the request is to be performed synchronously.
    *  <p>When done asynchronously,
    *  this method will return immediately after sending the request to the
    *  debug engine without waiting for a response from
    *  the debug engine. The response to the request will be
    *  received on a separate thread and client code will be notified of the
    *  the response via the event listener mechanism.
   *
   *  @exception java.io.IOException If there is a problem communicating
   *  with the debug engine.
   */

  public boolean stepDebug(ViewInformation viewInformation, int sendReceiveControlFlags)
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(2, "DebuggeeThread[" + debugEngineAssignedID() + "].stepDebug(" + viewInformation.name() + ")");

    DebugEngine debugEngine = _owningProcess.debugEngine();

    if (!debugEngine.prepareForEPDCRequest(EPDC.Remote_Execute, sendReceiveControlFlags))
       return false;

    if (Model.checkFCTBit)
    {
        if (!debugEngine.getCapabilities().getRunCapabilities().stepDebugSupported())
        {
            debugEngine.cancelEPDCRequest(EPDC.Remote_Execute);
            return false;
        }
    }

    // TODO: Should we call currentLocationWithinView and check for null to
    // make sure that this is a valid view for the part in which the thread is
    // currently stopped before making the request?

    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(2, "Sending EPDC request: Remote_Execute");

    return debugEngine.processEPDCRequest(new EReqExecuteStepDebug(debugEngineAssignedID(), viewInformation.index()),
                                           sendReceiveControlFlags
                                          );
  }

  /**
   * Do a step over.
   * @param viewInformation EPDC dictates that a step must be performed w.r.t.
   * a particular kind of view. The debug engine uses this info to determine
   * the granularity of the step
   * (e.g. step 1 statement vs. step 1 machine instruction), among other things.
    *  @param sendReceiveControlFlags A set of flags which specify the mode
    *  in which this request is to be performed. There is a set of constants
    *  in DebugEngine which define the possible values for this argument.
    *  For example, a value of DebugEngine.sendReceiveDefault means that the
    *  request is to be done asynchronously, while a value of DebugEngine.sendReceiveSynchronously
    *  means that the request is to be performed synchronously.
    *  <p>When done asynchronously,
    *  this method will return immediately after sending the request to the
    *  debug engine without waiting for a response from
    *  the debug engine. The response to the request will be
    *  received on a separate thread and client code will be notified of the
    *  the response via the event listener mechanism.
   *
   *  @exception java.io.IOException If there is a problem communicating
   *  with the debug engine.
   */

  public boolean stepOver(ViewInformation viewInformation, int sendReceiveControlFlags)
  throws java.io.IOException
  {
    return stepOver(viewInformation, sendReceiveControlFlags, null);
  }

  public boolean stepOver(ViewInformation viewInformation,
                          int sendReceiveControlFlags,
                          Object property)
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(2, "DebuggeeThread[" + debugEngineAssignedID() + "].stepOver(" + viewInformation.name() + ")");

    DebugEngine debugEngine = _owningProcess.debugEngine();

    if (!debugEngine.prepareForEPDCRequest(EPDC.Remote_Execute, sendReceiveControlFlags))
       return false;

    if (Model.checkFCTBit)
    {
        if (!debugEngine.getCapabilities().getRunCapabilities().stepOverSupported())
        {
            debugEngine.cancelEPDCRequest(EPDC.Remote_Execute);
            return false;
        }
    }

    // TODO: Should we call currentLocationWithinView and check for null to
    // make sure that this is a valid view for the part in which the thread is
    // currently stopped before making the request?

    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(2, "Sending EPDC request: Remote_Execute");

    return debugEngine.processEPDCRequest(new EReqExecuteStepOver(debugEngineAssignedID(), viewInformation.index()),
                                          sendReceiveControlFlags,
                                          property);
  }

  /**
   * Do a step Into.
   * @param viewInformation EPDC dictates that a step must be performed w.r.t.
   * a particular kind of view. The debug engine uses this info to determine
   * the granularity of the step
   * (e.g. step 1 statement vs. step 1 machine instruction), among other things.
    *  @param sendReceiveControlFlags A set of flags which specify the mode
    *  in which this request is to be performed. There is a set of constants
    *  in DebugEngine which define the possible values for this argument.
    *  For example, a value of DebugEngine.sendReceiveDefault means that the
    *  request is to be done asynchronously, while a value of DebugEngine.sendReceiveSynchronously
    *  means that the request is to be performed synchronously.
    *  <p>When done asynchronously,
    *  this method will return immediately after sending the request to the
    *  debug engine without waiting for a response from
    *  the debug engine. The response to the request will be
    *  received on a separate thread and client code will be notified of the
    *  the response via the event listener mechanism.
   *
   *  @exception java.io.IOException If there is a problem communicating
   *  with the debug engine.
   */

  public boolean stepInto(ViewInformation viewInformation, int sendReceiveControlFlags)
  throws java.io.IOException
  {
    return stepInto(viewInformation, sendReceiveControlFlags, null);
  }

  public boolean stepInto(ViewInformation viewInformation,
                          int sendReceiveControlFlags,
                          Object property)
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(2, "DebuggeeThread[" + debugEngineAssignedID() + "].stepInto(" + viewInformation.name() + ")");

    DebugEngine debugEngine = _owningProcess.debugEngine();

    if (!debugEngine.prepareForEPDCRequest(EPDC.Remote_Execute, sendReceiveControlFlags))
       return false;

    if (Model.checkFCTBit)
    {
        if (!debugEngine.getCapabilities().getRunCapabilities().stepIntoSupported())
        {
            debugEngine.cancelEPDCRequest(EPDC.Remote_Execute);
            return false;
        }
    }

    // TODO: Should we call currentLocationWithinView and check for null to
    // make sure that this is a valid view for the part in which the thread is
    // currently stopped before making the request?

    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(2, "Sending EPDC request: Remote_Execute");

    return debugEngine.processEPDCRequest(new EReqExecuteStepInto(debugEngineAssignedID(), viewInformation.index()),
                                          sendReceiveControlFlags,
                                          property);
  }

  /**
   * Do a step Return.
   * @param viewInformation EPDC dictates that a step must be performed w.r.t.
   * a particular kind of view. The debug engine uses this info to determine
   * the granularity of the step
   * (e.g. step 1 statement vs. step 1 machine instruction), among other things.
    *  @param sendReceiveControlFlags A set of flags which specify the mode
    *  in which this request is to be performed. There is a set of constants
    *  in DebugEngine which define the possible values for this argument.
    *  For example, a value of DebugEngine.sendReceiveDefault means that the
    *  request is to be done asynchronously, while a value of DebugEngine.sendReceiveSynchronously
    *  means that the request is to be performed synchronously.
    *  <p>When done asynchronously,
    *  this method will return immediately after sending the request to the
    *  debug engine without waiting for a response from
    *  the debug engine. The response to the request will be
    *  received on a separate thread and client code will be notified of the
    *  the response via the event listener mechanism.
   *
   *  @exception java.io.IOException If there is a problem communicating
   *  with the debug engine.
   */

  public boolean stepReturn(ViewInformation viewInformation, int sendReceiveControlFlags)
  throws java.io.IOException
  {
    return stepReturn(viewInformation, sendReceiveControlFlags, null);
  }

  public boolean stepReturn(ViewInformation viewInformation,
                            int sendReceiveControlFlags,
                            Object property)
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(2, "DebuggeeThread[" + debugEngineAssignedID() + "].stepReturn(" + viewInformation.name() + ")");

    DebugEngine debugEngine = _owningProcess.debugEngine();

    if (!debugEngine.prepareForEPDCRequest(EPDC.Remote_Execute, sendReceiveControlFlags))
       return false;

    if (Model.checkFCTBit)
    {
        if (!debugEngine.getCapabilities().getRunCapabilities().stepReturnSupported())
        {
            debugEngine.cancelEPDCRequest(EPDC.Remote_Execute);
            return false;
        }
    }

    // TODO: Should we call currentLocationWithinView and check for null to
    // make sure that this is a valid view for the part in which the thread is
    // currently stopped before making the request?

    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(2, "Sending EPDC request: Remote_Execute");

    return debugEngine.processEPDCRequest(new EReqExecuteStepReturn(debugEngineAssignedID(), viewInformation.index()),
                                          sendReceiveControlFlags,
                                          property);
  }

  public void print(PrintWriter printWriter)
  {
    if (Model.includePrintMethods)
    {
       printWriter.print("DU: " + debugEngineAssignedID());
       printWriter.print("  TID: " + systemAssignedID());
       printWriter.print("  State: " + state());
       printWriter.print("  Debug State: " + debugState());
       printWriter.println("  Priority: " + priority());
       try
       {
         printWriter.println("Thread Stopped in Part: " + partInWhichThreadIsCurrentlyStopped().name());
       }
       catch(java.io.IOException excp)
       {
       }

       DebugEngine debugEngine = owningProcess().debugEngine();

       ViewInformation[] views = debugEngine.supportedViews();

       for (int i = 0; i < views.length; i++)
           if (views[i] != null)
           {
              Location location = null;

              try
              {
                location = currentLocationWithinView(views[i]);
              }
              catch(java.io.IOException excp)
              {
              }

              if (location == null)
                 continue;

                   printWriter.print("Thread is currently stopped in the ");
                   printWriter.print(views[i].name() + " view at this location: ");

                   location.print(printWriter);

                   printWriter.println();
           }
     }
  }

  /**
   * This is the method to request to monitor the stack for a given thread.
   * @param sendReceiveControlFlags the flag indicating the state in which the
   * request is to be sent(synchronized, asynchronized).
   * <p>To get the stack information, use the method getStack().
   * @return 'true' if the epdc request is sent successfully and 'false' otherwise.
   * @exception java.io.IOException if there is a communication problem
   * with the debug engine, this exception occurs.
   * @see DebuggeeThread#getStack()
   */

  public boolean monitorStack(int sendReceiveControlFlags)
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(2, "DebuggeeThread[" + debugEngineAssignedID() + "].monitorStack(" + sendReceiveControlFlags + ")");

    if (_stack == null)
    {
      DebugEngine debugEngine = _owningProcess.debugEngine();

      if (!debugEngine.prepareForEPDCRequest(EPDC.Remote_Stack, sendReceiveControlFlags))
        return false;

      if (Model.checkFCTBit)
      {
          if (!debugEngine.getCapabilities().getWindowCapabilities().monitorStackSupported())
          {
              debugEngine.cancelEPDCRequest(EPDC.Remote_Stack);
              return false;
          }
      }

      if (Model.TRACE.EVT && Model.traceInfo())
        Model.TRACE.evt(2, "Sending EPDC request: Remote_Stack");

      if (!debugEngine.processEPDCRequest(new EReqStack(debugEngineAssignedID()),
                                          sendReceiveControlFlags))
        return false;

      return true;
    }
    else
      return false;
  }

  /**
   * Call this method to obtain the stack information for this thread.
   * <p>Need to call monitorStack() first if it has not been called before.
   * @see DebuggeeThread#monitorStack()
   */
  public Stack getStack()
  {
    return _stack;
  }

  synchronized void add(Stack stack)
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "DebuggeeThread[" + debugEngineAssignedID() + "].add(Stack<" + stack.owningThread().debugEngineAssignedID() + ">)");

    _stack = stack;

    //Queue StackAddedEvent here before StackFrameAddedEvent
    DebugEngine debugEngine = _owningProcess.debugEngine();
    int requestCode = debugEngine.getMostRecentReply().getReplyCode();

    debugEngine.getEventManager().addEvent(new StackAddedEvent(this,
                                                               stack,
                                                               requestCode),
                                          _eventListeners);
  }

  synchronized void removeStack()
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "DebuggeeThread[" + debugEngineAssignedID() + "].removeStack()");

    _stack.prepareToDie();

    _stack.setHasBeenDeleted();

    _stack = null;
  }

  //case when this thread is marked "HasBeenDeleted", info its children
  void tellChildrenThatOwnerHasBeenDeleted()
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "DebuggeeThread[" + debugEngineAssignedID() + "].tellChildrenThatOwnerHasBeenDeleted()");

    if (_stack != null)
      _stack.setOwnerHasBeenDeleted();

    if (_monRegisterGroups != null)
      for (int i=0; i<_monRegisterGroups.size(); i++)
        if (_monRegisterGroups.elementAt(i) != null)
          ((MonitoredRegisterGroup)_monRegisterGroups.elementAt(i)).setOwnerHasBeenDeleted();
  }

  /**
   * This is the method to request to monitor a group of registers for this thread.
   * @param group the RegisterGroup to be monitored
   * @param sendReceiveControlFlags the flag indicating the state in which the
   * request is to be sent(synchronized, asynchronized).
   * @return 'true' if the epdc request is sent successfully and 'false' otherwise.
   * @exception java.io.IOException if there is a communication problem
   * with the debug engine, this exception occurs.
   */
  public boolean monitorRegisterGroup(RegisterGroup group, int sendReceiveControlFlags)
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(2, "DebuggeeThread[" + debugEngineAssignedID() + "].monitorRegisterGroup(" + group.getGroupName() + ")");

    if (getMonRegisterGroup(group) == null)
    {
      DebugEngine debugEngine = _owningProcess.debugEngine();

      if (!debugEngine.prepareForEPDCRequest(EPDC.Remote_Registers2, sendReceiveControlFlags))
        return false;

      if (Model.checkFCTBit)
      {
          if (!debugEngine.getCapabilities().getWindowCapabilities().monitorRegistersSupported())
          {
              debugEngine.cancelEPDCRequest(EPDC.Remote_Registers2);
              return false;
          }
      }

      if (Model.TRACE.EVT && Model.traceInfo())
        Model.TRACE.evt(2, "Sending EPDC request: Remote_Registers2");

      return debugEngine.processEPDCRequest(new EReqRegisters2(debugEngineAssignedID(),
                                                              group.getGroupID(),
                                                              EPDC.RegistersEnabled),
                                            sendReceiveControlFlags);
    }
    else
      return false;
  }

  /**
   * This is the method to request to monitor all register groups for this thread.
   * Client code can call this method to start monitoring all available register
   * groups, unless there are already register groups being monitored. Then a method
   * stopMonitoringAllRegisterGroups() will need to be called first before calling
   * this one.
   * @param sendReceiveControlFlags the flag indicating the state in which the
   * request is to be sent(synchronized, asynchronized).
   * @return 'true' if the epdc request is sent successfully and 'false' otherwise.
   * @exception java.io.IOException if there is a communication problem
   * with the debug engine, this exception occurs.
   */
  public boolean monitorAllRegisterGroups(int sendReceiveControlFlags)
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(2, "DebuggeeThread[" + debugEngineAssignedID() + "].monitorAllRegisterGroups()");

    if (_monRegisterGroups == null)
    {
      DebugEngine debugEngine = _owningProcess.debugEngine();

      // Make sure that we have asked BE for register groups before we send EPDC request to
      //monitor all groups. Problems actually occure if we don't call
      //DebugEngine.getRegisterGroups here. While it is processing the EPDC reply, model needs
      //information of register groups to update itself. If register groups have not been
      //retrieved yet, model will try to send another EPDC request to get them, which will
      //cause failure in DebugEnging.prepareForEPDCRequest().
      // Now client code is safe to call this method without worrying whether it needs to ask
      //for register groups first or not.
      if (debugEngine.getRegisterGroups() == null)
        return false;

      if (!debugEngine.prepareForEPDCRequest(EPDC.Remote_Registers2, sendReceiveControlFlags))
        return false;

      if (Model.checkFCTBit)
      {
          if (!debugEngine.getCapabilities().getWindowCapabilities().monitorRegistersSupported())
          {
              debugEngine.cancelEPDCRequest(EPDC.Remote_Registers2);
              return false;
          }
      }

      if (Model.TRACE.EVT && Model.traceInfo())
        Model.TRACE.evt(2, "Sending EPDC request: Remote_Registers2");

      return debugEngine.processEPDCRequest(new EReqRegisters2(debugEngineAssignedID(),
                                                                0,
                                                                EPDC.RegistersEnabled),
                                            sendReceiveControlFlags);
    }
    else
      return false;
  }

  synchronized void add(MonitoredRegisterGroup group)
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "DebuggeeThread[" + debugEngineAssignedID() + "].add(MonitoredRegisterGroup<" + group.owningRegisterGroup().getGroupName() + ">)");

    if (_monRegisterGroups == null)
      _monRegisterGroups = new Vector();

    setVectorElementToObject(group, _monRegisterGroups, group.getID());

    // Queue MonRegGroupAddedEvent here:
    DebugEngine debugEngine = _owningProcess.debugEngine();
    int requestCode = debugEngine.getMostRecentReply().getReplyCode();

    debugEngine.getEventManager().addEvent(new MonitoredRegisterGroupAddedEvent(this,
                                                               group,
                                                               requestCode),
                                          _eventListeners);
  }

  synchronized void addAllMonRegisterGroups()
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "DebuggeeThread[" + debugEngineAssignedID() + "].addAllMonRegisterGroups()");

    if (_monRegisterGroups == null)
      _monRegisterGroups = new Vector();

    DebugEngine debugEngine = _owningProcess.debugEngine();
    RegisterGroup[] regGroups = debugEngine.getRegisterGroups();

    if (regGroups == null || regGroups.length == 0)
    {
      if (Model.TRACE.ERR && Model.traceInfo())
        Model.TRACE.err(1, "RegisterGroups is null");
      return;
    }

    for (int i=0; i<regGroups.length; i++)
      add(new MonitoredRegisterGroup(this, regGroups[i]));
  }

  // get monitored RegisterGroup if exists, otherwise add a new one use add()
  // To be used to file changed registers.

  MonitoredRegisterGroup getMonRegisterGroup(int groupID)
  {
    if (_monRegisterGroups == null)
      return null;

    try
    {
      return (MonitoredRegisterGroup)(_monRegisterGroups.elementAt(groupID));
    }
    catch (ArrayIndexOutOfBoundsException excp)
    {
      if (Model.TRACE.ERR && Model.traceInfo())
        Model.TRACE.err(3, "Exception{" + excp.getMessage() + "} occurred in DebuggeeThread.getMonRegisterGroup(" + groupID + ")");

      return null;
    }
  }

  /**
   * Call this method to get all monitored register groups for this thread.
   * @return a Vector of MonitoredRegisterGroup objects if any, 'null' otherwise.
   * @see MonitoredRegisterGroup
   */

  public Vector getMonRegisterGroups()
  {
    if (_monRegisterGroups == null)
      return null;

    Vector temp = new Vector();
    for (int i=0; i<_monRegisterGroups.size(); i++)
      if (_monRegisterGroups.elementAt(i) != null)
        temp.addElement(_monRegisterGroups.elementAt(i));

    if (temp.size() == 0)
      return null;

    return temp;
  }

  /**
   * Call this method to get a monitored register group for a given RegisterGroup
   * object.
   * @return a MonitoredRegisterGroup object if the RegisterGroup is being monitored
   * for this thread, 'null' otherwise.
   * @see MonitoredRegisterGroup
   */
  public MonitoredRegisterGroup getMonRegisterGroup(RegisterGroup group)
  {
    return getMonRegisterGroup(group.getGroupID());
  }

  /**
   * Call this method to stop monitoring all register groups that are currently
   * monitored for this thread.
   * @return 'true' if the epdc request is sent successfully and 'false' otherwise.
   * @exception java.io.IOException if there is a communication problem
   * with the debug engine, this exception occurs.
   */
  public boolean stopMonitoringAllRegisterGroups(int sendReceiveControlFlags)
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "DebuggeeThread[" + debugEngineAssignedID() + "].stopMonitoringAllRegisterGroups(" + sendReceiveControlFlags + ")");

    if (getMonRegisterGroups() != null)
    {
      DebugEngine debugEngine = _owningProcess.debugEngine();

      if (!debugEngine.prepareForEPDCRequest(EPDC.Remote_RegistersFree2, sendReceiveControlFlags))
        return false;

      if (Model.checkFCTBit)
      {
          if (!debugEngine.getCapabilities().getWindowCapabilities().monitorRegistersSupported())
          {
              debugEngine.cancelEPDCRequest(EPDC.Remote_RegistersFree2);
              return false;
          }
      }

      if (Model.TRACE.EVT && Model.traceInfo())
        Model.TRACE.evt(2, "Sending EPDC request: Remote_RegistersFree2");

      return debugEngine.processEPDCRequest(new EReqRegistersFree2(debugEngineAssignedID(),
                                                                    0),
                                            sendReceiveControlFlags);
    }
    else
      return false;
  }

  synchronized void remove(MonitoredRegisterGroup group)
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(4, "DebuggeeThread[" + debugEngineAssignedID() + "].remove(MonitoredRegisterGroup<" + group.owningRegisterGroup().getGroupName() + ">)");

    group.prepareToDie();
    group.setHasBeenDeleted();

    try
    {
      _monRegisterGroups.setElementAt(null, group.getID());
    }
    catch (ArrayIndexOutOfBoundsException excp)
    {
      if (Model.TRACE.ERR && Model.traceInfo())
        Model.TRACE.err(3, "Exception{" + excp.getMessage() + "} occurred in DebuggeeThread.remove(MonitoredRegisterGroup<" + group.owningRegisterGroup().getGroupName() + ">)");
    }
  }

  synchronized void removeAllMonRegisterGroups()
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(4, "DebuggeeThread[" + debugEngineAssignedID() + "].removeAllMonRegisterGroups()");

    if (_monRegisterGroups == null)
      return;

    for (int i=0; i<_monRegisterGroups.size(); i++)
      if (_monRegisterGroups.elementAt(i) != null)
        remove((MonitoredRegisterGroup)_monRegisterGroups.elementAt(i));

    _monRegisterGroups = null;
  }

  // public boolean stepException(ViewInformation viewInformation, int sendReceiveControlFlags)
  public boolean stepException(int sendReceiveControlFlags)
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      // Model.TRACE.dbg(2, "DebuggeeThread[" + debugEngineAssignedID() + "].stepException(" + viewInformation.name() + ")");
      Model.TRACE.dbg(2, "DebuggeeThread[" + debugEngineAssignedID() + "].stepException()");

    if (_isExceptionRaised)
    {
      _isExceptionRaised = false;

      DebugEngine debugEngine = _owningProcess.debugEngine();

      if (!debugEngine.prepareForEPDCRequest(EPDC.Remote_Execute, sendReceiveControlFlags))
        return false;

      if (Model.checkFCTBit)
      {
          if (!debugEngine.getCapabilities().getExceptionCapabilities().exceptionStepSupported())
          {
              debugEngine.cancelEPDCRequest(EPDC.Remote_Execute);
              return false;
          }
      }

      if (Model.TRACE.EVT && Model.traceInfo())
        Model.TRACE.evt(2, "Sending EPDC request: Remote_Execute");

      // return debugEngine.processEPDCRequest(new EReqExecuteStepException(debugEngineAssignedID(), viewInformation.index()),
      return debugEngine.processEPDCRequest(new EReqExecuteStepException(debugEngineAssignedID(), (short)1),
                                            sendReceiveControlFlags
                                           );
    }
    return false;
  }

  //public boolean runException(ViewInformation viewInformation, int sendReceiveControlFlags)
  public boolean runException(int sendReceiveControlFlags)
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      // Model.TRACE.dbg(2, "DebuggeeThread[" + debugEngineAssignedID() + "].runException(" + viewInformation.name() + ")");
      Model.TRACE.dbg(2, "DebuggeeThread[" + debugEngineAssignedID() + "].runException()");

    if (_isExceptionRaised)
    {
      _isExceptionRaised = false;

      DebugEngine debugEngine = _owningProcess.debugEngine();

      if (!debugEngine.prepareForEPDCRequest(EPDC.Remote_Execute, sendReceiveControlFlags))
        return false;

      if (Model.checkFCTBit)
      {
          if (!debugEngine.getCapabilities().getExceptionCapabilities().exceptionRunSupported())
          {
              debugEngine.cancelEPDCRequest(EPDC.Remote_Execute);
              return false;
          }
      }

      if (Model.TRACE.EVT && Model.traceInfo())
        Model.TRACE.evt(2, "Sending EPDC request: Remote_Execute");

      // return debugEngine.processEPDCRequest(new EReqExecuteRunException(debugEngineAssignedID(), viewInformation.index()),
      return debugEngine.processEPDCRequest(new EReqExecuteRunException(debugEngineAssignedID(), (short)1),
                                            sendReceiveControlFlags
                                           );
    }
    return false;
  }

  // public boolean ignoreException(ViewInformation viewInformation, int sendReceiveControlFlags)
  public boolean ignoreException(int sendReceiveControlFlags)
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      //Model.TRACE.dbg(2, "DebuggeeThread[" + debugEngineAssignedID() + "].ignoreException(" + viewInformation.name() + ")");
      Model.TRACE.dbg(2, "DebuggeeThread[" + debugEngineAssignedID() + "].ignoreException()");

    if (_isExceptionRaised)
    {
      _isExceptionRaised = false;

      DebugEngine debugEngine = _owningProcess.debugEngine();

      if (!debugEngine.prepareForEPDCRequest(EPDC.Remote_Execute, sendReceiveControlFlags))
        return false;

      if (Model.checkFCTBit)
      {
          if (!debugEngine.getCapabilities().getExceptionCapabilities().exceptionExamineSupported())
          {
              debugEngine.cancelEPDCRequest(EPDC.Remote_Execute);
              return false;
          }
      }

      if (Model.TRACE.EVT && Model.traceInfo())
        Model.TRACE.evt(2, "Sending EPDC request: Remote_Execute");

      // return debugEngine.processEPDCRequest(new EReqExecuteExamineException(debugEngineAssignedID(), viewInformation.index()),
      return debugEngine.processEPDCRequest(new EReqExecuteExamineException(debugEngineAssignedID(), (short)1),
                                            sendReceiveControlFlags
                                           );
    }
    return false;
  }

  void exceptionRaised()
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(4, "DebuggeeThread[" + debugEngineAssignedID() + "].exceptionRaised()");

    _isExceptionRaised = true;
  }

  /**
   * This method is used by Java Serialization - it is called from
   * ObjectOutputStream.writeObject in order to serialize objects of this
   * class.
   * <p>IMPORTANT: This method must be kept in synch with the readObject
   * method for this class so that we only ever try to read in those fields
   * that were written out (and in the same order). This includes the
   * possibility that the writeObject method has chosen to write nothing
   * out in which case the readObject method must be prepared to read nothing
   * in.
   * <p>If we want to write out the entire object, we will call the default
   * method provided by Java - ObjectOutputStream.defaultWriteObject. This
   * default method writes out all non-static, non-transient fields.
   */

  private void writeObject(ObjectOutputStream stream)
  throws java.io.IOException
  {
    // See if we want to save all (non-transient) fields in the object
    // or only some of them:

    if (stream instanceof ModelObjectOutputStream)
    {
       int flags = ((ModelObjectOutputStream)stream).getSaveFlags();

       if ((flags & SaveRestoreFlags.ALL_OBJECTS) != 0)
          stream.defaultWriteObject();
       else
       if ((flags & SaveRestoreFlags.RESTORABLE_OBJECTS) != 0)
       {
          // Save any objects that restorable objects might depend on
          // in order to be restored properly:

          stream.writeObject(_epdcThread);
       }
    }
    else
       stream.defaultWriteObject();
  }

  /**
   * This method is used by Java Serialization - it is called from
   * ObjectInputStream.readObject in order to serialize objects of this
   * class.
   * <p>IMPORTANT: This method must be kept in synch with the writeObject
   * method for this class so that we only ever try to read in those fields
   * that were written out (and in the same order). This includes the
   * possibility that the writeObject method has chosen to write nothing
   * out in which case the readObject method must be prepared to read nothing
   * in.
   * <p>If we want to read in the entire object, we will call the default
   * method provided by Java - ObjectInputStream.defaultReadObject. This
   * default method reads in all non-static, non-transient fields.
   */

  private void readObject(ObjectInputStream stream)
  throws java.io.IOException,
         java.lang.ClassNotFoundException
  {
    // See if we need to read all (non-transient) fields in the object
    // or only some of them:

    if (stream instanceof ModelObjectInputStream)
    {
       int flags = ((ModelObjectInputStream)stream).getSaveFlags();

       if ((flags & SaveRestoreFlags.ALL_OBJECTS) != 0)
          stream.defaultReadObject();
       else
       if ((flags & SaveRestoreFlags.RESTORABLE_OBJECTS) != 0)
       {
          // Read any objects that restorable objects might depend on
          // in order to be restored properly:

          _epdcThread = (ERepGetNextThread)stream.readObject();
       }
    }
    else
       stream.defaultReadObject();
  }

  /**
   * The method is used when the program is about to fork and the user
   * has requested to follow the child process. Since this request is not
   * dependent on a specific thread, the thread id of zero is passed to
   * the engine.
   * @exception java.io.IOException If there is a problem communicating
   * with the debug engine.
   */
  public boolean executeForkAndFollowChild(int sendReceiveControlFlags)
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(2, "DebuggeeThread[" + debugEngineAssignedID() +
                                         "].executeForkAndFollowChild()");

    DebugEngine debugEngine = _owningProcess.debugEngine();

    if (!debugEngine.prepareForEPDCRequest(EPDC.Remote_Execute,
                                           sendReceiveControlFlags))
       return false;

    if (Model.TRACE.EVT && Model.traceInfo())
        Model.TRACE.evt(2,
                     "Sending EPDC request: Remote_Execute (ForkFollowChild)");

    // DU is set to zero
    EReqExecuteForkFollowChild request = new EReqExecuteForkFollowChild(0);

    return debugEngine.processEPDCRequest(request, sendReceiveControlFlags);

  }

  /**
   * The method is used when the program is about to fork and the user
   * has requested to follow the parent process. Since this request is not
   * dependent on a specific thread, the thread id of zero is passed to
   * the engine.
   * @exception java.io.IOException If there is a problem communicating
   * with the debug engine.
   */
  public boolean executeForkAndFollowParent(int sendReceiveControlFlags)
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(2, "DebuggeeThread[" + debugEngineAssignedID() +
                                         "].executeForkAndFollowParent()");

    DebugEngine debugEngine = _owningProcess.debugEngine();

    if (!debugEngine.prepareForEPDCRequest(EPDC.Remote_Execute,
                                           sendReceiveControlFlags))
       return false;

    if (Model.TRACE.EVT && Model.traceInfo())
        Model.TRACE.evt(2,
                    "Sending EPDC request: Remote_Execute (ForkFollowParent)");

    // DU is set to zero
    EReqExecuteForkFollowParent request = new EReqExecuteForkFollowParent(0);

    return debugEngine.processEPDCRequest(request, sendReceiveControlFlags);

  }

  /**
   * Remove references so they can be gc'ed.
   */
  void cleanup()
  {
    _epdcThread = null;
    _owningProcess = null;
    if (_eventListeners != null)
       _eventListeners.removeAllElements();
    _currentPart = null;

    _stack = null;
     _monRegisterGroups = null;
    _localExpressionsMonitor = null;
    _currentLocation = null;

    _attributes = null;
    _nameOrTID = _state = _priority = _group = _blockingThread = null;
  }

  private ERepGetNextThread _epdcThread;
  private DebuggeeProcess _owningProcess;
  private transient Vector _eventListeners = new Vector();
  private Part _currentPart;

  private Stack _stack;
  private Vector _monRegisterGroups;
  private LocalMonitoredExpressions _localExpressionsMonitor;
  private boolean _isExceptionRaised = false;
  private Location[] _currentLocation;

  private ThreadAttribute[] _attributes;

  private ThreadAttribute _nameOrTID;
  private ThreadAttribute _state;
  private ThreadAttribute _priority;
  private ThreadAttribute _group;
  private ThreadAttribute _blockingThread;
}
