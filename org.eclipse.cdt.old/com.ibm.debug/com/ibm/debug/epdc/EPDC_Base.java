package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EPDC_Base.java, java-epdc, eclipse-dev, 20011128
// Version 1.34.1.2 (last modified 11/28/01 16:23:06)
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
* The base class that all EPDC classes extend
*
*/
abstract public class EPDC_Base implements java.io.Serializable
{
	/**
	* Used to decode an input reply/request
   * @exception IOException if an I/O error occurs
	*/
	protected EPDC_Base( byte[] inBuffer ) throws IOException {
		_in_command = new DataInputStream(new ByteArrayInputStream(inBuffer));
		_length = _in_command.available();
		_offset = 0;
	}

	/**
	* Used to decode an input reply/request, starting at a specific offset
   * @exception IOException if an I/O error occurs
	*/
   protected EPDC_Base(byte[] inBuffer, int offset) throws IOException {
      _in_command = new DataInputStream(new ByteArrayInputStream(inBuffer));
      _length = _in_command.available();
      _offset = 0;

      // Position the buffer at the specified offset and mark it
      posBuffer(offset);
      markOffset();
   }

	/**
	* Used to create a reply/request for output
	*/
	protected EPDC_Base() {
  		_length = 0;
		_offset = 0;
	}

	/**
	* Skips bytes... can be used to skip reserved fields
   * @exception IOException if an I/O error occurs
	*
	*/
	protected void skipBytes( int num ) throws IOException {
		_in_command.skipBytes( num );
	}


	/**
    * Reads in a character (1 byte)
    * @exception IOException if an I/O error occurs
    *
    */
	protected byte readChar() throws IOException {
		_offset += 1;
		return( _in_command.readByte() );
	}

   /**
    * Writes a EPDC Character to the output stream
    * @exception IOException if an I/O error occur
    */
   protected void writeChar(DataOutputStream os, byte b) throws IOException {
      os.writeByte(b);
   }

	/**
    * Reads in a short (2 bytes)
    * @exception IOException if an I/O error occurs
    *
    */
	protected short readShort() throws IOException {
		_offset += 2;
		return( _in_command.readShort() );
	}

	/**
    * Writes a short (2 bytes) to an output stream
    * @exception IOException if an I/O error occurs
    *
    */
	protected void writeShort(DataOutputStream os, short s) throws IOException {
      os.writeShort(s);
	}

	/**
    * Reads in an integer (4 bytes)
    * @exception IOException if an I/O error occurs
    *
    */
	protected int readInt() throws IOException {
		_offset += 4;
		return( _in_command.readInt() );
	}

	/**
    * Writes an int (4 bytes) to an output stream
    * @exception IOException if an I/O error occurs
    *
    */
	protected void writeInt(DataOutputStream os, int d) throws IOException {
      os.writeInt(d);
	}

	/**
    * Reads in a offset (4 bytes)
    * @exception IOException if an I/O error occurs
    *
    */
	protected int readOffset() throws IOException {
		return ( readInt() );
	}

   /**
    * Writes an offset (4 bytes) to an output stream
    * @exception IOException if an I/O error occurs
    */
   protected void writeOffset(DataOutputStream os, int offset) throws IOException {
      os.writeInt(offset);
   }

   /**
    * Reads an EStdString from the current offset.
    * @exception IOException if an I/O error occurs
    */
   protected EStdString readStdString() throws IOException
   {
      EStdString stdString = new EStdString (_in_command);
      _offset += stdString.actual_read();
      return stdString;
   }

   /**
    * Reads an EExtString from the current offset.
    * The difference between EExtString and EStdString is in their size,
    * the former is 4 bytes and the latter is only 2 bytes.
    * @exception IOException if an I/O error occurs
    */
   protected EExtString readExtString()
   throws IOException
   {
     EExtString extString = new EExtString(_in_command);
     _offset += extString.actual_read();

     return extString;
   }

   static int writeOffsetOrZero(DataOutputStream os, int offset, EPDC_Base object)
   throws IOException
   {
     if (object == null)
     {
        os.writeInt(0);
        return 0;
     }
     else
     {
        int total = object.totalBytes();
        if (total == 0)
           os.writeInt(0);
        else
           os.writeInt(offset);
        return total;
     }
   }

	/**
    * Returns the current offset in the buffer
    *
    */
	protected int getOffset() {
		return _offset;
	}

	/**
    * Marks the current position in the buffer
    * This must be used after the fixed portion of the EPDC request/reply
    *   has been read in.  This allows offsets to be processed properly
    * @exception IOException if an I/O error occurs
    */
	protected void markOffset() throws IOException {
		_markedOffset = _offset;
		_in_command.mark( _in_command.available() );
	}

	/**
    * Positions the buffer at the offset passed
    * @exception IOException if an I/O error occurs
    *
    */
	protected void posBuffer( int offset ) throws IOException {
		if (offset == _offset)
			return;
		if (offset > _offset) {
			_in_command.skipBytes( offset - _offset );
         _offset = offset;
			return;
		}
		// we have gone past the required offset
		// go back and reposition
		_in_command.reset();
		_offset = _markedOffset;    // reset the read offset
		_in_command.skipBytes( offset - _offset );
      _offset = offset;
		return;
	}

   /** This function must be defined by all subclasses.  It outputs
    *  the class into two byte streams for fixed and variable data,
    *  corresponding to the EPDC protocol.
    *
    *  @param fixedData output stream for the fixed data
    *  @param varData output stream for the variable data
    *  @param baseOffset the base offset to add to all offsets
    *
    *  @return total size of written data
    *  @exception IOException if an I/O error occurs
    *  @exception BadEPDCCommandException if the EPDC command
    *    is structured incorrectly
    */
   protected int toDataStreams(DataOutputStream fixedData,
         DataOutputStream varData, int baseOffset)
         throws IOException, BadEPDCCommandException
   {
     return 0;
   }

   /** Return the length of the fixed component */
   abstract protected int fixedLen();

      /** Return the length of the variable component */
   protected int varLen()
   {
     return 0;
   }

   int totalBytes()
   {
      return fixedLen() + varLen();
   }

   static int totalBytes(EPDC_Base object)
   {
     return (object == null) ? 0 : object.totalBytes();
   }

   static short getPlatformIdentifier()
   {
     // TODO: Detect other platforms e.g. 390, 400, etc.

     // NOTE: As of 06/24/98, System.getProperty("os.name") was returning the
     // following platform strings:

     // "Windows 95"
     // "Windows NT"
     // "Windows 2000"
     // "AIX"
     // "OS/2"
     // "HP-UX"
     // "Solaris"
     // "SunOS"

     String osName = System.getProperty("os.name");

     if (osName != null)
        if (osName.indexOf("Win") != -1)
           return EPDC.PLATFORM_ID_NT;
        else
        if (osName.indexOf("AIX") != -1)
           return EPDC.PLATFORM_ID_AIX;
        else
        if (osName.indexOf("OS/2") != -1)
           return EPDC.PLATFORM_ID_OS2;
        else
        if (osName.indexOf("HP") != -1)
           return EPDC.PLATFORM_ID_HPUX;
        else
        if (osName.equals("Solaris") || osName.equals("SunOS"))
           return EPDC.PLATFORM_ID_SUN;

     return 0; // unknown
   }

   /**
    * Need to set the EPDC version before each request is written out. This
    * is due to some requests having a different set of parameters based on
    * the EPDC version.
    */
   public void setEPDCEngineSession(EPDC_EngineSession engineSession)
   {
     _engineSession = engineSession;
   }

   /**
    * Check the EPDC version. This is used for a number of requests that have
    * different set of parameters based on the version of EPDC.
    */
   int getEPDCVersion()
   {
     return (_engineSession == null) ? 0 : _engineSession._negotiatedEPDCVersion;
   }

   EPDC_EngineSession getEPDCEngineSession()
   {
     return _engineSession;
   }

	//------------------------------------------------------
	// The following are used to dump out the contents of EPDC
	//------------------------------------------------------

    /**
     * Tell this EPDC object to write a formatted representation of itself
     * out to the specified PrintWriter.
     */

    public void write(PrintWriter printWriter)
    {
      printWriter.println(getName());
    }

   /**
    * Write out a packet to 'printWriter' in hex representation.
    */

   static void write(PrintWriter printWriter, byte[] packet)
   {
     final int bytesPerLine = 16;
     final int hexLength = bytesPerLine * 3 + (bytesPerLine / 4 - 1);
     StringBuffer lineInHex = new StringBuffer(hexLength);
     StringBuffer lineInChar = new StringBuffer(bytesPerLine);
     int offset = 0;

     for (int i = 0; i < packet.length; i++)
     {
         lineInHex.append(getHexDigit((packet[i] & 0x00F0) >> 4)); // Hi-order nibble
         lineInHex.append(getHexDigit(packet[i] & 0x0F)); // Low-order nibble

         lineInHex.append(' ');

         if (packet[i] >= 0x20 && packet[i] <= 0x7E)
            lineInChar.append((char)packet[i]);
         else
            lineInChar.append('.');

         if ((i+1) % bytesPerLine == 0 || i == packet.length - 1)
         {
            while (lineInHex.length() < hexLength)
                  lineInHex.append(' ');

            printWriter.print(getHexDigits(offset) + " :  ");
            printWriter.print(lineInHex.toString());
            printWriter.println("    " + lineInChar.toString());

            lineInHex.setLength(0);
            lineInChar.setLength(0);
            offset += bytesPerLine;
         }
         else
         if ((i+1) % 4 == 0)
            lineInHex.append(' ');
     }
   }

   static String getHexDigits(int offset)
   {
     StringBuffer hexDigits = new StringBuffer(8);
     final int mask = 0x0000000F;

     for (int i = 0; i < 8; i++)
     {
         // Mask off succesive nibbles and get the hex digit for each one:

         hexDigits.append(getHexDigit((offset >>> ((7-i)*4)) & mask));
     }

     return "0x" + hexDigits.toString();
   }

   private static char getHexDigit(int input)
   {
     return _hexDigits[input];
   }

    /**
     * Get the name of this EPDC object. This will be the unqualified name
     * of the object's class.
     */

    public String getName()
    {
      String name = getClass().getName();

      int indexOfLastDot = name.lastIndexOf('.');

      if (indexOfLastDot != -1)
         name = name.substring(indexOfLastDot + 1);

      return name;
    }

    public void setDetailLevel(byte detailLevel)
    {
       _detailLevel = detailLevel;
    }

    protected byte getDetailLevel()
    {
       return _detailLevel;
    }

    void setIndentLevel(int indentLevel)
    {
       _indentLevel = indentLevel;
    }

    int getIndentLevel()
    {
       return _indentLevel;
    }

    void increaseIndentLevel()
    {
       _indentLevel++;
    }

    void decreaseIndentLevel()
    {
       _indentLevel--;
    }

    void indent(PrintWriter printWriter)
    {
      for (int i = 0; i < _indentLevel; i++)
        printWriter.print("  ");
    }

    String getIndentString(int additionalLevels)
    {
      StringBuffer indentSpaces = new StringBuffer();
      int numOfIndentLevels = additionalLevels + getIndentLevel();
      for (int i = 0; i < numOfIndentLevels; i++) {
         indentSpaces.append("  ");
      }
      return indentSpaces.toString();

    }
        /**
         * Return the input stream
         */
        protected DataInputStream getDataInputStream()
        {
          return _in_command;
        }



	protected transient int     _prtOffset; // current print offset from left margin
	protected transient final int  PRTINDENT = 3;

	// internal data members

	private transient int       _offset;  // offset for the next read
	private transient int       _markedOffset;
	private transient DataInputStream	_in_command;  // the input stream
	private transient int       _length;

        private transient EPDC_EngineSession _engineSession;

        public static final byte DETAIL_LEVEL_LOW = 10;
        public static final byte DETAIL_LEVEL_MEDIUM = 20;
        public static final byte DETAIL_LEVEL_HIGH = 30;

        private static byte _detailLevel = DETAIL_LEVEL_HIGH;
        private static int _indentLevel;
        public static final byte INDENT_INCREASE_FOR_LISTS = 3; /*represents number of levels
                             to increase indent when lists are outputted in EPDC viewer*/

     static char[] _hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8',
                                 '9', 'A', 'B', 'C', 'D', 'E', 'F'};



   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";


}


