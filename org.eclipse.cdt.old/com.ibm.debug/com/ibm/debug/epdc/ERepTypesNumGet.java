package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/ERepTypesNumGet.java, java-epdc, eclipse-dev, 20011128
// Version 1.4.1.2 (last modified 11/28/01 16:23:56)
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
 * TypesNumGet reply
 */
public class ERepTypesNumGet extends EPDC_Reply {

   public ERepTypesNumGet() {
      super(EPDC.Remote_TypesNumGet);
      _types = new Vector();
   }

   public ERepTypesNumGet(byte[] packetBuffer, DataInputStream dataInputStream)
   throws IOException
   {
     super(packetBuffer, dataInputStream);

     int numTypes = dataInputStream.readInt();    // number of types returned

     if (numTypes > 0)
         _types = new Vector(numTypes);

     if (numTypes == 1)
     {
         _types.addElement(new ERepTypesAndRepsGetNext(packetBuffer,
                                                       dataInputStream));
     }
     else
     {
        int offset;
        for (int i = 0; i < numTypes; i++)
        {
             if ((offset = dataInputStream.readInt()) != 0)
             {
                 _types.addElement(new ERepTypesAndRepsGetNext(packetBuffer,
                              new OffsetDataInputStream(packetBuffer, offset)));
             }
        }
     }
   }

   /**
    * Return the default representation associated with the given type index
    */
   public int defaultRepresentation(int typeIndex)
   {
      for (int i=0;i<_types.size(); i++)
      {
         ERepTypesAndRepsGetNext tar = (ERepTypesAndRepsGetNext)
            _types.elementAt(i);
         if (tar.typeIndex() == typeIndex)
         {
            return tar.defaultRep();
         }
      }
      return 0;
   }

   /**
    * Set the default representation associated with the given type index
    */
   public void setDefaultRepresentation(int typeIndex, short defRep)
   {
      for (int i=0;i<_types.size(); i++)
      {
         ERepTypesAndRepsGetNext tar =
            (ERepTypesAndRepsGetNext) _types.elementAt(i);
         if (tar.typeIndex() == typeIndex)
         {
            tar.setDefaultRep(defRep);
            return;
         }
      }
   }

   /**
    * Return the array of reps associated with this type index
    */
   public short[] repsForType(int typeIndex)
   {
      for (int i=0;i<_types.size(); i++)
      {
         ERepTypesAndRepsGetNext tar = (ERepTypesAndRepsGetNext)
            _types.elementAt(i);
         if (tar.typeIndex() == typeIndex)
         {
            return tar.repsForType();
         }
      }
      return null;
   }


   /**
    * Add a ERepTypesAndRepsGetNext to this reply
    */
   public void addTypesAndReps(ERepTypesAndRepsGetNext entries)
   {
      _types.addElement(entries);
   }

   /**
    * Add an array of ERepTypesAndRepsGetNext to this reply
    */
   public void addTypesAndReps(ERepTypesAndRepsGetNext[] entries)
   {
      for (int i=0;i <entries.length; i++)
         _types.addElement(entries[i]);
   }

   /**
    * Return the list of representation types
    */
   public Vector types()
   {
     return _types;
   }

   /** Output class to data streams according to EPDC protocol.
    * @exception IOException if an I/O error occurs
    * @exception BadEPDCCommandException if the EPDC command
    *  is structured incorrectly
    */
   protected int toDataStreams(DataOutputStream fixedData,
         DataOutputStream varData, int baseOffset)
         throws IOException, BadEPDCCommandException {

      int total = _fixed_length;
      total += super.toDataStreams(fixedData, varData, baseOffset);
      int offset1 = baseOffset + super.varLen();

      writeInt(fixedData, _types.size());

      int offset2 = offset1 + _types.size() *
         ERepTypesAndRepsGetNext._fixedLen();
      ByteArrayOutputStream varByte2 = new ByteArrayOutputStream();
      DataOutputStream varData2 = new DataOutputStream(varByte2);

      // write out entry information
      for (int i=0; i<_types.size(); i++) {
         if (_types.size() > 1) {
            writeOffset(fixedData, offset1);
            total += 4;
         }
         ERepTypesAndRepsGetNext entry =
            (ERepTypesAndRepsGetNext) _types.elementAt(i);
         total+=entry.toDataStreams(varData, varData2, offset2);
         offset1 += entry.fixedLen();
         offset2 += entry.varLen();
      }

      varData.write(varByte2.toByteArray());      // merge data streams
      return total;
   }

   /**
    * Return size of "fixed" portion
    */
   protected int fixedLen() {
      int total = _fixed_length + super.fixedLen();
      if (_types.size() > 1)
          total += _types.size() * 4;
      return total;
   }

   protected int varLen() {
      int total = super.varLen();
      for (int i=0; i<_types.size(); i++) {
         ERepTypesAndRepsGetNext entry =
            (ERepTypesAndRepsGetNext) _types.elementAt(i);
         total += entry.fixedLen() + entry.varLen();
      }
      return total;
   }

   // Data fields
   private Vector _types;

   private static final int _fixed_length = 4;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}
