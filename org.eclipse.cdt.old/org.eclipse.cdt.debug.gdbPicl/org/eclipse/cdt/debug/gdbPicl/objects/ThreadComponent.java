/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package com.ibm.debug.gdbPicl.objects;
import  com.ibm.debug.gdbPicl.*;

import java.util.*;
import com.ibm.debug.epdc.*;
//import sun.tools.debug.*;

/*
 * This class stores the latest information about a thread.  All dynamic
 * thread information (eg. call stack, priority, line number, etc) is
 * encapsulated in this class.  When this information is known to have 
 * changed, the update() method should be called.  For efficiency, all
 * dynamic information about a thread should be taken from this class
 *
 */

public abstract class ThreadComponent    //HC
{
   //HC: remove a constructor here since it depends on RemoteThread

   ThreadComponent(DebugSession debugSession, int DU)  //HC
   {
	  // The following attributes are set once
      _DU             = DU;
      _debugSession    = debugSession;
      
      // The following attributes are dynamic and any change should trigger
      _lineNumber    = 0;
      _partID        = 0;
      _moduleID      = 0;
      _priority      = 0;
      _isZombie      = true;
      _state         = EPDC.StdThdUnknown;
      _dbgState      = EPDC.StdThdThawed;
      _stackReported = false;
      _stackTracking = false;
      _isPartial     = false;

      _prevLineNumber    = -1;
      _prevPartID        = -1;
      _prevModuleID      = -1;
      _prevPriority      = -1;
      _prevIsZombie      = true;
      _prevState         = EPDC.StdThdUnknown;
      _prevDbgState      = EPDC.StdThdThawed;
      _prevIsPartial     = false;
   }
   
   /**
    * Returns the thread DU for this component
    */
   public int getDU()
   {
      return _DU;
   }
   /**
    * sets the thread DU for this component
    */
   public void setDU(int i)
   {
      _DU = i;
   }

   /**
    * Returns the thread ID for this component
    */
   public int getID()
   {
      return _ID;
   }
   /**
    * sets the thread ID for this component
    */
   public void setID(int i)
   {
      _ID = i;
   }
   
   /**
    * Get the partID for this thread
    */
   public int partID() 
   {
      return _partID;
   }

   /**
    * Returns whether this thread will report partial information status.
    */
   public boolean isPartial() 
   {
      return _isPartial;
   }

   /**
    * Get the partID for this thread
    */
   public void setPartial(boolean partialStatus) 
   {
      _isPartial = partialStatus;
   }
  
   /**
    * Returns the size of the call stack for this thread
    */
   public abstract int getCallStackSize(); //HC

  /**
   * Return the thread name 
   */
   public abstract String threadName();

   /**
    * Get the current lineNumber for the specified stackEntry.  Use 0
    * for the top of the stack.
    */
   public abstract int lineNumber(int stackEntry); //HC
   
   /**
    * Get the current fileName for the specified stackEntry.  Use 0
    * for the top of the stack.
    */
   public abstract String fileName(int stackEntry); //HC
   

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

   public abstract String methodName(int stackEntry, boolean signature); //HC
   

  /**
   * Verify the views for the Part associated with the current location.
   * Important: update() must have been called prior to this.  This function
   * relies on update() to update _callStack.
   */
   void verifyPartViews()
   {
      if (_partID != 0)
      {
         Part part = _debugSession.getModuleManager().getPart(_partID);
 
         if (part != null)
         {
            part.verifyViews();
         }
      }
   }

   /** Sets the thread priority */
   void setPriority(int priority)
   {
      _priority = priority;
   }

   /** Sets whether this thread is thawed (true) or frozen (false) */
   public void setThawed(boolean thawed)
   {
      _dbgState = thawed ? EPDC.StdThdThawed : EPDC.StdThdFrozen;
   }

   /** Returns whether this thread is thawed (true) or frozen (false) */
   public boolean isThawed()
   {
      return (_dbgState == EPDC.StdThdThawed);
   }

   /**
    * Returns ERepGetNextThread change item for this thread
    */
   public ERepGetNextThread getEPDCThread()
   {
      ERepGetNextThread EPDCThread;

      // Introduced in EPDC level 307 is the ability to send attributes on the 
      // thread change packet.   The following gathers the information into attributes

      // The derived class must setup the attributes prior to calling this method
      // because the attributes are specific to the type of thread

      // The view info on where stopped is setup after creation of the ERepGetNextThread
      
      // temporary fix, if part ID <= 0, force it to run to avoid infinite loop
      if (_partID <= 0)
      {
       	_partID = 1;
      }     
      
      EStdView[] whereStopped = new EStdView[Part.NUM_VIEWS];
      whereStopped[Part.VIEW_SOURCE-1] = new EStdView((short)_partID,
                                                      (short)Part.VIEW_SOURCE,
                                                      1,
                                                      _lineNumber);
      whereStopped[Part.VIEW_DISASSEMBLY-1] = new EStdView((short)_partID,
                                                      (short)Part.VIEW_DISASSEMBLY,
                                                      1,
                                                      convertLineNum(_lineNumber, _partID));
	  if (Part.MIXED_VIEW_ENABLED)                                                      
	  {
	      whereStopped[Part.VIEW_MIXED-1] = new EStdView((short)_partID,
                                                      (short)Part.VIEW_MIXED,
                                                      1,
                                                      _lineNumber);
	  }
      
      EPDC_EngineSession _engineSession = _debugSession.getDebugEngine().getSession();
      EPDCThread = new ERepGetNextThread(_engineSession,
                                         _dbgState,
                                         _DU,
                                         whereStopped,
                                         _threadAttributes);
                                         
                                         
      // Handle partial thread support

      if (_isPartial || _isZombie)
      {
         EPDCThread.setPartialThreadInfo(Part.VIEW_SOURCE);
         EPDCThread.setPartialThreadInfo(Part.VIEW_DISASSEMBLY);
         if (Part.MIXED_VIEW_ENABLED)
	         EPDCThread.setPartialThreadInfo(Part.VIEW_MIXED);
      }
      

      return EPDCThread;
   }

   /**
    * Update file info for all classes on the stack
    */
   public abstract void updateFilesOnStack();    //HC
   
   /**
    * Returns whether the part this thread is in is debuggable or not.
    */
   public boolean isDebuggable() 
   {
      return _debugSession.getModuleManager().partIsDebuggable(_partID);
   }

   /**
    * Get stack change info packet for this thread.  If there is no call stack
    * this method will return a change packed with 0 entries.
    */
   public abstract ERepGetChangedStack getEPDCStack();		  //HC

   /**
    * Free the call stack associated with this thread
    */
   public void freeCallStack() 
   {
      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(2,"ThreadComponent.freeCallStack" );
      _stackReported = false;
      _stackTracking = false;
   }
   /**
    * Monitor the call stack associated with this thread
    */
   public void monitorCallStack() 
   {
      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(2,"ThreadComponent.monitorCallStack" );
      _stackTracking = true;
	  if (Gdb.traceLogger.DBG) 
              Gdb.traceLogger.dbg(3,"@@@@ ThreadComponent monitorCallStack _stackTracking==true");
   }

   /**
    * Set this ThreadComponent as terminated.
    */
   public void setTerminated()
   {
      _dbgState   = EPDC.StdThdTerminated;
   }

   /**
    * Returns whether this ThreadComponent has terminated.
    */
   public boolean isTerminated()
   {
      return (_dbgState == EPDC.StdThdTerminated);
   }

   /**
    * Force this ThreadComponent to report that its state has changed.
    */
   public void setChanged()
   {
      _prevLineNumber = -1;
      _prevPartID     = -1;
      _prevModuleID   = -1;
   }

   public boolean hasRegistersChanged()
   {
      return  _registersChanged;
   }
  /**
    * Returns whether this ThreadComponent's state has changed since the last
    * call.
    */
   public boolean hasChanged()
   {
      boolean hasChanged = false;

      // Organize these from most frequent to least frequent for efficiency
      if (
          _lineNumber != _prevLineNumber ||
          _partID     != _prevPartID     ||
          _moduleID   != _prevModuleID   ||
          _isZombie   != _prevIsZombie   ||
          _state      != _prevState      ||
          _dbgState   != _prevDbgState   ||
          _priority   != _prevPriority   ||
          _isPartial  != _prevIsPartial
         )
      {
         hasChanged = true;
      }

      _prevLineNumber = _lineNumber;
      _prevPartID     = _partID;
      _prevModuleID   = _moduleID;
      _prevIsZombie   = _isZombie;
      _prevState      = _state;
      _prevDbgState   = _dbgState;
      _prevPriority   = _priority;
      _prevIsPartial  = _isPartial;

      return hasChanged;
   }

   /**
    * Returns whether this ThreadComponent is currently blocked.
    */
   public boolean isBlocked()
   {
      return _state == EPDC.StdThdBlocked ? true : false;
   }

   /**
    * Returns whether this ThreadComponent is a zombie (ie. no callstack).
    */
   public boolean isZombie()
   {
      return _isZombie;
   }
   public void setIsZombie(boolean b)
   {
      _isZombie = b;
   }

   /**
    * Returns dbgState
    */
   short getDbgState()
   {
      return _dbgState;
   }

   /**
    * Returns State
    */
//   short getState()
//   {
//      return _state;
//   }
   /**
    * sets State
    */
   public void setState(short i)
   {
      _state = i;
   }


   /**
    * Returns moduleID
    */
   public int getModuleID()
   {
      return _moduleID;
   }
   /**
    * Sets moduleID
    */
   public void setModuleID(int i)
   {
      _moduleID = i;
   }


   /**
    * Returns partID
    */
   public int getPartID()
   {
      return _partID;
   }
   /**
    * Sets partID
    */
   public void setPartID(int i)
   {
      _partID = i;
   }
   
    public int convertLineNum(int line, int partID)
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
  

   // data fields
   protected DebugSession  _debugSession;
   protected int          _DU;
   protected int          _ID;
  

   protected int          _lineNumber;
   protected int          _partID;
   protected int          _moduleID;
   protected int          _priority;
   protected short        _state;
   private   short        _dbgState;
   protected boolean      _isZombie;
   private   boolean      _isPartial;

   private int          _prevLineNumber;
   private int          _prevPartID;
   private int          _prevModuleID;
   private int          _prevPriority;
   private short        _prevState;
   private short        _prevDbgState;
   private boolean      _prevIsZombie;
   private boolean      _prevIsPartial;
    

   protected boolean            _stackTracking;
   protected boolean            _stackReported;
   protected boolean            _registersTracking;
   protected boolean            _registersReported;
   protected EStdAttribute[] _threadAttributes;   

   protected boolean  _registersChanged = false;
   protected String[] _generalNames   = null;
   protected String[] _generalValues  = null;
   protected String[] _floatNames     = null;
   protected String[] _floatValues    = null;
}
