package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqRegistersValueSet.java, java-epdc, eclipse-dev, 20011128
// Version 1.1.1.2 (last modified 11/28/01 16:25:58)
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
 * Class to request to set a register to a value supplied by the user
 */
public class EReqRegistersValueSet extends EPDC_Request
{

  /**
   * Decodes request from an input buffer
   * @exception IOException if an I/O error occurs
   */
  EReqRegistersValueSet(byte[] inBuffer)
  throws IOException
  {
    super(inBuffer);

    _registerDU = readInt();
    _groupID = readInt();
    _registerID = readInt();
    _newRegisterValueOffset = readOffset();
  }

  public EReqRegistersValueSet(int DU, int groupID, int id, String value)
  {
    super(EPDC.Remote_RegistersValueSet);

    _registerDU = DU;
    _groupID = groupID;
    _registerID = id;
    _newRegisterValue = new EStdString(value);
  }

  /**
   * Return the ID of the register
   */
  public int getRegisterID()
  {
    return _registerID;
  }

  /**
   * Return the registers group ID
   */
  public int getGroupID()
  {
    return _groupID;
  }

  /**
   * Return new register value
   */
  public String getNewRegisterValue()
  throws IOException
  {
    if (_newRegisterValue == null && _newRegisterValueOffset != 0)
    {
        posBuffer(_newRegisterValueOffset);
        _newRegisterValue = readStdString();
     }

     return _newRegisterValue.string();
  }

  public void output(DataOutputStream dataOutputStream)
  throws IOException
  {
    super.output(dataOutputStream);

    dataOutputStream.writeInt(_registerDU);
    dataOutputStream.writeInt(_groupID);
    dataOutputStream.writeInt(_registerID);

    int offset = fixedLen() + super.varLen();
    writeOffsetOrZero(dataOutputStream, offset, _newRegisterValue);

    if (_newRegisterValue != null)
        _newRegisterValue.output(dataOutputStream);

  }

  /**
   * Return the length of the fixed component
   */

  protected int fixedLen()
  {
    return _fixed_length + super.fixedLen();
  }

  /**
   * Return the length of the variable component
   */

  protected int varLen()
  {
    return totalBytes(_newRegisterValue);
  }

  // data fields
  private int _registerDU;
  private int _groupID;
  private int _registerID;
  private int _newRegisterValueOffset;
  private EStdString _newRegisterValue;

  private static final int _fixed_length = 16;

  public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}

