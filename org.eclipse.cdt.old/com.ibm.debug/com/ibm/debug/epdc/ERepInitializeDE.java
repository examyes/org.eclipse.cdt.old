package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/ERepInitializeDE.java, java-epdc, eclipse-dev, 20011128
// Version 1.25.1.2 (last modified 11/28/01 16:23:37)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;
//
//
// EPDC_RepInitializeDE
//
//
public class ERepInitializeDE extends EPDC_Reply {
 /**
  * decode a reply from a buffer
  *
  */
  ERepInitializeDE( byte[] packetBuffer, DataInputStream dataInputStream )
  throws IOException
  {
    super(packetBuffer, dataInputStream);
    dataInputStream.readInt(); // reserved
    _engineID = dataInputStream.readShort(); // DE id
    _platformID = dataInputStream.readShort(); // DE platform id
    _defaultSettings = dataInputStream.readInt(); // Default Settings
    dataInputStream.readInt(); // PM Debugging Action
    dataInputStream.readInt(); // PM Debugging Colour
    dataInputStream.readInt(); // PM Debugging Mode
    dataInputStream.readInt(); // Process detach action

    short numberOfViews = dataInputStream.readShort(); // Number of views

    short numRepTypes = dataInputStream.readShort();   // Number of rep. types

    int numLangs = dataInputStream.readInt();          // Number of languages

    int numExceptions = dataInputStream.readInt();     // Number of exceptions
    dataInputStream.readInt(); // Number of settings
    dataInputStream.readInt(); // Number of file paths

    int offset;
    int i;

    if ((offset = dataInputStream.readInt()) != 0) // Offset to array of view info
    {
       _viewInformation = new ERepGetViews[numberOfViews];

       DataInputStream viewDataInputStream = new OffsetDataInputStream (packetBuffer, offset);

       for (i = 0; i < numberOfViews; i++)
            _viewInformation[i] = new ERepGetViews(packetBuffer, viewDataInputStream);
    }

    // offset to representation names
    if ((offset = dataInputStream.readInt()) != 0)
    {
        _repName = new EStdString[numRepTypes];
        DataInputStream nameDataInputStream = new
                                             OffsetDataInputStream(packetBuffer,
                                                                   offset);
        for (i = 0; i < numRepTypes; i++)
        {
             _repName[i] = new EStdString(packetBuffer,
                                           nameDataInputStream);
        }
    }

    // offset to ERepGetLanguages
    if ((offset = dataInputStream.readInt()) != 0)
    {
        _languageInfo = new ERepGetLanguages[numLangs];

        DataInputStream langDataInputStream = new
                                             OffsetDataInputStream(packetBuffer,
                                                                   offset);
        for (i = 0; i < numLangs; i++)
        {
             _languageInfo[i] = new ERepGetLanguages(packetBuffer,
                                                     langDataInputStream);
        }
    }

    // offset to ERepGetExceptions
    if ((offset = dataInputStream.readInt()) != 0)
    {
        _exceptionInfo = new ERepGetExceptions[numExceptions];

        DataInputStream excpDataInputStream = new
                                             OffsetDataInputStream(packetBuffer,
                                                                   offset);
        for (i = 0; i < numExceptions; i++)
        {
             _exceptionInfo[i] = new ERepGetExceptions(packetBuffer,
                                                     excpDataInputStream);
        }
    }

  }

  /**
   * Create new ERepInitialzeDE object.  NOTE:  This constructor
   * automatically adds an FCT change packet, so *DO NOT* add it separately.
   *
   */
   public ERepInitializeDE(EPDC_EngineSession engineSession)
   {
     super();

     setReplyCode(EPDC.Remote_Initialize_Debug_Engine);
     _engineSession = engineSession;
     _repName = new EStdString[0];
     addFCTChangePacket(new ERepGetFCT(engineSession._functCustomTable));
   }

   public void setRepNames(String[] repNames)
   {
      _repName = new EStdString[repNames.length];

      for (int i =0; i<_repName.length;i++)
      {
         _repName[i] = new EStdString(repNames[i]);
      }
   }

   public short numberOfViews()
   {
     return (short)((_viewInformation == null) ? 0 : _viewInformation.length);
   }

   public ERepGetViews[] viewInformation()
   {
      return _viewInformation;
   }

   /**
    * Return the size of the array of representation names
    */
   public short numberOfRepNames()
   {
     return (short)((_repName == null) ? 0 : _repName.length);
   }

   /**
    * Return the array of representation names
    */
   public EStdString[] repNames()
   {
     return _repName;
   }

   /**
    * Return the number of languages this engine supports
    */
   public int numberOfLanguages()
   {
     return ((_languageInfo == null) ? 0 : _languageInfo.length);
   }

   /**
    * Return array of language information (includes language id and name)
    */
   public ERepGetLanguages[] languageInfo()
   {
     return _languageInfo;
   }

   /**
    * Return the number of exceptions this engine supports
    */
   public int numberOfExceptions()
   {
     return ((_exceptionInfo == null) ? 0 : _exceptionInfo.length);
   }

   /**
    * Return array of exception information (includes exception status and name)
    */
   public ERepGetExceptions[] exceptionInfo()
   {
     return _exceptionInfo;
   }

   /** Output class to data streams according to EPDC protocol.
    * Because ERepInitalizeDE includes offsets to structures which
    * contain offsets, I had to be a bit sneaky in what goes to
    * the fixedData stream, and what goes to the varData stream.
    * @exception IOException if an I/O error occurs
    * @exception BadEPDCCommandException if the EPDC command
    * is structured incorrectly
    */
   protected int toDataStreams(DataOutputStream fixedData,
                               DataOutputStream varData, int baseOffset)
   throws IOException, BadEPDCCommandException
   {
     int offset1, offset2;

     // create two levels of variable streams
     ByteArrayOutputStream varBos1, varBos2;
     varBos1 = new ByteArrayOutputStream();
     varBos2 = new ByteArrayOutputStream();

     DataOutputStream varDos1 = new DataOutputStream(varBos1);
     DataOutputStream varDos2 = new DataOutputStream(varBos2);

     super.toDataStreams(fixedData, varDos1, baseOffset);      // output header

     offset1 = baseOffset + super.varLen();
     // set offset of 2nd level variable stream
     offset2 = offset1 +
          _engineSession._viewInfo.length * ERepGetViews._fixedLen() +
          _engineSession._languageInfo.length * ERepGetLanguages._fixedLen() +
          _engineSession._exceptionsInfo.length * ERepGetExceptions._fixedLen();

     writeInt(fixedData, 0); // reserved
     writeShort(fixedData, _engineSession._debugEngineID);
     writeShort(fixedData, _engineSession._debugEnginePlatformID);
     writeInt(fixedData, _engineSession._defaultSettings);
     writeInt(fixedData, _engineSession._PMDebuggingAction);
     writeInt(fixedData, _engineSession._PMDebuggingColor);
     writeInt(fixedData, _engineSession._PMDebuggingMode);
     writeInt(fixedData, _engineSession._processDetachAction);
     writeShort(fixedData, (short) _engineSession._viewInfo.length);
     writeShort(fixedData, (short) _repName.length);    // _NumRepTypes
     writeInt(fixedData, _engineSession._languageInfo.length);
     writeInt(fixedData,  _engineSession._exceptionsInfo.length);    // _NumExceptions
     writeInt(fixedData, 0);     // _NumSettings
     writeInt(fixedData, 0);     // _NumFilePaths

     if (_engineSession._viewInfo.length > 0)
     {
         writeOffset(fixedData, offset1);
         for (int i=0; i<_engineSession._viewInfo.length; i++)
         {   // write out views
              _engineSession._viewInfo[i].toDataStreams(varDos1, varDos2, offset2);
              offset1 += _engineSession._viewInfo[i].fixedLen();
            offset2 += _engineSession._viewInfo[i].varLen();
         }
      }
      else
         writeOffset(fixedData, 0);


      if (_repName.length > 0)
      {    // write out representations
          writeOffset(fixedData, offset2);
          for (int i=0; i<_repName.length; i++)
          {
            _repName[i].output(varDos2);
            offset2 += totalBytes(_repName[i]);
          }
      }
      else
         writeOffset(fixedData, 0);

      if (_engineSession._languageInfo.length > 0)
      {
          writeOffset(fixedData, offset1);
          for (int i=0; i<_engineSession._languageInfo.length; i++)
          {   // write out languages
              _engineSession._languageInfo[i].toDataStreams(varDos1, varDos2, offset2);
              offset1 += _engineSession._languageInfo[i].fixedLen();
              offset2 += _engineSession._languageInfo[i].varLen();
          }
      }
      else
         writeOffset(fixedData, 0);

      if (_engineSession._exceptionsInfo.length > 0)
      {
          writeOffset(fixedData, offset1);
          for (int i=0; i<_engineSession._exceptionsInfo.length; i++)
          {    // write out exceptions
              _engineSession._exceptionsInfo[i].toDataStreams(varDos1, varDos2, offset2);
              offset1 += _engineSession._exceptionsInfo[i].fixedLen();
              offset2 += _engineSession._exceptionsInfo[i].varLen();
          }
      }
      else
         writeOffset(fixedData, 0);


      writeOffset(fixedData, 0);    // Settings Info
      writeOffset(fixedData, 0);    // File Path Info

      EStdString helpFileName = new EStdString(_engineSession._helpFileName);
      offset2 += writeOffsetOrZero(fixedData, offset2, helpFileName);

      if (helpFileName != null)
         helpFileName.output(varDos2);

      EStdString tutorialFileName = new EStdString(_engineSession._tutorialFileName);
      offset2 += writeOffsetOrZero(fixedData, offset2, tutorialFileName);

      if (tutorialFileName != null)
         tutorialFileName.output(varDos2);

      // Now write the two variable streams to the one we were given
      varData.write(varBos1.toByteArray());
      varData.write(varBos2.toByteArray());

      return fixedLen() + varLen();
   }

   public void write(PrintWriter printWriter) {
      super.write(printWriter);
      increaseIndentLevel();
      if (getDetailLevel() >= DETAIL_LEVEL_MEDIUM) {
	 printWriter.println();
         indent(printWriter);
         printWriter.println("Back End Engine Type: " + engineID() );
         indent(printWriter);
         printWriter.println("Back End Platform: " + platformID () );
         indent(printWriter);
         printWriter.println("Default settings: ");
         printWriter.println( defaultSettingsString() );
         indent(printWriter);
         printWriter.println("Number of views: " + numberOfViews() );
         indent(printWriter);
         printWriter.println("Number of representations: " + numberOfRepNames() );
         indent(printWriter);
         printWriter.println("Number of languages: " + numberOfLanguages() );
         indent(printWriter);
         printWriter.println("Number of exceptions: " + numberOfExceptions() );
      }
      if (getDetailLevel() >= DETAIL_LEVEL_HIGH) {
         increaseIndentLevel();
         for (int i=0; i < numberOfViews(); i++) {
            printWriter.println();
            _viewInformation[i].write(printWriter);
         }
         printWriter.println();
         for (int i=0; i < numberOfRepNames(); i++) {
            indent(printWriter);
            printWriter.println("Rep name " + i + ": " + _repName[i].string());
         }
         printWriter.println();
         for (int i=0; i < numberOfLanguages(); i++) {
            _languageInfo[i].write(printWriter);
         }
         printWriter.println();
         for (int i=0; i < numberOfExceptions(); i++)
            _exceptionInfo[i].write(printWriter);

         decreaseIndentLevel();
      }
      decreaseIndentLevel();
   }

   public int fixedLen() {
      return _fixed_length + super.fixedLen();
   }

   public int varLen() {
      int total = 0;
      total = super.varLen();

      // add view size
      for (int i=0; i<_engineSession._viewInfo.length; i++)
           total += _engineSession._viewInfo[i].fixedLen() +
                    _engineSession._viewInfo[i].varLen();
      // add representation name size
      for (int i=0; i<_repName.length; i++)
           total += totalBytes(_repName[i]);

      // add language size
      for (int i=0; i<_engineSession._languageInfo.length; i++)
           total += _engineSession._languageInfo[i].fixedLen() +
                    _engineSession._languageInfo[i].varLen();

      // add exception list size
      for (int i=0; i<_engineSession._exceptionsInfo.length; i++)
           total += _engineSession._exceptionsInfo[i].fixedLen() +
                    _engineSession._exceptionsInfo[i].varLen();

      total += totalBytes(new EStdString(_engineSession._helpFileName));
      total += totalBytes(new EStdString(_engineSession._tutorialFileName));

      return total;
   }

   public short getEngineID()
   {
     return _engineID;
   }

   /**
    * returns a string representing the engine type
    */
   public String engineID()
   {
      switch ( getEngineID() )
      {
          case EPDC.BE_TYPE_IPMD:
             return "IPMD";
          case EPDC.BE_TYPE_SLD:
             return "SLD";
          case EPDC.BE_TYPE_DBX:
             return "DBX";
          case EPDC.BE_TYPE_PICL:
             return "PICL";
          case EPDC.BE_TYPE_CEL:
              return "CEL";
          case EPDC.BE_TYPE_WILEY:
              return "WILEY";
          case EPDC.BE_TYPE_JAVA_PICL:
              return "JAVA_PICL";
          case EPDC.LAST_BE_TYPE:
              return "UNKNOWN";
          default:
              return "UNKNOWN";
      }
   }

   public short getPlatformID()
   {
      return _platformID;
   }

   /**
    * returns a string representing the engine type
    */
   public   String platformID()
   {
      switch ( getPlatformID() )
      {
          case EPDC.PLATFORM_ID_OS2:
             return "OS2";
          case EPDC.PLATFORM_ID_MVS:
             return "MVS";
          case EPDC.PLATFORM_ID_VM370:
             return "VM370";
          case EPDC.PLATFORM_ID_AS400:
             return "AS400";
          case EPDC.PLATFORM_ID_AIX:
             return "AIX";
          case EPDC.PLATFORM_ID_NT:
             return "NT";
          case EPDC.PLATFORM_ID_JVM:
             return "JVM";
          case EPDC.PLATFORM_ID_HPUX:
             return "HPUX";
          case EPDC.PLATFORM_ID_SUN:
             return "SUN";
          default:
   	     return "UNKNOWN";
      }
   }

   public int getDefaultSettings()
   {
     return _defaultSettings;
   }

   public boolean isStgUsgChkEnabled() {
      return ( (_defaultSettings & EPDC.StorageUsageCheckEnable) != 0 ); }
   public boolean isDebuggerBsyBoxEnabled() {
      return ( (_defaultSettings & EPDC.DebuggerBusyBoxEnable) != 0 ); }
   public boolean isAutoSetEntryBkpEnabled() {
      return ( (_defaultSettings & EPDC.AutoSetEntryBkpEnable) != 0 ); }
   public boolean isRunMinEnabled() {
      return ( (_defaultSettings & EPDC.RunMinimizedEnable) != 0 ); }
   public boolean isDateBkpEnabled() {
      return ( (_defaultSettings & EPDC.DateBkpEnable) != 0 ); }


   /*
    *returns a string describing the default settings
    */
   public String defaultSettingsString()
   {
      StringBuffer returnStringBuf = new StringBuffer();
      String indentSpaces = getIndentString(INDENT_INCREASE_FOR_LISTS);

      if ( isStgUsgChkEnabled() )
         returnStringBuf.append(indentSpaces + "Storage Usage Check:        Enabled\n");
      else
         returnStringBuf.append(indentSpaces + "Storage Usage Check:        Disabled\n");
      if ( isDebuggerBsyBoxEnabled() )
         returnStringBuf.append(indentSpaces + "Debugger Busy Box:          Enabled\n");
      else
         returnStringBuf.append(indentSpaces + "Debugger Busy Box:          Disabled\n");
      if ( isAutoSetEntryBkpEnabled() )
         returnStringBuf.append(indentSpaces + "Auto Set Entry Breakpoints: Enabled\n");
      else
         returnStringBuf.append(indentSpaces + "Auto Set Entry Breakpoints: Disabled\n");
      if ( isRunMinEnabled() )
         returnStringBuf.append(indentSpaces + "Run Minimized:              Enabled\n");
      else
         returnStringBuf.append(indentSpaces + "Run Minimized:              Disabled\n");
      if ( isDateBkpEnabled() )
         returnStringBuf.append(indentSpaces + "Date Breakpoint:            Enabled\n");
      else
         returnStringBuf.append(indentSpaces + "Date Breakpoint:            Disabled\n");

      return returnStringBuf.toString();
   }



   private EPDC_EngineSession _engineSession;
   private short _engineID;
   private short _platformID;
   private int _defaultSettings;
   private ERepGetViews[] _viewInformation;
   private EStdString[] _repName;
   private ERepGetLanguages[] _languageInfo;
   private ERepGetExceptions[] _exceptionInfo;

   private static int _fixed_length = 80;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}