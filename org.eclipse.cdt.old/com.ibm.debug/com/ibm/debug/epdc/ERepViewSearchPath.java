package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/ERepViewSearchPath.java, java-epdc, eclipse-dev, 20011128
// Version 1.3.1.2 (last modified 11/28/01 16:25:54)
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
 * ERepViewSearchPath EPDC structure.
 */
public class ERepViewSearchPath extends EPDC_Reply
{
   public ERepViewSearchPath()
   {
      super(EPDC.Remote_ViewSearchPath);

      _numFilePaths = 0;
      _filePaths    = new Vector();
   }

   //Decode reply
   public ERepViewSearchPath(byte[] packetBuffer, DataInputStream dataInputStream)
   throws IOException
   {
     super( packetBuffer, dataInputStream );
     _numFilePaths = dataInputStream.readInt();

     if (_numFilePaths > 0)
     {
         _filePaths = new Vector(_numFilePaths);

         int offset;
         if ((offset = dataInputStream.readInt()) != 0)
         {
             EStdString _filePathName = new EStdString(packetBuffer,
                             new OffsetDataInputStream(packetBuffer, offset) );

             _filePaths.addElement(_filePathName);
         }
     }
   }

   public void addFilePath(String filePath)
   {
      _filePaths.addElement(new EStdString(filePath));
      _numFilePaths = _filePaths.size();
   }

   public int numFilePaths()
   {
     return _numFilePaths;
   }

   public String[] filePaths()
   {
     if (_filePaths == null || _filePaths.size() == 0)
         return null;

     String[] filePaths = new String[_filePaths.size()];

     for (int i=0; i<_filePaths.size(); i++)
     {
          EStdString path = (EStdString) _filePaths.elementAt(i);
          filePaths[i] = path.string();
     }
     return filePaths;
   }

   /**
    * Return fixed component size
    */
   protected int fixedLen() {
      return super.fixedLen() + _fixed_length;
   }

   /**
    * Return variable component size
    */
   protected int varLen()
   {
      int total = super.varLen();

      if (_filePaths.size() > 0)
      {
         total += 4 * _filePaths.size();
         for (int i=0;i <_filePaths.size(); i++)
         {
            EStdString path = (EStdString) _filePaths.elementAt(i);
            total += totalBytes(path);
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

      ByteArrayOutputStream varBos2 = new ByteArrayOutputStream();
      DataOutputStream varDos2 = new DataOutputStream(varBos2);

      int total = super.toDataStreams(fixedData, varData, baseOffset);

      writeInt(fixedData, _numFilePaths);

      if (_filePaths.size() > 0)
      {
         int offset = baseOffset + 4 * _filePaths.size();
         for (int i=0;i<_filePaths.size();i++)
         {
            writeOffset(fixedData, offset);
            EStdString path = (EStdString) _filePaths.elementAt(i);
            path.output(varData);
            offset += totalBytes(path);
         }
         varData.write(varBos2.toByteArray());  // merge data streams
      }

      return total + fixedLen() + varLen();
   }

   // data fields
   private int _numFilePaths;
   private Vector _filePaths;

   private static final int _fixed_length = 4;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}
