package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqEntryWhere.java, java-epdc, eclipse-dev, 20011128
// Version 1.5.1.2 (last modified 11/28/01 16:24:04)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;

/** Entry where request */
public class EReqEntryWhere extends EPDC_Request
{
   public EReqEntryWhere(int entryID)
   {
     super(EPDC.Remote_EntryWhere);

     _entryID = entryID;
   }

   /**
    * Decodes request from an input buffer
    * @exception IOException if an I/O error occurs
    */
   EReqEntryWhere(byte[] inBuffer) throws IOException {
      super(inBuffer);

      _entryID = readInt();

   }

   public void output(DataOutputStream dataOutputStream)
      throws IOException
   {
      super.output(dataOutputStream);  //for epdc_request_header

      dataOutputStream.writeInt(_entryID);
   }

   /**
    * Return entry ID
    */
   public int entryID() {
      return _entryID;
   }

   protected int fixedLen()
   {
      return _fixed_length + super.fixedLen();
   }

   // Data fields
   private int _entryID;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

   private final static int _fixed_length = 4;
}
