//////////////////////////////////////////////////////////////////////////////////////////////////////////
//Copyright (C)  1995, 2001 International Business Machines Corporation and others. All Rights Reserved.
//////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

import com.ibm.debug.epdc.*;

/**
 * Process execute command
 */
public class CmdStorage2 extends Command
{
   public CmdStorage2(DebugSession debugSession, EReqStorage2 req)
   {
      super(debugSession);
      _req = req;
   }

   /**
    * Execute program as specified
    */

   public boolean execute(EPDC_EngineSession EPDCSession)
   {
 
      _rep = new ERepStorage2();

      short addressStyle = _req.getAddressStyle();
      short unitStyle    = _req.getUnitStyle();
      int styleUnitCount = _req.getStyleUnitCount();
      int rangeEnd       = _req.getRangeEnd();
      int rangeStart     = _req.getRangeStart();
      int attributes     = _req.getAttributes();
      String exprString  = _req.getAddressExpr();
      EStdExpression2 expr = _req.getAddressExpression();

      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(1,"Attempting to monitor storage for AddressStyle="+_req.getAddressStyle()
            +" UnitStyle="+_req.getUnitStyle()+" StyleUnitCount="+_req.getStyleUnitCount()+" Attributes="+ Integer.toHexString(_req.getAttributes())
            +" RangeStart="+_req.getRangeStart()+" RangeEnd="+_req.getRangeEnd()+" Expr="+exprString );

      EStdExpression2 addressExpression = null;
      int exprDU = -1;
      int entryID = -1;
      EStdView context = null;
      int lineNum = -1;
      int PPID = -1;
      if (expr!=null)
      {
         addressExpression = _req.getAddressExpression();
         if (addressExpression!=null)
         {
            exprDU = addressExpression.getExprDU();
            entryID = addressExpression.getEntryID();
            context = addressExpression.getContext();
            if(context!=null)
            {
               lineNum = context.getLineNum();
               PPID = context.getPPID();
            }
         }
      }
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(1,"CmdStorage2.execute EReqStorage2 EStdExpression2 expr="+expr+" lineNum="+lineNum+" PPID="+PPID );

      long startAddress = 0;
      try
      { 
          String dataAddress = ((GdbDebugSession)_debugSession).getDataAddress();
          if(!dataAddress.equals(""))
          {
             dataAddress = dataAddress.substring(2);
             startAddress = Long.parseLong(dataAddress,16); 
             if (Gdb.traceLogger.DBG) 
                 Gdb.traceLogger.dbg(1,"CmdStorage2.execute hex dataAddress=0x"+dataAddress+" int dataAddress="+startAddress );
          }
      } catch(java.lang.NumberFormatException e) {}

      GdbStorageManager sm = (GdbStorageManager) _debugSession.getStorageManager();
      if(expr==null)
         sm.monitorStorage(startAddress, addressStyle, unitStyle, styleUnitCount, rangeStart, rangeEnd, attributes, expr);
      else
      {
         sm.monitorStorage(startAddress, addressStyle, unitStyle, styleUnitCount, rangeStart, rangeEnd, attributes, exprString, exprDU, PPID, lineNum, expr );
      }
      String error = ((GdbDebugSession)_debugSession)._getGdbStorage.lastEvaluationError;
      if(error!=null)
      {
         String msg = "";
         if(expr!=null)
            msg = " expression="+expr.getExpressionString();
         else
            msg = " address="+startAddress;
         msg += " caused error="+error;
         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(2,"CmdStorage2.execute ERROR "+msg );
         _rep.setMessage(msg);
         _rep.setReturnCode(EPDC.ExecRc_BadStorParm);
      }
      return false;
   }

   // Class fields
   private EReqStorage2 _req;
}

