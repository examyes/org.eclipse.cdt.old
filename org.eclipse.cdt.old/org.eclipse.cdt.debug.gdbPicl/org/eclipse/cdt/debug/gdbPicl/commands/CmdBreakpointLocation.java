/*
 * Copyright (c) 1995, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */


package org.eclipse.cdt.debug.gdbPicl.commands;
import org.eclipse.cdt.debug.gdbPicl.*;
import org.eclipse.cdt.debug.gdbPicl.objects.*;

import java.io.*;
import com.ibm.debug.epdc.*;

/**
 * Process location breakpoint request
 */
public class CmdBreakpointLocation extends Command
{
   public CmdBreakpointLocation(DebugSession debugSession, EReqBreakpointLocation req)
   {
      super(debugSession);
      _req = req;
   }

   /**
    * Processes command and updates breakpoints as necessary
    */
   public boolean execute(EPDC_EngineSession EPDCSession)
   {
      BreakpointManager bm = _debugSession.getBreakpointManager();
      ModuleManager      cm = _debugSession.getModuleManager();

      EStdView bkpContext = _req.bkpContext();

      int srcFileIndex = bkpContext.getSrcFileIndex();
      int viewNum      = Part.VIEW_SOURCE;
      int partID       = bkpContext.getPPID();
      int lineNum      = bkpContext.getLineNum();
//    srcFileIndex = bkpContext.getSrcFileIndex();


      int ret = 0;

      String moduleName;
      String partName;
      String methodName = "<unknown>";
      String sourceName;

      boolean isDeferred = (_req.bkpAttr() & EPDC.BkpDefer) != 0;


      boolean isEnabled = ((_req.bkpAttr() & EPDC.BkpEnable) == EPDC.BkpEnable) ? true : false;

      try
      {
         moduleName    = _req.DLLName();
         partName      = _req.sourceName();
         sourceName    = _req.includeName();
         /////////////////////////// start NT-LINUX HACK //////////////////////////////////
         if (partName!=null && partName.endsWith(".obj"))
         {   int i = partName.indexOf(".obj");
             String NTname = partName.substring(0,i)+".c";
             if (Gdb.traceLogger.ERR) 
                 Gdb.traceLogger.err(1,"######## HACK: CmdBreakpointLocation.execute mapping WindowsName="+partName+" -> LinuxName="+NTname +"\n");
             partName = NTname;
         }
         /////////////////////////// end NT-LINUX HACK //////////////////////////////////

         if (Gdb.traceLogger.EVT) 
             Gdb.traceLogger.evt(2,"CmdBreakpointLocation.execute DLLName="+moduleName+" sourceName="+sourceName+" partName="+partName +" moduleName="+moduleName+" lineNum="+lineNum );


         //////////////////////// start NT-LINUX HACK /////////////////////////////////////
         if(sourceName!=null&&partName!=null && !sourceName.equals(partName) && sourceName.equalsIgnoreCase(partName) )
         {
            if (Gdb.traceLogger.ERR) 
                Gdb.traceLogger.err(1,"######## HACK: CmdBreakpointLocation.execute LOWER_CASE partName="+partName+" -> SourceName="+sourceName+" (DLLName="+moduleName+ ") \n" );
            partName = sourceName;
         }
         else if(sourceName==null&&partName!=null &&partID>0)
         {
             String tempPartName = ((GdbModuleManager)cm).getPartName(partID);
             if(!partName.equals(tempPartName) && partName.equalsIgnoreCase(tempPartName))
             {
                  if (Gdb.traceLogger.ERR) 
                      Gdb.traceLogger.err(1,"######## HACK: CmdBreakpointLocation.execute LOWER_CASE partName="+partName+" -> partID.getName="+tempPartName+"\n" );
                  partName = tempPartName;
             }
         }else if(sourceName==null&&partName!=null)
         {
            int moduleID = ((GdbModuleManager)cm).getModuleID(moduleName);
            int[] partIDs = ((GdbModuleManager)cm).getPartIDs(moduleID);
            if(partIDs!=null && partIDs.length>0)
            {
               for(int i=0; i<partIDs.length; i++)
               {
                   String tempPartName = ((GdbModuleManager)cm).getPartName(partIDs[i]);
                   if(partName.equalsIgnoreCase(tempPartName) )
                   {
                        if (Gdb.traceLogger.ERR) 
                            Gdb.traceLogger.err(1,"######## HACK: CmdBreakpointLocation.execute LOWER_CASE partName="+partName+" -> module.partID.getName="+tempPartName+"\n" );
                        partName = tempPartName;
                        break;
                   }
               }
            }
         }
         //////////////////////// end NT-LINUX HACK /////////////////////////////////////
      }
      catch (IOException ioe)
      {
         moduleName    = "<unknown>";
         partName      = "<unknown>";
         sourceName    = "<unknown>";
         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(2,"CmdBreakpointLocation.execute IOException="+ioe.getMessage() );
      }

      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(1,"Module: " + moduleName);
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(1,"Part  : " + partName);

      _rep = new ERepBreakpointLocation();

      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(1,"CmdBreakpointLocation.execute deferred="+isDeferred+" bkpType="+_req.bkpType() );
      try
      {
         switch(_req.bkpType() )
         {
            // ########### LINE BREAKPOINT ####################################
            case EPDC.LineBkpType:
              if (isDeferred)     
              {
                if (_req.bkpAction() == EPDC.ReplaceBkp)   // UNIMPLEMENTED ########################
                  {
                      ret = bm.modifyDeferredLineBreakpoint(_req.bkpID(), moduleName, partName, sourceName, _req.bkpAttr(), bkpContext.getLineNum(), _req.getConditionalExpression());
                      if (ret > 0)
                      {
                          _rep.setReturnCode(EPDC.ExecRc_BadBrkAction);
                          _rep.setMessage(_debugSession.getResourceString("CANNOT_SET_BREAKPOINT_DEFERRED_MSG"));
                      }
                  }
                  else // _req.bkpAction() == EPDC.SetBkp
                  {
                     if (Gdb.traceLogger.EVT) 
                         Gdb.traceLogger.evt(2,"CmdBreakpointLocation.execute ABOUT TO SET_DEFERRED_LINEBREAKPOINT moduleName="+moduleName+" partName="+partName+" sourceName="+sourceName );
                     ret = bm.setDeferredLineBreakpoint(moduleName, partName, sourceName, _req.bkpAttr(), bkpContext.getLineNum(), isEnabled, _req.getConditionalExpression());

                     if (ret > 0)
                     {
                         _rep.setReturnCode(EPDC.ExecRc_DupBrkPt);
                         _rep.setMessage(_debugSession.getResourceString("DUPLICATE_BREAKPOINT_LOCATION_MSG"));
                         ((ERepBreakpointLocation) _rep).setDuplicateBkpID(ret);
                     }
                  }
                  return false;
              }
              else       // non-deferred
              {
                 partID = bkpContext.getPPID();
                 if (partID == 0)
                 {
                     int moduleID = cm.getModuleID(moduleName);
                     if (Gdb.traceLogger.DBG) 
                         Gdb.traceLogger.dbg(1,"ModuleID: " + moduleID);
                     partID = ((GdbModuleManager)cm).getPartID(moduleID,partName);
                 }
                 if (Gdb.traceLogger.DBG) 
                     Gdb.traceLogger.dbg(1,"PartID  : " + partID);

                 lineNum = bkpContext.getLineNum();
                 
                 if(partID<=0)
                 {
                     Part part = _debugSession.isPartInModule(partName, moduleName);

                     if(part!=null)
                     {  int moduleID = cm.getModuleID(moduleName);
                        partID = ((GdbModuleManager)cm).getPartID(moduleID,partName);
                       if (Gdb.traceLogger.EVT) 
                           Gdb.traceLogger.evt(1,"CmdExpression.execute added part for breakpoint moduleName="+moduleName+" partName="+partName+" partID="+partID+" srcFileIndex="+srcFileIndex+" lineNum="+lineNum );
                     }
                     else
                     {
                     	 // don't have a valid part id... try to set with just partName and line number
                     	 ret = bm.setLineBreakpoint(sourceName, lineNum, ((_req.bkpAttr() & EPDC.BkpEnable) == EPDC.BkpEnable) ? true : false, _req.getConditionalExpression());
						 
						 if (ret < 0)
						 {
						 	// should set deferred breakpoint when it is implemented
						 	_rep.setReturnCode(EPDC.ExecRc_BadLineNum);
	                        _rep.setMessage(_debugSession.getResourceString("INVALID_BREAKPOINT_LOCATION_MSG")+lineNum );
						 }
						 
                         return false;
                     }
                 }
                 if (Gdb.traceLogger.DBG) 
                     Gdb.traceLogger.dbg(1,"Attempting to set breakpoint in part " + Integer.toString(partID) + " at line " + Integer.toString(lineNum));

                 if (_req.bkpAction() == EPDC.ReplaceBkp)
                    ret = bm.modifyLineBreakpoint(_req.bkpID(), partID, srcFileIndex, viewNum, lineNum, ((_req.bkpAttr() & EPDC.BkpEnable) == EPDC.BkpEnable) ? true : false, _req.getConditionalExpression());
                 else
                 {
                     if (Gdb.traceLogger.EVT) 
                         Gdb.traceLogger.evt(2,"CmdBreakpointLocation.execute about to SET_LINEBREAKPOINT partID="+partID+" srcFileIndex="+srcFileIndex+" lineNum="+lineNum );
                    ret = bm.setLineBreakpoint(partID, srcFileIndex, viewNum, lineNum, ((_req.bkpAttr() & EPDC.BkpEnable) == EPDC.BkpEnable) ? true : false, _req.getConditionalExpression());
                 }
                 switch(ret)
                 {
                    case -1:
                       _rep.setReturnCode(EPDC.ExecRc_BadLineNum);
                       _rep.setMessage(
                          _debugSession.getResourceString("INVALID_BREAKPOINT_LOCATION_MSG")+lineNum );
                       return false;
                    case 0:
                       return false;
                    default:
                       // if a new breakpoint was to be added at a location 
                       // already with a breakpoint return an error
                       if (_req.bkpAction() == EPDC.SetBkp)
                       {
                          _rep.setReturnCode(EPDC.ExecRc_DupBrkPt);
                          _rep.setMessage(_debugSession.getResourceString("DUPLICATE_BREAKPOINT_LOCATION_MSG"));
                          ((ERepBreakpointLocation) _rep).setDuplicateBkpID(ret);
                       }
                       return false;
                 }
              }
            // ########### METHOD BREAKPOINT ####################################
            case EPDC.EntryBkpType:
              if (isDeferred)          // UNIMPLEMENTED ########################
              {
                  if (_req.bkpAction() == EPDC.ReplaceBkp)
                  {
                      ret = bm.modifyDeferredMethodBreakpoint(_req.bkpID(), moduleName, partName, _req.bkpVarInfo(), _req.bkpAttr(), _req.getConditionalExpression());
                      if (ret > 0)
                      {
                          _rep.setReturnCode(EPDC.ExecRc_BadBrkAction);
                          _rep.setMessage(_debugSession.getResourceString("CANNOT_SET_BREAKPOINT_DEFERRED_MSG"));
                      }
                  }
                  else // _req.bkpAction() == EPDC.SetBkp
                  {
                     ret = bm.setDeferredMethodBreakpoint(moduleName, partName, _req.bkpVarInfo(), _req.bkpAttr(), isEnabled, _req.getConditionalExpression());
                     if (ret > 0)
                     {
                         _rep.setReturnCode(EPDC.ExecRc_DupBrkPt);
                         _rep.setMessage(_debugSession.getResourceString("DUPLICATE_BREAKPOINT_LOCATION_MSG"));
                         ((ERepBreakpointLocation) _rep).setDuplicateBkpID(ret);
                     }
                  }
                  return false;
              } 
              else    // non-deferred
              {
                 int entryID = _req.bkpEntryID();

                 // if the function name has a class at the beginning, use that as the class name
                 if (_req.bkpVarInfo() != null)
                 {
                    int dotIndex = _req.bkpVarInfo().lastIndexOf('.');
                    if (dotIndex > 0)
                    {
                       partName = _req.bkpVarInfo().substring(0,dotIndex);
                       methodName    = _req.bkpVarInfo().substring(dotIndex+1);
                    }
                    else
                    {
                       methodName = _req.bkpVarInfo();
                    }  
                 }

                 // if entry ID is 0, try to construct class and function name
                 if (entryID == 0)
                 {
                    if (Gdb.traceLogger.EVT) 
                        Gdb.traceLogger.evt(1,"-------->>>>>>>> CmdBreakpointLocation.execute non-deferred entryID==0 moduleName="+moduleName+" partName="+partName +" methodName="+methodName );

                    int moduleID = cm.getModuleID(moduleName);
                    cm.checkPart(moduleID, partName);
                    partID = ((GdbModuleManager)cm).getPartID(moduleID,partName);

                    // if part ID is 0, try to find the part
                    if (partID == 0)
                    {
                       moduleID = cm.getModuleID(moduleName);
                       cm.checkPart(moduleID, partName);
                       partID = ((GdbModuleManager)cm).getPartID(moduleID,partName);
                    }

                    if (_req.bkpVarInfo() != null)
                       entryID = cm.getEntryID(partID, methodName);

                    // if entry ID is still 0, return an error message
                    if (entryID == 0)
                    {
                       _rep.setReturnCode(EPDC.ExecRc_BadParm);
                       _rep.setMessage(_debugSession.getResourceString("INVALID_BREAKPOINT_METHOD_MSG")  + methodName);
                       return false;
                    }
                 }
                 else
                 {
                    partID = cm.getPartID(entryID);
                 }

                 if (Gdb.traceLogger.DBG) 
                     Gdb.traceLogger.dbg(1,"Attempting to set breakpoint in part " + Integer.toString(partID) + " at method " + Integer.toString(entryID));

                 if (_req.bkpAction() == EPDC.ReplaceBkp)
                    ret = bm.modifyMethodBreakpoint(_req.bkpID(), partID, srcFileIndex, viewNum, entryID, ((_req.bkpAttr() & EPDC.BkpEnable) == EPDC.BkpEnable) ? true : false, _req.getConditionalExpression());
                 else
                    ret = bm.setMethodBreakpoint(partID, srcFileIndex, viewNum, entryID, 
                                                 ((_req.bkpAttr() & EPDC.BkpEnable) == EPDC.BkpEnable) ? true : false,
                                                 _req.getConditionalExpression());
                 switch(ret)
                 {
                    case -1:
                       _rep.setReturnCode(EPDC.ExecRc_BadLineNum);
                       _rep.setMessage(_debugSession.getResourceString("INVALID_BREAKPOINT_LOCATION_MSG"));
                       return false; 
                    case 0:
                       return false;

                    default:
                       if (_req.bkpAction() == EPDC.SetBkp)
                       {
                          _rep.setReturnCode(EPDC.ExecRc_DupBrkPt);
                          _rep.setMessage(_debugSession.getResourceString("DUPLICATE_BREAKPOINT_LOCATION_MSG"));
                          ((ERepBreakpointLocation) _rep).setDuplicateBkpID(ret);
                       }
                       return false;
                 }
              }
              // ########### Address BREAKPOINT ####################################
            case EPDC.AddressBkpType:
/*            
			if (isDeferred) {
				_rep.setReturnCode(EPDC.ExecRc_BadBrkType);
				_rep.setMessage(
					_debugSession.getResourceString("UNSUPPORTED_BREAKPOINT_TYPE_MSG"));
				return false;
			}
*/            
			if (Gdb.traceLogger.EVT)
				Gdb.traceLogger.evt(1, "Breakpoint type=" + _req.bkpType());
			String address = _req.bkpVarInfo(); // get requested address
			partID = bkpContext.getPPID();
			viewNum = bkpContext.getViewNo(); //Part.VIEW_SOURCE;
			
			if (isDeferred)
			{
				ret = ((GdbBreakpointManager)bm).setDeferredAddressBreakpoint(address, _req.bkpAttr(), ((_req.bkpAttr() & EPDC.BkpEnable) == EPDC.BkpEnable) ? 
					true : false, _req.getConditionalExpression());
			}
			else
			{
				ret = ((GdbBreakpointManager)bm).setAddressBreakpoint(address, ((_req.bkpAttr() & EPDC.BkpEnable) == EPDC.BkpEnable) ? 
					true : false, _req.getConditionalExpression());
				
				// if setting regular breakpoint failed, set breakpoint as deferred	
				if (ret < 0)
				{
					int attr = _req.bkpAttr();
					attr += EPDC.BkpDefer;

					ret = ((GdbBreakpointManager)bm).setDeferredAddressBreakpoint(address, attr, ((_req.bkpAttr() & EPDC.BkpEnable) == EPDC.BkpEnable) ? 
					true : false, _req.getConditionalExpression());	
				}
			}

			if (ret < 0) {
				_rep.setReturnCode(EPDC.ExecRc_BadLineNum);
				_rep.setMessage(
					_debugSession.getResourceString("INVALID_BREAKPOINT_LOCATION_MSG") + address);
			}

			return false;

            // ########### UNKNOWN BREAKPOINT TYPE ####################################
            default:
                if (Gdb.traceLogger.ERR) 
                    Gdb.traceLogger.err(2,"Unknown breakpoint type="+_req.bkpType());
                _rep.setReturnCode(EPDC.ExecRc_BadBrkType);
                _rep.setMessage(
                _debugSession.getResourceString("UNSUPPORTED_BREAKPOINT_TYPE_MSG") );
                return false;
         }
      }
      catch (IOException ioe)
      {
         Gdb.handleException(ioe);
      }
      return false;
   }

   // data fields
   private EReqBreakpointLocation _req;
}
