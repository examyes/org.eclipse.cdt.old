package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/WileyEngineInfo.java, java-model, eclipse-dev, 20011128
// Version 1.3.1.2 (last modified 11/28/01 16:14:01)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.connection.*;
import com.ibm.debug.epdc.EPDC;
import java.util.Vector;

/**
 * This class provides information that is needed
 * in order to load or reuse a debug engine of this type.
 * @see Host#loadEngine
 * @see Host#getExistingDebugEngine
 */

public class WileyEngineInfo extends EngineInfo
{
  /**
   * This ctor will give the Wiley engine a default name of
   * "dewd". When combined with a product prefix such as "der", this
   * would result in complete name of "derdewd".
   * <p>This ctor will also give the engine a default format of
   * EngineInfo.EXECUTABLE
   */

  public WileyEngineInfo()
  {
    // TODO: Get the name from the properties file:

    super("dewd", EngineInfo.EXECUTABLE);
    _userSpecified = true;
  }

  /**
   * @param engineName The name of the executable file, DLL, or shared library
   * for the Wiley
   * engine <b>WITHOUT</b> the product prefix. For example, if the full
   * name of the engine including a product prefix of "der" is "derdewd" then
   * this argument should be "dewd".
   * @param byte engineFormat The format of this engine. Should be either
   * EngineInfo.EXECUTABLE or EngineInfo.LOADABLE_LIBRARY.
   */

  public WileyEngineInfo(String engineName, byte engineFormat)
  {
    super(engineName, engineFormat);
  }

  short getEPDCEngineID()
  {
    return EPDC.BE_TYPE_WILEY;
  }

  /**
   * Returns 'true'.
   */

  public boolean supportsTCPIPConnections()
  {
    return true;
  }

  /**
   * Returns 'false'.
   */

  public boolean supportsAPPCConnections()
  {
    return false;
  }

  /**
   * Returns 'true'.
   */

  public boolean supportsNamedPipeConnections()
  {
    return true;
  }

  void getDebuggeeRelatedArgs(Vector args, EngineArgs engineArgs)
  {
  }

  /**
   * This method will return a string which contains a set of
   * connection-related command line args
   * that can be used when invoking the debug engine. Example:
   * "-qprotocol=tcpip -qaddr=8001"
   */

  void getConnectionArgs(Vector args,
                         ConnectionInfo connectionInfo,
                         boolean useDaemon)
  {
    String addr = connectionInfo.getConduit();

    // TODO: Get all hard-coded strings from the properties file so they can be
    // changed without recompilation:

    String addressArgFormat = "-qaddr=";     // Tell the engine to sit there and
                                          // wait for us to connect to it.

    if (connectionInfo instanceof TCPIPConnectionInfo)
       args.addElement("-qprotocol=tcpip");
    else
    if (connectionInfo instanceof NamedPipeConnectionInfo)
       args.addElement("-qprotocol=npipe");
    else
       return;

    if (addr != null)
       args.addElement(addressArgFormat + addr);
  }

  void getQuietArg(Vector args)
  {
    args.addElement("-qquiet");
  }

  public boolean canConnectToUIDaemon()
  {
    return false;
  }

  boolean canBeExeced()
  {
    return _engineFormat == EXECUTABLE;
  }

  boolean canBeLoaded()
  {
    return _engineFormat == LOADABLE_LIBRARY;
  }

  boolean canStartEngineFromShell()
  {
    return false;
  }
}
