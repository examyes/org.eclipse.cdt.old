package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EStdStorageLocation.java, java-epdc, eclipse-dev, 20011128
// Version 1.8.1.2 (last modified 11/28/01 16:25:23)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;

public class EStdStorageLocation extends EPDC_Base
{
  public EStdStorageLocation(String baseAddress, int lineOffset, int unitOffset)
  {
    _baseAddress = new EStdString(baseAddress);
    _lineOffset = lineOffset;
    _unitOffset = unitOffset;
  }

  EStdStorageLocation(String baseAddress)
  {
    _baseAddress = new EStdString(baseAddress);
  }

  EStdStorageLocation()
  {
  }

  EStdStorageLocation(byte[] packetBuffer, DataInputStream dataInputStream)
  throws IOException
  {
    int offset = dataInputStream.readInt();

    if (offset != 0)
       _baseAddress = new EStdString(packetBuffer,
                                     new OffsetDataInputStream(packetBuffer, offset));

    _lineOffset = dataInputStream.readInt();
    _unitOffset = dataInputStream.readInt();
  }

  public void output(DataOutputStream dataOutputStream, int baseOffset)
  throws IOException
  {
    outputFixedPart(dataOutputStream, baseOffset);
    outputVariablePart(dataOutputStream);
  }

  public void outputFixedPart(DataOutputStream dataOutputStream, int baseOffset)
  throws IOException
  {
    writeOffsetOrZero(dataOutputStream, baseOffset, _baseAddress);
    dataOutputStream.writeInt(_lineOffset);
    dataOutputStream.writeInt(_unitOffset);
  }

  public void outputVariablePart(DataOutputStream dataOutputStream)
  throws IOException
  {
    if (_baseAddress != null)
       _baseAddress.output(dataOutputStream);
  }

  protected int fixedLen()
  {
    return _fixed_length;
  }

  protected int varLen()
  {
    return totalBytes(_baseAddress);
  }

  String getAddress()
  {
    return _baseAddress.string();
  }
  int getLineOffset()
  {
    return _lineOffset;
  }
  int getUnitOffset()
  {
    return _unitOffset;
  }

  private EStdString _baseAddress;
  private int _lineOffset;
  private int _unitOffset;
  private static int _fixed_length = 12;
}
