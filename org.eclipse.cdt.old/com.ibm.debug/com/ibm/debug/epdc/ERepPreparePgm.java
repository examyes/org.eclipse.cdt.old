package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/ERepPreparePgm.java, java-epdc, eclipse-dev, 20011128
// Version 1.14.1.2 (last modified 11/28/01 16:23:45)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;

public class ERepPreparePgm extends EPDC_Reply {
  public ERepPreparePgm(EPDC_EngineSession session, EStdTime timestamp, EStdDate datestamp,
         String MachineName, String QualifiedName, int ProcessId, String profileName) {
    super();
      setReplyCode(EPDC.Remote_PreparePgm);
      _session = session;
      _timestamp = timestamp;
      _datestamp = datestamp;
      _MachineName = new EStdString(MachineName);
      _QualifiedName = new EStdString(QualifiedName);
      _ProcessId = ProcessId;
      _profileName = new EStdString(profileName);
  }

   ERepPreparePgm( byte[] packetBuffer, DataInputStream dataInputStream)
   throws IOException
   {
      super( packetBuffer, dataInputStream );

      _timestamp = new EStdTime(packetBuffer, dataInputStream );
      _datestamp = new EStdDate(packetBuffer, dataInputStream );

      dataInputStream.readInt(); // Offset to MachineName string

      if ((_QualifiedNameOffset = dataInputStream.readInt()) != 0)
         _QualifiedName = new EStdString
                             (
                               packetBuffer,
                               new OffsetDataInputStream (packetBuffer, _QualifiedNameOffset)
                             );

      _ProcessId = dataInputStream.readInt();

      int offset = dataInputStream.readInt(); // Offset to ProfileName string

      if (offset != 0)
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

   public String getProfileName()
   {
     if (_profileName != null)
       return _profileName.string();
     else
       return null;
   }

   public int ProcessId()
   {
      return _ProcessId;
   }

   /** Output class to data streams according to EPDC protocol.
     * @exception IOException if an I/O error occurs
    *  @exception BadEPDCCommandException if the EPDC command
    *    is structured incorrectly
    */
   protected int toDataStreams(DataOutputStream fixedData,
         DataOutputStream varData, int baseOffset)
         throws IOException, BadEPDCCommandException
   {
      int offset = baseOffset;

      super.toDataStreams(fixedData, varData, offset);
      offset += super.varLen();

      _timestamp.toDataStreams(fixedData, varData, offset);
      _datestamp.toDataStreams(fixedData, varData, offset);

      offset += writeOffsetOrZero(fixedData, offset, _MachineName);
      offset += writeOffsetOrZero(fixedData, offset, _QualifiedName);

      if (_MachineName != null)
         _MachineName.output(varData);

      if (_QualifiedName != null)
         _QualifiedName.output(varData);

      writeInt(fixedData, _ProcessId);

      offset += writeOffsetOrZero(fixedData, offset, _profileName);

      if (_profileName != null)
         _profileName.output(varData);

      return fixedLen() + varLen();
   }

   public int fixedLen() {
      return super.fixedLen() + _fixed_length + _timestamp.fixedLen() + _datestamp.fixedLen();
   }

   public int varLen() {
      int total = 0;
      total += super.varLen();
      total += totalBytes(_MachineName);
      total += totalBytes(_QualifiedName);
      total += totalBytes(_profileName);
      return total;
   }

   private EPDC_EngineSession _session;
   private EStdTime  _timestamp;
   private EStdDate  _datestamp;
   private EStdString _MachineName;
   private EStdString _QualifiedName;
   private int _QualifiedNameOffset;

   private int _ProcessId;
   private EStdString _profileName;

   private static int _fixed_length = 16;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}

