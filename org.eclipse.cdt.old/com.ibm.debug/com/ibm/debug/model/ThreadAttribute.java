package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/ThreadAttribute.java, java-model, eclipse-dev, 20011128
// Version 1.2.1.2 (last modified 11/28/01 16:14:19)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.epdc.EStdAttribute;

/**
 * A ThreadAttribute represents an attribute of a thread sent by the
 * debug engine. A thread can have zero or more attributes. Each
 * attribute has a type, a name, and a value. The engine may omit the
 * name in which case the UI should use a default name.
 * @see DebuggeeThread#getAttributes
 */

public class ThreadAttribute
{
  ThreadAttribute(EStdAttribute epdcAttribute)
  {
    _epdcAttribute = epdcAttribute;

    if (Model.TRACE.DBG && Model.traceInfo())
       Model.TRACE.dbg(3, "Constructing ThreadAttribute. Name=" +
                          epdcAttribute.getName() + " Value=" +
                          epdcAttribute.getValue() + " Type=" +
                          epdcAttribute.getType());
  }

  /**
   * Get the type of this attribute. The type of an attribute may be one of
   * the following constants from com.ibm.debug.epdc.EPDC:
   * <ul>
   * <li>ThreadMiscAttr This is an attribute that the engine wants the user
   *     to see but does not fit into any of the "well-known" attribute types
   *     below.
   * <li>ThreadNameOrTID This attribute is the name or TID of the thread.
   * <li>ThreadState This attribute is the thread state.
   * <li>ThreadPriority This attribute is the priority of the thread.
   * <li>ThreadBlockingThread This attribute is the name of some other thread
   *     that is blocking the execution of this thread (because it holds a
   *     lock on an object, for example).
   * </ul>
   * @see com.ibm.debug.epdc.EPDC#ThreadMiscAttr
   */

  public byte getType()
  {
    return _epdcAttribute.getType();
  }

  /**
   * The name or label that describes this attribute. The name may be null
   * for all attribute types except ThreadMiscAttr. If the name is null,
   * the UI should provide a default name when displaying this attribute
   * to the user e.g. "State", "Priority", etc.
   */

  public String getName()
  {
    return _epdcAttribute.getName();
  }

  /**
   * Get the value of the attribute.
   */

  public String getValue()
  {
    return _epdcAttribute.getValue();
  }

  private EStdAttribute _epdcAttribute;
}
