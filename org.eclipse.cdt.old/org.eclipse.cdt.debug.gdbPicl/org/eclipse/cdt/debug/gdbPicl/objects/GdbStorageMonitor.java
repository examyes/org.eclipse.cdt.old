//////////////////////////////////////////////////////////////////////////////////////////////////////////
//Copyright (C)  1995, 2001 International Business Machines Corporation and others. All Rights Reserved.
//////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.objects;
import  com.ibm.debug.gdbPicl.*;

import com.ibm.debug.epdc.*;
import java.util.*;
import java.text.*;

/**
 * StorageMonitor.
 */
public class GdbStorageMonitor extends StorageMonitor
{
   public GdbStorageMonitor(DebugSession debugSession, short ID, String totalColumns, int columnsPerLine, String mode, 
                                                    String wordSize, int charsPerColumn, 
                                                    int startLine, int endLine, String startAddress, String baseAddress, EStdExpression2 expr )
   {
      super(debugSession, ID, totalColumns, columnsPerLine ,mode, wordSize, charsPerColumn, startLine, endLine, startAddress, baseAddress, expr);
   }


}
