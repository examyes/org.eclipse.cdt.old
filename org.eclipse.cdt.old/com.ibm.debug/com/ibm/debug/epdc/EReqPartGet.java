package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqPartGet.java, java-epdc, eclipse-dev, 20011128
// Version 1.7.1.2 (last modified 11/28/01 16:24:18)
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
 * Part Get Request
 */
public class EReqPartGet extends EPDC_Request {

   EReqPartGet(byte[] inBuffer) throws IOException {
      super(inBuffer);
      _pId = readShort();
      _view = readShort();
      _srcFileIndex = readInt();
      _startLine = readInt();
      _numOfLines = readInt();
   }

   public EReqPartGet(short partId, short viewId, int sourceFileIndex,
                      int startLine, int numberOfLines)
   {

     super(EPDC.Remote_PartGet);
     _pId = partId;
     _view = viewId;
     _srcFileIndex = sourceFileIndex;
     _startLine = startLine;
     _numOfLines = numberOfLines;
   }

   protected int fixedLen() {
      return _fixed_length + super.fixedLen();
   }

   protected int varLen() {
      return super.varLen();
   }

   /**
    * return the part id
    */
   public short partID() {
      return _pId;
   }

   /**
    * Return view id
    */
   public short viewID() {
      return _view;
   }

   /**
    * Return source file index
    */
   public int srcFileIndex() {
      return _srcFileIndex;
   }

   /**
    * Return starting line number
    */
   public int startLine() {
      return _startLine;
   }

   /**
    * Return number of lines to get
    */
   public int numLines() {
      return _numOfLines;
   }

  public void output(DataOutputStream dataOutputStream)
    throws IOException
  {
      super.output(dataOutputStream);
      dataOutputStream.writeShort(_pId);
      dataOutputStream.writeShort(_view);
      dataOutputStream.writeInt(_srcFileIndex);
      dataOutputStream.writeInt(_startLine);
      dataOutputStream.writeInt(_numOfLines);
   }

  public void write(PrintWriter printWriter)
  {
    super.write(printWriter);

    indent(printWriter);

    printWriter.println("Part: " + _pId + "   View: " + _view +
                        "   File: " + _srcFileIndex +
                        "   Start Line: " + _startLine +
                        "   Number of lines: " + _numOfLines);
  }

   // Datafields
   private short _pId;           // part id
   private short _view;          // view id
   private int _srcFileIndex;    // source file index
   private int _startLine;       // starting line number
   private int _numOfLines;      // number of lines to get

   private static final int _fixed_length = 16;
}

