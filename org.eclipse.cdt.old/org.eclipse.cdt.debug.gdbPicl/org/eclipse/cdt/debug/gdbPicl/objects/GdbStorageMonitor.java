/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// %W%
// Version %I% (last modified %G% %U%)   (based on Jde 1.42 3/5/01)
/////////////////////////////////////////////////////////////////////////

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
