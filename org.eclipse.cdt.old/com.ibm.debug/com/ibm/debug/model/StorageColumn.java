package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/StorageColumn.java, java-model, eclipse-dev, 20011128
// Version 1.3.1.2 (last modified 11/28/01 16:12:55)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.PrintWriter;

/**
 * StorageColumn objects are contained within StorageLine objects and represent
 * individual units of storage within that line.
 * @see StorageLine
 */

public class StorageColumn extends DebugModelObject
{
  StorageColumn(StorageLine owningStorageLine,
                String storageContents,
                int offset,
                int unitFieldIndex,
                int numberOfUnits)
  {
    _owningStorageLine = owningStorageLine;
    _storageContents = storageContents;
    _offset = offset;
    _unitFieldIndex = unitFieldIndex;
    _numberOfUnits = numberOfUnits;
  }

  /**
   * Get a string containing the storage contents for this column.
   */

  public String getStorageContents()
  {
    return _storageContents;
  }

  public void print(PrintWriter printWriter)
  {
    printWriter.print(" " + _storageContents);
  }

  /**
   * Update the debuggee's storage with the given value.
   */

  public boolean update(String value, int sendReceiveControlFlags)
  throws java.io.IOException
  {
    return _owningStorageLine.update(value, _numberOfUnits, _unitFieldIndex, _offset, sendReceiveControlFlags);
  }

  private StorageLine _owningStorageLine;
  private String _storageContents;
  private int _offset; // Offset of this column within the line
  private int _unitFieldIndex;
  private int _numberOfUnits;
}
