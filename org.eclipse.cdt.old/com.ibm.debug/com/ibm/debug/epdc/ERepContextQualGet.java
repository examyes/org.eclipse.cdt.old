package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1998, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/ERepContextQualGet.java, java-epdc, eclipse-dev, 20011128
// Version 1.4.1.2 (last modified 11/28/01 16:25:21)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;

public class ERepContextQualGet extends EPDC_Reply
{
  // Debug Engine Functions and Data

  public ERepContextQualGet(int[] entryID) {
    super(EPDC.Remote_ContextQualGet);
    _entryIDs = entryID;
  }

  protected int toDataStreams(DataOutputStream fixedData,
			      DataOutputStream varData, int baseOffset)
    throws IOException, BadEPDCCommandException
  {
    int offset = baseOffset;

    super.toDataStreams(fixedData, varData, baseOffset);
    offset += super.varLen();

    writeInt(fixedData, _entryIDs.length);
    writeInt(fixedData, offset);

    for (int i=0; i<_entryIDs.length; ++i)
      writeInt(varData, _entryIDs[i]);

    return fixedLen() + varLen();
  }

  protected int fixedLen() {
    return super.fixedLen() + _fixed_length;
  }

  protected int varLen() {
    return super.varLen() + (_entryIDs.length * 4);
  }

  private static final int _fixed_length = 8;

   // Debug Front End Functions and Data

   ERepContextQualGet( byte[] packetBuffer, DataInputStream dataInputStream)
   throws IOException
   {
      super( packetBuffer, dataInputStream );

      int numberOfEntries = dataInputStream.readInt();

      if (numberOfEntries == 0)
         return;

      _entryIDs = new int[numberOfEntries];

      OffsetDataInputStream offsetDataInputStream =
                            new OffsetDataInputStream(
                                                      packetBuffer,
                                                      dataInputStream.readInt()
                                                     );

      for (int i = 0; i < numberOfEntries; i++)
          _entryIDs[i] = offsetDataInputStream.readInt();
   }

   public int[] getEntryIDs()
   {
     return _entryIDs;
   }

   private int[] _entryIDs;
}

