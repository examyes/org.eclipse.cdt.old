/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// com/ibm/debug/gdb/GdbStorageManager.java, gdb, java-dev
// Version 1.1 (last modified 5/24/01 13:41:54)   (based on Jde 5/1/98 1.43.1.6)
///////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl;
import  com.ibm.debug.gdbPicl.objects.*;

import java.util.*;
import java.io.*;
import com.ibm.debug.epdc.*;

/**
 * Manages the register monitors
 */
public class GdbStorageManager extends StorageManager
{
   public GdbStorageManager(GdbDebugSession debugSession)
   {
      super(debugSession);
   }


}
