package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EStdScalarItem.java, java-epdc, eclipse-dev, 20011128
// Version 1.11.1.2 (last modified 11/28/01 16:24:39)
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
 * Class corresponding to EStdScalarItem structure.  I believe this represents only simple data types.
 */
public class EStdScalarItem extends EStdGenericNode{

   /**
    * Create a new EStdScalar item
    * @param scalarName scalar name
    * @param scalarType scalar type
    * @param scalarValue scalar value
    * @param scalarReps array of representations for this scalar, according to the representations defined in
    *                      EPDC_EngineSession
    * @see EPDC_EngineSession
    * @param defRep default representation, the representation which scalarValue corresponds to
    */
   public EStdScalarItem(String scalarName, String scalarType, String scalarValue, short[] scalarReps, short defRep) {
      super(EPDC.StdScalarNode);

      _scalarName = new EStdString(scalarName);
      _scalarType = new EStdString(scalarType);
      _scalarValue = new EStdString(scalarValue);
      _scalarReps = scalarReps;
      _defRep = defRep;
   }

   public EStdScalarItem(byte[] packetBuffer, DataInputStream dataInputStream,
                         short nodeType)
   throws IOException
   {
     super(nodeType);

     dataInputStream.readShort();  //reserved

     int offset;

     if ((offset = dataInputStream.readInt()) != 0)
     {
         _scalarName = new EStdString(packetBuffer,
                                        new OffsetDataInputStream(packetBuffer,
                                                                  offset));
     }

     if ((offset = dataInputStream.readInt()) != 0)
     {
         _scalarType = new EStdString(packetBuffer,
                                        new OffsetDataInputStream(packetBuffer,
                                                                  offset));
     }

     if ((offset = dataInputStream.readInt()) != 0)
     {
         _scalarValue = new EStdString(packetBuffer,
                                         new OffsetDataInputStream(packetBuffer,
                                                                   offset));
     }

     _defRep = dataInputStream.readShort();
     _numValueReps = dataInputStream.readShort();

     _scalarReps = new short[_numValueReps];
     for (int i=0; i < _numValueReps; i++)
     {
          _scalarReps[i] = dataInputStream.readShort();
     }
   }

   /**
    * Return "fixed" length
    */
   protected int fixedLen() {
      return _fixed_length + _scalarReps.length * 2;
   }

   /**
    * Return variable length
    */
   protected int varLen() {
      return super.varLen() + totalBytes(_scalarName) +
             totalBytes(_scalarType) +
             totalBytes(_scalarValue);
   }


   /** Output class to data streams according to EPDC protocol.
     * @exception IOException if an I/O error occurs
    *  @exception BadEPDCCommandException if the EPDC command
    *    is structured incorrectly
    */
   protected int toDataStreams(DataOutputStream fixedData,
         DataOutputStream varData, int baseOffset)
         throws IOException, BadEPDCCommandException {

      int total = _fixed_length + _scalarReps.length * 2;
      int offset = baseOffset;

      writeShort(fixedData, (short) EPDC.StdScalarNode);
      writeShort(fixedData, (short) 0);

      offset += writeOffsetOrZero(fixedData, offset, _scalarName);
      offset += writeOffsetOrZero(fixedData, offset, _scalarType);
      offset += writeOffsetOrZero(fixedData, offset, _scalarValue);

      if (_scalarName != null)
         _scalarName.output(varData);

      if (_scalarType != null)
         _scalarType.output(varData);

      if (_scalarValue != null)
         _scalarValue.output(varData);

      writeShort(fixedData, _defRep);
      writeShort(fixedData, (short) _scalarReps.length);

      for (int i=0; i<_scalarReps.length; i++)
         writeShort(fixedData, _scalarReps[i]);

      total += offset - baseOffset;
      return total;
   }

   public String getName()
   {
     if (_scalarName != null)
       return _scalarName.string();
     else
       return null;
   }

   public String getType()
   {
      if (_scalarType != null)
      	return _scalarType.string();
      else
      	return null;
   }


   // contents of the variable
   public String getValue()
   {
     if (_scalarValue != null)
       return _scalarValue.string();
     else
       return null;
   }

   /**
    * Return the current representation index
    */
   public short getDefRep()
   {
     return _defRep;
   }

   /**
    * Return the array of all possible representation indices
    * of this type
    */
   public short[] getArrayOfReps()
   {
     return _scalarReps;
   }

   // data fields
   private EStdString _scalarName;
   private EStdString _scalarType;
   private EStdString _scalarValue;
   private short _defRep;
   private short _numValueReps;
   private short[] _scalarReps;

   private static final int _fixed_length = 20;
}
