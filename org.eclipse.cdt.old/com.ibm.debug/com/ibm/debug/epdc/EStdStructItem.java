package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EStdStructItem.java, java-epdc, eclipse-dev, 20011128
// Version 1.6.1.2 (last modified 11/28/01 16:25:06)
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
 * Class corresponding to an EStdStructItem structure.
 */
public class EStdStructItem extends EStdGenericNode {

   /**
    * Create a new EStdStructItem class
    * @param structItemCount number of items in the struct
    * @param structName name of the structure
    * @param structType type of the structure
    */
   public EStdStructItem(int structItemCount, String structName, String structType)
   {
      super(EPDC.StdStructNode);

      _structItemCount = structItemCount;
      _structName = new EStdString(structName);
      _structType = new EStdString(structType);
   }

   public EStdStructItem(byte[] packetBuffer, DataInputStream dataInputStream,
                         short nodeType)
   throws IOException
   {
     super(nodeType);

     _structItemCount = dataInputStream.readInt();

     int offset;
     if ((offset = dataInputStream.readInt()) != 0)
     {
         _structName = (new EStdString(packetBuffer,
                                       new OffsetDataInputStream(packetBuffer,
                                                                 offset)));
     }

     if ((offset = dataInputStream.readInt()) != 0)
     {
         _structType = (new EStdString(packetBuffer,
                                       new OffsetDataInputStream(packetBuffer,
                                                                 offset)));
     }
   }

   /**
    * Return "fixed" length
    */
   protected int fixedLen() {
      return _fixed_length;
   }

   /**
    * Get the name of the structure
    */
   public String getName()
   {
     if (_structName != null)
       return _structName.string();
     else
       return null;
   }

   /**
    * Get the type of the structure
    */
   public String getType()
   {
     if (_structType != null)
       return _structType.string();
     else
       return null;
   }

   /**
    * Get the number of members in the structre
    */
   public int getItemCount()
   {
     return _structItemCount;
   }

   // data fields
   private int _structItemCount;
   private EStdString _structName;
   private EStdString _structType;

   private static final int _fixed_length = 14;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";
}

