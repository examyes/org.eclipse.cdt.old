package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/ERepGetChangedStack.java, java-epdc, eclipse-dev, 20011128
// Version 1.11.1.2 (last modified 11/28/01 16:23:29)
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
 * ERepGetChangedStack Change item structure
 */
public class ERepGetChangedStack extends EPDC_ChangeItem {

   /**
    * Create new stack change item
    * @param DU dispatchable unit for stack
    * @param stackStatus stack status @see EPDC
    */
   public ERepGetChangedStack(int DU, int stackStatus) {
      _DU = DU;
      _stackStatus = stackStatus;
      _oldestChangedEntry = 1;
      _stackEntries = new Vector();
   }

  //Decode reply stream.
  //The list of stack entries is obtained here.
  ERepGetChangedStack(byte[] packetBuffer, DataInputStream dataInputStream, EPDC_EngineSession engineSession)
  throws IOException
  {
    //super(packetBuffer, dataInputStream); //Done in StackChangeInfo;

    _DU = dataInputStream.readInt();
    int numberOfStackEntries = dataInputStream.readInt();
    _oldestChangedEntry = dataInputStream.readInt();
    _stackStatus = dataInputStream.readInt();


    //Get an array of ERepGetNextStackEntry
    //Note:The actual # of stack entries in the changed info packet is not the
    //numberOfStackEntries, but (numberOfStackEntries - _oldestChangedEntry + 1)
    //If the whole stack is freed, do not bother retrieving ERepGetNextStackEntry
    if (_stackStatus != EPDC.STACK_ENTRY_DELETE)
    {
      _stackEntries = new Vector(numberOfStackEntries);
      int numberOfStackEntriesInThisPacket = numberOfStackEntries - _oldestChangedEntry + 1;
      if (numberOfStackEntries == 1)
   /*
        defect 14425: offset may be present if numberOfStackEntries > 1
   */
         _stackEntries.addElement(new ERepGetNextStackEntry(packetBuffer, dataInputStream, engineSession));
      else
      {
        int _offset;
        for (int i = 0; i < numberOfStackEntriesInThisPacket; i++)
          if ((_offset = dataInputStream.readInt()) != 0)
            _stackEntries.addElement(new ERepGetNextStackEntry
                                        (
                                          packetBuffer,
                                          new OffsetDataInputStream(packetBuffer, _offset),
                                          engineSession
                                        )
                                    );
      }
    }
  }

  public int DU()
  {
    return _DU;
  }

  public int stackStatus()
  {
    return _stackStatus;
  }

  public int oldestChangedEntry()
  {
    return _oldestChangedEntry;
  }

  public Vector stackEntries()
  {
    return _stackEntries;
  }

  public boolean isNewStack()
  {
    return (_stackStatus & EPDC.STACK_ENTRY_NEW) != 0;
  }

  public boolean isStackDeleted()
  {
    return (_stackStatus & EPDC.STACK_ENTRY_DELETE) != 0;
  }

   /**
    * Set the oldest changed stack entry.  I *believe* that this indicates the entry ID of the first
    * changed stack entry being sent.
    */
   public void setOldestChangedEntry(int oldestChangedEntry) {
      _oldestChangedEntry = oldestChangedEntry;
   }

   /**
    * Add a stack entry
    */
   public void addStackEntry(ERepGetNextStackEntry stackEntry) {
      _stackEntries.addElement(stackEntry);
   }

   /**
    * Add an array of stack entries
    */
   public void addStackEntries(ERepGetNextStackEntry[] stackEntries) {
      for (int i=0; i<stackEntries.length; i++)
         _stackEntries.addElement(stackEntries[i]);
   }

   /**
    * Return size of "fixed" component
    */
   protected int fixedLen() {
      int total = _fixed_length;

      // add offset array
      if (_stackEntries.size() > 1)
         total += _stackEntries.size() * 4;
      else
      if (_stackEntries.size() == 1)
         total += ((ERepGetNextStackEntry) _stackEntries.firstElement()).fixedLen();

      return total;
   }

   /**
    * Return size of "variable" component
    */
   protected int varLen() {
      int total = 0;
      ERepGetNextStackEntry entry;

      if (_stackEntries.size() > 1)
         for (int i=0; i<_stackEntries.size(); i++)
         {
            entry = (ERepGetNextStackEntry) _stackEntries.elementAt(i);
            total += entry.fixedLen() + entry.varLen();
         }
      else
      if (_stackEntries.size() == 1)
         total = ((ERepGetNextStackEntry) _stackEntries.firstElement()).varLen();

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

      int offset1 = baseOffset;
      int offset2 = offset1 + _stackEntries.size() * ERepGetNextStackEntry._fixedLen();
      int total = fixedLen();
      ERepGetNextStackEntry entry;

      ByteArrayOutputStream varByte2 = new ByteArrayOutputStream();
      DataOutputStream varData2 = new DataOutputStream(varByte2);

      writeInt(fixedData, _DU);
      writeInt(fixedData, _stackEntries.size());
      writeInt(fixedData, _oldestChangedEntry);
      writeInt(fixedData, _stackStatus);

      // write offset array if number of elements > 1
      if (_stackEntries.size() > 1)
         for(int i=0; i<_stackEntries.size(); i++) {
            writeOffset(fixedData, offset1);
            entry = (ERepGetNextStackEntry) _stackEntries.elementAt(i);
            total += entry.toDataStreams(varData, varData2, offset2);
            offset1 += ERepGetNextStackEntry._fixedLen();
            offset2 += entry.varLen();
         }
      else if (_stackEntries.size() == 1) {
         entry = (ERepGetNextStackEntry) _stackEntries.elementAt(0);
         total += entry.toDataStreams(fixedData, varData2, offset1);
/*
         total += entry.toDataStreams(varData, varData2, offset2);
         offset1 += ERepGetNextStackEntry._fixedLen();
         offset2 += entry.varLen();
*/
      }

      varData.write(varByte2.toByteArray());
      return total;
   }

   // data fields
   private int _DU;
   private int _stackStatus;
   private int _oldestChangedEntry;
   private Vector _stackEntries;

   private static final int _fixed_length = 16;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}
