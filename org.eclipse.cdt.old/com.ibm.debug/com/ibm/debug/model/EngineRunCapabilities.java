package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/EngineRunCapabilities.java, java-model, eclipse-dev, 20011128
// Version 1.10.1.2 (last modified 11/28/01 16:12:19)
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

public class EngineRunCapabilities extends EngineCapabilitiesGroup
{
  EngineRunCapabilities(EFunctCustTable FCTBits)
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

  public boolean isSameAs(EngineRunCapabilities capabilities)
  {
    if (capabilities == null)
       return false;
    else
       return getBits() == capabilities.getBits();
  }

   public boolean stepOverSupported()
   {
     return getFCTBits().stepOverSupported();
   }

   public boolean stepIntoSupported()
   {
     return getFCTBits().stepIntoSupported();
   }

   public boolean stepDebugSupported()
   {
     return getFCTBits().stepDebugSupported();
   }

   public boolean stepReturnSupported()
   {
     return getFCTBits().stepReturnSupported();
   }

   public boolean runToLocationSupported()
   {
     return getFCTBits().runToLocationSupported();
   }

   public boolean jumpToLocationSupported()
   {
     return getFCTBits().jumpToLocationSupported();
   }

   public boolean threadFreezeThawSupported()
   {
     return getFCTBits().threadFreezeThawSupported();
   }

   public boolean haltSupported()
   {
     return getFCTBits().haltSupported();
   }

   public boolean storageUsageCheckSupported()
   {
     return getFCTBits().storageUsageCheckSupported();
   }

  int getBits()
  {
    return getFCTBits().getRunCapabilities();
  }

  public void print(PrintWriter printWriter)
  {
    if (Model.includePrintMethods)
    {
       printWriter.println("Engine Run Capabilities:");

       printWriter.println("  Step Over Supported=" +
                           (stepOverSupported() ? "true" : "false"));

       printWriter.println("  Step Into Supported=" +
                           (stepIntoSupported() ? "true" : "false"));

       printWriter.println("  Step Return Supported=" +
                           (stepReturnSupported() ? "true" : "false"));

       printWriter.println("  Step Debug Supported=" +
                           (stepDebugSupported() ? "true" : "false"));

       printWriter.println("  Run To Location Supported=" +
                           (runToLocationSupported() ? "true" : "false"));

       printWriter.println("  Jump To Location Supported=" +
                           (jumpToLocationSupported() ? "true" : "false"));

       printWriter.println("  Thread Freeze/Thaw Supported=" +
                           (threadFreezeThawSupported() ? "true" : "false"));
    }
  }
}
