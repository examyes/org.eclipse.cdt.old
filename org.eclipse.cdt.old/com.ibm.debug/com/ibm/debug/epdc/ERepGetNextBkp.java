package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1995, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/ERepGetNextBkp.java, java-epdc, eclipse-dev, 20011128
// Version 1.16.1.2 (last modified 11/28/01 16:23:32)
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
 * Class for breakpoint change item
 */
public class ERepGetNextBkp extends EPDC_ChangeItem {

   /**
    * Create a new breakpoint change item
    */
   public ERepGetNextBkp(EPDC_EngineSession engineSession, int bkpID, int bkpFlags, int bkpType,
            String bkpTypeQual, int bkpAttr) {

      _session = engineSession;
      _bkpID = bkpID;
      _bkpFlags = (short)bkpFlags;
      _bkpType = (short)bkpType;

      if (bkpTypeQual != null && bkpTypeQual.length() > 0)
         _bkpTypeQual = new EStdString(bkpTypeQual);

      _bkpAttr = (short)bkpAttr;

      _everyVal = 1;
      _toVal = 0;
      _fromVal = 1;

      _context = new EStdView[_session._viewInfo.length];
   }

   ERepGetNextBkp(byte[] packetBuffer, DataInputStream dataInputStream, EPDC_EngineSession engineSession)
   throws IOException
   {
     _bkpID = dataInputStream.readInt();
     _bkpFlags = dataInputStream.readShort();
     _bkpType = dataInputStream.readShort();

     int offset;

     if ((offset = dataInputStream.readInt()) != 0)
        _bkpTypeQual = new EStdString
                           (
                             packetBuffer,
                             new OffsetDataInputStream (packetBuffer, offset)
                           );

     _bkpAttr = dataInputStream.readShort();

     _clause = new EEveryClause(packetBuffer, dataInputStream);

     if ((offset = dataInputStream.readInt()) != 0)
        _varPtr = new EStdString
                           (
                             packetBuffer,
                             new OffsetDataInputStream (packetBuffer, offset)
                           );

     if ((offset = dataInputStream.readInt()) != 0)
        _dllNamePtr = new EStdString
                           (
                             packetBuffer,
                             new OffsetDataInputStream (packetBuffer, offset)
                           );

     if ((offset = dataInputStream.readInt()) != 0)
        _sourceNamePtr = new EStdString
                           (
                             packetBuffer,
                             new OffsetDataInputStream (packetBuffer, offset)
                           );

     if ((offset = dataInputStream.readInt()) != 0)
        _fileNamePtr = new EStdString
                           (
                             packetBuffer,
                             new OffsetDataInputStream (packetBuffer, offset)
                           );

     if ((offset = dataInputStream.readInt()) != 0)
        _conditionalExpr =        new EStdExpression2
                           (
                             packetBuffer,
                             new OffsetDataInputStream (packetBuffer, offset)
                           );

     _bkpByteCount = dataInputStream.readInt();

     if ((offset = dataInputStream.readInt()) != 0)
        _bkpEntryReturnType = new EStdString
                           (
                             packetBuffer,
                             new OffsetDataInputStream (packetBuffer, offset)
                           );

     _bkpEntryID = dataInputStream.readInt();

     new EStdView(packetBuffer, dataInputStream);

     _bkpDU = dataInputStream.readInt();

     if ((offset = dataInputStream.readInt()) != 0)
        _bkpAddress = new EStdString
                           (
                             packetBuffer,
                             new OffsetDataInputStream (packetBuffer, offset)
                           );

     if ((offset = dataInputStream.readInt()) != 0)
        _bkpStmtNum = new EStdString
                           (
                             packetBuffer,
                             new OffsetDataInputStream (packetBuffer, offset)
                           );

     dataInputStream.readShort(); // Used to be # of views - no longer used

     _context = new EStdView[engineSession._viewInfo.length];

     for (int i = 0; i < engineSession._viewInfo.length; i++)
         _context[i] = new EStdView(packetBuffer, dataInputStream);
   }

   public int getID()
   {
     return _bkpID;
   }

   public short getType()
   {
     return _bkpType;
   }

   public EEveryClause getClause()
   {
     return _clause;
   }

   public String getEntryName()
   {
     if (_varPtr == null)
       return null;

     return _varPtr.string();
   }

   public String getDLLName()
   {
     if (_dllNamePtr == null)
       return null;

     return _dllNamePtr.string();
   }

   public String getSourceName()
   {
     if (_sourceNamePtr == null)
       return null;

     return _sourceNamePtr.string();
   }

   public String getFileName()
   {
     if (_fileNamePtr == null)
       return null;

     return _fileNamePtr.string();
   }

   public String getStatementNumber()
   {
     if (_bkpStmtNum == null)
         return null;

     return _bkpStmtNum.string();
   }

   public int getEntryID()
   {
     return _bkpEntryID;
   }

   public String getAddress()
   {
     if (_bkpAddress == null)
       return null;

     return _bkpAddress.string();
   }

  /**
   * update the conditional expression
   * @param replacement conditional expression
   */

   public void setConditionalExpr(EStdExpression2 conditionalExpr)
   {
      _conditionalExpr = conditionalExpr;
   }


   public boolean isNew()
   {
     return (_bkpFlags & EPDC.BkpNew) != 0;
   }

   public boolean isEnabled()
   {
     return (_bkpFlags & EPDC.BkpEnabled) != 0;
   }

   public boolean isDeleted()
   {
     return (_bkpFlags & EPDC.BkpDeleted) != 0;
   }

   public boolean isDeferred()
   {
     return (_bkpAttr & EPDC.BkpDefer) != 0;
   }

   public boolean isAutoSetEntry()
   {
     return (_bkpAttr & EPDC.BkpAutoSetEntry) != 0;
   }

   public boolean isReadOnly()
   {
     return (_bkpAttr & EPDC.BkpReadOnly) != 0;
   }

   public EStdView[] getContexts()
   {
     return _context;
   }

   /**
    * Return the thread id of the breakpoint
    */
   public int getDU()
   {
     return _bkpDU;
   }

   /**
    * Return the number of bytes to monitor (used only for Storage Change
    * breakpoint).
    */
   public int getByteCount()
   {
     return _bkpByteCount;
   }

   /**
    * Return the breakpoint attribute (enable, defer, or case sensitive)
    */
   public short getAttribute()
   {
     return _bkpAttr;
   }

   public String getExprString()
   {
     if (_conditionalExpr == null)
        return null;

     return _conditionalExpr.getExpressionString();
   }

  /**
   * return the conditional expression
   * @return EStdExpression2 - EPDC conditional expression
   */

   public EStdExpression2 getConditionalExpr()
   {
      return _conditionalExpr;
   }


   /**
    * Set the type-specific breakpoint info
    */
   public void setVarInfo(String varPtr) {
      if (varPtr == null || varPtr.length() <= 0)
         _varPtr = null;
      else
         _varPtr = new EStdString(varPtr);
   }

   /**
    * Set the DLL/EXE file name
    */
   public void setDLLName(String dllName) {
      if (dllName == null || dllName.length() <= 0)
         _dllNamePtr = null;
      else
         _dllNamePtr = new EStdString(dllName);
   }

   /**
    * Set source file name
    */
   public void setSourceName(String sourceName) {
      if (sourceName == null || sourceName.length() <= 0)
         _sourceNamePtr = null;
      else
         _sourceNamePtr = new EStdString(sourceName);
   }

   /**
    * Set include file name
    */
   public void setIncludeName(String includeName) {
      if (includeName == null || includeName.length() <= 0)
         _fileNamePtr = null;
      else
         _fileNamePtr = new EStdString(includeName);
   }

   /**
    * Set number of bytes to monitor
    */
   public void setByteCount(int byteCount) {
      _bkpByteCount = byteCount;
   }

   /**
    * Set the entry return type
    */
   public void setEntryReturnType(String entryReturnType) {
      if (entryReturnType == null || entryReturnType.length() <= 0)
         _bkpEntryReturnType = null;
      else
         _bkpEntryReturnType = new EStdString(entryReturnType);
   }

   /**
    * Set the breakpoint entry iD
    */
   public void setEntryID(int entryID) {
      _bkpEntryID = entryID;
   }

   /**
    * Set breakpoint dispatchable unit
    */
   public void setDU(int DU) {
      _bkpDU = DU;
   }

   /**
    * Set breakpoint address
    */
   public void setAddress(String address) {
      if (address == null || address.length() <= 0)
         _bkpAddress = null;
      else
         _bkpAddress = new EStdString(address);
   }

   /**
    * Set breakpoint statement number
    */
   public void setStatementNum(String stmtNum) {
      if (stmtNum == null || stmtNum.length() <= 0)
         _bkpStmtNum = null;
      else
         _bkpStmtNum = new EStdString(stmtNum);
   }

   /**
    * Set view context info -- must be set for each view defined in the EPDC_EngineSessin object
    * passed to the constructor
    */
   public void setBkpContext(short viewNo, short PPID, int srcFileIndex, int lineNum)  {
      _context[viewNo-1] = new EStdView(PPID, viewNo, srcFileIndex, lineNum);
   }

   /**
    * Return size of "fixed" component
    */
   protected int fixedLen() {
      return _fixed_length + _session._viewInfo.length * EStdView._fixedLen();
   }

   /**
    * Return size of "variable" component
    */
   protected int varLen() {
      return totalBytes(_bkpTypeQual) +
             totalBytes(_varPtr) +
             totalBytes(_dllNamePtr) +
             totalBytes(_sourceNamePtr) +
             totalBytes(_fileNamePtr) +
             totalBytes(_conditionalExpr) +
             totalBytes(_bkpEntryReturnType) +
             totalBytes(_bkpAddress) +
             totalBytes(_bkpStmtNum);
   }


   /**
    * Output class to data streams according to EPDC protocol.
    * @exception IOException if an I/O error occurs
    * @exception BadEPDCCommandException if the EPDC command
    *   is structured incorrectly
    */
   protected int toDataStreams(DataOutputStream fixedData,
         DataOutputStream varData, int baseOffset)
         throws IOException, BadEPDCCommandException {
      int offset = baseOffset;
      int total = fixedLen();

      writeInt(fixedData, _bkpID);
      writeShort(fixedData, _bkpFlags);
      writeShort(fixedData, _bkpType);

      offset += writeOffsetOrZero(fixedData, offset, _bkpTypeQual);

      writeShort(fixedData, _bkpAttr);

      // write out Every-To-From clause
      writeInt(fixedData, _everyVal);
      writeInt(fixedData, _toVal);
      writeInt(fixedData, _fromVal);

      offset += writeOffsetOrZero(fixedData, offset, _varPtr);

      offset += writeOffsetOrZero(fixedData, offset, _dllNamePtr);

      offset += writeOffsetOrZero(fixedData, offset, _sourceNamePtr);

      offset += writeOffsetOrZero(fixedData, offset, _fileNamePtr);

      int conditionalOffset = offset;   // save the offset to write out the conditional expression
      offset += writeOffsetOrZero(fixedData, offset, _conditionalExpr);

      writeInt(fixedData, _bkpByteCount);

      offset += writeOffsetOrZero(fixedData, offset, _bkpEntryReturnType);

      writeInt(fixedData, _bkpEntryID);

      // empty EStdView structure
      writeInt(fixedData, 0);
      writeInt(fixedData, 0);
      writeInt(fixedData, 0);

      writeInt(fixedData, _bkpDU);

      offset += writeOffsetOrZero(fixedData, offset, _bkpAddress);

      offset += writeOffsetOrZero(fixedData, offset, _bkpStmtNum);

      writeShort(fixedData, (short) _context.length);
      for (int i=0; i<_context.length; i++)
         _context[i].toDataStreams(fixedData, null, 0);

      // The fixed portion has been written out. Now write out the variable
      // portion:

      if (_bkpTypeQual != null)
         _bkpTypeQual.output(varData);

      if (_varPtr != null)
         _varPtr.output(varData);

      if (_dllNamePtr != null)
         _dllNamePtr.output(varData);

      if (_sourceNamePtr != null)
         _sourceNamePtr.output(varData);

      if (_fileNamePtr != null)
         _fileNamePtr.output(varData);

      if (_conditionalExpr != null)
         _conditionalExpr.output(varData, conditionalOffset);

      if (_bkpEntryReturnType != null)
         _bkpEntryReturnType.output(varData);

      if (_bkpAddress != null)
         _bkpAddress.output(varData);

      if (_bkpStmtNum != null)
         _bkpStmtNum.output(varData);

      total += offset - baseOffset;
      return total;
   }

   // Data fields
   private transient EPDC_EngineSession _session;

   private int _bkpID;
   private short _bkpFlags;
   private short _bkpType;
   private EStdString _bkpTypeQual;
   private short _bkpAttr;

   private EEveryClause _clause;

   // EEveryClause structure
   // The following 3 fields should be removed in favour of the above
   // _clause field:

   // Every-To-From clause
   private transient int _everyVal;
   private transient int _toVal;
   private transient int _fromVal;

   private EStdString _varPtr;
   private EStdString _dllNamePtr;
   private EStdString _sourceNamePtr;
   private EStdString _fileNamePtr;
   private EStdExpression2 _conditionalExpr;
   private int _bkpByteCount;
   private EStdString _bkpEntryReturnType;
   private int _bkpEntryID;
   private int _bkpDU;
   private EStdString _bkpAddress;
   private EStdString _bkpStmtNum;
   private EStdView[] _context;

   private static final int _fixed_length = 84;    // fixed length minus EStdView array

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}
