package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/ERepPartOpen.java, java-epdc, eclipse-dev, 20011128
// Version 1.6.1.2 (last modified 11/28/01 16:23:43)
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
 * Part open reply packet
 */
public class ERepPartOpen extends EPDC_Reply {

   public ERepPartOpen() {
      super(EPDC.Remote_PartOpen);
      _partIDs = new Vector();
   }

   public ERepPartOpen(byte[] packetBuffer, DataInputStream dataInputStream)
   throws IOException
   {
     super(packetBuffer, dataInputStream);

     short numberOfParts = dataInputStream.readShort();

     _partIDs = new Vector(numberOfParts);

     for (int i=0; i < numberOfParts; i++)
          _partIDs.addElement(new Short(dataInputStream.readShort()));
   }

   /**
    * Add a part ID to this reply packet
    */
   public void addPartID(int partID) {
      _partIDs.addElement(new Integer(partID));
   }

   /**
    * Add a vector of part IDs to this reply packet
    */
   public void addPartIDs(Vector partIDs)
   {
      for (int i=0; i<partIDs.size(); i++)
      {
         Integer num = (Integer) partIDs.elementAt(i);
         addPartID(num.intValue());
      }
   }

   /**
    * Add an array of part IDs to this reply packet
    */
   public void addPartIDs(int[] partIDs) {
      for (int i=0; i<partIDs.length; i++)
         addPartID(partIDs[i]);
   }

   /**
    * Return number of part IDs added to reply packet
    */
   public int numPartIDs() {
      return _partIDs.size();
   }

   public Vector getPartIDs()
   {
     return _partIDs;
   }

   /** Output class to data streams according to EPDC protocol.
     * @exception IOException if an I/O error occurs
    *  @exception BadEPDCCommandException if the EPDC command
    *    is structured incorrectly
    */
   protected int toDataStreams(DataOutputStream fixedData,
         DataOutputStream varData, int baseOffset)
         throws IOException, BadEPDCCommandException {

      super.toDataStreams(fixedData, varData, baseOffset);

      writeShort(fixedData, (short) _partIDs.size());
      for (int i=0; i<_partIDs.size(); i++)
         writeShort(fixedData, ((Integer) _partIDs.elementAt(i)).shortValue());

      return fixedLen() + varLen();
   }

   protected int fixedLen() {
      return super.fixedLen() + _fixed_length + 2 * _partIDs.size();
   }

   protected int varLen() {
      return super.varLen();
   }

   // data fields
   private Vector _partIDs;

   private static final int _fixed_length = 2;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}

