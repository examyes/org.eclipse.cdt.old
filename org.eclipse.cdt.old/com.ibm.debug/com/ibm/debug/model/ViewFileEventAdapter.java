package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/ViewFileEventAdapter.java, java-model, eclipse-dev, 20011128
// Version 1.2.1.2 (last modified 11/28/01 16:13:24)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

/**
 * The methods in this class are empty; this class is provided as a
 * convenience for easily creating listeners by extending this class
 * and overriding only the methods of interest.
 */

public abstract class ViewFileEventAdapter
       extends ModelEventAdapter // Get default implementations of getClient
                                 // and filterEvents
       implements ViewFileEventListener
{
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

   public ViewFileEventAdapter(Client client)
   {
     super(client);
   }

   /**
    * Construct an event adapter with no specified owning client.
    * Note that the owning client can be set after construction by
    * calling setClient.
    * @see ModelEventAdapter#getClient
    * @see ModelEventAdapter#setClient
    * @see ModelEventAdapter#filterEvents
    */

   public ViewFileEventAdapter()
   {
   }

   /**
    * This method will be called whenever a breakpoint is added to the ViewFile.
    * @param event The event
    */

   public void breakpointAdded(BreakpointAddedEvent event)
   {
   }
}
