package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/EngineCapabilities.java, java-model, eclipse-dev, 20011128
// Version 1.7.1.2 (last modified 11/28/01 16:12:12)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.epdc.*;
import java.io.*;

/**
 * This class corresponds directly to what are called "FCT bits" in EPDC -
 * it contains information regarding the capabilities of a given debug
 * engine. These capabilities can change during a debug session and a UI
 * must be prepared to dynamically enable/disable user function accordingly so that it
 * takes advantage of the full capabilities of the debug engine but
 * never asks it to do something that it is not capable of doing. Client code
 * is notified of changes in the capabilities of a debug via the event
 * listener mechanism.
 * <p>
 * Engine capabilities are divided into groups. There are currently 10 groups:
 * <ol>
 * <li>Startup capabilities
 * <li>General capabilities
 * <li>File capabilities
 * <li>Storage capabilities
 * <li>Breakpoint capabilities
 * <li>Monitor capabilities
 * <li>Window capabilities
 * <li>Run capabilities
 * <li>Exception capabilities
 * <li>Stack capabilities
 * </ol>
 * This class contains methods which allow client code to retrieve groups of
 * capabilities. For every group of capabilities the Model contains a class
 * which represents that group and each class contains methods for querying the
 * individual capabilities within the group.
 * @see DebugEngine#getCapabilities
 * @see EngineCapabilitiesGroup
 * @see DebugEngineEventListener#engineCapabilitiesChanged
 * @see    EngineStartupCapabilities
 * @see    EngineGeneralCapabilities
 * @see    EngineFileCapabilities
 * @see    EngineStorageCapabilities
 * @see    EngineBreakpointCapabilities
 * @see    EngineMonitorCapabilities
 * @see    EngineWindowCapabilities
 * @see    EngineRunCapabilities
 * @see    EngineExceptionCapabilities
 * @see    EngineStackCapabilities
 */

public class EngineCapabilities
{
  EngineCapabilities(DebugEngine owningEngine, EFunctCustTable FCTBits)
  {
    _owningEngine = owningEngine;
    _startupCapabilities = new EngineStartupCapabilities(FCTBits);
    _generalCapabilities = new EngineGeneralCapabilities(FCTBits);
    _fileCapabilities = new EngineFileCapabilities(FCTBits);
    _storageCapabilities = new EngineStorageCapabilities(FCTBits);
    _breakpointCapabilities = new EngineBreakpointCapabilities(FCTBits);
    _monitorCapabilities = new EngineMonitorCapabilities(FCTBits);
    _windowCapabilities = new EngineWindowCapabilities(FCTBits);
    _runCapabilities = new EngineRunCapabilities(FCTBits);
    _exceptionCapabilities = new EngineExceptionCapabilities(FCTBits);
    _stackCapabilities = new EngineStackCapabilities(FCTBits);
  }

  public DebugEngine getOwningEngine()
  {
    return _owningEngine;
  }

  public EngineStartupCapabilities getStartupCapabilities()
  {
    return _startupCapabilities;
  }

  public EngineGeneralCapabilities getGeneralCapabilities()
  {
    return _generalCapabilities;
  }

  public EngineFileCapabilities getFileCapabilities()
  {
    return _fileCapabilities;
  }

  public EngineStorageCapabilities getStorageCapabilities()
  {
    return _storageCapabilities;
  }

  public EngineBreakpointCapabilities getBreakpointCapabilities()
  {
    return _breakpointCapabilities;
  }

  public EngineMonitorCapabilities getMonitorCapabilities()
  {
    return _monitorCapabilities;
  }

  public EngineWindowCapabilities getWindowCapabilities()
  {
    return _windowCapabilities;
  }

  public EngineRunCapabilities getRunCapabilities()
  {
    return _runCapabilities;
  }

  public EngineExceptionCapabilities getExceptionCapabilities()
  {
    return _exceptionCapabilities;
  }

  public EngineStackCapabilities getStackCapabilities()
  {
    return _stackCapabilities;
  }

  public void print(PrintWriter printWriter)
  {
    if (Model.includePrintMethods)
    {
       _startupCapabilities.print(printWriter);
       _generalCapabilities.print(printWriter);
       _fileCapabilities.print(printWriter);
       _storageCapabilities.print(printWriter);
       _breakpointCapabilities.print(printWriter);
       _monitorCapabilities.print(printWriter);
       _windowCapabilities.print(printWriter);
       _runCapabilities.print(printWriter);
       _exceptionCapabilities.print(printWriter);
       _stackCapabilities.print(printWriter);
    }
  }

  private DebugEngine _owningEngine;

  private EngineStartupCapabilities _startupCapabilities;
  private EngineGeneralCapabilities _generalCapabilities;
  private EngineFileCapabilities _fileCapabilities;
  private EngineStorageCapabilities _storageCapabilities;
  private EngineBreakpointCapabilities _breakpointCapabilities;
  private EngineMonitorCapabilities _monitorCapabilities;
  private EngineWindowCapabilities _windowCapabilities;
  private EngineRunCapabilities _runCapabilities;
  private EngineExceptionCapabilities _exceptionCapabilities;
  private EngineStackCapabilities _stackCapabilities;
}
