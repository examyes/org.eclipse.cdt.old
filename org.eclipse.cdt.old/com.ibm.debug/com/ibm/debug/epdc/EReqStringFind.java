package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqStringFind.java, java-epdc, eclipse-dev, 20011128
// Version 1.9.1.2 (last modified 11/28/01 16:24:28)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;

public class EReqStringFind extends EPDC_Request
{
  public EReqStringFind(EStdView startingLocation,
                        String string,
                        int numLinesToSearch,
                        int startingColumn,
                        boolean caseSensitive)
  {
    super(EPDC.Remote_StringFind);

    _findContext = startingLocation;
    _searchString = new EStdString(string);
    _numLinesToSearch = numLinesToSearch;
    _startColumnNum = startingColumn;

    if (caseSensitive)
       _searchFlags = CaseSensitive;
  }

  /**
   * Decodes request
   */
  EReqStringFind(byte[] packetBuffer, DataInputStream dataInputStream)
    throws IOException
  {
    super(packetBuffer, dataInputStream);

    _findContext = new EStdView(packetBuffer, dataInputStream);

    if ((_searchStringOffset = dataInputStream.readInt()) != 0)
      _searchString =
	new EStdString(packetBuffer,
		       new OffsetDataInputStream(packetBuffer, _searchStringOffset));

    _numLinesToSearch = dataInputStream.readInt();
    _startColumnNum = dataInputStream.readInt();
    _searchFlags = dataInputStream.readInt();
  }

  public String getSearchString()
  {
     return _searchString.string();
  }

  public int getNumLinesToSearch()
  {
     return _numLinesToSearch;
  }

  public int getStartLine()
  {
     return _findContext.getLineNum();
  }

  public int getStartColumn()
  {
     return _startColumnNum;
  }

  public short getViewNumber()
  {
     return _findContext.getViewNo();
  }

  public short getPartID()
  {
     return _findContext.getPPID();
  }

  public int getSrcFileIndex()
  {
     return _findContext.getSrcFileIndex();
  }

  public int getSearchFlags()
  {
     return _searchFlags;
  }

  public void output(DataOutputStream dataOutputStream)
  throws IOException
  {
      super.output(dataOutputStream);

      _findContext.output(dataOutputStream);
      writeOffsetOrZero(dataOutputStream, fixedLen() + super.varLen(), _searchString);
      dataOutputStream.writeInt(_numLinesToSearch);
      dataOutputStream.writeInt(_startColumnNum);
      dataOutputStream.writeInt(_searchFlags);

      // Now write out the variable length data:

      if (_searchString != null)
         _searchString.output(dataOutputStream);
   }

  protected int fixedLen()
  {
     return _fixed_length + super.fixedLen();
  }

  protected int varLen()
  {
      return super.varLen() + totalBytes(_searchString);
  }

  private EStdView   _findContext;
  private int        _searchStringOffset;
  private EStdString _searchString;
  private int        _numLinesToSearch;
  private int        _startColumnNum;
  private int        _searchFlags;
  private static final int CaseSensitive = 0x80000000;
  private static final int _fixed_length = 28;
}
