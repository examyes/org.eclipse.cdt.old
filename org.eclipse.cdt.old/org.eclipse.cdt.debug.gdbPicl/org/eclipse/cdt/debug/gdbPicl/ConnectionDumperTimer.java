package org.eclipse.cdt.debug.gdbPicl;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */


import java.lang.System;

class ConnectionDumperTimer
{
  static long elapsedTime()
  {
    return System.currentTimeMillis() - _startTime;
  }

  static final byte timerFlag = 0x01;

  private static long _startTime = System.currentTimeMillis();
}
