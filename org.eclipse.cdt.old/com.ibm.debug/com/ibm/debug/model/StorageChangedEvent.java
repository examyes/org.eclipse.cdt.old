package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/StorageChangedEvent.java, java-model, eclipse-dev, 20011128
// Version 1.1.1.2 (last modified 11/28/01 16:13:15)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

public class StorageChangedEvent extends StorageEvent
{
  StorageChangedEvent(Object source, Storage storage, int requestCode)
  {
    super(source, storage, requestCode);
  }

  /**
   * @see ModelEvent#fire(ModelEventListener)
   */

  void fire(ModelEventListener listener)
  {
    ((StorageEventListener)listener).storageChanged(this);
  }

  /**
   * Will return true if the Storage object has been fundamentally rebuilt.
   * In this case the listener should throw away its representation of
   * the storage and rebuild as well using the new contents of the Storage object.
   */

  public boolean rebuild()
  {
    return _rebuild;
  }

  private boolean _rebuild = true;
}
