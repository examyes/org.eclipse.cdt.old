package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/EngineCapabilitiesGroup.java, java-model, eclipse-dev, 20011128
// Version 1.4.1.2 (last modified 11/28/01 16:12:24)
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
 * This class represents a group of debug engine capabilities. It is subclassed
 * by several classes representing specific kinds of engine capability groups.
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

abstract public class EngineCapabilitiesGroup
{
  EngineCapabilitiesGroup(EFunctCustTable FCTBits)
  {
    _FCTBits = FCTBits;
  }

  protected EFunctCustTable getFCTBits()
  {
    return _FCTBits;
  }

  abstract int getBits();

  abstract public void print(PrintWriter printStream);

  private EFunctCustTable _FCTBits;
}
