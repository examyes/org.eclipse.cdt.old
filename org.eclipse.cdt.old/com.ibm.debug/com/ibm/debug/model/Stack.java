package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/Stack.java, java-model, eclipse-dev, 20011128
// Version 1.21.1.2 (last modified 11/28/01 16:12:08)
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
 * This class represents a stack which contains stack information for a debuggee thread.
 * Each stack object contains a list of stack entries for a particular thread.
 * @see DebuggeeThread
 * @see StackFrame
 */

public class Stack extends DebugModelObject
{
  Stack (DebuggeeThread owningThread, ERepGetChangedStack epdcStack)
  {
    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(1, "Creating Stack : DUID=" + owningThread.debugEngineAssignedID());

    _owningThread = owningThread;

    //work around
    owningThread.add(this);

    change(epdcStack);
  }

  /**
   * Get the debuggee thread which owns this stack.
   */

  public DebuggeeThread owningThread()
  {
    return _owningThread;
  }


  /**
   * Return a vector of all StackFrame objects in this Stack.
   */
  public Vector getStackFrames()
  {
    return _stackFrames;
  }

  StackFrame getStackFrame(int index)
  {
    try
    {
      return (StackFrame)(_stackFrames.elementAt(index-1));
    }
    catch (ArrayIndexOutOfBoundsException excp)
    {
      if (Model.TRACE.ERR && Model.traceInfo())
        Model.TRACE.err(3, "Exception{" + excp.getMessage() + "} occurred in Stack.getStackFrame(" + index + ")");

      return null;
    }
  }

  void change(ERepGetChangedStack epdcStack)
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "Stack[" + _owningThread.debugEngineAssignedID() + "].change(" + epdcStack + ")");

    //Update the stack entry list
    Vector epdcStackEntries = epdcStack.stackEntries();

    int i;
    int numberOfEntries;
    DebugEngine debugEngine = _owningThread.owningProcess().debugEngine();
    StackFrame stackFrame;

    //The number of stack entries inside that packet is always greater than 0.

    if ((epdcStackEntries != null) && ((numberOfEntries = epdcStackEntries.size()) > 0))
    {
      if (_stackFrames == null)
      {
        //if (Model.TRACE.EVT && Model.traceInfo())
          //Model.TRACE.evt(3, "Building an array of StackFrames");

        //Create a Vector to store the array of stack frames.
        _stackFrames = new Vector(numberOfEntries);
        for (i=0; i<numberOfEntries; i++)
          add(new StackFrame(this, i+1, (ERepGetNextStackEntry)epdcStackEntries.elementAt(i)));
      }
      else
      {
        int oldestChangedStackEntries = epdcStack.oldestChangedEntry();
        int numberOfUnchangedStackEntries = oldestChangedStackEntries - 1;
        int numberOfPreviousStackEntries = _stackFrames.size();
        int numberOfCurrentStackEntries = numberOfUnchangedStackEntries + numberOfEntries;

        int index = oldestChangedStackEntries;

        _stackFrames.ensureCapacity(numberOfCurrentStackEntries);
        for (i=0; i<numberOfEntries; i++)
        {
          //if (Model.TRACE.EVT && Model.traceInfo())
            //Model.TRACE.evt(3, "Updating changed StackFrames");

          // update changed stack frames
          if ((stackFrame = getStackFrame(index)) != null)
            stackFrame.change((ERepGetNextStackEntry)epdcStackEntries.elementAt(i));
          // add new stack frames
          else
          {
            //if (Model.TRACE.EVT && Model.traceInfo())
              //Model.TRACE.evt(3, "Adding new StackFrames");

            stackFrame = new StackFrame(this, index, (ERepGetNextStackEntry)epdcStackEntries.elementAt(i));
            add(stackFrame);

            //if (Model.TRACE.EVT && Model.traceInfo())
              //Model.TRACE.evt(3, "Adding StackFrameAddedEvent");

            //Queue StackFrameAddedEvent here
            int requestCode = debugEngine.getMostRecentReply().getReplyCode();
            debugEngine.getEventManager().addEvent(new StackFrameAddedEvent(this,
                                                                            stackFrame,
                                                                            requestCode),
                                                   _eventListeners);
          }
          index++;
        }

        //if (Model.TRACE.EVT && Model.traceInfo())
          //Model.TRACE.evt(3, "Deleting non-existing StackFrames if any");

        // delete old stack frames
        for (;index<=numberOfPreviousStackEntries;index++)
          if ((stackFrame = getStackFrame(index)) != null)
            remove(stackFrame);

        // keep the Vector size up-to-date
        _stackFrames.setSize(numberOfCurrentStackEntries);
      }
    }
  }

  public void addEventListener(StackEventListener eventListener)
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "Stack[" + _owningThread.debugEngineAssignedID() + "].addEventListener(" + eventListener + ")");

    _eventListeners.addElement(eventListener);
  }

  public void removeEventListener(StackEventListener eventListener)
  {
    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(3, "Stack[" + _owningThread.debugEngineAssignedID() + "].removeEventListener(" + eventListener + ")");

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

  synchronized void add(StackFrame stackFrame)
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "Stack[" + _owningThread.debugEngineAssignedID() + "].add(StackFrame<" + stackFrame.index() + ">)");

    if (_stackFrames == null)
      _stackFrames = new Vector();

    //  It is assumed here that stack entries sent back in the stack changed packet
    //are always ordinal. Since the change() method above guarantees that the size of
    //the vector _stackFrame is always up to date. It is therefore safe to add each
    //new stackFrame to _stackFrames using addElement().
    _stackFrames.addElement(stackFrame);
    //DebuggeeProcess.setVectorElementToObject(stackFrame, _stackFrames, index);

    //No longer queue StackFrameAddedEvent here based on 03/20/98 meeting agreement
/*
    //Queue StackFrameAddedEvent
    DebugEngine debugEngine = _owningThread.owningProcess().debugEngine();
    int requestCode = debugEngine.getMostRecentReply().getReplyCode();
    debugEngine.getEventManager().addEvent(new StackFrameAddedEvent(this,
                                                                    stackFrame,
                                                                    requestCode),
                                           _eventListeners);
*/
  }

  /**
   * Print this Stack object.
   */

  public void print(PrintWriter printWriter)
  {
    if (Model.includePrintMethods)
    {
      printWriter.println("Call Stack:");
      super.print(printWriter);
      printWriter.println();

      /*
      super.print(printWriter);

      printWriter.println();
      printWriter.println("Call Stack:");
      if ((_stackFrames != null) && (_stackFrames.size() > 0))
        for (int j=0; j<_stackFrames.size(); j++)
          ((StackFrame)_stackFrames.elementAt(j)).print(printWriter);
      */
    }
  }

  /**
   * This is the method to request to free the stack for a given thread.
   * @param sendReceiveControlFlags the flag indicating the state in which the
   * request is to be sent(synchronized, asynchronized).
   * @return 'true' if the epdc request is sent successfully and 'false' otherwise.
   * <p>If the request is processed successfully, the StackEventListener will be
   * notified when the epdc reply returns.
   * @exception java.io.IOException if there is a communication problem
   * with the debug engine, this exception occurs.
   * @see StackEventListener
   */

  public boolean freeStack (int sendReceiveControlFlags)
  throws IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(2, "Stack[" + _owningThread.debugEngineAssignedID() + "].freeStack()");

    DebugEngine debugEngine = _owningThread.owningProcess().debugEngine();

    if (!debugEngine.prepareForEPDCRequest(EPDC.Remote_StackFree, sendReceiveControlFlags))
      return false;

    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(2, "Sending EPDC request: Remote_StackFree");

    if (!debugEngine.processEPDCRequest(new EReqStackFree(_owningThread.debugEngineAssignedID()),
                                          sendReceiveControlFlags))
      return false;

    return true;
  }

  void prepareToDie()
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "Stack[" + _owningThread.debugEngineAssignedID() + "].prepareToDie()");

    // Since we agreed that we no longer fire child ended events when parent is ended,
    //removeAllStackFrames() is not necessary to be called here. Each child will be
    //notified that the parent is deleted in the tellChildrenThatOwnerHasBeenDeleted().
/*
    // When this stack dies, all its stack frames die with it
    //(i.e. StackFrameEndedEvent will be fired for each stack frame)
    removeAllStackFrames();
*/

    // Queue StackEndedEvent here
    DebugEngine debugEngine = _owningThread.owningProcess().debugEngine();

    int requestCode = debugEngine.getMostRecentReply().getReplyCode();

    debugEngine.getEventManager().addEvent(new StackEndedEvent(this,
                                                                this,
                                                                requestCode
                                                               ),
                                           _eventListeners
                                          );
  }

  void tellChildrenThatOwnerHasBeenDeleted()
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "Stack[" + _owningThread.debugEngineAssignedID() + "].tellChildrenThatOwnerHasBeenDeleted()");

    if (_stackFrames != null)
      for (int i=0; i<_stackFrames.size(); i++)
        if (_stackFrames.elementAt(i) != null)
          ((StackFrame)_stackFrames.elementAt(i)).setOwnerHasBeenDeleted();
  }

/*
  synchronized void removeStackFrame(int index)
  {
    StackFrame stackFrame = getStackFrame(index);
    stackFrame.prepareToDie();
    stackFrame.setHasBeenDeleted();

    stackFrame = null;
  }
*/

  synchronized void remove(StackFrame stackFrame)
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "Stack[" + _owningThread.debugEngineAssignedID() + "].remove(StackFrame<" + stackFrame.index() + ">)");

    stackFrame.prepareToDie();
    stackFrame.setHasBeenDeleted();

    try
    {
      // Right now the Vector contains a full list of StackFrame objects,
      //because StackFrame doesn't really have a real id (which ought to be assigned
      //by the backend). So we now use the index (which we use as a fake id) to
      //find the position of a StackFrame object, and this index is always 1 bigger
      //than the actual index. Hopefully, picl can return the stack entry id in the
      //future, and there can be more consistency in the model.
      _stackFrames.setElementAt(null, stackFrame.index()-1);
    }
    catch (ArrayIndexOutOfBoundsException excp)
    {
    }
  }

  synchronized void removeAllStackFrames()
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "Stack[" + _owningThread.debugEngineAssignedID() + "].removeAllStackFrames()");

    if (_stackFrames == null)
      return;

    for (int i=0; i<_stackFrames.size(); i++)
      if (_stackFrames.elementAt(i) != null)
        remove((StackFrame)_stackFrames.elementAt(i));
  }

  private DebuggeeThread _owningThread;
  private Vector _stackFrames;
  private Vector _eventListeners = new Vector();
}
