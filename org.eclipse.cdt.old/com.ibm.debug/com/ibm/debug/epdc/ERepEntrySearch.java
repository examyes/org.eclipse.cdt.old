package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/ERepEntrySearch.java, java-epdc, eclipse-dev, 20011128
// Version 1.5.1.2 (last modified 11/28/01 16:23:17)
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
 * Entry search reply
 */
public class ERepEntrySearch extends EPDC_Reply {

   public ERepEntrySearch() {
      super(EPDC.Remote_EntrySearch);
      _entries = new Vector();
   }

   /**
    * Decode EPDC reply to create a list of ERepEntryGetNext objects.
    * A Function object is constructed based on a ERepEntryGetNext object.
    */

   ERepEntrySearch(byte[] packetBuffer, DataInputStream dataInputStream)
   throws IOException
   {
      super(packetBuffer, dataInputStream);

      int numberOfEntries = dataInputStream.readInt();

      _entries = new Vector(numberOfEntries);

      if (numberOfEntries == 1) // The ERepEntryGetNext is in-place
         _entries.addElement(new ERepEntryGetNext(packetBuffer, dataInputStream));
      else // It's an array of offsets to ERepEntryGetNext
      {
        int offset;

        for (int i = 0; i < numberOfEntries; i++)
            if ((offset = dataInputStream.readInt()) != 0)
               _entries.addElement(new ERepEntryGetNext
                                        (
                                          packetBuffer,
                                          new OffsetDataInputStream (packetBuffer, offset)
                                        )
                                   );
      }
   }


   /**
    * Add an entry
    */
   public void addEntry(ERepEntryGetNext entry) {
      _entries.addElement(entry);
   }

   /**
    * Add an array of entries
    */
   public void addEntries(ERepEntryGetNext[] entries) {
      for (int i=0; i<entries.length; i++)
         addEntry(entries[i]);
   }

   /**
    * Get an array of entries
    */
   public Vector entries()
   {
      return _entries;
   }

   /**
    * Return size of "fixed" portion
    */
   protected int fixedLen() {
      int total = _fixed_length + super.fixedLen();
      if (_entries.size() > 1)
         total += _entries.size() * 4;
      return total;
   }

   /**
    * Return size of "variable" portion
    */
   protected int varLen() {
      int total = super.varLen();
      for (int i=0; i<_entries.size(); i++) {
         ERepEntryGetNext entry = (ERepEntryGetNext) _entries.elementAt(i);
         total += entry.fixedLen() + entry.varLen();
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
      int total = _fixed_length;
      total += super.toDataStreams(fixedData, varData, baseOffset);
      int offset1 = baseOffset + super.varLen();

      writeInt(fixedData, _entries.size());

      int offset2 = offset1 + _entries.size() * ERepEntryGetNext._fixedLen();
      ByteArrayOutputStream varByte2 = new ByteArrayOutputStream();
      DataOutputStream varData2 = new DataOutputStream(varByte2);

      // write out entry information
      for (int i=0; i<_entries.size(); i++) {
         if (_entries.size() > 1) {
            writeOffset(fixedData, offset1);
            total += 4;
         }
         ERepEntryGetNext entry = (ERepEntryGetNext) _entries.elementAt(i);
         total+=entry.toDataStreams(varData, varData2, offset2);
         offset1 += entry.fixedLen();
         offset2 += entry.varLen();
      }

      varData.write(varByte2.toByteArray());      // merge data streams
      return total;
   }

   // data fields
   private static final int _fixed_length = 4;
   private Vector _entries;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}

