package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/ModelEventAdapter.java, java-model, eclipse-dev, 20011128
// Version 1.1.1.2 (last modified 11/28/01 16:13:58)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

/**
 * The purpose of this class is to provide default implementations for the
 * getClient and filterEvents methods.
 */

public abstract class ModelEventAdapter implements ModelEventListener
{
   /**
    * Construct an event adapter with no specified owning client.
    * Note that the owning client can be set after construction by
    * calling setClient.
    * @see ModelEventAdapter#getClient
    * @see ModelEventAdapter#setClient
    * @see ModelEventAdapter#filterEvents
    */

  public ModelEventAdapter()
  {
  }

   /**
    * Construct an event adapter which is owned by the specified client.
    * The client can be retrieved later by calling getClient. If no
    * owning client is specified when the adapter is constructed, then
    * getClient should be overidden in a subclass to return a Client
    * object by some other means. If getClient does not return a valid
    * Client object then no event filtering is possible for this
    * event adapter i.e. it will receive all events (for which it is
    * listening) regardless of which client generated the event.
    * <p>
    * Note that the owning client can also be set after construction by
    * calling setClient.
    * @see ModelEventAdapter#getClient
    * @see ModelEventAdapter#setClient
    * @see ModelEventAdapter#filterEvents
    */

  public ModelEventAdapter(Client client)
  {
    _client = client;
  }

  /**
   * If this adapter was constructed with a particular Client object or the
   * Client object has been set by calling setClient, that
   * object will be returned. If the adapter was NOT constructed with a
   * Client object and one has not been set using setClient,
   * then this method should be overridden in a
   * subclass to return a Client object by some other means.
   */

  public Client getClient()
  {
    return _client;
  }

  public void setClient(Client client)
  {
    _client = client;
  }

  /**
   * If getClient() returns null then this method returns 'false', otherwise,
   * this method returns getClient().filterEvents(). In other words, the
   * default event filtering for a listener is that of its owning client.
   * This method can be overidden in subclasses to provide different
   * behaviour for a particular listener.
   */

  public boolean filterEvents()
  {
    Client client = getClient();

    if (client == null)
       return false;
    else
       return client.filterEvents();
  }

  private Client _client;
}
