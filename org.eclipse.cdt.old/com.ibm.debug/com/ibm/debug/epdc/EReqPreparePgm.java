package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqPreparePgm.java, java-epdc, eclipse-dev, 20011128
// Version 1.14.1.2 (last modified 11/28/01 16:24:21)
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
 * Prepare Program Request
 */
public class EReqPreparePgm extends EPDC_Request {

    /**
    * Given an array of bytes this constructor will decode the request
    *
    */
   EReqPreparePgm( byte[] inBuffer ) throws IOException {
      super( inBuffer );
      _ReqPgmNameOffset = readOffset();
      _ReqPgmParmsOffset = readOffset();
      _ReqCmdNameOffset = readOffset();
      _ReqCmdParmsOffset = readOffset();
      _ReqChildNamesOffset = readOffset();
      _ReqJobNameOffset = readOffset();
      _ReqHostAddressOffset = readOffset();

      _ReqPMDBInfoOffset = readOffset();
      if (_ReqPMDBInfoOffset != 0)
         _ReqPMDBInfo = new EPMDBInfo(inBuffer, _ReqPMDBInfoOffset);

      _programType = readInt();
      if (getEPDCVersion() == 305)
          _platformInfoOffset = readOffset();
      else
      {
         _languageSetting = readChar();
         readChar();
         readChar();
         readChar();
      }

      markOffset();   // save current position as the end of fixed part
   }

   public EReqPreparePgm(String pgmName, String arguments,
                         String jobName, boolean runToMain,
                         byte dominantLanguage)
   {
     super(EPDC.Remote_PreparePgm);
     _ReqPgmName = new EStdString(pgmName);

     if (arguments != null && arguments.length() != 0)
        _ReqPgmParms = new EStdString(arguments);

     _ReqCmdName = null;
     _ReqCmdParms = null;

     _ReqChildNames = null;

     if (jobName != null)
         _ReqJobName = new EStdString(jobName);

     _ReqPMDBInfo = null;

     if (runToMain)
         _programType = EPDC.DefaultPgmType;      //DefaultPgmType
     else
         _programType = EPDC.ServicePgm;          //program is not run yet

     _languageSetting = dominantLanguage;
   }

   public String reqPgmName() throws IOException {
      if (_ReqPgmName == null)
         if (_ReqPgmNameOffset != 0)
         {
            posBuffer(_ReqPgmNameOffset);
            _ReqPgmName = readStdString();
         }
         else
           return null;

      return _ReqPgmName.string();
   }

   public String reqPgmParms() throws IOException {
      if (_ReqPgmParms == null)
         if (_ReqPgmParmsOffset != 0)
         {
            posBuffer(_ReqPgmParmsOffset);
            _ReqPgmParms = readStdString();
         }
        else
          return null;

      return _ReqPgmParms.string();
   }

   public String reqCmdName() throws IOException {
      if (_ReqCmdName == null)
         if (_ReqCmdNameOffset != 0)
         {
            posBuffer(_ReqCmdNameOffset);
            _ReqCmdName = readStdString();
         }
         else
           return null;

      return _ReqCmdName.string();
   }

   public String reqCmdParms() throws IOException {
      if (_ReqCmdParms == null)
         if (_ReqCmdParmsOffset != 0)
         {
            posBuffer(_ReqCmdParmsOffset);
            _ReqCmdParms = readStdString();
         }
         else
           return null;

      return _ReqCmdParms.string();
   }

   public String reqChildNames() throws IOException {
      if (_ReqChildNames == null)
         if (_ReqChildNamesOffset != 0)
         {
            posBuffer(_ReqChildNamesOffset);
            _ReqChildNames = readStdString();
         }
         else
           return null;

      return _ReqChildNames.string();
   }

   public String reqJobName() throws IOException {
      if (_ReqJobName == null)
         if (_ReqJobNameOffset != 0)
         {
            posBuffer(_ReqJobNameOffset);
            _ReqJobName = readStdString();
         }
         else
           return null;

      return _ReqJobName.string();
   }

   public String reqHostAddress() throws IOException {
      if (_ReqHostAddress == null)
         if (_ReqHostAddressOffset != 0)
         {
            posBuffer(_ReqHostAddressOffset);
            _ReqHostAddress = readStdString();
         }
         else
           return null;

      return _ReqHostAddress.string();
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

   public byte languageSetting()
   {
     return _languageSetting;
   }

   /** Return the length of the fixed component */
   protected int fixedLen() {
      return _fixed_length + super.fixedLen();
   }

   /** Return the length of the variable component */
   protected int varLen() {
      return super.varLen() +
             totalBytes(_ReqPgmName) +
             totalBytes(_ReqPgmParms) +
             totalBytes(_ReqCmdName) +
             totalBytes(_ReqCmdParms) +
             totalBytes(_ReqChildNames) +
             totalBytes(_ReqJobName) +
             totalBytes(_ReqHostAddress) +
             totalBytes(_ReqPMDBInfo);
   }


  public void output(DataOutputStream dataOutputStream)
    throws IOException
  {
      super.output(dataOutputStream);

      int offset = fixedLen() + super.varLen(); // Our starting offset for writing
                                                // out variable length data

      // Write out the offsets of the variable length data:

      offset += writeOffsetOrZero(dataOutputStream, offset, _ReqPgmName);
      offset += writeOffsetOrZero(dataOutputStream, offset, _ReqPgmParms);
      offset += writeOffsetOrZero(dataOutputStream, offset, _ReqCmdName);
      offset += writeOffsetOrZero(dataOutputStream, offset, _ReqCmdParms);
      offset += writeOffsetOrZero(dataOutputStream, offset, _ReqChildNames);
      offset += writeOffsetOrZero(dataOutputStream, offset, _ReqJobName);
      offset += writeOffsetOrZero(dataOutputStream, offset, _ReqHostAddress);
                writeOffsetOrZero(dataOutputStream, offset, _ReqPMDBInfo);

      dataOutputStream.writeInt(_programType);

      if (getEPDCVersion() == 305)
          writeOffsetOrZero(dataOutputStream,offset, null);   // Platform Info
      else
      {
         dataOutputStream.writeByte(_languageSetting);
         dataOutputStream.writeByte(0); // reserved
         dataOutputStream.writeByte(0); // reserved
         dataOutputStream.writeByte(0); // reserved
      }

      // Now write out the variable length data:

      if (_ReqPgmName != null)
        _ReqPgmName.output(dataOutputStream);

      if (_ReqPgmParms != null)
        _ReqPgmParms.output(dataOutputStream);

      if (_ReqCmdName != null)
        _ReqCmdName.output(dataOutputStream);

      if (_ReqCmdParms != null)
        _ReqCmdParms.output(dataOutputStream);

      if (_ReqChildNames != null)
        _ReqChildNames.output(dataOutputStream);

      if (_ReqJobName != null)
        _ReqJobName.output(dataOutputStream);

      if (_ReqHostAddress != null)
        _ReqHostAddress.output(dataOutputStream);

      if (_ReqPMDBInfo != null)
        _ReqPMDBInfo.output(dataOutputStream);
   }

   public void write(PrintWriter printWriter)
   {
     super.write(printWriter);

     indent(printWriter);

     try
     {
       printWriter.print("Debuggee Name: " + reqPgmName());
       printWriter.println("    Args: " + reqPgmParms());
     }
     catch(IOException excp)
     {
     }
   }

   private int _ReqPgmNameOffset;
   private EStdString _ReqPgmName;

   private int _ReqPgmParmsOffset;
   private EStdString _ReqPgmParms;

   private int _ReqCmdNameOffset;
   private EStdString _ReqCmdName;

   private int _ReqCmdParmsOffset;
   private EStdString _ReqCmdParms;

   private int _ReqChildNamesOffset;
   private EStdString _ReqChildNames;

   private int _ReqJobNameOffset;
   private EStdString _ReqJobName;

   private int _ReqHostAddressOffset;
   private EStdString _ReqHostAddress;

   private int _ReqPMDBInfoOffset;
   private EPMDBInfo _ReqPMDBInfo;

   private int _programType;

   private int _platformInfoOffset;

   private byte _languageSetting;

   private static final int _fixed_length = 40;
}
