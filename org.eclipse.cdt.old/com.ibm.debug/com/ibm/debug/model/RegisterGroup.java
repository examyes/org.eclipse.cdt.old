package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/RegisterGroup.java, java-model, eclipse-dev, 20011128
// Version 1.5.1.2 (last modified 11/28/01 16:12:54)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.epdc.*;
import java.util.Vector;
import java.io.*;

/**
 * This class represents details of a register group.
 * Each object of this class contains a group id and a group name.
 * column text alignment.
 */

public class RegisterGroup extends DebugModelObject
{
  RegisterGroup (ERepGetRegistersGroups group)
  {
    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(3, "Creating RegisterGroup : Name=" + group.getGroupName());

    _group = group;
  }

 int getGroupID()
  {
    return _group.getGroupID();
  }

  /**
   * Return group name as assigned by the backend.
   */

  public String getGroupName()
  {
    return _group.getGroupName();
  }

  void setDefault()
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(4, "RegisterGroup[" + getGroupName() + "].setDefault()");

    _isDefault = true;
  }

  /**
   * @return 'true' if this is a default group as set by the backend,
   * 'false' otherwise.
   */

  public boolean isDefault()
  {
    return _isDefault;
  }

  public void print(PrintWriter printWriter)
  {
    if (Model.includePrintMethods)
    {
      printWriter.print(" " + getGroupID());
      printWriter.print(" " + getGroupName());
      printWriter.println();
      printWriter.println("IsDefault: " + _isDefault);

      super.print(printWriter);
      printWriter.println();
    }
  }

  private ERepGetRegistersGroups _group;
  private boolean _isDefault = false;
}
