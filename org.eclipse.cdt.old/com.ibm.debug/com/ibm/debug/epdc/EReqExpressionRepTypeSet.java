package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqExpressionRepTypeSet.java, java-epdc, eclipse-dev, 20011128
// Version 1.5.1.2 (last modified 11/28/01 16:24:11)
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
 * Set expression representation type request
 */
public class EReqExpressionRepTypeSet extends EPDC_Request {

   /**
    * Decodes request from an input buffer
    * @exception IOException if an I/O error occurs
    */
   EReqExpressionRepTypeSet (byte[] inBuffer) throws IOException {
      super(inBuffer);

      _exprID = readShort();
      _nodeID = readInt();
      _newRepType = readShort();
   }

   public EReqExpressionRepTypeSet(short exprID, int nodeID, short nodeIndex)
   {
      super(EPDC.Remote_ExpressionRepTypeSet);

      _exprID = exprID;
      _nodeID = nodeID;
      _newRepType = nodeIndex;
   }

   /**
    * Return expression ID
    */
   public short exprID() {
      return _exprID;
   }

   /**
    * Return tree node ID
    */
   public int nodeID() {
      return _nodeID;
   }

   /**
    * Return new representation type
    */
   public short newRepType() {
      return _newRepType;
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
     dataOutputStream.writeInt(_nodeID);
     dataOutputStream.writeShort(_newRepType);
   }

   // data fields
   private short _exprID;
   private int _nodeID;
   private short _newRepType;

   private static final int _fixed_length = 8;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}
