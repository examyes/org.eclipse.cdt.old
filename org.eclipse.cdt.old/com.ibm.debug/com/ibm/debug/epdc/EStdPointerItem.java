package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EStdPointerItem.java, java-epdc, eclipse-dev, 20011128
// Version 1.2.2.2 (last modified 11/28/01 16:25:15)
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
 * Class corresponding to an EStdPointerItem structure.
 */
public class EStdPointerItem extends EStdGenericNode {

   /**
    * Create a new EStdPointerItem class
    * @param exprID expression id that is used to dereference this pointer
    * @param pointerName the name of the pointer variable
    * @param pointerType the type of the pointer variable
    * @param pointerRefType the type the pointer variable points to
    * @param pointerValue string that represents the value of the pointer
    * @param defPointerRepIndex the index into <code> pointerReps </code> of the default representation.
    * @param pointerReps list of possible representations
    */

   public EStdPointerItem(short exprID, String pointerName, String pointerType,
                          String pointerRefType, String pointerValue,
                          short defPointerRepIndex, short[] pointerReps)
   {

      super(EPDC.StdPointerNode);

      _exprID = exprID;
      _pointerName = new EStdString(pointerName);
      _pointerType = new EStdString(pointerType);
      _pointerRefType = new EStdString(pointerRefType);
      _pointerValue = new EStdString(pointerValue);
      _defPointerRepIndex = defPointerRepIndex;
      _pointerReps = pointerReps;
   }

   public EStdPointerItem(byte[] packetBuffer, DataInputStream dataInputStream,
                          short nodeType)
   throws IOException
   {
     super(nodeType);

     dataInputStream.readShort();  //reserved

     _exprID = dataInputStream.readShort();

     int offset;

     if ((offset = dataInputStream.readInt()) != 0)
     {
         _pointerName = (new EStdString(packetBuffer,
                                        new OffsetDataInputStream(packetBuffer,
                                                                  offset)));
     }

     if ((offset = dataInputStream.readInt()) != 0)
     {
         _pointerType = (new EStdString(packetBuffer,
                                        new OffsetDataInputStream(packetBuffer,
                                                                  offset)));
     }

     if ((offset = dataInputStream.readInt()) != 0)
     {
         _pointerRefType = (new EStdString(packetBuffer,
                                         new OffsetDataInputStream(packetBuffer,
                                                                   offset)));
     }

     if ((offset = dataInputStream.readInt()) != 0)
     {
         _pointerValue = (new EStdString(packetBuffer,
                                         new OffsetDataInputStream(packetBuffer,
                                                                  offset)));
     }

     _defPointerRepIndex = dataInputStream.readShort();
     _numValueReps = dataInputStream.readShort();

     _pointerReps = new short[_numValueReps];
     for (int i=0; i < _numValueReps; i++)
     {
          _pointerReps[i] = dataInputStream.readShort();
     }
   }

   /**
    * Return "fixed" length
    */
   protected int fixedLen() {
      return _fixed_length + _pointerReps.length * 2;
   }

   /**
    * Get the name of the pointer variable
    */
   public String getName()
   {
     if (_pointerName != null)
       return _pointerName.string();
     else
       return null;
   }

   /**
    * Get the type of the pointer variable
    */
   public String getType()
   {
     if (_pointerType != null)
       return _pointerType.string();
     else
       return null;
   }

   /**
    * Get the reference type of the pointer variable
    */
   public String getRefType()
   {
     if (_pointerRefType != null)
       return _pointerRefType.string();
     else
       return null;
   }

   /**
    * Get the contents of the variable
    */
   public String getValue()
   {
     if (_pointerValue != null)
       return _pointerValue.string();
     else
       return null;
   }
   /**
    * Return the current representation index
    */
   public short getDefRep()
   {
     return _defPointerRepIndex;
   }

   /**
    * Return the array of all possible representation indices
    * of this type
    */
   public short[] getArrayOfReps()
   {
     return _pointerReps;
   }

   // data fields
   private short _exprID;
   private EStdString _pointerName;
   private EStdString _pointerType;
   private EStdString _pointerRefType;
   private EStdString _pointerValue;
   private short _defPointerRepIndex;
   private short _numValueReps;
   private short[] _pointerReps;

   private static final int _fixed_length = 28;
}
