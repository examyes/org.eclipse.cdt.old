package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/JavaPICLEngineInfo.java, java-model, eclipse-dev, 20011128
// Version 1.25.1.2 (last modified 11/28/01 16:13:44)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.connection.*;
import com.ibm.debug.util.*;
import com.ibm.debug.epdc.EPDC;
import java.util.Vector;

/**
 * This class provides information that is needed
 * in order to load or reuse a debug engine of this type.
 * @see Host#loadEngine
 * @see Host#getExistingDebugEngine
 */

public class JavaPICLEngineInfo extends EngineInfo
{
  /**
   * This ctor will give the Java PICL engine a default name of
   * "com.ibm.debug.engine.Jde".
   * The format of this kind of engine will always be
   * EngineInfo.JAVA_CLASS
   */

  public JavaPICLEngineInfo()
  {
    // TODO: Get the name from the properties file:

    //super("com.ibm.debug.engine.Jde", EngineInfo.JAVA_CLASS);
  /**
  * The default is to use the wrapper 'irmtdbgj' to invoke
  * the Java PICL engine - defect 14108
  */
    super("irmtdbgj", EngineInfo.EXECUTABLE);
  }

  /**
   * @param engineName The name of the Java class which implements
   * Java PICL.
   * The format of this kind of engine will always be
   * EngineInfo.JAVA_CLASS
   */

  public JavaPICLEngineInfo(String engineName)
  {
    super(engineName, EngineInfo.JAVA_CLASS);
  }

  short getEPDCEngineID()
  {
    return EPDC.BE_TYPE_JAVA_PICL;
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
   * Returns 'false' (for now).
   */

  public boolean supportsNamedPipeConnections()
  {
    return false;
  }

  String[] getShellCommand(byte platformID, ProductInfo productInfo)
  {
    if (!canStartEngineFromShell())
        return null;

    String[] shellCommand = null;
    String engineWindowTitle = getEngineWindowTitle();

    // TODO: Use engine window title on all platforms.
    // TODO: Let client of Model specify title for engine window.

    ExecCommand cmd = new ExecCommand("", engineWindowTitle, " ", false);
    shellCommand = cmd.getShellCommand();

    return shellCommand;
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
    // Currently, Java PICL only supports tcp/ip connections:

    if (!(connectionInfo instanceof TCPIPConnectionInfo))
       return;

    // TODO: Get all hard-coded strings from the properties file so they can be
    // changed without recompilation:

    String addr = connectionInfo.getConduit();

    String addressArgFormat = null;

    // If engine is supposed to connect to daemon but the
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

       addressArgFormat = "-quiport=";   // Tell the engine to connect with
                                          // the daemon

       // Note that if the caller did not specify a conduit, the engine
       // will use a default - we do not have to specify the -quiport arg
       // at all.
    }
    else
    {
       addressArgFormat = "-qport=";     // Tell the engine to sit there and
                                          // wait for us to connect to it.

       // Note that if the caller did not specify a conduit, the engine
       // will use a default - we do not have to specify the -qport arg
       // at all.
    }

    // If the connectionInfo has a non-null conduit, use it, o.w. the
    // engine will use a default:

    if (addr != null)
       args.addElement(addressArgFormat + addr);
  }

  void getBaseInvocationCommand(Vector commandVector,
                                ProductInfo productInfo,
                                boolean verbose,
                                boolean quiet,
                                Vector debuggeeInterpreterArgs,
                                Vector engineInterpreterArgs)
  {
    if (this._engineFormat == JAVA_CLASS)
    {
      commandVector.addElement("java");

      // Add each of the engine interpreter args to the vector:

      if (engineInterpreterArgs != null)
      {
        int numberOfEngineInterpreterArgs = engineInterpreterArgs.size();

        for (int i = 0; i < numberOfEngineInterpreterArgs; i++)
           commandVector.addElement(engineInterpreterArgs.elementAt(i));
      }
    }

    super.getBaseInvocationCommand(commandVector,
                                   productInfo,
                                   verbose,
                                   quiet,
                                   debuggeeInterpreterArgs,
                                   engineInterpreterArgs);
  }

  void getDebuggeeInterpreterArgsArg(Vector args, Vector debuggeeInterpreterArgs)
  {
    // Unlike the engine interpreter args where we added each arg to the
    // vector individually, we'll take the debuggeeInterpreterArgs and
    // merge them into a single string, then we'll give that string to Java
    // PICL in its -jvmargs arg:

    int numberOfDebuggeeInterpreterArgs = 0;

    if (debuggeeInterpreterArgs != null &&
        (numberOfDebuggeeInterpreterArgs = debuggeeInterpreterArgs.size()) != 0)
    {
       String debuggeeInterpreterArgsString = "";

       for (int i = 0; i < numberOfDebuggeeInterpreterArgs; i++)
       {
           debuggeeInterpreterArgsString += (String)debuggeeInterpreterArgs.elementAt(i);

           if (i < numberOfDebuggeeInterpreterArgs - 1)
              debuggeeInterpreterArgsString += " ";
       }
       if (Platform.isWindows())
         args.addElement("-jvmargs="+QuoteUtil.enquoteIfNecessary(debuggeeInterpreterArgsString));
       else
         args.addElement("-jvmargs=\"" + debuggeeInterpreterArgsString + "\"");
    }
  }

  void getDebuggeeRelatedArgs(Vector args, EngineArgs engineArgs)
  {
    // If the engineArgs contains a RemoteAgentInfo object then we'll
    // tell the engine to attach using the information contained therein:

    RemoteAgentInfo remoteAgentInfo = engineArgs.getRemoteAgentInfo();

    if (remoteAgentInfo == null)
       super.getDebuggeeRelatedArgs(args, engineArgs);
    else
    {
       args.addElement("-host=" + remoteAgentInfo.getHost());
       args.addElement("-password=" + remoteAgentInfo.getPassword());
    }
  }

  void getVerboseArg(Vector args)
  {
    args.addElement("-dbg");

       String jt_engine = System.getProperty("JT_ENGINE");
       if (jt_engine!=null)
       {  args.addElement(" +DJT_EVT="+String.valueOf(Model.TRACE.getEventLevel()) );
          String jt_log = System.getProperty("JT_LOG");
          if (jt_log!=null)
          {   args.addElement(" +DJT_LOG" );
          }
          String jt_host = System.getProperty("JT_HOST");
          if (jt_host!=null)
          {   args.addElement(" +DJT_HOST="+jt_host );
              String jt_port = System.getProperty("JT_PORT");
              if (jt_port!=null)
              {   args.addElement(" +DJT_PORT="+jt_port );
              }
          }
       }
  }

  void getQuietArg(Vector args)
  {
    args.addElement("-qquiet");
  }

  /**
   * Product prefix is ignored.
   */

  String getEngineName(ProductInfo productInfo)
  {
    return _engineName;
  }

  /**
   * Returns true (because the interpreter for this engine, namely the
   * Java Virtual Machine, can be execed).
   */

  boolean canBeExeced()
  {
    return true;
  }

  /**
   * Returns false.
   */

  boolean canBeLoaded()
  {
    return false;
  }

  boolean canStartEngineFromShell()
  {
    return true;
  }
}
