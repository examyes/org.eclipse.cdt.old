package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/ERepNextModuleEntry.java, java-epdc, eclipse-dev, 20011128
// Version 1.12.1.2 (last modified 11/28/01 16:23:40)
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

/** Class for module entry change item */
public class ERepNextModuleEntry extends EPDC_ChangeItem {

   public ERepNextModuleEntry (int ModuleID, String ModuleName, String FullPathModuleName,
            int Flags) {
      super();
      _ModuleID = ModuleID;
      _ModuleName = new EStdString(ModuleName);
      _FullPathModuleName = new EStdString(FullPathModuleName);
      _Flags = Flags;
      // _PartID = new Vector();
   }

   ERepNextModuleEntry(byte[] packetBuffer, DataInputStream dataInputStream)
   throws IOException
   {
     _Flags = dataInputStream.readByte();
     _ModuleID = dataInputStream.readInt();

     int offset;

     if ((offset = dataInputStream.readInt()) != 0) // Offset to module name
  _ModuleName = new EStdString
          (
            packetBuffer,
            new OffsetDataInputStream (packetBuffer, offset)
          );

     if ((offset = dataInputStream.readInt()) != 0) // Offset to full path module name
  _FullPathModuleName = new EStdString
          (
            packetBuffer,
            new OffsetDataInputStream (packetBuffer, offset)
          );

     // Ignore part ids for now, each of which is 2 bytes long:

     dataInputStream.skipBytes(2 * dataInputStream.readShort());

   }

   public boolean isNewModule()
   {
     return (_Flags & EPDC.ModuleEntryNew) != 0;
   }

   public boolean hasBeenDeleted()
   {
     return (_Flags & EPDC.ModuleEntryDeleted) != 0;
   }

   public boolean hasDebugData()
   {
     return (_Flags & EPDC.ModuleEntryHasDebugData) != 0;
   }

   public int moduleID()
   {
     return  _ModuleID;
   }

   public String moduleName()
   {
     if (_ModuleName != null)
       return  _ModuleName.string();
     else
       return null;
   }

   public String fullPathModuleName()
   {
     if (_FullPathModuleName != null)
       return  _FullPathModuleName.string();
     else
       return null;
   }

   /**
    * @deprecated Part IDs are no longer added to the module change item.
    */

   public void addPartID(int partID) {
      // _PartID.addElement(new Integer(partID));
   }

   protected int toDataStreams(DataOutputStream fixedData,
         DataOutputStream varData, int baseOffset)
         throws IOException, BadEPDCCommandException {

      int offset = baseOffset;
      writeChar(fixedData, (byte)_Flags);
      writeInt(fixedData, _ModuleID);

      offset += writeOffsetOrZero(fixedData, offset, _ModuleName);

      if (_ModuleName != null)
         _ModuleName.output(varData);

      offset += writeOffsetOrZero(fixedData, offset, _FullPathModuleName);

      if (_FullPathModuleName != null)
         _FullPathModuleName.output(varData);

      // writeShort(fixedData, (short) _PartID.size());
      writeShort(fixedData, (short) 0);

      // for (int i=0; i<_PartID.size(); i++)
         // writeShort(fixedData, ((Integer) _PartID.elementAt(i)).shortValue());

      return fixedLen() + varLen();
   }

   /**
    * Return the length of the "fixed" component.
    */
   protected int fixedLen() {
      return _fixed_length; // + 2*_PartID.size();
   }

      /** Return the length of the "variable" component */
   protected int varLen() {
      int total = 0;

      total += totalBytes(_ModuleName);
      total += totalBytes(_FullPathModuleName);

      return total;
   }

   /* Data members */
   int _Flags;
   int _ModuleID;
   EStdString _ModuleName;
   EStdString _FullPathModuleName;

   // Vector _PartID;

   private static int _fixed_length = 15;


   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}

