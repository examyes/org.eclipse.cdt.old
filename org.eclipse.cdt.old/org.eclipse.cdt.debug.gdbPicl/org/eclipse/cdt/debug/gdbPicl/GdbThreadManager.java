/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package com.ibm.debug.gdbPicl;
import  com.ibm.debug.gdbPicl.objects.*;
import  com.ibm.debug.gdbPicl.gdbCommands.GdbProcess;
import  com.ibm.debug.gdbPicl.gdbCommands.GetGdbThreads;

import java.util.Vector;
import com.ibm.debug.epdc.*;

/**
 * Manages process threads and creates EPDC program state change packets
 */
public class GdbThreadManager extends ThreadManager     //HC
{
   GdbDebugSession _gdbDebugSession = null;
   Vector _gdbThreads = new Vector();

  /**
   * Create a new ThreadManager
   * @param session A reference to the DebugSession which created this
   * ThreadManager
   * @see DebugSession
   */
   GdbThreadManager(GdbDebugSession gdbDebugSession) 
   {
      super(gdbDebugSession);
      _gdbDebugSession = gdbDebugSession;
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(1,"GdbThreadManager constructor" );
   }

  /**
   * Update a specific thread by DU
   */
   public void updateThread(int du)
   {

//   Thread.currentThread().dumpStack();

   if(_threads==null)
      _threads = new Vector();
   if(_threads.size()==0)
   {  if (Gdb.traceLogger.ERR) 
          Gdb.traceLogger.err(3,"GdbThreadManager.updateThread _threads.size==0 DU="+du );
      return;
   }

   ThreadComponent tcmp = getThreadComponent(du);
   if(tcmp==null)
   {  if (Gdb.traceLogger.ERR) 
          Gdb.traceLogger.err(3,"GdbThreadManager.updateThread NULL threadComponent DU="+du );
      return;
   }
   tcmp.setChanged();


   String file = _gdbDebugSession.getCurrentFileName();
   GdbModuleManager mm = (GdbModuleManager)_gdbDebugSession.getModuleManager();
   String mainProgram = _gdbDebugSession.getProgramName();
   tcmp.setModuleID(mm.getModuleID(mainProgram));
   tcmp.setPartID(mm.getPartID(tcmp.getModuleID(), file));
   if (Gdb.traceLogger.EVT) 
       Gdb.traceLogger.evt(2,"GdbThreadManager.updateThread DU="+du+" file="+file+" program="+mainProgram+" moduleID="+tcmp.getModuleID()+" partID="+tcmp.getPartID() );
   tcmp.setIsZombie(false);
   tcmp.setState(EPDC.StdThdRunnable);
   tcmp.setPartial(false);
   tcmp.setThawed(true);
//    _changed = true;     //???????????????


	  GdbThreadComponent tc = (GdbThreadComponent) _threads.elementAt(du-1);

	  if (tc == null)
		 return;

	  _changed = true;
	  try
	  {
		 GdbStackFrame[] callStack = null;


		 try {
         if (Gdb.traceLogger.EVT) 
             Gdb.traceLogger.evt(2,"GdbThreadManager.updateThread du="+du +" calling ThreadComponent.getCallStack "   );
			callStack = tc.getCallStack();
		 }
		 catch (Exception e) {
		   String message = e.getMessage();
		 }
		 tc.update(callStack);

	  }
	  catch (Exception e)
	  {
		 Gdb.handleException(e);
	  }

   }

   void getGdbThreads()
   {
      _gdbThreads = _gdbDebugSession._getGdbThreads.getThreads();
   }

/**
   * Update list of threads to match threads known by the debugger.
   */
public void updateThreads()
{
   if (Gdb.traceLogger.EVT) 
       Gdb.traceLogger.evt(1,"================ GdbThreadManager.updateThreads()"  );

   getGdbThreads();

   if(_threads==null)
      _threads = new Vector();

	GdbThreadComponent tc = null;
	boolean found;

	// Phase 1 - remove threads that have terminated or are in the process
	//           of terminating
	for (int j = 0; j < _threads.size(); j++)
	{
		tc = (GdbThreadComponent) _threads.elementAt(j);
		if (tc != null)
		{
				// This thread WAS alive but is now a zombie, kill it.
				if (tc.threadStatus().equals("zombie"))
				{
					_threadNameIndex.remove(tc.threadName());
					tc.setTerminated();
					_changed = true;
				}
		}
	}

/*
	// Get which thread stopped us last
	RemoteThread stopThread = ((SuntoolsDebugSession) _debugEngine.getDebugSession()).stopThread();
	Gdb.debugOutput("GdbThreadManager: updateThreads() - StopThread = " + stopThread);
*/

	// Phase 2 - go through list of threads from the debug API and add new threads
	//           and update existing threads


	// IMPORTANT NOTE:  We DO NOT use RemoteThread.getName() when storing
	// thread names.  We use description() since getName() is not unique
	// accross multiple thread groups.
	_changed = true;
//	for (int z = 0; z < threadList.length); z++)
//	{ // loop through thread groups
//		Gdb.debugOutput("Updating " + threadList[z].length + " user threads in thread group " + z);

      

//for (int i = (_gdbThreads.size()-1); i >=0 ; i--)
//		{ // loop through threads
//         GdbThread gdbThread = (GdbThread)_gdbThreads.elementAt(i);

//      for (Enumeration en = _gdbThreads.elements() ; en.hasMoreElements() ;) 
      for (int z = 0; z < _gdbThreads.size(); z++) 
      {  
         GdbThread gdbThread = null;
         if(_gdbThreads.elementAt(z)!=null)
             gdbThread = (GdbThread) _gdbThreads.elementAt(z);
         if(gdbThread==null)  
            continue;

         if(gdbThread.isCurrentThread())
         {   _stoppingThread = gdbThread.getIntThreadID();
             _currentThread = gdbThread.getIntThreadID();
             if (Gdb.traceLogger.EVT) 
                 Gdb.traceLogger.evt(1,"<<<<<<<<======== GdbThreadManager.updateThreads STOPPING_THREAD="+gdbThread.getIntThreadID() );
         }

			String description = gdbThread.getThreadName(); 
         if (Gdb.traceLogger.DBG) 
             Gdb.traceLogger.dbg(1,"GdbThreadManager.updateThreads description="+description+" threadID="+gdbThread.getIntThreadID() );
		
			try
			{

				GdbStackFrame[] callStack = null;
				boolean partial = false;
		      if( gdbThread.getStatus().equals("zombie"))
					continue;
/*
				if (stopThread == threadList[z][i])
				{
					partial = false;
					// NOTE: This is the only place we should _ever_ call dumpStack()
					// since it is very expensive to do so!
						Gdb.debugOutput("GdbThreadManager:updateThreads() - Getting call Stack");
						callStack = gdbThread.getStack(); //HC:D10870
				}
*/
            String key = String.valueOf(gdbThread.getIntThreadID());
				if (_threadNameIndex.containsKey(key))
				{
               if (Gdb.traceLogger.EVT) 
                   Gdb.traceLogger.evt(2,"GdbThreadManager.updateThreads _threadNameIndex contains " + key);
					// Thread is already known about.   Find the corresponding ThreadComponent
					// NOTE: DO NOT USE getThreadComponent here or else the tc will be updated regarless of partial setting! Use _threads directly.
					Integer duInt = (Integer) _threadNameIndex.get(key);
					tc = (GdbThreadComponent) _threads.elementAt(duInt.intValue() - 1);
               if (Gdb.traceLogger.EVT) 
                   Gdb.traceLogger.evt(1,"<<<<<<<<-------- GdbThreadManager.updateThreads existing Thread key="+key+" description="+description+" tc.getDU()="+tc.getDU()+" thread.ID="+gdbThread.getIntThreadID()+" thread.Line="+gdbThread.getCurrentLine() );
					tc.setPartial(partial);
               gdbThread.setGdbThreadComponent(tc);
               tc.setGdbThread(gdbThread);
				} else
/*
					if ((callStack == null || callStack.length == 0) && !partial)
					{
						// This thread has no callstack so don't create a tc for him (or her)
						Gdb.debugOutput("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ warning: thread " + key + " has no call stack");
						tc = null;
					} else
*/
					{
						// Thread unknown, we must create a new ThreadComponent #### and a new GdbThread ####
						int DU = _threads.size() + 1;
                  //DU = gdbThread.getIntThreadID();   //?????????????????????????????
                  if (Gdb.traceLogger.EVT) 
                      Gdb.traceLogger.evt(1,"<<<<<<<<======== GdbThreadManager.updateThreads adding Thread DU="+DU+" threadName="+gdbThread.getThreadName()+" systemTID="+gdbThread.getSystemTID()+" key="+key+" description="+description );
//?????????????????????????						tc = new GdbThreadComponent(_debugEngine, DU, gdbThread);
                  tc = gdbThread.getGdbThreadComponent();  //???????????????????????????????
                  tc.setDU(DU); //?????????????????
//						tc = new GdbThreadComponent(_debugEngine, Integer.parseInt(gdbThread._systemTID), gdbThread);
						tc.setPartial(partial);
						_threads.addElement(tc);
						_threadNameIndex.put(key, new Integer(DU));
					}

					// Update this ThreadComponent's dynamic information (class name,
					// method name, priority, line number, partID)
				if (tc != null) 
				{
					tc.update( tc.getCallStack() );
				}
			} catch (Exception e)
			{
				Gdb.handleException(e);
			}

		} // end of _gdbThreads
//	} // end of threadGroups

}

/*
	This function parses through all call stacks and return
	a list of functions that are associated with the given
	part name.
*/
public Vector getMethodsforPart(String partName)
{
	Vector functions = new Vector();
	GdbThreadComponent tc;
	
	for (int j = 0; j < _threads.size(); j++)
	{
		tc = (GdbThreadComponent) _threads.elementAt(j);
		if (tc != null)
		{
			GdbStackFrame[] stackFrames = tc.getCallStack();
			
			for (int i=0; i < stackFrames.length; i++)
			{						
				if (stackFrames[i].getFileName().equals(partName))
				{
					if (Gdb.traceLogger.DBG) 
					{
						Gdb.traceLogger.dbg(1,"GdbThreadManager.getMethodsforPart filename: " + stackFrames[i].getFileName());
						Gdb.traceLogger.dbg(1,"GdbThreadManager.getMethodsforPart Matched:  " + stackFrames[i].getMethodName());
					}						
						
					functions.add(stackFrames[i].getMethodName());
				}
			}
		}
	}
	
	return functions;
}

/*
	This function parses through all call stacks and return
	a list of frame addresses that are associated with the given
	part name.
*/
public Vector getFrameAddressforPart(String partName)
{
	Vector linesNum = new Vector();
	GdbThreadComponent tc;
	
	for (int j = 0; j < _threads.size(); j++)
	{
		tc = (GdbThreadComponent) _threads.elementAt(j);
		if (tc != null)
		{
			GdbStackFrame[] stackFrames = tc.getCallStack();
			
			for (int i=0; i < stackFrames.length; i++)
			{				
//				if (stackFrames[i].getFileName().equals(partName))
				if (partName.endsWith(stackFrames[i].getFileName()))
				{
					if (Gdb.traceLogger.DBG) 
					{
						Gdb.traceLogger.dbg(1,"GdbThreadManager.getFrameAddressforPart filename: " + stackFrames[i].getFileName());
						Gdb.traceLogger.dbg(1,"GdbThreadManager.getFrameAddressforPart Matched:  " + stackFrames[i].getFrameAddress());
					}
					linesNum.add(stackFrames[i].getFrameAddress());
				}
			}
		}
	}
	
	return linesNum;
}

/*
	This function parses through all call stacks and return
	a list of frame addresses that are associated with the given
	part name.
*/
public Vector getLineNumforPart(String partName)
{
	Vector linesNum = new Vector();
	GdbThreadComponent tc;
	
	for (int j = 0; j < _threads.size(); j++)
	{
		tc = (GdbThreadComponent) _threads.elementAt(j);
		if (tc != null)
		{
			GdbStackFrame[] stackFrames = tc.getCallStack();
			
			for (int i=0; i < stackFrames.length; i++)
			{				
//				if (stackFrames[i].getFileName().equals(partName))
				if (partName.endsWith(stackFrames[i].getFileName()))
				{
					if (Gdb.traceLogger.DBG) 
					{
						Gdb.traceLogger.dbg(1,"GdbThreadManager.getLinesNumforPart filename: " + stackFrames[i].getFileName());
						Gdb.traceLogger.dbg(1,"GdbThreadManager.getLinesNumforPart Matched:  " + stackFrames[i].getLineNumber());
					}
					linesNum.add(new Integer(stackFrames[i].getLineNumber()));
				}
			}
		}
	}
	
	return linesNum;
}

}
