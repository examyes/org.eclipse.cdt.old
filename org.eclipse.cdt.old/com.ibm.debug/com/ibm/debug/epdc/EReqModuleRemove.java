package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqModuleRemove.java, java-epdc, eclipse-dev, 20011128
// Version 1.1.1.2 (last modified 11/28/01 16:26:01)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;

/**
 * Remove a module
 */
public class EReqModuleRemove extends EPDC_Request
{
  public EReqModuleRemove(int moduleId)
  {
    super(EPDC.Remote_ModuleRemove);

    _moduleId = moduleId;
  }

  public void output(DataOutputStream dataOutputStream)
  throws IOException
  {
    super.output(dataOutputStream);

    dataOutputStream.writeInt(_moduleId);
  }

  /**
   * Return module type
   */
  public int moduleId()
  {
    return _moduleId;
  }

  protected int fixedLen()
  {
    return _fixed_length + super.fixedLen();
  }

  // data fields
  private int _moduleId;


  private static final int  _fixed_length = 4;
}
