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
public class CmdRepForTypeSet extends Command
{
   public CmdRepForTypeSet(DebugSession debugSession, EReqRepForTypeSet req)
   {
      super(debugSession);
      _req = req;
   }

   /**
    * Execute program as specified
    */

   public boolean execute(EPDC_EngineSession EPDCSession)
   {
      // !!! Do we need to check the language id?  We only support one.
      // _req.languageId();

      _typeIndex = _req.typeIndex();
      _defRep = _req.rep();

      // NOTE: SUI Passes us defRep index based at 0 but we expect 1
      EPDCSession._repInfo.setDefaultRepresentation(_typeIndex,(short)_defRep);

      _rep = new ERepRepForTypeSet();
      return false;
   }

   // Class fields
   private EReqRepForTypeSet _req;
   private int _typeIndex;
   private int _defRep;
}

