package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/ERepRegistersDetailsGet.java, java-epdc, eclipse-dev, 20011128
// Version 1.1.1.2 (last modified 11/28/01 16:25:25)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;
import java.util.*;

/**
 * Get registers details reply
 */
public class ERepRegistersDetailsGet extends EPDC_Reply
{
   public ERepRegistersDetailsGet() {
      super(EPDC.Remote_RegistersDetailsGet);

      _groupInfo = new Vector();
      _defaultGroupIds = null;
   }

  //Decode EPDC reply
  ERepRegistersDetailsGet(byte[] packetBuffer, DataInputStream dataInputStream)
  throws IOException
  {
    super (packetBuffer, dataInputStream);  // epdc reply header

    //get numbers of groups available, as determined by the backend
    int numGroups = dataInputStream.readInt();

    _groupInfo = new Vector();

    //get offset to array of ERepGetRegistersGroups
    int offset = dataInputStream.readInt();
    if (offset != 0)
    {
      OffsetDataInputStream offsetDataInputStream = new OffsetDataInputStream(packetBuffer,offset);

      //get the array of ERepGetRegistersGroups
      for (int i = 0; i < numGroups; i++)
        _groupInfo.addElement (new ERepGetRegistersGroups(packetBuffer,offsetDataInputStream));
    }

    //get number of default groups to present as set by the backend
    int numDefaultGroups = dataInputStream.readInt();

    //get the array of default group ids, 1 based
    _defaultGroupIds = new int[numDefaultGroups];
    for (int i = 0; i < numDefaultGroups; i++)
      _defaultGroupIds[i] = dataInputStream.readInt();

  }

  public Vector getGroupInfo()
  {
    return _groupInfo;
  }

  public int[] getDefaultGroupIds()
  {
    return _defaultGroupIds;
  }

   /**
    * Add a new group
    */
   public void addGroup(String groupName)
   {
      int groupId = _groupInfo.size() + 1;

      _groupInfo.addElement(new ERepGetRegistersGroups(groupId, groupName));
   }

   /**
    * Set the default group list
    */
   public void setDefaultGroups(int[] defaultGroupIds)
   {
      _defaultGroupIds = defaultGroupIds;
   }

   /**
    * Return "fixed" component size
    */
   protected int fixedLen()
   {
      return super.fixedLen() + _fixed_length + _defaultGroupIds.length * 4;
   }

   /**
    * Return "variable" component size
    */
   protected int varLen()
   {
      int total = super.varLen();
      ERepGetRegistersGroups group;

      for (int i=0; i<_groupInfo.size(); i++)
      {
         group = (ERepGetRegistersGroups) _groupInfo.elementAt(i);
         total += group.fixedLen() + group.varLen();
      }

      return total;
   }

   /** Output class to data streams according to EPDC protocol.
     * @exception IOException if an I/O error occurs
    *  @exception BadEPDCCommandException if the EPDC command
    *    is structured incorrectly
    */
   protected int toDataStreams(DataOutputStream fixedData,
         DataOutputStream varData, int baseOffset)
         throws IOException, BadEPDCCommandException {

      ERepGetRegistersGroups group;

      int total = super.toDataStreams(fixedData, varData, baseOffset);
      baseOffset += super.varLen();
      total += _fixed_length + _defaultGroupIds.length;

      ByteArrayOutputStream varByte2 = new ByteArrayOutputStream();
      // data stream for variable portion of group informatin
      DataOutputStream varData2 = new DataOutputStream(varByte2);

      int offset1 = baseOffset;
      int offset2 = offset1 + _groupInfo.size() * ERepGetRegistersGroups._fixedLen();

      writeInt(fixedData, _groupInfo.size());

      // write out group info
      writeOffset(fixedData, offset1);
      for (int i=0; i<_groupInfo.size(); i++) {
         group = (ERepGetRegistersGroups) _groupInfo.elementAt(i);
         total += group.toDataStreams(varData, varData2, offset2);
         offset2 += group.varLen();
      }

      writeInt(fixedData, _defaultGroupIds.length);
      for (int i=0; i<_defaultGroupIds.length; i++)
         writeInt(fixedData, _defaultGroupIds[i]);

      varData.write(varByte2.toByteArray());

      return total;
   }

   // data fields
   private Vector _groupInfo;
   private int[] _defaultGroupIds;

   private static final int _fixed_length = 12;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}
