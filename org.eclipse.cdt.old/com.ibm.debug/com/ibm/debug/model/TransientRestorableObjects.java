package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/TransientRestorableObjects.java, java-model, eclipse-dev, 20011128
// Version 1.1.1.2 (last modified 11/28/01 16:13:32)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

/**
 * A class which can be used to save and restore "restorable" Model
 * objects e.g. breakpoints, monitored exprs, etc. When this class is
 * used, the Model objects will be saved transiently i.e. in a buffer.
 * @see PersistentRestorableObjects
 */

public class TransientRestorableObjects extends RestorableObjects
{
  /**
   * @param process The process into which objects will be restored and
   * from which objects will be saved. This can be changed by calling
   * setProcess.
   * @param flags Flags which control the saving and restoring of objects.
   * Initially, both the save flags and the restore flags are set to the
   * value of the 'flags' parameter and are therefore the same.
   * Both sets of flags can be changed by calling setSaveFlags and setRestoreFlags.
   */

  public TransientRestorableObjects(DebuggeeProcess process, int flags)
  {
    super(process,
          new TransientSerialization(flags | SaveRestoreFlags.RESTORABLE_OBJECTS),
          flags);
  }
}
