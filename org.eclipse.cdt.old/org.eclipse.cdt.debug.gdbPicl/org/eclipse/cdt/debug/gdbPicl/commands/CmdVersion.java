/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// com/ibm/debug/gdb/commands/CmdVersion.java, gdb, java-dev
// Version 1.1 (last modified 5/24/01 13:45:10)   (based on Jde 11/2/97 1.12)
///////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

import com.ibm.debug.epdc.*;

/**
 * Process execute command
 */
public class CmdVersion extends Command
{
   public CmdVersion(DebugSession debugSession, EReqVersion req)
   {
      super(debugSession);
      _req = req;
   }

   /**
    * Execute program as specified
    */

   public boolean execute(EPDC_EngineSession EPDCSession)
   {
      int front_end_version = _req.getFrontEndVersion();

      // Default to this engine's version before checking against the frontend's version

      _rep = new ERepVersion(DebugEngine.MAX_SUPPORTED_EPDC_VERSION);

      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(1,"Frontend version = " + front_end_version);
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(1,"Max backend version = " + DebugEngine.MAX_SUPPORTED_EPDC_VERSION);
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(1,"Min backend version = " + DebugEngine.MIN_SUPPORTED_EPDC_VERSION);


      // negotiate the EPDC version

      // Case 1 : Model newer than this engine
      //   - send back this engine's version and let the Model determine if it can handle the
      //     lower EPDC version

      if (front_end_version > DebugEngine.MAX_SUPPORTED_EPDC_VERSION)
      {

         EPDCSession._negotiatedEPDCVersion = DebugEngine.MAX_SUPPORTED_EPDC_VERSION;
         if (Gdb.traceLogger.DBG) 
             Gdb.traceLogger.dbg(1,"Model newer set negotiated version to " + EPDCSession._negotiatedEPDCVersion);
         ((ERepVersion)_rep).setVersion(EPDCSession._negotiatedEPDCVersion);
         _rep.setReturnCode(EPDC.ExecRc_OK);
      }
      else
         // Case 2 : Model and Engine match
         if (front_end_version == DebugEngine.MAX_SUPPORTED_EPDC_VERSION)
         {
            EPDCSession._negotiatedEPDCVersion = DebugEngine.MAX_SUPPORTED_EPDC_VERSION;
            if (Gdb.traceLogger.DBG) 
                Gdb.traceLogger.dbg(1,"Model and Engine match: negotiated version is " + EPDCSession._negotiatedEPDCVersion);
            ((ERepVersion)_rep).setVersion(EPDCSession._negotiatedEPDCVersion);
            _rep.setReturnCode(EPDC.ExecRc_OK);
         }
         else
            // Case 3 : Model older than Engine
            // Check if Model's version is supported by this engine
            // If it is then return the Model's level as the negotiated EPDC version
            if (front_end_version >= DebugEngine.MIN_SUPPORTED_EPDC_VERSION)
            {
               EPDCSession._negotiatedEPDCVersion = front_end_version;
               if (Gdb.traceLogger.DBG) 
                   Gdb.traceLogger.dbg(1,"Model older than Engine: negotiated version is " + EPDCSession._negotiatedEPDCVersion);
               ((ERepVersion)_rep).setVersion(EPDCSession._negotiatedEPDCVersion);
               _rep.setReturnCode(EPDC.ExecRc_OK);
            }
            else
            {
               // Case 4 : versions are not compatible therefore send error message
               _rep.setReturnCode(EPDC.ExecRc_Error);
               _rep.setMessage(_debugSession.getResourceString("FRONT_END_INCOMPATIBLE_MSG"));
            }


      return false;
   }

   // Class fields
   private EReqVersion _req;
}

