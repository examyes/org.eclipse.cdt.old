/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// com/ibm/debug/gdb/commands/CmdPartOpen.java, gdb, java-dev
// Version 1.1 (last modified 5/24/01 13:44:39)   (based on Jde 1.13.1.11 2/8/01)
///////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

import java.io.*;
import java.util.*;
import com.ibm.debug.epdc.*;

/**
 * Process part open request
 */
public class CmdPartOpen extends Command
{
   public CmdPartOpen(DebugSession debugSession, EReqPartOpen req)
   {
      super(debugSession);
      _moduleID = req.moduleID();

      String partNameString;
      try {
         _partFileName = req.partFileName();

         if (_partFileName == null)
            _partFileName = "";


      } catch (IOException ioe) {
         Gdb.handleException(ioe);
      }
   }

   /**
    * Searches for loaded classes which satisfy the module ID and part file name given
    */
   public boolean execute(EPDC_EngineSession EPDCSession) 
   {
      ModuleManager cm = _debugSession.getModuleManager();
      String moduleName = "";

      _rep = new ERepPartOpen();
      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(1,"========>>>>>>>> CmdPartOpen partFileName="+_partFileName + " in module " + Integer.toString(_moduleID));

      // if no search criteria given, return nothing
      if ((_moduleID == 0) && _partFileName.equals(""))
        return false;

      // if part file name is empty, return all parts in the give module
      if (_partFileName.equals(""))  {
         ((ERepPartOpen)_rep).addPartIDs(cm.getPartIDs(_moduleID));
         return false;
      }

      // if the module ID and part name are given, serach for the part in that module only
      if (_moduleID != 0) 
      {
            cm.checkPart(_moduleID, _partFileName);
            int partID = cm.getPartID(_moduleID, _partFileName);
            if (partID > 0)
            {
               ((ERepPartOpen) _rep).addPartID(partID);
            }
            else 
            {
/////////////////////////// start NT-LINUX HACK //////////////////////////////////
               String NTname = _partFileName;
               if (NTname.endsWith(".obj"))
               {   int i = NTname.indexOf(".obj");
                   NTname = NTname.substring(0,i)+".c";
                   cm.checkPart(_moduleID, NTname);
                   partID = cm.getPartID(_moduleID, NTname);
                   if (partID > 0)
                   {
                      ((ERepPartOpen) _rep).addPartID(partID);
                      if (Gdb.traceLogger.ERR) 
                          Gdb.traceLogger.err(2,"######## HACK: CmdPartOpen _moduleID="+_moduleID+" mapping WindowsName="+_partFileName+" -> LinuxName="+NTname+" \n");
                      return  false;
                   }
               }
/////////////////////////// end NT-LINUX HACK //////////////////////////////////

               _rep.setMessage(_debugSession.getResourceString("CLASS_NOT_FOUND_MSG")+_partFileName);
               return false;
            }
      }
      else  // moduleID==0
      {
         // The user has typed in only the part name and no moduleName
         // We need to search all known modules for this part.
         ((ERepPartOpen)_rep).addPartIDs(cm.getPartIDs(_partFileName));

         if (((ERepPartOpen) _rep).numPartIDs() == 0) 
         {

//  we dont have a check-and-if-found-then-add-part ////////////////
//            for (int i=1; i<=cm.numModules(); i++) 
//            {
//               moduleName = cm.getModuleName(i);
//               int moduleID = cm.getModuleID(moduleName);
//                  cm.checkPart(moduleID, _partFileName);
//            }

            Vector partIDs = cm.getPartIDs(_partFileName);
            ((ERepPartOpen)_rep).addPartIDs(partIDs);
   
            // if still no part matching the name is found, 
            if (partIDs.size() == 0) 
            {

/////////////////////////// start NT-LINUX HACK //////////////////////////////////
               String NTname = _partFileName;
               if (NTname.endsWith(".obj"))
               {   int i = NTname.indexOf(".obj");
                   NTname = NTname.substring(0,i)+".c";
               }

//  we dont have a check-and-if-found-then-add-part ////////////////
//            for (int i=1; i<=cm.numModules(); i++) 
//            {
//               moduleName = cm.getModuleName(i);
//               int moduleID = cm.getModuleID(moduleName);
//                  cm.checkPart(moduleID, _partFileName);
//            }

               partIDs = cm.getPartIDs(NTname);
               if (partIDs.size() > 0) 
               {
                  if (Gdb.traceLogger.ERR) 
                      Gdb.traceLogger.err(2,"######## HACK: CmdPartOpen _moduleID="+_moduleID+" mapping WindowsName="+_partFileName+" -> LinuxName="+NTname +"\n");
                  ((ERepPartOpen)_rep).addPartIDs(partIDs);
                  return  false;
               }
/////////////////////////// end NT-LINUX HACK //////////////////////////////////

                  _rep.setMessage(_debugSession.getResourceString("CLASS_NOT_FOUND_MSG")+_partFileName);
                  return false;
            }
         }
      }
      return false;
   }

   // data fields
   private int _moduleID;
   private String _partFileName;
}
