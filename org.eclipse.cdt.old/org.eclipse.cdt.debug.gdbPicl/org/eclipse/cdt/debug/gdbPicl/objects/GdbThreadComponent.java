/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package com.ibm.debug.gdbPicl.objects;
import  com.ibm.debug.gdbPicl.*;

import java.util.*;
import com.ibm.debug.epdc.*;

/*
 * This class stores the latest information about a thread.  All dynamic
 * thread information (eg. call stack, priority, line number, etc) is
 * encapsulated in this class.  When this information is known to have 
 * changed, the update() method should be called.  For efficiency, all
 * dynamic information about a thread should be taken from this class
 *
 */

public class GdbThreadComponent extends ThreadComponent
{
   GdbThread _gdbThread  = null;

   public GdbThreadComponent(DebugSession debugSession, 
			   int DU 
			   ,GdbThread gdbThread
            )
   {
     super(debugSession, DU); // use a new constructor from ThreadComponent 
     _gdbThread   = gdbThread;
     _callStack   = new GdbStackFrame[0];
   }

  /**
    * Returns ERepGetNextThread change item for this thread
    */
   public ERepGetNextThread getEPDCThread()
   {
      ERepGetNextThread EPDCThread;

      String threadName = _gdbThread._threadName;
      String threadStatus = "unknownStatus";
      String threadPriority = "unknownPriority";
      String threadGroupName = "unknownGroupName";

      _threadAttributes = new EStdAttribute[3];
      _threadAttributes[0] = new EStdAttribute(EPDC.ThreadNameOrTID,
                                              null,
                                              threadName);

      _threadAttributes[1] = new EStdAttribute(EPDC.ThreadState,
                                              null,
                                              threadStatus);

      _threadAttributes[2] = new EStdAttribute(EPDC.ThreadPriority,
                                              null,
                                              threadPriority);


         int systemTID = _DU;
         systemTID = _gdbThread._systemTID;

         if (_partID <= 1)
         {
			GdbModuleManager cm = (GdbModuleManager)_debugSession.getModuleManager();
			int partID = cm.getPartID(moduleID(0),fileName(0));
         	if (partID > 0)
         		_partID = partID;
         	else
         		_partID = 1;	
         }

         EPDCThread = super.getEPDCThread();
//         EPDCThread = new ERepGetNextThread(_debugEngine.getSession(),
//               _partID, _state, getDbgState(), _priority, systemTID, _DU); // ThreadComponent doesnt use systemTID

         // NOTE: We must call setWhereStopped for each supported view
         if (isPartial() || _isZombie)
         {
//            EPDCThread.setPartialThreadInfo(Part.VIEW_SOURCE);
            //EPDCThread.setPartialThreadInfo(Part.VIEW_BYTECODE);
            //EPDCThread.setPartialThreadInfo(Part.VIEW_MIXED);
         }
         else
         {
            EPDCThread.setWhereStopped(Part.VIEW_SOURCE, 1, lineNumber(0));
            if (Gdb.traceLogger.ERR) 
                Gdb.traceLogger.err(2,"######## UNIMPLEMENTED DISASSEMBLY VIEW GdbThreadComponent lineNumber(0)="+lineNumber(0) );
                		
            EPDCThread.setWhereStopped(Part.VIEW_DISASSEMBLY, 1,  convertLineNum(lineNumber(0), _partID)); 
            if (Part.MIXED_VIEW_ENABLED)
	            EPDCThread.setWhereStopped(Part.VIEW_MIXED, 1, lineNumber(0)); 
         }
      return EPDCThread;

   }


  /**
   * Return the thread name 
   */
   public String threadName() 
   {
      try
      {
         return "GdbThread-"+ new Integer(_DU).toString();
         // NOTE: We use thread's description method, NOT getName() since this
         // is not guaranteed to be unique.
//BCS         return _remoteThread.description();
      }
      catch (Exception e)
      {
         Gdb.handleException(e);
         return "?";
      }
   }


  /**
   * Return the thread status 
   */
   public String threadStatus() 
   {  String s = "null";
      if(_gdbThread!=null)
         s = _gdbThread.getStatus();
      return s;
   }

   /**
    * Returns the size of the call stack for this thread
    */
   public int getCallStackSize()
   {  
      if(_callStack==null)
      {
         return 0;
      }
      return _callStack.length;
   }


   
   /**
    * Get the current moduleID for the specified stackEntry.  Use 0
    * for the top of the stack.
    */
   public int moduleID(int stackEntry) 
   {
      if(_callStack==null)
      {
		  if (Gdb.traceLogger.ERR) 
              Gdb.traceLogger.err(1,"$$$$$$$$$$$$$$$$ GdbThreadComponent.moduleID="+_moduleID +"  (_callStack==null)" );
         return _moduleID;   //0
      }
      if (stackEntry >= _callStack.length)
      {
		  if (Gdb.traceLogger.ERR) 
              Gdb.traceLogger.err(1,"$$$$$$$$$$$$$$$$ GdbThreadComponent.moduleID="+_moduleID +"  (stackEntry>=_callStack.length)" );
         return _moduleID;   //0
      }
      if (stackEntry >= _callStack.length)
      {
		  if (Gdb.traceLogger.ERR) 
              Gdb.traceLogger.err(1,"$$$$$$$$$$$$$$$$ GdbThreadComponent.moduleID stackEntry="+stackEntry );
         return _moduleID;   //0
      }
      return _callStack[stackEntry].getModuleID();       
   }
   
   /**
    * Get the current lineNumber for the specified stackEntry.  Use 0
    * for the top of the stack.
    */
   public int lineNumber(int stackEntry) 
   {
      if(_callStack==null)
      {
	  if (Gdb.traceLogger.ERR) 
              Gdb.traceLogger.err(1,"$$$$$$$$$$$$$$$$ GdbThreadComponent.lineNumber="+_lineNumber +"  (_callStack==null)" );
         return _lineNumber;   //0
      }
      if (stackEntry >= _callStack.length)
      {
		  if (Gdb.traceLogger.ERR) 
              Gdb.traceLogger.err(1,"$$$$$$$$$$$$$$$$ GdbThreadComponent.lineNumber="+_lineNumber +"  (stackEntry>=_callStack.length)" );
         return _lineNumber;   //0
      }
      if (stackEntry <0 )
      {
		  if (Gdb.traceLogger.ERR) 
              Gdb.traceLogger.err(1,"$$$$$$$$$$$$$$$$ GdbThreadComponent.lineNumber stackEntry ="+stackEntry );
         return _lineNumber;   //0
      }
      return _callStack[stackEntry].getLineNumber();
   }

   /**
    * Get the current frameAddress for the specified stackEntry.  Use 0
    * for the top of the stack.
    */
   public String frameAddress(int stackEntry)
   {
     if (_callStack == null) {
       Gdb.debugOutput("GdbThreadComponent.fileName(): null _callStack");
       return "?Address?";
     }

     if (stackEntry <0 )
       return "?Address?";

     if (stackEntry >= _callStack.length)
       return "?Address?";

     try {
       return _callStack[stackEntry].getFrameAddress();
     }
     catch (Exception e) {
       Gdb.handleException(e);
       return "?Address?";
     }
   }

   /**
    * Get the current fileName for the specified stackEntry.  Use 0
    * for the top of the stack.
    */
   public String fileName(int stackEntry)
   {
     if (_callStack == null) {
       Gdb.debugOutput("GdbThreadComponent.fileName(): null _callStack");
       return "?FileName?";
     }

     if (stackEntry < 0)
       return "?FileName?";

     if (stackEntry >= _callStack.length)
       return "?FileName?";

     try {
       return _callStack[stackEntry].getFileName();
     }
     catch (Exception e) {
       Gdb.handleException(e);
       return "?FileName?";
     }
   }

   /**
    * Get the current methodName for the specified stackEntry.  Use 0
    * for the top of the stack.  If signature is true, returns the method
    * name AND signature if possible.  Otherwise, returns only the method 
    * name. 
    */

   // NOTE: The signature is not guaranteed to be correct 100% of the time. 
   // 
   // If you have stepped on the last statement of a method that has a return type
   // of void and has a return statement within the body of that method and the
   // very next method in the source file has the same name as the current method
   // but with different parameters then we will show the wrong signatures in the 
   // callstack.  This is the only time we will show the wrong sigs in the
   // call stack. (We have the right method name, but the wrong signature!)  If
   // the very next method does not have the same name, then we show the correct
   // method name but without its signature.  
   // The above is due to a line mapping problem.  When a void method has
   // a return statement inside it, the method declaration line is no longer
   // registered as executable.  This throws off our line mapping routine.

   public String methodName(int stackEntry, boolean signature) 
   {
      // getMethodName in GdbStackFrame does not return the signature
      // so we will use our entryID to get the full method name.  If for some
      // reason the root names do not match (ie. our line mapping is incorrect)
      // return the results from getMethodName
      if (stackEntry >= _callStack.length)
         return "?MethodName?";
      if (stackEntry <0 )
         return "?MethodName?";

      if (signature)
      {
         GdbModuleManager cm = (GdbModuleManager)_debugSession.getModuleManager(); 
         int moduleID = moduleID(stackEntry);
         int partID = cm.getPartID(moduleID,fileName(stackEntry));

         if (partID < 1)
            return _callStack[stackEntry].getMethodName();

         int entryID = cm.getEntryID(partID,lineNumber(stackEntry));

         if (entryID < 1)
            return _callStack[stackEntry].getMethodName();

         String methodName = cm.getEntryName(entryID);

         if (!methodName.startsWith(_callStack[stackEntry].getMethodName()))
            return _callStack[stackEntry].getMethodName();
         return methodName;
      }
      else
      {
         return _callStack[stackEntry].getMethodName();
      }
   }

   void clearCallStack()
   { 
      _callStack = new GdbStackFrame[0]; 
   }

   /**
    * Get the last known call stack for this thread
    * Only report call stack if _stackTracking is true
    */
   public GdbStackFrame[] getCallStack()
   {
      if (Gdb.traceLogger.DBG) 
         Gdb.traceLogger.dbg(1,"GdbThreadComponent.getCallStack DU="+_DU );

      _callStack = _gdbThread.getStack(false);

      return _callStack;
   }
  
   /**
    * Get the last known call stack for this thread
    * Only report call stack if _stackTracking is true
    */
   public GdbStackFrame[] getCallStack(boolean ignoreStackTracking)
   {
      if (Gdb.traceLogger.DBG) 
         Gdb.traceLogger.dbg(1,"GdbThreadComponent.getCallStack DU="+_DU );

      _callStack = _gdbThread.getStack(ignoreStackTracking);

      return _callStack;
   }

   /**
    * Update this thread's dynamic information with the given call stack info
    */
   public void update(GdbStackFrame[] callStack) throws Exception
   {
      // NOTE: This is the only place we should _ever_ call dumpStack()
      this._callStack = callStack;

      if (isPartial())
      {
         _lineNumber = 0;
         if (Gdb.traceLogger.DBG) 
            Gdb.traceLogger.dbg(1,"<<<<<<<<-------- GdbThreadComponent.update Thread="+_gdbThread+" **PARTIAL** lineNumber="+_lineNumber );
         _partID     = 0;
         _moduleID   = 0;

         String status = threadStatus();
         if (status.equals("cond. waiting"))
         {
            _state      = EPDC.StdThdBlocked;
         }
         else
         {
            _state      = EPDC.StdThdRunnable;
         }
         _isZombie   = false;
//         _priority   = getRemoteThreadPriority();
      }
      else 
           if (_callStack!=null && _callStack.length > 0)
      {
         GdbModuleManager cm = (GdbModuleManager)_debugSession.getModuleManager();
         _lineNumber = lineNumber(0);
         _moduleID   = moduleID(0);
         _partID     = cm.getPartID(_moduleID,fileName(0));

         String status = threadStatus();
         if (status.equals("cond. waiting"))
         {
            _state      = EPDC.StdThdBlocked;
         }
         else
         {
            _state      = EPDC.StdThdRunnable;
         }
         _isZombie   = false;
//         _priority   = getRemoteThreadPriority();

         int threadID = -1;
         if(_gdbThread!=null) threadID = _gdbThread._intThreadID;
         if (Gdb.traceLogger.EVT) 
             Gdb.traceLogger.evt(1,"<<<<<<<<-------- GdbThreadComponent.update Thread " + threadID + " partID:"+_partID
               +" file:" +fileName(0) +" moduleID:"+_moduleID+" line:" + lineNumber(0) );
      }
      else
      {
         if (Gdb.traceLogger.DBG) 
             Gdb.traceLogger.dbg(1,"<<<<<<<<-------- GdbThreadComponent.update Thread="+_gdbThread+" **NO CALLSTACK** lineNumber="+_lineNumber );
         // Even though the VM reports a thread _exists_ it may have not
         // been initialized yet.  It might be in a zombie state with no
         // stack information. 
         _isZombie = true;
      }
   }


   /**
   * Parses the toString string of a RemoteThread object for the priority.
   * The format of a toString string is "[name,priority,threadgroup]". See
   * java.lang.Thread.java toString() for details.
   * @returns The priority of the RemoteThread or -1 upon parsing failure
   */
/*
   private int getRemoteThreadPriority()
   {
      if (_remoteThread == null)
      {
         Gdb.debugOutput("GetRemoteThreadPriority returned -1");
         return -1;
      }

      try
      {
         StringTokenizer tokenizer = new StringTokenizer(
            _remoteThread.toString(), ",",false);
         if (tokenizer.hasMoreTokens())
         {
            tokenizer.nextToken();
            if (tokenizer.hasMoreTokens())
            {
               return Integer.parseInt(tokenizer.nextToken());
            }
         }
      }
      catch (Exception e) { }

      Gdb.debugOutput("GetRemoteThreadPriority returned -1");
      return -1;
   }
*/

   /**
    * Update file info for all files on the stack
    */
   public void updateFilesOnStack() 
   {
      if (_callStack == null) return;  // CMVC 15984
      ModuleManager mm = _debugSession.getModuleManager();

      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(2,"GdbThreadComponent.updateFilesOnStack ?????????????????????? getStack _DU="+_DU+" ??????????????" );

// ?????????????????????????????????????????????????????
      _stackTracking = true;
      _callStack = getCallStack();  

      for (int i=_callStack.length-1; i>=0; i--)
      {
         if(!fileName(i).startsWith("?"))
            mm.checkPart(moduleID(i),fileName(i));
      }
      
      // now check to see if the disassembly view is complete
      // if we are missing new function, add it when the view is verified
      for (int i=0; i<_callStack.length; i++)
      {
      	    int partId = mm.getPartID(fileName(i));
      	    GdbPart part = (GdbPart) mm.getPart(partId);
      	    
      	    if (part == null)
      	    	continue;
      	    
			View tempView = ((GdbPart)part).getView(Part.VIEW_DISASSEMBLY);
			
			if (tempView.isViewVerify())
			{
				part.setPartChanged(true);
           		tempView.setViewVerify(false);
           		part.verifyViews();
			}      	    
      }
   }

   /**
    * Get stack change info packet for this thread.  If there is no call stack
    * this method will return a change packed with 0 entries.
    */
   public ERepGetChangedStack getEPDCStack() 
   {
      int partID;
      ERepGetChangedStack stackChange  = null;

      GdbModuleManager cm = (GdbModuleManager)_debugSession.getModuleManager();

      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(1,"GdbThreadComponent.getEPDCStack  DU="+_DU+" _stackReported="+_stackReported  );

      int flags = EPDC.STACK_ENTRY_CHANGED;
      if (!_stackReported)
      {   flags = flags | EPDC.STACK_ENTRY_NEW;  // Report the stack change as NEW if we haven't reported one before
          _stackReported = true;
      }
      stackChange = new ERepGetChangedStack(_DU, flags);

      if (_callStack == null) return stackChange; // CMVC 15984

      EPDC_EngineSession _engineSession = _debugSession.getDebugEngine().getSession();
      ERepGetNextStackEntry entry;
      for (int i=_callStack.length-1; i>=0; i--) 
      {
         entry = new ERepGetNextStackEntry(_engineSession);
         entry.setColumn(1, Integer.toString(_callStack.length - i));
         entry.setColumn(2, methodName(i,true));
         entry.setColumn(3, fileName(i));
         entry.setColumn(4, frameAddress(i));
         entry.setNumParms(4);
         
         partID = cm.getPartID(moduleID(i),fileName(i));
         if (partID <= 0)
         {
         	cm.checkPart(moduleID(i), fileName(i));
         	partID = cm.getPartID(moduleID(i),fileName(i));
         	
         	if (partID <= 0)
	         	partID = 1;
         }

         // NOTE: We must call setStackEntryViewInfo for each supported view
         entry.setStackEntryViewInfo((short) Part.VIEW_SOURCE,
               (short)partID, 1, lineNumber(i));
         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(2,"######## UNIMPLEMENTED DISASSEMBLY/MIXED VIEW GdbThreadComponent.ERepGetChangedStack lineNum="+lineNumber(i) );
         entry.setStackEntryViewInfo((short) Part.VIEW_DISASSEMBLY,
               (short)partID, 1, convertLineNum(lineNumber(i), partID));
         
         if (Part.MIXED_VIEW_ENABLED)               
         {
	         entry.setStackEntryViewInfo((short) Part.VIEW_MIXED,
	               (short)partID, 1, lineNumber(i));
         }

         if (Gdb.traceLogger.EVT) 
             Gdb.traceLogger.evt(3,"GdbThreadComponent getEPDCStack DU="+_DU+" fileName="+fileName(i)+" methodName="+methodName(i,true)+"  _callStack.length-1="+(_callStack.length - i) );
         stackChange.addStackEntry(entry);
      }

      return stackChange;
   }
    
   public void setGdbThread(GdbThread gdbThread)
   {
      _gdbThread = gdbThread;
      String L = gdbThread._fileLine;
      if(L==null || L.equals("") )
         setLineNumber( -1 );
      else
      setLineNumber( Integer.parseInt( L ) );
   }
   public GdbThread getGdbThread()
   {   return _gdbThread; }

    private  GdbStackFrame[] _callStack;
    public void setLineNumber(int i) 
    {
       _lineNumber = i; 
    }
    
/*    
    private int convertLineNum(int line, int partID)
    {
        // convert to correct disassembly line number
		ModuleManager moduleManager = _debugSession.getModuleManager();            
		GdbPart part = (GdbPart)moduleManager.getPart(partID);
		int disNum;

		if (part != null)
		{			
			String partName = part.getName();           
			GdbDisassemblyView disassemblyView = (GdbDisassemblyView)part.getView(Part.VIEW_DISASSEMBLY);
			String address = ((GdbDebugSession)_debugSession)._getGdbFile.convertSourceLineToAddress(partName,String.valueOf(line));
			String disLineNum = null;
			
			if (address != null)
				disLineNum = disassemblyView.convertAddressToDisassemblyLine(address); 
			
			if (disLineNum != null)
			{
				disNum = Integer.parseInt(disLineNum);
			}
			else
			{
				disNum = line;
			}
		}
		else
		{
			disNum = 0;
		}
		
		return disNum;
    }
    */
}
