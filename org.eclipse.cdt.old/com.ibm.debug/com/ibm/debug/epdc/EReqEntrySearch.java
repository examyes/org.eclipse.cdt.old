package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqEntrySearch.java, java-epdc, eclipse-dev, 20011128
// Version 1.6.1.2 (last modified 11/28/01 16:24:03)
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
 * Entry Search request
 */
public class EReqEntrySearch extends EPDC_Request {

   /**
    * Constructor from an entry search request.
    * To request a list of functions, only need to fill in the partID
    * and type of search.
    */
   public EReqEntrySearch(short partID, String entryName, int entryID, byte caseInsensitive)
   {
      super(EPDC.Remote_EntrySearch);
      _partID = partID;
      if (entryName != null)
         _entryName = new EStdString(entryName);
      _entryID = entryID;
      _caseInsensitive = caseInsensitive;
   }


   /**
    * Decodes request from an input buffer
    * @exception IOException if an I/O error occurs
    */
   EReqEntrySearch(byte[] inBuffer) throws IOException {
      super(inBuffer);

      _partID = readShort();
      _offsetEntryName = readOffset();
      _entryID = readInt();
      _caseInsensitive = readChar();
      markOffset();
      _entryName = null;
   }

   public void output(DataOutputStream dataOutputStream)
      throws IOException
   {
      super.output(dataOutputStream);  //for epdc_request_header

      dataOutputStream.writeShort(_partID);

      int offset = fixedLen() + super.varLen(); // The starting offset for writing out
                                                // the variable entry name

      //Write out the offset of the variable entry name:

      writeOffsetOrZero(dataOutputStream, offset, _entryName);

      //Continue writing out fixed portion:

      dataOutputStream.writeInt(_entryID);
      dataOutputStream.writeByte(_caseInsensitive);  //language dependent

      //Write out the variable entry name:

      if (_entryName != null)
         _entryName.output(dataOutputStream);


   }

   /**
    * Return part ID
    */
   public short partID() {
      return _partID;
   }

   /**
    * Return entry name
    * @exception IOException if an I/O error occurs
    */
   public String entryName() throws IOException
   {
      if (_entryName == null)
         if (_offsetEntryName != 0)
         {
            posBuffer(_offsetEntryName);
            _entryName = readStdString();
         }
         else
            return null;
      return _entryName.string(); //check from the class EStdString
   }

   /**
    * Return entry ID
    */
   public int entryID() {
      return _entryID;
   }

   /**
    * Return if search is case sensitive */
   public boolean caseSensitive() {
      if (_caseInsensitive == 0)
         return true;
      return false;
   }

   /**
    * Return size of "fixed" portion
    */
   protected int fixedLen()
   {
      int total = _fixed_length + super.fixedLen();

      return total;
   }

   /**
    * Return size of "variable" portion
    */
   protected int varLen()
   {

      int total = totalBytes(_entryName) + super.varLen();

      return total;
   }

   // data fields
   private short _partID;
   private int _offsetEntryName;
   private int _entryID;
   private byte _caseInsensitive;
   private EStdString _entryName;

   private static final int _fixed_length = 11;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}

