package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/AutoSave.java, java-model, eclipse-dev, 20011128
// Version 1.2.1.2 (last modified 11/28/01 16:13:35)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

/**
 * Use this class to automatically save an object graph each time a given
 * debug engine fires the "model updates completed" event. When first
 * constructed, the object will register itself as a listener on the
 * given debug engine and thereafter will get notified whenever the Model
 * updates are completed. Use the suspend() method to tell the AutoSave
 * object to un-register itself as a listener so that the graph of objects
 * will not automatically be saved, and use the resume() method to tell the
 * AutoSave object to re-register itself as a listener so that the graph
 * of objects will once again be saved automatically.
 */

class AutoSave extends DebugEngineEventAdapter
{
  AutoSave(Serialization serialization, Object object, DebugEngine engine)
  {
    _serialization = serialization;
    _object = object;
    (_engine = engine).addEventListener(this);
  }

  public void modelStateChanged(ModelStateChangedEvent event)
  {
    // If the Model was being updated but the updates are now complete,
    // save the object graph:

    try
    {
      if (((event.getOldStateFlags() & DebugEngine.modelIsBeingUpdatedFlag) != 0) &&
          ((event.getNewStateFlags() & DebugEngine.modelIsBeingUpdatedFlag) == 0))
         _serialization.saveGraph(_object);
    }
    catch (java.io.IOException excp)
    {
    }
  }

  void suspend()
  {
    if (!_savingSuspended)
    {
       _savingSuspended = true;
       _engine.removeEventListener(this);
    }
  }

  boolean isSuspended()
  {
    return _savingSuspended;
  }

  void resume()
  {
    if (_savingSuspended)
    {
       _savingSuspended = false;
       _engine.addEventListener(this);
    }
  }

  /**
   * Change the object being saved.
   */

  void setObject(Object object)
  {
    _object = object;
  }

  private Serialization _serialization;
  private Object _object;
  private DebugEngine _engine;
  private boolean _savingSuspended = false;
}
