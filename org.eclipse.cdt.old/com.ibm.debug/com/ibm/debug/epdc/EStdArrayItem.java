package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EStdArrayItem.java, java-epdc, eclipse-dev, 20011128
// Version 1.10.1.2 (last modified 11/28/01 16:24:35)
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
 * Class corresponding to an EStdArrayItem structure.
 */
public class EStdArrayItem extends EStdGenericNode {

   /**
    * Create a new EStdArrayItem class
    * @param numElements the number of elements in the array
    * @param baseIndex the index of the first element
    * @param arrayName the array name
    * @param arrayType the array type
    * @param arrayItemType the array item type
    * @param arrayReps list of possible representations
    * @param defArrayRepIndex the index into <code> arrayReps </code> of the default representation.
    */

   public EStdArrayItem(int numElements, int baseIndex,
                        String arrayName, String arrayType,
                        String arrayItemType, short[] arrayReps,
                        short defArrayRepIndex)
   {

      super(EPDC.StdArrayNode);

      _numElements = numElements;
      _baseIndex = baseIndex;
      _arrayName = new EStdString(arrayName);
      _arrayType = new EStdString(arrayType);
      _arrayItemType = new EStdString(arrayItemType);
      _arrayReps = arrayReps;
      _defArrayRepIndex = defArrayRepIndex;
   }

   public EStdArrayItem(byte[] packetBuffer, DataInputStream dataInputStream,
                        short nodeType)
   throws IOException
   {
     super(nodeType);

     dataInputStream.readShort();  //reserved

     _numElements = dataInputStream.readInt();
     _baseIndex = dataInputStream.readInt();

     dataInputStream.readInt();    // reserved
     dataInputStream.readInt();    // reserved

     int offset;

     if ((offset = dataInputStream.readInt()) != 0)
     {
         _arrayName = ( new EStdString(packetBuffer,
                                       new OffsetDataInputStream(packetBuffer,
                                                                 offset)));
     }

     if ((offset = dataInputStream.readInt()) != 0)
     {
         _arrayType = ( new EStdString(packetBuffer,
                                       new OffsetDataInputStream(packetBuffer,
                                                                 offset)));
     }

     if ((offset = dataInputStream.readInt()) != 0)
     {
         _arrayItemType = ( new EStdString(packetBuffer,
                                         new OffsetDataInputStream(packetBuffer,
                                                                   offset)));
     }

     _defArrayRepIndex = dataInputStream.readShort();
     _numValueReps = dataInputStream.readShort();

     _arrayReps = new short[_numValueReps];
     for (int i=0; i < _numValueReps; i++)
     {
          _arrayReps[i] = dataInputStream.readShort();
     }
   }

   /**
    * Return "fixed" length
    */
   protected int fixedLen() {
      return _fixed_length + _arrayReps.length * 2;
   }

   /**
    * Return variable length
    */
   protected int varLen() {
      return totalBytes(_arrayName) +
             totalBytes(_arrayType) +
             totalBytes(_arrayItemType);
   }


   /** Output class to data streams according to EPDC protocol.
     * @exception IOException if an I/O error occurs
    *  @exception BadEPDCCommandException if the EPDC command
    *    is structured incorrectly
    */
   protected int toDataStreams(DataOutputStream fixedData,
         DataOutputStream varData, int baseOffset)
         throws IOException, BadEPDCCommandException {

      int total = _fixed_length + _arrayReps.length * 2;
      int offset = baseOffset;

      writeShort(fixedData, EPDC.StdArrayNode);
      writeShort(fixedData, (short) 0);      // reserved

      writeInt(fixedData, _numElements);
      writeInt(fixedData, _baseIndex);

      writeInt(fixedData, 0);       // NOT USED
      writeInt(fixedData, 0);       // NOT USED

      offset += writeOffsetOrZero(fixedData, offset, _arrayName);
      offset += writeOffsetOrZero(fixedData, offset, _arrayType);
      offset += writeOffsetOrZero(fixedData, offset, _arrayItemType);

      if (_arrayName != null)
         _arrayName.output(varData);

      if (_arrayType != null)
         _arrayType.output(varData);

      if (_arrayItemType != null)
         _arrayItemType.output(varData);

      writeShort(fixedData, _defArrayRepIndex);
      writeShort(fixedData, (short) _arrayReps.length);

      // write out representation array
      for (int i=0; i<_arrayReps.length; i++)
           writeShort(fixedData, _arrayReps[i]);

      total += offset - baseOffset;
      return total;
   }

   /**
    * Get the name of the array
    */
   public String getName()
   {
     if (_arrayName != null)
       return _arrayName.string();
     else
       return null;
   }

   /**
    * Get the type of the array
    */
   public String getType()
   {
     if (_arrayType != null)
       return _arrayType.string();
     else
       return null;
   }

   /**
    * Get the type of the array element
    */
   public String getItemType()
   {
     if (_arrayItemType != null)
       return _arrayItemType.string();
     else
       return null;
   }

   /**
    * Get the number of items in the array
    */
   public int getItemCount()
   {
     return _numElements;
   }

   /**
    * Return the current representation index
    */
   public short getDefRep()
   {
     return _defArrayRepIndex;
   }

   /**
    * Return the array of all possible representation indices
    * of this type
    */
   public short[] getArrayOfReps()
   {
     return _arrayReps;
   }

   // data fields
   private int _numElements;
   private int _baseIndex;
   private EStdString _arrayName;
   private EStdString _arrayType;
   private EStdString _arrayItemType;
   private short _defArrayRepIndex;
   private short _numValueReps;
   private short[] _arrayReps;

   private static final int _fixed_length = 36;
}
