//////////////////////////////////////////////////////////////////////////////////////////////////////////
//Copyright (C)  1995, 2001 International Business Machines Corporation and others. All Rights Reserved.
//////////////////////////////////////////////////////////////////////////////////////////////////////////

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
