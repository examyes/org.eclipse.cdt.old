package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/ERepGetLanguages.java, java-epdc, eclipse-dev, 20011128
// Version 1.10.1.2 (last modified 11/28/01 16:23:32)
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
  * Language information that is sent back with ERepInitializeDE
  */
public class ERepGetLanguages extends EPDC_Base {

   public ERepGetLanguages(int langID, String language) {
      super();
      _langID = langID;
      _language = new EStdString(language);
   }

   public ERepGetLanguages(byte[] packetBuffer,DataInputStream dataInputStream)
   throws IOException
   {
     _langID = dataInputStream.readInt();

     int offset;
     if ((offset = dataInputStream.readInt()) != 0)
     {
         _language = new EStdString(packetBuffer,
                                    new OffsetDataInputStream(packetBuffer,
                                                              offset)
                                   );
     }
   }

   /**
    * Return the name of language
    */
   public String getLanguageName()
   {
     if (_language != null)
       return _language.string();
     else
       return null;
   }

   /**
    * Return the id of the language that the backend provides
    */
   public int getLanguageID()
   {
     return _langID;
   }

   /** Output class to data streams according to EPDC protocol
     * @exception IOException if an I/O error occurs
    *  @exception BadEPDCCommandException if the EPDC command
    *    is structured incorrectly
    */
   protected int toDataStreams(DataOutputStream fixedData,
         DataOutputStream varData, int baseOffset)
         throws IOException, BadEPDCCommandException {

      writeInt(fixedData, _langID);

      writeOffsetOrZero(fixedData, baseOffset, _language);

      if (_language != null)
         _language.output(varData);

      return fixedLen() + varLen();
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
      return super.varLen() + totalBytes(_language);
   }

   public void write(PrintWriter printWriter) {
      indent(printWriter);
      printWriter.print("LangId: " + _langID );
      printWriter.println("      LangName: " + getLanguageName() );
   }

   private int _langID;
   private EStdString _language;

   private static final int _fixed_length = 8;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}