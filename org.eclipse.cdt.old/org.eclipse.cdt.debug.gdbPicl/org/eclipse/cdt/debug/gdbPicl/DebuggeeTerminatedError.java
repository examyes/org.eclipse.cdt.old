/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

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
