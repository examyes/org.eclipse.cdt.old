package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2000, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EStdView.java, java-epdc, eclipse-dev, 20011128
// Version 1.16.1.2 (last modified 11/28/01 16:24:44)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;

/** Class corresponding to EStdView structure */
public class EStdView extends EPDC_Base {

  public EStdView(short PPID, short View, int SrcFileIndex, int LineNum)
  {
    _PPID = PPID;
    _View = View;
    _SrcFileIndex = SrcFileIndex;
    _LineNum = LineNum;
  }

  public EStdView(EStdView view)
  {
    _PPID = view._PPID;
    _View = view._View;
    _SrcFileIndex = view._SrcFileIndex;
    _LineNum = view._LineNum;
  }

  EStdView()
  {
  }

  /**
   * Read in view information from a buffer
   * @exeception IOException if an I/O error occurs
   */
  EStdView(byte[] inBuffer, int offset) throws IOException {
    super(inBuffer, offset);
    _PPID = readShort();
    _View = readShort();
    _SrcFileIndex = readInt();
    _LineNum = readInt();
  }

  /**
   * Read in view information from a buffer
   * @exeception IOException if an I/O error occurs
   */
  EStdView(byte[] packetBuffer, DataInputStream dataInputStream)
    throws IOException
  {
    _PPID = dataInputStream.readShort();
    _View = dataInputStream.readShort();
    _SrcFileIndex = dataInputStream.readInt();
    _LineNum = dataInputStream.readInt();
  }

  /**
   * Return true if and only if the EStdView object is not null and all the
   * elements of the two EStdView objects match.
   */
  public boolean equals(EStdView view)
  {
    if ( view != null &&
         _PPID == view.getPPID() &&
         _View == view.getViewNo() &&
         _SrcFileIndex == view.getSrcFileIndex() &&
         _LineNum == view.getLineNum() )

         return true;
    else
         return false;
  }

  public short getPPID() {
    return _PPID;
  }

  public void setPPID(short partID) {
    _PPID = partID;
  }

  public short getViewNo() {
    return _View;
  }

  public int getSrcFileIndex() {
    return _SrcFileIndex;
  }

  public int getLineNum() {
    return _LineNum;
  }

  public boolean isComplete()
  {
    return _PPID != 0 && _View != 0 && _SrcFileIndex != 0 && _LineNum != 0;
  }

  public void setLineNum(int lineNumber)
  {
    _LineNum = lineNumber;
  }

  protected int fixedLen() {
    return _fixed_length;
  }

  protected static int _fixedLen() {
    return _fixed_length;
  }

  /**
   * @deprecated Use output instead. This method will be removed eventually.
   */

  protected int toDataStreams(DataOutputStream fixedData,
			      DataOutputStream varData,
			      int baseOffset)
    throws IOException, BadEPDCCommandException
  {
    writeShort(fixedData, _PPID);
    writeShort(fixedData, _View);
    writeInt(fixedData, _SrcFileIndex);
    writeInt(fixedData, _LineNum);

    return fixedLen();
  }

  public void output(DataOutputStream dataOutputStream)
  throws IOException
  {
    dataOutputStream.writeShort(_PPID);
    dataOutputStream.writeShort(_View);
    dataOutputStream.writeInt(_SrcFileIndex);
    dataOutputStream.writeInt(_LineNum);
  }

  public void write(PrintWriter printWriter)
  {
    printWriter.print("Part: " + _PPID + "   View: " + _View +
                      "   File: " + _SrcFileIndex + "   Line: " + _LineNum);
  }

  // Data members
  private short _PPID;
  private short _View;
  private int _SrcFileIndex;
  private int _LineNum;

  private static int _fixed_length = 12;
}
