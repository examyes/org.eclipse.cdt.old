package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/DebuggeeProcess.java, java-model, eclipse-dev, 20011128
// Version 1.133.1.2 (last modified 11/28/01 16:10:57)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.epdc.*;
import com.ibm.debug.epdc.EBPList;
import java.util.Vector;
import java.util.Hashtable;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

/**
 * This class represents a process that is being debugged. Every DebuggeeProcess
 * object is "owned" by a DebugEngine object. DebuggeeProcess objects are
 * created as a result
 * of that debug engine loading or attaching to the process for the purpose
 * of debugging it. Note that there is currently a limitation of 1 process
 * per debug engine - debugging multiple processes requires starting multiple
 * debug engines.
 * <p>Listeners can be registered with a DebugEngine object to
 * get notified when a process has been created or attached to. The listener
 * will get passed the DebuggeeProcess object for the new process.
 * See, for example, DebugEngine.prepareProgram(String programName),
 * which loads a program, and DebugEngineEventListener.processAdded(ProcessAddedEvent),
 * which notifies a listener that the process was created.
 * <p>A DebuggeeProcess object can contain several other kinds of objects,
 * including:
 * <ul>
 * <li>Thread objects. These represent threads of execution running within
 * the process.
 * <li>Module objects. These represent modules (.exes and .dlls) that have
 * been loaded into the process.
 * <li>Objects representing monitored storage.
 * <li>Objects representing monitored expressions.
 * </ul>
 * Event listeners can be registered with a DebuggeeProcess object in order
 * to get notified when the above kinds of objects are created and
 * added to the process.
 * <p>
 * Note: This class has been named DebuggeeProcess instead of just Process
 * mostly to avoid name collision with java.lang.Process.
 * @see DebuggeeProcessEventListener
 */

public class DebuggeeProcess extends DebugModelObject
{
  DebuggeeProcess(DebugEngine debugEngine,
                  int processID,
                  String qualifiedName,
                  String profileName)
  {
    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(1, "Creating DebuggeeProcess : ProcessID=" + processID + " QualifiedName=" + qualifiedName);

    _processID = processID;
    _qualifiedName = qualifiedName;
    _profileName = profileName;
    _debugEngine = debugEngine;
  }

  /**
   * Add a process event listener to this DebuggeeProcess object. Whenever
   * an event occurs for which there is a corresponding method in the
   * event listener's interface, that method will be called to inform the
   * listener of the event. More than one listener may be added to a given
   * DebuggeeProcess object - the listeners will be notified of events in
   * the order in which they were added (i.e. FIFO).
   * @param eventListener The object whose methods will be called when
   * events occur.
   */

  synchronized public void addEventListener(DebuggeeProcessEventListener eventListener)
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(2, "DebuggeeProcess[" + this.processID() + "]" + ".addEventListener(" + eventListener + ")");

    _eventListeners.addElement(eventListener);
  }

  /**
   * Remove a process event listener from this DebuggeeProcess object so
   * that it no longer receives event notifications.
   * @param eventListener The event listener to be removed from this
   * DebuggeeProcess object's list of listeners.
   */

  synchronized public void removeEventListener(DebuggeeProcessEventListener eventListener)
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(2, "DebuggeeProcess[" + this.processID() + "]" + ".removeEventListener(" + eventListener + ")");

     int index = _eventListeners.indexOf(eventListener);

     if (index != -1)
        try
        {
          _eventListeners.setElementAt(null, index);
        }
        catch(ArrayIndexOutOfBoundsException excp)
        {
        }
  }

  /**
   * Returns the operating system's process ID for this process.
   */

  public int processID()
  {
    return _processID;
  }

  /**
   * Returns the qualified name of the program executing in this process.
   */

  public String qualifiedName()
  {
    return _qualifiedName;
  }

  /**
   * Returns the debug engine object which "owns" this process.
   */

  public DebugEngine debugEngine()
  {
    return _debugEngine;
  }

  /** Return the entire list of modules in this process. No guarantee that
   *  the array won't contain some null entries.
   */

  public Module[] getModulesArray()
  {
    if (_modules == null)
       return null;

    int numberOfModules = _modules.size();

    if (numberOfModules == 0)
       return null;

    Module[] modules = new Module[numberOfModules];

    _modules.copyInto(modules);

    return modules;
  }

  /**
   * This method will retrieve all Module objects in the Model with the given
   * name. Only those modules that are contained in the Model will be
   * considered - it will <i>not</i> ask the debug engine for the Modules,
   * nor will it search the file system for the modules, etc.
   * The name used to look up a Module should be in the same format as that
   * returned by the Module.name method, since it is this name that is used
   * to store the Modules in a hashtable. The exact format of this name is
   * determined by the debug engine. On Intel-based systems (Windows and OS/2),
   * the name of a module typically has the extension ".exe" or ".dll".
   * @return A Vector of Module objects representing the modules with the
   * given name. If there are no modules in the Model with the given name,
   * null will be returned.
   */

  public Vector getModules(String name)
  {
    return (Vector)_modulesByName.get(name);
  }

  /** Return the entire list of breakpoints in this process. No guarantee that
   *  the array won't contain some null entries.
   */

  public Breakpoint[] getBreakpointsArray()
  {
    if (_breakpoints == null)
       return null;

    int numberOfBreakpoints = _breakpoints.size();

    if (numberOfBreakpoints == 0)
       return null;

    Breakpoint[] breakpoints = new Breakpoint[numberOfBreakpoints];

    _breakpoints.copyInto(breakpoints);

    return breakpoints;
  }

  /**
   * Halt a running debuggee.
   * @param sendReceiveControlFlags A set of flags which specify the mode
   * in which this request is to be performed. There is a set of constants
   * in DebugEngine which define the possible values for this argument.
   * For example, a value of DebugEngine.sendReceiveDefault means that the
   * request is to be done asynchronously, while a value of
   * DebugEngine.sendReceiveSynchronously means that the request is to be
   * performed synchronously.
   * <p>When done asynchronously, this method will return immediately after
   * sending the request to the debug engine without waiting for a response
   * from the debug engine. The response to the request will be
   * received on a separate thread and client code will be notified of the
   * the response via the event listener mechanism.
   * @return 'true' if the halt request was successfully sent to
   * the debug engine, 'false' otherwise.
   * @exception java.io.IOException If there is a problem communicating
   * with the debug engine.
   */
  public boolean halt(int sendReceiveControlFlags)
  throws java.io.IOException
  {
      return halt(sendReceiveControlFlags, null);
  }

  public boolean halt(int sendReceiveControlFlags, Object property)
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(2, "DebuggeeProcess[" + this.processID() + "]" +
                         ".halt()");

    final int requestCode = EPDC.Remote_Halt;

    if (!_debugEngine.prepareForEPDCRequest(requestCode,
                                            sendReceiveControlFlags))
        return false;

    if (Model.TRACE.EVT && Model.traceInfo())
        Model.TRACE.evt(1, "Sending EPDC request: Remote_Halt");

    return _debugEngine.processEPDCRequest(new EReqRemoteHalt(),
                                           sendReceiveControlFlags,
                                           property);
  }

  /** Return the entire list of threads in this process. No guarantee that
   *  the array won't contain some null entries.
   */

  public DebuggeeThread[] getThreadsArray()
  {
    if (_threads == null)
       return null;

    int numberOfThreads = _threads.size();

    if (numberOfThreads == 0)
       return null;

    DebuggeeThread[] threads = new DebuggeeThread[numberOfThreads];

    _threads.copyInto(threads);

    return threads;
  }

  /**
   * Return the entire list of monitored expressions
   */

  public MonitoredExpression[] getMonitoredExpressionsArray()
  {
    if (_monitoredExpressions == null)
        return null;

    int size = _monitoredExpressions.size();
    if (size == 0)
        return null;

    MonitoredExpression[] monitoredExpressions = new MonitoredExpression[size];

    _monitoredExpressions.copyInto(monitoredExpressions);

    return monitoredExpressions;
  }

  /**
   * Returns a Vector of Storage objects representing the storage within the
   * debuggee's address space which is currently being monitored by the
   * debug engine. Note that "private" storage monitors will not be returned.
   * @see Storage
   * @see DebuggeeProcess#monitorStorage
   */

  public Vector getStorage()
  {
    if (_storage == null || _storage.size() == 0)
       return null;

    Vector storage = new Vector();
    int storageSize = _storage.size();

    for (int i = 0; i < storageSize; i++)
    {
        Storage storageElement = (Storage)_storage.elementAt(i);

        if (storageElement != null && !storageElement.isPrivate())
           storage.addElement(storageElement);
    }

    return storage;
  }

  /**
   * Returns the thread with the given id. Will return null if there is
   * no thread with that id number.
   *
   * @param debugEngineAssignedThreadID The id number of the requested
   * thread. This is the the id number that was assigned to the thread by
   * the debug engine, not the operating system's thread id.
   */

  public DebuggeeThread thread(int debugEngineAssignedThreadID)
  {
    try
    {
      return (DebuggeeThread)_threads.elementAt(debugEngineAssignedThreadID);
    }
    catch (ArrayIndexOutOfBoundsException excp)
    {
      return null;
    }
  }

  /**
   * Run this process to the program's main entry point. Exactly what the
   * main entry point is is language-dependent. For example, if the
   * debuggee is written in C or C++, it will be the function 'main'.
   * <p>Note that this is usually the next method called after
   * DebugEngine.prepareProgram if the user has indicated that he/she
   * does not want to debug initialization code (e.g. static constructors
   * in C++). In this case, the user's debug session will start at the
   * main entry point to the program instead of at the start of the
   * compiler-generated initialization code.
   * <p>Calling this method on a process that has already successfully been
   * run to main by a previous call to this method is a no-op; the method
   * will simply return false.
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
   *  @exception java.io.IOException If there is a problem communicating
   *  with the debug engine.
   *
   */

  synchronized public boolean runToMainEntryPoint(int sendReceiveControlFlags)
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(1, "DebuggeeProcess[" + this.processID() + "]" + ".runToMainEntryPoint(" + sendReceiveControlFlags + ")");

    final int requestCode = EPDC.Remote_StartPgm;

    if (!_debugEngine.prepareForEPDCRequest(requestCode, sendReceiveControlFlags))
       return false;

    if (_hasBeenRunToMainEntryPoint)
    {
       _debugEngine.cancelEPDCRequest(requestCode);
       return false;
    }

    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(1, "Sending EPDC request: Remote_StartPgm");

    return _debugEngine.processEPDCRequest(new EReqStartPgm(), sendReceiveControlFlags);
  }

  void setHasBeenRunToMainEntryPoint()
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(4, "DebuggeeProcess[" + this.processID() + "]" + ".setHasBeenRunToMainEntryPoint()");

    _hasBeenRunToMainEntryPoint = true;
  }

  /**
   * Query whether or not this process has been run to its main entry point
   * via the runToMainEntryPoint method.
   * @see DebuggeeProcess#runToMainEntryPoint
   */

  public boolean hasBeenRunToMainEntryPoint()
  {
    return _hasBeenRunToMainEntryPoint;
  }

  /**
   * Force the current process to run to its <code>main</code> entry point,
   * regardless of whether this has been done before.  This is the preferred
   * way to advance to <code>main</code> after the debugger has been stopped
   * by execution of an <code>exec()</code> family call, since an arbitrary
   * number of such calls may take place during the lifetime of a process.
   * @see DebuggeeProcess#runToMainEntryPoint
   */
  public boolean forceRunToMainEntryPoint(int sendReceiveControlFlags)
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(1, "DebuggeeProcess[" + this.processID() + "]" + ".forceRunToMainEntryPoint(" + sendReceiveControlFlags + ")");

    _hasBeenRunToMainEntryPoint = false;

    return runToMainEntryPoint(sendReceiveControlFlags);
  }

  synchronized void add(Module module)
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(2, "DebuggeeProcess[" + this.processID() + "]" + ".add(Module<" + module.name() + ">)");

    // Note: EPDC appears to assign module ids starting at 1, not 0.

    setVectorElementToObject(module, _modules, module.id());

    addObjectToHashtable(module, _modulesByName, module.name());

    int requestCode = _debugEngine.getMostRecentReply().getReplyCode();

    //if (Model.TRACE.EVT && Model.traceInfo())
      //Model.TRACE.evt(3, "Adding ModuleAddedEvent");

    _debugEngine.getEventManager().addEvent(new ModuleAddedEvent(this,
                 module,
                 requestCode
                ),
              _eventListeners
             );
  }

  synchronized void add(Breakpoint breakpoint)
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "DebuggeeProcess[" + this.processID() + "]" + ".add(Breakpoint<" + breakpoint.id() + ">)");

    setVectorElementToObject(breakpoint, _breakpoints, breakpoint.id());

    int requestCode = _debugEngine.getMostRecentReply().getReplyCode();

    //if (Model.TRACE.EVT && Model.traceInfo())
      //Model.TRACE.evt(3, "Adding BreakpointAddedEvent");

    _debugEngine.getEventManager().addEvent(new BreakpointAddedEvent(this,
                 breakpoint,
                 requestCode
                ),
              _eventListeners
             );
  }

  // TODO: Remove parts!

  synchronized void add(Part part)
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "DebuggeeProcess[" + this.processID() + "]" + ".add(Part<" + part.name() + ">");

    // Note: EPDC appears to assign part ids starting at 1, not 0.

    setVectorElementToObject(part, _parts, part.id());
  }

  synchronized void add(DebuggeeThread debuggeeThread)
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(2, "DebuggeeProcess[" + this.processID() + "]" + ".add(DebuggeeThread<" + debuggeeThread.debugEngineAssignedID() + ">)");

    setVectorElementToObject(debuggeeThread, _threads, debuggeeThread.debugEngineAssignedID());

    int requestCode = _debugEngine.getMostRecentReply().getReplyCode();

    _debugEngine.getEventManager().addEvent(new ThreadAddedEvent(this,
                 debuggeeThread,
                 requestCode
                ),
              _eventListeners
             );
  }

  synchronized void add(MonitoredExpression monitor)
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "DebuggeeProcess[" + this.processID() + "]" + ".add(MonitoredExpression<" + monitor.getMonitoredExpressionAssignedID() + ">)");

    setVectorElementToObject(monitor, _monitoredExpressions, monitor.getMonitoredExpressionAssignedID());

    int requestCode = _debugEngine.getMostRecentReply().getReplyCode();

    _debugEngine.getEventManager().addEvent(new
           MonitoredExpressionAddedEvent(this,
                 monitor,
                 requestCode),
           _eventListeners);
  }

  synchronized void add(Storage storage)
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "DebuggeeProcess[" + this.processID() + "]" + ".add(Storage<" + storage.id() + ">)");

    if (_storage == null)
       _storage = new Vector();

    setVectorElementToObject(storage, _storage, storage.id());

    int requestCode = _debugEngine.getMostRecentReply().getReplyCode();

    // The StorageAddedEvent is a "vetoable" event. If a privileged event
    // listener was passed in on the call to monitorStorage, it will have
    // been saved in the DebugEngine and we now want to put that event
    // listener in the event. When the event is fired, the privileged
    // event listener will be given the opportunity to veto the event so
    // that it does not get fired on the listeners in the normal vector of
    // event listeners.

    _debugEngine.getEventManager().
                 addEvent(new StorageAddedEvent(this,
                                                storage,
                                                requestCode,
                                                _debugEngine.getCurrentClient(),
                                                _debugEngine.getPrivilegedEventListener()),
                                            _eventListeners);
  }

  synchronized void remove(DebuggeeThread debuggeeThread)
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(2, "DebuggeeProcess[" + this.processID() + "]" + ".remove(DebuggeeThread<" + debuggeeThread.debugEngineAssignedID() + ">)");

    int debugEngineAssignedID = debuggeeThread.debugEngineAssignedID();

    debuggeeThread.prepareToDie();
    debuggeeThread.setHasBeenDeleted();

    try
    {
      _threads.setElementAt(null, debugEngineAssignedID);
    }
    catch (ArrayIndexOutOfBoundsException excp)
    {
    }
  }

  synchronized void remove(Storage storage)
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "DebuggeeProcess[" + this.processID() + "]" + ".remove(Storage<" + storage.id() + ">)");

    storage.prepareToDie();
    storage.setHasBeenDeleted();

    try
    {
      _storage.setElementAt(null, storage.id());
    }
    catch (ArrayIndexOutOfBoundsException excp)
    {
    }
  }

  synchronized void removeModule(int moduleID)
  {
    if (Model.TRACE.DBG && Model.traceInfo())
       Model.TRACE.dbg(2, "DebuggeeProcess[" + this.processID() + "]" + ".removeModule(" + moduleID + ")");

    Module module = getModule(moduleID);

    if (module == null)
       return;

    module.prepareToDie();
    module.setHasBeenDeleted();

    removeObjectFromHashtable(module, _modulesByName, module.name());

    try
    {
      _modules.setElementAt(null, moduleID);
    }
    catch (ArrayIndexOutOfBoundsException excp)
    {
    }
  }

  synchronized void removeBreakpoint(int breakpointID)
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "DebuggeeProcess[" + this.processID() + "]" + ".removeBreakpoint(" + breakpointID + ")");

    Breakpoint breakpoint = getBreakpoint(breakpointID);

    breakpoint.prepareToDie();
    breakpoint.setHasBeenDeleted();

    try
    {
      _breakpoints.setElementAt(null, breakpointID);
    }
    catch (ArrayIndexOutOfBoundsException excp)
    {
    }
  }
  /**
   * Remove the monitored expression from the array of monitored expressions.
   * Fire the event to indicate the removal of the monitored expression next.
   */

  synchronized void removeMonitoredExpression(int id)
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "DebuggeeProcess[" + this.processID() + "]" + ".removeMonitoredExpression(" + id + ")");

     MonitoredExpression expr = getMonitoredExpression(id);

     expr.prepareToDie();
     expr.setHasBeenDeleted();

     try
     {
        _monitoredExpressions.setElementAt(null, id);
     }
     catch (ArrayIndexOutOfBoundsException excp)
     {
     }
  }

  void prepareToDie()
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "DebuggeeProcess[" + this.processID() + "]" + ".prepareToDie()");

    setHasBeenDeleted();

    int requestCode = _debugEngine.getMostRecentReply().getReplyCode();

    _debugEngine.getEventManager().addEvent(new ProcessEndedEvent(this,
                                                                 this,
                                                                 requestCode
                                                                ),
                                            _eventListeners
                                           );

    if (!_doingARestart && !_debugEngine.canBeReused())
       addEventListener(new ProcessEndedEngineTerminator());
  }

  Module getModule(int id)
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(4, "DebuggeeProcess[" + this.processID() + "]" + ".getModule(" + id + ")");

    try
    {
      return (Module)(_modules.elementAt(id));
    }
    catch (ArrayIndexOutOfBoundsException excp)
    {
      if (Model.TRACE.ERR)
        Model.TRACE.err(3, "Exception{" + excp.getMessage() + "} occurred in DebuggeeProcess[" + this.processID() + "]" + ".getModule(" + id + ")");

      return null;
    }
  }

  Breakpoint getBreakpoint(int id)
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(4, "DebuggeeProcess[" + this.processID() + "]" + ".getBreakpoint(" + id + ")");

    try
    {
      return (Breakpoint)(_breakpoints.elementAt(id));
    }
    catch (ArrayIndexOutOfBoundsException excp)
    {
      if (Model.TRACE.ERR)
        Model.TRACE.err(3, "Exception{" + excp.getMessage() + "} occurred in DebuggeeProcess[" + this.processID() + "]" + ".getBreakpoint(" + id + ")");

      return null;
    }
  }

  Storage getStorage(short id)
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(4, "DebuggeeProcess[" + this.processID() + "]" + ".getStorage(" + id + ")");

    try
    {
      return (Storage)(_storage.elementAt(id));
    }
    catch (ArrayIndexOutOfBoundsException excp)
    {
      if (Model.TRACE.ERR)
        Model.TRACE.err(3, "Exception{" + excp.getMessage() + "} occurred in DebuggeeProcess[" + this.processID() + "]" + ".getStorage(" + id + ")");

      return null;
    }
  }

  Part getPart(int id)
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(4, "DebuggeeProcess[" + this.processID() + "]" + ".getPart(" + id + ")");

    try
    {
      return (Part)(_parts.elementAt(id));
    }
    catch (ArrayIndexOutOfBoundsException excp)
    {
      if (Model.TRACE.ERR)
        Model.TRACE.err(3, "Exception{" + excp.getMessage() + "} occurred in DebuggeeProcess[" + this.processID() + "]" + ".getPart(" + id + ")");

      return null;
    }
  }

  DebuggeeThread getThread(int debugEngineAssignedID)
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(4, "DebuggeeProcess[" + this.processID() + "]" + ".getThread(" + debugEngineAssignedID + ")");

    try
    {
      return (DebuggeeThread)(_threads.elementAt(debugEngineAssignedID));
    }
    catch (ArrayIndexOutOfBoundsException excp)
    {
      if (Model.TRACE.ERR)
        Model.TRACE.err(3, "Exception{" + excp.getMessage() + "} occurred in DebuggeeProcess[" + this.processID() + "]" + ".getThread(" + debugEngineAssignedID + ")");

      return null;
    }
  }

  MonitoredExpression getMonitoredExpression(int id)
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(4, "DebuggeeProcess[" + this.processID() + "]" + ".getMonitoredExpression(" + id + ")");

    try
    {
       return (MonitoredExpression)(_monitoredExpressions.elementAt(id));
    }
    catch (ArrayIndexOutOfBoundsException excp)
    {
      if (Model.TRACE.ERR)
        Model.TRACE.err(3, "Exception{" + excp.getMessage() + "} occurred in DebuggeeProcess[" + this.processID() + "]" + ".getMonitoredExpression(" + id + ")");

      return null;
    }
  }

  void hasStopped(short reason, int threadID, String exceptionMsg, Vector breakidList, int requestCode)
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(2, "DebuggeeProcess[" + this.processID() + "]" + ".hasStopped() : Reason=" + reason + " ThreadID=" + threadID + " ExceptionMsg=" + exceptionMsg + " RequestCode=" + requestCode);

    DebuggeeThread thread = null;

    if (threadID == 0)
    {
       int numberOfThreads = (_threads == null ? 0 : _threads.size());

       for (int i = 0; i < numberOfThreads; i++)
           if ((thread = (DebuggeeThread)_threads.elementAt(i)) != null)
              break;
    }
    else
       thread = getThread(threadID);

    Breakpoint[] breakpoints = null;
    if (breakidList != null && breakidList.size() > 0)
    {
       breakpoints = new Breakpoint[breakidList.size()];
       for(int i = 0; i < breakidList.size(); i++)
       {
          int id = ((EBPList)breakidList.elementAt(i)).getBreakid();
          breakpoints[i] = getBreakpoint(id);
       }
    }


    _processStopInfo = new ProcessStopInfo(this, reason, thread, exceptionMsg, breakpoints);

    if (reason == EPDC.Why_done ||
	reason == EPDC.Why_DoneNoStop ||
	reason == EPDC.Why_ProcessDone ||
	reason == EPDC.Why_DoneCloseDebugger)
    {
       // The following will trigger a ProcessEndedEvent to be fired:

       _debugEngine.remove(this);

       // If we received a reason code of Why_DoneCloseDebugger this means
       // that the debuggee ran to completion AND the engine wants us to send
       // it a terminate request. We will do this by creating a listener for
       // the ProcessEndedEvent. That listener will send the terminate request
       // to the engine. (Note: AFAIK, this reason code is only used by the
       // S/390 debug engine.)

       // Having the _debugEngine.canBeReused() condition in here looks odd
       // but it prevents us from adding more than one ProcessEndedEngineTerminator
       // to the process, since one will also be added in prepareToDie().

       if (reason == EPDC.Why_DoneCloseDebugger && _debugEngine.canBeReused())
	  addEventListener(new ProcessEndedEngineTerminator());
    }
    else
    {
       _debugEngine.getEventManager().addEvent(new ProcessStoppedEvent(this,
								  _processStopInfo,
								  requestCode
								 ),
					  _eventListeners
					 );

       if (reason == EPDC.Why_PgmExcept ||
	   reason == EPDC.Why_PgmExcept_Nohandler ||
	   reason == EPDC.Why_PgmExcept_NoRetry ||
	   reason == EPDC.Why_PgmExcept_OnlyRun)
       {

	  // The following will toggle the control flag DebuggeeThread.isExceptionRaised:
	  thread.exceptionRaised();

	  // Queue ExceptionRaisedEvent here
	  _debugEngine.getEventManager().addEvent(new ExceptionRaisedEvent(this,
									   _processStopInfo,
									   exceptionMsg,
									   requestCode
									  ),
						 _eventListeners
						);
       }
    }
  }

  /**
   * Get information regarding which thread most recently caused the
   * process to stop running and why. Will return null if the process has
   * been created but not yet started and stopped at least once.
   */

  public ProcessStopInfo getProcessStopInfo()
  {
    return _processStopInfo;
  }

  /**
   * Let the process continue executing.
   * The reply will be received
   * only AFTER the debuggee has stopped running (because, for example, it
   * hit a breakpoint, ran to completion, etc.).
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

  public boolean run(int sendReceiveControlFlags)
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(1, "DebuggeeProcess[" + this.processID() + "]" + ".run(" + sendReceiveControlFlags + ")");

    if (!_debugEngine.prepareForEPDCRequest(EPDC.Remote_Execute, sendReceiveControlFlags))
       return false;

    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(2, "Sending EPDC request: Remote_Execute");

    return _debugEngine.processEPDCRequest(new EReqExecuteGo(),
                                           sendReceiveControlFlags
                                          );
  }

  /**
   * Tell the debug engine to delete all breakpoints in the process.
   * @param sendReceiveControlFlags A set of flags which specify the mode
   * in which this request is to be performed. There is a set of constants
   * in DebugEngine which define the possible values for this argument.
   * For example, a value of DebugEngine.sendReceiveDefault means that the
   * request is to be done asynchronously, while a value of
   * DebugEngine.sendReceiveSynchronously
   * means that the request is to be performed synchronously.
   * <p>When done asynchronously,
   * this method will return immediately after sending the request to the
   * debug engine without waiting for a response from
   * the debug engine. The response to the request will be
   * received on a separate thread and client code will be notified of the
   * the response via the event listener mechanism.
   * @return 'true' if the breakpoint delete request was successfully sent to
   * the debug engine, 'false' otherwise. Note that a return value of 'true'
   * does not imply that the debug engine was able to delete the breakpoints but
   * rather simply that the request was successfully sent to the debug engine.
   * Whether or not the breakpoints were actually deleted by the debug engine will
   * be indicated via the event listener mechanism.
   *  @exception java.io.IOException If there is a problem communicating
   *  with the debug engine.
   */

  public boolean removeAllBreakpoints(int sendReceiveControlFlags)
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(2, "DebuggeeProcess[" + this.processID() + "]" + ".removeAllBreakpoints(" + sendReceiveControlFlags + ")");

    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(2, "Sending EPDC request: Remote_BreakpointClear");

    if (_debugEngine.prepareForEPDCRequest(EPDC.Remote_BreakpointClear,
                                           sendReceiveControlFlags) &&
        _debugEngine.processEPDCRequest(new EReqBreakpointClear(0),
                                           sendReceiveControlFlags))
       return true;
    else
       return false;
  }

  /**
   * Terminate the process being debugged.
   * @param sendReceiveControlFlags A set of flags which specify the mode
   * in which this request is to be performed. There is a set of constants
   * in DebugEngine which define the possible values for this argument.
   * For example, a value of DebugEngine.sendReceiveDefault means that the
   * request is to be done asynchronously, while a value of
   * DebugEngine.sendReceiveSynchronously
   * means that the request is to be performed synchronously.
   * <p>When done asynchronously,
   * this method will return immediately after sending the request to the
   * debug engine without waiting for a response from
   * the debug engine. The response to the request will be
   * received on a separate thread and client code will be notified of the
   * the response via the event listener mechanism.
   * @return 'true' if the terminate request was successfully sent to
   * the debug engine, 'false' otherwise. Note that a return value of 'true'
   * does not imply that the process was successfully terminated but
   * rather simply that the request was successfully sent to the debug engine.
   * Whether or not the process was actually terminated will
   * be indicated via the event listener mechanism.
   *  @exception java.io.IOException If there is a problem communicating
   *  with the debug engine.
   */

   public boolean terminate(int sendReceiveControlFlags)
   throws java.io.IOException
   {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(1, "DebuggeeProcess[" + this.processID() + "]" + ".terminate(" + sendReceiveControlFlags + ")");

     final int requestCode = EPDC.Remote_TerminatePgm;

     // TODO: If we can't send the request we need to determine why we
     // can't - there may be some situations in which we can't send a
     // terminate request to the DE but we still want to remove this object
     // from the Model and fire a "terminated" event. For example, if for
     // some reason the connection to the DE is lost, we won't be able to
     // send a terminate request to it but we still want to remove the
     // DebuggeeProcess from the Model.

     if (!_debugEngine.prepareForEPDCRequest(requestCode, sendReceiveControlFlags))
        return false;

     if (Model.TRACE.EVT && Model.traceInfo())
       Model.TRACE.evt(1, "Sending EPDC request: Remote_TerminatePgm");

     return _debugEngine.processEPDCRequest(new EReqTerminatePgm(), sendReceiveControlFlags);
   }

  /**
   * Detach from the process being debugged. Note that a process can only
   * be detached from if it was originally attached to i.e. was brought
   * under debug control via the DebugEngine.attach method instead of the
   * DebugEngine.prepareProgram method. Note also that not all debug engines
   * are required to support detaching from a process - client code should
   * check this capability by calling EngineFileCapabilities.processDetachSupported
   * @param detachAction An argument which tells the debug engine what to do
   * with the debuggee process upon detaching from it. There are 3
   * possibilities: Release, Keep, and Kill. These possibilities have
   * corresponding constants in class EPDC (see below).
   * <p>Note that not all engines will necessarily support all 3 ways of
   * detaching - client code should call methods in class EngineFileCapabilities
   * to determine what the possibilities are (see below).
   * @param sendReceiveControlFlags A set of flags which specify the mode
   * in which this request is to be performed. There is a set of constants
   * in DebugEngine which define the possible values for this argument.
   * For example, a value of DebugEngine.sendReceiveDefault means that the
   * request is to be done asynchronously, while a value of
   * DebugEngine.sendReceiveSynchronously
   * means that the request is to be performed synchronously.
   * <p>When done asynchronously,
   * this method will return immediately after sending the request to the
   * debug engine without waiting for a response from
   * the debug engine. The response to the request will be
   * received on a separate thread and client code will be notified of the
   * the response via the event listener mechanism.
   * @return 'true' if the detach request was successfully sent to
   * the debug engine, 'false' otherwise. Note that a return value of 'true'
   * does not imply that the process was successfully detached but
   * rather simply that the request was successfully sent to the debug engine.
   * Whether or not the process was actually detached will
   * be indicated via the event listener mechanism.
   *  @exception java.io.IOException If there is a problem communicating
   *  with the debug engine.
   * @see com.ibm.debug.epdc.EPDC#ProcessRelease
   * @see com.ibm.debug.epdc.EPDC#ProcessKeep
   * @see com.ibm.debug.epdc.EPDC#ProcessKill
   * @see DebugEngine#attach
   * @see EngineFileCapabilities#processDetachSupported
   * @see EngineFileCapabilities#processDetachKillSupported
   * @see EngineFileCapabilities#processDetachKeepSupported
   * @see EngineFileCapabilities#processDetachReleaseSupported
   */

   public boolean detach(int detachAction, int sendReceiveControlFlags)
   throws java.io.IOException
   {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(1, "DebuggeeProcess[" + this.processID() + "]" + ".detach(" + sendReceiveControlFlags + ")");

     final int requestCode = EPDC.Remote_ProcessDetach;

     if (!_debugEngine.prepareForEPDCRequest(requestCode, sendReceiveControlFlags))
        return false;

     if (Model.TRACE.EVT && Model.traceInfo())
       Model.TRACE.evt(1, "Sending EPDC request: Remote_ProcessDetach");

     return _debugEngine.processEPDCRequest(new EReqProcessDetach(_processID,
                                                                  detachAction),
                                            sendReceiveControlFlags);
   }

   /** @deprecated Use the version which takes the new enablement arguments
    *  instead.
    */

   public boolean monitorStorage(String addressExpression,
                                 Location evaluationContext,
                                 DebuggeeThread evaluationThread,
                                 int offsetToFirstLine,
                                 int offsetToLastLine,
                                 StorageStyle storageStyle,
                                 int numberOfUnitsPerLine,
                                 int sendReceiveControlFlags)
   throws java.io.IOException
   {
     return monitorStorage(addressExpression,
               evaluationContext,
               evaluationThread,
               offsetToFirstLine,
               offsetToLastLine,
               storageStyle,
               numberOfUnitsPerLine,
               true, // enable storage
               addressExpression == null ? false : true, // enable the expr
               null, // client
               null, // privileged listener
               sendReceiveControlFlags);
   }

   /**
    * Tell the debug engine to start monitoring this process' storage. The
    * debug engine will continue to monitor this storage and send the FE
    * updates to the storage until it is told not to via the 'Storage.remove' method.
    * @param addressExpression A string containing an arbitrary expression. This
    * expression will be evaluated and the resulting value will be used as the
    * base address of the storage to be monitored.
    * <p>
    * @param evaluationContext A Location object which determines the context
    * in which the addressExpression is to be evaluated. This affects various
    * aspects of the evaluation, including a) which expr evaluator will be
    * used and b) the starting scope for name lookup.
    * <p>
    * Note that if the expr needs no evaluation (e.g. a literal value), then
    * this argument is optional - null can be passed instead.
    * <p>
    * @param evaluationThread A DebuggeeThread object which determines the context
    * in which the addressExpression is to be evaluated. If the expr contains
    * references to automatic (i.e. stack-allocated) variables, this arg will
    * be used to determine which thread's stack will be used to retrieve
    * values for those variables.
    * <p>
    * Note that if the expr needs no evaluation (e.g. a literal value), then
    * this argument is optional - null can be passed instead.
    * <p>
    * @param offsetToFirstLine The monitored storage will be returned from the
    * debug engine as a series of storage <i>lines</i>. This argument tells
    * the debug engine what the first line of monitored storage should be,
    * relative to the base address for the monitor. A negative number may be
    * given in which case the first line of storage will be that many lines
    * <i>before</i> the line containing the base address.
    * <p>
    * @param offsetToLastLine This argument tells the debug engine what the
    * last line of monitored storage should be, relative to the base address for the monitor.
    * <p>
    * @param storageStyle This argument tells the debug engine
    * several things about how the FE wants the storage to be formatted:
    * <ul>
    * <li>The unit size i.e. how many bytes of storage make up a single unit.
    * Examples: 8 bits per unit, 16 bits per unit, etc.
    * <li>The unit type i.e. what type should the engine interpret each unit
    * of storage as? Examples: int, float, etc.
    * <li>How to format the value. Examples: decimal, hex, etc.
    * </ul>
    * <p>
    * @param numberOfUnitsPerLine Tells the debug engine how many storage units
    * should be displayed per line.
    * <p>
    * @param enableStorage If 'true' the engine will monitor the storage and
    * send the Model updates whenever the storage changes. If 'false', the
    * engine will not monitor the storage. The enablement of the storage
    * monitor can be toggled using the Storage.enable and Storage.disable
    * methods.
    * <p>
    * @param enableExpression If 'true' the engine will re-evaluate the
    * the expression in addressExpression and ensure that the storage
    * monitor's base address always reflects the current value of the
    * expression. If this arg is 'false', the expression will be evaluated
    * only once (when the storage monitor is first created) and that is the
    * value that will be used as the base address of the storage monitor -
    * the base address will not change as the value of the expression
    * changes. The enablement of the expression can be toggled using the
    * Storage.enableExpression and Storage.disableExpression methods.
    * @see Storage
    * @see StorageAddedEvent
    */

   public boolean monitorStorage(String addressExpression,
                                 Location evaluationContext,
                                 DebuggeeThread evaluationThread,
                                 int offsetToFirstLine,
                                 int offsetToLastLine,
                                 StorageStyle storageStyle,
                                 int numberOfUnitsPerLine,
                                 boolean enableStorage,
                                 boolean enableExpression,
                                 Client client,
                                 DebuggeeProcessEventListener privilegedListener,
                                 int sendReceiveControlFlags)
   throws java.io.IOException
   {
     if (Model.TRACE.DBG && Model.traceInfo())
       Model.TRACE.dbg(2, "DebuggeeProcess[" + this.processID() + "]" + ".monitorStorage() : Address=" + addressExpression );

     // TODO: When the deprecated versions of this method are removed, the
     // client arg should no longer be optional i.e. must be non-null.

     EStdExpression2 epdcExpression = null;

     if (addressExpression != null)
        epdcExpression = new EStdExpression2(evaluationContext == null ?
                                             null :
                                             evaluationContext.getEStdView(),
                                             addressExpression,
                                             evaluationThread == null ?
                                             0 :
                                             evaluationThread.debugEngineAssignedID(),
                                             0);

     return monitorStorage(epdcExpression,
                           offsetToFirstLine,
                           offsetToLastLine,
                           storageStyle.getStyleIdentifier(),
                           numberOfUnitsPerLine,
                           enableStorage,
                           enableExpression,
                           client,
                           privilegedListener,
                           sendReceiveControlFlags);
   }

   /**
    * @deprecated
    */

   public boolean monitorStorage(String addressExpression,
                                 Location evaluationContext,
                                 DebuggeeThread evaluationThread,
                                 int offsetToFirstLine,
                                 int offsetToLastLine,
                                 StorageStyle storageStyle,
                                 int numberOfUnitsPerLine,
                                 boolean enableStorage,
                                 boolean enableExpression,
                                 int sendReceiveControlFlags)
   throws java.io.IOException
   {
      return monitorStorage(addressExpression,
                            evaluationContext,
                            evaluationThread,
                            offsetToFirstLine,
                            offsetToLastLine,
                            storageStyle,
                            numberOfUnitsPerLine,
                            enableStorage,
                            enableExpression,
                            null, // client
                            null, // priveleged event listener
                            sendReceiveControlFlags);
   }

   boolean monitorStorage(EStdExpression2 epdcExpression,
                          int offsetToFirstLine,
                          int offsetToLastLine,
                          short storageStyle,
                          int numberOfUnitsPerLine,
                          boolean enableStorage,
                          boolean enableExpression,
                          Client client,
                          DebuggeeProcessEventListener privilegedListener,
                          int sendReceiveControlFlags)
   throws java.io.IOException
   {
     final int requestCode = EPDC.Remote_Storage2;

     if (!_debugEngine.prepareForEPDCRequest(requestCode, sendReceiveControlFlags))
        return false;

     if (Model.checkFCTBit)
     {
         if (!_debugEngine.getCapabilities().getWindowCapabilities().monitorStorageSupported())
         {
             _debugEngine.cancelEPDCRequest(requestCode);
             return false;
         }
     }

     if (Model.TRACE.EVT && Model.traceInfo())
       Model.TRACE.evt(2, "Sending EPDC request: Remote_Storage2");

     return _debugEngine.processEPDCRequest(new EReqStorage2(epdcExpression,
                                                             offsetToFirstLine,
                                                             offsetToLastLine,
                                                             storageStyle,
                                                             numberOfUnitsPerLine,
                                                             enableStorage,
                                                             enableExpression),
                                            client,
                                            privilegedListener,
                                            sendReceiveControlFlags);
   }

   /**
    * Tell the debug engine to start monitoring this process' storage. Because
    * this particular version of the request takes no address argument, it is left up to the
    * debug engine to decide what address will be used to monitor the storage.
    * By default, this method causes the storage monitor to be enabled.
    */

   public boolean monitorStorage(int offsetToFirstLine,
                                 int offsetToLastLine,
                                 StorageStyle storageStyle,
                                 int numberOfUnitsPerLine,
                                 int sendReceiveControlFlags)
   throws java.io.IOException
   {
      if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(2, "DebuggeeProcess[" + this.processID() + "]" + ".monitorStorage()");

      return monitorStorage(null, // expr string
                            null, // context
                            null, // thread
                            offsetToFirstLine,
                            offsetToLastLine,
                            storageStyle,
                            numberOfUnitsPerLine,
                            true,  // enable storage
                            false, // don't enable the expr (there isn't one)
                            null,  // client
                            null,  // privileged listener
                            sendReceiveControlFlags);
   }

   /**
    * Send a request to set a breakpoint at an address or an expression
    * (that would result into an address).
    * @param addrOrExpr the address or expression as typed by the user
    * @param location The context of the breakpoint. This parameter will be
    * needed if the conditional expression contains variable names. However,
    * if the expression is a literal (such as an address), no location is
    * necessary. Therefore, for the latter case a value of null can be
    * provided.
    * @param stopEvery the number of times the breakpoint should stop
    * the execution when the breakpoint is hit. The default value for this
    * parameter is 1.
    * @param from the breakpoint should stop execution if the number of
    * times the breakpoint is hit is greater that 'from'. The default value
    * for this parameter is 1.
    * @param to the breakpoint should stop execution if the number of
    * times the breakpoint is hit is less than 'to'. The default value for
    * this parameter is 0 (infinity).
    * @param threadID The id of the DebuggeeThread object in which the
    * breakpoint is to be evaluated. If the value of zero is passed as
    * the thread id, the evaluation will be for every possible thread
    * available.
    * @param sendReceievControlFlags this flag indicates the state in
    * which the request is to be sent.
    * @return 'true' if the request to add the breakpoint was sent
    * successfully, and 'false' otherwise.
    * @exception java.io.IOException if there is a communication problem
    * with the debug engine, this exception occurs.
    * @see AddressBreakpoint
    * @see LocationBreakpoint
    * @see Breakpoint
    * @deprecated The method setAddressBreakpoint(boolean, String, location, stopEvery, from, to, expr, int, ints)
    *             should be used.  Same parameters, but with "true" or
    *             "false" as the first parm
    */
   public boolean setAddressBreakpoint(String addrOrExpr,
                                       Location location,
                                       int stopEvery,
                                       int from,
                                       int to,
                                       String expr,
                                       int threadID,
                                       int sendReceiveControlFlags)
   throws java.io.IOException
   {
      return setAddressBreakpoint(true,
                                  addrOrExpr,
                                  location,
                                  stopEvery,
                                  from,
                                  to,
                                  expr,
                                  threadID,
                                  sendReceiveControlFlags);
   }

   /**
    * Send a request to set a breakpoint at an address or an expression
    * (that would result into an address).
    * @param enabled True if the breakpoint is to be created "enabled"
    * @param addrOrExpr the address or expression as typed by the user
    * @param location The context of the breakpoint. This parameter will be
    * needed if the conditional expression contains variable names. However,
    * if the expression is a literal (such as an address), no location is
    * necessary. Therefore, for the latter case a value of null can be
    * provided.
    * @param stopEvery the number of times the breakpoint should stop
    * the execution when the breakpoint is hit. The default value for this
    * parameter is 1.
    * @param from the breakpoint should stop execution if the number of
    * times the breakpoint is hit is greater that 'from'. The default value
    * for this parameter is 1.
    * @param to the breakpoint should stop execution if the number of
    * times the breakpoint is hit is less than 'to'. The default value for
    * this parameter is 0 (infinity).
    * @param threadID The id of the DebuggeeThread object in which the
    * breakpoint is to be evaluated. If the value of zero is passed as
    * the thread id, the evaluation will be for every possible thread
    * available.
    * @param sendReceievControlFlags this flag indicates the state in
    * which the request is to be sent.
    * @return 'true' if the request to add the breakpoint was sent
    * successfully, and 'false' otherwise.
    * @exception java.io.IOException if there is a communication problem
    * with the debug engine, this exception occurs.
    * @see AddressBreakpoint
    * @see LocationBreakpoint
    * @see Breakpoint
    */

   public boolean setAddressBreakpoint(boolean enabled,
                                       String addrOrExpr,
                                       Location location,
                                       int stopEvery,
                                       int from,
                                       int to,
                                       String expr,
                                       int threadID,
                                       int sendReceiveControlFlags)
   throws java.io.IOException
   {
       return setAddressBreakpoint(enabled,
                                   addrOrExpr,
                                   location,
                                   stopEvery, from, to,
                                   expr,
                                   threadID,
                                   sendReceiveControlFlags,
                                   null);
   }

   public boolean setAddressBreakpoint(boolean enabled,
                                       String addrOrExpr,
                                       Location location,
                                       int stopEvery,
                                       int from,
                                       int to,
                                       String expr,
                                       int threadID,
                                       int sendReceiveControlFlags,
                                       Object property)
   throws java.io.IOException
   {
     if (Model.TRACE.DBG && Model.traceInfo())
         Model.TRACE.dbg(2, "DebuggeeProcess[" + this.processID() + "]" + ".setAddressBreakpoint() : AddOrExpr=" + addrOrExpr);

     if (!_debugEngine.prepareForEPDCRequest(EPDC.Remote_BreakpointLocation,
                                             sendReceiveControlFlags))
         return false;

     if (Model.checkFCTBit)
     {
         if (!_debugEngine.getCapabilities().getBreakpointCapabilities().addressBreakpointsSupported())
         {
             _debugEngine.cancelEPDCRequest(EPDC.Remote_BreakpointLocation);
             return false;
         }
     }

     EEveryClause clause = null;

     // If no values have been specified for the every clause create one
     // with a set of defaults.
     if (!(stopEvery == 0 && from == 0 && to == 0))
         clause = new EEveryClause(stopEvery, to, from);

     EStdExpression2 conditionalExpr = null;
     if (expr != null)
         conditionalExpr = new EStdExpression2(null, expr, threadID, 0);

     EStdView context = null;
     if (location != null)
         context = location.getEStdView();

     if (Model.TRACE.EVT && Model.traceInfo())
         Model.TRACE.evt(2, "Sending EPDC request: Remote_BreakpointLocation");

     if (!_debugEngine.processEPDCRequest(new EReqBreakpointAddress(
                                                       enabled ? EPDC.BkpEnable : 0,
                                                       clause,
                                                       addrOrExpr,
                                                       null, null, null,
                                                       conditionalExpr,
                                                       threadID,
                                                       context),
                                         sendReceiveControlFlags,
                                         property))
         return false;
     else
         return true;
   }

   /**
    * Send a request to set a breakpoint when a particular address or an
    * expression (that would result into an address) is changed.
    * @param addrOrExpr the address or expression as typed by the user
    * @param stopEvery the number of times the breakpoint should stop
    * the execution when the breakpoint is hit
    * @param from the breakpoint should stop execution if the number of
    * times the breakpoint is hit is greater that 'from'.
    * @param to the breakpoint should stop execution if the number of
    * times the breakpoint is hit is less than 'to'.
    * @parm byteCount the number of bytes to watch, depending on the
    * language the range of the byteCount will vary. For example, for
    * C++ the maximum byte count is 4. This means that the byteCount to
    * watch for an address could be set to 1, 2, or 4 bytes.
    * @param threadID The id of the DebuggeeThread object in which the
    * breakpoint is to be evaluated. If the value of zero is passed as
    * the thread id, the evaluation will be for every possible thread
    * available.
    * @param sendReceievControlFlags this flag indicates the state in
    * which the request is to be sent.
    * @return 'true' if the request to add the breakpoint was sent
    * successfully, and 'false' otherwise.
    * @exception java.io.IOException if there is a communication problem
    * with the debug engine, this exception occurs.
    * @see Watchpoint
    * @see EventBreakpoint
    * @see Breakpoint
    */

   public boolean setWatchpoint(String addrOrExpr,
                                int stopEvery, int from, int to,
                                int byteCount,
                                int threadID,
                                int sendReceiveControlFlags)
   throws java.io.IOException
   {
     return setWatchpoint(EPDC.BkpEnable,
                          addrOrExpr,
                          stopEvery, from, to,
                          byteCount,
                          threadID,
                          null, // module name
                          null, // part name
                          null, // file name
                          null, // computed addr
                          sendReceiveControlFlags,
                          null);
   }

   public boolean setWatchpoint(String addrOrExpr,
                                int stopEvery, int from, int to,
                                int byteCount,
                                int threadID,
                                int sendReceiveControlFlags,
                                Object property)
   throws java.io.IOException
   {
     return setWatchpoint(EPDC.BkpEnable,
                          addrOrExpr,
                          stopEvery, from, to,
                          byteCount,
                          threadID,
                          null, // module name
                          null, // part name
                          null, // file name
                          null, // computed addr
                          sendReceiveControlFlags,
                          property);
   }

   boolean setWatchpoint(short attributes,
                         String addrOrExpr,
                         int stopEvery, int from, int to,
                         int byteCount,
                         int threadID,
                         String moduleName,
                         String partName,
                         String fileName,
                         String computedAddress,
                         int sendReceiveControlFlags)
   throws java.io.IOException
   {
     return setWatchpoint(EPDC.BkpEnable,
                          addrOrExpr,
                          stopEvery, from, to,
                          byteCount,
                          threadID,
                          moduleName,
                          partName,
                          fileName,
                          computedAddress,
                          sendReceiveControlFlags,
                          null);
   }

   boolean setWatchpoint(short attributes,
                         String addrOrExpr,
                         int stopEvery, int from, int to,
                         int byteCount,
                         int threadID,
                         String moduleName,
                         String partName,
                         String fileName,
                         String computedAddress,
                         int sendReceiveControlFlags,
                         Object property)
   throws java.io.IOException
   {
     if (Model.TRACE.DBG && Model.traceInfo())
         Model.TRACE.dbg(2, "DebuggeeProcess[" + this.processID() + "]" + ".setWatchpoint() : AddrOrExpr=" + addrOrExpr);

     if (!_debugEngine.prepareForEPDCRequest(EPDC.Remote_BreakpointEvent,
                                             sendReceiveControlFlags))
         return false;

     if (Model.checkFCTBit)
     {
         if (!_debugEngine.getCapabilities().getBreakpointCapabilities().watchpointsSupported())
         {
             _debugEngine.cancelEPDCRequest(EPDC.Remote_BreakpointEvent);
             return false;
         }
     }

     EEveryClause everyClause;

     if (stopEvery == 0 && from == 0 && to == 0)
         everyClause = null;
     else
         everyClause = new EEveryClause(stopEvery, to, from);

     if (Model.TRACE.EVT && Model.traceInfo())
         Model.TRACE.evt(2, "Sending EPDC request: Remote_BreakpointEvent");

     if (!_debugEngine.processEPDCRequest(new EReqBreakpointWatchpoint(
                                              attributes,
                                              everyClause,
                                              addrOrExpr,
                                              moduleName,
                                              partName,
                                              fileName,
                                              null, // cond expr
                                              byteCount,
                                              null, // context
                                              threadID,
                                              computedAddress),
                                          sendReceiveControlFlags,
                                          property))
         return false;
     else
         return true;
   }


   /**
    * Send a request to set a breakpoint when a particular module
    * is to be loaded.
    * @param moduleName the name of the module as typed by the user
    * @param stopEvery the number of times the breakpoint should stop
    * the execution when the breakpoint is hit
    * @param from the breakpoint should stop execution if the number of
    * times the breakpoint is hit is greater that 'from'.
    * @param to the breakpoint should stop execution if the number of
    * times the breakpoint is hit is less than 'to'.
    * @param threadID The id of the DebuggeeThread object in which the
    * breakpoint is to be evaluated. If the value of zero is passed as
    * the thread id, the evaluation will be for every possible thread
    * available.
    * @param sendReceievControlFlags this flag indicates the state in
    * which the request is to be sent.
    * @return 'true' if the request to add the breakpoint was sent
    * successfully, and 'false' otherwise.
    * @exception java.io.IOException if there is a communication problem
    * with the debug engine, this exception occurs.
    * @see ModuleLoadBreakpoint
    * @see EventBreakpoint
    * @see Breakpoint
    */
   public boolean setModuleLoadBreakpoint(boolean enable,
                                          String moduleName,
                                          int stopEvery, int from, int to,
                                          int threadID,
                                          int sendReceiveControlFlags)
   throws java.io.IOException
   {
     return setModuleLoadBreakpoint(enable,
                                    moduleName,
                                    stopEvery, from, to,
                                    threadID,
                                    sendReceiveControlFlags,
                                    null);
   }

   public boolean setModuleLoadBreakpoint(boolean enable,
                                          String moduleName,
                                          int stopEvery, int from, int to,
                                          int threadID,
                                          int sendReceiveControlFlags,
                                          Object property)
   throws java.io.IOException
   {
     if (Model.TRACE.DBG && Model.traceInfo())
         Model.TRACE.dbg(2, "DebuggeeProcess[" + this.processID() + "]" + ".setModuleLoadBreakpoint() : ModuleName=" + moduleName);


     if (!_debugEngine.prepareForEPDCRequest(EPDC.Remote_BreakpointEvent,
                                             sendReceiveControlFlags))
         return false;

     if (Model.checkFCTBit)
     {
         if (!_debugEngine.getCapabilities().getBreakpointCapabilities().moduleLoadBreakpointsSupported())
         {
             _debugEngine.cancelEPDCRequest(EPDC.Remote_BreakpointEvent);
             return false;
         }
     }

     EEveryClause everyClause;

     if (stopEvery == 0 && from == 0 && to == 0)
         everyClause = null;
     else
         everyClause = new EEveryClause(stopEvery, to, from);

     if (Model.TRACE.EVT && Model.traceInfo())
       Model.TRACE.evt(2, "Sending EPDC request: Remote_BreakpointEvent");

     _debugEngine.setRequestProperty(property);
     if (!_debugEngine.processEPDCRequest(new EReqBreakpointModuleLoad(
                                              enable ? EPDC.BkpEnable : 0,
                                              everyClause,
                                              moduleName,
                                              threadID),
                                          sendReceiveControlFlags))
         return false;
     else
         return true;
   }

   void tellChildrenThatOwnerHasBeenDeleted()
   {
     if (Model.TRACE.DBG && Model.traceInfo())
       Model.TRACE.dbg(3, "DebuggeeProcess[" + this.processID() + "]" + ".tellChildrenThatOwnerHasBeenDeleted()");

     int i;

     if (Model.TRACE.EVT && Model.traceInfo())
       Model.TRACE.evt(3, "Telling all modules that " + "DebuggeeProcess[" + this.processID() + "]" + " has been deleted");

     for (i = 0; i < _modules.size(); i++)
         if (_modules.elementAt(i) != null)
           ((DebugModelObject)_modules.elementAt(i)).setOwnerHasBeenDeleted();

     if (Model.TRACE.EVT && Model.traceInfo())
       Model.TRACE.evt(3, "Telling all threads that " + "DebuggeeProcess[" + this.processID() + "]" + " has been deleted");

     for (i = 0; i < _threads.size(); i++)
         if (_threads.elementAt(i) != null)
           ((DebugModelObject)_threads.elementAt(i)).setOwnerHasBeenDeleted();

     if (Model.TRACE.EVT && Model.traceInfo())
       Model.TRACE.evt(3, "Telling all monitored expressions that " + "DebuggeeProcess[" + this.processID() + "]" + " has been deleted");

     for (i = 0; i < _monitoredExpressions.size(); i++)
         if (_monitoredExpressions.elementAt(i) != null)
           ((DebugModelObject)_monitoredExpressions.elementAt(i)).setOwnerHasBeenDeleted();

     if (Model.TRACE.EVT && Model.traceInfo())
       Model.TRACE.evt(3, "Telling all breakpoints that " + "DebuggeeProcess[" + this.processID() + "]" + " has been deleted");

     for (i = 0; i < _breakpoints.size(); i++)
         if (_breakpoints.elementAt(i) != null)
           ((DebugModelObject)_breakpoints.elementAt(i)).setOwnerHasBeenDeleted();

     if (Model.TRACE.EVT && Model.traceInfo())
       Model.TRACE.evt(3, "Telling storage that " + "DebuggeeProcess[" + this.processID() + "]" + " has been deleted");

     if (_storage != null)
        for (i = 0; i < _storage.size(); i++)
            if (_storage.elementAt(i) != null)
              ((DebugModelObject)_storage.elementAt(i)).setOwnerHasBeenDeleted();
   }

  /**
   * Send a request to set a deferred line breakpoint in a file at a given
   * line number. The file at the time the breakpoint is set is not loaded
   * yet. This method cannot be used for setting non-deferred line breakpoints.
   * Therefore, the result of using thid method to set a non-deferred
   * line breakpoint is unpredictable.
   * @param enable A boolean variable to indicate whether the breakpoint should
   * be set as enabled or disabled. This breakpoint will be set to be initially
   * deferred and depending on the value of this argument it may be enabled
   * (or disabled) as well.
   * @param lineNumber The line at which the breakpoint is to be set.
   * @param moduleName The name of the executable or dll file
   * @param partName The name of the object file
   * @param fileName The name of the source file
   * @param threadID The id of the DebuggeeThread object in which the
   * breakpoint is to be evaluated. If the value of zero is passed as
   * the thread id, the evaluation will be for every possible thread
   * available.
   * @param stopEvery the number of times the breakpoint should stop
   * the execution when the breakpoint is hit
   * @param from the breakpoint should stop execution if the number of
   * times the breakpoint is hit is greater that 'from'.
   * @param to the breakpoint should stop execution if the number of
   * times the breakpoint is hit is less than 'to'.
   * @param expr the conditional expression for this breakpoint
   * @param sendReceiveControlFlags the flag indicating the state in which the
   * request is to be sent(synchronized, asynchronized).
   * @return 'true' if the set breakpoint request was successfully sent to
   * the debug engine, 'false' otherwise.
   * @exception java.io.IOException if there is a communication problem
   * with the debug engine, this exception occurs.
   * @see Breakpoint
   * @see LocationBreakpoint
   * @see ViewFile
   * @see LineBreakpoint
   */


  public boolean setDeferredLineBreakpoint(boolean enable,
                                           int lineNumber,
                                           String moduleName,
                                           String partName,
                                           String fileName,
                                           int threadID,
                                           int stopEvery, int from, int to,
                                           String expr,
                                           int sendReceiveControlFlags)
  throws java.io.IOException
  {
    return setDeferredLineBreakpoint(enable,
                                     lineNumber,
                                     null, // Function name
                                     moduleName,
                                     partName,
                                     fileName,
                                     threadID,
                                     stopEvery, from, to,
                                     expr,
                                     sendReceiveControlFlags,
                                     null);
  }

  public boolean setDeferredLineBreakpoint(boolean enable,
                                           int lineNumber,
                                           String moduleName,
                                           String partName,
                                           String fileName,
                                           int threadID,
                                           int stopEvery, int from, int to,
                                           String expr,
                                           int sendReceiveControlFlags,
                                           Object property)
  throws java.io.IOException
  {
    return setDeferredLineBreakpoint(enable,
                                     lineNumber,
                                     null, // Function name
                                     moduleName,
                                     partName,
                                     fileName,
                                     threadID,
                                     stopEvery, from, to,
                                     expr,
                                     sendReceiveControlFlags,
                                     property);
  }

  public boolean setDeferredLineBreakpoint(boolean enable,
                                           int lineNumber,
                                           String functionName,
                                           String moduleName,
                                           String partName,
                                           String fileName,
                                           int threadID,
                                           int stopEvery, int from, int to,
                                           String expr,
                                           int sendReceiveControlFlags)
  throws java.io.IOException
  {
    return setDeferredLineBreakpoint(enable,
                                     lineNumber,
                                     functionName, // Function name
                                     moduleName,
                                     partName,
                                     fileName,
                                     threadID,
                                     stopEvery, from, to,
                                     expr,
                                     sendReceiveControlFlags,
                                     null);
  }

  public boolean setDeferredLineBreakpoint(boolean enable,
                                           int lineNumber,
                                           String functionName,
                                           String moduleName,
                                           String partName,
                                           String fileName,
                                           int threadID,
                                           int stopEvery, int from, int to,
                                           String expr,
                                           int sendReceiveControlFlags,
                                           Object property)
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(2, "DebuggeeProcess[" + this.processID() + "]" +
                        ".setDeferredLineBreakpoint()");

    if (!_debugEngine.prepareForEPDCRequest(EPDC.Remote_BreakpointLocation,
                                           sendReceiveControlFlags))
       return false;

    EngineBreakpointCapabilities breakpointCapabilities = _debugEngine.getCapabilities().getBreakpointCapabilities();

    if (Model.checkFCTBit)
    {
        if ((!breakpointCapabilities.lineBreakpointsSupported()) ||
            // See defect 9389:
            // (!breakpointCapabilities.deferredBreakpointsSupported()) ||
            (!breakpointCapabilities.conditionalBreakpointsSupported() &&
             expr != null))
        {
            _debugEngine.cancelEPDCRequest(EPDC.Remote_BreakpointLocation);
            return false;
        }
    }

    // This will be a deferred breakpoint, check to see if it should be
    // enabled or disabled.
    short attribute = EPDC.BkpDefer;
    if (enable)
        attribute = (short)(attribute ^ EPDC.BkpEnable);

    EEveryClause clause = null;

    // If no values have been specified for the every clause create one
    // with a set of defaults.
    if (!(stopEvery == 0 && from == 0 && to == 0))
        clause = new EEveryClause(stopEvery, to, from);


    EStdView epdcContext = new EStdView((short)0, (short)0, 0, lineNumber);

    EStdExpression2 conditionalExpr = null;
    if (expr != null)
        conditionalExpr = new EStdExpression2(epdcContext, expr, 0, 0);

    String stmtNumber = null;

    if (breakpointCapabilities.statementBreakpointSupported())
    {
        stmtNumber = Integer.toString(lineNumber);
    }

    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(2, "Sending EPDC request Remote_BreakpointLocation");

    if (!_debugEngine.processEPDCRequest(new EReqBreakpointLine
                                            (attribute,
                                             clause,
                                             functionName,
                                             moduleName,
                                             partName,
                                             fileName,
                                             conditionalExpr,
                                             threadID,
                                             stmtNumber,
                                             epdcContext),
                                        sendReceiveControlFlags,
                                        property))
       return false;
    else
       return true;
  }

  /**
   * Send a request to set a deferred entry breakpoint to a given function.
   * This method cannot be used to set non-deferred entry breakpoints.
   * Therefore, the result of using thid method to set a non-deferred
   * entry breakpoint is unpredictable.
   * @param enable A boolean variable to indicate whether the breakpoint should
   * be set as enabled or disabled. This breakpoint will be set to be initially
   * deferred and depending on the value of this argument it may be enabled
   * (or disabled) as well.
   * @param entryName The name of function to set the breakpoint to
   * @param moduleName The name of the executable or dll file
   * @param partName The name of the object file
   * @param threadID The id of the DebuggeeThread object in which the
   * breakpoint is to be evaluated. If the value of zero is passed as
   * the thread id, the evaluation will be for every possible thread
   * available.
   * @param stopEvery the number of times the breakpoint should stop
   * the execution when the breakpoint is hit
   * @param from the breakpoint should stop execution if the number of
   * times the breakpoint is hit is greater that 'from'.
   * @param to the breakpoint should stop execution if the number of
   * times the breakpoint is hit is less than 'to'.
   * @param expr the conditional expression for this breakpoint
   * @param sendReceiveControlFlags the flag indicating the state in which the
   * request is to be sent(synchronized, asynchronized).
   * @return 'true' if the set breakpoint request was successfully sent to
   * the debug engine, 'false' otherwise.
   * @exception java.io.IOException if there is a communication problem
   * with the debug engine, this exception occurs.
   * @see Breakpoint
   * @see LocationBreakpoint
   * @see Function
   * @see EntryBreakpoint
   */
  public boolean setDeferredEntryBreakpoint(boolean enable,
                                            String entryName,
                                            String moduleName,
                                            String partName,
                                            int threadID,
                                            int stopEvery, int from, int to,
                                            String expr,
                                            int sendReceiveControlFlags)
  throws java.io.IOException
  {
      return setDeferredEntryBreakpoint(enable,
                                 entryName,
                                 moduleName,
                                 partName,
                                 threadID,
                                 stopEvery, from, to,
                                 expr,
                                 sendReceiveControlFlags,
                                 null);
  }

  public boolean setDeferredEntryBreakpoint(boolean enable,
                                            String entryName,
                                            String moduleName,
                                            String partName,
                                            int threadID,
                                            int stopEvery, int from, int to,
                                            String expr,
                                            int sendReceiveControlFlags,
                                            Object property)
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(2, "DebuggeeProcess[" + this.processID() + "]" +
                        ".setDeferredEntryBreakpoint()");

    if (!_debugEngine.prepareForEPDCRequest(EPDC.Remote_BreakpointLocation,
                                            sendReceiveControlFlags))
       return false;

    if (Model.checkFCTBit)
    {
        EngineBreakpointCapabilities breakpointCapabilities = _debugEngine.getCapabilities().getBreakpointCapabilities();

        if ((!breakpointCapabilities.functionBreakpointsSupported()) ||
            // See defect 9389:
            // (!breakpointCapabilities.deferredBreakpointsSupported()) ||
            (!breakpointCapabilities.conditionalBreakpointsSupported() &&
             expr != null))
        {
            _debugEngine.cancelEPDCRequest(EPDC.Remote_BreakpointLocation);
            return false;
        }
    }

    // By default the attribute to this breakpoint is defer, check to see
    // if it should be set as enabled or not (disabled).
    short attribute = EPDC.BkpDefer;

    if (enable)
        attribute = (short)(attribute ^ EPDC.BkpEnable);

    EEveryClause clause = null;

    // If no values have been specified for the every clause create one
    // with a set of defaults.
    if (!(stopEvery == 0 && from == 0 && to == 0))
        clause = new EEveryClause(stopEvery, to, from);

    EStdExpression2 conditionalExpr = null;
    if (expr != null)
        conditionalExpr = new EStdExpression2(null, expr, threadID, 0);

    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(2, "Sending EPDC request Remote_BreakpointLocation");

    if (!_debugEngine.processEPDCRequest(new EReqBreakpointEntry
                                            (attribute,
                                             clause,
                                             entryName,
                                             moduleName,
                                             partName,
                                             null,
                                             conditionalExpr,
                                             threadID,
                                             0),                  // entry id
                                        sendReceiveControlFlags,
                                        property))
       return false;
    else
       return true;
  }

  boolean monitorExpression(EStdView location,
                            int threadId,
                            String expression,
                            byte attribute,
                            short type,
                            String moduleName,
                            String partName,
                            String fileName,
                            int sendReceiveControlFlags)
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "DebuggeeProcess.monitorExpression(" + expression + ", " + attribute + ")");

    String stmtNum = null;

    if (_debugEngine.getEPDCVersion() > 305)
    {
        if (_debugEngine.getCapabilities().getBreakpointCapabilities().statementBreakpointSupported())
        {
            Part part = getPart(location.getPPID());
            View view = part.getView(location.getViewNo());
            ViewFile file = view.file(location.getSrcFileIndex());
            Line line = file.getLineFromCache(location.getLineNum());

            String prefix = line.getPrefix();

            if (prefix != null)
                stmtNum = prefix.trim();
        }
    }

    return monitorExpression(location, threadId, expression, attribute,
                             type, moduleName, partName, fileName,
                             stmtNum, sendReceiveControlFlags);
  }

  boolean monitorExpression(EStdView location,
                            int threadId,
                            String expression,
                            byte attribute,
                            short type,
                            String moduleName,
                            String partName,
                            String fileName,
                            String stmtNum,
                            int sendReceiveControlFlags)
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "DebuggeeProcess.monitorExpression(" + expression + ", " + attribute + ", " + stmtNum + ")");

    EStdExpression2 expr = new EStdExpression2(location, expression, threadId, 0);

    if (!_debugEngine.prepareForEPDCRequest(EPDC.Remote_Expression,
                                           sendReceiveControlFlags))
        return false;

    EngineCapabilities engineCapabilities = _debugEngine.getCapabilities();

    if (Model.checkFCTBit)
    {
        if (!engineCapabilities.getMonitorCapabilities().monitorEnableDisableSupported())
        {
            // the monitored expression is initially disabled
            if ((attribute & EPDC.MonEnable) == 0)
                _debugEngine.cancelEPDCRequest(EPDC.Remote_Expression);

            return false;
        }
    }

    EReqExpression monitorExprRequest = new EReqExpression(attribute,
                                                           (byte)0,
                                                           type,
                                                           expr,
                                                           moduleName,
                                                           partName,
                                                           fileName,
                                                           stmtNum);

    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(2, "Sending EPDC request: Remote_Expression");

    if (!_debugEngine.processEPDCRequest(monitorExprRequest,
                                        sendReceiveControlFlags))
        return false;
    else
        return true;

  }

  /**
   * Restart the debuggee using the same options that were used to start it
   * originally. Note that processes which were "attached" to cannot be
   * restarted.
   * <p>Calling this method is equivalent to calling DebuggeeProcess.terminate
   * followed by DebugEngine.prepareProgram. If successful, event listeners
   * will receive a processEnded event followed by a processAdded event i.e.
   * the process on which 'restart' is called is replaced by a new process
   * for the same debuggee.
   * @param sendReceiveControlFlags A set of flags which specify the mode
   * in which this request is to be performed. There is a set of constants
   * in DebugEngine which define the possible values for this argument.
   * For example, a value of DebugEngine.sendReceiveDefault means that the
   * request is to be done asynchronously, while a value of
   * DebugEngine.sendReceiveSynchronously
   * means that the request is to be performed synchronously.
   * <p>When done asynchronously,
   * this method will return immediately after sending the request to the
   * debug engine without waiting for a response from
   * the debug engine. The response to the request will be
   * received on a separate thread and client code will be notified of the
   * the response via the event listener mechanism.
   * @return 'true' if the request was successfully sent to
   * the debug engine, 'false' otherwise. Note that a return value of 'true'
   * does not imply that the debuggee was successfully restarted but
   * rather simply that the request was successfully sent to the debug engine.
   * Whether or not the debuggee was actually restarted will
   * be indicated via the event listener mechanism.
   * @see DebuggeeProcess#terminate
   * @see DebugEngine#prepareProgram
   * @see DebuggeePrepareOptions
   * @exception java.io.IOException If there is a problem communicating
   * with the debug engine.
   */

  public boolean restart(int sendReceiveControlFlags)
  throws java.io.IOException
  {
    DebuggeeStartupOptions debuggeeStartupOptions = _debugEngine.getDebuggeeStartupOptions();

    // For now (and for simplicity), we'll do the 'terminate' synchronously
    // even if the caller requested "asynchronous" in the sendReceiveControlFlags arg.
    // We will, however, honour the sendReceiveControlFlags when doing the
    // 'prepare'.
    // In order to restart, we check the following: i) The debuggee was
    // originally started via 'prepare', not 'attach', since attached-to
    // debuggees can't be restarted, ii) This object has not been deleted,
    // and iii) We can successfully terminate the current process before
    // trying to start a new one.

    if (debuggeeStartupOptions instanceof DebuggeePrepareOptions &&
        !thisObjectOrItsOwnerHasBeenDeleted())
    {
       _doingARestart = true;

       if (terminate(DebugEngine.sendReceiveSynchronously) &&
           hasBeenDeleted())
          return _debugEngine.prepareProgram(null, sendReceiveControlFlags);
       else
         _doingARestart = false;
    }

    return false;
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
       {
         if ((flags & SaveRestoreFlags.RESTORABLE_OBJECTS) != 0)
         {
            // Save those objects that restorable objects might depend on
            // in order to be restored:

            stream.writeObject(_debugEngine);
            stream.writeObject(_threads);

            if ((flags & SaveRestoreFlags.BREAKPOINTS) != 0)
            {
                Vector savedBreakpoints = new Vector();

                // Do not save auto set entry breakpoints
                for (int i = 0; i < _breakpoints.size(); i++)
                {
                     if (_breakpoints.elementAt(i) != null &&
                         !((Breakpoint)_breakpoints.elementAt(i)).isAutoSetEntry())
                         savedBreakpoints.addElement(_breakpoints.elementAt(i));
                }
                stream.writeObject(savedBreakpoints);
            }

            if ((flags & SaveRestoreFlags.STORAGE) != 0)
               stream.writeObject(_storage);

            if ((flags & SaveRestoreFlags.PROGRAM_MONITORS) != 0)
               stream.writeObject(_monitoredExpressions);
         }
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
       {
         if ((flags & SaveRestoreFlags.RESTORABLE_OBJECTS) != 0)
         {
            // Read those objects that restorable objects might depend on
            // in order to be restored:

            _debugEngine = (DebugEngine)stream.readObject();
            _threads = (Vector)stream.readObject();

            if ((flags & SaveRestoreFlags.BREAKPOINTS) != 0)
               _breakpoints = (Vector)stream.readObject();

            if ((flags & SaveRestoreFlags.STORAGE) != 0)
               _storage = (Vector)stream.readObject();

            if ((flags & SaveRestoreFlags.PROGRAM_MONITORS) != 0)
               _monitoredExpressions = (Vector)stream.readObject();
         }
       }
    }
    else
       stream.defaultReadObject();
  }

  /**
   * Save all restorable objects from this process into a file with the
   * given name.
   * This method would typically be used in conjunction with the restoreSavedObjects
   * method as follows: When a
   * process is first created, call restoreSavedObjects with a non-null
   * file name in order to restore the saved objects into the process. For
   * the remainder of the life of this process, call saveRestorableObjects
   * with a <i>null</i> file name so that restorable objects are saved back
   * into the same file.
   * @param fileName The name of the file to contain the saved Model objects.
   * If this argument is null, the method will use the last file name passed
   * to either restoreSavedObjects or saveRestorableObjects i.e. it will
   * "remember" which file contains (or will contain) the restorable objects.
   * @param saveFlags A set of flags for controlling which objects will be
   * saved.
   * @param asynchronously Indicates whether the saving of objects is
   * to be done synchronously (false) or asynchronously (true). When done
   * asynchronously, a new thread will be created and the saving of
   * objects will be done on that thread instead of on the thread that
   * called save. In this case, the return value from saveRestorableObjects() is
   * meaningless.
   * @return 'true' if restorable objects were successfully saved to the file,
   * 'false' otherwise
   * @see DebuggeeProcess#restoreSavedObjects
   * @see SaveRestoreFlags
   * @see PersistentRestorableObjects
   * @exception java.io.IOException If there is a problem opening or writing
   * to the given file.
   */

  public boolean saveRestorableObjects(String fileName,
                                       int saveFlags,
                                       boolean asynchronously
                                      )
  throws java.io.IOException
  {
    if (fileName == null)
    {
       if (_restorableObjects == null)
          return false;
    }
    else
    {
      // If we don't have a RestorableObjects object yet, or we do but the
      // file name is different than the one passed in, create a new one:

      if (_restorableObjects == null ||
          !_restorableObjects.getFileName().equals(fileName))
         _restorableObjects = new PersistentRestorableObjects(this, saveFlags, fileName);
    }

    _restorableObjects.setSaveFlags(saveFlags);

    _restorableObjects.save(asynchronously);

    return true;
  }

  /**
   * Retrieve saved Model objects from the given file and restore them into
   * this process. This method would typically be used as follows: When a
   * process is first created, call restoreSavedObjects with a non-null
   * file name in order to restore the saved objects into the process. For
   * the remainder of the life of this process, call saveRestorableObjects
   * with a <i>null</i> file name so that restorable objects are saved back
   * into the same file.
   * <p>Note that once started, the restoring of objects can be cancelled
   * by calling stopRestoring().
   * @param fileName The name of the file containing the saved Model objects.
   * If this argument is null, the method will use the last file name passed
   * to either restoreSavedObjects or saveRestorableObjects i.e. it will
   * "remember" which file contains (or will contain) the restorable objects.
   * @param restoreFlags A set of flags for controlling which objects will be
   * restored.
   * @param asynchronously Indicates whether the restoration of objects is
   * to be done synchronously (false) or asynchronously (true). When done
   * asynchronously, a new thread will be created and the restoration of
   * objects will be done on that thread instead of on the thread that
   * called restoreSavedObjects. In this case, the return value from restoreSavedObjects() is
   * meaningless.
   * @see DebuggeeProcess#saveRestorableObjects
   * @see DebuggeeProcess#stopRestoring
   * @see SaveRestoreFlags
   * @see PersistentRestorableObjects
   * @return 'true' if the saved objects were successfully restored into this process,
   * 'false' otherwise
   * @exception java.io.IOException If there is a problem communicating
   * with the debug engine while trying to restore the objects into this
   * process, or if there is problem reading the file containing the
   * saved objects.
   */

  public boolean restoreSavedObjects(String fileName,
                                     int restoreFlags,
                                     boolean asynchronously
                                    )
  throws java.io.IOException
  {
    if (fileName == null)
    {
       if (_restorableObjects == null)
          return false;
    }
    else
    {
      // If we don't have a RestorableObjects object yet, or we do but the
      // file name is different than the one passed in, create a new one:

      if (_restorableObjects == null ||
          !_restorableObjects.getFileName().equals(fileName))
         _restorableObjects = new PersistentRestorableObjects(this, restoreFlags, fileName);
    }

    _restorableObjects.setRestoreFlags(restoreFlags);

    return _restorableObjects.restore(asynchronously);
  }

  RestorableObjects getRestorableObjects()
  {
     return _restorableObjects;
  }

  /**
   * Cancel the restoration of bkps, monitored exprs, etc. that was started
   * by a call to restoreSavedObjects(). If the restoreSavedObjects() method is not executing, the
   * call to stopRestoring() will be ignored. This method is intended to
   * provide support for a "Cancel" button (or a similar mechanism) so that
   * the user can cancel the restoration of saved objects.
   */

  public void stopRestoring()
  {
    _restorableObjects.stopRestoring();
  }

  void clearAllChangeFlags()
  {
    _changeFlags = 0;
  }

  void setMonitoredExpressionsHaveChanged(boolean haveChanged)
  {
    if (haveChanged)
       _changeFlags |= PROGRAM_MONITORS_HAVE_CHANGED;
    else
    if ((_changeFlags & PROGRAM_MONITORS_HAVE_CHANGED) != 0)
       _changeFlags ^= PROGRAM_MONITORS_HAVE_CHANGED;
  }

  void setBreakpointsHaveChanged(boolean haveChanged)
  {
    if (haveChanged)
       _changeFlags |= BREAKPOINTS_HAVE_CHANGED;
    else
    if ((_changeFlags & BREAKPOINTS_HAVE_CHANGED) != 0)
       _changeFlags ^= BREAKPOINTS_HAVE_CHANGED;
  }

  void setStorageMonitorsHaveChanged(boolean haveChanged)
  {
    if (haveChanged)
       _changeFlags |= STORAGE_MONITORS_HAVE_CHANGED;
    else
    if ((_changeFlags & STORAGE_MONITORS_HAVE_CHANGED) != 0)
       _changeFlags ^= STORAGE_MONITORS_HAVE_CHANGED;
  }

  /**
   * Determine if the most recent reply from the debug engine caused any
   * changes to this process's monitored expressions.
   */

  public boolean monitoredExpressionsHaveChanged()
  {
    return (_changeFlags & PROGRAM_MONITORS_HAVE_CHANGED) != 0;
  }

  /**
   * Determine if the most recent reply from the debug engine caused any
   * changes to this process's breakpoints.
   */

  public boolean breakpointsHaveChanged()
  {
    return (_changeFlags & BREAKPOINTS_HAVE_CHANGED) != 0;
  }

  /**
   * Determine if the most recent reply from the debug engine caused any
   * changes to this process's storage monitors.
   */

  public boolean storageMonitorsHaveChanged()
  {
    return (_changeFlags & STORAGE_MONITORS_HAVE_CHANGED) != 0;
  }

  String getSaveRestoreFileName()
  {
    String directoryName = _debugEngine.getDebuggeeStartupOptions().getSaveRestoreDirectory();

    if (directoryName == null)
       directoryName = "";
    else
    {
       String fileSeparator = System.getProperty("file.separator");

       if (!directoryName.endsWith(fileSeparator))
          directoryName += fileSeparator;
    }

    return directoryName +
           _profileName +
           ".@" +
           Host.getPlatformMnemonic(_debugEngine.getPlatformID()) +
           Language.getLanguageMnemonic(_debugEngine.getDominantLanguage());
  }

  /*
   * @deprecated Use getFunctions(String functionName, boolean caseSensitive)
   */
  public Vector getFunctions(String functionName)
  throws java.io.IOException
  {
    return getFunctions(functionName, true);
  }

  /*
   * Return a list of functions matching a name.
   * @param functionName The string that is to be used as a criteria for
   * finding all functions within the process with that name.
   * @param caseSenitive The boolean variable to check if the string specified
   * is case sensitive or not.
   * @exception java.io.IOException If there is a problem communicating
   * with the debug engine.
   */
  public Vector getFunctions(String functionName, boolean caseSensitive)
  throws java.io.IOException
  {
    return getFunctions(functionName, 0, caseSensitive);
  }

  Vector getFunctions(String functionName, int partId, boolean caseSensitive)
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(2, "DebuggeeProcess.getFunctions("+functionName + ", " +partId + ", " + caseSensitive +")");

    if (functionName == null)
       return null;

    // Since this vector will be reused for each request sent we will set it
    // to null everytime before we go ahead with the request.
    _functionsByName = null;

    if (_debugEngine.prepareForEPDCRequest(EPDC.Remote_EntrySearch,
                                          DebugEngine.sendReceiveSynchronously))
    {
        if (Model.TRACE.EVT && Model.traceInfo())
            Model.TRACE.evt(2, "Sending EPDC request Remote_EntrySearch");

        _requestByName = true;
        _debugEngine.processEPDCRequest(new EReqEntrySearch((short)partId,
                                                            functionName,
                                                            0,
                                                            caseSensitive ? EPDC.CaseSensitive : EPDC.CaseInsensitive),
                                        DebugEngine.sendReceiveSynchronously);

        // Functions returned by the EntrySearch may not have complete
        // location information. In this case, the functions will have been
        // added to the process as "unresolved" functions. We now call
        // resolveFunctions in order to get the location information for the
        // functions: (See defect 10658.)

        resolveFunctions();
    }

    // reset the boolean if the next EntrySearch request is not done by
    // name (done by part id).
    _requestByName = false;

    return _functionsByName;
  }

  void resolveFunctions()
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(2, "In DebuggeeProcess.resolveFunctions");

    if (_unresolvedFunctions == null)
       return;

    int numberOfUnresolvedFunctions = _unresolvedFunctions.size();

    // For every unresolved function, do a Remote_EntryWhere request and use
    // the returned location(s) to add the function to the Model:
    EStdView context = null;
    for (int i = 0; i < numberOfUnresolvedFunctions; i++)
    {
      ERepEntryGetNext epdcEntry = (ERepEntryGetNext)_unresolvedFunctions.elementAt(i);

      context = _debugEngine.resolveFunction(epdcEntry);

      // Only add the function to the list if we trully have complete
      // context information
      if (context != null)
      {
          epdcEntry.setContext(context);
          addFunction(epdcEntry);
      }
    }

    _unresolvedFunctions.removeAllElements();
  }

  void addUnresolvedFunction(ERepEntryGetNext epdcEntry)
  {
    if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(2, "In DebuggeeProcess.addUnresolvedFunction");

    if (_unresolvedFunctions == null)
       _unresolvedFunctions = new Vector();

    _unresolvedFunctions.addElement(epdcEntry);
  }

  // Use this method to add a function to the Model if the location
  // information for the function in epdcEntry is complete. If it is not
  // complete, use addUnresolvedFunction.

  void addFunction(ERepEntryGetNext epdcEntry)
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(2, "In DebuggeeProcess.addFunction");

    // See if the Model already contains a Function object for this function.
    // If not, create one and add it to both the DebuggeeProcess as well as
    // the appropriate ViewFile :

    Function function = getFunction(epdcEntry.getEntryID(), false);

    if (function == null)
    {
       EStdView context = epdcEntry.getEStdView();
       Part part = getPart(context.getPPID());
       View view = part.getView(context.getViewNo());
       int sourceFileIndex = context.getSrcFileIndex();

       // The file containing this function may not yet
       // have a ViewFile object in the Model. If this is
       // the case then we will create a "dummy" ViewFile
       // object to contain the Function object. When we
       // ask the View if the ViewFile exists or not, we
       // want the View to only look at what it currently
       // contains i.e. we do not want the View to do a
       // "views verify" request in order to retrieve a list
       // of files from the engine. Doing so would result in
       // poor performance since we don't need a full blown
       // ViewFile object (we just want a container for the
       // Function object) and we certainly don't need the
       // engine to actually verify the file.

       ViewFile file = view.getFileNoVerify(sourceFileIndex);

       if (file == null)
       {
	  // Create a "dummy" ViewFile object:

	  file = new ViewFile(view, sourceFileIndex);
	  view.addViewFile(file);
       }

       setVectorElementToObject(function = new Function(file, epdcEntry),
                                _functions,
                                epdcEntry.getEntryID());

       file.addFunction(function);
    }

    // add the functions to the process, if their name
    // match the entry name in the request.

    if (_requestByName)
    {
       if (_functionsByName == null)
           _functionsByName = new Vector();

       _functionsByName.addElement(function);
    }
  }

  Function getFunction(int id, boolean askEngine)
  throws java.io.IOException
  {
    Function function = null;

    try
    {
      function = (Function)(_functions.elementAt(id));
    }
    catch (ArrayIndexOutOfBoundsException excp)
    {
    }

    if (function == null && askEngine)
    {
       // Do an entry search request using the entry ID:

       if (_debugEngine.prepareForEPDCRequest(EPDC.Remote_EntrySearch, DebugEngine.sendReceiveSynchronously))
       {
	  if (Model.TRACE.EVT && Model.traceInfo())
	    Model.TRACE.evt(2, "Sending EPDC request Remote_EntrySearch");

	  _debugEngine.processEPDCRequest(new EReqEntrySearch((short)0, // part id
							      null, // entry name
							      id,
							      EPDC.CaseSensitive),
					  DebugEngine.sendReceiveSynchronously);

	  // Functions returned by the EntrySearch may not have complete
	  // location information. In this case, the functions will have been
	  // added to the process as "unresolved" functions. We now call
	  // resolveFunctions in order to get the location information for the
	  // functions: (See defect 10658.)

	  resolveFunctions();

          function = getFunction(id, false);
       }
    }

    return function;
  }

  void setEvaluatedExpression(MonitoredExpression evaluatedExpression)
  {
    _evaluatedExpression = evaluatedExpression;
  }

  MonitoredExpression getEvaluatedExpression()
  {
    return _evaluatedExpression;
  }

  void removeEvaluatedExpression()
  {
    _evaluatedExpression.setHasBeenDeleted();
    _evaluatedExpression = null;
  }

  /**
   * Enable or disable the memory checking on the debug engine.
   * @param heapCheckEnabled A boolean variable that indicates the memory
   * check is to be turned on or off.
   * @param sendReceiveControlFlags the flag indicating the state in which the
   * request is to be sent(synchronized, asynchronized).
   * @exception java.io.IOException If there is a problem communicating
   * with the debug engine.
   */
  public boolean heapCheck(boolean heapCheckEnabled,
                           int sendReceiveControlFlags)
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(2, "DebuggeeProcess.heapCheck");

    if (!_debugEngine.prepareForEPDCRequest(EPDC.Remote_StorageUsageCheckSet,
                                            sendReceiveControlFlags))
        return false;

    // Do not send this request if the FCT bit is not set
    if (Model.checkFCTBit)
    {
        if (!_debugEngine.getCapabilities().getRunCapabilities().storageUsageCheckSupported())
        {
           _debugEngine.cancelEPDCRequest(EPDC.Remote_StorageUsageCheckSet);
           return false;
        }
    }

    if (Model.TRACE.EVT && Model.traceInfo())
        Model.TRACE.evt(1, "Sending EPDC request: Remote_StorageUsageCheckSet");

    int heapCheckAttribute = (heapCheckEnabled == true) ? EPDC.StorageUsageCheckEnable : 0;

    return _debugEngine.processEPDCRequest(new EReqStorageUsageCheckSet(
                                                           heapCheckAttribute),
                                           sendReceiveControlFlags);
  }

  void setIsPostMortem()
  {
    _isPostMortem = true;
  }

  public boolean isPostMortem()
  {
    return _isPostMortem;
  }

  /**
   * Remove references so they can be gc'ed.
   */
  void cleanup()
  {
    _qualifiedName = null;
    _profileName = null;
    //reused _debugEngine = null;
    if (_modules != null)
    {
       int cnt = _modules.size();
       for (int i = 0; i < cnt; i++)
       {
          Module m = (Module)_modules.elementAt(i);
          if (m != null)
             m.cleanup();
       }
       _modules.removeAllElements();
       _modules = null;
    }
    if (_parts != null)
    {
       int cnt = _parts.size();
       for (int i = 0; i < cnt; i++)
       {
          Part p = (Part)_parts.elementAt(i);
          if (p != null)
             p.cleanup();
       }
       _parts.removeAllElements();
       _parts = null;
    }
    if (_threads != null)
    {
       int cnt = _threads.size();
       for (int i = 0; i < cnt; i++)
       {
          DebuggeeThread t = (DebuggeeThread)_threads.elementAt(i);
          if (t != null)
             t.cleanup();
       }
       _threads.removeAllElements();
       _threads = null;
    }
    _eventListeners.removeAllElements();
    _processStopInfo = null;
    _monitoredExpressions = null;
    _breakpoints = null;
    if (_storage != null)
    {
       int cnt = _storage.size();
       for (int i = 0; i < cnt; i++)
       {
          Storage s = (Storage)_storage.elementAt(i);
          if (s != null)
             s.cleanup();
       }
       _storage.removeAllElements();
       _storage = null;
    }
    _modulesByName = null;
    _functionsByName = null;
    _evaluatedExpression = null;
    _unresolvedFunctions = null;
    _functions = null;
    _restorableObjects = null;
  }

  private int _processID;
  private String _qualifiedName;
  private String _profileName;
  private DebugEngine _debugEngine;
  private Vector _modules = new Vector(20, 20);
  private Vector _parts = new Vector(60, 60);
  private Vector _threads = new Vector(5, 5);
  private transient Vector _eventListeners = new Vector();
  private boolean _hasBeenRunToMainEntryPoint = false;
  private ProcessStopInfo _processStopInfo;
  private Vector _monitoredExpressions = new Vector();
  private Vector _breakpoints = new Vector();
  private Vector _storage;
  private Hashtable _modulesByName = new Hashtable();
  private PersistentRestorableObjects _restorableObjects;
  private int _changeFlags;
  private Vector _functionsByName;
  private boolean _requestByName;
  private MonitoredExpression _evaluatedExpression;
  private Vector _unresolvedFunctions;
  private Vector _functions = new Vector();
  private boolean _isPostMortem = false;
  private boolean _doingARestart = false;

  // Using same values here as in SaveRestoreFlags (just for consistency):

  static final int BREAKPOINTS_HAVE_CHANGED = 0x20000000;

  static final int PROGRAM_MONITORS_HAVE_CHANGED = 0x10000000;

  static final int STORAGE_MONITORS_HAVE_CHANGED = 0x04000000;
}
