package com.ibm.debug.util;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/util/Assertion.java, java-util, eclipse-dev, 20011128
// Version 1.6.1.2 (last modified 11/28/01 16:32:40)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.lang.RuntimeException;

/**
 * Assertion class provides a similar function as the assert() in C.
 *
 * Usage sample:
 *
 *   if (Assertion.ON)          // to control code generation
 *     Assertion.check(i < j);
 */
public class Assertion extends Object
{
  // constant to control the generation of the assertion code
  // set to false for production release
  public static final boolean ON = true;

  public static void check(boolean assertion) {
    if (!assertion)
      throw new RuntimeException("Assertion failed");
  }

  public static void check(boolean assertion, String description) {
    if (!assertion)
      throw new RuntimeException(description);
  }

  public static void check(Object obj) {
    if (obj == null)
      throw new RuntimeException("Assertion failed");
  }

  public static void check(Object obj, String description) {
    if (obj == null)
      throw new RuntimeException(description);
  }


}
