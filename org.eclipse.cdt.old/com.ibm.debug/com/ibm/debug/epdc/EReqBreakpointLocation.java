package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1995, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqBreakpointLocation.java, java-epdc, eclipse-dev, 20011128
// Version 1.14.1.2 (last modified 11/28/01 16:24:02)
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
 * EPDC Request to set a location breakpoint
 */
public class EReqBreakpointLocation extends EPDC_Request {

   /**
    * Decodes request from an input buffer
    * @exception IOException if an I/O error occurs
    */
   EReqBreakpointLocation (byte[] inBuffer) throws IOException {
      super(inBuffer);

      _inBuffer = inBuffer;
      _action = readShort();
      _type = readShort();
      _attr = readShort();

      // EEveryClause structure
      _everyVal = readInt();
      _toVal = readInt();
      _fromVal = readInt();

      _offsetVarPtr = readOffset();       // pointer to breakpoint info
      _offsetDLLNamePtr = readOffset();
      _offsetSourceNamePtr = readOffset();
      _offsetFileNamePtr = readOffset();
      _offsetExpr = readOffset();

      _DU = readInt();
      _bkpID = readInt();
      _entryID = readInt();
      _offsetStmtNum = readOffset();
      markOffset();
      _location = new EStdView(inBuffer, getOffset());
   }

   EReqBreakpointLocation(short action,
                          short bkpType,
                          short attr,
                          EEveryClause clause,
                          String addrOrExpr,
                          String moduleName,
                          String partName,
                          String fileName,
                          EStdExpression2 condition,
                          int threadID,
                          int bkpID,
                          int entryID,
                          String stmtNum,
                          EStdView context)
   {
     super(EPDC.Remote_BreakpointLocation);
     _action = action;
     _type = bkpType;
     _attr = attr;

     if ((_clause = clause) == null)
        _clause = new EEveryClause(1, 0, 1);

     if (addrOrExpr != null)
        _varPtr = new EStdString(addrOrExpr);

     if (moduleName != null)
        _DLLNamePtr = new EStdString(moduleName);

     if (partName != null)
        _sourceNamePtr = new EStdString(partName);

     if (fileName != null)
        _fileNamePtr = new EStdString(fileName);

     _condition = condition;
     _DU = threadID;
     _bkpID = bkpID;
     _entryID = entryID;

     // The support for statement breakpoint is provided from EPDC 306
     if (stmtNum != null)
         _stmtNum = new EStdString(stmtNum);

     if ((_location = context) == null)
      _location = new EStdView((short)0,(short)0,0,0);
   }

   /** Return the length of the fixed component */
   protected int fixedLen() {
      return _fixed_length + super.fixedLen();
   }

   /** Return the length of the variable component */
   protected int varLen() {
      int total = super.varLen() +
                  totalBytes(_varPtr) +
                  totalBytes(_DLLNamePtr) +
                  totalBytes(_sourceNamePtr) +
                  totalBytes(_fileNamePtr) +
                  totalBytes(_condition);

      if (_stmtNum != null)
          total += totalBytes(_stmtNum);

      return total;
   }

  public void output(DataOutputStream dataOutputStream)
    throws IOException
  {
      super.output(dataOutputStream);

      dataOutputStream.writeShort(_action);
      dataOutputStream.writeShort(_type);
      dataOutputStream.writeShort(_attr);

      _clause.output(dataOutputStream);

      int offset = fixedLen() + super.varLen(); // Our starting offset for writing
                                                // out variable length data

      // Write out the offsets of the variable length data:

      offset += writeOffsetOrZero(dataOutputStream, offset, _varPtr);
      offset += writeOffsetOrZero(dataOutputStream, offset, _DLLNamePtr);
      offset += writeOffsetOrZero(dataOutputStream, offset, _sourceNamePtr);
      offset += writeOffsetOrZero(dataOutputStream, offset, _fileNamePtr);
      int offset2 = offset;
      offset += writeOffsetOrZero(dataOutputStream, offset, _condition);

      // Continue writing out fixed portion:

      dataOutputStream.writeInt(_DU);
      dataOutputStream.writeInt(_bkpID);
      dataOutputStream.writeInt(_entryID);

      if (getEPDCVersion() > 305)
          writeOffsetOrZero(dataOutputStream, offset, _stmtNum);
      else
          dataOutputStream.writeInt(0); // 4 reserved bytes

      _location.output(dataOutputStream);

      // Now write out the variable length data:

      if (_varPtr != null)
         _varPtr.output(dataOutputStream);

      if (_DLLNamePtr != null)
         _DLLNamePtr.output(dataOutputStream);

      if (_sourceNamePtr != null)
         _sourceNamePtr.output(dataOutputStream);


      if (_fileNamePtr != null)
         _fileNamePtr.output(dataOutputStream);

      if (_condition != null)
         _condition.output(dataOutputStream, offset2);

      if (_stmtNum != null)
          _stmtNum.output(dataOutputStream);
   }

   /**
    * Get breakpoint action
    */
   public short bkpAction() {
      return _action;
   }

   /**
    * Get breakpoint type
    */
   public short bkpType() {
      return _type;
   }

   /**
    * Get breakpoint attribute
    */
   public short bkpAttr() {
      return _attr;
   }

   /**
    * Get Every value from Every To From clause
    */
   public int everyVal() {
      return _everyVal;
   }

   /**
    * Get To value from Every To From clause
    */
   public int toVal() {
      return _toVal;
   }

   /**
    * Get From value from Every To From clause
    */
   public int fromVal() {
      return _fromVal;
   }

   /**
    * Get breakpoint info
    * @exception IOException if an I/O error occurs
    */
   public String bkpVarInfo() throws IOException {
      if (_varPtr == null)
         if (_offsetVarPtr != 0)
         {
            posBuffer(_offsetVarPtr);
            _varPtr = readStdString();
         }
         else
           return null;

      return _varPtr.string();
   }

   /**
    * Get EXE/DLL name
    * @exception IOException if an I/O error occurs
    */
   public String DLLName() throws IOException {
      if (_DLLNamePtr == null)
         if (_offsetDLLNamePtr != 0)
   {
            posBuffer(_offsetDLLNamePtr);
            _DLLNamePtr = readStdString();
   }
         else
           return null;

      return _DLLNamePtr.string();
   }

   /**
    * Get source file name
    * @exception IOException if an I/O error occurs
    */
   public String sourceName() throws IOException {
      if (_sourceNamePtr == null)
   if (_offsetSourceNamePtr != 0)
         {
      posBuffer(_offsetSourceNamePtr);
      _sourceNamePtr = readStdString();
         }
         else
           return null;

      return _sourceNamePtr.string();
   }

   /**
    * Get the include file name
    * @exception IOException if an I/O error occurs
    */
   public String includeName() throws IOException {
      if (_fileNamePtr == null)
   if (_offsetFileNamePtr != 0)
         {
      posBuffer(_offsetFileNamePtr);
      _fileNamePtr = readStdString();
         }
         else
           return null;

      return _fileNamePtr.string();
   }

   /**
    * Get the statement field text
    * @exception IOException if an I/O error occurs
    */
   public String statementNumber() throws IOException {
      if (_stmtNum == null)
          if (_offsetStmtNum != 0)
          {
              posBuffer(_offsetStmtNum);
              _stmtNum = readStdString();
          }
          else
              return null;

      return _stmtNum.string();
   }

   /**
    * Get the dispatchable unit for breakpoint
    */
   public int bkpDU() {
      return _DU;
   }

   /**
    * Get the breakpoint id to be replaced
    */
   public int bkpID() {
      return _bkpID;
   }

   /**
    * Get the entry id to set breakpoint
    */
   public int bkpEntryID() {
      return _entryID;
   }

   public void setEntryID(int entryID)
   {
     _entryID = entryID;
   }

   /**
    * Get the breakpoint context
    */
   public EStdView bkpContext() {
      return _location;
   }

  /**
   * Returns the conditional expression for this breakpoint.
   * @return EStdExpression2
   * @exception IOException
   */

   public EStdExpression2 getConditionalExpression() throws IOException
   {
      if (_condition == null)  // first time requested
      {
         if (_offsetExpr != 0)
         {
            posBuffer(_offsetExpr);
            _condition = new EStdExpression2(_inBuffer,_offsetExpr);
         }
         else
           return null;
      }

      return _condition;

   }

   // Data fields
   private byte[] _inBuffer;

   private short _action;
   private short _type;
   private short _attr;

   private EEveryClause _clause;

   // EEveryClause structure
   // The following 3 fields should be removed in favour of the above
   // _clause field:

   private int _everyVal;
   private int _toVal;
   private int _fromVal;

   private int _offsetVarPtr;
   private EStdString _varPtr;
   private int _offsetDLLNamePtr;
   private EStdString _DLLNamePtr;
   private int _offsetSourceNamePtr;
   private EStdString _sourceNamePtr;
   private int _offsetFileNamePtr;
   private EStdString _fileNamePtr;
   private int _offsetExpr;
   private EStdExpression2 _condition;

   private int _DU;
   private int _bkpID;
   private int _entryID;

   private int _offsetStmtNum;
   private EStdString _stmtNum = null;

   private EStdView _location;

   private static final int _fixed_length = 66;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}
