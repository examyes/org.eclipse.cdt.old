package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/ERepGetNextMonitorStorageLine.java, java-epdc, eclipse-dev, 20011128
// Version 1.6.1.2 (last modified 11/28/01 16:25:34)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;

public class ERepGetNextMonitorStorageLine extends EPDC_Base
{

   public ERepGetNextMonitorStorageLine(int lineNumber, String address, String storage)
   {
      _lineNumber = lineNumber;
      EStdString line = new EStdString("\0"+address+"\0"+storage);
      _line = line;

      _address = "????????";
      _storage = new String[1];
      _storage[0] = "??????????????????????????";
      String s = line.string();
      int startingIndex = 0;
      int indexOfNull = s.indexOf('\0',1);
      if(indexOfNull<0)
         return;
      _address = s.substring(startingIndex, indexOfNull);
      startingIndex = indexOfNull+2;
      _storage[0] = s.substring(startingIndex);
   }

  public int outputTable(DataOutputStream dataOutputStream, int storageOffset)  throws IOException
  {
    dataOutputStream.writeInt(_lineNumber);
    writeOffsetOrZero(dataOutputStream, storageOffset, _line);
    int bytesNeeded = 0;
    if(_line!=null)
       bytesNeeded = _line.totalBytes();
    return bytesNeeded;
  }
  public int outputStorage(DataOutputStream dataOutputStream)  throws IOException
  {
    int bytesUsed = 0;
    if(_line!=null)
    {
        _line.output(dataOutputStream);
        bytesUsed = _line.totalBytes();
    }
    return bytesUsed;
  }

   /** Output class to data streams according to EPDC protocol.
     * @exception IOException if an I/O error occurs
    *  @exception BadEPDCCommandException if the EPDC command
    *    is structured incorrectly
    */
   protected int toDataStreams(DataOutputStream fixedData,
         DataOutputStream varData, int baseOffset)
         throws IOException, BadEPDCCommandException {
      int offset = baseOffset;

      writeInt(varData, _lineNumber);
      offset += writeOffsetOrZero(varData, offset, _line);

      _line.output(varData);

      return _fixed_length + offset - baseOffset;
      //return fixedLen() + varLen();
   }


  ERepGetNextMonitorStorageLine(byte[] packetBuffer,
                                DataInputStream dataInputStream,
                                ERepGetNextMonitorStorageId owner)
  throws IOException
  {
    _lineNumber = dataInputStream.readInt();
    int offset;

    if ((offset = dataInputStream.readInt()) != 0)
    {
       _line = new EStdString(packetBuffer,
                              new OffsetDataInputStream(packetBuffer,
                                                        offset
                                                       )
                             );
       String line = _line.string();

       // Apparently the storage line contains null-terminated strings for
       // the address and storage contents.

       int startingIndex = owner.beginAddress();
       int indexOfNull = line.indexOf(0, startingIndex);

       _address = line.substring(startingIndex,
                                 indexOfNull);

       switch(owner.getUnitStyle())
       {
         case EPDC.StorageStyleByteHexCharacter: // Intentional fall-through:
         case EPDC.StorageStyleByteHexEBCDIC:
         case EPDC.StorageStyleByteHexDisasm:
         case EPDC.StorageStyleByteHexASCII:

              _storage = new String[2];

              // TODO: Exactly where the 2nd storage string appears is kind
              // of fuzzy so the following may need some tweaking. The EPDC
              // document doesn't spell out how to find the 2nd string. Hopefully
              // all back ends are doing it this way:

              startingIndex = owner.endStorage();
              indexOfNull = line.indexOf(0, startingIndex);

              _storage[1] = line.substring(startingIndex,
                                           indexOfNull);
              break;

         default:

              _storage = new String[1];
              break;
       }

       startingIndex = owner.beginStorage();
       indexOfNull = line.indexOf(0, startingIndex);

       _storage[0] = line.substring(startingIndex,
                                 indexOfNull);
    }
  }

  public int fixedLen()
  {
    return _fixed_length;
  }
  public int varLen()
  {
    return  totalBytes(_line);
  }

  public int getLineNumber()
  {
    return _lineNumber;
  }

  public String getAddress()
  {
    return _address;
  }

  public String[] getStorage()
  {
    return _storage;
  }

  private int _lineNumber;
  private EStdString _line;
  private String _address;
  private String[] _storage;
  private static final int _fixed_length = 0;
}
