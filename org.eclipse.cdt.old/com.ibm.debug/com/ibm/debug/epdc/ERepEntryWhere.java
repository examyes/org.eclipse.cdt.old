package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/ERepEntryWhere.java, java-epdc, eclipse-dev, 20011128
// Version 1.5.1.2 (last modified 11/28/01 16:23:18)
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
 * Entry Where reply
 */
public class ERepEntryWhere extends EPDC_Reply
{
   ERepEntryWhere(byte[] packetBuffer, DataInputStream dataInputStream, EPDC_EngineSession engineSession)
   throws IOException
   {
      super(packetBuffer, dataInputStream);

      dataInputStream.readInt();  // reserved bytes

      _context = new Vector(engineSession._viewInfo.length);

      for (int i = 0; i < engineSession._viewInfo.length; i++)
          _context.addElement(new EStdView(packetBuffer, dataInputStream));
   }

   /**
    * Create new reply item
    * @param session tells how many views there are
    */
   public ERepEntryWhere() {
      super(EPDC.Remote_EntryWhere);
      _context = new Vector();
   }

   public void addContextInfo(short PPID, short View, int SrcFileIndex, int LineNum) {
      _context.addElement(new EStdView(PPID, View, SrcFileIndex, LineNum));
   }

   protected int fixedLen() {
      return super.fixedLen() + _fixed_length;
   }

   protected int varLen() {
      return EStdView._fixedLen() * _context.size();
   }

   public Vector getContexts()
   {
     return _context;
   }

   /**
    * Output class to data streams according to EPDC protocol.
    * @exception IOException if an I/O error occurs
    * @exception BadEPDCCommandException if the EPDC command
    *   is structured incorrectly
    */
   protected int toDataStreams(DataOutputStream fixedData,
         DataOutputStream varData, int baseOffset)
         throws IOException, BadEPDCCommandException {

      super.toDataStreams(fixedData, varData, baseOffset);
      writeInt(fixedData,_context.size());

      for (int i=0; i<_context.size(); i++)
         ((EStdView)_context.elementAt(i)).toDataStreams(varData, null, 0);

      return fixedLen() + varLen();
   }

   // Data fields
   private int _fixed_length = 4;
   private Vector _context;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}
