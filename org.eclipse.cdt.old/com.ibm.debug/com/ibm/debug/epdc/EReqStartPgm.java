package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqStartPgm.java, java-epdc, eclipse-dev, 20011128
// Version 1.5.1.2 (last modified 11/28/01 16:24:27)
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
 * Start Program Request.  This contains know fixed or variable components,
 * only the request header.
 */
public class EReqStartPgm extends EPDC_Request
{
   public EReqStartPgm()
   {
     super(EPDC.Remote_StartPgm);
   }

   EReqStartPgm(byte[] inBuffer) throws IOException {
      super (inBuffer);
   }


   /** Return the length of the fixed component */
   protected int fixedLen() {
      return super.fixedLen();
   }

   /** Return the length of the variable component */
   protected int varLen() {
      return super.varLen();
   }

}
