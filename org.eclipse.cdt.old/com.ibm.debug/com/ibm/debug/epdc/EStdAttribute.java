package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1995, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EStdAttribute.java, java-epdc, eclipse-dev, 20011128
// Version 1.1.1.2 (last modified 11/28/01 16:26:14)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;

public class EStdAttribute extends EPDC_Base
{
  public EStdAttribute(byte attributeType,
                             String attributeName,
                             String attributeValue)
  {
    _attributeType = attributeType;
    _attributeNameValue = new EStdNameValuePair(attributeName, attributeValue);
  }

  EStdAttribute(byte[] packetBuffer, DataInputStream dataInputStream)
  throws IOException
  {
    _attributeType = dataInputStream.readByte();

    // read pad to 4 byte boundary
    dataInputStream.readByte();
    dataInputStream.readByte();
    dataInputStream.readByte();

    _attributeNameValue = new EStdNameValuePair(packetBuffer, dataInputStream);
  }

  protected int toDataStreams(DataOutputStream fixedData,
                              DataOutputStream varData,
                              int baseOffset)
            throws IOException, BadEPDCCommandException
  {
    // Write out the attribute

    writeChar(fixedData,_attributeType);

    // pad to 4 byte boundary

    writeChar(fixedData,(byte)0);
    writeChar(fixedData,(byte)0);
    writeChar(fixedData,(byte)0);

    // write out the name/value pair

    _attributeNameValue.toDataStreams(fixedData, varData, baseOffset);

    return fixedLen() + varLen();
  }

  protected int fixedLen()
  {
    return _fixed_length + _attributeNameValue.fixedLen();
  }

  protected int varLen()
  {
    return _attributeNameValue.varLen();
  }

  public byte getType()
  {
    return _attributeType;
  }

  public String getName()
  {
    return _attributeNameValue.getName();
  }

  public String getValue()
  {
    return _attributeNameValue.getValue();
  }

  private byte _attributeType;
  private EStdNameValuePair _attributeNameValue;

  private final static int _fixed_length = 4;

  public final static String jIBMCopyright = "(c) Copyright IBM Corporation 2000 - All Rights Reserved";
}
