package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/ERepNextPart.java, java-epdc, eclipse-dev, 20011128
// Version 1.13.1.2 (last modified 11/28/01 16:23:41)
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

public class ERepNextPart extends EPDC_ChangeItem {

   public ERepNextPart (EPDC_EngineSession engineSession, short Partid, int PartAttr, byte PartLang,
         String PartName, String PartFileName, String PartPathName, int ModuleID)
   {
      super();
      _engineSession = engineSession;
      _Partid = Partid;
      _PartAttr = (byte) PartAttr;
      _PartLang = PartLang;
      _PartName = new EStdString(PartName);
      _PartFileName = new EStdString(PartFileName);
      _PartPathName = new EStdString(PartPathName);
      _ModuleID = ModuleID;
      _Viewdata = new EViewData[_engineSession._viewInfo.length];        // set up array

      _viewNo = 0;
   }

   ERepNextPart(byte[] packetBuffer, DataInputStream dataInputStream, EPDC_EngineSession engineSession)
   throws IOException
   {
     _Partid = dataInputStream.readShort();
     _PartAttr = dataInputStream.readByte();
     _PartLang = dataInputStream.readByte();
     dataInputStream.readInt(); // reserved

     int offset;

     if ((offset = dataInputStream.readInt()) != 0) // Offset to PartName
        _PartName = new EStdString(packetBuffer, offset);

     dataInputStream.readInt(); // Offset to PartFileName
     dataInputStream.readInt(); // Offset to PartPathName
     _ModuleID = dataInputStream.readInt();

     _Viewdata = new EViewData[engineSession._viewInfo.length];

     for (int i = 0; i < engineSession._viewInfo.length; i++)
         _Viewdata[i] = new EViewData(packetBuffer, dataInputStream);
   }

   /** Create information for a new view.
     * @return the view number, if no more available views (as specified by
     * EPDC_EngineSession._NViews), returns -1. */
   public int createView(byte Prefixl, int ViewDataAttr) {
      if (_viewNo < _engineSession._viewInfo.length) {
         _Viewdata[_viewNo] = new EViewData(Prefixl, ViewDataAttr);
         return _viewNo++;        // return _viewNo then increment it
      }
      return -1;
   }

   /** Add a source file to a particular view */
   public void addSrcFile(int viewNo, int RecLength, int Startline, int Endline, String Filename,
         String BaseFilename,  int ViewAttr) {
      _Viewdata[viewNo].AddSrcFile(RecLength, Startline, Endline, Filename,
            BaseFilename, ViewAttr);      // create corresponding EViews element
   }

   public short id()
   {
     return _Partid;
   }

   public String name()
   {
     if (_PartName != null)
       return _PartName.string();
     else
       return null;
   }

   public int owningModuleID()
   {
     return _ModuleID;
   }

   public boolean isDeletedPart()
   {
     return (_PartAttr & EPDC.PartDeleted) != 0;
   }

   public boolean isNewPart()
   {
     return (_PartAttr & EPDC.PartNew) != 0;
   }

   public boolean hasBeenVerified()
   {
     return (_PartAttr & EPDC.Verified) != 0;
   }

   /** Return value may not be reliable if part has not been verified - as
    *  far as I can tell, lang is not set until part has been verified.
    */

   public byte language()
   {
     return _PartLang;
   }

   /** As far as I can tell, a part does not have to be verified in order to
    *  know if it has debug info.
    */

   public boolean hasDebugInfo()
   {
     return (_PartAttr & EPDC.SymbolTbl) != 0;
   }

   public EViewData[] views()
   {
     return _Viewdata;
   }

   protected int toDataStreams(DataOutputStream fixedData,
         DataOutputStream varData, int baseOffset)
         throws IOException, BadEPDCCommandException {

      int offset = baseOffset;
      writeShort(fixedData, _Partid);
      writeChar(fixedData, _PartAttr);
      writeChar(fixedData, _PartLang);
      writeInt(fixedData, 0);

      offset += writeOffsetOrZero(fixedData, offset, _PartName);

      if (_PartName != null)
         _PartName.output(varData);

      offset += writeOffsetOrZero(fixedData, offset, _PartFileName);

      if (_PartFileName != null)
         _PartFileName.output(varData);

      offset += writeOffsetOrZero(fixedData, offset, _PartPathName);

      if (_PartPathName != null)
         _PartPathName.output(varData);

      writeInt(fixedData, _ModuleID);

      for (int i=0; i<_engineSession._viewInfo.length; i++) {
         _Viewdata[i].toDataStreams(fixedData, varData, offset);   // write array
         offset += _Viewdata[i].varLen();
      }

      return fixedLen() + varLen();
   }

   /**
    * Return the length of the "fixed" component.  The "fixed"
    * component sized is not actually "fixed" because it incldues
    * a variable sized array of view information.
    */
   protected int fixedLen() {
      int total = _fixed_length;
      for (int i=0; i < _engineSession._viewInfo.length; i++)
         total += _Viewdata[i].fixedLen();
      return total;
   }

      /** Return the length of the "variable" component */
   protected int varLen() {
      int total = 0;

      total += totalBytes(_PartName);
      total += totalBytes(_PartFileName);
      total += totalBytes(_PartPathName);

      for (int i=0; i < _engineSession._viewInfo.length; i++)
         total += _Viewdata[i].varLen();

      return total;
   }

   /* Data members */
   EPDC_EngineSession _engineSession;
   short _Partid;
   byte _PartAttr;
   byte _PartLang;
   EStdString _PartName;
   EStdString _PartFileName;
   EStdString _PartPathName;
   int _ModuleID;
   EViewData[] _Viewdata;

   int _viewNo;

   private static int _fixed_length = 24;


   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}

