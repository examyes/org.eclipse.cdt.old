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
// Version %I% (last modified %G% %U%)   (based on Jde 1.1 2/23/01)
/////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl;

import java.lang.*;

/**
 * This error is thrown when the debuggee is terminated.
 */
public class DebuggeeTerminatedError extends Error
{
  public DebuggeeTerminatedError()
  {
    super();
  }

  public DebuggeeTerminatedError(String message)
  {
    super(message);
  }
}
