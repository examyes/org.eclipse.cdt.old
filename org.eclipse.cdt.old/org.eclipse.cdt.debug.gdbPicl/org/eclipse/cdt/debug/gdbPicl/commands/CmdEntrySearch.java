/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// com/ibm/debug/gdb/commands/CmdEntrySearch.java, gdb, java-dev
// Version 1.1 (last modified 5/24/01 13:44:25)   (based on Jde 3/9/01 1.16)
/////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

import java.io.*;
import java.util.*;
import com.ibm.debug.epdc.*;

/**
 * Process entry search command
 */
public class CmdEntrySearch extends Command
{
   public CmdEntrySearch(DebugSession debugSession, EReqEntrySearch req)
   {
      super(debugSession);
      _req = req;
   }

   /**
    * Find and return a list of entries which fit the criteria given.
    */
   public boolean execute(EPDC_EngineSession EPDCSession)
   {
      String entryName = null;
      _rep = new ERepEntrySearch();
      ModuleManager cm = _debugSession.getModuleManager();

      int partID = _req.partID();
      int moduleID = _req.partID();  //GDB

      try
      {
         entryName = _req.entryName();
      }
      catch (IOException ioe)
      {
         Gdb.handleException(ioe);
      }

      int entryID = _req.entryID();

      // if an entry ID is given, return only that entry
      if (_req.entryID() != 0)
      {
         ((ERepEntrySearch)_rep).addEntry(cm.getEntry(_req.entryID()));
         return false;
      }

      if (Gdb.traceLogger.ERR) 
          Gdb.traceLogger.err(1,"######## TEMP_DEBUG: CmdEntrySearch partid="+partID+" entryName="+entryName );

      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(1,"partid is "+partID);
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(1,"entryName is "+entryName);

      // if no information, don't add any entries
      if ((partID == 0) && (entryName == null || entryName.equals("")))
         return false;

      boolean caseSensitive = _req.caseSensitive();

      // If the part ID is 0, we search only loaded classes.
      if (partID == 0) 
      {
         String[] parts = _debugSession.getPartsList();

         for (int i=0;i<parts.length;i++)
         {
            String partName = parts[i];

            if (partName != null)
            {
	      ((ERepEntrySearch)_rep).addEntries(
//			       cm.getEntries(cm.getPartID(partName), 
			       cm.getEntries(((GdbModuleManager)cm).getPartID(moduleID,partName), 

					     entryName, 
					     caseSensitive));
            }
         }
         if (((ERepEntrySearch)_rep).entries().size() == 0)
         {
           if( entryName!=null && !entryName.equals("") )
           {
           _rep.setMessage(_debugSession.getResourceString("METHOD_NOT_FOUND_MSG"));
           _rep.setReturnCode(EPDC.ExecRc_FindFailed);
           }
         }
         return false;
      }

      // if a part ID is given, search only that part for entryName
//      ((ERepEntrySearch)_rep).addEntries(      cm.getEntries(partID, entryName, caseSensitive));

      ERepEntryGetNext[] entries = ((GdbModuleManager)cm).getEntries(partID, entryName, caseSensitive);
      if(entries==null)
      {
         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(2,"CmdEntrySearch.execute entries==NULL partID="+partID+" entryName="+entryName+" caseSensitive="+caseSensitive );
      }else             
         ((ERepEntrySearch)_rep).addEntries(((GdbModuleManager)cm).getEntries(partID, entryName, caseSensitive)); //GDB


      if (((ERepEntrySearch)_rep).entries().size() == 0)
      {
         if( entryName!=null && !entryName.equals("") )
         {
        _rep.setMessage(_debugSession.getResourceString("METHOD_NOT_FOUND_MSG"));
        _rep.setReturnCode(EPDC.ExecRc_FindFailed);
         }
      }

      return false;
   }

   // data fields
   private EReqEntrySearch _req;
}
