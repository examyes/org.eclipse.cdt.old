package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqTypesNumGet.java, java-epdc, eclipse-dev, 20011128
// Version 1.3.1.2 (last modified 11/28/01 16:24:33)
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
 * TypesNumGet Request
 */
public class EReqTypesNumGet extends EPDC_Request {

   EReqTypesNumGet(byte[] inBuffer) throws IOException {
      super(inBuffer);
      _languageId = readInt();
   }

   public EReqTypesNumGet(int languageId)
   {
     super(EPDC.Remote_TypesNumGet);
     _languageId = languageId;
   }

   protected int fixedLen() {
      return _fixed_length + super.fixedLen();
   }

   protected int varLen() {
      return super.varLen();
   }

   /**
    * return the part id
    */
   public int languageId() {
      return _languageId;
   }

   public void output(DataOutputStream dataOutputStream)
      throws IOException
   {
      super.output(dataOutputStream);
      dataOutputStream.writeInt(_languageId);
   }

   // Datafields
   private int _languageId;

   private static final int _fixed_length = 4;
}

