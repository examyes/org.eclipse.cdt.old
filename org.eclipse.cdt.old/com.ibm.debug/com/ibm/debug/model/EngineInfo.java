package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/EngineInfo.java, java-model, eclipse-dev, 20011128
// Version 1.17.1.2 (last modified 11/28/01 16:13:42)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.connection.*;
import java.util.Vector;

/**
 * The main purpose of this class is to provide information regarding
 * the different types of debug engines that can be launched from the
 * Model.
 * @see Host#loadEngine
 * @see Host#getExistingDebugEngine
 * @see JavaPICLEngineInfo
 * @see CPlusPlusPICLEngineInfo
 */

public abstract class EngineInfo
{
  EngineInfo(String engineName, byte engineFormat)
  {
    _engineName = engineName;
    _engineFormat = engineFormat;
  }

  abstract short getEPDCEngineID();

  /**
   * This method will return a string which contains a set of
   * connection-related command line args
   * that can be used when invoking the debug engine. Example:
   * "-qprotocol=tcpip -qhost=smee -quiaddr=8001"
   * <p>This method should be implemented by any subclass which supports
   * connection-related command line args. The default implementation
   * does nothing.
   */

  void getConnectionArgs(Vector args,
                         ConnectionInfo connectionInfo,
                         boolean useDaemon)
  {
  }

  /**
   * Get a string containing the command that can be used to invoke this
   * engine. The default implementation of this method simply returns
   * the name of the engine as determined by the getEngineName method.
   * <p>Engines which are not executable (e.g. engine comes packaged
   * only as a .dll, not an .exe) should provide an implementation
   * of this method which returns null. Engines which can only be run
   * within some interpreter (e.g. the Java Virtual Machine) should return
   * a string which includes the name of the interpreter e.g.
   * "java engineClassName" as opposed to just "engineClassName".
   */

  void getBaseInvocationCommand(Vector commandVector,
                                ProductInfo productInfo,
                                boolean verbose,
                                boolean quiet,
                                Vector debuggeeInterpreterArgs,
                                Vector engineInterpreterArgs)
  {
    commandVector.addElement(getEngineName(productInfo));

    if (verbose)
       getVerboseArg(commandVector);

    if (quiet)
       getQuietArg(commandVector);

    getDebuggeeInterpreterArgsArg(commandVector, debuggeeInterpreterArgs);
  }

  String[] getCompleteInvocationCommand(ProductInfo productInfo,
                                        EngineArgs engineArgs,
                                        ConnectionInfo connectionInfo,
                                        byte platformID)
  {
    Vector commandVector = new Vector();

    getBaseInvocationCommand(commandVector,
                             productInfo,
                             engineArgs.getVerbose(),
                             engineArgs.getQuiet(),
                             engineArgs.getDebuggeeInterpreterArgs(),
                             engineArgs.getEngineInterpreterArgs());

    getConnectionArgs(commandVector,
                      connectionInfo,
                      engineArgs.engineShouldConnectToDaemon());

    engineArgs.getAdditionalEngineArgs(commandVector);

    getDebuggeeRelatedArgs(commandVector, engineArgs);

    String[] command = null;
    int vectorSize = commandVector.size();

    // If the engine is not being run within a shell or console window,
    // simply copy the invocation command to the array, otherwise,
    // get a command array which already has the shell invocation in the
    // the first 1 or more elements, then add our invocation command as
    // the last element i.e. the thing the shell is to exec:

    if ((command = getShellCommand(platformID, productInfo)) == null)
    {
       command = new String[vectorSize];
       commandVector.copyInto(command);

       if (Model.TRACE.EVT && Model.traceInfo())
          Model.TRACE.evt(1, "Engine invocation cmd: " + commandVector.toString());
    }
    else
    {
       int lastArrayElement = command.length - 1;

       for (int i = 0; i < vectorSize; i++)
           command[lastArrayElement] += commandVector.elementAt(i) + " ";

       if (Model.TRACE.EVT && Model.traceInfo())
       {
          String shellCommand = "";

          for (int i = 0; i <= lastArrayElement; i++)
              shellCommand += command[i] + " ";

          Model.TRACE.evt(1, "Engine invocation cmd: " + shellCommand);
       }
    }

    return command;
  }

  String[] getShellCommand(byte platformID, ProductInfo productInfo)
  {
    return null;
  }

  void getDebuggeeRelatedArgs(Vector args, EngineArgs engineArgs)
  {
    String debuggeeName = engineArgs.getDebuggeeName();

    if (debuggeeName != null && !debuggeeName.equals(""))
    {
       args.addElement(debuggeeName);

       String debuggeeArgs = engineArgs.getDebuggeeArgs();

       if (debuggeeArgs != null && !debuggeeArgs.equals(""))
          args.addElement(debuggeeArgs);
    }
  }

  void getVerboseArg(Vector args)
  {
  }

  void getQuietArg(Vector args)
  {
  }

  String getEngineWindowTitle()
  {
    return Model.getResourceString("EngineWindowTitle");
  }

  void getDebuggeeInterpreterArgsArg(Vector args, Vector debuggeeInterpreterArgs)
  {
  }

  /**
   * @return true if this engine can be invoked via the java.lang.Runtime.exec
   * method, false otherwise. Engines which run within some kind of interpreter
   * (e.g. the Java Virtual Machine) should return true if the interpreter
   * can be execed.
   */

  abstract boolean canBeExeced();

  /**
   * @return true if this engine is a library which can be loaded via the
   * java.lang.System.load method, false otherwise.
   */

  abstract boolean canBeLoaded();

  /**
   * Can this engine communicate using TCP/IP?
   */

  public abstract boolean supportsTCPIPConnections();

  /**
   * Can this engine communicate using APPC?
   */

  public abstract boolean supportsAPPCConnections();

  /**
   * Can this engine communicate using a named pipe?
   */

  public abstract boolean supportsNamedPipeConnections();

  /**
   * @return true if this engine can be started from a shell in a different
   * window and false otherwise.
   */
  abstract boolean canStartEngineFromShell();

  /**
   * Can this engine connect to the UI's daemon?
   * @return 'true' if the engine can be told to connect to the UI's
   * daemon upon startup, otherwise 'false'.
   * <p>The default implementation of this method returns 'true'. Engines
   * which cannot connect to the UI's daemon will override this method and
   * and return 'false'.
   */

  public boolean canConnectToUIDaemon()
  {
    return true;
  }

  /**
   * Can this engine wait for a connection from the UI?
   * @return 'true' if the engine can be told to wait for a connection from
   * the UI at startup, otherwise 'false'.
   * <p>The default implementation of this method returns 'true'. Engines
   * which cannot wait for a connection from the UI will override this method and
   * and return 'false'.
   */

  public boolean canWaitForConnection()
  {
    return true;
  }

  /**
   * The default implementation of this method simply returns the name of the
   * engine (as specified when the engine was constructed), prefixed by
   * the given product prefix and fully qualified by the given install
   * directory. This method should be overridden for those
   * engines where this default return value is not appropriate.
   */

  String getEngineName(ProductInfo productInfo)
  {
  /* Defect 14170 - no need for prefix since we use the wrapper
     irmtdbgc.
   */
    String engineName = _engineName;
    if (_userSpecified) {
       String productPrefix = productInfo.getProductPrefix();

       if (productPrefix != null)
         engineName = productPrefix + engineName;
    }
    return getEngineDirectory(productInfo) + engineName;
  }

  protected static String getEngineDirectory(ProductInfo productInfo)
  {
    String installDirectory = System.getProperty("INSTALL_DIR");

    // If the install directory was not specified by the INSTALL_DIR property,
    // see if it has been specified by the client of the Model:

    if (installDirectory == null)
       installDirectory = productInfo.getInstallDirectory();

    // We get the engine directory by adding "bin" to the install directory:

    if (installDirectory != null)
    {
       String fileSeparator = System.getProperty("file.separator");

       if (!installDirectory.endsWith(fileSeparator))
          installDirectory += fileSeparator;

       return installDirectory + "bin" + fileSeparator;
    }
    else
       return "";
  }

  protected String _engineName;
  protected byte _engineFormat;
  protected boolean _userSpecified = false;

  /**
   * Engine is an executable.
   */

  public static final byte EXECUTABLE = 0;

  /**
   * Engine is a DLL or shared library.
   */

  public static final byte LOADABLE_LIBRARY = 1;

  /**
   * Engine is a Java class.
   */

  public static final byte JAVA_CLASS = 2;
}
