package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/DebuggeeAttachOptions.java, java-model, eclipse-dev, 20011128
// Version 1.8.1.2 (last modified 11/28/01 16:13:35)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

public class DebuggeeAttachOptions extends DebuggeeStartupOptions
{
  /**
   * Use this constructor when a SystemProcess object is not available but
   * a process ID is. For example, when the ID of a process to attach to is
   * passed on the command line when the debugger is invoked, this ctor should
   * be used. When a SystemProcess object <i>is</i> available (i.e. after
   * calling DebugEngine.getSystemProcessList()), client code should use the
   * ctor which takes a SystemProcess object as an argument (see below).
   * @param executeAfterAttach If this argument is 'true', the Model
   * will let the debuggee run after attaching to it.
   */

  public DebuggeeAttachOptions(String debuggeeName,
                               int processID,
			       int saveRestoreFlags,
			       String saveRestoreDirectory,
			       boolean restoreSavedObjects,
			       boolean executeAfterAttach,
                               byte dominantLanguage)
  {
    super(debuggeeName,
          saveRestoreFlags,
          saveRestoreDirectory,
          restoreSavedObjects,
          executeAfterAttach);

    _processID = processID;
    _dominantLanguage    = dominantLanguage;
  }

  /**
   * @deprecated Use the above version instead.
   */

  public DebuggeeAttachOptions(String debuggeeName,
                               int processID,
			       int saveRestoreFlags,
			       String saveRestoreDirectory,
			       boolean restoreSavedObjects)
  {
    super(debuggeeName,
          saveRestoreFlags,
          saveRestoreDirectory,
          restoreSavedObjects,
          false);

    _processID = processID;
  }

  /**
   * Use this constructor when both a process ID and an event handler ID are
   * available. This ctor is intended to be used in support of Debug-On-Demand.
   */

  public DebuggeeAttachOptions(String debuggeeName,
                               int processID,
                               int eventHandlerID,
			       int saveRestoreFlags,
			       String saveRestoreDirectory,
			       boolean restoreSavedObjects,
                               byte dominantLanguage)
  {
    super(debuggeeName,
          saveRestoreFlags,
          saveRestoreDirectory,
          restoreSavedObjects,
          false);

    _processID = processID;
    _eventHandlerID = eventHandlerID;

    _dominantLanguage    = dominantLanguage;
  }

  /**
   * Use this ctor whenever a SystemProcess object is available which
   * represents the process to attach to. SystemProcess objects are created
   * when the debug engine is queried for a list of all processes via the
   * DebugEngine.getSystemProcessList() method.
   */

  public DebuggeeAttachOptions(String debuggeeName,
                               SystemProcess process,
			       int saveRestoreFlags,
			       String saveRestoreDirectory,
			       boolean restoreSavedObjects,
                               byte dominantLanguage)
  {
    super(debuggeeName,
          saveRestoreFlags,
          saveRestoreDirectory,
          restoreSavedObjects,
          false);

    _process = process;
    _dominantLanguage    = dominantLanguage;
  }

  /**
   * Get the process ID.
   * @return The process ID that was used to construct this DebuggeeAttachOptions
   * object, or -1 if this object was constructed from a SystemProcess object
   * instead of a process ID.
   */

  public int getProcessID()
  {
    if (_process == null)
       return _processID;
    else
       return -1; // The SystemProcess object probably contains a process ID
                  // but we don't know in which column it is contained.
  }

  /**
   * Returns the event handler id, or 0 if this DebuggeeAttachOptions object
   * was not constructed using the ctor which takes an event handler id.
   */

  public int getEventHandlerID()
  {
    return _eventHandlerID;
  }

  /**
   * Returns the SystemProcess object that was used to construct this
   * DebuggeeAttachOptions object, or null if the DebuggeeAttachOptions
   * object was not constructed using a SystemProcess object.
   */

  public SystemProcess getProcess()
  {
    return _process;
  }

  public boolean executeAfterAttach()
  {
    return executeAfterStartup();
  }

  public byte getDominantLanguage()
  {
    return _dominantLanguage;
  }

  private int _processID;
  private int _eventHandlerID;
  private SystemProcess _process;
  private byte _dominantLanguage;
}
