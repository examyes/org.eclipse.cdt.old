package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqVersion.java, java-epdc, eclipse-dev, 20011128
// Version 1.6.1.2 (last modified 11/28/01 16:25:02)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;
import com.ibm.debug.connection.*;

/**
 * Class for Version request.
 */
public class EReqVersion extends EPDC_Request
{
   public EReqVersion(int uiVersion)
   {
     super(EPDC.Remote_Version);
     _front_end_version = uiVersion;
   }

   /**
    * Construct a new EReqVersion from a byte buffer
    */
   public EReqVersion(byte[] inBuffer) throws IOException {
       super(inBuffer);
      _front_end_version    = readInt();
   }

   /**
    * Get the front end version
    */
   public int getFrontEndVersion() {
      return _front_end_version;
   }

   /**
    * Get the front end version
    */
   public void setFrontEndVersion(int front_end_version) {
      _front_end_version = front_end_version;
   }

   public void output(DataOutputStream dataOutputStream)
   throws IOException
   {
      super.output(dataOutputStream);
      dataOutputStream.writeInt(_front_end_version);
   }

   /** Return the length of the fixed component */
   protected int fixedLen() {
      return _fixed_length + super.fixedLen();
   }

   public void write(PrintWriter printWriter)
   {
     super.write(printWriter);

     indent(printWriter);
     printWriter.println("UI EPDC Version: " + _front_end_version);
   }

   // data fields
   private int _front_end_version;
   private int _fixed_length = 4;
}
