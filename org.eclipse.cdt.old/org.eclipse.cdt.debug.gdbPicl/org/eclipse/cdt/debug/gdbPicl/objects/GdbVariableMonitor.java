/*
 * Copyright (c) 1995, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.objects;
import  org.eclipse.cdt.debug.gdbPicl.*;

import com.ibm.debug.epdc.*;
import java.util.*;
import java.text.*;

/**
 * Represents a variable monitor.  Specific monitor types (LocalMonitor, InstanceMonitor,
 * ClassMonitor) descend from this class.
 */
public class GdbVariableMonitor extends Gdb_VariableMonitor
{
   public GdbVariableMonitor(GdbDebugSession debugSession, int exprID, int monType, GdbVariable monVar, EStdView context, int DU)
   {
     super(debugSession, exprID, monType, monVar, context, DU);
   }
   public GdbVariableMonitor(DebugSession debugSession, int exprID, int monType, GdbVariable monVar, EStdView context, int DU)
   {
     super((GdbDebugSession)debugSession, exprID, monType, monVar, context, DU);
   }

   void modifyMonitorValue()
   {
      _monValuesChanged = true;
   }

   public void deleteMonitor()
   { 
      if (this._monType == EPDC.MonTypeProgram)
      { 	 
      	 String ID = String.valueOf(this.getMonitoredVariable().getNodeID());
         
         if (Gdb.traceLogger.EVT) 
           Gdb.traceLogger.evt(2,"GdbVariableMonitor.deleteMonitor MonTypeProgram variable name: " + this.getMonitoredVariable().getName() + " id: " + ID );
           
         String cmd = "undisplay "+ID;
         boolean ok = _debugSession.executeGdbCommand(cmd);
         if( ok )
            _debugSession.addCmdResponsesToUiMessages();
            
      }    

      _monDeleted = true;
   }

   
   public void updateVariable()
   {
      if (_monDeleted || !_monEnabled)
         return;

      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(2,"GdbVariableMonitor: updateVariable() BYPASSING - done by GdbVariableMonitorManager.updateMonitors" );

      // Set our flags to reflect the state of the monitored variable
      _monValuesChanged     = _monitoredVar.hasChanged();
      _monTreeStructChanged = _monitoredVar.isTreeStructChanged();
   }


  /**
   * Process a general expression
   */
  public static GdbExprEvalInfo evaluateExpression(GdbDebugSession _debugSession,
                                         String      exprString, 
                                         EStdView    context, 
                                         int         DU) 
  {
      Gdb.debugOutput("GdbVariableMonitor: Evaluating: " + exprString +" DU: "+DU);
    
//      GdbDebugSession debugSession = (GdbDebugSession) ((GdbDebugEngine)debugEngine).getDebugSession();

      int l = context.getLineNum();
      String lineNo = Integer.toString(l);
      int partID = context.getPPID();
      String fileName = "";
      GdbModuleManager mm = (GdbModuleManager)_debugSession.getModuleManager();
      if(partID!=0)
      {   GdbPart part = (GdbPart)mm.getPart(partID);
          if(part!=null) 
             fileName = part.getName();
      }
      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(1,"-------->>>>>>>> GdbVariableMonitor.evaluateExpression exprString="+exprString+" context.partID="+ partID+" context.partID.Name="+fileName+" context.lineNum="+lineNo );

///////  GDB uses stackFrame context rather than user specified source context //////////////
//      _debugSession.setTemporaryContext(fileName,lineNo);

      int exprNumber = -1;
      String exprName = "???";
      String exprValue = "???";
      String exprType = "unknown_Type";
      String cmd = "display "+exprString;
      boolean ok = _debugSession.executeGdbCommand(cmd);
      if( ok )
      {
         _debugSession.addCmdResponsesToUiMessages();
         if(_debugSession.cmdResponses.size()>0)
         {
            String str = null;
            int i = 0;
            for(i=0; i<_debugSession.cmdResponses.size(); i++)
            {
               str = (String)_debugSession.cmdResponses.elementAt(i);
               if(str!=null && !str.equals("") && !str.equals(" ") && !str.startsWith("(gdb)") )
                  break;
            }
            
            if (str.startsWith("Cannot access memory at address") || 
            	str.startsWith("Attempt to take contents of a non-pointer value.") ||
            	str.startsWith("The history is empty."))
            {
            	if (i+1 < _debugSession.cmdResponses.size())
            	{
            		str = (String)_debugSession.cmdResponses.elementAt((short)i+1);
            		// try to clean up for this invalid display
            		if (str.startsWith("Disabling display"))
            		{
            			String keyword1 = "display ";
            			String keyword2 = " to";
            			int idx = str.indexOf(keyword1);
            			int end = str.indexOf(keyword2);
            			if (idx > -1 && end > -1)
            			{
            				str = str.substring(idx+keyword1.length(), end);
            				cmd = "undisplay "+str;
					 	    _debugSession.executeGdbCommand(cmd);
            			}
        			    return new GdbExprEvalInfo(GdbExprEvalInfo.exprNOTFOUND);
            		}
            	}
            }
            
            if(str!=null && !str.equals("") && !str.equals(" ") && !str.startsWith("(gdb)") )
            {
               if (Gdb.traceLogger.EVT) 
                   Gdb.traceLogger.evt(2,"GdbVariableMonitor.evaluateExpression str="+str );
               if(str.startsWith("No symbol "))
               {
                  if (Gdb.traceLogger.EVT) 
                      Gdb.traceLogger.evt(1,"GdbVariableMonitor.evaluateExpression NO SYMBOL "+exprString );
                  return new GdbExprEvalInfo(GdbExprEvalInfo.exprNOTFOUND);
               }
               if (Gdb.traceLogger.DBG) 
                   Gdb.traceLogger.dbg(2,"GdbVariableMonitor.evaluateExpression "+exprString +" >>>> "+str );

               int colon = str.indexOf(": ");
               if(colon<=0)
               {
                  if (Gdb.traceLogger.ERR) 
                      Gdb.traceLogger.err(2,"GdbVariableMonitor.evaluateExpression missing ':' from gdb response="+str );
                  exprString = "";
               }
               else
               {
                  String s = str.substring(0,colon);
                  if (Gdb.traceLogger.DBG) 
                      Gdb.traceLogger.dbg(3,"GdbVariableMonitor.evaluateExpression exprNumber_STR="+s +"<<<<" );
                  exprNumber = Integer.parseInt(s);
                  if (Gdb.traceLogger.DBG) 
                      Gdb.traceLogger.dbg(3,"GdbVariableMonitor.evaluateExpression exprNumber_INT="+exprNumber +"<<<<" );
                  str = str.substring(colon+2);
                  int equals = str.indexOf(" = ");
                  exprName = str.substring(0,equals);
                  if (Gdb.traceLogger.DBG) 
                      Gdb.traceLogger.dbg(3,"GdbVariableMonitor.evaluateExpression exprName="+exprName +"<<<<" );
                  exprValue = str.substring(equals+3);
                  
                  if (exprValue.equals("void") || (exprValue.equals("Attempt to dereference a generic pointer.") && exprName.startsWith("(*")))
                  {
                  	exprValue = "???";
                  	cmd = "undisplay "+exprNumber;
					_debugSession.executeGdbCommand(cmd);
                  }
                  
                  if (Gdb.traceLogger.DBG) 
                      Gdb.traceLogger.dbg(3,"GdbVariableMonitor.evaluateExpression exprValue="+exprValue +"<<<<" );
               }
            }
            _debugSession.cmdResponses.removeAllElements(); 
         }
      }
///////  GDB uses stackFrame context rather than user specified source context //////////////
//    _debugSession.restoreContext();

    if(!exprValue.equals("???"))
    {
    	exprType = getExpressionType(_debugSession, exprString);
    }

    if (Gdb.traceLogger.EVT) 
        Gdb.traceLogger.evt(1,"<<<<<<<<-------- GdbVariableMonitor.evaluateExpression String="+exprString+" Name="+exprName +" Value="+exprValue +" Number="+exprNumber+" Type="+exprType+"<<<<" );

    if (exprString == null || exprValue.equals("???") || exprType == null)
    {
      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(1,"GdbVariableMonitor.evaluateExpression exprString==null || exprValue==??? ");
        return new GdbExprEvalInfo(GdbExprEvalInfo.exprFAILED);
    }

    if (exprName.equals("")) 
    {
      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(1,"GdbVariableMonitor.evaluateExpression exprName==\"\" " );
      // No first token
      return new GdbExprEvalInfo(GdbExprEvalInfo.exprINVALID);
    }

    GdbExprEvalInfo varInfo = new GdbExprEvalInfo(GdbExprEvalInfo.exprVARIABLEFOUND);
    varInfo.setInitialValue(exprValue);
    varInfo.setType(exprType);
    varInfo.setNodeID(exprNumber);
    if (Gdb.traceLogger.EVT) 
        Gdb.traceLogger.evt(3,"GdbVariableMonitor.evaluateExpression initialValue="+exprValue );
    
        // Expression evaluation successful.
    return varInfo;   
  }
  
  static public String getExpressionType(GdbDebugSession _debugSession, String exprString)
  {
  	// give blanks for now... bad performance caused by asking for
  	// types of all variables
  	if (false)
  	{
	  	return "<No Type Info>";
  	}
  	  	
      String exprType = "<No Type Info>";  	
      String cmd = "whatis "+exprString;
      
      boolean ok = _debugSession.executeGdbCommand(cmd);
      if( ok )
      {
         _debugSession.addCmdResponsesToUiMessages();
         if(_debugSession.cmdResponses.size()>0)
         {
            String str = null;

            for(int i=0; i<_debugSession.cmdResponses.size(); i++)
            {
               str = (String)_debugSession.cmdResponses.elementAt(i);
               if(str!=null && !str.equals("") && !str.equals(" ") && !str.startsWith("(gdb)") )
                  break;
            }
            if(str!=null && !str.equals("") && !str.equals(" ") && !str.startsWith("(gdb)") )
            {
            	
               if (Gdb.traceLogger.EVT) 
                   Gdb.traceLogger.evt(2,"GdbVariableMonitor.evaluateExpression str="+str );
               if(str.startsWith("No symbol "))
               {
                  if (Gdb.traceLogger.EVT) 
                      Gdb.traceLogger.evt(1,"GdbVariableMonitor.evaluateExpression NO SYMBOL "+exprString );
               }
               if (Gdb.traceLogger.DBG) 
                   Gdb.traceLogger.dbg(2,"GdbVariableMonitor.evaluateExpression "+exprString +" >>>> "+str );

               int equals = str.indexOf("= ");
               if(equals<=0)
               {
               	
                  if (Gdb.traceLogger.ERR) 
                      Gdb.traceLogger.err(2,"GdbVariableMonitor.evaluateExpression missing '= from gdb response="+str );
                      
               }
               else
               {
          	  	  // scalar type
                  exprType = str.substring(equals+2);
                 
                  if (Gdb.traceLogger.DBG) 
                      Gdb.traceLogger.dbg(3,"GdbVariableMonitor.evaluateExpression exprType="+exprType+"<<<<" );
               }
            }
            _debugSession.cmdResponses.removeAllElements(); 
         }     
      }
      
      return exprType;
  }

}
