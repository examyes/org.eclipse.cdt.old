package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/Client.java, java-model, eclipse-dev, 20011128
// Version 1.1.1.2 (last modified 11/28/01 16:13:57)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

/**
 * Client objects are created by calling Model.createClient.
 */

public class Client
{
  Client(byte id,
         String name,
         boolean filterEvents
        )
  {
    _id = id;

    if (name != null)
       _name = name;
    else
       switch(id)
       {
         // Client names are for internal debugger use and do not need to be
         // translated:

         case UNKNOWN:
              _name = "Unknown";
              break;

         case VADD:
              _name = "VisualAge Distributed Debugger (Java SUI)";
              break;

         case DBX:
              _name = "dbx";
              break;

         case OLT:
              _name = "Object Level Trace";
              break;

         case DCI:
              _name = "DCI";
              break;

         case EWB:
              _name = "Enterprise Workbench";
              break;
       }

    _filterEvents = filterEvents;
  }

  /**
   * Determine which client this is.
   * @see Client#UNKNOWN
   */

  public byte getID()
  {
    return _id;
  }

  /**
   * Client names are for internal debugger use and have not necessarily
   * been translated for NLS.
   */

  public String getName()
  {
    return _name;
  }

  /**
   * Does this client want the Model to filter the events that it receives
   * so that it only sees events that it originated,
   * or does it want to see all events regardless of which client
   * originated them?
   */

  public boolean filterEvents()
  {
    return _filterEvents;
  }

  public void setFilterEvents(boolean filterEvents)
  {
    _filterEvents = filterEvents;
  }

  private byte _id;
  private String _name;
  private boolean _filterEvents = false;

  /**
   * Client is unknown.
   */

  public static final byte UNKNOWN = 0;

  /**
   * Client is the VisualAge Distributed Debugger (Java SUI).
   */

  public static final byte VADD = 1;

  /**
   * Client is DBX.
   */

  public static final byte DBX = 2;

  /**
   * Client is the Object Level Trace tool.
   */

  public static final byte OLT = 3;

  /**
   * Client is the Debug Command Interface (Model test tool).
   */

  public static final byte DCI = 4;

  /**
   * Client is Enterprise Workbench.
   */

  public static final byte EWB = 5;

  static final byte LAST_ID = 5;
}
