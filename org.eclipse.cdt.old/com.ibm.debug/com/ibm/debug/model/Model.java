package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/Model.java, java-model, eclipse-dev, 20011128
// Version 1.22.1.2 (last modified 11/28/01 16:11:07)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.util.*;
import com.ibm.debug.connection.*;
import java.util.Vector;
import java.util.Hashtable;
import java.net.InetAddress;
import java.util.ResourceBundle;
import java.util.Locale;
import java.util.MissingResourceException;

public abstract class Model
{
  static final boolean dumpInfo = false;
  static final boolean traceCalls = true;
  static final boolean includePrintMethods = true;
  static final boolean checkFCTBit = true;

  // Behaviour flags:

  public static final int HANDLE_AMBIGUOUS_BREAKPOINTS = 0x00000001;
  public static final int SUPPRESS_ERROR_EVENTS_DURING_RESTORE = 0x00000002;
  public static final int SUPPRESS_MESSAGE_EVENTS_DURING_RESTORE = 0x00000004;
  public static final int HANDLE_AMBIGUOUS_MONITORED_EXPRESSIONS = 0x00000008;
  public static final int REUSE_DEBUG_ENGINES = 0x00000010;

  static private int _behaviourFlags = HANDLE_AMBIGUOUS_BREAKPOINTS |
									   SUPPRESS_ERROR_EVENTS_DURING_RESTORE |
									   SUPPRESS_MESSAGE_EVENTS_DURING_RESTORE |
									   HANDLE_AMBIGUOUS_MONITORED_EXPRESSIONS;

  //static final boolean traceInfo = false;
  static TraceLogger TRACE = new TraceLogger("MODEL");
  static final String packageName = "com.ibm.debug.model";
  private final static boolean _traceModelInfo = (Model.TRACE.DBG && System.getProperty("JT_MODEL") != null);

  private static Vector _hosts = new Vector();

  private static Hashtable _TCPIPHosts = new Hashtable();
  private static Hashtable _APPCHosts = new Hashtable();
  private static LocalHost _localHost;
  private static Vector _eventListeners = new Vector();
  private static EventManager _eventManager = new EventManager();
  private static EngineDaemon _engineDaemon = null;

  private static ResourceBundle _resourceBundle;

  static
  {
	// Try to get the appropriate message file. If not available, get the
	// English version:

	try
	{
	  _resourceBundle = ResourceBundle.getBundle(packageName + ".Resource",
												 Locale.getDefault());
	}
	catch (MissingResourceException excp1)
	{
	  try
	  {
		_resourceBundle = ResourceBundle.getBundle(packageName + ".Resource",
												   Locale.US);
	  }
	  catch (MissingResourceException excp2)
	  {
	  }
	}
  }
   /**
	* Add a Model event listener. Whenever
	* an event occurs for which there is a corresponding method in the
	* event listener's interface, that method will be called to inform the
	* listener of the event. More than one listener may be added
	* in which case the listeners will be notified of events in
	* the order in which they were added (i.e. FIFO).
	* @param eventListener The object whose methods will be called when
	* events occur.
	*/

   static public void addEventListener(DebugModelEventListener eventListener)
   {
	 if (Model.TRACE.DBG && Model.traceInfo())
		Model.TRACE.dbg(1, "Model.addEventListener(" + eventListener + ")");

	 _eventListeners.addElement(eventListener);
   }
  static void addHost(Host host, Hashtable hostHashtable)
  {
	// Local host will be added to the Vector of all hosts, but will
	// not be added to the hashtable for tcp/ip or APPC hosts:

	_hosts.addElement(host);

	if (host instanceof LocalHost)
	{
	   if (_localHost == null)
		  _localHost = (LocalHost)host;
	}
	else
	{
	   Object key = host.getAddress();

	   if (key != null && hostHashtable != null)
		  hostHashtable.put(key, host);
	}

	try
	{
	  _eventManager.fireEvent(new HostAddedEvent(Class.forName("com.ibm.debug.model.Model"),
												 host,
												 -1), // requestCode N/A
							  _eventListeners);
	}
	catch (ClassNotFoundException excp)
	{
	  if (Model.TRACE.ERR && Model.traceInfo())
		 Model.TRACE.err(1, "WARNING: HostAddedEvent NOT FIRED!");
	}
  }
  /**
   * This is equivalent to createClient(id, null, false).
   */

  public Client createClient(byte id)
  {
	return createClient(id, null, false);
  }
  /**
   * Create a new Client object.
   * @param id A value which identifies this client. There is a list of
   * possible values in the Client class. If none of these values are
   * appropriate, use Client.UNKNOWN. This method will return null if the
   * id is not one of the values in the Client class and is not Client.UNKNOWN.
   * <p>
   * NOTE: Client IDs are NOT used in determining equality - two different
   * Client objects could have the same ID and still not be considered the
   * same client. As far as the Model is concerned, each Client object
   * represents a different client, regardless of ID number. This allows a
   * given tool, such as Java SUI, to create more than one Client object
   * (all with the same ID: Client.VADD) and have those objects act as
   * somewhat independent clients of the Model. Each Client object can then
   * have its own rules for event filtering, event hiding, etc.
   * <p>
   * @param name The name of the client. If a name is not provided (i.e. this
   * arg is null) then the Model will use a default name which is appropriate
   * for the given client ID. Note that default names are for internal debugger
   * use only and are therefore NOT translated for NLS.
   * <p>
   * @param filterEvents This arg indicates whether the client wants the
   * Model to filter events so that the client receives only those events that
   * it generated, or it wants to receive all events (for which it
   * is listening) regardless of which client originated the event.
   * <br>
   * This behaviour can be changed later on by calling Client.setFilterEvents.
   * @see Client#UNKNOWN
   */

  public Client createClient(byte id,
							 String name,
							 boolean filterEvents
							)
  {
	// Not sure yet whether to save created clients in a list or not.

	if (id < 0 || id > Client.LAST_ID)
	   return null;

	return new Client(id, name, filterEvents);
  }

  /**
   * Retrieve a Host object based on the given ConnectionInfo object.
   * The host to be retrieved/created will be based on the host specified
   * by the ConnectionInfo object.
   * <p>
   * If a Host object does not yet exist for the host specified by the
   * ConnectionInfo object, the Model will create one. Note that if the
   * ConnectionInfo object does not specify a host (as is always the case
   * with a NamedPipeConnectionInfo object, for example) this method will
   * return the Host object which represents the local host (although
   * the getLocalHost method is the preferred way of getting the local
   * host). The LocalHost will also be returned if the connectionInfo arg
   * is of type TCPIPConnectionInfo <i>and</i> the Model can determine that
   * the tcp/ip address contained therein is the same address as the local
   * host.
   * <p>
   * Note that the same PHYSICAL/ACTUAL host machine might end up having
   * more than one Host object created for it if a) Under a given protocol
   * (e.g. tcp/ip) it has more than one name and, for some reason, we end
   * up connecting to it with more than one name, or b) It supports more
   * than one protocol and we end up connecting to it using more than one
   * protocol (e.g. tcp/ip as well as APPC). I doubt that these scenarios
   * are likely and I also don't think that they will be problematic for us
   * even if they do occur - as far as the Model is concerned, each time a
   * machine is connected to using a different name and/or protocol, it will
   * be treated as a completely different machine i.e. a new Host object will
   * be created to represent it.
   */

  public static Host getHost(ConnectionInfo connectionInfo)
  {
	// See if we already have a host which corresponds to the given
	// connectionInfo object:

	String hostName = connectionInfo.getHost();
	Object key = hostName; // key starts out being the host name

	// If the connectionInfo object is a NamedPipeConnectionInfo object, or
	// the host contained within the connectionInfo object is null, return
	// the local host:

	if (connectionInfo instanceof NamedPipeConnectionInfo ||
		hostName == null)
	   return getLocalHost(false);

	Hashtable hostHashtable = null;

	if (connectionInfo instanceof TCPIPConnectionInfo)
	{
	   // For TCPIP hosts, we'll use the InetAddress object as the key
	   // instead of the host name string since a given machine might have
	   // more than one tcp/ip name. By using the InetAddress object, the
	   // various names by which the machine is known will all map to the
	   // same Host object in the Model which is desirable. If an InetAddress
	   // object cannot be obtained from the given host name, then the
	   // host name itself will be used as the key.

	   try
	   {
		 key = InetAddress.getByName(hostName);
	   }
	   catch (java.net.UnknownHostException excp)
	   {
		 // Should we return null here or just use the host name as the key?
		 // Currently we're doing the latter.
	   }

	   hostHashtable = _TCPIPHosts;
	}
	else
	if (connectionInfo instanceof APPCConnectionInfo)
	   hostHashtable = _APPCHosts;
	else
	   return null;

	if (hostHashtable.containsKey(key))
	   return (Host)hostHashtable.get(key);

	// At this point we have not yet found an existing Host object which
	// corresponds to the given ConnectionInfo object. Before creating a
	// remote host object, we'll try and determine if the ConnectionInfo
	// object corresponds to the local host. This is currently only possible
	// for TCPIPConnectionInfo objects:

	if (connectionInfo instanceof TCPIPConnectionInfo)
	{
	   InetAddress localHostInetAddress = null;

	   try
	   {
		 localHostInetAddress = InetAddress.getLocalHost();
	   }
	   catch (java.net.UnknownHostException excp)
	   {
	   }

	   // If we were able to convert the host name into an InetAddress, above,
	   // then we'll compare that InetAddress with the one for the local
	   // host, otherwise, we'll compare the host name with the host name of
	   // the local host (all of which assumes that we can get the InetAddress
	   // for the local host in the first place!):

	   if (localHostInetAddress != null)
		  if (key instanceof InetAddress)
		  {
	    if (((InetAddress)key).equals(localHostInetAddress))
			{
	       getLocalHost(false).setAddress(localHostInetAddress);
			   return _localHost;
			}
		  }
		  else
			if (((String)key).equals(localHostInetAddress.getHostName()))
			{
	       getLocalHost(false).setAddress(key);
	       return _localHost;
			}
	}

	// If none of the above yielded a Host object, create a RemoteHost
	// and return it:

	RemoteHost remoteHost = new RemoteHost(key);
	addHost(remoteHost, hostHashtable);
	return remoteHost;
  }
  /**
   * Return a list of all Host objects that currently exist in the Model.
   */

  public static Vector getHosts()
  {
	return _hosts;
  }
  /**
   * Get the Model object which represents the local host. If a LocalHost
   * object does not yet exist, one will be created. The Model will automatically
   * attempt to determine the IP address of the local host when it is created
   * if the setIPAddress argument is 'true'.
   */

  public static LocalHost getLocalHost(boolean setIPAddress)
  {
	if (_localHost == null)
	{
	   _localHost = new LocalHost(setIPAddress);
	   addHost(_localHost, null);
	}

	return _localHost;
  }
  public static int getModelBehaviour()
  {
	return _behaviourFlags;
  }
  static String getResourceString(String key)
  {
	if (_resourceBundle == null)
	   return null;
	else
	   return _resourceBundle.getString(key);
  }
  /**
   * This method can be used to tell the Model whether or not it should
   * try to automatically handle ambiguous breakpoints. As an example,
   * consider a C++ function template which has been instantiated more than
   * once in the program being debugged. If the user tries to set a line
   * breakpoint within the source for the template, the engine may reply
   * with an error code which indiates that the location for setting the
   * breakpoint is ambiguous. If the Model has been told to automatically
   * handle this situation, it will detect the "ambiguous" return code
   * from the engine and will try to set a breakpoint in each instantiation
   * of the template. In this case, the client of the Model will not be
   * informed that the original breakpoint request was ambiguous.
   * <p>If the Model has been told NOT to handle ambiguous breakpoints,
   * an errorOccurred event will be fired. The client of the Model can then
   * examine the return code in the event and take appropriate action.
   * <p>The default behaviour of the Model is that it <i>will</i> attempt
   * to automatically handle ambiguous breakpoints.
   */

  public static void handleAmbiguousBreakpoints(boolean handle)
  {
	if (handle)
	   _behaviourFlags |= HANDLE_AMBIGUOUS_BREAKPOINTS;
	else
	   if (willHandleAmbiguousBreakpoints())
		  _behaviourFlags ^= HANDLE_AMBIGUOUS_BREAKPOINTS;
  }
  public static void handleAmbiguousMonitoredExpressions(boolean handle)
  {
	if (handle)
	   _behaviourFlags |= HANDLE_AMBIGUOUS_MONITORED_EXPRESSIONS;
	else
	   if (willHandleAmbiguousMonitoredExpressions())
		  _behaviourFlags ^= HANDLE_AMBIGUOUS_MONITORED_EXPRESSIONS;
  }
   /**
	* Remove a Model event listener so
	* that it no longer receives event notifications.
	* @param eventListener The event listener to be removed from the
	* list of listeners.
	*/

   static public void removeEventListener(DebugModelEventListener eventListener)
   {
	 if (Model.TRACE.EVT && Model.traceInfo())
		Model.TRACE.evt(1, "Model.removeEventListener(" + eventListener + ")");

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
  static void removeHost(Host host, Hashtable hostHashtable)
  {
	_hosts.removeElement(host);

	Object key = host.getAddress();

	if (key != null && hostHashtable != null)
	   hostHashtable.remove(key);

	if (host == _localHost)
	   _localHost = null;

	// TODO: Tell the host to fire a HostRemovedEvent on its listeners.
  }
  public static void reuseDebugEngines(boolean reuse)
  {
	if (reuse)
	   _behaviourFlags |= REUSE_DEBUG_ENGINES;
	else
	   if (willReuseDebugEngines())
		  _behaviourFlags ^= REUSE_DEBUG_ENGINES;
  }
  public static void setModelBehaviour(int behaviourFlags)
  {
	_behaviourFlags = behaviourFlags;
  }
  public static void suppressErrorEventsDuringRestore(boolean suppress)
  {
	if (suppress)
	   _behaviourFlags |= SUPPRESS_ERROR_EVENTS_DURING_RESTORE;
	else
	   if (willSuppressErrorEventsDuringRestore())
		  _behaviourFlags ^= SUPPRESS_ERROR_EVENTS_DURING_RESTORE;
  }
  public static void suppressMessageEventsDuringRestore(boolean suppress)
  {
	if (suppress)
	   _behaviourFlags |= SUPPRESS_MESSAGE_EVENTS_DURING_RESTORE;
	else
	   if (willSuppressMessageEventsDuringRestore())
		  _behaviourFlags ^= SUPPRESS_MESSAGE_EVENTS_DURING_RESTORE;
  }
  static final boolean traceInfo()
  {
  	return _traceModelInfo;
  }
  public static boolean willHandleAmbiguousBreakpoints()
  {
	return (_behaviourFlags & HANDLE_AMBIGUOUS_BREAKPOINTS) != 0;
  }
  public static boolean willHandleAmbiguousMonitoredExpressions()
  {
	return (_behaviourFlags & HANDLE_AMBIGUOUS_MONITORED_EXPRESSIONS) != 0;
  }
  public static boolean willReuseDebugEngines()
  {
	return (_behaviourFlags & REUSE_DEBUG_ENGINES) != 0;
  }
  public static boolean willSuppressErrorEventsDuringRestore()
  {
	return (_behaviourFlags & SUPPRESS_ERROR_EVENTS_DURING_RESTORE) != 0;
  }
  public static boolean willSuppressMessageEventsDuringRestore()
  {
	return (_behaviourFlags & SUPPRESS_MESSAGE_EVENTS_DURING_RESTORE) != 0;
  }
}
