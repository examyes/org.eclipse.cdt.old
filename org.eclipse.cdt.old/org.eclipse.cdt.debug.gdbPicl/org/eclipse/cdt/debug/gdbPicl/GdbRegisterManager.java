/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// %W%
// Version %I% (last modified %G% %U%)   (based on Jde 5/1/98 1.43.1.6)
///////////////////////////////////////////////////////////////////////

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
