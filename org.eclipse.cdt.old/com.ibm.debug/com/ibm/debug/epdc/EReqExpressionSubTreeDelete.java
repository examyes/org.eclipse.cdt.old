package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqExpressionSubTreeDelete.java, java-epdc, eclipse-dev, 20011128
// Version 1.4.1.2 (last modified 11/28/01 16:24:13)
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
 * Expression subtree delete request
 */
public class EReqExpressionSubTreeDelete extends EPDC_Request {

   /**
    * Read in request packet from a buffer
    * @exeception IOException if an I/O error occurs
    */
   protected EReqExpressionSubTreeDelete(byte[] inBuffer) throws IOException {
      super(inBuffer);
      _exprID = readShort();
      _exprTreeNodeID = readInt();
      _exprTreeStartChild = readInt();
      _exprTreeEndChild = readInt();
   }

   public EReqExpressionSubTreeDelete(short exprID, int exprTreeNodeID,
                                      int startChild, int endChild)
   {
     super(EPDC.Remote_ExpressionSubTreeDelete);

     _exprID = exprID;
     _exprTreeNodeID = exprTreeNodeID;
     _exprTreeStartChild = startChild;
     _exprTreeEndChild = endChild;
   }

   /**
    * Return monitor expression id
    */
   public short exprID() {
      return _exprID;
   }

   /**
    * Return root node id of expression tree
    */
   public int exprTreeNodeID() {
      return _exprTreeNodeID;
   }

   /**
    * Return start node of subtree to delete
    */
   public int exprTreeStartChild() {
      return _exprTreeStartChild;
   }

   /**
    * Return end node of subtree to delete
    */
   public int exprTreeEndChild() {
      return _exprTreeEndChild;
   }

   /**
    * Return the length of the fixed component
    */
   protected int fixedLen()
   {
      return _fixed_length + super.fixedLen();
   }

   public void output(DataOutputStream dataOutputStream)
   throws IOException
   {
     super.output(dataOutputStream);

     dataOutputStream.writeShort(_exprID);
     dataOutputStream.writeInt(_exprTreeNodeID);
     dataOutputStream.writeInt(_exprTreeStartChild);
     dataOutputStream.writeInt(_exprTreeEndChild);
   }

   // data fields
   private short _exprID;           // monitor expression id
   private int _exprTreeNodeID;     // root node id of expression tree
   private int _exprTreeStartChild; // start node of subtree to delete
   private int _exprTreeEndChild;   // end node of subtree to delete

   private static final int _fixed_length = 14;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}

