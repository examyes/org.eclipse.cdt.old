package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/CPlusPlusPICLEngineInfo.java, java-model, eclipse-dev, 20011128
// Version 1.18.1.2 (last modified 11/28/01 16:13:43)
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

public class CPlusPlusPICLEngineInfo extends EngineInfo
{
  /**
   * This ctor will give the C++ PICL engine a default name of
   * "irmtdbgc". This is the wrapper that will start up the engine.
   * <p>This ctor will also give the engine a default format of
   * EngineInfo.EXECUTABLE
   */

  public CPlusPlusPICLEngineInfo()
  {
    // TODO: Get the name from the properties file:

    //super("dfsrv", EngineInfo.EXECUTABLE);
    /* Defect 14170 - use the wrapper instead */
    super("irmtdbgc", EngineInfo.EXECUTABLE);
  }

  /**
   * @param engineName The name of the executable file, DLL, or shared library
   * for the C++ PICL
   * engine <b>WITHOUT</b> the product prefix. For example, if the full
   * name of the engine including a product prefix of "iwz" is "iwzdfsrv" then
   * this argument should be "dfsrv".
   * @param byte engineFormat The format of this PICL engine. Should be either
   * EngineInfo.EXECUTABLE or EngineInfo.LOADABLE_LIBRARY.
   * The flag _userSpecified is set to indicate this is not the default.
   */

  public CPlusPlusPICLEngineInfo(String engineName, byte engineFormat)
  {
    super(engineName, engineFormat);
    _userSpecified = true;
  }

  short getEPDCEngineID()
  {
    return EPDC.BE_TYPE_PICL;
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

  String[] getShellCommand(byte platformID, ProductInfo productInfo)
  {
    if (!canStartEngineFromShell())
        return null;

    String[] shellCommand = null;
    // TODO: Let client of Model specify title for engine window.

    if (platformID == Host.AIX)
    {
       shellCommand = new String[3];

       shellCommand[0] = "/bin/sh";
       shellCommand[1] = "-c";
       shellCommand[2] = "_LIBPATH=$LIBPATH; export _LIBPATH; aixterm ";

       String engineWindowTitle = getEngineWindowTitle();

       if (engineWindowTitle != null)
          shellCommand[2] += "-T \"" + engineWindowTitle + "\" ";

       shellCommand[2] += "-e " + getEngineDirectory(productInfo) + "derdsetlp ";
    }

    return shellCommand;
  }

  void getDebuggeeRelatedArgs(Vector args, EngineArgs engineArgs)
  {
    // If a process ID was specified then we want to pass the engine the
    // -qpid=processID argument:

    String processID = engineArgs.getProcessID();

    if (processID == null)
       super.getDebuggeeRelatedArgs(args, engineArgs);
    else
       args.addElement("-qpid=" + processID);
  }

  /**
   * This method will return a string which contains a set of
   * connection-related command line args
   * that can be used when invoking the debug engine. Example:
   * "-qprotocol=tcpip -qhost=smee -quiaddr=8001"
   */

  void getConnectionArgs(Vector args,
                         ConnectionInfo connectionInfo,
                         boolean useDaemon)
  {
    String addr = connectionInfo.getConduit();

    String addressArgFormat = null;

    // TODO: Get all hard-coded strings from the properties file so they can be
    // changed without recompilation:

    if (useDaemon)
    {
       addressArgFormat = "-quiaddr=";   // Tell the engine to connect with
                                          // the daemon

       // If the caller did not specify a conduit, we'll use the daemon's
       // default; C++ PICL does not seem to know what the daemon's default is
       // i.e. the
       // -quiaddr arg cannot be omitted in the same way that the -qaddr
       // arg can (see below).

       if (addr == null)
          addr = DebugDaemon.getDefaultConduit(connectionInfo);
    }
    else
    {
       addressArgFormat = "-qaddr=";     // Tell the engine to sit there and
                                          // wait for us to connect to it.

       // Note that if the caller did not specify a conduit, the engine
       // will use a default - we do not have to specify the -qaddr arg
       // at all.
    }

    if (connectionInfo instanceof TCPIPConnectionInfo)
    {
       args.addElement("-qprotocol=tcpip");

       // If engine is supposed to connect to daemon via tcp/ip, but the
       // connectionInfo does not
       // specify the host where the daemon is running, we'll use
       // "localhost":

       if (useDaemon)
       {
          String hostName = connectionInfo.getHost();

          if (hostName != null)
             args.addElement("-qhost=" + hostName);
          else
             args.addElement("-qhost=localhost");
       }
    }
    else
    if (connectionInfo instanceof NamedPipeConnectionInfo)
    {
       args.addElement("-qprotocol=npipe");
    }
    else
       return;

    if (addr != null)
       args.addElement(addressArgFormat + addr);
  }

  void getQuietArg(Vector args)
  {
    args.addElement("-qquiet");
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
