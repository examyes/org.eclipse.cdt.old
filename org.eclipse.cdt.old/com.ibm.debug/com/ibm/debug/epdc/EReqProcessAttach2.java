package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqProcessAttach2.java, java-epdc, eclipse-dev, 20011128
// Version 1.6.1.2 (last modified 11/28/01 16:24:56)
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
 * ProcessAttach2 Request
 */
public class EReqProcessAttach2 extends EPDC_Request {

    /**
    * Given an array of bytes this constructor will decode the request
    *
    */
   EReqProcessAttach2( byte[] inBuffer ) throws IOException {
      super( inBuffer );
      _ReqProcessIndex = readInt();
      _ReqProcessPathOffset = readOffset();
      _ReqPMDBInfoOffset = readOffset();
      if (_ReqPMDBInfoOffset != 0)
         _ReqPMDBInfo = new EPMDBInfo(inBuffer, _ReqPMDBInfoOffset);

      markOffset();   // save current position as the end of fixed part
   }

   public EReqProcessAttach2(int processIndex, String processPath,
                             byte dominantLanguage)
   {
     super(EPDC.Remote_ProcessAttach2);
     _ReqProcessIndex = processIndex;
     _ReqProcessPath = new EStdString(processPath);

     _dominantLanguage = dominantLanguage;
   }

   public int processIndex() {
      return _ReqProcessIndex;
   }

   public String processPath() throws IOException {
      if (_ReqProcessPath == null)
         if (_ReqProcessPathOffset != 0)
         {
            posBuffer(_ReqProcessPathOffset);
            _ReqProcessPath = readStdString();
         }
         else
           return null;

      return _ReqProcessPath.string();
   }

   public int PMLockMessage() throws IOException {
      if (_ReqPMDBInfo == null)
         return 0;

      return _ReqPMDBInfo.PMLockMessage();
   }

   public int PMHandle() {
      if (_ReqPMDBInfo == null)
         return 0;

      return _ReqPMDBInfo.PMHandle();
   }

   public int _PMHandleType() {
      if (_ReqPMDBInfo == null)
         return 0;

      return _ReqPMDBInfo.PMHandleType();
  }

   public byte dominantLanguage()
   {
     return _dominantLanguage;
   }

   /** Return the length of the fixed component */
   protected int fixedLen() {
      if (getEPDCVersion() > 305)
          return _fixed_length + super.fixedLen() + 1;

      return _fixed_length + super.fixedLen();
   }

   /** Return the length of the variable component */
   protected int varLen() {
      return super.varLen() +
             totalBytes(_ReqProcessPath) +
             totalBytes(_ReqPMDBInfo);
   }


  public void output(DataOutputStream dataOutputStream)
    throws IOException
  {
      super.output(dataOutputStream);

      int offset = fixedLen() + super.varLen(); // Our starting offset for writing
                                                // out variable length data

      // Write out the offsets of the variable length data:

      dataOutputStream.writeInt(_ReqProcessIndex);
      offset += writeOffsetOrZero(dataOutputStream, offset, _ReqProcessPath);
      writeOffsetOrZero(dataOutputStream, offset, _ReqPMDBInfo);

      if (getEPDCVersion() > 305)
          dataOutputStream.writeByte(_dominantLanguage);

      // Now write out the variable length data:

      if (_ReqProcessPath != null)
        _ReqProcessPath.output(dataOutputStream);

      if (_ReqPMDBInfo != null)
        _ReqPMDBInfo.output(dataOutputStream);
   }

   private int _ReqProcessIndex;

   private int _ReqProcessPathOffset;
   private EStdString _ReqProcessPath;

   private int _ReqPMDBInfoOffset;
   private EPMDBInfo _ReqPMDBInfo;

   // Eventhough the dominant language is added to the fixed part we will not
   // change the _fixed_length so that we will not break compatibility will
   // EPDC versions that are less than 306. The extra byte is added to the
   // fixedLen method
   private byte _dominantLanguage;

   private static final int _fixed_length = 12;
}
