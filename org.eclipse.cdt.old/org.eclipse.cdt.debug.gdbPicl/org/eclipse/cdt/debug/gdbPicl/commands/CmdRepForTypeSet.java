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
// Version %I% (last modified %G% %U%)   (based on Jde 11/2/97 1.12)
///////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

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

