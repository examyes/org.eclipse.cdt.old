package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EExtString.java, java-epdc, eclipse-dev, 20011128
// Version 1.1.1.2 (last modified 11/28/01 16:26:12)
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
 * All text transported via EPDC must be in the form of an EExtString.
 * The EExtString class is used to convert Java String objects into
 * EPDC EExtStrings and vice versa.  EExtStrings are encoded
 * using the current encoding scheme as defined by the
 * method getEncoding().
 * <P>
 * Please note: Modifying the default encoding scheme via setEncoding()
 * will modify encoding/decoding for ALL EExtStrings.
 */

// NOTE: Please ensure that all conversion and sizing decisions are handled
// inside this class for maintainability.

// This class is public so that users of epdc can set the encoding scheme.

public class EExtString extends EPDC_Base
{

  EExtString(byte[] byteArray)
  {
     _len = byteArray.length;
     _string = byteArray;
  }

  EExtString(byte[] packetBuffer, DataInputStream dataInputStream)
     throws IOException
  {
     this(dataInputStream);
  }

  EExtString(DataInputStream dataInputStream)
     throws IOException
  {
     _len = dataInputStream.readInt();
     _string = new byte[_len];
     dataInputStream.read((byte[])_string);
  }

  int actual_read()
  {
    return fixedLen() + _len;
  }

  /**
   * Returns the byte array this string would occupy in the current encoding
   * scheme.
   */
  public byte[] getBytes()
  {
     if (_string == null )
         return null;

     return (byte[])_string;
  }

  /**
   * Write a String to an output stream as an EExtString according to the
   * current encoding scheme.  The current encoding scheme may be queried via
   * getEncoding().
   * @exception IOException if an I/O error occurs
   */
  void output(DataOutputStream dataOutputStream)
     throws IOException
  {
     if (stringIsEmpty())
        return;

     byte[] b = getBytes();
     dataOutputStream.writeInt(b.length);

     dataOutputStream.write(b);
  }

  private boolean stringIsEmpty()
  {
    return _string == null ||
           _string instanceof byte[] && ((byte[])_string).length == 0;
  }

  /**
   * Returns the String object this EExtString contains
   */
   public String string()
   {
     try
     {
       String encoding = getEncoding();

       if (encoding != null)
           _string = new String((byte[])_string, getEncoding());
       else
           _string = new String((byte[])_string);
     }
     catch (UnsupportedEncodingException e)
     {
       return null;
     }

     return (String)_string;
   }

  /**
   * Returns the fixed length of this EExtString
   */
   protected int fixedLen()
   {
      return 4;
   }

  /**
   * Returns the number of bytes (_not_ the number of _characters) the
   * specified string would occupy if it were written to a byte stream as an
   * EExtString in the current encoding scheme.  The current encoding scheme
   * may be queried via getEncoding().
   */
   protected int varLen()
   {
      if (stringIsEmpty())
         return 0;

      return getBytes().length;
   }

  /**
   * Sets the encoding scheme used to convert characters to bytes when
   * writing EExtStrings to a DataOutputStream and converting bytes to
   * characters when creating EExtStrings from a DataInputStream.
   */
  public static void setEncoding(String encoding)
  {
     _encoding = encoding;
  }

  /**
   * Returns the encoding scheme used to convert characters to bytes when
   * writing EExtStrings to a DataOutputStream and converting bytes to
   * characters when creating EExtStrings from a DataInputStream.
   */
  public static String getEncoding()
  {
     return _encoding;
  }

  /**
   * Returns the total number of bytes this EStdstring will occupy
   */
  int totalBytes()
  {
     // NOTE:  We _MUST_ override EPDC_Base totalBytes because we need to
     // detect if our string is empty or not.  We don't output offsets for
     // empty strings.
     if (stringIsEmpty())
        return 0;

     return super.totalBytes();
  }

  public int streamLength()
  {
    return _len;
  }

  private int    _len = 0;
  private Object _string;

  // MUST be static!!!  We default to ibm-850 but this will be set correctly
  // after EReqInitialize is processed.
  private static String _encoding = "ibm-850";
}
