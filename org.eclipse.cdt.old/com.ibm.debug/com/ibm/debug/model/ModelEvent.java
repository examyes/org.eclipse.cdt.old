package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/ModelEvent.java, java-model, eclipse-dev, 20011128
// Version 1.6.1.2 (last modified 11/28/01 16:11:27)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

public abstract class ModelEvent extends java.util.EventObject
{
  ModelEvent(Object source, int requestCode)
  {
    super(source);
    _requestCode = requestCode;
  }

  ModelEvent(Object source,
             int requestCode,
             Client client,
             ModelEventListener privilegedListener)
  {
    this(source, requestCode);
    _client = client;
    _privilegedListener = privilegedListener;
  }

  /**
   * Determine what request was made of the debug engine that caused this
   * event to be fired. The value returned by this method corresponds to the
   * set of values for reply/request codes in EPDC. See com.ibm.debug.epdc.EPDC
   * for a list of symbolic constants which define the possible values.
   * For example, a return value of EPDC.Remote_ProcessAttach indicates that
   * this event was fired as a result of asking the debug engine to attach
   * to a running process.
   * N.B.: This method will return -1 if the event is not associated with
   * any particular debug engine request.
   * @see com.ibm.debug.epdc.EPDC#Remote_BreakpointLocation
   */

  public int getRequestCode()
  {
    return _requestCode;
  }

  /**
   * Fire this event on the given event listener. This method must be
   * overridden in each particular event subclass. Each of those overrides
   * will know what kind(s) of event listeners can accept this kind of event
   * and will downcast the ModelEventListener argument to the right subclass.
   * @param listener The listener who is to receive this event.
   */

  abstract void fire(ModelEventListener listener);

  /**
   * Determine which client caused this event to be generated. Will be null
   * if the client is unknown.
   */

  public Client getClient()
  {
    return _client;
  }

  void setClient(Client client)
  {
    _client = client;
  }

  public boolean hasBeenVetoed()
  {
    return _hasBeenVetoed;
  }

  /**
   * Veto this event so that it is not fired on any subsequent event
   * listeners. Note that vetoing an event is only effective when done by
   * the "privileged" event listener. This method will return 'true' if the
   * event was successfully vetoed, otherwise 'false'.
   */

  public boolean veto()
  {
    if (isVetoable())
       return _hasBeenVetoed = true;
    else
       return false;
  }

  public boolean isVetoable()
  {
    return _isVetoable;
  }

  void setIsVetoable(boolean isVetoable)
  {
    _isVetoable = isVetoable;
  }

  ModelEventListener getPrivilegedListener()
  {
    return _privilegedListener;
  }

  /*
   * Add an arbitrary "property" to this event.
   */
  public void setRequestProperty(Object property)
  {
    _requestProperty = property;
  }

  /*
   * Returns the value of the property.
   */
  public Object getRequestProperty()
  {
    return _requestProperty;
  }

  private int _requestCode;
  private Client _client;
  private ModelEventListener _privilegedListener;
  private boolean _hasBeenVetoed = false;
  private boolean _isVetoable = false;
  private Object _requestProperty;
}
