package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1995, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqBreakpointEvent.java, java-epdc, eclipse-dev, 20011128
// Version 1.4.1.2 (last modified 11/28/01 16:25:47)
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
 * This is the class to set the event breakpoints (load breakpoint
 * and storage change breakpoint). The Event breakpoints do not
 * currently support conditional expressions.
 */

public class EReqBreakpointEvent extends EPDC_Request
{

  /**
   * Decodes request from an input buffer
   * @exception IOException if an I/O error occurs
   */
  public EReqBreakpointEvent(byte[] inBuffer) throws IOException
  {
    super(inBuffer);

    _buffer = inBuffer;

    _action = readShort();
    _type = readShort();
    _attr = readShort();

    _clause = new EEveryClause(readInt(), readInt(), readInt());

    _offsetVarPtr = readOffset();
    _offsetDLLNamePtr = readOffset();
    _offsetSourceNamePtr = readOffset();
    _offsetFileNamePtr = readOffset();
    _offsetExpr = readOffset();

    _byteCount = readInt();

    _location = new EStdView(inBuffer, getOffset());
    posBuffer(_location.getOffset());

    _DU = readInt();
    _bkpID = readInt();

    _offsetAddr = readOffset();

    markOffset();
  }

  /**
   * Constuctor to set an address change breakpoint
   */
  public EReqBreakpointEvent(short action,
                             short bkpType,
                             short attr,
                             EEveryClause clause,
                             String addrOrExpr,
                             String moduleName,
                             String partName,
                             String fileName,
                             EStdExpression2 condition,
                             int byteCount,
                             EStdView context,
                             int threadID,  // 0 for all threads
                             int bkpID,
                             String address)
  {
    super(EPDC.Remote_BreakpointEvent);

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
    _byteCount = byteCount;

    if ((_location = context) == null)
        _location = new EStdView((short)0,(short)0,0,0);

    _DU = threadID;
    _bkpID = bkpID;

    if (address != null)
        _address = new EStdString(address);
  }

  /**
   * Get breakpoint action
   */
  public short bkpAction()
  {
     return _action;
  }

  /**
   * Get breakpoint type
   */
  public short bkpType()
  {
    return _type;
  }

  /**
   * Get breakpoint attribute
   */
  public short bkpAttr()
  {
    return _attr;
  }

  /**
   * Get the dispatchable unit for breakpoint
   */
  public int bkpDU()
  {
    return _DU;
  }

  /**
   * Get the breakpoint id to be replaced
   */
  public int bkpID()
  {
    return _bkpID;
  }

  /**
   * Get the breakpoint context
   */
  public EStdView bkpContext()
  {
    return _location;
  }

  /**
   * Get breakpoint info
   * @exception IOException if an I/O error occurs
   */
  public String bkpVarInfo() throws IOException
  {
    if(_varPtr == null && _offsetVarPtr != 0)
    {
      posBuffer(_offsetVarPtr);
      _varPtr = readStdString();
    }
    return _varPtr==null ? null : _varPtr.string();
  }

  /**
   * Get EXE/DLL name
   * @exception IOException if an I/O error occurs
   */
  public String DLLName() throws IOException
  {
    if(_DLLNamePtr == null && _offsetDLLNamePtr != 0)
    {
      posBuffer(_offsetDLLNamePtr);
      _DLLNamePtr = readStdString();
    }
    return _DLLNamePtr==null ? null : _DLLNamePtr.string();
  }

  /**
   * Get source file name
   * @exception IOException if an I/O error occurs
   */
  public String sourceName() throws IOException
  {
    if(_sourceNamePtr == null && _offsetSourceNamePtr != 0)
    {
      posBuffer(_offsetSourceNamePtr);
      _sourceNamePtr = readStdString();
    }
    return _sourceNamePtr==null ? null : _sourceNamePtr.string();
  }

  /**
   * Get include file name
   * @exception IOException if an I/O error occurs
   */
  public String includeName() throws IOException
  {
    if(_fileNamePtr == null && _offsetFileNamePtr != 0)
    {
      posBuffer(_offsetFileNamePtr);
      _fileNamePtr = readStdString();
    }
    return _fileNamePtr==null ? null : _fileNamePtr.string();
  }

  /**
   * Get breakpoint condition
   * @exception IOException if an I/O error occurs
   */
  public EStdExpression2 condition() throws IOException
  {
    if(_condition == null && _offsetExpr != 0)
    {
      _condition = new EStdExpression2(_buffer, _offsetExpr);
    }
    return _condition;
  }

  /**
   * Get the byte count.
   */
  public int byteCount()
  {
    return _byteCount;
  }

  /**
   * Return the computed address used by the address breakpoint and change
   * address breakpoint.
   */
  public String computedAddress()
  {
    if (_address == null)
        return null;

    return _address.string();
  }

  /**
   * Return the length of the variable component
   */
  protected int varLen()
  {
    return super.varLen() +
           totalBytes(_varPtr) +
           totalBytes(_DLLNamePtr) +
           totalBytes(_sourceNamePtr) +
           totalBytes(_fileNamePtr) +
           totalBytes(_condition) +
           totalBytes(_address);
  }

  /**
   * Return the length of the fixed component
   */
  protected int fixedLen()
  {
    return _fixed_length + super.fixedLen();
  }

  public void output(DataOutputStream dataOutputStream)
  throws IOException
  {
    super.output(dataOutputStream);

    dataOutputStream.writeShort(_action);
    dataOutputStream.writeShort(_type);
    dataOutputStream.writeShort(_attr);

    _clause.output(dataOutputStream);

    int offset = fixedLen() + super.varLen();

    offset += writeOffsetOrZero(dataOutputStream, offset, _varPtr);
    offset += writeOffsetOrZero(dataOutputStream, offset, _DLLNamePtr);
    offset += writeOffsetOrZero(dataOutputStream, offset, _sourceNamePtr);
    offset += writeOffsetOrZero(dataOutputStream, offset, _fileNamePtr);
    int conditionOffset = offset;
    offset += writeOffsetOrZero(dataOutputStream, offset, _condition);

    dataOutputStream.writeInt(_byteCount);
    _location.output(dataOutputStream);

    dataOutputStream.writeInt(_DU);
    dataOutputStream.writeInt(_bkpID);

    writeOffsetOrZero(dataOutputStream, offset, _address);

    if (_varPtr != null)
        _varPtr.output(dataOutputStream);

    if (_DLLNamePtr != null)
        _DLLNamePtr.output(dataOutputStream);

    if (_sourceNamePtr != null)
        _sourceNamePtr.output(dataOutputStream);

    if (_fileNamePtr != null)
        _fileNamePtr.output(dataOutputStream);

    if (_condition != null)
         _condition.output(dataOutputStream, conditionOffset);

    if (_address != null)
        _address.output(dataOutputStream);
  }

  // Data fields
  private byte[] _buffer;

  private short _action;
  private short _type;
  private short _attr;

  private EEveryClause _clause;
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

  private int _byteCount;
  private EStdView _location;
  private int _DU;
  private int _bkpID;

  private int _offsetAddr;
  private EStdString _address;

  private static final int _fixed_length = 66;

  public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";
}
