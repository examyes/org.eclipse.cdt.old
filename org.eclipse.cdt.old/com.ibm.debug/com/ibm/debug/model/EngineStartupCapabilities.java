package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/EngineStartupCapabilities.java, java-model, eclipse-dev, 20011128
// Version 1.7.1.2 (last modified 11/28/01 16:12:13)
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

public class EngineStartupCapabilities extends EngineCapabilitiesGroup
{
  EngineStartupCapabilities(EFunctCustTable FCTBits)
  {
    super(FCTBits);
  }

  /**
   * Compare one set of capabilities with another to see if they are the
   * same. This is typically used to find out if a given set of capabilities
   * has changed when an EngineCapabilitiesChangedEvent is fired. This event
   * contains the old set of engine capabilities as well as the new one.
   * Calling isSameAs to compare the old set of capabilities with the new one
   * allows client code to find out what has changed on
   * less granular basis than comparing individual capabilities. If a set of
   * capabilities has changed then the client may want to compare individual
   * capabilities within that set to see exactly what has changed.
   */

  public boolean isSameAs(EngineStartupCapabilities capabilities)
  {
    if (capabilities == null)
       return false;
    else
       return getBits() == capabilities.getBits();
  }

  public boolean debugInitializationSupported()
  {
    return getFCTBits().debugInitializationSupported();
  }

  int getBits()
  {
    return getFCTBits().getStartupCapabilities();
  }

  public void print(PrintWriter printWriter)
  {
    if (Model.includePrintMethods)
    {
       printWriter.println("Engine Startup Capabilities:");
    }
  }
}
