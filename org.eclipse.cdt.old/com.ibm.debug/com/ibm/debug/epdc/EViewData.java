package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EViewData.java, java-epdc, eclipse-dev, 20011128
// Version 1.10.1.2 (last modified 11/28/01 16:24:44)
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

public class EViewData extends EPDC_Base{

   EViewData (byte Prefixl, int ViewDataAttr) {
      super();
      _SrcFilesForView = (short) 0;
      _Prefixl = Prefixl;
      _ViewDataAttr = (byte) ViewDataAttr;
      _Viewsinfo = new Vector();
   }

   EViewData(byte[] packetBuffer, DataInputStream dataInputStream)
   throws IOException
   {
     _SrcFilesForView = dataInputStream.readShort(); // Number of source files
     _Prefixl = dataInputStream.readByte();  // Prefix length
     dataInputStream.readByte();  // reserved
     _ViewDataAttr = dataInputStream.readByte();  // View data attribute
     dataInputStream.readByte();  // reserved

      _Viewsinfo = new Vector();

     // My understanding is that there is always at least one array element
     // here even if # of files == 0. We need to skip that one element:

     if (_SrcFilesForView == 0)
        new EViews(packetBuffer, dataInputStream);
     else
       for (int i = 0; i < _SrcFilesForView; i++)
           _Viewsinfo.addElement(new EViews(packetBuffer, dataInputStream));
   }

   // As far as I can tell, if a view says "not validated" this means that the
   // debug engine knows that it cannot build this view for this part. For
   // example, the source view will be "not validated" if the part does not
   // contain a line number table. As another example, the Intel debug engine
   // (PICL) appears to always say "validated" for the disassembly view for
   // every part because it knows that it can always build that view.

   // Note that the debug engine may still not be able to build a validated
   // view if the files in that view cannot be VERIFIED. For example, a part
   // may contain debug data so the source view will be VALIDATED, but if the
   // source files for that part cannot be found then the VERIFICATION of
   // those files will fail and the source view cannot be built.

   // It also appears that there is not alot of information available for a
   // view until the files in the view have been VERIFIED i.e. validation by
   // itself does not tell us much about the view other than the debug engine
   // will try to build this view when asked to do so. For example, until the
   // view has been VERIFIED, we do not know the names of the files in the
   // view nor even how many files are in the view. We also do not know the
   // programming language of the part.


   public boolean validated()
   {
     return (_ViewDataAttr & EPDC.VIEW_VALIDATED) != 0;
   }

   public Vector files()
   {
     return _Viewsinfo;
   }

   public byte prefixLength()
   {
     return _Prefixl;
   }

   void AddSrcFile(int RecLength, int Startline, int Endline, String Filename,
         String BaseFilename,  int ViewAttr) {
      _SrcFilesForView++;     // increment count
      _Viewsinfo.addElement(new EViews(RecLength, Startline, Endline, Filename,
            BaseFilename, ViewAttr));      // create correspond EViews element
   }
    /**
    * Output the command to data streams.
    * @exception IOException if an I/O error occours.
    * @exception BadEPDCCommandException if the EPDC command
    *    is structured incorrectly
    */
   protected int toDataStreams(DataOutputStream fixedData,
         DataOutputStream varData, int baseOffset)
         throws IOException, BadEPDCCommandException {

      int offset = baseOffset;
      writeShort(fixedData, _SrcFilesForView);
      writeChar(fixedData, _Prefixl);
      writeChar(fixedData, (byte) 0);     // reserved
      writeChar(fixedData, (byte) _ViewDataAttr);
      writeChar(fixedData, (byte) 0);

      if (_Viewsinfo.size() == 0)
         _Viewsinfo.addElement(new EViews(0, 0, 0, null, null, 0));

      Enumeration en = _Viewsinfo.elements();
      EViews ev;
      while (en.hasMoreElements()) {
         ev = (EViews) en.nextElement();
         ev.toDataStreams(fixedData, varData, offset);
         offset += ev.varLen();
      }

      return fixedLen() + varLen();
   }

   /**
    * Return the length of the fixed component.  The "fixed"
    * component sized is not actually "fixed" because it incldues
    * a variable sized vector of source file information.
    */
   protected int fixedLen() {
      return _fixed_length + 22 * (_SrcFilesForView == 0 ? 1 : _SrcFilesForView);
   }

      /** Return the length of the variable component */
   protected int varLen() {
      int total = 0;
      Enumeration e = _Viewsinfo.elements();
      EViews ev;

      while (e.hasMoreElements()) {
         ev = (EViews) e.nextElement();
         total += ev.varLen();
      }
      return total;
   }

   /* Data members */
   private short _SrcFilesForView;
   private byte _Prefixl;
   private byte _ViewDataAttr;
   private Vector _Viewsinfo;

   private static int _fixed_length = 6;

}

