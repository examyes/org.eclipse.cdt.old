package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1995, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqExpression.java, java-epdc, eclipse-dev, 20011128
// Version 1.14.1.2 (last modified 11/28/01 16:24:07)
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
 * Exression request class
 */
public class EReqExpression extends EPDC_Request
{

   /**
    * Read in request packet from a buffer
    * @exeception IOException if an I/O error occurs
    */
   protected EReqExpression(byte[] inBuffer) throws IOException {
      super(inBuffer);
      _inBuffer = inBuffer;
      _monAttributes = readChar();
      _monRetValAttr = readChar();
      _monType = readShort();

      _offsetExpression = readOffset();

      _monitorModuleOffset = readOffset();
      _monitorPartOffset   = readOffset();
      _monitorFileOffset   = readOffset();

   }

   public EReqExpression(byte attributes, byte retValAttr, short type,
                         EStdExpression2 stdExpression, String moduleName,
                         String partName, String viewFileName,
                         String stmtNum)
   {
      super(EPDC.Remote_Expression);

      _monAttributes = attributes;
      _monRetValAttr = retValAttr;           //reserved
      _monType = type;
      _expression = stdExpression;

      if (moduleName != null)
         _moduleName = new EStdString(moduleName);

      if (partName != null)
         _partName = new EStdString(partName);

      if (viewFileName != null)
         _viewFileName = new EStdString(viewFileName);

      if (stmtNum != null)
          _stmtNum = new EStdString(stmtNum);
   }

   public void output(DataOutputStream dataOutputStream)
   throws IOException
   {
     super.output(dataOutputStream);

     dataOutputStream.writeByte(_monAttributes);
     dataOutputStream.writeByte(0);           //reserved
     dataOutputStream.writeShort(_monType);

     int offset = fixedLen() + super.varLen();

      // Write out the offsets of the variable length data
     int exprOffset = offset;

     offset += writeOffsetOrZero(dataOutputStream, offset, _expression);
     offset += writeOffsetOrZero(dataOutputStream, offset, _moduleName);
     offset += writeOffsetOrZero(dataOutputStream, offset, _partName);
     offset += writeOffsetOrZero(dataOutputStream, offset, _viewFileName);

     if (getEPDCVersion() > 305)
         writeOffsetOrZero(dataOutputStream, offset, _stmtNum);

     if (_expression != null)
         _expression.output(dataOutputStream, exprOffset);

     if (_moduleName != null)
         _moduleName.output(dataOutputStream);

     if (_partName != null)
         _partName.output(dataOutputStream);

     if (_viewFileName != null)
         _viewFileName.output(dataOutputStream);

     if (_stmtNum != null)
         _stmtNum.output(dataOutputStream);
   }

   /**
    * Return variable data length
    */
   protected int varLen()
   {
      int total = totalBytes(_expression) +
                  totalBytes(_moduleName) +
                  totalBytes(_partName) +
                  totalBytes(_viewFileName) +
                  super.varLen();

      if (_stmtNum != null)
          total += totalBytes(_stmtNum);

      return total;
   }

   /**
    * Return monitor attributes
    */
   public byte getMonAttributes() {
      return _monAttributes;
   }

   /**
    * Return whether monitor is deferred
    */
   public boolean isDeferred() {
      return (_monAttributes & EPDC.MonDefer) != 0;
   }

   /**
    * Return monitor type
    */
   public short getMonType() {
      return _monType;
   }

   /**
    * Return part name
    */
   public EStdString getPartName() throws IOException {
      if ((_partName == null) && (_monitorPartOffset != 0))
         _partName = new EStdString(_inBuffer, _monitorPartOffset);
      return _partName;
   }

   /**
    * Return module name
    */
   public EStdString getModuleName() throws IOException {
      if ((_moduleName == null) && (_monitorModuleOffset != 0))
         _moduleName = new EStdString(_inBuffer, _monitorModuleOffset);
      return _moduleName;
   }

   /**
    * Return expression
    * @exception IOException if an I/O error occurs
    */
   public EStdExpression2 getExpression() throws IOException {
      if ((_expression == null) && (_offsetExpression != 0))
         _expression = new EStdExpression2(_inBuffer, _offsetExpression);
      return _expression;
   }

   // Return the length of the fixed component
   protected int fixedLen()
   {
      // If the debug engine supports statement breakpoint, the fixed length
      // must include an offset to the statement number string
      if (getEPDCVersion() > 305)
          return _fixed_length + 4 + super.fixedLen();

      return _fixed_length + super.fixedLen();
   }

   // data fields
   private byte[] _inBuffer;

   private byte _monAttributes;
   private byte _monRetValAttr;           // NOT USED
   private short _monType;

   private int _offsetExpression;
   EStdExpression2 _expression = null;

   private int _monitorModuleOffset;
   private EStdString _moduleName = null;

   private int _monitorPartOffset;
   private EStdString _partName = null;

   private int _monitorFileOffset;
   private EStdString _viewFileName = null;

   private int _stmtNumberOffset;
   private EStdString _stmtNum = null;

   private static final int _fixed_length = 20;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}
