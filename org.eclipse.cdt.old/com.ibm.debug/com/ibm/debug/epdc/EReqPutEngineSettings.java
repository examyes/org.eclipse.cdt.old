package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqPutEngineSettings.java, java-epdc, eclipse-dev, 20011128
// Version 1.1.1.2 (last modified 11/28/01 16:26:10)
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
 *  Remote_PutEngineSettings request
 */
public class EReqPutEngineSettings extends EPDC_Request
{
   public EReqPutEngineSettings(byte[] XMLStream)
   {
     super(EPDC.Remote_PutEngineSettings);

     _XMLStream = new EExtString(XMLStream);
   }

   /**
    * Return the size of the XML stream
    */
   public int streamLength() {
      return _XMLStream.streamLength();
   }

   /**
    * Return a handle to the XML stream
    * @exception IOException if an I/O error occurs
    */
   public String XMLStream() throws IOException {
      if ((_XMLStream == null) && (_XMLStreamOffset != 0)) {
         posBuffer(_XMLStreamOffset);
         _XMLStream = readExtString();
      }

      if (_XMLStream == null)
          return null;

      return _XMLStream.string();
   }

   public void output(DataOutputStream dataOutputStream)
   throws IOException
   {
     super.output(dataOutputStream);

     int offset = fixedLen() + super.varLen();

     writeOffsetOrZero(dataOutputStream, offset, _XMLStream);

     if (_XMLStream != null)
         _XMLStream.output(dataOutputStream);
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
     return super.varLen() + totalBytes(_XMLStream);
   }

   // data fields
   private int _XMLStreamOffset;
   private EExtString _XMLStream;

   private static final int _fixed_length = 4;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";
}
