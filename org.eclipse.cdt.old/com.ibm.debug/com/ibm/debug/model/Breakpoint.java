package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/Breakpoint.java, java-model, eclipse-dev, 20011128
// Version 1.20.1.2 (last modified 11/28/01 16:11:20)
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

/**
 * An abstract class which is the superclass for all breakpoint types in
 * the Model. This class is subclassed by LocationBreakpoint and
 * EventBreakpoint. Location breakpoints are breakpoints which can be
 * mapped to a specific location in the user's code. These include
 * LineBreakpoints, AddressBreakpoints, and EntryBreakpoints, all of which
 * are subclasses of LocationBreakpoint. EventBreakpoints are breakpoints
 * that are associated with some event occuring in the debuggee. The EventBreakpoint
 * class has subclasses Watchpoint and ModuleLoadBreakpoint.
 * @see LocationBreakpoint
 * @see LineBreakpoint
 * @see AddressBreakpoint
 * @see EntryBreakpoint
 * @see EventBreakpoint
 * @see Watchpoint
 * @see ModuleLoadBreakpoint
 */

public abstract class Breakpoint extends DebugModelObject
{
  Breakpoint(DebuggeeProcess owningProcess, ERepGetNextBkp epdcBkp)
  {
    _owningProcess = owningProcess;

    change(epdcBkp, true);
  }

  public void addEventListener(BreakpointEventListener eventListener)
  {
    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(3, "Breakpoint[" + id() + "].addEventListener(" + eventListener + ")");

    _eventListeners.addElement(eventListener);
  }

  public void removeEventListener(BreakpointEventListener eventListener)
  {
    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(3, "Breakpoint[" + id() + "].removeEventListener(" + eventListener + ")");

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

  int id()
  {
    return _epdcBkp.getID();
  }

  /**
   * Get the process in which this breakpoint is set.
   */

  public DebuggeeProcess getOwningProcess()
  {
    return _owningProcess;
  }

  /**
   * Tell the debug engine to delete this breakpoint.
   * @param sendReceiveControlFlags A set of flags which specify the mode
   * in which this request is to be performed. There is a set of constants
   * in DebugEngine which define the possible values for this argument.
   * For example, a value of DebugEngine.sendReceiveDefault means that the
   * request is to be done asynchronously, while a value of
   * DebugEngine.sendRequestSynchronously
   * means that the request is to be performed synchronously.
   * <p>When done asynchronously,
   * this method will return immediately after sending the request to the
   * debug engine without waiting for a response from
   * the debug engine. The response to the request will be
   * received on a separate thread and client code will be notified of the
   * the response via the event listener mechanism.
   * @return 'true' if the breakpoint delete request was successfully sent to
   * the debug engine, 'false' otherwise. Note that a return value of 'true'
   * does not imply that the debug engine was able to delete the breakpoint but
   * rather simply that the request was successfully sent to the debug engine.
   * Whether or not the breakpoint was actually deleted by the debug engine will
   * be indicated via the event listener mechanism.
   *  @exception java.io.IOException If there is a problem communicating
   *  with the debug engine.
   */

  public boolean remove(int sendReceiveControlFlags)
  throws java.io.IOException
  {
    return remove(sendReceiveControlFlags, null);
  }

  public boolean remove(int sendReceiveControlFlags, Object property)
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "Breakpoint[" + id() + "].remove(" + sendReceiveControlFlags + ")");

    DebugEngine debugEngine = _owningProcess.debugEngine();

    if (debugEngine.prepareForEPDCRequest(EPDC.Remote_BreakpointClear,
                                           sendReceiveControlFlags) &&
        debugEngine.processEPDCRequest(new EReqBreakpointClear(id()),
                                           sendReceiveControlFlags,
                                           property))
       return true;
    else
       return false;
  }

  // A call to 'remove' (if successful) will ultimately end up in a call
  // to 'prepareToDie'. 'prepareToDie' gives the Breakpoint object a chance
  // to say goodbye to its event listeners...

  void prepareToDie()
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "Breakpoint[" + id() + "].prepareToDie()");

    DebugEngine debugEngine = _owningProcess.debugEngine();

    int requestCode = debugEngine.getMostRecentReply().getReplyCode();

    debugEngine.getEventManager().addEvent(new BreakpointDeletedEvent(this,
                                                                this,
                                                                requestCode
                                                               ),
                                           _eventListeners
                                          );
  }

  /**
   * Send a request to enable a breakpoint that has been disabled.
   * If the breakpoint is already enabled no request will be sent, or if
   * the current engine does not have any capability for enabling breakpoints
   * no request is sent.
   * @param sendReceievControlFlags this flag indicates the state in
   * which the request is to be sent (synchronized or asynchronized).
   * @return 'true' if the request to enable the breakpoint was sent
   * successfully, and 'false' otherwise.
   * @exception java.io.IOException if there is a communication problem
   * with the debug engine, this exception occurs.
   */
  public boolean enable(int sendReceiveControlFlags)
  throws java.io.IOException
  {
    return enable(sendReceiveControlFlags, null);
  }

  public boolean enable(int sendReceiveControlFlags, Object property)
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "Breakpoint[" + id() + "].enable()");

    DebugEngine debugEngine = getOwningProcess().debugEngine();

    if (!debugEngine.prepareForEPDCRequest(EPDC.Remote_BreakpointEnable,
                                           sendReceiveControlFlags))
        return false;

    // If the breakpoint is already enabled or if the debug engine does not
    // have the capability of enabling a breakpoint cancel the request.
    if (Model.checkFCTBit)
    {
        if (!debugEngine.getCapabilities().getBreakpointCapabilities().breakpointEnableDisableSupported())
        {
            debugEngine.cancelEPDCRequest(EPDC.Remote_BreakpointEnable);
            return false;
        }
    }

    if (isEnabled())
    {
        debugEngine.cancelEPDCRequest(EPDC.Remote_BreakpointEnable);
        return false;
    }

    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(2, "Sending EPDC request: Remote_BreakpointEnable");

    if (!debugEngine.processEPDCRequest(new EReqBreakpointEnable(id()),
                                        sendReceiveControlFlags,
                                        property))
        return false;
    else
        return true;
  }

  /**
   * Send a request to disable a breakpoint that has been enabled.
   * If the breakpoint is already disabled no request will be sent, or if
   * the current engine does not have any capability for disabling breakpoints
   * no request is sent.
   * @param sendReceievControlFlags this flag indicates the state in
   * which the request is to be sent (synchronized or asynchronized).
   * @return 'true' if the request to disable the breakpoint was sent
   * successfully, and 'false' otherwise.
   * @exception java.io.IOException if there is a communication problem
   * with the debug engine, this exception occurs.
   */
  public boolean disable(int sendReceiveControlFlags)
  throws java.io.IOException
  {
    return disable(sendReceiveControlFlags, null);
  }

  public boolean disable(int sendReceiveControlFlags, Object property)
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "Breakpoint[" + id() + "].disable()");

    DebugEngine debugEngine = getOwningProcess().debugEngine();

    if (!debugEngine.prepareForEPDCRequest(EPDC.Remote_BreakpointDisable,
                                           sendReceiveControlFlags))
        return false;

    // If the breakpoint is already disabled or if the debug engine does not
    // have the capability of disabling a breakpoint cancel the request.
    if (Model.checkFCTBit)
    {
        if (!debugEngine.getCapabilities().getBreakpointCapabilities().breakpointEnableDisableSupported())
        {
            debugEngine.cancelEPDCRequest(EPDC.Remote_BreakpointDisable);
            return false;
        }
    }

    if (!isEnabled())
    {
        debugEngine.cancelEPDCRequest(EPDC.Remote_BreakpointDisable);
        return false;
    }

    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(2, "Sending EPDC request: Remote_BreakpointDisable");

    if (!debugEngine.processEPDCRequest(new EReqBreakpointDisable(id()),
                                        sendReceiveControlFlags,
                                        property))
        return false;
    else
        return true;
  }

  /**
   * Send a breakpoint changed event if this is not a new breakpoint
   * that is being added.
   */
  void change(ERepGetNextBkp epdcBkp, boolean isNew)
  {
    _epdcBkp = epdcBkp;

    if (isNew)
        return;

    DebugEngine debugEngine = _owningProcess.debugEngine();

    int requestCode = debugEngine.getMostRecentReply().getReplyCode();

    debugEngine.getEventManager().addEvent(new BreakpointChangedEvent(
                                                                   this,
                                                                   this,
                                                                   requestCode),
                                           _eventListeners
                                          );
  }

  /**
   * Returns 'true' if this breakpoint is enabled, 'false' otherwise.
   * @return 'true' if this breakpoint is enabled, 'false' otherwise.
   */

  public boolean isEnabled()
  {
    return _epdcBkp.isEnabled();
  }

  /**
   * Return 'true' if the breakpoint is deferred, 'false' otherwise.
   */
  public boolean isDeferred()
  {
    return _epdcBkp.isDeferred();
  }

  /**
   * Return 'true' if the breakpoint was set through auto set entry flag
   */
  public boolean isAutoSetEntry()
  {
    return _epdcBkp.isAutoSetEntry();
  }

  /**
   * Return 'true' if the debug engine supports modifying breakpoints but this
   * breakpoint cannot be modified, and 'false' otherwise.
   */
  public boolean isReadOnly()
  {
    if (_owningProcess.debugEngine().getCapabilities().getBreakpointCapabilities().breakpointModifySupported())
        return _epdcBkp.isReadOnly();

    return false;
  }

  /**
   * Return the number of times a breakpoint should stop when it is hit.
   */
  public int getEveryVal()
  {
    return _epdcBkp.getClause().everyVal();
  }

  /**
   * Return the upper limit of the number of times a breakpoint is stopped
   * when it has been hit (less than).
   */
  public int getToVal()
  {
    return _epdcBkp.getClause().toVal();
  }

  /**
   * Return the lower limit of the number of times a breakpoint is stopped
   * when it has been hit (greater than).
   */
  public int getFromVal()
  {
    return _epdcBkp.getClause().fromVal();
  }

  /**
   * Return the breakpoint attribute. The attribute in the change packet
   * could be one of the following:
   * - defer: adding a breakpoint to a module that has not yet been loaded
   * - defer active: a previously deferred breakpoint is now active
   * - defer ambiguous:
   * - defer failed: a previously deferred breakpoint failed to be added
   *                 when its module was loaded
   * - auto set entry: the breakpoint was set by auto entry set
   */
  public short getAttribute()
  {
    return _epdcBkp.getAttribute();
  }

  /**
   * Get the specific thread in which this breakpoint is set. Will return
   * null if the breakpoint applies to all threads, not just one, OR if
   * the breakpoint has been set in a thread that does not currently exist.
   * In the latter case, the breakpoint will be marked as 'deferred' and
   * the method getThreadID can be used to retrieve the ID of the thread
   * on which this bkp has been set.
   * @see Breakpoint#isDeferred
   * @see Breakpoint#getThreadID
   */

  public DebuggeeThread getThread()
  {
    return _owningProcess.getThread(_epdcBkp.getDU());
  }

  /**
   * Get the debug engine-assigned ID of the thread on which this breakpoint
   * has been set. Will return 0 if this breakpoint applies to all threads.
   * @see Breakpoint#getThread
   */

  public int getThreadID()
  {
    return _epdcBkp.getDU();
  }

  boolean restore(DebuggeeProcess targetProcess, int sendReceiveControlFlags)
  throws java.io.IOException
  {
    return false;
  }

  // NOTE: There is currently no writeObject nor readObject in this class
  // because we always want the default Java serialization done!

  private DebuggeeProcess _owningProcess;
  private transient Vector _eventListeners = new Vector();
  protected ERepGetNextBkp _epdcBkp;

}
