package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqStorageStyleSet.java, java-epdc, eclipse-dev, 20011128
// Version 1.2.1.2 (last modified 11/28/01 16:25:40)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;

public class EReqStorageStyleSet extends EPDC_Request
{
  public EReqStorageStyleSet(short id, String address, short storageUnitSizeTypeAndStyle, int numberOfUnitsPerLine)
  {
    super(EPDC.Remote_StorageStyleSet);
    _id = id;
    _addressStyle = EPDC.StorageAddrStyleFlat;
    _unitStyle = storageUnitSizeTypeAndStyle;
    _styleUnitCount = numberOfUnitsPerLine;
    _location = new EStdStorageLocation(address);
  }

   /**
    * Decodes request from an input buffer
    * @exception IOException if an I/O error occurs
    */
   EReqStorageStyleSet (byte[] inBuffer) throws IOException {
      super(inBuffer);

      _id = readShort();

      int _addressStyle = readInt();
      int _unitStyle = readInt();
      int _styleUnitCount = readInt();

      int _baseAddressOffset = readInt();
      int _lineOffset = readInt();
      int _unitOffset = readInt();
      if(_baseAddressOffset!=0)
      {   EStdString add = new EStdString(inBuffer, _baseAddressOffset);
          _location = new EStdStorageLocation(add.string(),_lineOffset,_unitOffset );
      }

   }


  public void output(DataOutputStream dataOutputStream)
  throws IOException
  {
    super.output(dataOutputStream);

    dataOutputStream.writeShort(_id);
    dataOutputStream.writeShort(_addressStyle);
    dataOutputStream.writeShort(_unitStyle);
    dataOutputStream.writeInt(_styleUnitCount);
    _location.output(dataOutputStream, fixedLen() + super.varLen());
  }

  /** Return the length of the fixed component */
  protected int fixedLen()
  {
     return _fixed_length + super.fixedLen();
  }

  /** Return the length of the variable component */
  protected int varLen()
  {
     return super.varLen() + _location.varLen();
  }

  public short  getID()             { return _id;                       }
  public short  getAddressStyle()   { return _addressStyle;             }
  public short  getUnitStyle()      { return _unitStyle;                }
  public int    getStyleUnitCount() { return _styleUnitCount;           }
  public String getBaseAddress()    { return _location.getAddress();    }
  public int    getLineOffset()     { return _location.getLineOffset(); }
  public int    getUnitOffset()     { return _location.getUnitOffset(); }

  private short _id;
  private short _addressStyle;
  private short _unitStyle;
  private int _styleUnitCount;
  private EStdStorageLocation _location;

  private static final int _fixed_length = 22;
}
