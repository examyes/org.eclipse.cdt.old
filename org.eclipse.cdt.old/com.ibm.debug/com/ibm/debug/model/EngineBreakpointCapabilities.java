package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/EngineBreakpointCapabilities.java, java-model, eclipse-dev, 20011128
// Version 1.11.1.2 (last modified 11/28/01 16:12:22)
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

public class EngineBreakpointCapabilities extends EngineCapabilitiesGroup
{
  EngineBreakpointCapabilities(EFunctCustTable FCTBits)
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

  public boolean isSameAs(EngineBreakpointCapabilities capabilities)
  {
    if (capabilities == null)
       return false;
    else
       return getBits() == capabilities.getBits();
  }

  public boolean lineBreakpointsSupported()
  {
    return getFCTBits().lineBreakpointsSupported();
  }

  public boolean functionBreakpointsSupported()
  {
    return getFCTBits().functionBreakpointsSupported();
  }

  public boolean addressBreakpointsSupported()
  {
    return getFCTBits().addressBreakpointsSupported();
  }

  public boolean watchpointsSupported()
  {
    return getFCTBits().watchpointsSupported();
  }

  public boolean moduleLoadBreakpointsSupported()
  {
    return getFCTBits().moduleLoadBreakpointsSupported();
  }

  public boolean breakpointEnableDisableSupported()
  {
    return getFCTBits().breakpointEnableDisableSupported();
  }

  public boolean breakpointModifySupported()
  {
    return getFCTBits().breakpointModifySupported();
  }

  public boolean deferredBreakpointsSupported()
  {
    return getFCTBits().deferredBreakpointsSupported();
  }

  public boolean entryBreakpointsAutoSetSupported()
  {
    return getFCTBits().entryBreakpointsAutoSetSupported();
  }

  public boolean conditionalBreakpointsSupported()
  {
    return getFCTBits().conditionalBreakpointsSupported();
  }

  public boolean breakpointThreadsSupported()
  {
    return getFCTBits().breakpointThreadsSupported();
  }

  public boolean breakpointFrequencySupported()
  {
    return getFCTBits().breakpointFrequencySupported();
  }

  public boolean monitor1ByteSupported()
  {
    return getFCTBits().monitor1BytesSupported();
  }

  public boolean monitor2BytesSupported()
  {
    return getFCTBits().monitor2BytesSupported();
  }

  public boolean monitor4BytesSupported()
  {
    return getFCTBits().monitor4BytesSupported();
  }

  public boolean monitor8BytesSupported()
  {
    return getFCTBits().monitor8BytesSupported();
  }

  public boolean monitor0_128BytesSupported()
  {
    return getFCTBits().monitor0_128BytesSupported();
  }

  public boolean dateBreakpointsSupported()
  {
    return getFCTBits().dateBreakpointsSupported();
  }

  public boolean statementBreakpointSupported()
  {
    return getFCTBits().statementBreakpointSupported();
  }

  int getBits()
  {
    return getFCTBits().getBreakpointCapabilities();
  }

  public void print(PrintWriter printWriter)
  {
    if (Model.includePrintMethods)
    {
       printWriter.println("Engine Breakpoint Capabilities:");

       printWriter.println("  Line Breakpoints Supported=" +
			   (lineBreakpointsSupported() ? "true" : "false"));

       printWriter.println("  Function Breakpoints Supported=" +
			   (functionBreakpointsSupported() ? "true" : "false"));

       printWriter.println("  Address Breakpoints Supported=" +
			   (addressBreakpointsSupported() ? "true" : "false"));

       printWriter.println("  Watchpoints Supported=" +
			   (watchpointsSupported() ? "true" : "false"));

       printWriter.println("  Module Load Breakpoints Supported=" +
			   (moduleLoadBreakpointsSupported() ? "true" : "false"));

       printWriter.println("  Breakpoint Enable/Disable Supported=" +
			   (breakpointEnableDisableSupported() ? "true" : "false"));

       printWriter.println("  Breakpoint Modify Supported=" +
			   (breakpointModifySupported() ? "true" : "false"));

       printWriter.println("  Deferred Breakpoints Supported=" +
			   (deferredBreakpointsSupported() ? "true" : "false"));

       printWriter.println("  Conditional Breakpoints Supported=" +
			   (conditionalBreakpointsSupported() ? "true" : "false"));
       printWriter.println("  Breakpoint Threads Supported=" +
                           (breakpointThreadsSupported() ? "true" : "false"));

       printWriter.println(" Breakpoint Frequency Supported=" +
                           (breakpointFrequencySupported() ? "true" : "false"));

       printWriter.println(" Statement Breakpoint Supported=" +
                           (statementBreakpointSupported() ? "true" : "false"));
    }
  }
}
