package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EStdString.java, java-epdc, eclipse-dev, 20011128
// Version 1.7.2.2 (last modified 11/28/01 16:24:41)
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
 * All text transported via EPDC must be in the form of an EStdString.
 * The EStdString class is used to convert Java String objects into
 * EPDC EStdStrings and vice versa.  EStdStrings are encoded
 * using the current encoding scheme as defined by the
 * method getEncoding().
 * <P>
 * Please note: Modifying the default encoding scheme via setEncoding()
 * will modify encoding/decoding for ALL EStdStrings.
 */

// NOTE: Please ensure that all conversion and sizing decisions are handled
// inside this class for maintainability.

// This class is public so that users of epdc can set the encoding scheme.

public class EStdString extends EPDC_Base
{
 /**
  * Construct a new EStdString object from the specified String
  */
  EStdString(String string)
  {
     _string = string;
  }

  /**
   * Constructs an EPDC StdString that is a length prefixed string
   * from the specified DataInputStream and returns a String according to the
   * current encoding scheme. The current encoding scheme may be queried via
   * getEncoding().
   * <p> *note* this assumes that the current position in the buffer is
   *        at the StdString
   * @exception IOException if an I/O error occurs
   */
  EStdString(byte[] packetBuffer, DataInputStream dataInputStream)
     throws IOException
  {
     this(dataInputStream);
  }


  EStdString(byte[] byteArray)
  {
     _len = byteArray.length;
     _string = byteArray;
  }

  /**
   * Likely the fastest ctor.
   */

  EStdString(byte[] packetBuffer, int offset)
     throws IOException
  {
     // We need to make sure that this ctor can handle strings longer than
     // 128 characters. Therefore, we cannot calculate the length of the
     // of the string by left shifting of the high order bit and adding the
     // low order bit as was done before:
     // _len = (packetBuffer[offset] << 8) + packetBuffer[offset + 1];
     // However, we might run into problems if the string is longer than
     // 32000 (and a bit more) which is the maximum size of a short.

     DataInputStream dataInputStream = new OffsetDataInputStream(packetBuffer, offset);
     _len = dataInputStream.readShort();
     _string = new byte[_len];
     System.arraycopy(packetBuffer,  // copy from this array
                      offset + 2,    // starting at this position
                      _string,       // copy into this array
                      0,             // starting at this position
                      _len);         // copy this many bytes
  }

  /**
   * Constructs an EPDC StdString that is a length prefixed string
   * from the specified DataInputStream.
   * <p> *note* this assumes that the current position in the buffer is
   *        at the StdString
   * @exception IOException if an I/O error occurs
   */
  EStdString(DataInputStream dataInputStream)
     throws IOException
  {
     _len = dataInputStream.readShort();
     _string = new byte[_len];
     dataInputStream.read((byte[])_string);
  }

  /**
   * Returns the number of bytes we actually read from the data input stream
   * after constructing a new EStdString.  DO NOT use this method to
   * determine the size of an EStdString.  Use fixedLen() and varLen()
   * instead.  This method should only be used to increment the offset
   * within the dataInputStream after a construction of a new EStdString.
   */
  int actual_read()
  {
     return fixedLen() + _len;
  }

  /**
   * Returns the byte array this string would occupy in the current encoding
   * scheme.
   */
  private byte[] getBytes()
  {
     // ------------------------------------------------------------------
     // NOTE: This should be the ONLY place we construct a byte array from
     // a string object!
     // ------------------------------------------------------------------

     // Once we've converted from a String to a byte[], we'll save the byte[]
     // so that it doesn't have to be done again:

     if (_string != null && _string instanceof String)
        try
        {
	  byte[] byteArray;
	  String encoding = getEncoding();

	  if (encoding != null)
	    byteArray = (byte[])(_string = ((String)_string).getBytes(getEncoding()));
	  else
	    byteArray = (byte[])(_string = ((String)_string).getBytes());

	  return byteArray;
        }
        catch (UnsupportedEncodingException e)
        {
           return new byte[0];
        }
     else
        return (byte[])_string;
  }

  /**
   * Write a String to an output stream as an EStdString according to the
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
     dataOutputStream.writeShort(b.length);

     dataOutputStream.write(b);
  }

  private boolean stringIsEmpty()
  {
    return _string == null ||
           _string instanceof String && ((String)_string).length() == 0 ||
           _string instanceof byte[] && ((byte[])_string).length == 0;
  }

  /**
   * Returns the String object this EStdString contains
   */
   public String string()
   {
     // If we have a byte[] but not a String, create a String from the byte[]:

     if (_string != null && !(_string instanceof String))
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
   * Returns the fixed length of this EStdString
   */
   protected int fixedLen()
   {
      return 2;
   }

  /**
   * Returns the number of bytes (_not_ the number of _characters) the
   * specified string would occupy if it were written to a byte stream as an
   * EStdString in the current encoding scheme.  The current encoding scheme
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
   * writing EStdStrings to a DataOutputStream and converting bytes to
   * characters when creating EStdStrings from a DataInputStream.
   */
  public static void setEncoding(String encoding)
  {
     _encoding = encoding;
  }

  /**
   * Returns the encoding scheme used to convert characters to bytes when
   * writing EStdStrings to a DataOutputStream and converting bytes to
   * characters when creating EStdStrings from a DataInputStream.
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

  public void write(java.io.PrintWriter printWriter)
  {
    if (!stringIsEmpty())
       printWriter.print(string());
  }

  private int    _len = 0;
  private Object _string;

  // MUST be static!!!  We default to ibm-850 but this will be set correctly
  // after EReqInitialize is processed.
  private static String _encoding = "ibm-850";
}
