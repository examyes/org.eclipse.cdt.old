package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EStdGenericNode.java, java-epdc, eclipse-dev, 20011128
// Version 1.10.1.2 (last modified 11/28/01 16:24:39)
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
 * Class corresponding to EStdGenericNode structure.  This is the abstract superclass to all
 * node classes.
 */
public abstract class EStdGenericNode extends EPDC_Base {

   EStdGenericNode(short nodeType) {
      _nodeType = nodeType;
   }

   static EStdGenericNode decodeEStdGenericNodeStream(byte[] packetBuffer,
                                               DataInputStream dataInputStream)
   throws IOException
   {
       EStdGenericNode type = null;
       short nodeType;
       nodeType = dataInputStream.readShort();

       // read the "reserved" field within the constructor of each node type

       switch (nodeType)
       {
          case EPDC.StdScalarNode:
               type = new EStdScalarItem(packetBuffer, dataInputStream,
                                         nodeType);
               break;
          case EPDC.StdStructNode:
               type = new EStdStructItem(packetBuffer, dataInputStream,
                                         nodeType);
               break;
          case EPDC.StdClassNode:
               type = new EStdClassItem(packetBuffer, dataInputStream,
                                        nodeType);
               break;
          case EPDC.StdArrayNode:
               type = new EStdArrayItem(packetBuffer, dataInputStream,
                                        nodeType);
               break;
          case EPDC.StdPointerNode:
               type = new EStdPointerItem(packetBuffer, dataInputStream,
                                          nodeType);
               break;
       }

       return type;
   }

   public abstract String getName();

   public abstract String getType();

   public short getGenericNodeType()
   {
     return _nodeType;
   }

   // data fields
   private short _nodeType;
}
