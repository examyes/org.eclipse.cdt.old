/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.commands;
import org.eclipse.cdt.debug.gdbPicl.*;
import org.eclipse.cdt.debug.gdbPicl.objects.*;

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
          Gdb.traceLogger.err(2,"######## CmdStorageStyleSet.execute UNIMPLEMENTED id="+_id +" addressStyle="+_addressStyle
                   +" _unitStyle="+_unitStyle+" _styleUnitCount="+_styleUnitCount
                   +" baseAddress="+_baseAddress+" lineOffset="+_lineOffset+" unitOffset="+_unitOffset );

      _rep = new ERepStorageStyleSet();

      return false;
   }

   // Class fields
   private EReqStorageStyleSet _req;
}

