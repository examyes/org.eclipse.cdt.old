package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqProcessDetailsGet.java, java-epdc, eclipse-dev, 20011128
// Version 1.2.1.2 (last modified 11/28/01 16:24:58)
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
 * ProcessDetailsGet Request
 */
public class EReqProcessDetailsGet extends EPDC_Request {

   EReqProcessDetailsGet(byte[] inBuffer) throws IOException {
      super(inBuffer);
   }

   public EReqProcessDetailsGet()
   {
     super(EPDC.Remote_ProcessDetailsGet);
   }

   public void output(DataOutputStream dataOutputStream)
    throws IOException
   {
      super.output(dataOutputStream);
   }
}

