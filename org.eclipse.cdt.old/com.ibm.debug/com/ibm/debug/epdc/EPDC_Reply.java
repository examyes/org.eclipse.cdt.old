package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EPDC_Reply.java, java-epdc, eclipse-dev
// Version 1.74.1.1 (last modified 11/21/01 09:24:15)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.connection.*;
import java.io.*;
import java.util.Vector;

// Reply packet class

public class EPDC_Reply extends  EPDC_Base
{
  protected EPDC_Reply(byte[] packetBuffer, DataInputStream dataInputStream)
  throws IOException
  {
    super();
    _reply_code = dataInputStream.readInt();
    _return_code = dataInputStream.readInt();
    _changed_info = dataInputStream.readInt();

    if ((_message_offset = dataInputStream.readInt()) != 0)
       _message_text = new EStdString
                           (
                             new OffsetDataInputStream (packetBuffer, _message_offset)
                           );

    _PMDebuggingStatus = dataInputStream.readInt();
    _total_bytes = dataInputStream.readInt();
  }

   /**
   * Used to create a reply to be sent
   */
   protected EPDC_Reply() {
      super();
      _return_code = EPDC.ExecRc_OK;  // default to ok... caller should update if it is not ok
      _changed_info = 0;         // set changed info to none by default

      _message_offset = 0;
      _message_text = null;
      _PMDebuggingStatus = EPDC.InputUnlocked;
      _total_bytes = 0;          // to be updated when the complete reply is built
      _PartChange = null;
   }


   /**
   * Used to create a reply to be sent
   */
   protected EPDC_Reply(int reply_code) {
      super();
      _reply_code = reply_code;
      _return_code = EPDC.ExecRc_OK;  // default to ok... caller should update if it is not ok
      _changed_info = 0;         // set changed info to none by default

      _message_offset = 0;
      _message_text = null;
      _PMDebuggingStatus = EPDC.InputUnlocked;
      _total_bytes = 0;          // to be updated when the complete reply is built
      _PartChange = null;
   }

   /**
    * Will decode the reply as well as the change packets associated with that
    * reply.
    */

   static public EPDC_Reply decodeReplyStream(Connection connection,
                                              EPDC_EngineSession engineSession)
   throws IOException
   {
      return decodeReplyStream(connection, engineSession, true);
   }

   /**
    * Reads a reply packet from 'connection' and puts it into 'bos'.
    * @return The reply code.
    */

   private static int getPacket(Connection connection,
                                ByteArrayOutputStream bos)
   throws IOException
   {
      DataOutputStream dos = new DataOutputStream(bos);

      connection.beginPacketRead();
      DataInputStream inStream = new DataInputStream(connection.getInputStream());

	  inStream.readInt();

      int reqCode = inStream.readInt();
      dos.writeInt(reqCode);

      int totBytes;
      byte[] b;

      // Remote_Version is the only EPDC command with a different reply
      // structure since it does not extend from EPDC_Reply.
      if (reqCode == EPDC.Remote_Version)
      {
         totBytes = inStream.readInt();
         dos.writeInt(totBytes);
         dos.writeInt(inStream.readInt());     // return_code
         b = new byte[totBytes - 12];
      }
      else
      {
         dos.writeInt(inStream.readInt() );   // return code
         dos.writeInt(inStream.readInt() );   // changed info bits
         dos.writeInt(inStream.readInt() );   // offset
         dos.writeInt(inStream.readInt() );   // PM Debugging status
         totBytes = inStream.readInt();  // finally, we get to the total number of bytes
         dos.writeInt(totBytes);
         b = new byte[totBytes - 24];
      }

      inStream.readFully(b);
      dos.write(b);
      connection.endPacketRead(EPDC.ReplyPacket);

      return reqCode;
   }

   public String getName()
   {
      switch ( getReplyCode() )
      {
         case EPDC.Remote_BreakpointLocation:
            return "Remote_BreakpointLocation";
         case EPDC.Remote_Execute:
            return "Remote_Execute";
         case EPDC.Remote_Expression:
            return "Remote_Expression";
         case EPDC.Remote_ExpressionDisable:
           return "Remote_ExpressionDisable";
         case EPDC.Remote_ExpressionEnable:
            return "Remote_ExpressionEnable";
         case EPDC.Remote_ExpressionFree:
            return "Remote_ExpressionFree";
         case EPDC.Remote_ExpressionValueModify:
            return "Remote_ExpressionValueModify";
         case EPDC.Remote_PartGet:
            return "Remote_PartGet";
         case EPDC.Remote_PointerDeref:
            return "Remote_PointerDeref";
         case EPDC.Remote_EntrySearch:
            return "Remote_EntrySearch";
         case EPDC.Remote_Registers:
            return "Remote_Registers";
         case EPDC.Remote_RegistersFree:
            return "Remote_RegistersFree";
         case EPDC.Remote_Stack:
            return "Remote_Stack";
         case EPDC.Remote_StackFree:
            return "Remote_StackFree";
         case EPDC.Remote_StackBuildView:
            return "Remote_StackBuildView";
         case EPDC.Remote_FilePathVerify:
            return "Remote_FilePathVerify";
         case EPDC.Remote_PartOpen:
            return "Remote_PartOpen";
         case EPDC.Remote_Storage2:
            return "Remote_Storage2";
         case EPDC.Remote_StorageEnablementSet:
            return "Remote_StorageEnablementSet";
         case EPDC.Remote_StorageFree:
            return "Remote_StorageFree";
         case EPDC.Remote_StorageRangeSet2:
            return "Remote_StorageRangeSet2";
         case EPDC.Remote_StorageStyleSet:
            return "Remote_StorageStyleSet";
         case EPDC.Remote_StorageUpdate:
            return "Remote_StorageUpdate";
         case EPDC.Remote_StringFind:
            return "Remote_StringFindh";
         case EPDC.Remote_TerminatePgm:
            return "Remote_TerminatePgm";
         case EPDC.Remote_ThreadFreeze:
            return "Remote_ThreadFreeze";
         case EPDC.Remote_ThreadThaw:
            return "Remote_ThreadThaw";
         case EPDC.Remote_ViewsVerify:
            return "Remote_ViewsVerify";
         case EPDC.Remote_Initialize_Debug_Engine:
            return "Remote_Initialize_Debug_Engine";
         case EPDC.Remote_PreparePgm:
            return "Remote_PreparePgm";
         case EPDC.Remote_StartPgm:
            return "Remote_StartPgm";
         case EPDC.Remote_BreakpointClear:
            return "Remote_BreakpointClear";
         case EPDC.Remote_BreakpointDisable:
            return "Remote_BreakpointDisable";
         case EPDC.Remote_BreakpointEnable:
            return "Remote_BreakpointEnable";
         case EPDC.Remote_BreakpointEvent:
            return "Remote_BreakpointEvent";
         case EPDC.Remote_PartSet:
            return "Remote_PartSet";
         case EPDC.Remote_ExpressionSubTree:
            return "Remote_ExpressionSubTree";
         case EPDC.Remote_ExpressionSubTreeDelete:
            return "Remote_ExpressionSubTreeDelete";
         case EPDC.Remote_ExpressionRepTypeSet:
            return "Remote_ExpressionRepTypeSet";
         case EPDC.Remote_LocalVariable:
            return "Remote_LocalVariable";
         case EPDC.Remote_LocalVariableFree:
            return "Remote_LocalVariableFree";
         case EPDC.Remote_Terminate_Debug_Engine:
            return "Remote_Terminate_Debug_Engine";
         case EPDC.Remote_EntryWhere:
            return "Remote_EntryWhere";
         case EPDC.Remote_CommandLogExecute:
            return "Remote_CommandLogExecute";
         case EPDC.Remote_PrepareChild:
            return "Remote_PrepareChild";
         case EPDC.Remote_ProcessAttach:
            return "Remote_ProcessAttach";
         case EPDC.Remote_ProcessDetach:
            return "Remote_ProcessDetach";
         case EPDC.Remote_ProcessListGet:
            return "Remote_ProcessListGet";
         case EPDC.Remote_ProcessAttach2:
            return "Remote_ProcessAttach2";
         case EPDC.Remote_ContextConvert:
            return "Remote_ContextConvert";
         case EPDC.Remote_BreakpointEntryAutoSet2:
            return "Remote_BreakpointEntryAutoSet2";
         case EPDC.Remote_ModuleAdd:
            return "Remote_ModuleAdd";
         case EPDC.Remote_ModuleRemove:
            return "Remote_ModuleRemove";
         case EPDC.Remote_Registers2:
            return "Remote_Registers2";
         case EPDC.Remote_RegistersEnablementSet:
            return "Remote_RegistersEnablementSet";
         case EPDC.Remote_RegistersFree2:
            return "Remote_RegistersFree2";
         case EPDC.Remote_RegistersValueSet:
            return "Remote_RegistersValueSet";
         case EPDC.Remote_StackEnablementSet:
            return "Remote_StackEnablementSet";
         case EPDC.Remote_StackOpenStorage:
            return "Remote_StackOpenStorage";
         case EPDC.Remote_StackSetBreakpoint:
            return "Remote_StackSetBreakpoint";
         case EPDC.Remote_PassThru:
            return "Remote_PassThru";
         case EPDC.Remote_PassThruEnablementSet:
            return "Remote_PassThruEnablementSet";
         case EPDC.Remote_PassThruFree:
            return "Remote_PassThruFree";
         case EPDC.Remote_PassThruSendCommand:
            return "Remote_PassThruSendCommand";
         case EPDC.Remote_ThreadInfoGet:
            return "Remote_ThreadInfoGet";
         case EPDC.Remote_Version:
            return "Remote_Version";

         case EPDC.Remote_StackView:
            return "Remote_StackView";
         case EPDC.Remote_Storage:
            return "Remote_Storage";
         case EPDC.Remote_StorageAddressStyleSet:
            return "Remote_StorageAddressStyleSet";
         case EPDC.Remote_StorageDisable:
            return "Remote_StorageDisable";
         case EPDC.Remote_StorageEnable:
            return "Remote_StorageEnable";
         case EPDC.Remote_StorageRangeSet:
            return "Remote_StorageRangeSet";
         case EPDC.Remote_StorageUnitStyleSet:
            return "Remote_StorageUnitStyleSet";

         case EPDC.Remote_DBD_Calls:
            return "Remote_DBD_Calls";
         case EPDC.Remote_ClassDetailsGet:
            return "Remote_ClassDetailsGet";
         case EPDC.Remote_ClassDetailsFree:
            return "Remote_ClassDetailsFree";
         case EPDC.Remote_ClassPartGet:
            return "Remote_ClassPartGet";
         case EPDC.Remote_Halt:
            return "Remote_Halt";
         case EPDC.Remote_CommandLog:
            return "Remote_CommandLog";
         case EPDC.Remote_CommandLogFree:
            return "Remote_CommandLogFree";
         case EPDC.Remote_CommandLogGetText:
            return "Remote_CommandLogGetText";
         case EPDC.Remote_CommandLogSearch:
            return "Remote_CommandLogSearch";
         case EPDC.Remote_TypesNumGet:
            return "Remote_TypesNumGet";
         case EPDC.Remote_RepForTypeSet:
            return "Remote_RepForTypeSet";
         case EPDC.Remote_ContextQualGet:
            return "Remote_ContextQualGet";
         case EPDC.Remote_ContextFromAddrGet:
            return "Remote_ContextFromAddrGet";
         case EPDC.Remote_ExceptionStatusChange:
            return "Remote_ExceptionStatusChange";
         case EPDC.Remote_StorageUsageCheckSet:
            return "Remote_StorageUsageCheckSet";
         case EPDC.Remote_JobsListGet:
            return "Remote_JobsListGet";
         case EPDC.Remote_RegistersDetailsGet:
            return "Remote_RegistersDetailsGet";
         case EPDC.Remote_PMDebuggingModeSet:
            return "Remote_PMDebuggingModeSet";
         case EPDC.Remote_StackDetailsGet:
            return "Remote_StackDetailsGet";
         case EPDC.Remote_ProcessDetailsGet:
            return "Remote_ProcessDetailsGet";
         case EPDC.Remote_PassThruDetailsGet:
            return "Remote_PassThruDetailsGet";
         case EPDC.Remote_BreakpointEntryAutoSet:
            return "Remote_BreakpointEntryAutoSet";
         case EPDC.Remote_EnvironmentDetailsGet:
            return "Remote_EnvironmentDetailsGet";
         case EPDC.Remote_EnvironmentSet:
            return "Remote_EnvironmentSet";
         case EPDC.Remote_FilePathSet:
            return "Remote_FilePathSet";
         case EPDC.Remote_ProcessActiveListGet:
            return "Remote_ProcessActiveListGet";
         case EPDC.Remote_ViewFileInfoSet:
            return "Remote_ViewFileInfoSet";
         case EPDC.Remote_ViewSearchPath:
            return "Remote_ViewSearchPath";
         case EPDC.Remote_GetStatusInfo:
            return "Remote_GetStatusInfo";
         case EPDC.Remote_GetEngineSettings:
            return "Remote_GetEngineSettings";
         case EPDC.Remote_PutEngineSettings:
            return "Remote_PutEngineSettings";
         default:
            return "Unknown";
      }
   }


   /**
    * returns a reply object from a buffer based on the reply type
    *
    */
   static public EPDC_Reply decodeReplyStream(Connection connection,
                                              EPDC_EngineSession engineSession,
                                              boolean decodeChangePackets)
   throws IOException
   {

      ByteArrayOutputStream bos = new ByteArrayOutputStream();

      int reqCode = getPacket(connection, bos);

      EPDC_Reply result = null;

      // Get a byte array which contains the entire packet:

      byte[] packetByteArray = bos.toByteArray();

      DataInputStream dataInputStream = new DataInputStream(new
                                                ByteArrayInputStream(
                                                   packetByteArray));

      switch (reqCode)
      {
         case EPDC.Remote_CommandLogExecute:
              result = new ERepCommandLogExecute(packetByteArray, dataInputStream );
              break;

         case EPDC.Remote_Initialize_Debug_Engine:
              result = new ERepInitializeDE(packetByteArray, dataInputStream);
              break;

         case EPDC.Remote_PreparePgm:
              result = new ERepPreparePgm(packetByteArray, dataInputStream );
              break;

         case EPDC.Remote_StartPgm:
              result = new ERepStartPgm(packetByteArray, dataInputStream );
              break;

         case EPDC.Remote_ProcessAttach:
         case EPDC.Remote_ProcessAttach2:
              result = new ERepProcessAttach(packetByteArray, dataInputStream );
              break;

         case EPDC.Remote_ProcessDetach:
              result = new ERepProcessDetach(packetByteArray, dataInputStream );
              break;

         case EPDC.Remote_PartGet:
              result = new ERepPartGet(packetByteArray, dataInputStream );
              break;

         case EPDC.Remote_PartSet:
              result = new ERepPartSet(packetByteArray, dataInputStream );
              break;

         case EPDC.Remote_Execute:
              result = new ERepExecute(packetByteArray, dataInputStream );
              break;

         case EPDC.Remote_EntrySearch:
              result = new ERepEntrySearch(packetByteArray, dataInputStream );
              break;

         case EPDC.Remote_StackDetailsGet:
              result = new ERepStackDetailsGet(packetByteArray, dataInputStream);
              break;

         case EPDC.Remote_StackBuildView:
              result = new ERepStackBuildView(packetByteArray, dataInputStream);
              break;

         case EPDC.Remote_ContextConvert:
              result = new ERepContextConvert(packetByteArray, dataInputStream);
              break;

         case EPDC.Remote_ContextQualGet:
              result = new ERepContextQualGet(packetByteArray, dataInputStream);
              break;

         case EPDC.Remote_Registers2:
              result = new ERepRegisters2(packetByteArray, dataInputStream);
              break;

         case EPDC.Remote_Storage2:
              result = new ERepStorage2(packetByteArray, dataInputStream);
              break;

         case EPDC.Remote_StorageEnablementSet:
              result = new ERepStorageEnablementSet(packetByteArray, dataInputStream);
              break;

         case EPDC.Remote_StorageStyleSet:
              result = new ERepStorageStyleSet(packetByteArray, dataInputStream);
              break;

         case EPDC.Remote_StorageRangeSet2:
              result = new ERepStorageRangeSet2(packetByteArray, dataInputStream);
              break;

         case EPDC.Remote_StorageFree:
              result = new ERepStorageFree(packetByteArray, dataInputStream);
              break;

         case EPDC.Remote_RegistersFree2:
              result = new ERepRegistersFree2(packetByteArray, dataInputStream);
              break;

         case EPDC.Remote_RegistersDetailsGet:
              result = new ERepRegistersDetailsGet(packetByteArray, dataInputStream);
              break;

         case EPDC.Remote_TypesNumGet:
              result = new ERepTypesNumGet(packetByteArray, dataInputStream);
              break;

         case EPDC.Remote_StringFind:
              result = new ERepStringFind(packetByteArray, dataInputStream);
              break;

         case EPDC.Remote_ExceptionStatusChange:
              result = new ERepExceptionStatusChange();
              break;

         case EPDC.Remote_Version:
              result = new ERepVersion(packetByteArray, dataInputStream);
              break;

         case EPDC.Remote_ProcessDetailsGet:
              result = new ERepProcessDetailsGet(packetByteArray, dataInputStream);
              break;

         case EPDC.Remote_ProcessListGet:
              result = new ERepProcessListGet(packetByteArray, dataInputStream);
              break;

         case EPDC.Remote_PartOpen:
              result = new ERepPartOpen(packetByteArray, dataInputStream);
              break;

         case EPDC.Remote_BreakpointLocation:
              result = new ERepBreakpointLocation(packetByteArray, dataInputStream);
              break;

         case EPDC.Remote_EntryWhere:
              result = new ERepEntryWhere(packetByteArray, dataInputStream, engineSession);
              break;

         case EPDC.Remote_ViewSearchPath:
              result = new ERepViewSearchPath(packetByteArray, dataInputStream);
              break;

         case EPDC.Remote_JobsListGet:
              result = new ERepJobsListGet(packetByteArray, dataInputStream);
              break;

         case EPDC.Remote_GetEngineSettings:
              result = new ERepGetEngineSettings(packetByteArray, dataInputStream);
              break;

         case EPDC.Remote_PutEngineSettings:
              result = new ERepPutEngineSettings(packetByteArray, dataInputStream);
              break;

         default:
              result = new EPDC_Reply(packetByteArray, dataInputStream );
              break;
      }

      if (decodeChangePackets)
      {
         // Decode change packets, if any:

         int numberOfChangePacketsExpected = result.minChangePackets();

         int totalNumberOfChangeItemsOfThisType = 0;

         for (int numberOfChangePacketsRead = 0;
              numberOfChangePacketsRead < numberOfChangePacketsExpected;
              ++numberOfChangePacketsRead)
         {
            EPDC_ChangeInfo changeInfo = EPDC_ChangeInfo.decodeChangeInfoStream(
                                                           connection,
                                                           result,
                                                           engineSession
                                                           );

            totalNumberOfChangeItemsOfThisType += changeInfo.changeItemsInThisPacket();

            if (totalNumberOfChangeItemsOfThisType == changeInfo.totalChangeItemsOfThisType())
               totalNumberOfChangeItemsOfThisType = 0;
            else
               ++numberOfChangePacketsExpected;
         }
      }

      return result;
   }

   /**
    * returns the reply code
    * see ibm.EPDC.EPDC.java for a list of the codes
    */
   public int getReplyCode() {
      return( _reply_code );
   }
   protected void setReplyCode( int rc ) {
      _reply_code = rc;
   }

   /**
    * returns the return code
    *
    */
   public int getReturnCode() {
      return _return_code;
   }

   /** Set the return code */
   public void setReturnCode(int return_code) {
      _return_code = return_code;
   }

   /**
    * returns a string that represents the return code
    *
    */
   public String returnCodeString () {
      switch (_return_code) {
         case EPDC.ExecRc_OK:
            return "ExecRc_OK";
         case EPDC.ExecRc_Error:
            return "ExecRc_Error";
         default:
            return "UNKNOWN";
      }
   }

   /**
    * returns a string listing the changed parts
    *
    */
   public String changeInfoBitString() {
      StringBuffer returnStringBuf = new StringBuffer();
      String indentSpaces = getIndentString(INDENT_INCREASE_FOR_LISTS);

      if ( isPartChgd() )
         returnStringBuf.append(indentSpaces + "Parts Tbl\n");
      if ( isBrkPtChgd() )
         returnStringBuf.append(indentSpaces + "Breakpoint Tbl\n");
      if ( isPgmStateChgd() )
         returnStringBuf.append(indentSpaces + "Program State\n");
      if ( isMonVariableChgd() )
         returnStringBuf.append(indentSpaces + "Monitored Vars\n");
      if ( isThrdStateChgd() )
         returnStringBuf.append(indentSpaces + "ThdState\n");
      if ( isMonStorChgd() )
         returnStringBuf.append(indentSpaces + "Storage\n");
      if ( isMonStackChgd() )
         returnStringBuf.append(indentSpaces + "Stack\n");
      if ( isMonRegsChgd() )
         returnStringBuf.append(indentSpaces + "Registers\n");
      if ( isModuleEntryChgd() )
         returnStringBuf.append(indentSpaces + "Module Entry\n");
      if ( isFCTChgd() )
         returnStringBuf.append(indentSpaces + "FCT\n");
      if (returnStringBuf.length() <= 1)
         returnStringBuf.append(indentSpaces + "None\n");

      return returnStringBuf.toString();
   }

   /**
    * returns a string that represents the PMDebuggingStatus (only relevant
    * for OS/2)
    */
   public String PMDebuggingStatusString () {
      switch (_PMDebuggingStatus) {
         case EPDC.InputUnlocked:
            return "InputUnlocked";
         case EPDC.InputLocked:
            return "InputLocked";
         default:
            return "UNKNOWN";
      }
   }

   /**
   * returns the text of the message if there is any
   * if there is no text then a null string is returned
   */
   public String messageText() {
      // check to see if we have gotten the message text yet
      if ( _message_offset != 0 && _message_text == null ) {
         try {
            posBuffer( _message_offset );
            _message_text = readStdString();
         } catch ( IOException err ) {
            _message_offset = 0;
            _message_text = null;
         }
      }

      if (_message_text != null)
         return _message_text.string();
      else
         return null;
   }

   public void setMessage(String msg) {
      _message_text = new EStdString(msg);
   }

   public boolean isInputLocked() {
      return (_PMDebuggingStatus == EPDC.InputLocked) ? true : false;
   }

   public void setPMDebuggingStatus(int PMDebuggingStatus) {
      _PMDebuggingStatus = PMDebuggingStatus;
   }

   /** Outputs
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
         throws IOException, BadEPDCCommandException {

      writeInt(fixedData, _reply_code);      // write out fixed part
      writeInt(fixedData, _return_code);
      writeInt(fixedData, _changed_info);

      if (_message_text == null)
         writeOffset(fixedData, 0);
      else {
         writeOffset(fixedData, baseOffset);
         _message_text.output(varData);
      }

      writeInt(fixedData, _PMDebuggingStatus);
      writeInt(fixedData, _total_bytes);

      return _fixed_length + totalBytes(_message_text);
   }

   /** Output reply to an EPDC connection */

   public int output(Connection connection) throws IOException, BadEPDCCommandException  {
      _total_bytes = fixedLen() + varLen();

      connection.beginPacketWrite(_total_bytes + 4);
      OutputStream outStream = connection.getOutputStreamBuffer();

      new DataOutputStream(connection.getOutputStreamBuffer()).writeInt(703);

      ByteArrayOutputStream fixedByteStream = new ByteArrayOutputStream();
      ByteArrayOutputStream varByteStream = new ByteArrayOutputStream();

      int tot = toDataStreams(new DataOutputStream(fixedByteStream),
            new DataOutputStream(varByteStream), fixedLen());
      outStream.write(fixedByteStream.toByteArray());
      outStream.write(varByteStream.toByteArray());

      connection.endPacketWrite(EPDC.ReplyPacket);

      /* Output change packets */
      if (isPartChgd())
         _PartChange.output(connection);
      if (isModuleEntryChgd())
         _ModuleChange.output(connection);
      if (isPgmStateChgd())
         _ProgChange.output(connection);
      if (isBrkPtChgd())
         _BkpChange.output(connection);
      if (isMonStackChgd())
         _StackChange.output(connection);
      if (isFCTChgd())
         _FCTChange.output(connection);
      if (isMonVariableChgd())
         _MonVarChange.output(connection);
      if ( isMonStorChgd() )
         _StorageChange.output(connection);
      if ( isMonRegsChgd() )
         _RegistersChange.output(connection);
      return tot;
   }


   /** Return the length of the fixed component */
   protected int fixedLen() {
      return _fixed_length;
   }

   /** Return the length of the variable component */
   protected int varLen() {
      return totalBytes(_message_text);
   }

   /** Add part change.  This corresponds to one ERepNextPart structure
     * @param PartItem the part table change item
     */
   public void addPartChangePacket(ERepNextPart partItem) {
      _changed_info |= EPDC.StdStat_Ptab;      // update changed info field
      if (_PartChange == null)
         _PartChange = new EPDC_ChangeInfo(EPDC.PARTS_TABLE_INFO);
      _PartChange.addChangedItem(partItem);
   }

   public Vector FCTChangeInfo()
   {
     return _FCTChangeInfo;
   }

   public Vector storageChangeInfo()
   {
     return _storageChangeInfo;
   }

   public Vector moduleChangeInfo()
   {
     return _moduleChangeInfo;
   }

   public Vector threadChangeInfo()
   {
     return _threadChangeInfo;
   }

   public Vector partChangeInfo()
   {
     return _partChangeInfo;
   }

   public Vector breakpointChangeInfo()
   {
     return _breakpointChangeInfo;
   }

   public Vector monitorChangeInfo()
   {
     return _monitorChangeInfo;
   }

   public Vector stackChangeInfo()
   {
     return _stackChangeInfo;
   }

   public Vector regsChangeInfo()
   {
     return _regsChangeInfo;
   }

   void add(RegistersChangeInfo regsChangeInfo)
   {
     if (_regsChangeInfo == null)
       _regsChangeInfo = new Vector();

     _regsChangeInfo.addElement(regsChangeInfo);
   }

   void add(StackChangeInfo stackChangeInfo)
   {
     if (_stackChangeInfo == null)
       _stackChangeInfo = new Vector();

     _stackChangeInfo.addElement(stackChangeInfo);
   }

   void add(FCTChangeInfo fctChangeInfo)
   {
     if (_FCTChangeInfo == null)
        _FCTChangeInfo = new Vector();

     _FCTChangeInfo.addElement(fctChangeInfo);
   }

   void add(StorageChangeInfo storageChangeInfo)
   {
     if (_storageChangeInfo == null)
        _storageChangeInfo = new Vector();

     _storageChangeInfo.addElement(storageChangeInfo);
   }

   /** Add a module change packet to this reply.
    */

   void add(ModuleChangeInfo moduleChangeInfo)
   {
     if (_moduleChangeInfo == null)
        _moduleChangeInfo = new Vector();

     _moduleChangeInfo.addElement(moduleChangeInfo);
   }

   /** Add a thread change packet to this reply.
    */

   void add(ThreadChangeInfo threadChangeInfo)
   {
     if (_threadChangeInfo == null)
        _threadChangeInfo = new Vector();

     _threadChangeInfo.addElement(threadChangeInfo);
   }

   /** Add a part change packet to this reply.
    */

   void add(PartChangeInfo partChangeInfo)
   {
     if (_partChangeInfo == null)
        _partChangeInfo = new Vector();

     _partChangeInfo.addElement(partChangeInfo);
   }

   /** Add a breakpoint change packet to this reply.
    */

   void add(BreakpointChangeInfo breakpointChangeInfo)
   {
     if (_breakpointChangeInfo == null)
        _breakpointChangeInfo = new Vector();

     _breakpointChangeInfo.addElement(breakpointChangeInfo);
   }

   /** Add a monitor change packet to this reply.
    */

   void add(MonitorChangeInfo monitorChangeInfo)
   {
     if (_monitorChangeInfo == null)
        _monitorChangeInfo = new Vector();

     _monitorChangeInfo.addElement(monitorChangeInfo);
   }

   /** Add module change.  This corresponds to one ERepNextModuleEntry structure
     * @param ModuleItem the module entry change item
     */
   public void addModuleChangePacket(ERepNextModuleEntry moduleItem) {
      _changed_info |= EPDC.StdStat_Module;      // update changed info field
      if (_ModuleChange == null)
         _ModuleChange = new EPDC_ChangeInfo(EPDC.MODULE_ENTRY_INFO);
      _ModuleChange.addChangedItem(moduleItem);
   }

   /** Add thread change.  This corresponds to one ERepGetNextThread structure
     * @param ThreadItem the thread change item
     */
   public void addThreadChangePacket(ERepGetNextThread threadItem) {
      _changed_info |= EPDC.StdStat_Prog;      // update changed info field
      if (_ProgChange == null)
         _ProgChange = new EPDC_ChangeInfo(EPDC.PROGRAM_STATE_INFO);
      _ProgChange.addChangedItem(threadItem);
   }

   /** Add breakpoint table change.  This corresponds to one ERepGetNextBkp structure
     * @param BkpItem the breakpoint change item
     */
   public void addBkpChangePacket(ERepGetNextBkp bkpItem) {
      _changed_info |= EPDC.StdStat_Btab;    // update change info field
      if (_BkpChange == null)
         _BkpChange = new EPDC_ChangeInfo(EPDC.BREAKPOINT_TABLE_INFO);
      _BkpChange.addChangedItem(bkpItem);
   }

   /**
    * Add stack tabel change.  This corresponds to one ERepGetChangedStack structure
    * @param StackItem the stack table change item
    */
   public void addStackChangePacket(ERepGetChangedStack stackItem) {
      _changed_info |= EPDC.StdStat_MStack;  // update change info field
      if (_StackChange == null)
         _StackChange = new EPDC_ChangeInfo(EPDC.STACK_INFO);
      _StackChange.addChangedItem(stackItem);
   }

   /**
    * Add registers change.  This corresponds to one ERepGetNextRegister structure
    * @param RegItem the register change item
    */
   public void addRegisterChangePacket(ERepGetNextRegister regItem) {
      _changed_info |= EPDC.StdStat_MRegs;  // update change info field
      if (_RegistersChange == null)
         _RegistersChange = new EPDC_ChangeInfo(EPDC.REGISTERS_INFO);
      _RegistersChange.addChangedItem(regItem);
   }

   /**
    * Add storage change.  This corresponds to one ERepGetNextMonitorStorageLine/ERepGetNextMonitorStorageId structure
    * @param storItem the storage change item
    */
   public void addStorageChangePacket(ERepGetNextMonitorStorageId storItem) {
      _changed_info |= EPDC.StdStat_MStor;  // update change info field
      if (_StorageChange == null)
         _StorageChange = new EPDC_ChangeInfo(EPDC.STORAGE_INFO);
      _StorageChange.addChangedItem(storItem);
   }

   /**
    * Add FCT bit change.  This corresponds to one ERepGetFCT structure
    * @param FCTItem the FCT change item
    */
   public void addFCTChangePacket(ERepGetFCT FCTItem) {
      _changed_info |= EPDC.StdStat_FCT;     // update change info field
      if (_FCTChange == null)
         _FCTChange = new EPDC_ChangeInfo(EPDC.FCT_INFO);
      _FCTChange.addChangedItem(FCTItem);
   }

   /**
    * Add monitored variable change.  This corresponds to one ERepGetNextMonitorExpr structure
    * @param MonitorVarItem the montiored variable change item
    */
   public void addMonVarChangePacket(ERepGetNextMonitorExpr monitorVarItem) {
      _changed_info |= EPDC.StdStat_Mvar;     // update change info field
      if (_MonVarChange == null)
          _MonVarChange = new EPDC_ChangeInfo(EPDC.MONITORED_VARIABLE_INFO);
      _MonVarChange.addChangedItem(monitorVarItem);
   }

   public void write(PrintWriter printWriter)
   {
     printWriter.println("--------REPLY--------");
     printWriter.println();
     printWriter.println("REPLY TYPE: " + getName() );
     increaseIndentLevel();

     if ( getDetailLevel() >= DETAIL_LEVEL_MEDIUM )
     {
        indent(printWriter);
        printWriter.print("Reply Code : " + _reply_code);
        printWriter.println("    Length : " + _total_bytes);

        indent(printWriter);
        printWriter.println("ReturnCode : " + returnCodeString());
        indent(printWriter);
        printWriter.println("Changed info bits : 0x" + Integer.toHexString(_changed_info));
        indent(printWriter);
        printWriter.println("Packets Expected : ");
        printWriter.println(changeInfoBitString());
     }
     if ( getDetailLevel() >= DETAIL_LEVEL_MEDIUM )
     {
     	indent(printWriter);
     	printWriter.println("PMDebuggingStatus (OS/2 Only): " + PMDebuggingStatusString());
     }
     if (getDetailLevel() >= DETAIL_LEVEL_HIGH
         || _message_text != null
         || _return_code != EPDC.ExecRc_OK)
     {
       indent(printWriter);
       printWriter.println("Message Text : ");

       if (_message_text != null)
       {
          indent(printWriter);
          indent(printWriter);
       	  _message_text.write(printWriter);
          printWriter.println();
       }
     }
     decreaseIndentLevel();
   }

   /**
    * Write out a packet to 'printWriter' in hex representation.
    */

   public static void write(PrintWriter printWriter,
                            Connection connection,
                            boolean readOnly)
   {
     ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

     try
     {
        int repCode = getPacket(connection, byteStream);

        if (readOnly)
           return;

        // Get a byte array which contains the entire packet:

        byte[] packet = byteStream.toByteArray();

        printWriter.print("Reply Code : " + repCode +
                          " (" + getHexDigits(repCode) + ")");
        printWriter.println("    Length : " + packet.length +
                            " (" + getHexDigits(packet.length) + ")");

        EPDC_Base.write(printWriter, packet);
     }
     catch(IOException excp)
     {
        printWriter.println("Error: IOException occurred trying to read packet.");
     }
   }

   /**
   * returns true if any changed bits are on
   *
   */
   public boolean anyChanged() {
      return ( (_changed_info & 0xffffffff) != 0 );
   }

   public boolean isPartChgd() {
      return ( (_changed_info & EPDC.StdStat_Ptab) != 0 ); }
   public boolean isBrkPtChgd() {
      return ( (_changed_info & EPDC.StdStat_Btab) != 0 ); }
   public boolean isPgmStateChgd() {
      return ( (_changed_info & EPDC.StdStat_Prog) != 0 ); }
   public boolean isMonVariableChgd() {
      return ( (_changed_info & EPDC.StdStat_Mvar) != 0 ); }
   public boolean isThrdStateChgd() {
      return ( (_changed_info & EPDC.StdStat_ThdState) != 0 ); }
   public boolean isMonStorChgd() {
      return ( (_changed_info & EPDC.StdStat_MStor) != 0 ); }
   public boolean isMonStackChgd() {
      return ( (_changed_info & EPDC.StdStat_MStack) != 0 ); }
   public boolean isMonRegsChgd() {
      return ( (_changed_info & EPDC.StdStat_MRegs) != 0 ); }
   public boolean isModuleEntryChgd() {
      return ( (_changed_info & EPDC.StdStat_Module) != 0 ); }
   public boolean isFCTChgd() {
      return ( (_changed_info & EPDC.StdStat_FCT) != 0); }


   private int minChangePackets()
   {
      return (isPartChgd() ? 1 : 0) +
        (isBrkPtChgd() ? 1 : 0) +
        (isPgmStateChgd() ? 1 : 0) +
        (isMonVariableChgd() ? 1 : 0) +
        (isThrdStateChgd() ? 1 : 0) +
        (isMonStorChgd() ? 1 : 0) +
        (isMonStackChgd() ? 1 : 0) +
        (isMonRegsChgd() ? 1 : 0) +
        (isModuleEntryChgd() ? 1 : 0) +
        (isFCTChgd() ? 1 : 0);
   }

   protected int      _reply_code;
   protected int        _return_code;
   private int        _changed_info;
   protected int        _message_offset;
   protected EStdString _message_text;
   private int        _PMDebuggingStatus;
   protected int      _total_bytes;

   private EPDC_ChangeInfo _PartChange;      // change in part table
   private EPDC_ChangeInfo _ModuleChange;    // change in modules
   private EPDC_ChangeInfo _ProgChange;      // change in program state
   private EPDC_ChangeInfo _BkpChange;       // change in breakpoint table
   private EPDC_ChangeInfo _StackChange;     // change in monitored stack table
   private EPDC_ChangeInfo _FCTChange;       // change in FCT bits
   private EPDC_ChangeInfo _MonVarChange;    // change in monitored variables
   private EPDC_ChangeInfo _RegistersChange; // change in registers
   private EPDC_ChangeInfo _StorageChange;   // change in storage

   // TODO: These members should eventually replace the above ones (once
   // PICL has been changed over to use the new ones, below):

   private Vector _moduleChangeInfo;
   private Vector _threadChangeInfo;
   private Vector _partChangeInfo;
   private Vector _breakpointChangeInfo;
   private Vector _monitorChangeInfo;
   private Vector _stackChangeInfo;
   private Vector _regsChangeInfo;
   private Vector _FCTChangeInfo;
   private Vector _storageChangeInfo;

   private static final int _fixed_length = 24;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}