package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EStdStorageRange.java, java-epdc, eclipse-dev, 20011128
// Version 1.4.1.2 (last modified 11/28/01 16:25:24)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;

public class EStdStorageRange extends EPDC_Base
{
  public EStdStorageRange(int startLineOffset, int endLineOffset)
  {
    _startLineOffset = startLineOffset;
    _endLineOffset = endLineOffset;
  }

  EStdStorageRange(byte[] packetBuffer, DataInputStream dataInputStream)
  throws IOException
  {
    _startLineOffset = dataInputStream.readInt();
    _endLineOffset = dataInputStream.readInt();
  }

  public void output(DataOutputStream dataOutputStream)
  throws IOException
  {
    dataOutputStream.writeInt(_startLineOffset);
    dataOutputStream.writeInt(_endLineOffset);
  }

  int getFirstLineOffset()
  {
    return _startLineOffset;
  }

  int getLastLineOffset()
  {
    return _endLineOffset;
  }

  protected int fixedLen()
  {
    return _fixed_length;
  }

  private int _startLineOffset;
  private int _endLineOffset;
  private static int _fixed_length = 8;
}
