package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/EventBreakpoint.java, java-model, eclipse-dev, 20011128
// Version 1.12.1.2 (last modified 11/28/01 16:11:23)
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

public abstract class EventBreakpoint extends Breakpoint
{
  EventBreakpoint(DebuggeeProcess owningProcess, ERepGetNextBkp epdcBkp)
  {
    super(owningProcess, epdcBkp);
  }

  void change(ERepGetNextBkp epdcBkp, boolean isNew)
  {
    super.change(epdcBkp, isNew);
  }

  public void print(PrintWriter printWriter)
  {
    if (Model.includePrintMethods)
    {
      printWriter.println("State: " + (isEnabled() ? "Enabled" : "Disabled"));
      printWriter.println("Thread ID: " + getThreadID());
      printWriter.println("Breakpoint Every: " + getEveryVal());
      printWriter.println("           To: " + getToVal());
      printWriter.println("           From: " + getFromVal());
    }
  }
}
