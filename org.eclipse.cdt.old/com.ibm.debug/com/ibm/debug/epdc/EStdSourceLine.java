package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EStdSourceLine.java, java-epdc, eclipse-dev, 20011129
// Version 1.13.1.3 (last modified 11/29/01 14:15:31)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;

/** EStdSourceLine EPDC structure */
public class EStdSourceLine extends EPDC_Base {

   /**
    * EStdSourceLine constructor
    * @param SourceLine the source line text, including prefix
    * @param Executable whether this line is executable
    */
   EStdSourceLine(String SourceLine, boolean Executable, boolean local) {
      _sourceLine = new EStdString(SourceLine);
      _executable = Executable;
      _local      = local;

      // **************************************************************
      // !!! NOTE: The following code should be REMOVED when we change
      // sui to use the local source bit instead of the
      // SourceLinePlaceholder character.  All Changes to support the
      // local source bit are contained to this file.  Look for 'FIX'
      // BEGIN FIX
      if (_local)
      {
         try
         {
            // We replace the last byte with the placeholder.
            byte[] b1 = SourceLine.getBytes(EStdString.getEncoding());
            if (b1.length > 0)
            {
               b1[b1.length-1] = EPDC.SourceLinePlaceHolder;
            }
            else
            {
               // This should never happen...but
               b1 = new byte[1];
               b1[0] = EPDC.SourceLinePlaceHolder;
            }
            _sourceLine = new EStdString(b1);
         }
         catch (UnsupportedEncodingException uee)
         {
         }
      }
      // END FIX
      // **************************************************************

   }

   EStdSourceLine( byte[] packetBuffer, DataInputStream dataInputStream)
   throws IOException
   {
      int flags = dataInputStream.readByte();
      _executable = ((flags & EPDC.SourceLineExecutable) != 0) ?  true : false;
      _local = ((flags & EPDC.SourceLineGetLocal) != 0) ? true : false;

      int offset;

      if ((offset = dataInputStream.readInt()) != 0)
         _sourceLine =   new EStdString
                             (
                               packetBuffer,
                               new OffsetDataInputStream(packetBuffer, offset)
                             );
   }

   /**
    * Returns a string containing the line of source corresponding to this
    * EStdSourceLine object.
    */
   public String lineText()
   {
     if (_sourceLine != null)
       return _sourceLine.string();
     else
       return null;
   }

   /**
    * Update the source line text with line the line information from
    * the local source file. The new text will consist of the prefix
    * information provided by the engine plus the text of the line in the
    * local source file.
    * @param text The text of the line in the local source file
    * @param prefixLength The length of the prefix string that the
    * engine has provided
    */
   public void setLineTextWithPrefix(String text, int prefixLength)
   {
     String prefix = _sourceLine.string().substring(0, prefixLength);
     _sourceLine = new EStdString(prefix+text);
   }

   /**
    * Returns whether this line of source is executable.
    */
   public boolean isExecutable()
   {
     return _executable;
   }

   /**
    * Returns whether this line of source is local (UI side).
    */
   public boolean isLocal()
   {
     return _local;
   }

   /**
    * This function is used by ERepPartGet so that it does not have to call the instance method
    * fixedLen() for each source line.
    */
   protected static int StaticfixedLen() {
      return _fixed_length;
   }

   protected int fixedLen() {
      return _fixed_length;
   }

   protected int varLen() {
      return super.varLen() + totalBytes(_sourceLine);
   }

   /**
    * Write this structure out to data streams
    * @exception IOException if an I/O error occurs
    * @exception BadEPDCCommandException if the EPDC Command is not properly formed
    */
   protected int toDataStreams(DataOutputStream fixedData, DataOutputStream varData,
         int baseOffset) throws IOException, BadEPDCCommandException
   {
      int flags = 0;

      flags  = (_executable ? EPDC.SourceLineExecutable : 0);
      flags |= (_local ? EPDC.SourceLineGetLocal : 0);

      writeChar(fixedData, (byte) flags);
      writeOffsetOrZero(fixedData, baseOffset, _sourceLine);

      if (_sourceLine != null)
         _sourceLine.output(varData);

      return fixedLen() + varLen();
   }

   // Data fields
   private EStdString _sourceLine;
   private boolean _executable;
   private boolean _local;

   private static final int _fixed_length = 5;
}
