package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/ModelStateChangedEvent.java, java-model, eclipse-dev, 20011128
// Version 1.2.1.2 (last modified 11/28/01 16:11:53)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

/**
 * This kind of event is fired whenever the state of the Model changes. There
 * are 4 possible states that the Model can be in:
 * <ol>
 * <li>Idle
 * <li>Debug Engine Is Busy
 * <li>Model Is Being Updated
 * <li>Queued Events Are Being Fired
 * </ol>
 * @see DebugEngineEventListener#modelStateChanged
 */

public class ModelStateChangedEvent extends DebugEngineEvent
{
  ModelStateChangedEvent(Object source,
                        DebugEngine debugEngine,
                        byte oldStateFlags,
                        byte newStateFlags,
                        int requestCode
                       )
  {
    super(source, debugEngine, requestCode);

    _oldStateFlags = oldStateFlags;
    _newStateFlags = newStateFlags;
  }

  /** Get the state that the Model was in prior to this event being fired.
   *  The value returned by this method corresponds to the set of flags in
   *  the DebugEngine class which represent the possible states of the Model.
   * @see DebugEngine#debugEngineIsBusyFlag
   * @see DebugEngine#modelIsBeingUpdatedFlag
   * @see DebugEngine#queuedEventsAreBeingFiredFlag
   * @see DebugEngine#isBusy
   * @see DebugEngine#modelIsBeingUpdated
   * @see DebugEngine#queuedEventsAreBeingFired
   * @see DebugEngine#getState
   */

  public byte getOldStateFlags()
  {
    return _oldStateFlags;
  }

  /** Get the state that the Model is now in. The firing of this event marks
   *  the very beginning of that state.
   *  The value returned by this method corresponds to the set of flags in
   *  the DebugEngine class which represent the possible states of the Model.
   * @see DebugEngine#debugEngineIsBusyFlag
   * @see DebugEngine#modelIsBeingUpdatedFlag
   * @see DebugEngine#queuedEventsAreBeingFiredFlag
   * @see DebugEngine#isBusy
   * @see DebugEngine#modelIsBeingUpdated
   * @see DebugEngine#queuedEventsAreBeingFired
   * @see DebugEngine#getState
   */

  public byte getNewStateFlags()
  {
    return _newStateFlags;
  }

  void fire(ModelEventListener listener)
  {
    ((DebugEngineEventListener)listener).modelStateChanged(this);
  }

  private byte _oldStateFlags;
  private byte _newStateFlags;
}
