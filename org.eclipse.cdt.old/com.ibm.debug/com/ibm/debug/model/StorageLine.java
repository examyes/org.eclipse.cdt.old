package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/StorageLine.java, java-model, eclipse-dev, 20011128
// Version 1.13.1.2 (last modified 11/28/01 16:12:42)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.epdc.*;
import java.io.PrintWriter;
import java.util.Vector;

/**
 * A StorageLine object contains pre-formatted strings which can be displayed
 * to the user to show the contents of the debuggee's storage. Each StorageLine
 * object contains the following:
 * <ul>
 * <li>A String containing the starting address for this line of storage. See
 *     getAddress().
 * <li>One or more strings containing the storage for the entire line. See
 *     getStorage().
 * <li>An array of StorageColumn objects. See getStorageColumns.
 * </ul>
 */

public class StorageLine extends DebugModelObject
{
  StorageLine(Storage owningStorage, ERepGetNextMonitorStorageLine epdcStorageLine,
              int offset)
  {
    _owningStorage = owningStorage;
    _offset = offset;
    change(epdcStorageLine, true);
  }

  public void addEventListener(StorageLineEventListener eventListener)
  {
    _eventListeners.addElement(eventListener);
  }

  public void removeEventListener(StorageLineEventListener eventListener)
  {
     int index = _eventListeners.indexOf(eventListener);

     if (index != -1)
     {
         try
         {
           _eventListeners.setElementAt(null, index);
         }
         catch(ArrayIndexOutOfBoundsException excp)
         {
         }
     }
  }

  void change(ERepGetNextMonitorStorageLine epdcStorageLine, boolean isNew)
  {
    _epdcStorageLine = epdcStorageLine;

    String[] storage = getStorage();

    String mainStorageString = storage[0];

    int numberOfStrings = storage.length;

    int numberOfUnitsPerLine = _owningStorage.getNumberOfUnitsPerLine();

    _storageColumns = new StorageColumn[numberOfUnitsPerLine +
                                        (numberOfStrings > 1 ? 1 : 0)];

    int charsPerColumn = mainStorageString.length() / numberOfUnitsPerLine;

    for (int i = 0, j = 0; i < numberOfUnitsPerLine; i++, j += charsPerColumn)
        _storageColumns[i] = new StorageColumn(this,
                                               mainStorageString.substring(j, j+charsPerColumn),
                                               i, // Column offset
                                               1, // Unit Field Index
                                               1); // Number of units in the column

    // If we have 2 strings for this storage line, put the 2nd string in
    // the last column:

    if (numberOfStrings > 1)
       _storageColumns[_storageColumns.length - 1] = new StorageColumn(this,
                                                                       storage[1],
                                                                       0, // Column offset
                                                                       2, // Unit Field Index
                                                                       numberOfUnitsPerLine);
    if (!isNew)
    {
       DebugEngine debugEngine = _owningStorage.getOwningProcess().debugEngine();

       int requestCode = debugEngine.getMostRecentReply().getReplyCode();

       debugEngine.getEventManager().addEvent(new StorageLineChangedEvent(this,
                                                                   this,
                                                                   requestCode
                                                                  ),
                                              _eventListeners
                                             );
    }
  }

  /**
   * Update the debuggee's storage with the given value.
   * Note that in most cases, StorageColumn.update should be used to update
   * storage. This method is provided only for those situations in which
   * StorageColumn.update is not appropriate for some reason.
   */

  public boolean update(String value, int numberOfUnits, int unitFieldIndex, int columnOffset, int sendReceiveControlFlags)
  throws java.io.IOException
  {
    return _owningStorage.update(value, numberOfUnits, unitFieldIndex, _offset, columnOffset, sendReceiveControlFlags);
  }

  /**
   * Get the line number of this line relative to the first line in the
   * storage monitor to which it belongs. Storage lines are numbered
   * starting at 0.
   */

  public int getLineNumber()
  {
    return _epdcStorageLine.getLineNumber();
  }

  /**
   * Get the line offset of this line relative to the line containing the
   * base addr of the storage being monitored e.g. 0 means this IS the line
   * containing the base address, -1 means this line is the line immediately
   * BEFORE the line containing the base address, 1 means this line is the line
   * immediately AFTER the line containing the base address, etc.
   */

  public int getLineOffset()
  {
    return _offset;
  }

  /**
   * Get the starting address for this line of storage.
   */

  public String getAddress()
  {
    return _epdcStorageLine.getAddress();
  }

  /**
   * Get a string containing formatted storage contents. This string is
   * exactly what was returned from the debug engine and may not in itself
   * be suitable for displaying to the user. Other methods are available for
   * obtaining the storage contents as a series of columns, with each column
   * containing the value for a single unit of storage.
   * <p>Exactly how many Strings are returned by this method depends on the
   * style in which the debug engine has been asked to format the storage.
   * For most styles there will be only one string, but for the following
   * styles, there could be two:
   * <ul>
   * <li>EPDC.StorageStyleByteHexCharacter
   * <li>EPDC.StorageStyleByteHexEBCDIC
   * <li>EPDC.StorageStyleByteHexDisasm
   * <li>EPDC.StorageStyleByteHexASCII
   * </ul>
   * Each string shows the contents of the same piece of storage but in a
   * different format. For example, if the style is StorageStyleByteHexCharacter
   * then the 1st string will show the storage formatted as hex digits, while
   * the 2nd string will show it formatted as characters.
   * @see EPDC#StorageStyleByteHexCharacter
   * @see StorageLine#getStorageColumns
   */

  public String[] getStorage()
  {
    return _epdcStorageLine.getStorage();
  }

  /**
   * Get an array of StorageColumn objects. Each StorageColumn object
   * represents one unit of storage in this line, with the following
   * exception: Certain storage styles result in more than one formatted
   * string per line (see getStorage(), above). For these styles, there will
   * be one StorageColumn object for every unit of storage in the first string
   * plus one more StorageColumn object for the entire second string i.e.
   * number of columns = number of units + 1. These storage styles typically
   * have hex digits in the first string and characters (ASCII, EBCDIC, etc.)
   * in the second string. For example, if the storage style is
   * EPDC.StorageStyleByteHexCharacter and the number of units per line is 8,
   * there will be a total of 9 columns: 8 columns containing 2 hex digits each plus
   * 1 column with the entire string of 8 chars.
   */

  public StorageColumn[] getStorageColumns()
  {
    return _storageColumns;
  }

  public void print(PrintWriter printWriter)
  {
    printWriter.print(getLineNumber() + " " + getAddress());

    String[] storage = getStorage();

    for (int i = 0; i < storage.length; i++)
        printWriter.print(" " + storage[i]);

    printWriter.println();

    printWriter.print(getLineNumber() + " " + getAddress());

    for (int i = 0; i < _storageColumns.length; i++)
        _storageColumns[i].print(printWriter);

    printWriter.println();
  }

  void cleanup()
  {
    _epdcStorageLine = null;
    _owningStorage = null;
    if (_storageColumns != null)
    {
        int cnt = _storageColumns.length;
        for (int i = 0; i < cnt; i++) _storageColumns[i] = null;
       _storageColumns = null;
    }
    if (_eventListeners != null)
       _eventListeners.removeAllElements();
  }

  private ERepGetNextMonitorStorageLine _epdcStorageLine;
  private Storage _owningStorage;
  private StorageColumn[] _storageColumns;
  private Vector _eventListeners = new Vector();

  // this is the line offset of this line relative to the line containing the
  // base addr of the storage being monitored e.g. 0 means this IS the line
  // containing the base address, -1 means this line is the line immediately
  // BEFORE the line containing the base address, 1 means this line is the line
  // immediately AFTER the line containing the base address, etc.

  private int _offset;

}
