package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/DebuggeePrepareOptions.java, java-model, eclipse-dev, 20011128
// Version 1.7.1.2 (last modified 11/28/01 16:13:36)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

/**
 * Objects of this class are passed to DebuggeeProcess.prepareProgram in order
 * to tell the Model how to control the startup of the debuggee.
 * @see DebugEngine#prepareProgram
 */

public class DebuggeePrepareOptions extends DebuggeeStartupOptions
{
 /**
  * @param debuggeeName The name of the program to load.
  * @param debuggeeArguments A String containing the arguments that are to be
  * passed to the debuggee. May be null if there are no arguments.
  * @param saveRestoreFlags A set of flags which control how Model objects are
  * to be saved and restored for the program being debugged.
  * @param saveRestoreDirectory The name of a directory which will be used to save
  * and restore Model objects.
  * @param runToMainEntryPoint If 'true', the debuggee will be run to its
  * main entry point after being loaded. If 'false', the Model will not
  * automatically run the debuggee to its main entry point, but client code
  * can do so by calling DebuggeeProcess.runToMainEntryPoint.
  * <p><b>N.B.:</b> Unexpected results may occur if this arg is 'false'
  * (meaning do <i>not</i> run the debuggee to its main entry point) but the
  * debug engine has indicated that it does not support debugging
  * initialization code (i.e. the code prior to the main entry point). This
  * engine capability can be checked by calling
  * EngineStartupCapabilities.debugInitializationSupported(). If this
  * method returns 'false' then it is strongly recommended that client code
  * construct the DebuggeePrepareOptions object with a value of 'true'
  * for the runToMainEntryPoint arg. <b>In a future driver, the Model
  * will enforce this rule and will reject the "prepareProgram" request
  * if the client attempts to debug initialization code and the engine
  * does not support it.</b>
  * @param restoreSavedObjects If 'true', objects that were saved in the
  * directory 'saveRestoreDirectory' will be restored automatically after starting
  * the debuggee. Exactly when this will be done depends on the value of the
  * runToMainEntryPoint arg: If this arg is 'false', objects will be
  * restored after the program is loaded, but if it is 'true', objects will
  * be restored after running the debuggee to its main entry point.
  * @param executeAfterPrepare If 'true', the Model will let the debuggee
  * run after it is loaded. (Note that it will do this asynchronously.) If this
  * argument is 'true', the runToMainEntryPoint argument will be ignored.
  * @param jobName Job name to debug (AS400 only)
  * @param dominantLanguage The dominant language the user has selected
  * @see DebuggeeProcess#runToMainEntryPoint
  * @see DebuggeeProcess#restoreSavedObjects
  * @see SaveRestoreFlags
  */
  public DebuggeePrepareOptions(String debuggeeName,
                                String debuggeeArguments,
                                int saveRestoreFlags,
                                String saveRestoreDirectory,
                                boolean runToMainEntryPoint,
                                boolean restoreSavedObjects,
                                boolean executeAfterPrepare,
                                String jobName,
                                byte dominantLanguage)
  {
    super(debuggeeName,
          saveRestoreFlags,
          saveRestoreDirectory,
          restoreSavedObjects,
          executeAfterPrepare);

    _debuggeeArguments   = debuggeeArguments;
    _runToMainEntryPoint = runToMainEntryPoint;
    _jobName             = jobName;
    _dominantLanguage    = dominantLanguage;
  }

  /**
   * @deprecated Use the above version instead
   */

  public DebuggeePrepareOptions(String debuggeeName,
                                String debuggeeArguments,
                                int saveRestoreFlags,
                                String saveRestoreDirectory,
                                boolean runToMainEntryPoint,
                                boolean restoreSavedObjects,
                                String jobName)
  {
    super(debuggeeName,
          saveRestoreFlags,
          saveRestoreDirectory,
          restoreSavedObjects,
          false);

    _debuggeeArguments = debuggeeArguments;
    _runToMainEntryPoint = runToMainEntryPoint;
    _jobName             = jobName;
  }

  public String getDebuggeeArguments()
  {
    return _debuggeeArguments;
  }

  /**
   * Determine if the debuggee will be run to its main entry point after
   * it has been loaded.
   */

  public boolean runToMainEntryPoint()
  {
    return _runToMainEntryPoint;
  }

  public boolean executeAfterPrepare()
  {
    return executeAfterStartup();
  }

  public String getJobName()
  {
    return _jobName;
  }

  public byte getDominantLanguage()
  {
    return _dominantLanguage;
  }

  private String _debuggeeArguments;
  private boolean _runToMainEntryPoint;
  private String _jobName;
  private byte _dominantLanguage;
}
