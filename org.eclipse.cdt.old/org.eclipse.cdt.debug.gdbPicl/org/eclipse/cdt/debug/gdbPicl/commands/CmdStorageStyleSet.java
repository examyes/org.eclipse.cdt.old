/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// com/ibm/debug/gdb/commands/CmdStorageStyleSet.java, gdb, java-dev
// Version 1.1 (last modified 5/24/01 13:45:00)   (based on Jde 11/2/97 1.12)
///////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

import com.ibm.debug.epdc.*;

/**
 * Process execute command
 */
public class CmdStorageStyleSet extends Command
{
   public CmdStorageStyleSet(DebugSession debugSession, EReqStorageStyleSet req)
   {
      super(debugSession);
      _req = req;
   }

   /**
    * Execute program as specified
    */

   public boolean execute(EPDC_EngineSession EPDCSession)
   {
      short _id               = _req.getID();
      int _addressStyle       = _req.getAddressStyle();
      int _unitStyle          = _req.getUnitStyle();
      int _styleUnitCount     = _req.getStyleUnitCount();
      String _baseAddress     = _req.getBaseAddress();
      int _lineOffset         = _req.getLineOffset();
      int _unitOffset        = _req.getUnitOffset();
      if (Gdb.traceLogger.ERR) 
          Gdb.traceLogger.err(1,"######## CmdStorageStyleSet.execute UNIMPLEMENTED id="+_id +" addressStyle="+_addressStyle
                   +" _unitStyle="+_unitStyle+" _styleUnitCount="+_styleUnitCount
                   +" baseAddress="+_baseAddress+" lineOffset="+_lineOffset+" unitOffset="+_unitOffset );

      _rep = new ERepStorageStyleSet();

      return false;
   }

   // Class fields
   private EReqStorageStyleSet _req;
}

