package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/ERepProcessListGet.java, java-epdc, eclipse-dev, 20011128
// Version 1.5.1.2 (last modified 11/28/01 16:24:55)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;
import java.util.*;

/**
 * ERepProcessListGet EPDC structure.
 */
public class ERepProcessListGet extends EPDC_Reply {

   public ERepProcessListGet(int numColumns) {
      super(EPDC.Remote_ProcessListGet);
      _processes = new Vector();
      _numColumns = numColumns;
   }

   public ERepProcessListGet(byte[] packetBuffer, DataInputStream dataInputStream)
   throws IOException
   {
      super(packetBuffer, dataInputStream);

      int numProcesses = dataInputStream.readInt();
      int numColumns = dataInputStream.readInt();

      _processes = new Vector(numProcesses);

      DataInputStream saveDataInputStream = dataInputStream;

      for (int i = 0; i < numProcesses; i++)
      {
          if (numProcesses > 1)
             dataInputStream = new OffsetDataInputStream(packetBuffer,
                                                         saveDataInputStream.readInt());

          String[] columns = new String[numColumns];

          for (int j = 0; j < numColumns; j++)
              columns[j] = (new EStdString(dataInputStream)).string();

          _processes.addElement(columns);
      }
   }

   public void addProcess(String[] row)
   {
      _processes.addElement(row);
   }

   /**
    * Static function that returns fixed component size
    */
   protected int fixedLen() {
      return super.fixedLen() + _fixed_length;
   }

   /**
    * Return variable component size
    */
   protected int varLen() {
      int total = super.varLen();
      String row[];

      if (_processes.size() > 1)
      {
         total += 4 * _processes.size();
      }

      for (int i=0;i<_processes.size();i++)
      {
         row = (String[]) _processes.elementAt(i);
         for (int j=0;j<row.length;j++)
         {
            total += totalBytes(new EStdString(row[j]));
         }
      }
      return total;
   }


   /** Output class to data streams according to EPDC protocol.
     * @exception IOException if an I/O error occurs
    *  @exception BadEPDCCommandException if the EPDC command
    *    is structured incorrectly
    */
   protected int toDataStreams(DataOutputStream fixedData,
         DataOutputStream varData, int baseOffset)
         throws IOException, BadEPDCCommandException {

      int total = super.toDataStreams(fixedData,varData,baseOffset);
      baseOffset += super.varLen();
      total +=  _fixed_length;

      writeInt(fixedData, _processes.size());
      writeInt(fixedData, _numColumns);

      String[] row;
      int i,j;

      if (_processes.size() == 0)
      {
         // Do nothing if we have nothing to report
      }
      else if (_processes.size() == 1)
      {
         // We only have 1 process so just output the string array as std strngs
         row = (String[]) _processes.elementAt(0);
         for (i=0;i<row.length;i++)
         {
            new EStdString(row[i]).output(varData);
         }
      }
      else
      {
         // We need to write an array of offsets to string lists
         int offset2 = total + 4 * _processes.size();
         int len;
         for (i=0;i<_processes.size();i++)
         {
            writeOffset(fixedData,offset2);
            total += 4;
            row = (String[]) _processes.elementAt(i);
            for (j=0;j<row.length;j++)
            {
               EStdString stdRow = new EStdString(row[j]);
               stdRow.output(varData);
               len = totalBytes(stdRow);
               offset2 += len;
               total += len;
            }
         }
      }

      return total;
   }

   public Vector getProcesses()
   {
     return _processes;
   }

   // data fields
   private Vector _processes;
   private int _numColumns;

   private static final int _fixed_length = 8;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}
