package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/MonitoredRegister.java, java-model, eclipse-dev, 20011128
// Version 1.7.1.3 (last modified 11/28/01 16:12:56)
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

public class MonitoredRegister extends DebugModelObject
{
  MonitoredRegister(MonitoredRegisterGroup owningGroup, ERepGetNextRegister epdcRegister)
  {
    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(4, "Creating MonitoredRegister : Name=" + epdcRegister.getName());

    _owningGroup = owningGroup;
    change(epdcRegister, true);
  }

  void change (ERepGetNextRegister epdcRegister, boolean isNew)
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(4, "MonitoredRegister[" + epdcRegister.getName() + "].change(IsNew:" + isNew + ")");

    _epdcRegister = epdcRegister;

    if (isNew)
      return;

    // Queue MonRegChangedEvent here
    DebugEngine debugEngine = _owningGroup.owningThread().owningProcess().debugEngine();
    int requestCode = debugEngine.getMostRecentReply().getReplyCode();

    debugEngine.getEventManager().addEvent(new MonitoredRegisterChangedEvent(this,
                                                                   this,
                                                                   requestCode),
                                           _eventListeners);
  }

  /**
   * Send a request to change the value of a monitored register. This method
   * can only be used with the registers that are currently being monitored.
   * @param value The string representing the register value the user has
   * provided
   * sendReceiveControlFlags, the flag indicating the state in which the
   * request is to be sent(synchronized, unsynchronized).
   * @return 'true' if the request to modify the value of the monitored
   * register was sent successfully, and 'false' otherwise.
   * @exception java.io.IOException if there is a communication problem
   * with the debug engine, this exception occurs.
   */
  public boolean modifyValue(String value, int sendReceiveControlFlags)
  throws java.io.IOException
  {
    DebugEngine debugEngine = _owningGroup.owningThread().owningProcess().debugEngine();

    if (!debugEngine.prepareForEPDCRequest(EPDC.Remote_RegistersValueSet,
                                           sendReceiveControlFlags))
        return false;

     EReqRegistersValueSet request = new EReqRegistersValueSet(
                                         _epdcRegister.getDU(),
                                         _owningGroup.getID(),
                                         getID(),
                                         value);

     return debugEngine.processEPDCRequest(request, sendReceiveControlFlags);
  }

  int getID()
  {
    return _epdcRegister.getRegisterID();
  }

  /**
   * Returns the monitored register group to which this register belongs.
   * @return a MonitoredRegisterGroup this MonitoredRegister belongs to.
   * @see MonitoredRegisterGroup
   * @see RegisterGroup
   */
  public MonitoredRegisterGroup owningMonitoredRegisterGroup()
  {
    return _owningGroup;
  }

  /**
   * Returns the name of this monitored register.
   * @return the name of this monitored register.
   */
  public String getName()
  {
    return _epdcRegister.getName();
  }

  /**
   * Returns the current value of this monitored register.
   * @return the current value of this monitored register.
   */
  public String getValue()
  {
    return _epdcRegister.getValue();
  }

  void prepareToDie()
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(4, "MonitoredRegister[" + getName() + "].prepareToDie()");

    // Queue MonRegEndedEvent here
    DebugEngine debugEngine = _owningGroup.owningThread().owningProcess().debugEngine();
    int requestCode = debugEngine.getMostRecentReply().getReplyCode();

    debugEngine.getEventManager().addEvent(new MonitoredRegisterEndedEvent(this,
                                                               this,
                                                               requestCode),
                                          _eventListeners);
  }

  /**
   * Print this MonitoredRegister.
   */
  public void print(PrintWriter printWriter)
  {
    if (Model.includePrintMethods)
    {
      printWriter.println("Monitored Register id:" + getID());
      printWriter.print("Register Name: " + getName());
      printWriter.println("; Register Value:  " + getValue());
      super.print(printWriter);
      printWriter.println();
    }
  }

  public void addEventListener(MonitoredRegisterEventListener eventListener)
  {
    _eventListeners.addElement(eventListener);
  }

  public void removeEventListener(MonitoredRegisterEventListener eventListener)
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

	public boolean supportsModifying() {
		return (_epdcRegister.getType() != EPDC.ConstantRegister);
	}


  // data field
  private MonitoredRegisterGroup _owningGroup;
  private ERepGetNextRegister _epdcRegister;
  private Vector _eventListeners = new Vector();
}
