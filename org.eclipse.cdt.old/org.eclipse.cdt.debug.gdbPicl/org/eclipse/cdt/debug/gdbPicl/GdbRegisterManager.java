/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

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
