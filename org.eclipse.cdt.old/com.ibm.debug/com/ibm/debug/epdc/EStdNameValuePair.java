package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EStdNameValuePair.java, java-epdc, eclipse-dev, 20011128
// Version 1.1.1.2 (last modified 11/28/01 16:26:13)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;

public class EStdNameValuePair extends EPDC_Base
{
  public EStdNameValuePair(String name,
                           String value)
  {
    _name = new EStdString(name);
    _value = new EStdString(value);
  }

  EStdNameValuePair(byte[] packetBuffer, DataInputStream dataInputStream)
  throws IOException
  {
    int offset = dataInputStream.readInt(); // Offset to name

    if (offset > 0)
       _name = new EStdString(packetBuffer,
                              new OffsetDataInputStream(packetBuffer,
                                                        offset)
                             );

    offset = dataInputStream.readInt(); // Offset to value

    if (offset > 0)
       _value = new EStdString(packetBuffer,
                               new OffsetDataInputStream(packetBuffer,
                                                         offset)
                             );
  }

  protected int toDataStreams(DataOutputStream fixedData,
                              DataOutputStream varData,
                              int baseOffset)
            throws IOException, BadEPDCCommandException
  {
    int nameLength = writeOffsetOrZero(fixedData, baseOffset, _name);

    if (nameLength > 0)
    {
       _name.output(varData);

       baseOffset += nameLength;
    }

    if (writeOffsetOrZero(fixedData, baseOffset, _value) > 0)
       _value.output(varData);

    return fixedLen() + varLen();
  }

  protected int fixedLen()
  {
    return _fixed_length;
  }

  protected int varLen()
  {
    return totalBytes(_name) + totalBytes(_value);
  }

  public String getName()
  {
    if (_name == null)
       return null;
    else
       return _name.string();
  }

  public String getValue()
  {
    if (_value == null)
       return null;
    else
       return _value.string();
  }

  private EStdString _name;
  private EStdString _value;

  private final static int _fixed_length = 8;

  public final static String jIBMCopyright = "(c) Copyright IBM Corporation 2000 - All Rights Reserved";
}
