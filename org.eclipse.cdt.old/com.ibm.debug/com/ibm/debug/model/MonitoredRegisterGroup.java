package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/MonitoredRegisterGroup.java, java-model, eclipse-dev, 20011128
// Version 1.11.1.2 (last modified 11/28/01 16:12:57)
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

public class MonitoredRegisterGroup extends DebugModelObject
{
  MonitoredRegisterGroup(DebuggeeThread owningThread, RegisterGroup owningGroup)
  {
    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(3, "Creating MonitoredRegisterGroup : RegisterGroup<" + owningGroup.getGroupName() + ">");

    _owningThread = owningThread;
    _owningGroup = owningGroup;
  }

  int getID()
  {
    return _owningGroup.getGroupID();
  }

  synchronized void add(MonitoredRegister monReg)
  {
    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(4, "MonitoredRegisterGroup[" + _owningGroup.getGroupName() + "].add(MonitoredRegister<" + monReg.getName() + ">");

    // Case when start monitoring this register group, only require to fire
    //parent added evnet based on current agreement. No individual register added
    //event will be fired.
    if (_monRegisters == null)
      _monRegisters = new Vector();

    setVectorElementToObject(monReg, _monRegisters, monReg.getID());
  }

  synchronized void addNew(MonitoredRegister monReg)
  {
    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(4, "MonitoredRegisterGroup[" + _owningGroup.getGroupName() + "].addNew(MonitoredRegister<" + monReg.getName() + ">");

    // Case when individual register is added to this MonitoredRegisterGroup,
    //monitored register added event is fired.
    setVectorElementToObject(monReg, _monRegisters, monReg.getID());

    // Queue MonRegAddedEvent here:
    DebugEngine debugEngine = _owningThread.owningProcess().debugEngine();
    int requestCode = debugEngine.getMostRecentReply().getReplyCode();

    debugEngine.getEventManager().addEvent(new MonitoredRegisterAddedEvent(this,
                                                               monReg,
                                                               requestCode),
                                          _eventListeners);
  }

  MonitoredRegister getMonRegister(int id)
  {
    if (_monRegisters == null)
      return null;

    try
    {
      return (MonitoredRegister)(_monRegisters.elementAt(id));
    }
    catch (ArrayIndexOutOfBoundsException excp)
    {
      return null;
    }
  }

  synchronized void remove(MonitoredRegister monReg)
  {
    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(4, "MonitoredRegisterGroup[" + _owningGroup.getGroupName() + "].remove(MonitoredRegister<" + monReg.getName() + ">");

    monReg.prepareToDie();
    monReg.setHasBeenDeleted();

    try
    {
      _monRegisters.setElementAt(null, monReg.getID());
    }
    catch (ArrayIndexOutOfBoundsException excp)
    {
    }
  }

  synchronized void removeAllMonitoredRegisters()
  {
    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(4, "MonitoredRegisterGroup[" + _owningGroup.getGroupName() + "].removeAllMonitoredRegisters()");

    _monRegisters = null;
  }

  /**
   * Print this MonitoredRegisterGroup.
   */
  public void print(PrintWriter printWriter)
  {
    if (Model.includePrintMethods)
    {
      printWriter.println("Monitored Register Group:");
      printWriter.print("Register Group Name:" + _owningGroup.getGroupName());
      printWriter.println(" Owning Thread: thread #" + _owningThread.debugEngineAssignedID());
      super.print(printWriter);
      printWriter.println();
    }
  }

  /**
   * Return all monitored registers within this group.
   * @return a Vector of MonitoredRegister objects.
   * @see MonitoredRegister
   */
  public Vector getMonitoredRegisters()
  {
    return _monRegisters;
  }

  public void addEventListener(MonitoredRegisterGroupEventListener eventListener)
  {
    _eventListeners.addElement(eventListener);
  }

  public void removeEventListener(MonitoredRegisterGroupEventListener eventListener)
  {
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

  /**
   * Returns the thread to which this monitored register group belongs.
   * @return a DebuggeeThread object that contains this MonitoredRegisterGroup object.
   * @see DebuggeeThread
   */
  public DebuggeeThread owningThread()
  {
    return _owningThread;
  }

  /**
   * Returns the register group to which this monitored register group belongs.
   * @return a RegisterGroup object to which this monitored register group belongs.
   * @see RegisterGroup
   */
  public RegisterGroup owningRegisterGroup()
  {
    return _owningGroup;
  }

  void prepareToDie()
  {
    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(4, "MonitoredRegisterGroup[" + _owningGroup.getGroupName() + "].prepareToDie()");

    // Queue MonRegGroupEndedEvent here:
    DebugEngine debugEngine = _owningThread.owningProcess().debugEngine();
    int requestCode = debugEngine.getMostRecentReply().getReplyCode();

    debugEngine.getEventManager().addEvent(new MonitoredRegisterGroupEndedEvent(this,
                                                               this,
                                                               requestCode),
                                          _eventListeners);
  }

  /**
   * Call this method to stop monitoring this register groups.
   * @return 'true' if the epdc request is sent successfully and 'false' otherwise.
   * @exception java.io.IOException if there is a communication problem
   * with the debug engine, this exception occurs.
   */
  public boolean stopMonitoring(int sendReceiveControlFlags)
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "MonitoredRegisterGroup[" + _owningGroup.getGroupName() + "].stopMonitoring()");

    DebugEngine debugEngine = _owningThread.owningProcess().debugEngine();

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
      Model.TRACE.evt(2, "Sending EPDC request Remote_RegistersFree2");

    return debugEngine.processEPDCRequest(new EReqRegistersFree2(_owningThread.debugEngineAssignedID(),
                                                                    getID()),
                                            sendReceiveControlFlags);
  }

  // data field
  private DebuggeeThread _owningThread;
  private RegisterGroup _owningGroup;
  private Vector _monRegisters;
  private Vector _eventListeners = new Vector();
}
