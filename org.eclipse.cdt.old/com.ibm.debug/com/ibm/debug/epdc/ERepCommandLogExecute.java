package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/ERepCommandLogExecute.java, java-epdc, eclipse-dev, 20011128
// Version 1.2.1.2 (last modified 11/28/01 16:26:17)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;
import java.util.*;

/**
 * Execute Program reply
 */
public class ERepCommandLogExecute extends EPDC_Reply {

   /**
    * Create execute reply packet.
    * @param DU the dispatchable unit (thread) that stopped
    * @param Whystop the reason for stopping
    * @see EPDC
    */
   public ERepCommandLogExecute(int DU, int Whystop) {
      super(EPDC.Remote_CommandLogExecute);
      _DU = DU;
      _whyStop = (short)Whystop;
      _exceptionMsg = null;
      _responseLines = new Vector();
   }

   ERepCommandLogExecute( byte[] packetBuffer, DataInputStream dataInputStream)
   throws IOException
   {
      super( packetBuffer, dataInputStream );

      _DU = dataInputStream.readInt();
      _whyStop = dataInputStream.readShort();
      int offset;
      if ((offset = dataInputStream.readInt()) != 0)
         _exceptionMsg = new EStdString(packetBuffer,
                               new OffsetDataInputStream (packetBuffer, offset)  );

      if(_exceptionMsg==null) _exceptionMsg=new EStdString("null");

      int numberOfResponseLines = dataInputStream.readInt();
      int offsetOfResponseLines = dataInputStream.readInt();

      if (numberOfResponseLines > 0)
      {
         _responseLines = new Vector(numberOfResponseLines);

         OffsetDataInputStream ds = new OffsetDataInputStream(packetBuffer, offsetOfResponseLines );
         for (int i = 0; i < numberOfResponseLines; i++)
         {
             EStdString _txt = new EStdString(ds);
             _responseLines.addElement(_txt);
         }
      }
   }

   /**
    * Set the exception message
    */
   public void setExceptionMsg(String exceptionMsg) {
      _exceptionMsg = new EStdString(exceptionMsg);
   }

   /**
    * Add a ResponseLine to the ResponseLine list
    */
   public void addResponseLine(String textLine) {
      _responseLines.addElement(new EStdString(textLine));
   }

   /**
    * Output class to data streams according to EPDC protocol.
    * @exception IOException if an I/O error occurs
    * @exception BadEPDCCommandException if the EPDC command
    *    is structured incorrectly
    */
   protected int toDataStreams(DataOutputStream fixedData,
         DataOutputStream varData, int baseOffset)
         throws IOException, BadEPDCCommandException {

      int offset = baseOffset;

      super.toDataStreams(fixedData, varData, baseOffset);
      offset += super.varLen();

      writeInt(fixedData, _DU);
      writeShort(fixedData, _whyStop);

      offset += writeOffsetOrZero(fixedData, offset, _exceptionMsg);

      if (_exceptionMsg != null)
         _exceptionMsg.output(varData);

      writeInt(fixedData, _responseLines.size() );   // write number of responseLines
      writeInt(fixedData, offset);

      for (int i=0; i<_responseLines.size(); i++)
      {
           ((EStdString) _responseLines.elementAt(i)).output(varData);
      }

      return fixedLen() + varLen();
   }

   public int fixedLen() {
      int size = super.fixedLen() + _fixed_length;
      return size;
   }

   public int varLen() {
      int linesBytes = 0;
      if(_responseLines!=null)
         for(int i=0; i<_responseLines.size(); i++)
             if(_responseLines.elementAt(i)!=null)
                 linesBytes += totalBytes((EStdString)_responseLines.elementAt(i));
      return super.varLen() +linesBytes +totalBytes(_exceptionMsg);
   }

   public short getWhyStop()
   {
     return _whyStop;
   }

   public int getThreadID()
   {
     return _DU;
   }

   public String getExceptionMsg()
   {
     if (_exceptionMsg == null)
       return null;

     return _exceptionMsg.string();
   }

   public String[] getResponseLines()
   {
     String[] responseLines = null;
     if( _responseLines!=null && _responseLines.size()>0 )
     {
        responseLines = new String[_responseLines.size()];
        for(int i=0; i<_responseLines.size(); i++)
           responseLines[i] = ((EStdString)_responseLines.elementAt(i)).string();
     }
     return responseLines;
   }

   // Data fields
   private int _DU;                    // dispatchable unit for execution
   private short _whyStop;             // why execution stopped
   private EStdString _exceptionMsg;   // exception message, null if none
   private Vector _responseLines;      // the GDB response lines

   private static final int _fixed_length = 18;  // DU-4 +whyStop-2 +offsetExceptionMessage-4 +lineCount-4 +offsetLineMessages-4
         // includes number of responseLines in list
}
