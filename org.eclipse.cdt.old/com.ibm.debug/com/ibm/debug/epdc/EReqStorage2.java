package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqStorage2.java, java-epdc, eclipse-dev, 20011128
// Version 1.6.1.2 (last modified 11/28/01 16:25:27)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;

public class EReqStorage2 extends EPDC_Request
{
  public EReqStorage2(EStdExpression2 expr,
                      int offsetToFirstLine,
                      int offsetToLastLine,
                      short storageUnitSizeTypeAndStyle,
                      int numberOfUnitsPerLine,
                      boolean enableStorage,
                      boolean enableExpression
                     )
  {
    super(EPDC.Remote_Storage2);
    _addressExpression = expr;
    _range = new EStdStorageRange(offsetToFirstLine, offsetToLastLine);
    _addressStyle = EPDC.StorageAddrStyleFlat;
    _unitStyle = storageUnitSizeTypeAndStyle;
    _styleUnitCount = numberOfUnitsPerLine;

    if (enableStorage)
       _attributes = EPDC.StorageEnabled;

    if (enableExpression)
       _attributes |= EPDC.StorageExprEnabled;
  }


   /**
    * Decodes request from an input buffer
    * @exception IOException if an I/O error occurs
    */
   EReqStorage2 (byte[] inBuffer) throws IOException {
      super(inBuffer);

      int _startLineOffset = readInt();
      int _endLineOffset = readInt();
      _range = new EStdStorageRange(_startLineOffset,_endLineOffset);
      _addressStyle = readShort();
      _unitStyle = readShort();
      _styleUnitCount = readInt();
      _attributes = readInt();
      int _addressExpressionOffset = readOffset();
      if(_addressExpressionOffset!=0)
         _addressExpression = new EStdExpression2(inBuffer, _addressExpressionOffset);
   }


  public void output(DataOutputStream dataOutputStream)
  throws IOException
  {
    super.output(dataOutputStream);

    _range.output(dataOutputStream);
    dataOutputStream.writeShort(_addressStyle);
    dataOutputStream.writeShort(_unitStyle);
    dataOutputStream.writeInt(_styleUnitCount);
    dataOutputStream.writeInt(_attributes);

    int offset = fixedLen() + super.varLen();

    writeOffsetOrZero(dataOutputStream, offset, _addressExpression);

    if (_addressExpression != null)
       _addressExpression.output(dataOutputStream, offset);
  }

  /** Return the length of the fixed component */
  protected int fixedLen()
  {
     return _fixed_length + super.fixedLen();
  }

  /** Return the length of the variable component */
  protected int varLen()
  {
     return super.varLen() + totalBytes(_addressExpression);
  }

  public short getAddressStyle()
  {  return _addressStyle;
  }
  public EStdExpression2 getAddressExpression()
  {  return _addressExpression;
  }
  public String getAddressExpr()
  {  if(_addressExpression==null) return null;
     else
     {  try { return _addressExpression.getExprString(); }
        catch(java.io.IOException exc)  { return null; }
     }
  }
  public int getAddressDU()
  {  if(_addressExpression==null) return -1;
     else return _addressExpression.getExprDU();
  }
  public int getAddressPPID()
  {  if(_addressExpression==null) return -1;
     else return _addressExpression.getContext().getPPID();
  }
  public int getAddressLineNum()
  {  if(_addressExpression==null) return -1;
     else return _addressExpression.getContext().getLineNum();
  }

  public short getUnitStyle()
  {  return _unitStyle; }
  public int getStyleUnitCount()
  {  return _styleUnitCount; }
  public int getAttributes()
  {  return _attributes; }
  public int getRangeStart()
  {  return _range.getFirstLineOffset();  }
  public int getRangeEnd()
  {  return _range.getLastLineOffset();  }

  private EStdStorageRange _range;
  private short _addressStyle;
  private short _unitStyle;
  private int _styleUnitCount;
  private int _attributes;
  private EStdExpression2 _addressExpression;

  private static final int _fixed_length = 24;
}
