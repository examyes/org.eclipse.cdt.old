package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/ProcessStopInfo.java, java-model, eclipse-dev, 20011128
// Version 1.3.1.2 (last modified 11/28/01 16:11:42)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

public class ProcessStopInfo
{
  ProcessStopInfo(DebuggeeProcess process,
                  short reason,
                  DebuggeeThread thread,
                  String exceptionMsg,
                  Breakpoint[] breakpoints)
  {
    _process = process;
    _reason = reason;
    _threadThatCausedProcessToStop = thread;
    _exceptionMsg = exceptionMsg;
    _breakpoints = breakpoints;
  }

  /**
   * Determine why the process stopped.
   * @return The reason that the process stopped. The value returned will
   * be one of the named constants in EPDC which represent the reasons that
   * a process might stop.
   * @see com.ibm.debug.epdc.EPDC#Why_none
   */

  public short getReason()
  {
    return _reason;
  }

  /**
   * Query which thread caused the process to stop (because, for example,
   * it hit a breakpoint).
   * @return The thread that caused the process to stop. May return null if,
   * for example, the process ran to completion.
   */

  public DebuggeeThread getThreadThatCausedProcessToStop()
  {
    return _threadThatCausedProcessToStop;
  }

  /**
   * Get that process to which this ProcessStopInfo object applies.
   */

  public DebuggeeProcess getProcess()
  {
    return _process;
  }

  public String getExceptionMsg()
  {
    return _exceptionMsg;
  }

  public Breakpoint[] getBreakpointsHit() {
    return _breakpoints;
  }

  private short _reason;
  private DebuggeeThread _threadThatCausedProcessToStop;
  private DebuggeeProcess _process;
  private String _exceptionMsg;
  private Breakpoint[] _breakpoints;
}
