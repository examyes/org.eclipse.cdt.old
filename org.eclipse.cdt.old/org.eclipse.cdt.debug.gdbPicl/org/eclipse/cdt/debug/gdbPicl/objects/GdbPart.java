/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.objects;
import  org.eclipse.cdt.debug.gdbPicl.*;
import  org.eclipse.cdt.debug.gdbPicl.gdbCommands.GetGdbFile;

import java.util.*;
import com.ibm.debug.epdc.*;


/**
 * This class stores information corresponding to an EPDC "Part".  
 * A Part object maintains a list of View objects.
 */
public class GdbPart extends Part
{
   public GdbPart(DebugSession debugSession, short partID, int moduleID, String partName, String fullPartName)
   {
      super(debugSession);
      // Initialize this part
      _debugSession  = debugSession;
      _partID        = partID;
      _moduleID      = moduleID;
      _partName     = partName;
      _fullPartName = fullPartName;

      getTotalLines(_partName);
      if(_totalLines>0) _SymbolTbl = true;
      else              _SymbolTbl = false;
/*
	  // sam:  don't need this anymore as we are now dynamically generating the disassembly view
      if(isDebuggable())
         setStartEndAddress(_partName);         
	  if (Gdb.traceLogger.DBG) 
              Gdb.traceLogger.dbg(1,"**** GdbPart CTOR _totalLines="+_totalLines+" start="+_startAddress+" end="+_endAddress );
*/              

      _methods       = null;
      if(isDebuggable())
      {
          ModuleManager moduleManager = _debugSession.getModuleManager();
          Module m = moduleManager.getModule(_moduleID);
          m.setIsDebuggable(true);
          ((GdbDebugSession)_debugSession).getPartMethods(this);
      }

      _views         = new View[NUM_VIEWS];

      // NOTE: When adding views, make sure you make the appropriate
      // modifications to these files:
      //    LocationBreakpoint.java
      //    ThreadComponent.java
      //    CmdContextConvert.java
      //    CmdEntryWhere.java
      //    CmdInitializeDE.java

      // Create the source view for this Part
      DebugEngine _debugEngine = _debugSession.getDebugEngine();
      _views[VIEW_SOURCE-1] = new GdbSourceView((GdbDebugEngine)_debugEngine, this);  //HC

      // Create the disassembly view for this Part
      _views[VIEW_DISASSEMBLY-1] = new GdbDisassemblyView((GdbDebugEngine)_debugEngine, this);

	  if (Part.MIXED_VIEW_ENABLED)
	  {
    	  // Create the mixed view for this Part
	      _views[VIEW_MIXED-1] = new GdbMixedView((GdbDebugEngine)_debugEngine, this);
	  }

      _InUse        = true;
      _PartNew      = true;
      _PartDeleted  = false;
      _PartChanged  = true;
      _PartVerified = true;
   }

   private int getTotalLines(String fileName)
   {
      GdbDebugSession debugSession = (GdbDebugSession)_debugSession;

      _totalLines = debugSession._getGdbFile.getTotalLines(fileName);

//      _executableLines = new int[_totalLines];
//      for(int k=0; k<_totalLines; k++)
//          _executableLines[k]=k+1;

      return _totalLines;
   }

   private void setStartEndAddress(String fileName)
   {
      GdbDebugSession debugSession = (GdbDebugSession)_debugSession;

      GetGdbFile.StartEnd startEnd = debugSession._getGdbFile.getStartEnd(fileName, _totalLines);
      if(startEnd!=null)
      {
         _startAddress = startEnd.startAddress;
         _endAddress   = startEnd.endAddress;
      }
   }


   public View getView(int viewNum)
   {
      if (viewNum >0 && viewNum <= NUM_VIEWS)
      {
         return _views[viewNum-1];
      }
      else
      {
         Gdb.debugOutput("Invalid view number requested");
         return null;
      }
   }

   Part getPart()			//HC
   {
      return _part;
   }

   public int[] getLineNumbers()
   {
	  if (Gdb.traceLogger.DBG) 
              Gdb.traceLogger.dbg(1,"*************** GdbPart.getLineNumbers for GdbPart="+_partName );
      if(_views==null || _views[VIEW_SOURCE-1]==null)
         return new int[0];

      return _views[VIEW_SOURCE-1]._executableLines;
   }

  /**
    * Fetches and stores a list of methods for this part
    */
   void loadMethods()
   {
      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(2,"GdbPart.loadMethods called");

      ModuleManager cm = _debugSession.getModuleManager();

      if (_methods == null)
      {
         Gdb.debugOutput("GdbPart.loadMethods Getting methods for  part="+getName() +" _partID="+_partID+" name="+ _fullPartName);
         //if ((_methods = ((SuntoolsDebugSession)session).getMethods(_part)) == null)     //HC
//         if (!isDebuggable() || (_methods = ((GdbDebugSession)session).getMethods(_part)) == null)     //HC
         if (!isDebuggable() || _methods == null)     //HC
         {
//            if(!isDebuggable())
//            {
//                _methods = new MethodInfo[0];
//                _entryIDs = new int[0];
//                if (Gdb.traceLogger.EVT) 
//                    Gdb.traceLogger.evt(2,"GdbPart.loadMethods BYPASSING for NON-DEBUGGABLE part="+getName() );
//                return;
//            }
//
            _methods = new MethodInfo[0];
            _entryIDs = new int[0];
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(2,"GdbPart.loadMethods DONE part="+getName()+" isDebuggable="+isDebuggable()+" _methods.length="+_methods.length );
            return;
         }

      }
            //else
      {
         Gdb.debugOutput(Integer.toString(_methods.length) + " methods");
         _entryIDs = new int[_methods.length];
      }
/*
      _methods = session.getMethods(_FullModuleName);
      Gdb.debugOutput(Integer.toString(_methods.length) + " methods");
      _entryIDs = new int[_methods.length];
*/

      for (int i=0; i<_methods.length; i++)
      {
         _entryIDs[i] = cm.addEntry(_partID, i);
      }
   }


   private GdbPart _part   = this; //((GdbDebugSession) _debugEngine.getDebugSession()).findAndGetPart(_FullModuleName); //HC
//   public  boolean isDebuggable() 
//   { 
//System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%% GdbPART.isDebuggable <<<<"+(_totalLines>0) +" PART="+_partName );     
//      if(_totalLines>0) return true;        else              return false; 
//   }
   private int    _totalLines = 0;
   private String _startAddress = null;
   private String _endAddress = null;
   public  String getStartAddress() { return _startAddress; }
   public  String getEndAddress() { return _endAddress; }
}
