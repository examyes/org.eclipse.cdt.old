package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/StorageAddedEvent.java, java-model, eclipse-dev, 20011128
// Version 1.2.1.2 (last modified 11/28/01 16:12:44)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

public class StorageAddedEvent extends StorageEvent
{
  StorageAddedEvent(Object source,
                    Storage storage,
                    int requestCode,
                    Client client, // The client that caused this event to be generated
                    ModelEventListener privilegedListener)
  {
    super(source, storage, requestCode, client, privilegedListener);
  }

  /**
   * @see ModelEvent#fire(ModelEventListener)
   */

  void fire(ModelEventListener listener)
  {
    ((DebuggeeProcessEventListener)listener).storageAdded(this);

    // When a StorageAddedEvent is vetoed, mark the Storage object as
    // private:

    if (hasBeenVetoed())
       getStorage().setIsPrivate(true);
  }
}
