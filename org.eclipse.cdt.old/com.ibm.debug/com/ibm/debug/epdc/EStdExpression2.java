package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EStdExpression2.java, java-epdc, eclipse-dev, 20011128
// Version 1.13.1.2 (last modified 11/28/01 16:24:38)
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
 * This corresponds to the EPDC EStdExpression2 structure.
 */
public class EStdExpression2 extends EPDC_Base {

   /**
    * Read in expression information from a buffer
    * @exeception IOException if an I/O error occurs
    */
   EStdExpression2(byte[] inBuffer, int offset) throws IOException {
      super(inBuffer, offset);
      _offsetContext = readOffset();
      _context = new EStdView(inBuffer, _offsetContext);

      _offsetExprString = readOffset();
      _exprDU = readInt();
      _entryID = readInt();
      markOffset();
   }

   EStdExpression2(byte[] packetBuffer, DataInputStream dataInputStream)
   throws IOException
   {
     int offset;

     if ((offset = dataInputStream.readInt()) != 0)
        _context = new EStdView
                           (
                             packetBuffer,
                             new OffsetDataInputStream (packetBuffer, offset)
                           );

     if ((offset = dataInputStream.readInt()) != 0)
        _exprString = new EStdString
                           (
                             packetBuffer,
                             new OffsetDataInputStream (packetBuffer, offset)
                           );

     _exprDU = dataInputStream.readInt();
     _entryID = dataInputStream.readInt();
   }

   public EStdExpression2(EStdView context, String expr,
                          int exprDU, int entryID)
   {
     if (context == null)
        _context = new EStdView();
     else
        _context = context;

     if (expr != null)
        _exprString = new EStdString(expr);

     _exprDU = exprDU;
     _entryID = entryID;
   }

   EStdExpression2()
   {
   }

   /**
    * Get the expression context
    */
   public EStdView getContext() {
      return _context;
   }

   /**
    * Get the expression string
    * @exception IOExcption if an I/O error occurs
    */
   public String getExprString() throws IOException {
      if (_exprString == null)
         if (_offsetExprString != 0)
         {
            posBuffer(_offsetExprString);
            _exprString = readStdString();
         }
         else
           return null;

      return _exprString.string();
   }

   /**
    * Return the expr string, no lazy read.
    */

   public String getExpressionString()
   {
      if (_exprString == null)
         return null;

      return _exprString.string();
   }

   /**
    * Get the dispatchable unit for the expression
    */
   public int getExprDU() {
      return _exprDU;
   }

   /**
    * Get the entry ID for the expression
    */
   public int getEntryID() {
      return _entryID;
   }

   /**
    * Set the entry id of an expression
    */
   public void setEntryID(int id)
   {
     _entryID = id;
   }

   protected int fixedLen() {
      return _fixed_length;
   }

   protected int varLen() {
      return totalBytes(_context) + totalBytes(_exprString);
   }


/*
   protected int toDataStreams(DataOutputStream fixedData,
         DataOutputStream varData, int baseOffset)
         throws IOException, BadEPDCCommandException {

      return 0;
   }
*/

   void output(DataOutputStream dataOutputStream, int baseOffset)
   throws IOException
   {
     int offset = _fixed_length + baseOffset;

     offset += writeOffsetOrZero(dataOutputStream, offset, _context);
               writeOffsetOrZero(dataOutputStream, offset, _exprString);

     dataOutputStream.writeInt(_exprDU);
     dataOutputStream.writeInt(_entryID);

     if (_context != null)
         _context.output(dataOutputStream);

     if (_exprString != null)
         _exprString.output(dataOutputStream);
   }

   // data fields
   private transient int _offsetContext;
   private EStdView _context = null;

   private transient int _offsetExprString;
   private EStdString _exprString = null;
   private int _exprDU;
   private int _entryID;

   private static final int _fixed_length = 16;
}
