package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/ERepGetRegistersGroups.java, java-epdc, eclipse-dev, 20011128
// Version 1.3.1.2 (last modified 11/28/01 16:25:25)
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
 * ERepGetRegistersGroups EPDC structure.  Used by ERepRegistersDetailsGet.
 */
public class ERepGetRegistersGroups extends EPDC_Base {

   ERepGetRegistersGroups(int groupID, String groupName)
   {
      _groupID = groupID;
      _groupName = new EStdString(groupName);
   }

  //Decode reply
  ERepGetRegistersGroups(byte[] packetBuffer, DataInputStream dataInputStream)
  throws IOException
  {
    _groupID = dataInputStream.readInt();

    int offset;
    if ((offset = dataInputStream.readInt()) != 0) //Offset to group name
      _groupName = new EStdString(packetBuffer,
                                    new OffsetDataInputStream(packetBuffer, offset)
                                    );
  }

  public int getGroupID()
  {
    return _groupID;
  }

  public String getGroupName()
  {
    if (_groupName != null)
      return _groupName.string();
    else
      return null;
  }

   /**
    * Return fixed component size
    */
   protected int fixedLen() {
      return _fixed_length;
   }

   /**
    * Static function that returns fixed component size
    */
   protected static int _fixedLen() {
      return _fixed_length;
   }

   /**
    * Return variable component size
    */
   protected int varLen() {
      return super.varLen() + totalBytes(_groupName);
   }


   /** Output class to data streams according to EPDC protocol.
     * @exception IOException if an I/O error occurs
    *  @exception BadEPDCCommandException if the EPDC command
    *    is structured incorrectly
    */
   protected int toDataStreams(DataOutputStream fixedData,
         DataOutputStream varData, int baseOffset)
         throws IOException, BadEPDCCommandException {

      int offset = baseOffset;
      writeInt(fixedData, _groupID);

      offset += writeOffsetOrZero(fixedData, offset, _groupName);

      if (_groupName != null)
         _groupName.output(varData);

      return _fixed_length + offset - baseOffset;
   }

   // data fields
   private int _groupID;
   private EStdString _groupName;

   private static final int _fixed_length = 8;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}
