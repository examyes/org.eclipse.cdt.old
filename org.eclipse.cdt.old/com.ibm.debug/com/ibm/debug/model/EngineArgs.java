package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/EngineArgs.java, java-model, eclipse-dev, 20011128
// Version 1.6.1.2 (last modified 11/28/01 16:13:47)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.util.Vector;

/**
 * Objects of this class provide information regarding arguments that will
 * be passed to a debug engine when it is started.
 * @see Host#loadEngine
 */

public class EngineArgs
{
  /**
   * This ctor is intended to be used when the client wants to launch a
   * debug engine but does NOT want to pass the engine any program name or
   * process id which identifies the debuggee.
   * @param engineShouldConnectToDaemon A value of 'true' indicates that the
   * engine should connect to the UI's daemon, while a value of 'false'
   * indicates that the engine should wait and listen for the UI to connect to
   * it directly.
   * <p>
   * @param debuggeeInterpreterArgs If the <i>debuggee</i> runs inside of an
   * interpreter (or virtual machine), each string in this vector will be passed as an
   * argument to that interpreter. For example, if the debuggee is an
   * interpreted Java application, the strings will be passed to the Java
   * virtual machine in which the Java application runs. If the debuggee
   * does not run inside of an interpreter, this arg will be ignored.
   * <p>NOTE: The strings contained within this vector
   * should contain the "raw" arguments to the debuggee's interpreter - if
   * those arguments need to be passed into the debuggee's interpreter via the
   * debug engine, the Model will automatically take care of wrapping the
   * arguments inside of the appropriate debug engine argument. This may
   * sound confusing, so here's an example: If the args "arg1" and "arg2" are to
   * be passed to the Java interpreter in which a Java debuggee will run,
   * then the debuggeeInterpreterArgs vector should contain exactly
   * two strings: "arg1" and "arg2".
   * When the Model launches the Java debug engine (Java PICL), it will
   * pass these args to the debug engine using the -jvmargs argument i.e.
   * -jvmargs="arg1 arg2", and the debug engine will ensure that the args
   * "arg1" and "arg2" get passed to the JVM in which the debuggee will run.
   * <p>
   * @param engineInterpreterArgs If the <i>debug engine</i> runs inside of an
   * interpreter (or virtual machine), each string in this vector will be passed as-is as an
   * argument to that interpreter. For example, if the debug engine is
   * Java PICL, the strings will be passed to the Java
   * virtual machine in which Java PICL runs. If the engine
   * does not run inside of an interpreter, this arg will be ignored.
   * <p>
   * @param additionalEngineArgs A Vector containing any arbitrary arguments that
   * should be passed to the debug engine when it is started. These args
   * will be passed to the debug engine as-is. Note that several arguments
   * to the debug engine will be manufactured automatically by the Model
   * based on other information that is available at the time that the engine
   * is launched (including other information contained within this EngineArgs
   * object). Any args which will be automatically generated should NOT be
   * specified in the additionalEngineArgs Vector otherwise the args will be
   * passed to the engine twice; the additionalEngineArgs Vector is intended to
   * allow a client of the Model to specify any arguments it wants passed
   * to the engine above and beyond those that are automatically generated.
   */

  public EngineArgs(boolean engineShouldConnectToDaemon,
                    Vector debuggeeInterpreterArgs,
                    Vector engineInterpreterArgs,
                    Vector additionalEngineArgs,
                    boolean verbose)
  {
    _engineShouldConnectToDaemon = engineShouldConnectToDaemon;
    _additionalEngineArgs = additionalEngineArgs;
    _debuggeeInterpreterArgs = debuggeeInterpreterArgs;
    _engineInterpreterArgs = engineInterpreterArgs;
    _verbose = verbose;
  }

  public EngineArgs(boolean engineShouldConnectToDaemon,
                    Vector debuggeeInterpreterArgs,
                    Vector engineInterpreterArgs,
                    Vector additionalEngineArgs,
                    boolean verbose,
                    boolean quiet)
  {
    this(engineShouldConnectToDaemon, debuggeeInterpreterArgs, engineInterpreterArgs, additionalEngineArgs, verbose);

    _quiet = quiet;
  }

  /**
   * Client code should use this constructor when it wants to launch an
   * engine and pass the engine the name of a debuggee as well as the
   * arguments to that debuggee. When this ctor is used, the Model will
   * assume that the client wants the engine to connect to the UI's daemon
   * instead of simply waiting for a connection from the UI.
   */

  public EngineArgs(Vector debuggeeInterpreterArgs,
                    Vector engineInterpreterArgs,
                    Vector additionalEngineArgs,
                    String debuggeeName,
                    String debuggeeArgs,
                    boolean verbose)
  {
    this(true, debuggeeInterpreterArgs, engineInterpreterArgs, additionalEngineArgs, verbose);

    _debuggeeName = debuggeeName;
    _debuggeeArgs = debuggeeArgs;
  }

  /**
   * Client code should use this constructor when it wants to launch an
   * engine and pass the engine a process id for attaching.
   * When this ctor is used, the Model will
   * assume that the client wants the engine to connect to the UI's daemon
   * instead of simply waiting for a connection from the UI.
   */

  public EngineArgs(Vector debuggeeInterpreterArgs,
                    Vector engineInterpreterArgs,
                    Vector additionalEngineArgs,
                    String processID,
                    boolean verbose)
  {
    this(true, debuggeeInterpreterArgs, engineInterpreterArgs, additionalEngineArgs, verbose);

    _processID = processID;
  }

  public EngineArgs(Vector debuggeeInterpreterArgs,
                    Vector engineInterpreterArgs,
                    Vector additionalEngineArgs,
                    RemoteAgentInfo remoteAgentInfo,
                    boolean verbose)
  {
    this(true, debuggeeInterpreterArgs, engineInterpreterArgs, additionalEngineArgs, verbose);

    _remoteAgentInfo = remoteAgentInfo;
  }


  public boolean engineShouldConnectToDaemon()
  {
    return _engineShouldConnectToDaemon;
  }

  public Vector getDebuggeeInterpreterArgs()
  {
    return _debuggeeInterpreterArgs;
  }

  public Vector getEngineInterpreterArgs()
  {
    return _engineInterpreterArgs;
  }

  /**
   * Any additional engine args that were specified when this EngineArgs
   * object was constructed will be added to the argVector Vector.
   */

  public void getAdditionalEngineArgs(Vector argVector)
  {
    if (_additionalEngineArgs != null)
    {
       int numberOfAdditionalArgs = _additionalEngineArgs.size();

       for (int i = 0; i < numberOfAdditionalArgs; i++)
           argVector.addElement(_additionalEngineArgs.elementAt(i));
    }
  }

  public String[] getEngineEnvars()
  {
    return _engineEnvars;
  }

  public boolean getVerbose()
  {
    return _verbose;
  }

  public boolean getQuiet()
  {
    return _quiet;
  }

  public String getProcessID()
  {
    return _processID;
  }

  public String getDebuggeeName()
  {
    if (_debuggeeName != null)
       return _debuggeeName;
    else
       return "";
  }

  public String getDebuggeeArgs()
  {
    if (_debuggeeArgs != null)
       return _debuggeeArgs;
    else
       return "";
  }

  public RemoteAgentInfo getRemoteAgentInfo()
  {
    return _remoteAgentInfo;
  }

  public void setEngineShouldConnectToDaemon(boolean engineShouldConnectToDaemon)
  {
    _engineShouldConnectToDaemon = engineShouldConnectToDaemon;
  }

  public void setDebuggeeInterpreterArgs(Vector debuggeeInterpreterArgs)
  {
    _debuggeeInterpreterArgs = debuggeeInterpreterArgs;
  }

  public void setEngineInterpreterArgs(Vector engineInterpreterArgs)
  {
    _engineInterpreterArgs = engineInterpreterArgs;
  }

  public void setAdditionalEngineArgs(Vector additionalEngineArgs)
  {
    _additionalEngineArgs = additionalEngineArgs;
  }

  public void setEngineEnvars(String[] envars)
  {
    _engineEnvars = envars;
  }

  public void setVerbose(boolean verbose)
  {
    _verbose = verbose;
  }

  public void setQuiet(boolean quiet)
  {
    _quiet = quiet;
  }

  private boolean _engineShouldConnectToDaemon;
  private Vector _debuggeeInterpreterArgs;
  private Vector _engineInterpreterArgs;
  private Vector _additionalEngineArgs;
  private String[] _engineEnvars;
  private boolean _verbose = false;
  private boolean _quiet = false;
  private String _debuggeeName;
  private String _debuggeeArgs;
  private String _processID;
  private RemoteAgentInfo _remoteAgentInfo;
}
