package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/StorageEvent.java, java-model, eclipse-dev, 20011128
// Version 1.2.1.2 (last modified 11/28/01 16:12:43)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

/**
 * The super class for all Storage events.
 */

public abstract class StorageEvent extends ModelEvent
{
  StorageEvent(Object source,
               Storage storage,
               int requestCode)
  {
    this(source, storage, requestCode, null, null);
  }

  StorageEvent(Object source,
               Storage storage,
               int requestCode,
               Client client, // The client that caused this event to be generated
               ModelEventListener privilegedListener
              )
  {
    super(source, requestCode, client, privilegedListener);
    _storage = storage;
  }

  public Storage getStorage()
  {
    return _storage;
  }

  private Storage _storage;
}
