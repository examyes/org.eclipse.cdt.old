package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EPDC_ChangeInfo.java, java-epdc, eclipse-dev
// Version 1.31.1.1 (last modified 11/21/01 09:24:11)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.connection.*;
import java.io.*;
import java.util.*;
//
//
// EPDC_ChangeInfo
//
//
/** EPDC Change Info Header */
public class EPDC_ChangeInfo extends EPDC_Base
{
  /**
  * This represents the changed info packets that follow reply packets
  *
  */

  EPDC_ChangeInfo( byte[] packetBuffer, DataInputStream dataInputStream)
  throws IOException
  {
    super();
    _compression = dataInputStream.readByte();
    dataInputStream.skipBytes( 3 );  // reserved bytes
    _change_type = dataInputStream.readInt();
    _total_bytes = dataInputStream.readInt();
    _num_changed_items = dataInputStream.readInt();
    _num_chgd_in_packet = dataInputStream.readInt();
    _changed_items = new Vector();
  }

   /**
    * Create a new change info packet.
    * @param change_type type of changed information
    */
   EPDC_ChangeInfo(int change_type) {
      _compression = EPDC.NO_COMPRESSION;
      _change_type = change_type;
      _total_bytes = 0;
      _num_changed_items = _num_chgd_in_packet = 0;
      _changed_items = new Vector();
   }

   /**
    * returns a request object from a buffer based on the request type
    *
    */
/*
   static public EPDC_ChangeInfo decodeChangeInfoBuffer( byte[] inBuffer ) throws IOException {

      EPDC_ChangeInfo r = new EPDC_ChangeInfo( inBuffer );

      switch (r.changeCode()) {
         //case r.PARTS_TABLE_INFO:
            // return new EPDC_ReqInitializeDE( inBuffer );
         default:
            return new EPDC_ChangeInfo( inBuffer );
      }
   }
*/

   int changeItemsInThisPacket()
   {
     return _num_chgd_in_packet;
   }

   int totalChangeItemsOfThisType()
   {
     return _num_changed_items;
   }

   public String getName ()
   {
      switch ( changeCode() )
      {
         case EPDC.UNDEFINED_INFO:
              return "UNDEFINED";

         case EPDC.PARTS_TABLE_INFO:
              return "PARTS_TABLE";

         case EPDC.BREAKPOINT_TABLE_INFO:
              return "BREAKPOINT_TABLE";

         case EPDC.PROGRAM_STATE_INFO:
              return "PROGRAM_STATE";

         case EPDC.MONITORED_VARIABLE_INFO:
              return "MONITORED_VARIABLE";

         case EPDC.PROGRAM_COMMAND_INFO:
              return "PROGRAM_COMMAND";

         case EPDC.PROGRAM_OUTPUT_INFO:
              return "PROGRAM_OUTPUT";

         case EPDC.PROGRAM_INPUT_INFO:
              return "PROGRAM_INPUT";

         case EPDC.THREAD_STATE_INFO:
              return "THREAD_STATE";

         case EPDC.STORAGE_INFO:
              return "STORAGE";

         case EPDC.STACK_INFO:
              return "STACK";

         case EPDC.PM_QUEUE_INFO:
              return "PM_QUEUE";

         case EPDC.MODULE_ENTRY_INFO:
              return "MODULE_ENTRY";

         case EPDC.LOG_CHANGED_INFO:
              return "LOG_CHANGED";

         case EPDC.REGISTERS_INFO:
              return "REGISTERS";

         case EPDC.PASSTHRU_INFO:
              return "PASSTHRU";

         case EPDC.PROCESS_INFO:
              return "PROCESS";

         case EPDC.FCT_INFO:
              return "FCT";

         default:
              return "CHANGE_INFO";
      }
   }

   /**
    * Reads a change packet from 'connection' and puts it into 'bos'.
    * @return The change code.
    */

   private static int getPacket(Connection connection,
                                ByteArrayOutputStream bos)
   throws IOException
   {
      DataOutputStream dos = new DataOutputStream(bos);

      connection.beginPacketRead();
      DataInputStream inStream = new DataInputStream(connection.getInputStream());

	  inStream.readInt();

      dos.write(inStream.read());  // compression
      for (int i=0; i<3; i++)
        dos.write(inStream.read()); // reserved

      int changeCode = inStream.readInt();
      dos.writeInt(changeCode);
      int totBytes = inStream.readInt();
      dos.writeInt(totBytes);

      byte[] b = new byte[totBytes - 12];

      inStream.readFully(b);
      dos.write(b);
      connection.endPacketRead(EPDC.ChangePacket);

      return changeCode;
   }

   /**
    * returns a request object from a buffer based on the request type
    *
    */
   static public EPDC_ChangeInfo decodeChangeInfoStream(Connection connection,
                                                 EPDC_Reply reply,
                                                 EPDC_EngineSession engineSession)
   throws IOException
   {

      ByteArrayOutputStream bos = new ByteArrayOutputStream();

      int changeCode = getPacket(connection, bos);

      EPDC_ChangeInfo result = null;

      // Get a byte array which contains the entire packet:

      byte[] packetByteArray = bos.toByteArray();

      DataInputStream dataInputStream = new DataInputStream
                                            (
                                              new ByteArrayInputStream
                                                  (
                                                    packetByteArray
                                                  )
                                            );

      switch (changeCode)
      {
         case EPDC.FCT_INFO:
              result = new FCTChangeInfo( packetByteArray, dataInputStream);
              reply.add((FCTChangeInfo)result);
              break;

         case EPDC.MODULE_ENTRY_INFO:
              result = new ModuleChangeInfo( packetByteArray, dataInputStream);
              reply.add((ModuleChangeInfo)result);
              break;

         case EPDC.PROGRAM_STATE_INFO:
              result = new ThreadChangeInfo( packetByteArray, dataInputStream, engineSession);
              reply.add((ThreadChangeInfo)result);
              break;

         case EPDC.PARTS_TABLE_INFO:
              result = new PartChangeInfo( packetByteArray, dataInputStream, engineSession);
              reply.add((PartChangeInfo)result);
              break;

         case EPDC.BREAKPOINT_TABLE_INFO:
              result = new BreakpointChangeInfo( packetByteArray, dataInputStream, engineSession);
              reply.add((BreakpointChangeInfo)result);
              break;

         case EPDC.MONITORED_VARIABLE_INFO:
              result = new MonitorChangeInfo(packetByteArray, dataInputStream);
              reply.add((MonitorChangeInfo)result);
              break;

         case EPDC.STACK_INFO:
              result = new StackChangeInfo(packetByteArray, dataInputStream, engineSession);
              reply.add((StackChangeInfo)result);
              break;

         case EPDC.REGISTERS_INFO:
              result = new RegistersChangeInfo(packetByteArray, dataInputStream);
              reply.add((RegistersChangeInfo)result);
              break;

         case EPDC.STORAGE_INFO:
              result = new StorageChangeInfo(packetByteArray, dataInputStream);
              reply.add((StorageChangeInfo)result);
              break;

         default:
              result = new EPDC_ChangeInfo( packetByteArray, dataInputStream );
              break;
      }

      return result;
   }

   /**
    * Add a changed item to this change info packet
    */

   void addChangedItem(EPDC_ChangeItem ci)
   {
     addChangedItem(ci, true);
   }

   void addChangedItem(EPDC_ChangeItem ci, boolean incrementCount)
   {
      if (_changed_items == null)
         _changed_items = new Vector();

      _changed_items.addElement(ci);

      if (incrementCount)
      {
        _num_changed_items++;
        _num_chgd_in_packet++;
      }
   }

   public Vector changedItems()
   {
      return _changed_items;
   }

   /**
    * Output changed information packet to an EPDC connection
    * @param connection the EPDC connection
    * @exception IOException if an I/O error occurs
    */
   public int output(Connection connection) throws IOException, BadEPDCCommandException {
      _total_bytes = fixedLen() + varLen();

      connection.beginPacketWrite(_total_bytes + 4);
      OutputStream outStream = connection.getOutputStreamBuffer();

      new DataOutputStream(connection.getOutputStreamBuffer()).writeInt(703);

      ByteArrayOutputStream fixedByteStream = new ByteArrayOutputStream();
      ByteArrayOutputStream varByteStream = new ByteArrayOutputStream();

      DataOutputStream fixedDataStream = new DataOutputStream(fixedByteStream);
      DataOutputStream varDataStream = new DataOutputStream(varByteStream);

      int tot = toDataStreams(fixedDataStream, null, 0);
      outStream.write(fixedByteStream.toByteArray());

      /* Write out changed information structures */

      int offset = fixedLen();
      fixedByteStream.reset();   // clear byte arrays
      varByteStream.reset();
      for (int i=0; i<_changed_items.size(); i++) {
         EPDC_ChangeItem ci = (EPDC_ChangeItem) _changed_items.elementAt(i);
         tot += ci.toDataStreams(fixedDataStream, varDataStream, offset);
         offset += ci.varLen();
      }
      outStream.write(fixedByteStream.toByteArray());
      outStream.write(varByteStream.toByteArray());

      connection.endPacketWrite(EPDC.ChangePacket);

      return tot;
   }

   /** Outputs
    *  the class into two byte streams for fixed and variable data,
    *  corresponding to the EPDC protocol.
    *
    *  @param fixedData output stream for the fixed data
    *  @param varData output stream for the variable data
    *  @param baseOffset the base offset to add to all offsets
    *
    *  @return total size of written data
    *  @exception IOException if an I/O error occurs
    *  @exception BadEPDCCommandException if the EPDC command
    *    is structured incorrectly
    */

   protected int toDataStreams(DataOutputStream fixedData,
         DataOutputStream varData, int baseOffset)
         throws IOException, BadEPDCCommandException {
      writeChar(fixedData, _compression);
      for (int i=0; i<3; i++)
         writeChar(fixedData, (byte) 0);  // reserved
      writeInt(fixedData, _change_type);
      writeInt(fixedData, _total_bytes);
      writeInt(fixedData, _num_changed_items);
      writeInt(fixedData, _num_chgd_in_packet);

      /* Now, we need to make the offset array if there should be one */
      if (_changed_items.size() > 1) {
         int itemoffset = _fixed_length + _changed_items.size() * 4;
         for (int i=0; i<_changed_items.size(); i++) {
            writeOffset(fixedData, itemoffset);
            itemoffset += ((EPDC_ChangeItem) _changed_items.elementAt(i)).fixedLen();
         }
      }

      return _fixed_length;
   }


   /** Return the length of the fixed component */
   protected int fixedLen() {
      int total = _fixed_length;

      if (_changed_items.size() > 1)
         total += _changed_items.size() * 4;   // space taken up by offsets to changed items

      for (int i=0; i<_changed_items.size(); i++)
         total += ((EPDC_ChangeItem) _changed_items.elementAt(i)).fixedLen();

      return total;
   }

   /** Return the length of the variable component */
   protected int varLen() {
      int total = 0;

      for (int i=0; i<_changed_items.size(); i++)
         total += ((EPDC_ChangeItem) _changed_items.elementAt(i)).varLen();

      return total;
   }

  /**
  * returns the change type
  *
  */
  public int changeCode() { return _change_type; }

   /**
    * Write out a packet to 'printWriter' in hex representation.
    */

   public void write(PrintWriter printWriter)
   {
     EPDC_ChangeItem tempChangeItem;

     printWriter.println("--------CHANGED INFO PACKET--------");
     printWriter.println();
     printWriter.println("CHANGED INFO TYPE: " + getName() );
     increaseIndentLevel();

     if (getDetailLevel() >= DETAIL_LEVEL_MEDIUM)
     {
        indent(printWriter);
        printWriter.println("Total bytes :        " + _total_bytes);
        indent(printWriter);
        printWriter.println("Changed items :      " + _num_changed_items);
        indent(printWriter);
        printWriter.println("Changed items here : " + _num_chgd_in_packet);

     	increaseIndentLevel();
     	for (int i = 0; i < _changed_items.size(); i++) {
     	   tempChangeItem = (EPDC_ChangeItem) _changed_items.elementAt(i);
     	   tempChangeItem.write(printWriter);
     	}
        decreaseIndentLevel();
     }
     decreaseIndentLevel();

   }


   public static void write(PrintWriter printWriter,
                            Connection connection,
                            boolean readOnly)
   {
     ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

     try
     {
        int changeCode = getPacket(connection, byteStream);

        if (readOnly)
           return;

        // Get a byte array which contains the entire packet:

        byte[] packet = byteStream.toByteArray();

        printWriter.print("Change Code : " + changeCode +
                          " (" + getHexDigits(changeCode) + ")");
        printWriter.println("    Length : " + packet.length +
                            " (" + getHexDigits(packet.length) + ")");

        EPDC_Base.write(printWriter, packet);
     }
     catch(IOException excp)
     {
        printWriter.println("Error: IOException occurred trying to read packet.");
     }
   }


  private byte _compression;
  private int _change_type;
  private int _total_bytes;
  private int _num_changed_items;
  private int _num_chgd_in_packet;
   Vector _changed_items;

   private static final int _fixed_length = 20;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}
