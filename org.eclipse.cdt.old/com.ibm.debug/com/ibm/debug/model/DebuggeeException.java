package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/DebuggeeException.java, java-model, eclipse-dev, 20011128
// Version 1.10.1.2 (last modified 11/28/01 16:13:19)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.epdc.*;
import java.io.*;

/**
 * Class representing any possible exception supported by a given debug engine.
 */
public class DebuggeeException extends DebugModelObject
{
  DebuggeeException(ERepGetExceptions excp, DebugEngine engine)
  {
    if (Model.TRACE.EVT && Model.traceInfo())
       Model.TRACE.evt(4, "Creating DebuggeeException : Name=<" + excp.exceptionName() + ">");

    _name = excp.exceptionName();

    // When an exception is first created, the pending state, default state,
    // and current state are all the same:

    _pendingState = _defaultState = _currentState = excp.exceptionStatus();
    _debugEngine = engine;
  }

  /**
   * Returns the name of the exception
   */
  public String name()
  {
    return _name.string();
  }

  /**
   * Get the default state of this exception. The default state of an exception
   * is determined by the debug engine.
   * @return true if the default state of this exception is
   * enabled,
   * and false if the default state is disabled
   */

  public boolean defaultStateIsEnabled()
  {
    return _defaultState == EPDC.EXCEPTION_ENABLED;
  }

  /**
   * Get the pending state of this exception. The pending state of the exception
   * is set using the enable() and disable() methods and represents the state
   * that this exception will have <i>after</i> a successful call to
   * DebugEngine.changeExceptionStatus() i.e. after calling
   * DebugEngine.changeExceptionStatus(), the pending
   * state will become the current state.
   * @return true if the pending state of this exception is
   * enabled,
   * and false if the pending state is disabled
   * @see DebuggeeException#isEnabled
   */

  public boolean pendingStateIsEnabled()
  {
    return _pendingState == EPDC.EXCEPTION_ENABLED;
  }

  /**
   * Get the current state of this exception. The current state of the
   * exception is what determines whether or not the debug engine will
   * notify the FE when an exception occurs i.e. it is the state of the
   * exception from the debug engine's point of view. The current state
   * of an exception is modified by calling DebugEngine.changeExceptionStatus().
   * @return true if the current state of this exception is "enabled",
   * otherwise false.
   * @see DebuggeeException#pendingStateIsEnabled
   */

  public boolean isEnabled()
  {
    return _currentState == EPDC.EXCEPTION_ENABLED;
  }

  /**
   * Returns the debug engine object to which this exception belongs
   */
  public DebugEngine debugEngine()
  {
    return _debugEngine;
  }

  /**
   * Call this method to change the <i>pending</i> state of the exception
   * to "enabled". The actual state of the exception (i.e. the <i>current</i>
   * state) will not be changed until after a successful call to
   * DebugEngine.changeExceptionStatus().
   */
  public void enable()
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "DebuggeeException[" + this.name() + "]" + ".enable()");

    _pendingState = EPDC.EXCEPTION_ENABLED;
  }

  /**
   * Call this method to change the <i>pending</i> state of the exception
   * to "disabled". The actual state of the exception (i.e. the <i>current</i>
   * state) will not be changed until after a successful call to
   * DebugEngine.changeExceptionStatus().
   */
  public void disable()
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "DebuggeeException[" + this.name() + "]" + ".disable()");

    _pendingState = EPDC.EXCEPTION_DISABLED;
  }

  int getPendingState()
  {
    return _pendingState;
  }

  void commitPendingStateChange()
  {
    _currentState = _pendingState;
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
          stream.writeInt(_currentState);
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
          _currentState = stream.readInt();
    }
    else
       stream.defaultReadObject();
  }

  public void print(PrintWriter printWriter)
  {
    printWriter.print("Name: " + name());
    printWriter.println(";  State: " + (isEnabled() ? "enabled" : "disabled"));
  }

  private EStdString _name;
  private DebugEngine _debugEngine;
  private int _currentState;
  private int _pendingState;
  private int _defaultState;
}

