package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqPartOpen.java, java-epdc, eclipse-dev, 20011128
// Version 1.6.1.2 (last modified 11/28/01 16:24:19)
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
 * Part open request
 */
public class EReqPartOpen extends EPDC_Request {

   /**
    * Decodes request from an input buffer
    * @exception IOException if an I/O error occurs
    */
   EReqPartOpen (byte[] inBuffer) throws IOException {
      super(inBuffer);

      _moduleID = readInt();
      _offsetPartFileName = readOffset();
      _partFileName = null;
   }

   public EReqPartOpen(int id, String partName)
   {
     super(EPDC.Remote_PartOpen);

     _moduleID = id;
     _partFileName = new EStdString(partName);
   }

   /**
    * Return the module ID
    */
   public int moduleID() {
      return _moduleID;
   }

   /**
    * Return the part file name
    * @exception IOException if an I/O error occurs
    */
   public String partFileName() throws IOException {
      if ((_partFileName == null) && (_offsetPartFileName != 0)) {
         posBuffer(_offsetPartFileName);
         _partFileName = readStdString();
      }

      if (_partFileName != null)
         return _partFileName.string();
      else
         return null;
   }

   public void output(DataOutputStream dataOutputStream)
   throws IOException
   {
     super.output(dataOutputStream);
     dataOutputStream.writeInt(_moduleID);

     int offset = fixedLen() + super.varLen();
     writeOffsetOrZero(dataOutputStream, offset, _partFileName);

     if (_partFileName != null)
         _partFileName.output(dataOutputStream);
   }

   /**
    * Return the length of the fixed component
    */
   protected int fixedLen()
   {
     return _fixed_length + super.fixedLen();
   }

   /**
    * Return the length of the variable component
    */
   protected int varLen()
   {
     return totalBytes(_partFileName);
   }

   // data fields
   private int _moduleID;
   private int _offsetPartFileName;
   private EStdString _partFileName;

   private static final int _fixed_length = 8;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";
}

