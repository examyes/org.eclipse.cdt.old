package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/ERepProcessAttach.java, java-epdc, eclipse-dev, 20011128
// Version 1.7.1.2 (last modified 11/28/01 16:24:51)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;

public class ERepProcessAttach extends EPDC_Reply {

   public ERepProcessAttach(EPDC_EngineSession session, EStdTime timestamp,
      EStdDate datestamp, String QualifiedName, int ProcessId, int DU,
      int whyStop, String exceptionMsg, String profileName) {

      super();
      setReplyCode(EPDC.Remote_ProcessAttach);
      _session = session;
      _timestamp = timestamp;
      _datestamp = datestamp;
      _QualifiedName = new EStdString(QualifiedName);
      _ProcessId = ProcessId;
      _DU = DU;
      _WhyStop = (short) whyStop;
      _ExceptionMsg = new EStdString(exceptionMsg);
      _profileName = new EStdString(profileName);
   }

   ERepProcessAttach(byte[] packetBuffer, DataInputStream dataInputStream)
   throws IOException
   {
      super( packetBuffer, dataInputStream );

      _timestamp = new EStdTime(packetBuffer, dataInputStream );
      _datestamp = new EStdDate(packetBuffer, dataInputStream );

      int offset = 0;

      if ((offset = dataInputStream.readInt()) != 0)
         _QualifiedName = new EStdString
                             (
                               packetBuffer,
                               new OffsetDataInputStream (packetBuffer, offset)
                             );

      _ProcessId = dataInputStream.readInt();
      _DU = dataInputStream.readInt();
      _WhyStop = dataInputStream.readShort();

      if ((offset = dataInputStream.readInt()) != 0)
         _ExceptionMsg = new EStdString
                             (
                               packetBuffer,
                               new OffsetDataInputStream (packetBuffer, offset)
                             );

      if ((offset = dataInputStream.readInt()) != 0)
         _profileName = new EStdString
                             (
                               packetBuffer,
                               new OffsetDataInputStream (packetBuffer, offset)
                             );
   }

   public String QualifiedName()
   {
     if (_QualifiedName != null)
       return _QualifiedName.string();
     else
       return null;
   }

   public int ProcessId()
   {
      return _ProcessId;
   }

   public String getProfileName()
   {
     if (_profileName != null)
       return _profileName.string();
     else
       return null;
   }

   public short getWhyStop()
   {
     return _WhyStop;
   }

   public int getThreadID()
   {
     return _DU;
   }

   public String getExceptionMsg()
   {
     if (_ExceptionMsg == null)
        return null;
     else
        return _ExceptionMsg.string();
   }

   /** Output class to data streams according to EPDC protocol.
    *  @exception IOException if an I/O error occurs
    *  @exception BadEPDCCommandException if the EPDC command
    *    is structured incorrectly
    */
   protected int toDataStreams(DataOutputStream fixedData,
         DataOutputStream varData, int baseOffset)
         throws IOException, BadEPDCCommandException {
      int offset = baseOffset;

      super.toDataStreams(fixedData, varData, offset);
      offset += super.varLen();

      _timestamp.toDataStreams(fixedData, varData, offset);
      _datestamp.toDataStreams(fixedData, varData, offset);

      offset += writeOffsetOrZero(fixedData, offset, _QualifiedName);

      if (_QualifiedName != null)
         _QualifiedName.output(varData);

      writeInt(fixedData, _ProcessId);
      writeInt(fixedData, _DU);
      writeShort(fixedData, _WhyStop);

      offset += writeOffsetOrZero(fixedData, offset, _ExceptionMsg);

      if (_ExceptionMsg != null)
         _ExceptionMsg.output(varData);

      offset += writeOffsetOrZero(fixedData, offset, _profileName);

      if (_profileName != null)
         _profileName.output(varData);

      return fixedLen() + varLen();
   }

   public int fixedLen() {
      return super.fixedLen() + _fixed_length + _timestamp.fixedLen() + _datestamp.fixedLen();
   }

   public int varLen() {
      return super.varLen() +
             totalBytes(_QualifiedName) +
             totalBytes(_ExceptionMsg) +
             totalBytes(_profileName);
   }

   private EPDC_EngineSession _session;
   private EStdTime  _timestamp;
   private EStdDate  _datestamp;
   private EStdString _QualifiedName;
   private int _QualifiedNameOffset;
   private int _ProcessId;
   private int _DU;
   private short _WhyStop;
   private EStdString _ExceptionMsg;
   private EStdString _profileName;

   private static int _fixed_length = 22;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";
}
