/*
 * Copyright (c) 1995, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.objects;
import  org.eclipse.cdt.debug.gdbPicl.*;

import com.ibm.debug.epdc.*;
import java.util.*;
import java.text.*;

/**
 * RegistersMonitor.
 */
public class GdbRegistersMonitor extends RegistersMonitor
{
   public GdbRegistersMonitor(DebugSession debugSession, int DU)
   {
      super(debugSession, DU);
   }


}
