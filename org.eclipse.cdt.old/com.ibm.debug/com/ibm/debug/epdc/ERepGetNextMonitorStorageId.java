package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/ERepGetNextMonitorStorageId.java, java-epdc, eclipse-dev, 20011128
// Version 1.12.1.2 (last modified 11/28/01 16:25:28)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;

public class ERepGetNextMonitorStorageId extends EPDC_ChangeItem
{
   public ERepGetNextMonitorStorageId(short id, EStdStorageRange range, EStdStorageLocation location,
          EStdExpression2 expr, short unitStyle, int unitCount, short addressStyle,
          int beginAddress, int endAddress, int beginStorage, int endStorage, int attributeIndex, short flags,
          ERepGetNextMonitorStorageLine[] lines)
   {
      _id = id;
      _range = range;
      _location = location;
      _expr = expr;
      _unitStyle = unitStyle;
      _unitCount = unitCount;
      _addressStyle = addressStyle;
      _firstAddress = beginAddress;
      _secondAddress = endAddress;
      _firstContents = beginStorage;
      _secondContents = endStorage;
      _attributeIndex = attributeIndex;
      _flags = flags;
      _lines = lines;
   }

   ERepGetNextMonitorStorageId(byte[] packetBuffer, DataInputStream dataInputStream)
   throws IOException
   {
      _id = dataInputStream.readShort();
      _range = new EStdStorageRange(packetBuffer, dataInputStream);
      _location = new EStdStorageLocation(packetBuffer, dataInputStream);

      int offset = dataInputStream.readInt(); // offset to Expression
      if (offset > 0)
         _expr = new EStdExpression2(packetBuffer,
                                     new OffsetDataInputStream(packetBuffer,
                                                               offset)
                                    );

      _unitStyle = dataInputStream.readShort();
      _unitCount = dataInputStream.readInt();
      _addressStyle = dataInputStream.readShort();
      _firstAddress = dataInputStream.readInt();
      _secondAddress= dataInputStream.readInt();
      _firstContents = dataInputStream.readInt();
      _secondContents= dataInputStream.readInt();
      _attributeIndex = dataInputStream.readInt();
      int numberOfLines = dataInputStream.readInt();
      _flags = dataInputStream.readShort();

      offset = dataInputStream.readInt();       // offset to array of ERepGetNextMonitorStorageLine

      if (numberOfLines > 0 && offset != 0)
      {
         _lines = new ERepGetNextMonitorStorageLine[numberOfLines];

         // Get a data input stream which is positioned at the start of the
         // lines array:
         OffsetDataInputStream offsetDataInputStream =
                                     new OffsetDataInputStream(packetBuffer,
                                                               offset);

         for (int i = 0; i < numberOfLines; i++)
             _lines[i] = new ERepGetNextMonitorStorageLine(packetBuffer, offsetDataInputStream, this);
      }
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


      writeShort(fixedData, _id);
      _range.output(fixedData);

      _location.outputFixedPart(fixedData, offset);
      _location.outputVariablePart(varData);
      offset += _location.varLen();

      int exprOffset = offset;
      offset += writeOffsetOrZero(fixedData, offset, _expr);
      writeShort(fixedData, _unitStyle);
      writeInt(fixedData, _unitCount);
      writeShort(fixedData, _addressStyle);
      writeInt(fixedData, _firstAddress);
      writeInt(fixedData, _secondAddress);
      writeInt(fixedData, _firstContents);
      writeInt(fixedData, _secondContents);
      writeInt(fixedData, _attributeIndex);
      int _numberOfLines = 0;
      if(_lines!=null)
         _numberOfLines = _lines.length;
      writeInt(fixedData, _numberOfLines);
      writeShort(fixedData, _flags);
      writeInt(fixedData, offset);
      if(_expr!=null)
      {
          _expr.output(varData, exprOffset);
      }


      if (_numberOfLines > 0)
      {
         offset += (_numberOfLines * storageLineTableEntryLength);    // 'The offsetTable' consists of a 4byte lineNumber and a 4byte lineString offset

         for (int i = 0; i < _numberOfLines; i++)
         {
             offset += _lines[i].outputTable(varData, offset);
         }
         for (int i = 0; i < _numberOfLines; i++)
         {
             _lines[i].outputStorage(varData);
         }
      }

      return _fixed_length + offset - baseOffset;
   }


   protected int fixedLen()
   {
      return _fixed_length;
   }

   protected int varLen()
   {
      int length = 0;
      if(_expr!=null)
         length += totalBytes(_expr);
      if(_location.getAddress()!=null)
         length += _location.varLen();
      int lines = _lines.length;
      for(int z=0; z<lines; z++)
          length += storageLineTableEntryLength + totalBytes(_lines[z]);
      return length;
   }

   public int getFirstLineOffset()
   {
     return _range.getFirstLineOffset();
   }

   public int getLastLineOffset()
   {
     return _range.getLastLineOffset();
   }

   public String getAddress()
   {
     return _location.getAddress();
   }

   public short id()
   {
     return _id;
   }

   public boolean isNew()
   {
     return (_flags & EPDC.MonStorNew) != 0;
   }

   public boolean isDeleted()
   {
     return (_flags & EPDC.MonStorDeleted) != 0;
   }

   public boolean styleChanged()
   {
     return (_flags & EPDC.MonStorStyleChanged) != 0;
   }

   public boolean addressChanged()
   {
     return (_flags & EPDC.MonStorAddressChanged) != 0;
   }

   public boolean isEnabled()
   {
     return (_flags & EPDC.MonStorEnabled) != 0;
   }

   public boolean exprIsEnabled()
   {
     return (_flags & EPDC.MonStorExprEnabled) != 0;
   }

   public ERepGetNextMonitorStorageLine[] getLines()
   {
     return _lines;
   }

   public void dumpLines()
   {
     for (int i = 0; i < _lines.length; i++)
     {
         String[] sArray = _lines[i].getStorage();
         String s = sArray[0];
         if(sArray.length>1) s = s +" " +sArray[1];
         System.out.println(_lines[i].getLineNumber() + " " +
                            _lines[i].getAddress() + " " +
                            s );
     }
   }

   int beginAddress()
   {
     return _firstAddress;
   }

   int endAddress()
   {
     return _secondAddress;
   }

   int beginStorage()
   {
     return _firstContents;
   }

   int endStorage()
   {
     return _secondContents;
   }

   public int unitCount()
   {
      return _unitCount;
   }

   int attributeIndex()
   {
     return _attributeIndex;
   }

   public short getUnitStyle()
   {
     return _unitStyle;
   }

   public EStdExpression2 getExpression()
   {
     return _expr;
   }

   private short               _id;
   private EStdStorageRange    _range;
   private EStdStorageLocation _location;
   private EStdExpression2     _expr;
   private short               _unitStyle;    // default hexAscii=0x01, 32bitHex=0x08
   private int                 _unitCount;
   private short               _addressStyle; // typically flat=0x01
   private int                 _firstAddress;
   private int                 _secondAddress;
   private int                 _firstContents;
   private int                 _secondContents;
   private int                 _attributeIndex;
   private short               _flags;
   private ERepGetNextMonitorStorageLine[] _lines;

   private static final int storageLineTableEntryLength = 8; // 4byte LineNumber + 4byte offset
   private static final int _fixed_length = 64;
 }
