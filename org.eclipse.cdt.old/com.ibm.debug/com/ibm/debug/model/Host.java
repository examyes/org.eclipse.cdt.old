package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1995, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/Host.java, java-model, eclipse-dev, 20011128
// Version 1.32.1.2 (last modified 11/28/01 16:11:03)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.epdc.EPDC;
import com.ibm.debug.connection.ConnectionInfo;
import java.net.*;
import java.util.*;

/** This class represents a machine on which a debuggee process is running. It
 *  is abstract since it is extended by LocalHost and RemoteHost.
 *  Host objects
 *  contain DebugEngine objects which represent the debug engines that are
 *  being used to debug processes on the host machine.
 *  A LocalHost
 *  object should be used when the debug engine is running on the same
 *  machine as the debugger FE, and a RemoteHost object should be used when
 *  the debug engine and the debugger FE are running on different machines.
 *  Any number and
 *  combination of LocalHost and RemoteHost objects may be constructed
 *  but the intention is that there should be a 1-1 correspondence between
 *  these objects and the actual machines that they represent. This implies
 *  that, typically, only 1 LocalHost object should be constructed.
 *  <p>Client code typically constructs only two kinds of objects in the
 *  Model: one or more Host objects and one or more DebugEngine objects
 *  representing the debug engines on those hosts. A Host object must be
 *  constructed before constructing any DebugEngine on that Host since the
 *  DebugEngine constructor takes a Host object as its argument. This
 *  establishes the fact that the debug engine is owned by (or resides on)
 *  the given host. Almost all other objects in the model are constructed automatically
 *  based on information received from the debug engine regarding the processes being
 *  debugged. Host objects are the highest-level objects within the Model -
 *  they contain (either directly or indirectly) all other objects.
 *  @see DebugEngine
 *  @see LocalHost
 *  @see RemoteHost
 */

public abstract class Host extends DebugModelObject
{
  Host()
  {
  }

  Host(Object address)
  {
    setAddress(address);
  }


   /**
    * Add a host event listener to this Host object. Whenever
    * an event occurs for which there is a corresponding method in the
    * event listener's interface, that method will be called to inform the
    * listener of the event. More than one listener may be added to a given
    * Host object - the listeners will be notified of events in
    * the order in which they were added (i.e. FIFO).
    * @param eventListener The object whose methods will be called when
    * events occur.
    */

   public void addEventListener(HostEventListener eventListener)
   {
     if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(1, "Host.addEventListener(" + eventListener + ")");

     _eventListeners.addElement(eventListener);
   }

   /**
    * Remove a host event listener from this Host object so
    * that it no longer receives event notifications.
    * @param eventListener The event listener to be removed from this
    * Host object's list of listeners.
    */

   public void removeEventListener(HostEventListener eventListener)
   {
     if (Model.TRACE.EVT && Model.traceInfo())
        Model.TRACE.evt(1, "Host.removeEventListener(" + eventListener + ")");

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
   * @param isLoaded Pass 'true' if the actual debug engine that will be
   * represented by the new DebugEngine object has already been loaded
   * and is waiting for a connection from the FE, o.w. pass 'false'.
   */

  public DebugEngine getNewDebugEngine(boolean isLoaded)
  {
    DebugEngine debugEngine = new DebugEngine(this);

    debugEngine.setIsLoaded(isLoaded);

    return debugEngine;
  }

  /**
   * The returned object will be either an InetAddress (for those tcp/ip
   * hosts where the InetAddress is known), a String (for APPC hosts as well
   * as tcp/ip hosts where the InetAddress cannot be determined), or null
   * if no address is known for this host.
   */

  public Object getAddress()
  {
    return _address;
  }

  void setAddress(Object address)
  {
    if (address instanceof InetAddress || address instanceof String)
       _address = address;
  }

  /**
   * Ask the host to load a debug engine so that it can be used to debug a
   * program.
   * <p>Before calling loadEngine, client code should call reuseDebugEngine
   * to see if there is an engine already up and running which can be used.
   * <p>If an engine is successfully loaded, the next thing client code
   * would typically do is call Host.getNewDebugEngine to get a DebugEngine
   * object which represents the actual debug engine that was loaded.
   * @see LocalHost#loadEngine
   * @see Host#reuseDebugEngine
   */

  public abstract boolean loadEngine(EngineInfo engineInfo,
                                     ProductInfo productInfo,
                                     ConnectionInfo connectionInfo,
                                     EngineArgs engineArgs);

  /** Get a string representation of the host's address. Returns null if the
   *  address is not known.
   */

  public String name()
  {
    if (_address != null)
       return _address.toString();
    else
       return null;
  }

  InetAddress internetAddress()
  {
    if (_address instanceof InetAddress)
       return (InetAddress)_address;
    else
       return null;
  }

  /** When a debug engine is constructed, it will add itself to the host
   *  via this method.
   */

  void add(DebugEngine debugEngine)
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(1, "Host[" + this.name() + "].add(" + debugEngine + ")");

    _debugEngines.addElement(debugEngine);

    _eventManager.fireEvent(new DebugEngineAddedEvent(this, debugEngine, -1),
                            _eventListeners);
  }

  void remove(DebugEngine debugEngine)
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(1, this.name() + ".remove(" + debugEngine + ")");

    debugEngine.prepareToDie();

    _debugEngines.removeElement(debugEngine);
  }

  /**
   * Get a list of the debug engines currently owned by this Host.
   */

  public DebugEngine[] getDebugEnginesArray()
  {
    if (_debugEngines == null)
       return null;

    int numberOfDebugEngines = _debugEngines.size();

    if (numberOfDebugEngines == 0)
       return null;

    DebugEngine[] debugEngines = new DebugEngine[numberOfDebugEngines];

    _debugEngines.copyInto(debugEngines);

    return debugEngines;
  }

  /**
   * Client code should call this method <i><b>before</i></b> asking the
   * Model to launch a debug engine via the loadEngine method. If this method
   * returns a DebugEngine, the client can start using it to debug another
   * debuggee. Client code can assume that the engine has been initialized,
   * is not currently debugging a process, and we already have a connection
   * to it (i.e. it is not necessary to call DebugEngine.connect on the
   * returned engine).
   * <p>Engines returned by this method have been previously used to debug
   * a process but are no longer being used because the process was terminated
   * or ran to completion.
   * <p>If this method returns null, then
   * there are no engines available for reuse and the client code should
   * call loadEngine in order to get a brand new engine loaded.
   * @param engineInfo This identifies what kind of engine the client wants
   * (Java PICL, C++ PICL, etc.)
   * @param engineArgs A set of engine arguments that would be used to launch
   * an engine if one is not available for reuse. The Model will use this
   * set of arguments to further determine whether or not an existing
   * engine can be reused. For example, a Java PICL engine cannot be reused
   * if the jvm args that were used when the engine was launched are not
   * the same as the ones contained in the engineArgs arg. In other words,
   * a Java PICL engine cannot be reused if the user wants to pass the
   * engine a different set of jvm args than were used when the engine was
   * originally launched.
   */

  public DebugEngine reuseDebugEngine(EngineInfo engineInfo,
                                      EngineArgs engineArgs,
                                      byte dominantLanguage)
  {
    // TODO: For Java engines, make sure the debuggeeInterpreterArgs within
    // engineArgs are the same as what was used when the engine was
    // launched. For now we're not checking this when we're deciding
    // whether or not an engine can be reused but we should.

    // I'm assuming that the list of engines on a given host will be small
    // enough that a linear search here is okay:

    int numberOfDebugEngines = _debugEngines.size();

    for (int i = 0; i < numberOfDebugEngines; i++)
    {
        DebugEngine engine = (DebugEngine)_debugEngines.elementAt(i);

        if (engine.getEngineID() == engineInfo.getEPDCEngineID() &&
            engine.hasBeenInitialized() &&
            engine.process() == null &&
            dominantLanguage == engine.getDominantLanguage() &&
            engine.canBeReused())
        {
           if (Model.TRACE.DBG && Model.traceInfo())
              Model.TRACE.dbg(1, "Reusing debug engine " + engine);

           return engine;
        }
    }

    return null;
  }

  void setPlatformID(byte platformID, boolean canBeOverridden)
  {
    if (_platformIDCanBeOverridden)
    {
       _platformIDCanBeOverridden = canBeOverridden;
       _platformID = platformID;

       if (Model.TRACE.DBG && Model.traceInfo())
          Model.TRACE.dbg(1, "Host platform ID has been set to " + platformID);

    }
  }

  /**
   * For the local host, this method will return a valid platform ID as
   * soon as the LocalHost object is constructed. For remote hosts, however,
   * this method will only return a valid platform ID after one or more
   * debug engines on the host have been initialized. If no engine has
   * been initialized on a remote host, this method will return UNKNOWN.
   * @see Host#UNKNOWN
   */

  public byte getPlatformID()
  {
    return _platformID;
  }

  static char getPlatformMnemonic(short EPDCPlatformID)
  {
    return _platformMnemonics[EPDCPlatformID];
  }

  private final static char[] _platformMnemonics = new char[EPDC.LAST_PLATFORM_ID];

  static
  {
    _platformMnemonics[EPDC.PLATFORM_ID_OS2]   = 'o';
    _platformMnemonics[EPDC.PLATFORM_ID_MVS]   = 'm';
    _platformMnemonics[EPDC.PLATFORM_ID_VM370] = 'v';
    _platformMnemonics[EPDC.PLATFORM_ID_AS400] = 'a';
    _platformMnemonics[EPDC.PLATFORM_ID_AIX]   = 'x';
    _platformMnemonics[EPDC.PLATFORM_ID_NT]    = 'w';
    _platformMnemonics[EPDC.PLATFORM_ID_JVM]   = 'j';
    _platformMnemonics[EPDC.PLATFORM_ID_HPUX]  = 'h';
    _platformMnemonics[EPDC.PLATFORM_ID_SUN]   = 's';
  }

  private Object _address;
  private Vector _debugEngines = new Vector();
  private Vector _eventListeners = new Vector();
  private EventManager _eventManager = new EventManager();

  // NOTE: I'm not reusing EPDC's platform ids here, mostly because there isn't
  // one for Windows 95:

  private byte _platformID;
  private boolean _platformIDCanBeOverridden = true;

  public static final byte UNKNOWN   = 0;
  public static final byte OS2       = 1;
  public static final byte WindowsNT = 2;
  public static final byte Windows95 = 3;
  public static final byte AIX       = 4;
  public static final byte Linux     = 5;
  public static final byte OS400     = 6;
  public static final byte OS390     = 7;
  public static final byte JVM       = 8;
  public static final byte HPUX      = 9;
  public static final byte SUNOS     = 10;
}
