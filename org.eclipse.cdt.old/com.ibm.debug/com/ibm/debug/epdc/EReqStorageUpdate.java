package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqStorageUpdate.java, java-epdc, eclipse-dev, 20011128
// Version 1.3.1.2 (last modified 11/28/01 16:25:41)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;

public class EReqStorageUpdate extends EPDC_Request
{
  public EReqStorageUpdate(short id,
                           String address,
                           int lineOffset,
                           int columnOffset,
                           int unitFieldIndex,
                           int numberOfUnits,
                           String value)
  {
    super(EPDC.Remote_StorageUpdate);
    _id = id;
    _location = new EStdStorageLocation(address, lineOffset, columnOffset);
    _unitFieldIndex = unitFieldIndex; // no idea what this is
    _numberOfUnits = numberOfUnits;
    _value = new EStdString(value);
  }

   /**
    * Decodes request from an input buffer
    * @exception IOException if an I/O error occurs
    */
   EReqStorageUpdate (byte[] inBuffer) throws IOException {
      super(inBuffer);

      _id = readShort();

      int _baseAddressOffset = readInt();
      int _lineOffset = readInt();
      int _unitOffset = readInt();
      if(_baseAddressOffset!=0)
      {   EStdString add = new EStdString(inBuffer, _baseAddressOffset);
          _location = new EStdStorageLocation(add.string(),_lineOffset,_unitOffset );
      }

      int _unitFieldIndex = readInt();
      int _numberOfUnits = readInt();
      int valueOffset = readOffset();
      if(valueOffset!=0)
         _value = new EStdString(inBuffer, valueOffset);
   }


  public void output(DataOutputStream dataOutputStream)
  throws IOException
  {
    super.output(dataOutputStream);

    dataOutputStream.writeShort(_id);

    int locationOffset = fixedLen() + super.varLen();

    _location.outputFixedPart(dataOutputStream, locationOffset);

    dataOutputStream.writeInt(_unitFieldIndex);
    dataOutputStream.writeInt(_numberOfUnits);

    writeOffsetOrZero(dataOutputStream, locationOffset + _location.varLen(), _value);

    _location.outputVariablePart(dataOutputStream);
    _value.output(dataOutputStream);
  }

  /** Return the length of the fixed component */
  protected int fixedLen()
  {
     return _fixed_length + super.fixedLen();
  }

  /** Return the length of the variable component */
  protected int varLen()
  {
     return super.varLen() + _location.varLen() + totalBytes(_value);
  }

  private short _id;
  public  short getID() { return _id; }
  private EStdStorageLocation _location;
  public  String getBaseAddress()
  {  if(_location==null) return null;
     else                return _location.getAddress();
  }
  public  int getLineOffset()
  {  if(_location==null) return 0;
     else                return _location.getLineOffset();
  }
  public  int getUnitOffset()
  {  if(_location==null) return 0;
     else                return _location.getUnitOffset();
  }
  private int _unitFieldIndex;
  private int _numberOfUnits;
  private EStdString _value;
  public  String getValue()  { return _value.string(); }

  private static final int _fixed_length = 26;
}
