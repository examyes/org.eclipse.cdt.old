package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/ERepGetViews.java, java-epdc, eclipse-dev, 20011128
// Version 1.11.1.2 (last modified 11/28/01 16:23:36)
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
  * View information that is sent back with ERepInitializeDE
  */
public class ERepGetViews extends EPDC_Base {

   public ERepGetViews(short viewType, short viewClass, String viewName,
         byte viewAttr) {
      super();
      _viewType = viewType;
      _viewClass = viewClass;
      _viewName = new EStdString(viewName);
      _viewAttr = viewAttr;
   }

   public ERepGetViews(byte[] packetBuffer, DataInputStream dataInputStream)
   throws IOException
   {
      super();
      _viewType = dataInputStream.readShort();
      _viewClass = dataInputStream.readShort();

      int offset;

      if ((offset = dataInputStream.readInt()) != 0) // Offset to view name
         _viewName = new EStdString(packetBuffer,
                                    new OffsetDataInputStream(packetBuffer, offset)
                                   );

      _viewAttr = dataInputStream.readByte();
      dataInputStream.readByte(); // reserved byte
   }

   public String name()
   {
     if (_viewName != null)
       return _viewName.string();
     else
       return null;
   }

   public boolean hasPrefixArea()
   {
     return _viewType == EPDC.Viewtype_prefix;
   }

   public short kind()
   {
     return _viewClass;
   }

   public String viewClass() {
      switch ( kind() ) {
         case EPDC.View_Class_Unk:
            return "View_Class_Unk";
         case EPDC.View_Class_Struc:  //not used
            return "View_Class_Struc";
         case EPDC.View_Class_Source:
            return "View_Class_Source";
         case EPDC.View_Class_Mixed:
            return "View_Class_Mixed";
         case EPDC.View_Class_Disasm:
            return "View_Class_Disasm";
         case EPDC.View_Class_Listing:
            return "View_Class_Listing";
         case EPDC.View_Class_Annotated: //not used
            return "View_Class_Annotated";
         default:
            return "Unknown";
      }
   }

   public String viewType() {
      switch ( _viewType ) {
         case EPDC.Viewtype_simp:
            return "Viewtype_simp";
         case EPDC.Viewtype_prefix:  //not used
            return "Viewtype_prefix";
         case EPDC.Viewtype_dynam:
            return "Viewtype_dynam";
         default:
            return "Unknown";
      }
   }

   public String viewAttr() {
      if (isMonitorCapable() && isLineBreakpointCapable())
         return "MONITOR_CAPABLE and LINEBP_CAPABLE";
      else if (isMonitorCapable())
         return "MONITOR_CAPABLE";
      else if (isLineBreakpointCapable())
         return "LINEBP_CAPABLE";
      else
         return "NONE";
   }

   public boolean isMonitorCapable()
   {
     return (_viewAttr & EPDC.MONITOR_CAPABLE) != 0;
   }

   public boolean isLineBreakpointCapable()
   {
     return (_viewAttr & EPDC.LINEBP_CAPABLE) != 0;
   }

   /** Output class to data streams according to EPDC protocol
     * @exception IOException if an I/O error occurs
    *  @exception BadEPDCCommandException if the EPDC command
    *    is structured incorrectly
    */
   protected int toDataStreams(DataOutputStream fixedData,
         DataOutputStream varData, int baseOffset)
         throws IOException, BadEPDCCommandException {
      writeShort(fixedData, _viewType);
      writeShort(fixedData, _viewClass);

      writeOffsetOrZero(fixedData, baseOffset, _viewName);

      if (_viewName != null)
         _viewName.output(varData);

      writeChar(fixedData, _viewAttr);
      writeChar(fixedData, (byte) 0);              // reserved

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
      return super.varLen() + totalBytes(_viewName);
   }

   public void write(PrintWriter printWriter) {
      indent(printWriter);
      printWriter.println("Viewtype: " + viewType() );
      indent(printWriter);
      printWriter.println("Viewclass: " + viewClass() );
      indent(printWriter);
      printWriter.println("ViewName: " + name() );
      indent(printWriter);
      printWriter.println("ViewAttr: " + viewAttr() );
   }

   private short _viewType;
   private short _viewClass;
   private EStdString _viewName;
   private byte _viewAttr;

   private static final int _fixed_length = 10;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}