package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/ERepTypesAndRepsGetNext.java, java-epdc, eclipse-dev, 20011128
// Version 1.8.1.2 (last modified 11/28/01 16:23:56)
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
 * ERepTypesAndRepsGetNext structure
 */
public class ERepTypesAndRepsGetNext extends EPDC_Base {

   public ERepTypesAndRepsGetNext(int typeIndex, String typeName,
      short defRepIndex, short reps[]) {
      _typeIndex = typeIndex;
      _typeName = new EStdString(typeName);
      _reps = reps;

      _defaultRep = 0;
      // Calculate the defaultRep from the defRepIndex
      for (int i=0;i<_reps.length;i++)
      {
         if (_reps[i] == defRepIndex) {
            _defaultRep = i;
         }
      }

   }

   public ERepTypesAndRepsGetNext(byte[] packetBuffer,
                                  DataInputStream dataInputStream)
   throws IOException
   {
     _typeIndex = dataInputStream.readInt();

     int offset;
     if ((offset = dataInputStream.readInt()) != 0)
     {
         _typeName = new EStdString(packetBuffer,
                               new OffsetDataInputStream(packetBuffer, offset));

     }

     int numReps = dataInputStream.readInt();
     _defaultRep = dataInputStream.readInt();

     _reps = new short[numReps];

     OffsetDataInputStream offsetDataInputStream =
                            new OffsetDataInputStream(
                                                      packetBuffer,
                                                      dataInputStream.readInt()
                                                     );
     for (int i=0; i < numReps; i++)
     {
          _reps[i] = (short)offsetDataInputStream.readInt();
     }

   }

   public String typeName()
   {
      if (_typeName != null)
        return _typeName.string();
      else
        return null;
   }

   public int typeIndex()
   {
      return _typeIndex;
   }

   public short defaultRep()
   {
      return (short) _defaultRep;
   }

   public void setDefaultRep(short rep)
   {
      _defaultRep = rep;
   }

   public short[] repsForType()
   {
      return _reps;
   }

   protected int fixedLen() {
      return _fixed_length;
   }

   /**
    * Static version of fixedLen
    */
   protected static int _fixedLen() {
      return _fixed_length;
   }

   protected int varLen() {
      int total = 0;
      total += totalBytes(_typeName);

      if (_reps != null)
         total += _reps.length * 4;

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

      writeInt(fixedData, _typeIndex);

      offset += writeOffsetOrZero(fixedData, offset, _typeName);

      if (_typeName != null)
         _typeName.output(varData);

      writeInt(fixedData, _reps.length);
      writeInt(fixedData, (int) _defaultRep);

      if (_reps != null) {
         writeOffset(fixedData, offset);
         for (int i=0;i<_reps.length;i++)
         {
            writeInt(varData, (int)_reps[i]);
            offset += 4;
         }
      } else
         writeOffset(fixedData, 0);

      return fixedLen() + varLen();
   }


   // Data fields
   int _typeIndex;
   EStdString _typeName;
   int _defaultRep;
   short _reps[];

   private static final int _fixed_length = 20;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}

