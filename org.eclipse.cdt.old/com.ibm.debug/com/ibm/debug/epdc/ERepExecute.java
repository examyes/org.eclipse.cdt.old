package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/ERepExecute.java, java-epdc, eclipse-dev, 20011128
// Version 1.10.1.2 (last modified 11/28/01 16:23:20)
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
public class ERepExecute extends EPDC_Reply {

   /**
    * Create execute reply packet.
    * @param DU the dispatchable unit (thread) that stopped
    * @param Whystop the reason for stopping
    * @see EPDC
    */
   public ERepExecute(int DU, int Whystop) {
      super(EPDC.Remote_Execute);
      _DU = DU;
      _whyStop = (short)Whystop;
      _exceptionMsg = null;
      _BreakidList = new Vector();
   }

   ERepExecute( byte[] packetBuffer, DataInputStream dataInputStream)
   throws IOException
   {
      super( packetBuffer, dataInputStream );

      _DU = dataInputStream.readInt();
      _whyStop = dataInputStream.readShort();

      int offset;

      if ((offset = dataInputStream.readInt()) != 0)
         _exceptionMsg = new EStdString
                             (
                               packetBuffer,
                               new OffsetDataInputStream (packetBuffer, offset)
                             );

      int numberOfBreakpointIDs = dataInputStream.readInt();

      if (numberOfBreakpointIDs > 0)
      {
         _BreakidList = new Vector(numberOfBreakpointIDs);

         for (int i = 0; i < numberOfBreakpointIDs; i++)
             _BreakidList.addElement(new EBPList(packetBuffer, dataInputStream));
      }

   }

   /**
    * Set the exception message
    */
   public void setExceptionMsg(String exceptionMsg) {
      _exceptionMsg = new EStdString(exceptionMsg);
   }

   /**
    * Add a breakpopint to the breakpoint list
    */
   public void addBreakpoint(int BPid, byte BPKind) {
      _BreakidList.addElement(new EBPList(BPid, BPKind));
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

      writeInt(fixedData, _BreakidList.size() );   // write number of breakpoints

      for (int i=0; i<_BreakidList.size(); i++)
         ((EBPList) _BreakidList.elementAt(i)).toDataStreams(fixedData, null, 0);

      return fixedLen() + varLen();
   }

   protected int fixedLen() {
      return super.fixedLen() + _fixed_length + EBPList.statfixedLen() * _BreakidList.size();
   }

   protected int varLen() {
      return super.varLen() + totalBytes(_exceptionMsg);
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

   public Vector getBreakidList()
   {
     return _BreakidList;
   }

   public void write(PrintWriter printWriter)
   {
     super.write(printWriter);

     if (getDetailLevel() >= DETAIL_LEVEL_MEDIUM)
     {
        String why;

        switch(_whyStop)
        {
           case EPDC.Why_none:
                why = "Why_none";
                break;

           case EPDC.Why_break:
                why = "Why_break";
                break;

           case EPDC.Why_Watchpoint:
                why = "Why_Watchpoint";
                break;

           case EPDC.Why_done:
                why = "Why_done";
                break;

           case EPDC.Why_ChildDone:
                why = "Why_ChildDone";
                break;

           case EPDC.Why_PgmExcept:
                why = "Why_PgmExcept";
                break;

           case EPDC.Why_PgmExcept_Nohandler:
                why = "Why_PgmExcept_Nohandler";
                break;

           case EPDC.Why_PgmExcept_NoRetry:
                why = "Why_PgmExcept_NoRetry";
                break;

           case EPDC.Why_PgmExcept_OnlyRun:
                why = "Why_PgmExcept_OnlyRun";
                break;

           case EPDC.Why_StorageUsageCheck:
                why = "Why_StorageUsageCheck";
                break;

           case EPDC.Why_ResourceInterlock:
                why = "Why_ResourceInterlock";
                break;

           case EPDC.Why_DoneNoStop:
                why = "Why_DoneNoStop";
                break;

           case EPDC.Why_ProcessChanged:
                why = "Why_ProcessChanged";
                break;

           case EPDC.Why_ProcessDone:
                why = "Why_ProcessDone";
                break;

           case EPDC.Why_DoneCloseDebugger:
                why = "Why_DoneCloseDebugger";
                break;

           case EPDC.Why_PgmForked:
                why = "Why_PgmForked";
                break;

           case EPDC.Why_PgmExeced:
                why = "Why_PgmExeced";
                break;

           case EPDC.Why_RemoteCall:
                why = "Why_RemoteCall";
                break;

           case EPDC.Why_Other:
                why = "Why_Other";
                break;

           case EPDC.Why_msg:
                why = "Why_msg";
                break;

           default:
                why = "UNKNOWN REASON CODE: " + _whyStop;
                break;
        }

        indent(printWriter);
        printWriter.println("Why Stopped: " + why + "   Thread : " + _DU);
     }

     if (_exceptionMsg != null && _exceptionMsg.string() != null)
     {
        indent(printWriter);
        printWriter.println("Exception Message: " + _exceptionMsg.string());
     }
   }

   // Data fields
   private int _DU;                    // dispatchable unit for execution
   private short _whyStop;               // why execution stopped
   private EStdString _exceptionMsg;       // exception message, null if none
   private Vector _BreakidList;        // breakpoint id list, vector of EBPList's

   private static final int _fixed_length = 14;
         // includes number of breapkoints in list

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}
