package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/ERepEntryGetNext.java, java-epdc, eclipse-dev, 20011128
// Version 1.10.1.2 (last modified 11/28/01 16:23:16)
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
 * ERepEntryGetNext structure
 */
public class ERepEntryGetNext extends EPDC_Base {

   public ERepEntryGetNext(int entryID, String entryName, String demangledName, String entryReturnType,
            EStdView eStdView) {
      _entryID = entryID;
      _entryName = new EStdString(entryName);
      _demangledName = new EStdString(demangledName);
      _entryReturnType = new EStdString(entryReturnType);
      _EStdView = eStdView;
   }

   ERepEntryGetNext (byte[] packetBuffer, DataInputStream dataInputStream)
   throws IOException
   {
      _entryID = dataInputStream.readInt();  //Entry ID

      int offset;

      if ((offset = dataInputStream.readInt()) != 0)  //Offset to entry name
         _entryName = new EStdString(packetBuffer,
                                      new OffsetDataInputStream(packetBuffer, offset)
                                      );

      if ((offset = dataInputStream.readInt()) != 0)  //Offset to demangled entryname
         _demangledName = new EStdString(packetBuffer,
                                      new OffsetDataInputStream(packetBuffer, offset)
                                      );

      if ((offset = dataInputStream.readInt()) != 0)  //Offset to entry return type
         _entryReturnType = new EStdString(packetBuffer,
                                      new OffsetDataInputStream(packetBuffer, offset)
                                      );

      _EStdView = new EStdView(packetBuffer, dataInputStream);   //Context of the entry point
   }

   public int getEntryID()
   {
      return _entryID;
   }

   public String getEntryName()
   {
      if (_entryName != null)
        return _entryName.string();
      else
        return null;
   }

   public String getDemangledName()
   {
      if (_demangledName != null)
        return _demangledName.string();
      else
        return null;
   }

   public String getEntryReturnType()
   {
      if (_entryReturnType != null)
        return _entryReturnType.string();
      else
        return null;
   }

   public EStdView getEStdView()
   {
      return _EStdView;
   }

   public void setContext(EStdView context)
   {
      _EStdView = context;
   }

   protected int fixedLen() {
      return _fixed_length + _EStdView.fixedLen();
   }

   protected static int _fixedLen() {
     return _fixed_length + EStdView._fixedLen();
   }

   protected int varLen() {
      return totalBytes(_entryName) + totalBytes(_demangledName) +
             totalBytes(_entryReturnType) + super.varLen();
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

      writeInt(fixedData, _entryID);

      offset += writeOffsetOrZero(fixedData, offset, _entryName);
      offset += writeOffsetOrZero(fixedData, offset, _demangledName);
                writeOffsetOrZero(fixedData, offset, _entryReturnType);

      if (_entryName != null)
         _entryName.output(varData);

      if (_demangledName != null)
         _demangledName.output(varData);

      if (_entryReturnType != null)
         _entryReturnType.output(varData);

      _EStdView.toDataStreams(fixedData, null, 0);

      return fixedLen() + varLen();
   }


   // Data fields
   private int _entryID;
   private EStdString _entryName;
   private EStdString _demangledName;
   private EStdString _entryReturnType;
   private EStdView _EStdView;

   private static final int _fixed_length = 16;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}

