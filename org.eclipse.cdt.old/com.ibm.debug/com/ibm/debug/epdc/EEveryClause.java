package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EEveryClause.java, java-epdc, eclipse-dev, 20011128
// Version 1.5.1.2 (last modified 11/28/01 16:23:03)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class EEveryClause extends EPDC_Base
{
   public EEveryClause(int every, int to, int from)
   {
     _every = every;
     _to = to;
     _from = from;
   }

   EEveryClause(byte[] packetBuffer, DataInputStream dataInputStream)
   throws IOException
   {
     _every = dataInputStream.readInt();
     _to = dataInputStream.readInt();
     _from = dataInputStream.readInt();
   }

   public int everyVal()
   {
     return _every;
   }

   public int toVal()
   {
     return _to;
   }

   public int fromVal()
   {
     return _from;
   }

   void output(DataOutputStream dataOutputStream)
   throws IOException
   {
     dataOutputStream.writeInt(_every);
     dataOutputStream.writeInt(_to);
     dataOutputStream.writeInt(_from);
   }

   protected int fixedLen()
   {
     return _fixed_length;
   }

   private int _every;
   private int _to;
   private int _from;
   private static final int _fixed_length = 12;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}
