package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/ERepGetNextRegister.java, java-epdc, eclipse-dev, 20011128
// Version 1.7.1.2 (last modified 11/28/01 16:25:31)
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
 * ERepGetNextRegister structure
 */
public class ERepGetNextRegister extends EPDC_ChangeItem
{
   public ERepGetNextRegister(int DU, int groupID, int registerID, String name, String value, int flags, int type)
   {
      _DU = DU;
      _groupID = groupID;
      _registerID = registerID;
      _name = new EStdString(name);
      _value = new EStdString(value);
      _flags = flags;
      _type = type;
   }

   ERepGetNextRegister (byte[] packetBuffer, DataInputStream dataInputStream)
   throws IOException
   {
      _DU = dataInputStream.readInt();
      _groupID = dataInputStream.readInt();
      _registerID = dataInputStream.readInt();

      int offset;

      if ((offset = dataInputStream.readInt()) != 0)  //Offset to register name
         _name = new EStdString(packetBuffer,
                                 new OffsetDataInputStream(packetBuffer, offset)
                                );

      if ((offset = dataInputStream.readInt()) != 0)  //Offset to register value
         _value = new EStdString(packetBuffer,
                                  new OffsetDataInputStream(packetBuffer, offset)
                                 );

      _flags = dataInputStream.readInt(); //register flags
      _type = dataInputStream.readInt();  //register type
   }

   public int getDU()
   {
      return _DU;
   }

   public int getGroupID()
   {
      return _groupID;
   }

   public int getRegisterID()
   {
      return _registerID;
   }

   public String getName()
   {
      if (_name != null)
        return _name.string();
      else
        return null;
   }

   public String getValue()
   {
      if (_value != null)
        return _value.string();
      else
        return null;
   }

   public int getFlags()
   {
      return _flags;
   }

   public boolean isNew()
   {
      return (_flags & EPDC.RegisterNew) != 0;
   }

   public boolean isDeleted()
   {
      return (_flags & EPDC.RegisterDeleted) != 0;
   }

   public int getType()
   {
      return _type;
   }

   protected int fixedLen()
   {
      return _fixed_length;
   }

   protected static int _fixedLen()
   {
     return _fixed_length;
   }

   protected int varLen() {
      int total = 0;
      total += totalBytes(_name);
      total += totalBytes(_value);

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
      int offset = baseOffset;

      writeInt(fixedData, _DU);
      writeInt(fixedData, _groupID);
      writeInt(fixedData, _registerID);

      offset += writeOffsetOrZero(fixedData, offset, _name);
      offset += writeOffsetOrZero(fixedData, offset, _value);

      if (_name != null)
         _name.output(varData);

      if (_value != null)
         _value.output(varData);

      writeInt(fixedData, _flags);
      writeInt(fixedData, _type);

      return _fixed_length + offset - baseOffset;
   }


   // Data fields
   private int _DU;
   private int _groupID;
   private int _registerID;
   private EStdString _name;
   private EStdString _value;
   private int _flags;
   private int _type;

   private static final int _fixed_length = 28;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}

