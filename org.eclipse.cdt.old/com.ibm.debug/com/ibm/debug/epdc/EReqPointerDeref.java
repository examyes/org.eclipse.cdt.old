package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqPointerDeref.java, java-epdc, eclipse-dev, 20011128
// Version 1.1.1.2 (last modified 11/28/01 16:25:16)
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
 * Request to de-reference a pointer
 */
public class EReqPointerDeref extends EPDC_Request
{

   /**
    * Decodes request from an input buffer
    * @exception IOException if an I/O error occurs
    */
   EReqPointerDeref(byte[] inBuffer)
   throws IOException
   {
     super(inBuffer);

     _exprID = readShort();
     _exprTreeNode = readInt();
   }

   public EReqPointerDeref(short id, int treeNodeId)
   {
     super(EPDC.Remote_PointerDeref);

     _exprID = id;
     _exprTreeNode = treeNodeId;
   }

   /**
    * Return expression ID
    */
   public short exprID() {
      return _exprID;
   }

   /**
    * Return expression tree node id
    */
   public int exprTreeNode() {
      return _exprTreeNode;
   }

   public void output(DataOutputStream dataOutputStream)
   throws IOException
   {
     super.output(dataOutputStream);

     dataOutputStream.writeShort(_exprID);
     dataOutputStream.writeInt(_exprTreeNode);
   }

  /**
   * Return the length of the fixed component
   */

   protected int fixedLen()
   {
      return _fixed_length + super.fixedLen();
   }

   // data fields
   private short _exprID;
   private int _exprTreeNode;

   private static final int _fixed_length = 6;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}

