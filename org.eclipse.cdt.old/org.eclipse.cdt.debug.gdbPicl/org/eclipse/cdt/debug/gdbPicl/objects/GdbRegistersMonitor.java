//////////////////////////////////////////////////////////////////////////////////////////////////////////
//Copyright (C)  1995, 2001 International Business Machines Corporation and others. All Rights Reserved.
//////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.objects;
import  com.ibm.debug.gdbPicl.*;

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
