package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EStdClassItem.java, java-epdc, eclipse-dev, 20011128
// Version 1.9.1.2 (last modified 11/28/01 16:24:36)
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
 * Class corresponding to an EStdClassItem structure.
 */
public class EStdClassItem extends EStdGenericNode {

   /**
    * Create a new EStdClassItem class
    * @param stdClassAttr class attributes
    * @param classID class id
    * @param classItemCnt number of items in class
    * @param className class name
    * @param classType class type
    */
   public EStdClassItem(int stdClassAttr, short classID, int classItemCnt,
                        String className, String classType)
   {
      super(EPDC.StdClassNode);

      _stdClassAttr = (byte)stdClassAttr;
      _classID = classID;
      _classItemCnt = classItemCnt;
      _className = new EStdString(className);
      _classType = new EStdString(classType);
   }

   public EStdClassItem(byte[] packetBuffer, DataInputStream dataInputStream,
                        short nodeType)
   throws IOException
   {
     super(nodeType);

     _stdClassAttr = dataInputStream.readByte();    // reserved

     _classID = dataInputStream.readShort();
     _classItemCnt = dataInputStream.readInt();

     int offset;
     if ((offset = dataInputStream.readInt()) != 0)
     {
         _className = (new EStdString(packetBuffer,
                                       new OffsetDataInputStream(packetBuffer,
                                                                 offset)));
     }

     if ((offset = dataInputStream.readInt()) != 0)
     {
         _classType = (new EStdString(packetBuffer,
                                       new OffsetDataInputStream(packetBuffer,
                                                                 offset)));
     }
   }

   /**
    * Return "fixed" length
    */
   protected int fixedLen() {
      return _fixed_length;
   }

   /**
    * Return variable length
    */
   protected int varLen() {
      return totalBytes(_className) + totalBytes(_classType) + super.varLen();
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

      writeShort(fixedData, EPDC.StdClassNode);
      writeChar(fixedData, _stdClassAttr);
      writeShort(fixedData, _classID);
      writeInt(fixedData, _classItemCnt);

      offset += writeOffsetOrZero(fixedData, offset, _className);
      offset += writeOffsetOrZero(fixedData, offset, _classType);

      if (_className != null)
         _className.output(varData);

      if (_classType != null)
         _classType.output(varData);

      return _fixed_length + offset - baseOffset;
   }

   /**
    * Get the name of the class
    */
   public String getName()
   {
     if (_className != null)
       return _className.string();
     else
       return null;
   }

   /**
    * Get the type of the class
    */
   public String getType()
   {
     if (_classType != null)
       return _classType.string();
     else
       return null;
   }

   /**
    * Get the number of members in the class
    */
   public int getItemCount()
   {
     return _classItemCnt;
   }

   // data fields
   private byte _stdClassAttr;
   private short _classID;
   private int _classItemCnt;
   private EStdString _className;
   private EStdString _classType;

   private static final int _fixed_length = 17;
}

