package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqInitializeDE.java, java-epdc, eclipse-dev, 20011128
// Version 1.23.1.2 (last modified 11/28/01 16:24:15)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;

// Initialize Debug Engine request
public class EReqInitializeDE extends EPDC_Request {

   /**
    * This creates a default packet.  This constructor will obviously
    * have to be changed when the SUI is implemented.  It is currently
    * only being used for testing.
    */
   public EReqInitializeDE(byte dominantLanguage,
                           String productPrefix,
                           String DEArguments,
                           String remoteSearchPath) {
      super(EPDC.Remote_Initialize_Debug_Engine);

      _debug_frontend_encoding = (byte) EPDC.StrEncode_UTF8;
      _debug_frontend_platform_id = getPlatformIdentifier();

      _language_setting = dominantLanguage;

      String codePage = System.getProperty("file.encoding");

      if (codePage == null)
         codePage = "IBM-850"; // TODO: IS this an appropriate default?

      _codePage = new EStdString(codePage);

      _locale = new EStdString(java.util.Locale.getDefault().toString());

      if (productPrefix != null)
         _filePrefix = new EStdString(productPrefix);

      if (DEArguments != null)
          _DEArguments = new EStdString(DEArguments);

      if (remoteSearchPath != null)
          _remote_search_path = new EStdString(remoteSearchPath);
   }


    /**
    * Given an array of bytes this constructor will decode the request
    *
    */
   EReqInitializeDE( byte[] inBuffer ) throws IOException {
      super( inBuffer );
      _debug_frontend_encoding = readChar();
      readChar(); // reserved
      readChar(); // reserved
      readChar(); // reserved
      _debug_frontend_platform_id = readShort();
      _language_setting = readChar();
      _codePage_offset = readOffset();
      _locale_offset = readOffset();
      _filePrefix_offset = readOffset();
      _DEArguments_offset = readOffset();
      markOffset();   // save current position as the end of fixed part
      if ( _codePage_offset != 0 ) {
         posBuffer( _codePage_offset );
         _codePage = readStdString();
      }
      if ( _locale_offset != 0 ) {
         posBuffer( _locale_offset );
         _locale = readStdString();
      }
      if ( _filePrefix_offset != 0 ) {
         posBuffer( _filePrefix_offset );
         _filePrefix = readStdString();
      }
      if ( _DEArguments_offset != 0 ) {
         posBuffer( _DEArguments_offset );
         _DEArguments = readStdString();
      }

   }

   /**
    * returns debug encoding ID
    *
    */
   public byte debugFrontendEncoding() {
      return _debug_frontend_encoding;
   }

   /**
    * returns debug platform ID
    *
    */
   public int debugFrontendPlatformID() {
      return _debug_frontend_platform_id;
   }

   /**
    * returns language setting
    *
    */
   public byte languageSetting() {
      return _language_setting;
   }

   /**
    * returns a string that represents the Code page
    *
    */
   public String codePage() {
      if (_codePage == null)
         return null;
      else
         return _codePage.string();
   }

   /**
    * returns a string that represents the Locale
    *
    */
   public String locale() {
      if (_locale == null)
         return null;
      else
        return _locale.string();
   }

   /**
    * returns a string that represents the File prefix
    *
    */
   public String filePrefix() {
      if (_filePrefix == null)
         return null;
      else
        return _filePrefix.string();
   }

   /**
    * returns a string that represents the encodings supported
    *
    */
   public String frontEndEncoding() {
      switch (_debug_frontend_encoding) {
         case EPDC.StrEncode_Ext_ASCII:
            return "ASCII";
         case EPDC.StrEncode_UTF8:
            return "UTF8";
         default:
            return "UNKNOWN";
      }
   }

   /**
    * returns a string that represents the OS the frontend is running on
    *
    */
   public String frontEndPlatform () {
      switch (_debug_frontend_platform_id) {
         case EPDC.PLATFORM_ID_OS2:
            return "OS2";
         case EPDC.PLATFORM_ID_MVS:
            return "MVS";
         case EPDC.PLATFORM_ID_VM370:
            return "VM370";
         case EPDC.PLATFORM_ID_AS400:
            return "AS400";
         case EPDC.PLATFORM_ID_AIX:
            return "AIX";
         case EPDC.PLATFORM_ID_NT:
            return "NT";
         case EPDC.PLATFORM_ID_JVM:
            return "JVM";
         case EPDC.PLATFORM_ID_HPUX:
            return "HPUX";
         case EPDC.PLATFORM_ID_SUN:
            return "SUN";
         default:
            return "UNKNOWN";
      }
   }

   /**
    * returns a string that represents the dominant language setting (305 and earlier)
    *
    */
   public String dominantLanguageSetting() {
      switch (_language_setting) {
         case EPDC.LANG_C:
            return "C";
         case EPDC.LANG_CPP:
            return "CPP";
         case EPDC.LANG_PLX86:
            return "PLX86";
         case EPDC.LANG_PLI:
            return "PLI";
         case EPDC.LANG_RPG:
            return "RPG";
         case EPDC.LANG_COBOL:
            return "COBOL";
         case EPDC.LANG_ALP_ASM:
            return "ALP_ASM";
         case EPDC.LANG_OPM_RPG:
            return "OPM_RPG";
         case EPDC.LANG_CL_400:
            return "CL_400";
         case EPDC.LANG_JAVA:
            return "JAVA";
         case EPDC.LANG_FORTRAN:
            return "FORTRAN";
         default:
            return "UNKNOWN";
      }
   }

   /**
    * returns a string that represents the arguments
    *
    */
   public String arguments() {
      if (_DEArguments == null)
         return null;
      else
        return _DEArguments.string();
   }


  public void output(DataOutputStream dataOutputStream)
    throws IOException
  {
      super.output(dataOutputStream);

      dataOutputStream.writeByte(_debug_frontend_encoding);
      dataOutputStream.writeByte(0); // reserved
      dataOutputStream.writeByte(0); // reserved
      dataOutputStream.writeByte(0); // reserved
      dataOutputStream.writeShort(_debug_frontend_platform_id);

      if (getEPDCVersion() < 306)
         dataOutputStream.writeByte(_language_setting);
      else
         dataOutputStream.writeByte((byte)0);

      int offset = fixedLen() + super.varLen(); // Our starting offset for writing
                                                // out variable length data

      // Write out the offsets of the variable length data:

      offset += writeOffsetOrZero(dataOutputStream, offset, _codePage);
      offset += writeOffsetOrZero(dataOutputStream, offset, _locale);
      offset += writeOffsetOrZero(dataOutputStream, offset, _filePrefix);
      offset += writeOffsetOrZero(dataOutputStream, offset, _DEArguments);
                writeOffsetOrZero(dataOutputStream, offset, _remote_search_path);

      // Now write out the variable length data:

      if (_codePage != null)
         _codePage.output(dataOutputStream);

      if (_locale != null)
         _locale.output(dataOutputStream);

      if (_filePrefix != null)
         _filePrefix.output(dataOutputStream);

      if (_DEArguments != null)
         _DEArguments.output(dataOutputStream);

      if (_remote_search_path != null)
         _remote_search_path.output(dataOutputStream);
   }


   /** Return the length of the fixed component */
   protected int fixedLen() {
      return _fixed_length + super.fixedLen();
   }

   /** Return the length of the variable component */
   protected int varLen() {
      return totalBytes(_codePage) +
             totalBytes(_locale) +
             totalBytes(_filePrefix) +
             totalBytes(_DEArguments) +
             totalBytes(_remote_search_path) +
             super.varLen();
   }

   public void write(PrintWriter printWriter) {
      super.write(printWriter);
      increaseIndentLevel();
      if (getDetailLevel() >= DETAIL_LEVEL_MEDIUM) {
         indent(printWriter);
         printWriter.println("FE string encoding: " + frontEndEncoding() );
         indent(printWriter);
         printWriter.println("Front End Platform: " + frontEndPlatform () );
         indent(printWriter);
         printWriter.println("CodePage of front end: " + codePage() );
         indent(printWriter);
         printWriter.println("Locale of front end: " + locale() );
         indent(printWriter);
         printWriter.println("Prefix characters: " + filePrefix() );
         if (getEPDCVersion() < 306)
           indent(printWriter);
           printWriter.println("Dominant Language setting: " + dominantLanguageSetting() );
      }
      decreaseIndentLevel();
   }


   private byte         _debug_frontend_encoding;
   private short        _debug_frontend_platform_id;
   private byte         _language_setting; //only relevant in version 305 and earlier
   private int          _codePage_offset;
   private EStdString   _codePage;
   private int          _locale_offset;
   private EStdString   _locale;
   private int          _filePrefix_offset;
   private EStdString   _filePrefix;
   private int          _DEArguments_offset;
   private EStdString   _DEArguments;
   private int          _remote_search_path_offset;
   private EStdString   _remote_search_path;

   private static final int _fixed_length = 27;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}