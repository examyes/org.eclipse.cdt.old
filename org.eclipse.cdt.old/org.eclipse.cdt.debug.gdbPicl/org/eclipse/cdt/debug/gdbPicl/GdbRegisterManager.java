//////////////////////////////////////////////////////////////////////////////////////////////////////////
//Copyright (C)  1995, 2001 International Business Machines Corporation and others. All Rights Reserved.
//////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl;
import  com.ibm.debug.gdbPicl.objects.*;

import java.util.*;
import java.io.*;
import com.ibm.debug.epdc.*;

/**
 * Manages the register monitors
 */
public class GdbRegisterManager extends RegisterManager
{
   public GdbRegisterManager(GdbDebugSession debugSession)
   {
      super(debugSession);
   }


}
