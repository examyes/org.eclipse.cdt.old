package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/ThreadChangeInfo.java, java-epdc, eclipse-dev, 20011128
// Version 1.4.1.2 (last modified 11/28/01 16:24:50)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;

public class ThreadChangeInfo extends EPDC_ChangeInfo
{
  ThreadChangeInfo(byte[] packetBuffer, DataInputStream dataInputStream, EPDC_EngineSession engineSession)
  throws IOException
  {
    super(packetBuffer, dataInputStream); // Construct packet header

    // Get all the changed items and add them to the vector of changed items:

    if (changeItemsInThisPacket() == 1)
       addChangedItem(new ERepGetNextThread(packetBuffer, dataInputStream, engineSession), false);
    else
      for (int i = 0; i < changeItemsInThisPacket(); i++)
      {
          int offset = dataInputStream.readInt();

          addChangedItem(new ERepGetNextThread
                             (
                               packetBuffer,
                               new OffsetDataInputStream(packetBuffer, offset),
                               engineSession),
                         false
                        );
      }
  }
}
