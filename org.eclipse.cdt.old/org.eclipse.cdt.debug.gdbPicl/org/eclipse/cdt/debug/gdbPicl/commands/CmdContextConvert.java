/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

import com.ibm.debug.epdc.*;

/**
 * Process Context Convert command
 */
public class CmdContextConvert extends Command
{
   public CmdContextConvert(DebugSession debugSession, EReqContextConvert req)
   {
      super(debugSession);
      _req = req;
   }

   /**
    * Convert the given context to the requested view number.
    * _req.context() contains the context to be converted
    * _req.viewNum() contains the view number the context is to be converted to
    */
   public boolean execute(EPDC_EngineSession EPDCSession)
   {
     String lineNum = String.valueOf( _req.context().getLineNum() );
     GdbDebugSession gdbDebugSession = (GdbDebugSession)_debugSession;
     ModuleManager moduleManager = gdbDebugSession.getModuleManager();
     GdbPart part = (GdbPart)moduleManager.getPart(_req.context().getPPID());
     String partName = part.getName();
     String address = null; 
     int viewNo = _req.context().getViewNo();
     GdbSourceView sourceView = (GdbSourceView)part.getView(Part.VIEW_SOURCE);
     GdbDisassemblyView disassemblyView = (GdbDisassemblyView)part.getView(Part.VIEW_DISASSEMBLY);
     
     if (Part.MIXED_VIEW_ENABLED)
     {
     	GdbMixedView mixedView = (GdbMixedView)part.getView(Part.VIEW_MIXED);
     }

     switch (_req.viewNum())
     {
        case Part.VIEW_SOURCE:
           // Convert the provided context to the source view

           address = disassemblyView.convertDisassemblyLineToAddress(lineNum);
           //address = gdbDebugSession._getGdbFile.convertDisassemblyLineToAddress(lineNum, part.getStartAddress(), part.getEndAddress() ); 
           String srcLineNum = gdbDebugSession._getGdbFile.convertAddressToSourceLine(address); 
System.out.println("CmdContextConvert.SOURCE file="+partName+" line="+lineNum+" address="+address+" sourceyLine="+srcLineNum );
           int srcLine = Integer.parseInt(srcLineNum);

           _rep = new ERepContextConvert( new EStdView(
              _req.context().getPPID(),
              (short)Part.VIEW_SOURCE, 
              _req.context().getSrcFileIndex(),
              srcLine )
           );
           break;

        case Part.VIEW_DISASSEMBLY:
           // Convert the provided context to the disassembly view

           if (Gdb.traceLogger.ERR) 
               Gdb.traceLogger.err(1,"######## UNIMPLEMENTED DISASSEMBLY VIEW CmdContextConvert partID="+_req.context().getPPID()
                                   +" fileIndex="+_req.context().getSrcFileIndex()+" lineNum="+_req.context().getLineNum() );

           address = gdbDebugSession._getGdbFile.convertSourceLineToAddress(partName,lineNum); 
           String disLineNum = disassemblyView.convertAddressToDisassemblyLine(address); 
           //String disLineNum = gdbDebugSession._getGdbFile.convertAddressToDisassemblyLine(address, part.getStartAddress(), part.getEndAddress() ); 
System.out.println("CmdContextConvert.DISSASM file="+partName+" line="+lineNum+" address="+address+" disassemblyLine="+disLineNum );
           int disLine = Integer.parseInt(disLineNum);

           _rep = new ERepContextConvert( new EStdView(
              _req.context().getPPID(),
              (short)Part.VIEW_DISASSEMBLY, 
              _req.context().getSrcFileIndex(),
              disLine )
           );
           break;

        case Part.VIEW_MIXED:
           // Convert the provided context to the mixed view

           // NOTE: !!! This conversion is not implemented.  We just
           // return the old context information with the new view num.
           if (Gdb.traceLogger.ERR) 
               Gdb.traceLogger.err(1,"######## UNIMPLEMENTED MIXED VIEW CmdContextConvert partID="+_req.context().getPPID()
                                   +" fileIndex="+_req.context().getSrcFileIndex()+" lineNum="+_req.context().getLineNum() );
           _rep = new ERepContextConvert( new EStdView(
              _req.context().getPPID(),
              (short)Part.VIEW_MIXED, 
              _req.context().getSrcFileIndex(),
              _req.context().getLineNum())
           );
           break;

        default:
           // Something's wrong. We got an invalid view number.
              if (Gdb.traceLogger.ERR) 
                  Gdb.traceLogger.err(2,"Invalid view number in context convert, viewNum="+_req.viewNum() );
              _rep = new ERepContextConvert( new EStdView(
              _req.context().getPPID(),
              (short)Part.VIEW_SOURCE, 
              _req.context().getSrcFileIndex(),
              _req.context().getLineNum())
           );
           break;
     }
     return false;
   }

   // data fields
   EReqContextConvert _req;
}
