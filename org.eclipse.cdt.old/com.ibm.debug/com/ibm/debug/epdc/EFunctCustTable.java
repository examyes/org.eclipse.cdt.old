package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1995, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EFunctCustTable.java, java-epdc, eclipse-dev, 20011128
// Version 1.37.1.2 (last modified 11/28/01 16:23:04)
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
  * Function customization table
  */
public class EFunctCustTable extends EPDC_Base {

  EFunctCustTable(byte[] packetBuffer, DataInputStream dataInputStream)
  throws IOException
  {
    _startup = dataInputStream.readInt();
    _general_functions = dataInputStream.readInt();
    _file_options = dataInputStream.readInt();
    _storage_options = dataInputStream.readInt();
    _breakpoint_options =  dataInputStream.readInt();
    _monitor_options = dataInputStream.readInt();
    _windows_options = dataInputStream.readInt();
    _run_options = dataInputStream.readInt();
    _exception_options = dataInputStream.readInt();
    _stack_options = dataInputStream.readInt();
  }

   public EFunctCustTable(int startup, int general_functions, int file_options,
         int storage_options, int breakpoint_options, int monitor_options,
         int windows_options, int run_options, int exception_options,
         int stack_options) {
      super();
      _startup = startup;
      _general_functions = general_functions;
      _file_options = file_options;
      _storage_options = storage_options;
      _breakpoint_options = breakpoint_options;
      _monitor_options = monitor_options;
      _windows_options = windows_options;
      _run_options = run_options;
      _exception_options = exception_options;
      _stack_options = stack_options;
   }

   /** Output class to data streams according to EPDC protocol
     * @exception IOException if an I/O error occurs
    *  @exception BadEPDCCommandException if the EPDC command
    *    is structured incorrectly
    */
   protected int toDataStreams(DataOutputStream fixedData,
         DataOutputStream varData, int baseOffset)
         throws IOException, BadEPDCCommandException {

      writeInt(fixedData, _startup);
      writeInt(fixedData, _general_functions);
      writeInt(fixedData, _file_options);
      writeInt(fixedData, _storage_options);
      writeInt(fixedData, _breakpoint_options);
      writeInt(fixedData, _monitor_options);
      writeInt(fixedData, _windows_options);
      writeInt(fixedData, _run_options);
      writeInt(fixedData, _exception_options);
      writeInt(fixedData, _stack_options);

      return fixedLen() + varLen();
   }

   /** Return length of fixed portion */
   protected int fixedLen() {
      return _fixed_length;
   }

   /** Return length of fixed portion -- static function*/
   protected static int _fixedLen() {
      return _fixed_length;
   }

   /** Return length of variable portion */
   protected int varLen() {
      return 0;
   }

   // STARTUP CAPABILITIES

   public int getStartupCapabilities()
   {
     return _startup;
   }

   public boolean debugInitializationSupported()
   {
     return (_startup & EPDC.FCT_DEBUG_APPLICATION_INIT) != 0;
   }

   /* PROCEDURE_NAME_ACCEPTED is not currently supported (as of v306).*/
   public boolean procedureNameAcceptSupported()
   {
     return (_startup & EPDC.FCT_PROCEDURE_NAME_ACCEPTED) != 0;
   }

   public boolean jobNameSupported()
   {
     return (_startup & EPDC.FCT_JOB_NAME) != 0;
   }

   public boolean programFilelistSupported()
   {
     return (_startup & EPDC.FCT_PROGRAM_FILELIST) != 0;
   }

   public boolean hostAddressSupported()
   {
     return (_startup & EPDC.FCT_HOST_ADDRESS) != 0;
   }

   // GENERAL CAPABILITIES

   public int getGeneralCapabilities()
   {
     return _general_functions;
   }

   public boolean startupSupported()
   {
     return (_general_functions & EPDC.FCT_STARTUP) != 0;
   }

   public boolean processListStartupSupported()
   {
     return (_general_functions & EPDC.FCT_PROCESS_LIST_STARTUP) != 0;
   }

   public boolean multipleThreadsSupported()
   {
     return (_general_functions & EPDC.FCT_MULTIPLE_THREADS) != 0;
   }

   public boolean postMortemDebugMode()
   {
     return (_general_functions & EPDC.FCT_POST_MORTEM_DEBUG) != 0;
   }

   /*This mode not supported*/
   public boolean multipleProcessesSupported()
   {
     return (_general_functions & EPDC.FCT_MULTIPLE_PROCESSES) != 0;
   }

   /*Supported in OS/2 only*/
   public boolean PMDebuggingSupported()
   {
     return (_general_functions & EPDC.FCT_PM_DEBUGGING) != 0;
   }

   public boolean fileListAvailSupported()
   {
     return (_general_functions & EPDC.FCT_FILE_LIST_AVAILABLE) != 0;
   }

   public boolean childProcessSupported()
   {
     return (_general_functions & EPDC.FCT_CHILD_PROCESSES) != 0;
   }

   public boolean includeFilesSupported()
   {
     return (_general_functions & EPDC.FCT_INCLUDE_FILES) != 0;
   }

   // Not supported as of v306
   public boolean environmentModifySupported()
   {
     return (_general_functions & EPDC.FCT_ENVIRONMENT_MODIFY) != 0;
   }

   // Not supported as of v306
   public boolean filePathAvailSupported()
   {
     return (_general_functions & EPDC.FCT_FILE_PATH_AVAILABLE) != 0;
   }

   public boolean debugOnDemandSupported()
   {
     return (_general_functions & EPDC.FCT_DEBUG_ON_DEMAND) != 0;
   }

   public boolean postMortemCapableSupported()
   {
     return (_general_functions & EPDC.FCT_POST_MORTEM_CAPABLE) != 0;
   }

   // FILE CAPABILITIES

   public int getFileCapabilities()
   {
     return _file_options;
   }

   public boolean changeSourceFileSupported()
   {
     return (_file_options & EPDC.FCT_CHANGE_SOURCE_FILE) != 0;
   }

   public boolean restartSupported()
   {
     return (_file_options & EPDC.FCT_FILE_RESTART) != 0;
   }

   public void setProcessAttachSupported(boolean supported)
   {
     if (supported)
        _file_options |= EPDC.FCT_PROCESS_ATTACH;
     else
     if ((_file_options & EPDC.FCT_PROCESS_ATTACH) != 0)
        _file_options ^= EPDC.FCT_PROCESS_ATTACH;
   }

   public boolean processAttachSupported()
   {
     return (_file_options & EPDC.FCT_PROCESS_ATTACH) != 0;
   }

   public boolean processAttachPathSupported()
   {
     return (_file_options & EPDC.FCT_PROCESS_ATTACH_PATH) != 0;
   }

   public boolean processDetachSupported()
   {
     return (_file_options & EPDC.FCT_PROCESS_DETACH) != 0;
   }

   public boolean processDetachKillSupported()
   {
     return (_file_options & EPDC.FCT_PROCESS_DETACH_KILL) != 0;
   }

   public boolean processDetachKeepSupported()
   {
     return (_file_options & EPDC.FCT_PROCESS_DETACH_KEEP) != 0;
   }

   public boolean processDetachReleaseSupported()
   {
     return (_file_options & EPDC.FCT_PROCESS_DETACH_RELEASE) != 0;
   }

   public boolean localSourceFilesSupported()
   {
     return (_file_options & EPDC.FCT_LOCAL_SOURCE_FILES) != 0;
   }

   public boolean moduleAddSupported()
   {
     return (_file_options & EPDC.FCT_MODULE_ADD) != 0;
   }

   public boolean moduleRemoveSupported()
   {
     return (_file_options & EPDC.FCT_MODULE_REMOVE) != 0;
   }

   // STORAGE CAPABILITIES

   public int getStorageCapabilities()
   {
     return _storage_options;
   }

   public boolean storageAddressFlatSupported() {
     return (_storage_options & EPDC.FCT_STORAGE_ADDRESS_FLAT) != 0;
   }
   public boolean storageAddress1616Supported() {
     return (_storage_options & EPDC.FCT_STORAGE_ADDRESS_1616) != 0;
   }
   public boolean storageAddressFlat1616Supported() {
     return (_storage_options & EPDC.FCT_STORAGE_ADDRESS_FLAT_1616) != 0;
   }
   public boolean storageContentHexCharSupported() {
     return (_storage_options & EPDC.FCT_STORAGE_CONTENT_HEX_CHAR) != 0;
   }
   public boolean storageContentCharSupported() {
     return (_storage_options & EPDC.FCT_STORAGE_CONTENT_CHAR) != 0;
   }
   public boolean storageContent16IntSupported() {
     return (_storage_options & EPDC.FCT_STORAGE_CONTENT_16INT) != 0;
   }
   public boolean storageContent16UIntSupported() {
     return (_storage_options & EPDC.FCT_STORAGE_CONTENT_16UINT) != 0;
   }
   public boolean storageContent16IntHexSupported() {
     return (_storage_options & EPDC.FCT_STORAGE_CONTENT_16INTHEX) != 0;
   }
   public boolean storageContent32IntSupported() {
     return (_storage_options & EPDC.FCT_STORAGE_CONTENT_32INT) != 0;
   }
   public boolean storageContent32UIntSupported() {
     return (_storage_options & EPDC.FCT_STORAGE_CONTENT_32UINT) != 0;
   }
   public boolean storageContent32IntHexSupported() {
     return (_storage_options & EPDC.FCT_STORAGE_CONTENT_32INTHEX) != 0;
   }
   public boolean storageContent32FloatSupported() {
     return (_storage_options & EPDC.FCT_STORAGE_CONTENT_32FLOAT) != 0;
   }
   public boolean storageContent64FloatSupported() {
     return (_storage_options & EPDC.FCT_STORAGE_CONTENT_64FLOAT) != 0;
   }
   public boolean storageContent88FloatSupported() {
     return (_storage_options & EPDC.FCT_STORAGE_CONTENT_88FLOAT) != 0;
   }
   public boolean storageContent16PtrSupported() {
     return (_storage_options & EPDC.FCT_STORAGE_CONTENT_16PTR) != 0;
   }
   public boolean storageContent1616PtrSupported() {
     return (_storage_options & EPDC.FCT_STORAGE_CONTENT_1616PTR) != 0;
   }
   public boolean storageContent32PtrSupported() {
     return (_storage_options & EPDC.FCT_STORAGE_CONTENT_32PTR) != 0;
   }
   public boolean storageContentHexEBCDICSupported() {
     return (_storage_options & EPDC.FCT_STORAGE_CONTENT_HEX_EBCDIC) != 0;
   }
   public boolean storageContentEBCDICSupported() {
     return (_storage_options & EPDC.FCT_STORAGE_CONTENT_EBCDIC) != 0;
   }
   public boolean storageContentHexASCIISupported() {
     return (_storage_options & EPDC.FCT_STORAGE_CONTENT_HEX_ASCII) != 0;
   }
   public boolean storageContentASCIISupported() {
     return (_storage_options & EPDC.FCT_STORAGE_CONTENT_ASCII) != 0;
   }
   public boolean storageContentIEEE32Supported() {
     return (_storage_options & EPDC.FCT_STORAGE_CONTENT_IEEE_32) != 0;
   }
   public boolean storageContentIEEE64Supported() {
     return (_storage_options & EPDC.FCT_STORAGE_CONTENT_IEEE_64) != 0;
   }
   public boolean storageContent64IntSupported() {
     return (_storage_options & EPDC.FCT_STORAGE_CONTENT_64INT) != 0;
   }
   public boolean storageContent64UIntSupported() {
     return (_storage_options & EPDC.FCT_STORAGE_CONTENT_64UINT) != 0;
   }
   public boolean storageContent64IntHexSupported() {
     return (_storage_options & EPDC.FCT_STORAGE_CONTENT_64INTHEX) != 0;
   }
   public boolean storageContent64PtrSupported() {
     return (_storage_options & EPDC.FCT_STORAGE_CONTENT_64PTR) != 0;
   }

   public boolean storageEnableDisableSupported()
   {
     return (_storage_options & EPDC.FCT_STORAGE_ENABLE_TOGGLE) != 0;
   }

   public boolean storageExprEnableDisableSupported()
   {
     return (_storage_options & EPDC.FCT_STORAGE_EXPR_ENABLE_TOGGLE) != 0;
   }

   // BREAKPOINT CAPABILITIES

   public int getBreakpointCapabilities()
   {
     return _breakpoint_options;
   }

   public void setBreakpointCapabilities(int breakpoint_options)
   {
     _breakpoint_options = breakpoint_options;
   }

   public boolean lineBreakpointsSupported()
   {
     return (_breakpoint_options & EPDC.FCT_LINE_BREAKPOINT) != 0;
   }

   public boolean functionBreakpointsSupported()
   {
     return (_breakpoint_options & EPDC.FCT_FUNCTION_BREAKPOINT) != 0;
   }

   public boolean addressBreakpointsSupported()
   {
     return (_breakpoint_options & EPDC.FCT_ADDRESS_BREAKPOINT) != 0;
   }

   public boolean watchpointsSupported()
   {
     return (_breakpoint_options & EPDC.FCT_CHANGE_ADDRESS_BREAKPOINT) != 0;
   }

   public boolean moduleLoadBreakpointsSupported()
   {
     return (_breakpoint_options & EPDC.FCT_LOAD_BREAKPOINT) != 0;
   }

   public boolean breakpointEnableDisableSupported()
   {
     return (_breakpoint_options & EPDC.FCT_BREAKPOINT_ENABLE_TOGGLE) != 0;
   }

   public boolean breakpointModifySupported()
   {
     return (_breakpoint_options & EPDC.FCT_BREAKPOINT_MODIFY) != 0;
   }

   public boolean deferredBreakpointsSupported()
   {
     return (_breakpoint_options & EPDC.FCT_BREAKPOINT_DEFERRED) != 0;
   }

   public boolean entryBreakpointsAutoSetSupported()
   {
     return (_breakpoint_options & EPDC.FCT_BREAKPOINT_ENTRY_AUTOSET) != 0;
   }

   public boolean conditionalBreakpointsSupported()
   {
     return (_breakpoint_options & EPDC.FCT_BREAKPOINT_EXPRESSION) != 0;
   }

   public boolean breakpointThreadsSupported()
   {
     return (_breakpoint_options & EPDC.FCT_BREAKPOINT_NO_THREADS) == 0;
   }

   public boolean breakpointFrequencySupported()
   {
     return (_breakpoint_options & EPDC.FCT_BREAKPOINT_NO_FREQUENCY) == 0;
   }

   public boolean monitor8BytesSupported()
   {
     return (_breakpoint_options & EPDC.FCT_BREAKPOINT_MONITOR_8BYTES) != 0;
   }

   public boolean monitor4BytesSupported()
   {
     return (_breakpoint_options & EPDC.FCT_BREAKPOINT_MONITOR_4BYTES) != 0;
   }

   public boolean monitor2BytesSupported()
   {
     return (_breakpoint_options & EPDC.FCT_BREAKPOINT_MONITOR_2BYTES) != 0;
   }

   public boolean monitor1BytesSupported()
   {
     return (_breakpoint_options & EPDC.FCT_BREAKPOINT_MONITOR_1BYTES) != 0;
   }

   public boolean monitor0_128BytesSupported()
   {
     return (_breakpoint_options & EPDC.FCT_BREAKPOINT_MONITOR_0_128) != 0;
   }

   public boolean dateBreakpointsSupported()
   {
     return (_breakpoint_options & EPDC.FCT_BREAKPOINT_DATE) != 0;
   }

   public boolean statementBreakpointSupported() //not supported (as of v306)
   {
     return (_breakpoint_options & EPDC.FCT_STATEMENT_BREAKPOINT) != 0;
   }

   public boolean eventBreakpointSupported() //not supported (as of v306)
   {
     return (_breakpoint_options & EPDC.FCT_EVENT_BREAKPOINT) != 0;
   }

   // MONITOR CAPABILITIES

   public int getMonitorCapabilities()
   {
     return _monitor_options;
   }

   public boolean monitorEnableDisableSupported()
   {
     return (_monitor_options & EPDC.FCT_MONITOR_ENABLE_TOGGLE) != 0;
   }

   // WINDOW CAPABILITIES

   public int getWindowCapabilities()
   {
     return _windows_options;
   }

   public boolean monitorLocalVariablesSupported()
   {
     return (_windows_options & EPDC.FCT_LOCAL_VARIABLES) != 0;
   }

   public boolean monitorRegistersSupported()
   {
     return (_windows_options & EPDC.FCT_REGISTERS) != 0;
   }

   public boolean monitorStackSupported()
   {
     return (_windows_options & EPDC.FCT_STACK) != 0;
   }

   public boolean monitorStorageSupported()
   {
     return (_windows_options & EPDC.FCT_STORAGE) != 0;
   }

   public boolean monitorWindowAnalysisSupported()
   {
     return (_windows_options & EPDC.FCT_WINDOW_ANALYSIS) != 0;
   }

   public boolean monitorMessageQueueSupported()
   {
     return (_windows_options & EPDC.FCT_MESSAGE_QUEUE_MONITOR) != 0;
   }

   public boolean commandLogSupported()
   {
     return (_windows_options & EPDC.FCT_COMMAND_LOG) != 0;
   }

   public boolean monitorInheritanceViewSupported() //not supported
   {
     return (_windows_options & EPDC.FCT_INHERITANCE_VIEW) != 0;
   }

   public boolean monitorCommandLogSupported() //not supported
   {
     return (_windows_options & EPDC.FCT_COMMAND_LOG) != 0;
   }

   public boolean monitorPassThruSupported() //not supported
   {
     return (_windows_options & EPDC.FCT_PASSTHRU) != 0;
   }

   // RUN CAPABILITIES

   public int getRunCapabilities()
   {
     return _run_options;
   }

   public boolean threadFreezeThawSupported()
   {
     return (_run_options & EPDC.FCT_THREAD_ENABLED) != 0;
   }

   public boolean stepOverSupported()
   {
     return (_run_options & EPDC.FCT_STEP_OVER) != 0;
   }

   public boolean stepIntoSupported()
   {
     return (_run_options & EPDC.FCT_STEP_INTO) != 0;
   }

   public boolean stepDebugSupported()
   {
     return (_run_options & EPDC.FCT_STEP_DEBUG) != 0;
   }

   public boolean stepReturnSupported()
   {
     return (_run_options & EPDC.FCT_STEP_RETURN) != 0;
   }

   public boolean runToLocationSupported()
   {
     return (_run_options & EPDC.FCT_RUN_TO_LOCATION) != 0;
   }

   public boolean jumpToLocationSupported()
   {
     return (_run_options & EPDC.FCT_JUMP_TO_LOCATION) != 0;
   }

   public boolean haltSupported()
   {
     return (_run_options & EPDC.FCT_HALT) != 0;
   }

   public boolean storageUsageCheckSupported()
   {
     return (_run_options & EPDC.FCT_STORAGE_USAGE_CHECK) != 0;
   }

   // EXCEPTION CAPABILITIES
   public int getExceptionCapabilities()
   {
     return _exception_options;
   }

   public boolean exceptionFilterSupported()
   {
     return (_exception_options & EPDC.FCT_EXCEPTION_FILTER) != 0;
   }

   public boolean exceptionExamineSupported()
   {
     return (_exception_options & EPDC.FCT_EXCEPTION_EXAMINE) != 0;
   }

   public boolean exceptionStepSupported()
   {
     return (_exception_options & EPDC.FCT_EXCEPTION_STEP) != 0;
   }

   public boolean exceptionRunSupported()
   {
     return (_exception_options & EPDC.FCT_EXCEPTION_RUN) != 0;
   }

   //STACK CAPABILITIES
   public int getStackCapabilities()
   {
     return _stack_options;
   }

   public boolean stackRemainingSizeSupported()
   {
     return (_stack_options & EPDC.FCT_STACK_REMAINING_SZE) != 0;
   }

   /*This option not supported*/
   public boolean stackSetBreakpointSupported()
   {
     return (_stack_options & EPDC.FCT_STACK_SET_BREAKPOINT) != 0;
   }

   /*This option not supported*/
   public boolean stackOpenStorageSupported()
   {
     return (_stack_options & EPDC.FCT_STACK_OPEN_STORAGE) != 0;
   }

   public void startupWrite(PrintWriter printWriter, byte detailLevel)
   {
      printWriter.println("DE Startup Options");
      increaseIndentLevel();
      if (procedureNameAcceptSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_PROCEDURE_NAME_ACCEPTED");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_PROCEDURE_NAME_ACCEPTED");
      }
      if (debugInitializationSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_DEBUG_APPLICATION_INIT");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_DEBUG_APPLICATION_INIT");
      }
      if (jobNameSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_JOB_NAME");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_JOB_NAME");
      }
      if (programFilelistSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_PROGRAM_FILELIST");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_PROGRAM_FILELIST");
      }
      if (hostAddressSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_HOST_ADDRESS");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_HOST_ADDRESS");
      }
      decreaseIndentLevel();
   }

   public void genFunctionsWrite(PrintWriter printWriter, byte detailLevel)
   {
      printWriter.println("General Function options");
      increaseIndentLevel();
      if (multipleThreadsSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_MULTIPLE_THREADS");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_MULTIPLE_THREADS");
      }
      if (multipleProcessesSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_MULTIPLE_PROCESSES");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_MULTIPLE_PROCESSES");
      }
      if (PMDebuggingSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_PM_DEBUGGING");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_PM_DEBUGGING");
      }
      if (fileListAvailSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_FILE_LIST_AVAILABLE");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_FILE_LIST_AVAILABLE");
      }
      if (childProcessSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_CHILD_PROCESSES");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_CHILD_PROCESSES");
      }
      if (includeFilesSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_INCLUDE_FILES");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_INCLUDE_FILES");
      }
      if (environmentModifySupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_ENVIRONMENT_MODIFY");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_ENVIRONMENT_MODIFY");
      }
      if (filePathAvailSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_FILE_PATH_AVAILABLE");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_FILE_PATH_AVAILABLE");
      }
      if (debugOnDemandSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_DEBUG_ON_DEMAND");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_DEBUG_ON_DEMAND");
      }
      if (startupSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_STARTUP");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_STARTUP");
      }
      if (processListStartupSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_PROCESS_LIST_STARTUP");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_PROCESS_LIST_STARTUP");
      }
      if (postMortemDebugMode()) {
         indent(printWriter);
         printWriter.println ("X  FCT_POST_MORTEM_DEBUG");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_POST_MORTEM_DEBUG");
      }
      if (postMortemCapableSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_POST_MORTEM_CAPABLE");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_POST_MORTEM_CAPABLE");
      }
      decreaseIndentLevel();
   }

   public void fileOptionsWrite(PrintWriter printWriter, byte detailLevel)
   {
      printWriter.println("File options");
      increaseIndentLevel();
      if (changeSourceFileSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_CHANGE_SOURCE_FILE");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_CHANGE_SOURCE_FILE");
      }
      if (restartSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_FILE_RESTART");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_FILE_RESTART");
      }

      if (moduleAddSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_MODULE_ADD");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_MODULE_ADD");
      }
      if (moduleRemoveSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_MODULE_REMOVE");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_MODULE_REMOVE");
      }

      if (processAttachSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_PROCESS_ATTACH");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_PROCESS_ATTACH");
      }
      if (processDetachSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_PROCESS_DETACH");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_PROCESS_DETACH");
      }
      if (processDetachKillSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_PROCESS_DETACH_KILL");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_PROCESS_DETACH_KILL");
      }
      if (processDetachKeepSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_PROCESS_DETACH_KEEP");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_PROCESS_DETACH_KEEP");
      }
      if (processDetachReleaseSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_PROCESS_DETACH_RELEASE");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_PROCESS_DETACH_RELEASE");
      }
      if (processAttachPathSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_PROCESS_ATTACH_PATH");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_PROCESS_ATTACH_PATH");
      }
      if (localSourceFilesSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_LOCAL_SOURCE_FILES");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_LOCAL_SOURCE_FILES");
      }
      decreaseIndentLevel();
   }

   public void storOptionsWrite(PrintWriter printWriter, byte detailLevel)
   {
      printWriter.println("Storage options");
      increaseIndentLevel();
      if (storageAddressFlatSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_STORAGE_ADDRESS_FLAT");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_STORAGE_ADDRESS_FLAT");
      }

      if (storageAddress1616Supported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_STORAGE_ADDRESS_1616");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_STORAGE_ADDRESS_1616");
      }

      if (storageAddressFlat1616Supported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_STORAGE_ADDRESS_FLAT_1616");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_STORAGE_ADDRESS_FLAT_1616");
      }

      if (storageContentHexCharSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_STORAGE_CONTENT_HEX_CHAR");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_STORAGE_CONTENT_HEX_CHAR");
      }

      if (storageContentCharSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_STORAGE_CONTENT_CHAR");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_STORAGE_CONTENT_CHAR");
      }

      if (storageContent16IntSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_STORAGE_CONTENT_16INT");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_STORAGE_CONTENT_16INT");
      }

      if (storageContent16UIntSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_STORAGE_CONTENT_16UINT");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_STORAGE_CONTENT_16UINT");
      }

      if (storageContent16IntHexSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_STORAGE_CONTENT_16INTHEX");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_STORAGE_CONTENT_16INTHEX");
      }

      if (storageContent32IntSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_STORAGE_CONTENT_32INT");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_STORAGE_CONTENT_32INT");
      }

      if (storageContent32UIntSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_STORAGE_CONTENT_32UINT");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_STORAGE_CONTENT_32UINT");
      }

      if (storageContent32IntHexSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_STORAGE_CONTENT_32INTHEX");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_STORAGE_CONTENT_32INTHEX");
      }

      if (storageContent32FloatSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_STORAGE_CONTENT_32FLOAT");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_STORAGE_CONTENT_32FLOAT");
      }

      if (storageContent64FloatSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_STORAGE_CONTENT_64FLOAT");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_STORAGE_CONTENT_64FLOAT");
      }

      if (storageContent88FloatSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_STORAGE_CONTENT_88FLOAT");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_STORAGE_CONTENT_88FLOAT");
      }

      if (storageContent16PtrSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_STORAGE_CONTENT_16PTR");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_STORAGE_CONTENT_16PTR");
      }

      if (storageContent1616PtrSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_STORAGE_CONTENT_1616PTR");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_STORAGE_CONTENT_1616PTR");
      }

      if (storageContent32PtrSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_STORAGE_CONTENT_32PTR");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_STORAGE_CONTENT_32PTR");
      }

      if (storageContentHexEBCDICSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_STORAGE_CONTENT_HEX_EBCDIC");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_STORAGE_CONTENT_HEX_EBCDIC");
      }

      if (storageContentEBCDICSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_STORAGE_CONTENT_EBCDIC");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_STORAGE_CONTENT_EBCDIC");
      }

      if (storageContentHexASCIISupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_STORAGE_CONTENT_HEX_ASCII");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_STORAGE_CONTENT_HEX_ASCII");
      }

      if (storageContentASCIISupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_STORAGE_CONTENT_ASCII");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_STORAGE_CONTENT_ASCII");
      }

      if (storageContentIEEE32Supported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_STORAGE_CONTENT_IEEE_32");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_STORAGE_CONTENT_IEEE_32");
      }

      if (storageContentIEEE64Supported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_STORAGE_CONTENT_IEEE_64");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_STORAGE_CONTENT_IEEE_64");
      }

      if (storageContent64IntSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_STORAGE_CONTENT_64INT");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_STORAGE_CONTENT_64INT");
      }

      if (storageContent64UIntSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_STORAGE_CONTENT_64UINT");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_STORAGE_CONTENT_64UINT");
      }

      if (storageContent64IntHexSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_STORAGE_CONTENT_64INTHEX");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_STORAGE_CONTENT_64INTHEX");
      }

      if (storageContent64PtrSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_STORAGE_CONTENT_64PTR");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_STORAGE_CONTENT_64PTR");
      }

      if (storageEnableDisableSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_STORAGE_ENABLE_TOGGLE");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_STORAGE_ENABLE_TOGGLE");
      }

      if (storageExprEnableDisableSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_STORAGE_EXPR_ENABLE_TOGGLE");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_STORAGE_EXPR_ENABLE_TOGGLE");
      }
      decreaseIndentLevel();
   }

   public void bkpOptionsWrite(PrintWriter printWriter, byte detailLevel)
   {
      printWriter.println("Breakpoint options");
      increaseIndentLevel();
      if (lineBreakpointsSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_LINE_BREAKPOINT");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_LINE_BREAKPOINT");
      }

      if (statementBreakpointSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_STATEMENT_BREAKPOINT");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_STATEMENT_BREAKPOINT");
      }

      if (functionBreakpointsSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_FUNCTION_BREAKPOINT");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_FUNCTION_BREAKPOINT");
      }

      if (addressBreakpointsSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_ADDRESS_BREAKPOINT");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_ADDRESS_BREAKPOINT");
      }

      if (watchpointsSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_CHANGE_ADDRESS_BREAKPOINT");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_CHANGE_ADDRESS_BREAKPOINT");
      }

      if (moduleLoadBreakpointsSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_LOAD_BREAKPOINT");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_LOAD_BREAKPOINT");
      }

      if (eventBreakpointSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_EVENT_BREAKPOINT");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_EVENT_BREAKPOINT");
      }

      if (breakpointEnableDisableSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_BREAKPOINT_ENABLE_TOGGLE");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_BREAKPOINT_ENABLE_TOGGLE");
      }

      if (breakpointModifySupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_BREAKPOINT_MODIFY");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_BREAKPOINT_MODIFY");
      }

      if (deferredBreakpointsSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_BREAKPOINT_DEFERRED");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_BREAKPOINT_DEFERRED");
      }

      if (entryBreakpointsAutoSetSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_BREAKPOINT_ENTRY_AUTOSET");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_BREAKPOINT_ENTRY_AUTOSET");
      }

      if (conditionalBreakpointsSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_BREAKPOINT_EXPRESSION");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_BREAKPOINT_EXPRESSION");
      }

      if (monitor8BytesSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_BREAKPOINT_MONITOR_8BYTES");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_BREAKPOINT_MONITOR_8BYTES");
      }

      if (monitor4BytesSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_BREAKPOINT_MONITOR_4BYTES");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_BREAKPOINT_MONITOR_4BYTES");
      }

      if (monitor2BytesSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_BREAKPOINT_MONITOR_2BYTES");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_BREAKPOINT_MONITOR_2BYTES");
      }

      if (monitor1BytesSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_BREAKPOINT_MONITOR_1BYTES");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_BREAKPOINT_MONITOR_1BYTES");
      }

      if (monitor0_128BytesSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_BREAKPOINT_MONITOR_0_128");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_BREAKPOINT_MONITOR_0_128");
      }

      if (dateBreakpointsSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_BREAKPOINT_DATE");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_BREAKPOINT_DATE");
      }
      decreaseIndentLevel();
   }

   public void monitOptionsWrite(PrintWriter printWriter, byte detailLevel)
   {
      printWriter.println("Monitor options");
      increaseIndentLevel();
      if (monitorEnableDisableSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_MONITOR_ENABLE_TOGGLE");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_MONITOR_ENABLE_TOGGLE");
      }
      decreaseIndentLevel();
   }

   public void winOptionsWrite(PrintWriter printWriter, byte detailLevel)
   {
      printWriter.println("Window options");
      increaseIndentLevel();
      if (monitorLocalVariablesSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_LOCAL_VARIABLES");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_LOCAL_VARIABLES");
      }

      if (monitorRegistersSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_REGISTERS");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_REGISTERS");
      }

      if (monitorStackSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_STACK");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_STACK");
      }

      if (monitorStorageSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_STORAGE");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_STORAGE");
      }

      if (monitorWindowAnalysisSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_WINDOW_ANALYSIS");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_WINDOW_ANALYSIS");
      }

      if (monitorMessageQueueSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_MESSAGE_QUEUE_MONITOR");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_MESSAGE_QUEUE_MONITOR");
      }

      if (monitorInheritanceViewSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_INHERITANCE_VIEW");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_INHERITANCE_VIEW");
      }

      if (monitorCommandLogSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_COMMAND_LOG");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_COMMAND_LOG");
      }

      if (monitorPassThruSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_PASSTHRU");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_PASSTHRU");
      }
      decreaseIndentLevel();
   }

   public void runOptionsWrite(PrintWriter printWriter, byte detailLevel)
   {
      printWriter.println("Run options");
      increaseIndentLevel();
      if (threadFreezeThawSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_THREAD_ENABLED");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_THREAD_ENABLED");
      }

      if (stepOverSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_STEP_OVER");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_STEP_OVER");
      }

      if (stepIntoSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_STEP_INTO");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_STEP_INTO");
      }

      if (stepDebugSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_STEP_DEBUG");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_STEP_DEBUG");
      }

      if (stepReturnSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_STEP_RETURN");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_STEP_RETURN");
      }

      if (runToLocationSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_RUN_TO_LOCATION");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_RUN_TO_LOCATION");
      }

      if (jumpToLocationSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_JUMP_TO_LOCATION");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_JUMP_TO_LOCATION");
      }

      if (haltSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_HALT");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_HALT");
      }

      if (storageUsageCheckSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_STORAGE_USAGE_CHECK");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_STORAGE_USAGE_CHECK");
      }
      decreaseIndentLevel();
   }

   public void excepOptionsWrite(PrintWriter printWriter, byte detailLevel)
   {
      printWriter.println("Exception options");
      increaseIndentLevel();
      if (exceptionFilterSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_EXCEPTION_FILTER");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_EXCEPTION_FILTER");
      }

      if (exceptionExamineSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_EXCEPTION_EXAMINE");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_EXCEPTION_EXAMINE");
      }

      if (exceptionStepSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_EXCEPTION_STEP");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_EXCEPTION_STEP");
      }

      if (exceptionRunSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_EXCEPTION_RUN");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_EXCEPTION_RUN");
      }
      decreaseIndentLevel();
   }

   public void stackOptionsWrite(PrintWriter printWriter, byte detailLevel)
   {
      printWriter.println("Stack options");
      increaseIndentLevel();
      if (stackRemainingSizeSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_STACK_REMAINING_SZE");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_STACK_REMAINING_SZE");
      }

      if (stackSetBreakpointSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_STACK_SET_BREAKPOINT");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_STACK_SET_BREAKPOINT");
      }

      if (stackOpenStorageSupported()) {
         indent(printWriter);
         printWriter.println ("X  FCT_STACK_OPEN_STORAGE");
      }
      else if (detailLevel==DETAIL_LEVEL_HIGH) {
         indent(printWriter);
         printWriter.println ("   FCT_STACK_OPEN_STORAGE");
      }
      decreaseIndentLevel();
   }

   public void write(PrintWriter printWriter)
   {
     printWriter.println("'X' indicates option is enabled.");

     if (getDetailLevel() == DETAIL_LEVEL_MEDIUM) {
        printWriter.println("Only enabled FCT bits are shown.");
        printWriter.println();
        startupWrite(printWriter, DETAIL_LEVEL_MEDIUM);
        printWriter.println();
        genFunctionsWrite(printWriter, DETAIL_LEVEL_MEDIUM);
        printWriter.println();
        fileOptionsWrite(printWriter, DETAIL_LEVEL_MEDIUM);
        printWriter.println();
        storOptionsWrite(printWriter, DETAIL_LEVEL_MEDIUM);
        printWriter.println();
        bkpOptionsWrite(printWriter, DETAIL_LEVEL_MEDIUM);
        printWriter.println();
        monitOptionsWrite(printWriter, DETAIL_LEVEL_MEDIUM);
        printWriter.println();
        winOptionsWrite(printWriter, DETAIL_LEVEL_MEDIUM);
        printWriter.println();
        runOptionsWrite(printWriter, DETAIL_LEVEL_MEDIUM);
        printWriter.println();
        excepOptionsWrite(printWriter, DETAIL_LEVEL_MEDIUM);
        printWriter.println();
        stackOptionsWrite(printWriter, DETAIL_LEVEL_MEDIUM);
     }
     if (getDetailLevel() >= DETAIL_LEVEL_HIGH) {
        printWriter.println("All FCT bits are shown.");
        printWriter.println();
        startupWrite(printWriter, DETAIL_LEVEL_HIGH);
        printWriter.println();
        genFunctionsWrite(printWriter, DETAIL_LEVEL_HIGH);
        printWriter.println();
        fileOptionsWrite(printWriter, DETAIL_LEVEL_HIGH);
        printWriter.println();
        storOptionsWrite(printWriter, DETAIL_LEVEL_HIGH);
        printWriter.println();
        bkpOptionsWrite(printWriter, DETAIL_LEVEL_HIGH);
        printWriter.println();
        monitOptionsWrite(printWriter, DETAIL_LEVEL_HIGH);
        printWriter.println();
        winOptionsWrite(printWriter, DETAIL_LEVEL_HIGH);
        printWriter.println();
        runOptionsWrite(printWriter, DETAIL_LEVEL_HIGH);
        printWriter.println();
        excepOptionsWrite(printWriter, DETAIL_LEVEL_HIGH);
        printWriter.println();
        stackOptionsWrite(printWriter, DETAIL_LEVEL_HIGH);
     }
   }

   private int _startup;
   private int _general_functions;
   private int _file_options;
   private int _storage_options;
   private int _breakpoint_options;
   private int _monitor_options;
   private int _windows_options;
   private int _run_options;
   private int _exception_options;
   private int _stack_options;

   private static final int _fixed_length = 40;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}