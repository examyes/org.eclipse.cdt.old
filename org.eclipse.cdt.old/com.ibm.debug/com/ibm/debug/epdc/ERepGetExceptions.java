package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/ERepGetExceptions.java, java-epdc, eclipse-dev, 20011128
// Version 1.10.1.2 (last modified 11/28/01 16:23:30)
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
  * Exception information that is sent back with ERepInitializeDE
  */
public class ERepGetExceptions extends EPDC_Base {

 /**
  * decode a reply from a buffer
  *
  */
  ERepGetExceptions(byte[] packetBuffer, DataInputStream dataInputStream )
  throws IOException
  {
    _exceptionStatus = dataInputStream.readInt();

    int offset;

    if ((offset = dataInputStream.readInt()) != 0)
       _exceptionName = new EStdString(packetBuffer,
                                       offset);
  }

   public ERepGetExceptions(int exceptionStatus, String exceptionName) {
      super();
      _exceptionStatus = exceptionStatus;
      _exceptionName = new EStdString(exceptionName);
   }

   /**
    * Set exception status
    */
   public void setExceptionStatus(int exceptionStatus) {
      _exceptionStatus = exceptionStatus;
   }


   /**
    * Return exception status
    */
   public int exceptionStatus() {
      return _exceptionStatus;
   }

   /**
    * Set exception name
    */
   public void setExceptionName(String exceptionName) {
      _exceptionName = new EStdString(exceptionName);
   }

   /**
    * Return exception name
    */
   public EStdString exceptionName()
   {
      return _exceptionName;
   }

   /** Output class to data streams according to EPDC protocol
     * @exception IOException if an I/O error occurs
    *  @exception BadEPDCCommandException if the EPDC command
    *    is structured incorrectly
    */
   protected int toDataStreams(DataOutputStream fixedData,
         DataOutputStream varData, int baseOffset)
         throws IOException, BadEPDCCommandException {

      int total = _fixed_length;

      writeInt(fixedData, _exceptionStatus);

      writeOffsetOrZero(fixedData, baseOffset, _exceptionName);

      if (_exceptionName != null)
         _exceptionName.output(varData);

      return total;
   }

   /** Return length of fixed portion */
   protected int fixedLen() {
      return _fixed_length;
   }

   /** Return length of fixed portion -- static function*/
   protected static int _fixedLen() {
      return _fixed_length;
   }

   /** Return length of variable portion */
   protected int varLen() {
      return super.varLen() + totalBytes(_exceptionName);
   }

   public void write(PrintWriter printWriter) {
      indent(printWriter);
      printWriter.print("ExceptionStatus: " + exceptionStatus() );
      printWriter.println("    Name: " + _exceptionName.string() );
   }

   // data fields
   private int _exceptionStatus;
   private EStdString _exceptionName;

   private static final int _fixed_length = 8;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}