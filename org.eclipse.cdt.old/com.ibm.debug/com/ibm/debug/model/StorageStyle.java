package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/StorageStyle.java, java-model, eclipse-dev, 20011128
// Version 1.8.1.2 (last modified 11/28/01 16:13:10)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.util.ResourceBundle;
import java.util.Locale;
import java.util.Vector;
import java.io.PrintWriter;

import com.ibm.debug.epdc.EPDC;

/**
 * StorageStyle objects are used when monitoring storage to tell the debug
 * engine how to format the storage.
 * @see EngineStorageCapabilities#getSupportedStorageStyles
 */

public class StorageStyle extends DebugModelObject
{
  private StorageStyle(short storageUnitSizeTypeAndStyle,
                       int supportFlag,
                       int bytesPerUnit
                      )
  {
    _styleIdentifier = storageUnitSizeTypeAndStyle;
    _supportFlag = supportFlag;
    _bytesPerUnit = bytesPerUnit;
  }

  short getStyleIdentifier()
  {
    return _styleIdentifier;
  }

  public String getName()
  {
    return Model.getResourceString("Storage.Style." + _styleIdentifier);
  }

  public int getBytesPerUnit()
  {
    return _bytesPerUnit;
  }

  public void print(PrintWriter printWriter)
  {
    printWriter.print("Style: " + getName() + " ");
    printWriter.print("Bytes per unit: " + _bytesPerUnit);
  }

  /**
   * Is this storage style supported by the given debug engine? It should only
   * be necessary to call this method on StorageStyle objects that were
   * obtained via the StorageStyle.getStorageStyle
   * method. It is not necessary to call this method on
   * StorageStyles that were obtained via the
   * EngineStorageCapabilities.getSupportedStorageStyles method since that
   * method <i>only</i> returns supported storage styles.
   */

  public boolean isSupported(DebugEngine engine)
  {
    EngineCapabilities engineCapabilities = engine.getCapabilities();

    if (engineCapabilities == null)
       return false;

    EngineStorageCapabilities engineStorageCapabilities =
                              engineCapabilities.getStorageCapabilities();

    if (engineStorageCapabilities == null)
       return false;

    return isSupported(engineStorageCapabilities.getBits());
  }

  private boolean isSupported(int engineSupportFlags)
  {
    return (_supportFlag & engineSupportFlags) != 0;
  }

  static Vector getSupportedStorageStyles(int engineSupportFlags)
  {
    Vector supportedStorageStyles = new Vector();

    for (short s = _firstStorageStyle; s <= _lastStorageStyle; s++)
        if (_storageStyles[s].isSupported(engineSupportFlags))
           supportedStorageStyles.addElement(_storageStyles[s]);

    if (supportedStorageStyles.size() == 0)
       return null;
    else
       return supportedStorageStyles;
  }

  /**
   * Use this method to retrieve a specific storage style. Note that, before
   * using the storage style, client code should call isSupported on it to
   * ensure that the debug engine actually supports this storage style.
   * As an alternative, client code can call
   * EngineStorageCapabilities.getSupportedStorageStyles which will return
   * a list of storage styles that are supported.
   * @param styleIdentifier A value which identifies the requested storage
   * style. There are constants in class com.ibm.debug.epdc.EPDC which
   * define all possible storage styles.
   * @see EngineStorageCapabilities#getSupportedStorageStyles
   * @see com.ibm.debug.epdc.EPDC#StorageStyleByteHexCharacter
   */

  public static StorageStyle getStorageStyle(short styleIdentifier)
  {
    return _storageStyles[styleIdentifier];
  }

  private short _styleIdentifier;

  private int _supportFlag;
  private int _bytesPerUnit;

  private static final short _firstStorageStyle = EPDC.StorageStyleByteHexCharacter;
  private static final short _lastStorageStyle = EPDC.StorageStyle64BitFlat;

  private static StorageStyle[] _storageStyles = new StorageStyle[_lastStorageStyle + 1];

  static
  {
     // Build an array of all possible storage styles:

      _storageStyles[EPDC.StorageStyleByteHexCharacter]
           = new StorageStyle(EPDC.StorageStyleByteHexCharacter, EPDC.FCT_STORAGE_CONTENT_HEX_CHAR, 1);

      _storageStyles[EPDC.StorageStyleByteCharacter]
           = new StorageStyle(EPDC.StorageStyleByteCharacter, EPDC.FCT_STORAGE_CONTENT_CHAR, 1);

      _storageStyles[EPDC.StorageStyle16BitIntSigned]
           = new StorageStyle(EPDC.StorageStyle16BitIntSigned, EPDC.FCT_STORAGE_CONTENT_16INT, 2);

      _storageStyles[EPDC.StorageStyle16BitIntUnsigned]
           = new StorageStyle(EPDC.StorageStyle16BitIntUnsigned, EPDC.FCT_STORAGE_CONTENT_16UINT, 2);

      _storageStyles[EPDC.StorageStyle16BitIntHex]
           = new StorageStyle(EPDC.StorageStyle16BitIntHex, EPDC.FCT_STORAGE_CONTENT_16INTHEX, 2);

      _storageStyles[EPDC.StorageStyle32BitIntSigned]
           = new StorageStyle(EPDC.StorageStyle32BitIntSigned, EPDC.FCT_STORAGE_CONTENT_32INT, 4);

      _storageStyles[EPDC.StorageStyle32BitIntUnsigned]
           = new StorageStyle(EPDC.StorageStyle32BitIntUnsigned, EPDC.FCT_STORAGE_CONTENT_32UINT, 4);

      _storageStyles[EPDC.StorageStyle32BitIntHex]
           = new StorageStyle(EPDC.StorageStyle32BitIntHex, EPDC.FCT_STORAGE_CONTENT_32INTHEX, 4);

      _storageStyles[EPDC.StorageStyle32BitFloat]
           = new StorageStyle(EPDC.StorageStyle32BitFloat, EPDC.FCT_STORAGE_CONTENT_32FLOAT, 4);

      _storageStyles[EPDC.StorageStyle64BitFloat]
           = new StorageStyle(EPDC.StorageStyle64BitFloat, EPDC.FCT_STORAGE_CONTENT_64FLOAT, 8);

      _storageStyles[EPDC.StorageStyle80BitFloat]
           = new StorageStyle(EPDC.StorageStyle80BitFloat, EPDC.FCT_STORAGE_CONTENT_88FLOAT, 10);

      _storageStyles[EPDC.StorageStyle16BitNear]
           = new StorageStyle(EPDC.StorageStyle16BitNear, EPDC.FCT_STORAGE_CONTENT_16PTR, 2);

      _storageStyles[EPDC.StorageStyle16BitFar]
           = new StorageStyle(EPDC.StorageStyle16BitFar, EPDC.FCT_STORAGE_CONTENT_1616PTR, 2);

      _storageStyles[EPDC.StorageStyle32BitFlat]
           = new StorageStyle(EPDC.StorageStyle32BitFlat, EPDC.FCT_STORAGE_CONTENT_32PTR, 4);

      _storageStyles[EPDC.StorageStyleByteHexEBCDIC]
           = new StorageStyle(EPDC.StorageStyleByteHexEBCDIC, EPDC.FCT_STORAGE_CONTENT_HEX_EBCDIC, 1);

      _storageStyles[EPDC.StorageStyleByteEBCDIC]
           = new StorageStyle(EPDC.StorageStyleByteEBCDIC, EPDC.FCT_STORAGE_CONTENT_EBCDIC, 1);

      _storageStyles[EPDC.StorageStyleByteHexDisasm]
           = new StorageStyle(EPDC.StorageStyleByteHexDisasm, 0, 1);

      _storageStyles[EPDC.StorageStyleByteHexASCII]
           = new StorageStyle(EPDC.StorageStyleByteHexASCII, EPDC.FCT_STORAGE_CONTENT_HEX_ASCII, 1);

      _storageStyles[EPDC.StorageStyleByteASCII]
           = new StorageStyle(EPDC.StorageStyleByteASCII, EPDC.FCT_STORAGE_CONTENT_ASCII, 1);

      _storageStyles[EPDC.StorageStyle32IEEE]
           = new StorageStyle(EPDC.StorageStyle32IEEE, EPDC.FCT_STORAGE_CONTENT_IEEE_32, 4);

      _storageStyles[EPDC.StorageStyle64IEEE]
           = new StorageStyle(EPDC.StorageStyle64IEEE, EPDC.FCT_STORAGE_CONTENT_IEEE_64, 8);

      _storageStyles[EPDC.StorageStyle64BitIntSigned]
           = new StorageStyle(EPDC.StorageStyle64BitIntSigned, EPDC.FCT_STORAGE_CONTENT_64INT, 8);

      _storageStyles[EPDC.StorageStyle64BitIntUnsigned]
           = new StorageStyle(EPDC.StorageStyle64BitIntUnsigned, EPDC.FCT_STORAGE_CONTENT_64UINT, 8);

      _storageStyles[EPDC.StorageStyle64BitIntHex]
           = new StorageStyle(EPDC.StorageStyle64BitIntHex, EPDC.FCT_STORAGE_CONTENT_64INTHEX, 8);

      _storageStyles[EPDC.StorageStyle64BitFlat]
           = new StorageStyle(EPDC.StorageStyle64BitFlat, EPDC.FCT_STORAGE_CONTENT_64PTR, 8);
  }
}
