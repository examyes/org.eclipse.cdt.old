package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/SaveRestoreFlags.java, java-model, eclipse-dev, 20011128
// Version 1.5.1.2 (last modified 11/28/01 16:13:33)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

public abstract class SaveRestoreFlags
{
  /**
   * In the given set of flags, is the 'BREAKPOINTS' flag on?
   */

  public static boolean breakpointsFlagIsSet(int flags)
  {
    return (flags & BREAKPOINTS) != 0;
  }

  /**
   * In the given set of flags, is the 'PROGRAM_MONITORS' flag on?
   */

  public static boolean programMonitorsFlagIsSet(int flags)
  {
    return (flags & PROGRAM_MONITORS) != 0;
  }

  /**
   * In the given set of flags, is the 'STORAGE' flag on?
   */

  public static boolean storageFlagIsSet(int flags)
  {
    return (flags & STORAGE) != 0;
  }

  /**
   * In the given set of flags, is the 'DEFAULT_DATA_REPRESENTATIONS' flag on?
   */

  public static boolean defaultDataRepresentationsFlagIsSet(int flags)
  {
    return (flags & DEFAULT_DATA_REPRESENTATIONS) != 0;
  }

  /**
   * In the given set of flags, is the 'EXCEPTION_FILTERS' flag on?
   */

  public static boolean exceptionFiltersFlagIsSet(int flags)
  {
    return (flags & EXCEPTION_FILTERS) != 0;
  }

  /**
   * When this flag is used, all non-static, non-transient objects
   * will be saved i.e. the entire graph. This flag overrides all others
   * i.e. if the other more specific flags are set, they will be
   * ignored.
   */

  static final int ALL_OBJECTS = 0x80000000;

  /**
   * This flag is used to save only objects in the graph which are
   * "restorable" (as well as any objects that they might depend on).
   * Examples of "restorable" objects are: breakpoints, expression monitors,
   * storage monitors, etc. By saving such objects, they can be reinstated
   * when the debugger is started (or restarted) on the same debuggee so that
   * the user does not have to explicitly set them up again.
   * This flag will be ignored if the ALL_OBJECTS flag is also set.
   */

  static final int RESTORABLE_OBJECTS = 0x40000000;

  /**
   * Save or restore all breakpoints.
   */

  public static final int BREAKPOINTS = 0x20000000;

  /**
   * Save or restore all program-level monitored expressions.
   */

  public static final int PROGRAM_MONITORS = 0x10000000;

  /**
   * Save or restore all local variable monitors.
   */

  public static final int LOCAL_MONITORS = 0x08000000;

  /**
   * Save or restore all storage monitors.
   */

  public static final int STORAGE = 0x04000000;

  public static final int DEFAULT_DATA_REPRESENTATIONS = 0x02000000;

  public static final int EXCEPTION_FILTERS = 0x01000000;

  /**
   * Automatically save the specified objects every time a round of Model
   * updates has completed.
   */

  public static final int AUTOSAVE = 0x00000001;
         static final int SECONDARY_RESTORE = 0x00000002;
}
