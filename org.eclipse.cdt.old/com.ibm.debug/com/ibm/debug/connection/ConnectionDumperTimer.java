package com.ibm.debug.connection;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/connection/ConnectionDumperTimer.java, java-connection, eclipse-dev, 20011128
// Version 1.1.1.2 (last modified 11/28/01 16:29:52)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
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
