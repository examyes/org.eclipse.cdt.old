package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1998, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EPDC_Request.java, java-epdc, eclipse-dev
// Version 1.59.1.1 (last modified 11/21/01 09:24:16)
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

/**
 * Request packet class
 */
public class EPDC_Request extends EPDC_Base {

  protected EPDC_Request(byte[] packetBuffer,
			 DataInputStream dataInputStream)
    throws IOException
  {
    _request_code = dataInputStream.readInt();
    _total_bytes = dataInputStream.readInt();
  }

  /**
   * All request classes inherit from this class
   */
  protected EPDC_Request(byte[] inBuffer) throws IOException {
    super( inBuffer );
    _request_code = readInt();
    _total_bytes = readInt();
  }

  protected EPDC_Request(int request_code) {
    _request_code = request_code;
  }

   /**
    * Reads a request packet from 'connection' and puts it into 'bos'.
    * @return The request code.
    */

   private static int getPacket(Connection connection,
                                ByteArrayOutputStream bos)
   throws IOException
   {
     DataOutputStream dos = new DataOutputStream (bos);

     connection.beginPacketRead();
     DataInputStream inStream =
       new DataInputStream(connection.getInputStream());

	 inStream.readInt();
	 
     int reqCode = inStream.readInt();      // get request code
     dos.writeInt(reqCode);
     int totBytes = inStream.readInt();     // get total bytes
     dos.writeInt(totBytes);

     byte[] b = new byte[totBytes - 8];

     if (b.length > 0) {
       inStream.readFully(b);
       dos.write(b);
     }

     connection.endPacketRead(EPDC.RequestPacket);

     return reqCode;
   }

   public String getName()
   {
      switch ( requestCode() )
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
   * Decodes and returns a request object from an EPDC connection
   * based on the request type
   */
  static public EPDC_Request decodeRequestStream(Connection connection)
    throws IOException
  {
    ByteArrayOutputStream bos = new ByteArrayOutputStream ();

    int reqCode = getPacket(connection, bos);

    byte[] packetByteArray = bos.toByteArray();

    DataInputStream dataInputStream =
      new DataInputStream(new ByteArrayInputStream(packetByteArray));

    switch (reqCode) {
    case EPDC.Remote_BreakpointClear:
      return new EReqBreakpointClear(packetByteArray);
    case EPDC.Remote_BreakpointDisable:
      return new EReqBreakpointDisable(packetByteArray);
    case EPDC.Remote_BreakpointEnable:
      return new EReqBreakpointEnable(packetByteArray);
    case EPDC.Remote_BreakpointEvent:
      return new EReqBreakpointEvent(packetByteArray);
    case EPDC.Remote_BreakpointLocation:
      return new EReqBreakpointLocation(packetByteArray);
    case EPDC.Remote_CommandLogExecute:
      return new EReqCommandLogExecute(packetByteArray);
    case EPDC.Remote_ContextConvert:
      return new EReqContextConvert(packetByteArray, dataInputStream);
    case EPDC.Remote_ContextQualGet:
      return new EReqContextQualGet(packetByteArray);
    case EPDC.Remote_EntrySearch:
      return new EReqEntrySearch(packetByteArray);
    case EPDC.Remote_EntryWhere:
      return new EReqEntryWhere(packetByteArray);
    case EPDC.Remote_ExceptionStatusChange:
      return new EReqExceptionStatusChange(packetByteArray);
    case EPDC.Remote_Execute:
      return new EReqExecute(packetByteArray);
    case EPDC.Remote_Expression:
      return new EReqExpression(packetByteArray);
    case EPDC.Remote_ExpressionDisable:
      return new EReqExpressionDisable(packetByteArray);
    case EPDC.Remote_ExpressionEnable:
      return new EReqExpressionEnable(packetByteArray);
    case EPDC.Remote_ExpressionFree:
      return new EReqExpressionFree(packetByteArray);
    case EPDC.Remote_ExpressionRepTypeSet:
      return new EReqExpressionRepTypeSet(packetByteArray);
    case EPDC.Remote_ExpressionSubTree:
      return new EReqExpressionSubTree(packetByteArray);
    case EPDC.Remote_ExpressionSubTreeDelete:
      return new EReqExpressionSubTreeDelete(packetByteArray);
    case EPDC.Remote_ExpressionValueModify:
      return new EReqExpressionValueModify(packetByteArray);
    case EPDC.Remote_Initialize_Debug_Engine:
      return new EReqInitializeDE(packetByteArray);
    case EPDC.Remote_LocalVariable:
      return new EReqLocalVariable(packetByteArray);
    case EPDC.Remote_LocalVariableFree:
      return new EReqLocalVariableFree(packetByteArray);
    case EPDC.Remote_PartGet:
      return new EReqPartGet(packetByteArray);
    case EPDC.Remote_PartOpen:
      return new EReqPartOpen(packetByteArray);
    case EPDC.Remote_PartSet:
      return new EReqPartSet(packetByteArray);
    case EPDC.Remote_PointerDeref:
      return new EReqPointerDeref(packetByteArray);
    case EPDC.Remote_PreparePgm:
      return new EReqPreparePgm(packetByteArray);
    case EPDC.Remote_ProcessAttach:
      return new EReqProcessAttach(packetByteArray);
    case EPDC.Remote_ProcessAttach2:
      return new EReqProcessAttach2(packetByteArray);
    case EPDC.Remote_ProcessDetach:
      return new EReqProcessDetach(packetByteArray);
    case EPDC.Remote_ProcessDetailsGet:
      return new EReqProcessDetailsGet(packetByteArray);
    case EPDC.Remote_ProcessListGet:
      return new EReqProcessListGet(packetByteArray);
    case EPDC.Remote_RepForTypeSet:
      return new EReqRepForTypeSet(packetByteArray);
    case EPDC.Remote_Stack:
      return new EReqStack(packetByteArray);
    case EPDC.Remote_StackBuildView:
      return new EReqStackBuildView(packetByteArray);
    case EPDC.Remote_StackDetailsGet:
      return new EReqStackDetailsGet(packetByteArray);
    case EPDC.Remote_StackFree:
      return new EReqStackFree(packetByteArray);
    case EPDC.Remote_StartPgm:
      return new EReqStartPgm(packetByteArray);
    case EPDC.Remote_StringFind:
      return new EReqStringFind(packetByteArray, dataInputStream);
    case EPDC.Remote_Terminate_Debug_Engine:
      return new EReqTerminateDE(packetByteArray);
    case EPDC.Remote_TerminatePgm:
      return new EReqTerminatePgm(packetByteArray);
    case EPDC.Remote_ThreadFreeze:
      return new EReqThreadFreeze(packetByteArray);
    case EPDC.Remote_ThreadThaw:
      return new EReqThreadThaw(packetByteArray);
    case EPDC.Remote_TypesNumGet:
      return new EReqTypesNumGet(packetByteArray);
    case EPDC.Remote_Version:
      return new EReqVersion(packetByteArray);
    case EPDC.Remote_ViewsVerify:
      return new EReqVerifyViews(packetByteArray);
    case EPDC.Remote_Halt:
      return new EReqRemoteHalt(packetByteArray);
    case EPDC.Remote_ViewSearchPath:
      return new EReqViewSearchPath(packetByteArray);
    case EPDC.Remote_ViewFileInfoSet:
      return new EReqViewFileInfoSet(packetByteArray);
    case EPDC.Remote_StorageUsageCheckSet:
      return new EReqStorageUsageCheckSet(packetByteArray);
    case EPDC.Remote_Storage2:
      return new EReqStorage2(packetByteArray);
    case EPDC.Remote_StorageRangeSet2:
      return new EReqStorageRangeSet2(packetByteArray);
    case EPDC.Remote_StorageStyleSet:
      return new EReqStorageStyleSet(packetByteArray);
    case EPDC.Remote_StorageEnablementSet:
      return new EReqStorageEnablementSet(packetByteArray);
    case EPDC.Remote_StorageUpdate:
      return new EReqStorageUpdate(packetByteArray);
    case EPDC.Remote_StorageFree:
      return new EReqStorageFree(packetByteArray);
    case EPDC.Remote_RegistersValueSet:
      return new EReqRegistersValueSet(packetByteArray);
    case EPDC.Remote_RegistersDetailsGet:
      return new EReqRegistersDetailsGet(packetByteArray);
    case EPDC.Remote_Registers2:
      return new EReqRegisters2(packetByteArray);
    case EPDC.Remote_RegistersFree2:
      return new EReqRegistersFree2(packetByteArray);
    case EPDC.Remote_ThreadInfoGet:
      return new EReqThreadInfoGet(packetByteArray);

    default:
      System.out.println("Unknown epdc request typeCode:" +reqCode);
      return null;
    }

  }

  /**
   * Calculate total bytes, give connection object a chance to write out a
   * packet prefix, then stream out this object.
   */

  public void output(Connection connection)
    throws IOException
  {
    _total_bytes = fixedLen() + varLen();

    connection.beginPacketWrite(_total_bytes + 4);

    DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStreamBuffer());

	dataOutputStream.writeInt(703);
    output(dataOutputStream);
    connection.endPacketWrite(EPDC.RequestPacket);
  }


  public void output(DataOutputStream dataOutputStream)
    throws IOException
  {
    dataOutputStream.writeInt(_request_code);
    dataOutputStream.writeInt(_total_bytes);
  }

  /** Return the length of the fixed component */
  protected int fixedLen() {
    return _fixed_length;
  }

  /** Return the length of the variable component */
  protected int varLen() {
    return 0;
  }

  /**
   * Returns the request code
   *    See interface EPDC for request code values
   *
   */
  public int requestCode() { return _request_code; }

  public void write(PrintWriter printWriter)
  {
    printWriter.println("--------REQUEST--------");
    printWriter.println();
    printWriter.println("REQUEST TYPE: " + getName() );
    increaseIndentLevel();
    if (getDetailLevel() >= DETAIL_LEVEL_MEDIUM)
    {
       indent(printWriter);
       printWriter.print("Request Code : " + _request_code);
       printWriter.println("    Length : " + _total_bytes);
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
        int reqCode = getPacket(connection, byteStream);

        if (readOnly)
           return;

        // Get a byte array which contains the entire packet:

        byte[] packet = byteStream.toByteArray();

        printWriter.print("Request Code : " + reqCode +
                          " (" + getHexDigits(reqCode) + ")");
        printWriter.println("    Length : " + packet.length +
                          " (" + getHexDigits(packet.length) + ")");

        EPDC_Base.write(printWriter, packet);
     }
     catch(IOException excp)
     {
        printWriter.println("Error: IOException occurred trying to read packet.");
     }
   }

  private int               _request_code;
  private int               _total_bytes;
  private static final int  _fixed_length = 8;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";
}