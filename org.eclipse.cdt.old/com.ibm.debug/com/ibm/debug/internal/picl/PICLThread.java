package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// %W%
// Version %I% (last modified %G% %U%)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.epdc.EPDC;
import com.ibm.debug.jni.control.IInternalThread;
import com.ibm.debug.jni.control.InternalStepEvent;
import com.ibm.debug.model.*;
import org.eclipse.debug.core.*;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.*;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 * Instances of this class represent a thread in the PICL debug model
 */
public class PICLThread extends PICLDebugElement implements IRegisterSupport, IThread, StackEventListener, DebuggeeThreadEventListener, IInternalThread {

	// Resource String keys
	private static final String PREFIX= "picl_thread.";
	private static final String DEBUG_STATE= PREFIX + "debug_state.";
	private static final String FROZEN= DEBUG_STATE + "frozen";
	private static final String THAWED= DEBUG_STATE + "thawed";
	private static final String UNRECOGNIZED= DEBUG_STATE + "unrecognized";
	private static final String LABEL= PREFIX + "label.";
	private static final String NORMAL= LABEL + "normal";
	private static final String SYSTEM= LABEL + "system";
	private static final String RUNNING= LABEL + "running";
	private static final String SUSPENDED0= LABEL + "suspended0";
	private static final String SUSPENDED1= LABEL + "suspended1";
	private static final String SUSPENDED2= LABEL + "suspended2";
	private static final String SUSPENDED3= LABEL + "suspended3";
	private static final String BREAKPOINT = LABEL + "breakpoint";
	private static final String STEPPING= LABEL + "stepping";
	private static final String DEFAULT= LABEL + "default";
	private static final String EXCEPTION= LABEL + "exception";
	private static final String STATE= PREFIX + "state.";
	private static final String TERMINATED= STATE + "terminated";
	private static final String RUNNABLE= STATE + "runnable";
	private static final String BLOCKED= STATE + "blocked";
	private static final String SUSPENDED_STATE= STATE + "suspended";
	private static final String CRITICAL_SECTION= STATE + "critical_section";
	private static final String UNKNOWN_STATE= STATE + "unknown";
	private static final String UNRECOGNIZED_STATE= STATE + "unrecognized";
	private static final String ERROR= PREFIX + "error.";
	private static final String ENGINE_BUSY= ERROR + "engine_busy";
	private static final String CLIENT_FAILURE= ERROR + "client_failure";
	private static final String NO_THREAD_TERM= ERROR + "no_thread_termination";
	private static final String STEP_REQUEST_FAILED= ERROR + "step_request_failed";

	/**
	 * The IBM debug model thread this PICL element is a proxy for.
	 */
	protected DebuggeeThread fDebuggeeThread;

	/**
	 * This class is used as the parent of the register groups
	 */
	private PICLRegisterGroupParent fRegisterGroupParent = null;

	/**
	 * The IBM debug model stack for this PICL thread
	 */
	protected Stack fStack;
	/**
	 * The local expressions monitor for this stack frame.
	 */
	protected LocalMonitoredExpressions fLocalExpressionsMonitor;
	/**
	 * Is the thread currently stepping
	 */
	protected boolean fStepping= false;

	/**
	 * Cached copy of the underlying DebuggeeThread's name
	 */
	protected String fName;

	/**
	 * Cached copy of the underlying DebuggeeThread's priority
	 */
	protected int fPriority;

	/**
	 * Indicates that this thread is monitoring its stack
	 */

	private boolean fMonitoringStack = false;


	/**
	 * Creates a PICL thread.
	 */
	public PICLThread(IDebugTarget parent, DebuggeeThread dThread) {
		super(parent, IDebugElement.THREAD);
		fDebuggeeThread= dThread;
		loadCachedPropertyValues();
		fDebuggeeThread.addEventListener(this);
			}

	/**
	 * Returns an object that is the parent of the register groups.
	 * @return PICLRegisterGroupParent that has PICLRegisterGroups as children
	 */
	public PICLRegisterGroupParent getRegisterGroupParent() {

		if (haveDoneCleanup())
			return null;

		if (fRegisterGroupParent != null)
			return fRegisterGroupParent;

		if (supportsRegisters()) {
			fRegisterGroupParent = new PICLRegisterGroupParent(getDebugTarget());
    		// get list of register groups from the engine
    		RegisterGroup[] engineRegisterGroups = null;
    		try {
    			engineRegisterGroups = getDebugEngine().getRegisterGroups();
    		} catch(IOException ioe) {}

    		if (engineRegisterGroups == null)
    			return null;

    		// now create an array of PICLRegisterGroups
    		for (int i=0; i < engineRegisterGroups.length; i++) {
    			fRegisterGroupParent.addChild(new PICLRegisterGroup(fRegisterGroupParent,this,engineRegisterGroups[i]));
    		}
    	}

    	return fRegisterGroupParent;
	}

	/**
	 * Retrieve property values from the underlying DebuggeeThread and cache them
	 * in this object.  In this way, a PICLThread acts as a handle to a
	 * DebuggeeThread, allowing the cached properties to be accessed even after
	 * the DebuggeeThread is gone.
	 */
	protected void loadCachedPropertyValues() {
		fName= fDebuggeeThread.getNameOrTID().getValue();
//		fPriority= Integer.parseInt(fDebuggeeThread.getPriority().getValue());
        fPriority= fDebuggeeThread.priority();
       }

	/**
	 * Adds this child as the first child in the collection of children for this thread.
	 * Fires a creation event for the child.
	 */
	public void addChild(IDebugElement child) {
		boolean added= false;
		if (fChildren == fgEmptyChildren) {
			fChildren= new Vector();
			fChildren.add(child);
			added= true;
		} else {
			if (!fChildren.contains(child)) {
				fChildren.add(0, child);
				added= true;
			}
		}

		if (added) {
			((PICLDebugElement) child).fireCreationEvent();
		}
	}

    /**
     * @see ISuspendResume#canResume()
     */
	public boolean canResume() {
		return ((IDebugTarget) getParent()).canResume();
	}

    /**
     * @see ISuspendResume#canSuspend()
     */
	public boolean canSuspend() {
		return ((IDebugTarget) getParent()).canSuspend();
	}

	/**
	 * @see ITerminate#canTerminate()
	 */
	public boolean canTerminate() {
		return false;
	}

	/**
	 * @see IStep#canStepInto()
	 */
	public boolean canStepInto() {
		return !haveDoneCleanup() & isSuspended();
	}

	/**
	 * @see IStep#canStepOver()
	 */
	public boolean canStepOver() {
		return !haveDoneCleanup() & isSuspended();
	}

	/**
	 * @see IStep#canStepReturn
	 */
	public boolean canStepReturn() {
		return !haveDoneCleanup() & isSuspended();
	}

	/**
	 * Supported by PICL but not available in the debug core yet...
	 */
	public boolean canStepDebug() {
		return !haveDoneCleanup() & isSuspended();
	}

	/**
	 * If the process is running, return an empty collection, otherwise
	 * return my real children.
	 *
	 * @see IDebugElement
	 */
	public IDebugElement[] getChildren() throws DebugException {
		if (isSuspended()) {
			if (!fMonitoringStack)
				monitorStack();

			return super.getChildren();
		} else {
			return null;
		}

	}

	/**
	 * @see PICLDebugElement#getChildrenNoExpand()
	 * Return the children without getting children from the engine.
	 */
	public IDebugElement[] getChildrenNoExpand() throws DebugException {
		return super.getChildren();
	}


	/**
	 * Returns a <code>String</code> that represents the debug state of the
	 * underlying debuggee thread.
	 */
	protected String getDebugState() {
		if (haveDoneCleanup()) {
			return "";
		}
		short result= fDebuggeeThread.debugState();
		if (result == EPDC.StdThdFrozen) {
			return PICLUtils.getResourceString(FROZEN);
		}
		if (result == EPDC.StdThdThawed) {
			return PICLUtils.getResourceString(THAWED);
		}
		return PICLUtils.getResourceString(UNRECOGNIZED);
	}




	/**
	 * @see IDebugElement
	 */
	public String getLabel(boolean qualified) {

		if (haveDoneCleanup())
			return PICLUtils.getResourceString(TERMINATED);

		PICLDebugTarget target= (PICLDebugTarget) getParent();
		StringBuffer labelString= new StringBuffer();
		String name= getName();
		int threadNumber = 0;

		try {
			threadNumber = Integer.parseInt(name);
		} catch (NumberFormatException e) {
		}
		if (threadNumber > 7) {
			labelString.append(PICLUtils.getResourceString(NORMAL));
		} else {
			labelString.append(PICLUtils.getResourceString(SYSTEM));
		}
		if (!target.isSuspended() && !target.isTerminated()) {
			labelString.append(PICLUtils.getFormattedString(RUNNING, name));
		} else if (isStoppedByException()) {
			labelString.append(PICLUtils.getFormattedString(EXCEPTION, new String[] {name, getStoppingExceptionName()}));
		} else if (isStoppedByBreakpoint()) { // See PR 1G4CGHX
			labelString.append(getSuspendedLabel(name));
		} else if (isStepping()) {
			labelString.append(PICLUtils.getFormattedString(STEPPING, name));
		} else if (target.isSuspended()){
			labelString.append(PICLUtils.getFormattedString(SUSPENDED0, name));
		} else {
			labelString.append(PICLUtils.getFormattedString(DEFAULT, new String[] {name, getState(), getDebugState()}));
		}
		return labelString.toString();
	}

	/**
	 * Returns whether this thread was stopped by encountering an exception
	 */
	protected boolean isStoppedByException() {

		if (haveDoneCleanup())
			return false;

		PICLDebugTarget target= (PICLDebugTarget)getParent();
		if (!target.isStoppedByException()) {
			return false;
		}
		DebuggeeProcess debuggeeProcess= target.getDebuggeeProcess();
		if (debuggeeProcess == null) {
			return false;
		}
		ProcessStopInfo stopInfo= debuggeeProcess.getProcessStopInfo();
		if (stopInfo == null) {
			return false;
		}
		DebuggeeThread debuggeeThread= stopInfo.getThreadThatCausedProcessToStop();
		if (debuggeeThread == null) {
			return false;
		}
		return (debuggeeThread == fDebuggeeThread);
	}

	/**
	 * Return the String name (exception message) of the exception event that stopped this process
	 */
	protected String getStoppingExceptionName() {

		if (haveDoneCleanup())
			return "";

		if (!isStoppedByException()) {
			return "";
		}
		PICLDebugTarget target= (PICLDebugTarget)getParent();
		if (target == null) {
			return "";
		}
		ExceptionRaisedEvent exceptionEvent= target.getExceptionEventThatStoppedProcess();
		if (exceptionEvent == null) {
			return "";
		}
		return exceptionEvent.exceptionMsg();
	}

	/**
	 * Returns whether this thread was stopped by encountering a breakpoint
	 */
	protected boolean isStoppedByBreakpoint() {

		if (haveDoneCleanup())
			return false;

		PICLDebugTarget target= (PICLDebugTarget)getParent();
		if (!target.isStoppedByBreakpoint()) {
			return false;
		}
		DebuggeeProcess debuggeeProcess= target.getDebuggeeProcess();
		if (debuggeeProcess == null) {
			return false;
		}
		ProcessStopInfo stopInfo= debuggeeProcess.getProcessStopInfo();
		if ( (stopInfo == null) || (stopInfo.getReason() != EPDC.Why_break) ) {
			return false;
		}
		DebuggeeThread debuggeeThread= stopInfo.getThreadThatCausedProcessToStop();
		if (debuggeeThread == null) {
			return false;
		}
		return (debuggeeThread.equals(fDebuggeeThread));
	}


	/**
	 * Return a formatted String that can serve as a label for this Thread when it is suspended
	 */
	protected String getSuspendedLabel(String name) {

		if (haveDoneCleanup())
			return "";

		IStackFrame topStackFrame= getTopStackFrame();
		if (topStackFrame == null) {
			return PICLUtils.getFormattedString(SUSPENDED0, name);
		}
		return PICLUtils.getFormattedString(BREAKPOINT, name);
//		ISourceLocation sourceLocation= topStackFrame.getSourceLocation();
//		if (sourceLocation == null) {
//			return PICLUtils.getFormattedString(SUSPENDED0, name);
//		}
//		String lineNumString= String.valueOf(sourceLocation.getLineNumber());
//		IPath path= sourceLocation.getPath();
//		if (path == null) {
//			return PICLUtils.getFormattedString(SUSPENDED1, new String[] {name, lineNumString});
//		}
//		String lastSegment= path.lastSegment();
//		if ( (lastSegment == null) || (lastSegment.length() < 1) ) {
//			return PICLUtils.getFormattedString(SUSPENDED2, new String[] {name, lineNumString});
//		}
//		return PICLUtils.getFormattedString(SUSPENDED3, new String[] {name, lineNumString, lastSegment});
	}

	/**
	 * Returns the local expressions monitor for this thread.
	 */
	protected LocalMonitoredExpressions getLocalExpressionsMonitor() {
		return fLocalExpressionsMonitor;
	}

	/**
	 * @see IDebugElement
	 */
	public String getName() {
		return fName;
	}

	/**
	 * @see IThread
	 */
	public int getPriority() {
		return fPriority;
	}

	/**
	 * Returns a <code>String</code> that represents the state of the
	 * underlying debuggee thread.
	 */
	protected String getState() {

		if (haveDoneCleanup())
			return PICLUtils.getResourceString(TERMINATED);
		else
			return fDebuggeeThread.getState().getValue();

//		if (haveDoneCleanup()) {
//			return PICLUtils.getResourceString(TERMINATED);
//		}
//		if (fDebuggeeThread == null) {
//			return PICLUtils.getResourceString(UNRECOGNIZED_STATE);
//		}
//
//
//		short result= Short.parseShort(fDebuggeeThread.getState().getValue());
//		if (result == EPDC.StdThdRunnable) {
//			return PICLUtils.getResourceString(RUNNABLE);
//		}
//		if (result == EPDC.StdThdSuspended) {
//			return PICLUtils.getResourceString(SUSPENDED_STATE);
//		}
//		if (result == EPDC.StdThdBlocked) {
//			return PICLUtils.getResourceString(BLOCKED);
//		}
//		if (result == EPDC.StdThdCritSect) {
//			return PICLUtils.getResourceString(CRITICAL_SECTION);
//		}
//		if (result == EPDC.StdThdTerminated) {
//			return PICLUtils.getResourceString(TERMINATED);
//		}
//		if (result == EPDC.StdThdUnknown) {
//			return PICLUtils.getResourceString(UNKNOWN_STATE);
//		}
//		return PICLUtils.getResourceString(UNRECOGNIZED_STATE);
	}

	/**
	 * @see IDebugElement
	 */
	public IThread getThread() {
		return this;
	}

	/**
	 * @see IThread
	 */
	public IStackFrame getTopStackFrame() {

		if (haveDoneCleanup())
			return null;

		if (!fMonitoringStack)
			monitorStack();

		List c= getChildrenAsList();
		if (c.isEmpty()) {
			return null;
		} else {
			return (IStackFrame) c.get(0);
		}
	}

	/**
	 * Returns the <code>ViewInformation</code> object currently used to
	 * get the view for this thread element
	 */
	public ViewInformation getViewInformation() {

		if (haveDoneCleanup())
			return null;

		PICLStackFrame frame = (PICLStackFrame) getTopStackFrame();
		if (frame != null)
			return frame.getViewInformation();
		else
			// Can this ever happen ??
			return getDebugEngine().getSourceViewInformation();
	}

	/**
	 * Returns <code>true</code> if this thread is frozen,
	 * otherwise <code>false</code>. This is a proxy-only method.
	 * Threads which are frozen cannot be started until thawed.
	 * Note, if this thread's debug engines does not support
	 * thread freeze/thaw, <code>false</code> is returned.
	 */
	public boolean isFrozen() {

		if (haveDoneCleanup())
			return false;
		else
			return Short.parseShort(fDebuggeeThread.getState().getValue()) == EPDC.StdThdFrozen;
	}

	/**
	 * @see IStep#isStepping()
	 */
	public boolean isStepping() {
		return fStepping;
	}

    /**
     * @see ISuspendResume#isSuspended()
     */
	public boolean isSuspended() {

		if (haveDoneCleanup())
			return false;
		else
			return ((IDebugTarget) getParent()).isSuspended();
	}

	/**
	 * @see ITerminate#isTerminated()
	 */
	public boolean isTerminated() {

		if (haveDoneCleanup())
			return true;
		else
			return false;
	}

	/**
     * @see DebuggeeThreadEventListener#localExpressionsMonitorAdded(LocalMonitoredExpressionsAddedEvent)
     */

	public void localExpressionsMonitorAdded(LocalMonitoredExpressionsAddedEvent event) {
    	PICLUtils.logEvent("local expression monitor added",this);

		fLocalExpressionsMonitor= event.getLocalExpressionsMonitor();
	}

	/**
	 * @see com.ibm.debug.model.DebuggeeThreadEventListener
	 */
	public void monitoredRegisterGroupAdded(MonitoredRegisterGroupAddedEvent event) {
    	PICLUtils.logEvent("monitored register group added",this);

		PICLDebugTarget debugTarget = (PICLDebugTarget)getDebugTarget();

        if (debugTarget.getPendingEngineRequest() instanceof MonitorRegisterGroupRequest)
	        ((MonitorRegisterGroupRequest)debugTarget.getPendingEngineRequest()).setMonitoredRegisterGroup(event.getMonitoredRegisterGroup());
	    else
	    	PICLUtils.logText("Mismatch... expected monitor register group pending request");

	}

	/**
	 * Monitor locals for this thread.
	 */
	protected void monitorLocalExpressions(int stackEntryNum) {
		if (!getDebugEngine().isAcceptingSynchronousRequests()) {
			logError(new DebugException(new Status(IStatus.ERROR,"com.ibm.debug",IDebugStatusConstants.TARGET_REQUEST_FAILED, PICLUtils.getFormattedString(ENGINE_BUSY, "PICLThread#monitorLocalExpressions"), null)));
			return;
		}
		try {
			if (!fDebuggeeThread.monitorLocalVariables(DebugEngine.sendReceiveSynchronously,stackEntryNum)) {
				logError(new DebugException(new Status(IStatus.ERROR,"com.ibm.debug",IDebugStatusConstants.TARGET_REQUEST_FAILED, "PICLThread#monitorLocalExpressions", null)));
			}
		} catch (IOException ioe) {
			logError(new DebugException(new Status(IStatus.ERROR,"com.ibm.debug",IDebugStatusConstants.TARGET_REQUEST_FAILED, PICLUtils.getResourceString(COMM_FAILURE), ioe)));
		}
	}

	/**
	 * Monitor the stack of this thread.
	 */
	protected void monitorStack() {
		if (!getDebugEngine().isAcceptingSynchronousRequests()) {
			logError(new DebugException(new Status(IStatus.ERROR,"com.ibm.debug",IDebugStatusConstants.TARGET_REQUEST_FAILED, PICLUtils.getFormattedString(ENGINE_BUSY, "PICLThread#monitorStack"), null)));
			return;
		}
		try {
			if (!fDebuggeeThread.monitorStack(DebugEngine.sendReceiveSynchronously)) {
				DebugException de= new DebugException(new Status(IStatus.ERROR,"com.ibm.debug",IDebugStatusConstants.TARGET_REQUEST_FAILED, "PICLThread#monitorStack", null));
				logError(de);
			} else
				fMonitoringStack = true;
		} catch (IOException ioe) {
			logError(new DebugException(new Status(IStatus.ERROR,"com.ibm.debug",IDebugStatusConstants.TARGET_REQUEST_FAILED, PICLUtils.getResourceString(COMM_FAILURE), ioe)));
		}
	}

	/**
	 * The local expressions monitor for this thread is no longer needed.
	 */
	public void releaseLocalExpressionsMonitor() {
		if ((fLocalExpressionsMonitor != null) && (!fLocalExpressionsMonitor.thisObjectOrItsOwnerHasBeenDeleted())) {
			if (!getDebugEngine().isAcceptingSynchronousRequests()) {
				logError(new DebugException(new Status(IStatus.ERROR,"com.ibm.debug",IDebugStatusConstants.TARGET_REQUEST_FAILED, PICLUtils.getFormattedString(ENGINE_BUSY, "PICLThread#releaseLocalExpressionsMonitor"), null)));
				return;
			}
			try {
				fLocalExpressionsMonitor.remove();
			} catch (IOException ioe) {
				logError(new DebugException(new Status(IStatus.ERROR,"com.ibm.debug",IDebugStatusConstants.TARGET_REQUEST_FAILED, PICLUtils.getResourceString(COMM_FAILURE), ioe)));
			} catch (NullPointerException npe) {
				// catch npe out of picl client should be removed
				logError(new DebugException(new Status(IStatus.ERROR,"com.ibm.debug",IDebugStatusConstants.TARGET_REQUEST_FAILED, PICLUtils.getResourceString(CLIENT_FAILURE), npe)));
			}
			fLocalExpressionsMonitor= null;
		}
	}

	public void releaseStackMonitor() {
		if ((fStack != null) && (!fStack.thisObjectOrItsOwnerHasBeenDeleted())) {
			fStack.removeEventListener(this);
			if (!getDebugEngine().isAcceptingSynchronousRequests()) {
				logError(new DebugException(new Status(IStatus.ERROR,"com.ibm.debug",IDebugStatusConstants.TARGET_REQUEST_FAILED, PICLUtils.getFormattedString(ENGINE_BUSY, "PICLThread#releaseStackMonitor"), null)));
				return;
			}
			try {
				fStack.freeStack(DebugEngine.sendReceiveSynchronously);
			} catch (IOException ioe) {
				logError(new DebugException(new Status(IStatus.ERROR,"com.ibm.debug",IDebugStatusConstants.TARGET_REQUEST_FAILED, PICLUtils.getResourceString(COMM_FAILURE), ioe)));
			}
			fStack= null;
			fMonitoringStack = false;
		}
	}

    /**
     * @see ISuspendResume#resume()
     */
	public void resume() throws DebugException {
		((IDebugTarget) getParent()).resume();
	}

	/**
	 * Sets whether this thread is currently in the process of
	 * stepping and fires the appropriate debug event.
	 */
	public void setStepping(boolean isStepping) {
		fStepping= isStepping;
		if (isStepping) {
			fireResumeEvent(DebugEvent.STEP_START);
			PICLDebugTarget parent= (PICLDebugTarget)getParent();
			parent.clearStoppedByException();
			parent.clearStoppedByBreakpoint();
		} else {
			fireSuspendEvent(DebugEvent.STEP_END);
		}

	}



    /**
     * Sets that this thread is executing an internal step request.
     */
    protected void startInternalStepping() {
        fStepping = true;
		PICLDebugTarget parent= (PICLDebugTarget)getParent();
		parent.clearStoppedByException();
		parent.clearStoppedByBreakpoint();
	}

    /**
     * Sets that this thread is finished executing an internal step request
     * and fires an internal step event.
     */
    protected void endInternalStepping(int stepKind) {
        fStepping = false;
		PICLDebugTarget parent= (PICLDebugTarget)getParent();
        parent.fireInternalEvent(new InternalStepEvent(this, stepKind));
	}

    /**
     * @see DebuggeeThreadEventListener#stackAdded(StackAddedEvent)
     */
	public void stackAdded(StackAddedEvent event) {
    	PICLUtils.logEvent("stack added",this);

		fStack= event.getStack();
		if (fStack != null) {
			fStack.addEventListener(this);
			Vector sFrames= fStack.getStackFrames();
			if (sFrames != null) {
				Enumeration stackFrames= sFrames.elements();
				while (stackFrames.hasMoreElements()) {
					PICLStackFrame stackFrame= new PICLStackFrame(this, (StackFrame) stackFrames.nextElement());
					addChild(stackFrame);
				}
			}
		}
	}

    /**
     * @see StackEventListener#stackEnded(StackEndedEvent)
     */
	public void stackEnded(StackEndedEvent event) {
    	PICLUtils.logEvent("stack ended",this);
		//Since this occurs at our request we will ignore this message
	}

    /**
     * @see StackEventListener#stackFrameAdded(StackFrameAddedEvent)
     */
	public void stackFrameAdded(StackFrameAddedEvent event) {
    	PICLUtils.logEvent("stack frame added",this);

		PICLStackFrame stackFrameElement= new PICLStackFrame(this, event.getStackFrame());
		addChild(stackFrameElement);
	}

	/**
	 * Checks if run to location is ok
	 * @return ok to run to location
	 */
	public boolean canRunToLocation() {
		return !haveDoneCleanup() & isSuspended();
	}

	/**
	 * Checks if jump to location is ok
	 * @return ok to jump to location
	 */
	public boolean canJumpToLocation() {
		return !haveDoneCleanup() & isSuspended();
	}

	/**
	 * Causes the thread to run to a location.  Basically adds a breakpoint runs and then
	 * removes the breakpoint
	 * @param a marker that represents the location to run to.
	 */

	public void runToLocation(IMarker marker) {
		// reset changed flags
		((PICLDebugTarget)getDebugTarget()).resetChanged();

		// Using the marker get a location object to use for run to location

		ViewFile viewFile = ((PICLDebugTarget)getDebugTarget()).getViewFile(marker,getViewInformation());
		if (viewFile == null)
			return;


		Location loc = new Location(viewFile, getBreakpointManager().getLineNumber(marker));
		try {
			fDebuggeeThread.runToLocation(loc,DebugEngine.sendReceiveDefault);
		} catch(IOException ioe) {}
	}

	/**
	 * Causes the thread to jump to a location.  For engines that support this function
	 * it means that the program starts exection at the specified location
	 * @param a marker that represents the location to run to.
	 */

	public void jumpToLocation(IMarker marker) {

		// reset changed flags
		((PICLDebugTarget)getDebugTarget()).resetChanged();
	
		// Using the marker get a location object to use for run to location
		ViewFile viewFile = ((PICLDebugTarget)getDebugTarget()).getViewFile(marker,getViewInformation());
		if (viewFile == null)
			return;

		Location loc = new Location(viewFile, getBreakpointManager().getLineNumber(marker));
		try {
			fDebuggeeThread.jumpToLocation(loc,DebugEngine.sendReceiveDefault);
		} catch(IOException ioe) {}
	}

	/**
	 * @see IStep#stepInto()
	 */
	public void stepInto() throws DebugException {
		if (canStepInto()) {

			// first check to make sure that there isn't a pending engine request
			// by doing this now the state of the debugger won't change
			if (((PICLDebugTarget)getDebugTarget()).isEngineRequestPending())
				return;

			// reset changed flags
			((PICLDebugTarget)getDebugTarget()).resetChanged();

			StepIntoRequest stepRequest = new StepIntoRequest((PICLDebugTarget)getDebugTarget(),
															  this,
															  getViewInformation());
			try {
				setStepping(true);
				stepRequest.execute();
			} catch(PICLEngineBusyException pe) {
				setStepping(false);
				throw new DebugException(new Status(IStatus.ERROR, "com.ibm.debug.picl", IDebugStatusConstants.REQUEST_FAILED, PICLUtils.getResourceString(ERROR + "engine_busy"), null));
			} catch(PICLException pe) {
				setStepping(false);
				throw new DebugException(new Status(IStatus.ERROR, "com.ibm.debug.picl", IDebugStatusConstants.REQUEST_FAILED, PICLUtils.getResourceString(ERROR + "request_failed"), null));
			}
		} else
			throw new DebugException(new Status(IStatus.ERROR, "com.ibm.debug.picl", IDebugStatusConstants.NOT_SUPPORTED, PICLUtils.getResourceString(ERROR + "not_supported"), null));
		
	}

    /**
     * @see IInternalThread#internalStepInto
     */
    public void internalStepInto() throws DebugException {
        if (canStepInto()) {
            ((PICLDebugTarget)getDebugTarget()).resetChanged();

			StepIntoRequest stepRequest = new StepIntoRequest((PICLDebugTarget)getDebugTarget(),
															  this,
															  getViewInformation());
            stepRequest.setInternal(true);

			try {
				stepRequest.execute();
			} catch(PICLEngineBusyException pe) {
				new DebugException(new Status(IStatus.ERROR, "com.ibm.debug.picl", IDebugStatusConstants.REQUEST_FAILED, PICLUtils.getResourceString(ERROR + "engine_busy"), null));
			} catch(PICLException pe) {
				new DebugException(new Status(IStatus.ERROR, "com.ibm.debug.picl", IDebugStatusConstants.REQUEST_FAILED, PICLUtils.getResourceString(ERROR + "request_failed"), null));
			}
        }
        startInternalStepping();
    }


	/**
	 * @see IStep#stepOver()
	 */
	public void stepOver() throws DebugException {
		if (canStepOver()) {

			// first check to make sure that there isn't a pending engine request
			// by doing this now the state of the debugger won't change
			if (((PICLDebugTarget)getDebugTarget()).isEngineRequestPending())
				return;

			// reset changed flags
			((PICLDebugTarget)getDebugTarget()).resetChanged();

			StepOverRequest stepRequest = new StepOverRequest((PICLDebugTarget)getDebugTarget(),
															  this,
															  getViewInformation());
			try {
				setStepping(true);
				stepRequest.execute();
			} catch(PICLEngineBusyException pe) {
				setStepping(false);
				throw new DebugException(new Status(IStatus.ERROR, "com.ibm.debug.picl", IDebugStatusConstants.REQUEST_FAILED, PICLUtils.getResourceString(ERROR + "engine_busy"), null));
			} catch(PICLException pe) {
				setStepping(false);
				throw new DebugException(new Status(IStatus.ERROR, "com.ibm.debug.picl", IDebugStatusConstants.REQUEST_FAILED, PICLUtils.getResourceString(ERROR + "request_failed"), null));
			}

		} else
			throw new DebugException(new Status(IStatus.ERROR, "com.ibm.debug.picl", IDebugStatusConstants.NOT_SUPPORTED, PICLUtils.getResourceString(ERROR + "not_supported"), null));
		
	}

	/**
     * @see IInternalThread#internalStepOver
	 */
	public void internalStepOver() throws DebugException {
		if (canStepOver()) {

			// reset changed flags
			((PICLDebugTarget)getDebugTarget()).resetChanged();

			StepOverRequest stepRequest = new StepOverRequest((PICLDebugTarget)getDebugTarget(),
															  this,
															  getViewInformation());
            stepRequest.setInternal(true);

			try {
				stepRequest.execute();
			} catch(PICLEngineBusyException pe) {
				new DebugException(new Status(IStatus.ERROR, "com.ibm.debug.picl", IDebugStatusConstants.REQUEST_FAILED, PICLUtils.getResourceString(ERROR + "engine_busy"), null));
			} catch(PICLException pe) {
				new DebugException(new Status(IStatus.ERROR, "com.ibm.debug.picl", IDebugStatusConstants.REQUEST_FAILED, PICLUtils.getResourceString(ERROR + "request_failed"), null));
			}


			startInternalStepping();
		}
	}

	/**
	 * @see IStep#stepReturn()
	 */
	public void stepReturn() throws DebugException {
		if (canStepReturn()) {

			// first check to make sure that there isn't a pending engine request
			// by doing this now the state of the debugger won't change
			if (((PICLDebugTarget)getDebugTarget()).isEngineRequestPending())
				return;

			// reset changed flags
			((PICLDebugTarget)getDebugTarget()).resetChanged();

			StepReturnRequest stepRequest = new StepReturnRequest((PICLDebugTarget)getDebugTarget(),
															  this,
															  getViewInformation());
			try {
				setStepping(true);
				stepRequest.execute();
			} catch(PICLEngineBusyException pe) {
				setStepping(false);
				throw new DebugException(new Status(IStatus.ERROR, "com.ibm.debug.picl", IDebugStatusConstants.REQUEST_FAILED, PICLUtils.getResourceString(ERROR + "engine_busy"), null));
			} catch(PICLException pe) {
				setStepping(false);
				throw new DebugException(new Status(IStatus.ERROR, "com.ibm.debug.picl", IDebugStatusConstants.REQUEST_FAILED, PICLUtils.getResourceString(ERROR + "request_failed"), null));
			}
		} else
			throw new DebugException(new Status(IStatus.ERROR, "com.ibm.debug.picl", IDebugStatusConstants.NOT_SUPPORTED, PICLUtils.getResourceString(ERROR + "not_supported"), null));
			
	}

	/**
     * @see IInternalThread#internalStepReturn
	 */
	public void internalStepReturn() throws DebugException {
		if (canStepReturn()) {

			// reset changed flags
			((PICLDebugTarget)getDebugTarget()).resetChanged();

			StepReturnRequest stepRequest = new StepReturnRequest((PICLDebugTarget)getDebugTarget(),
															  this,
															  getViewInformation());
            stepRequest.setInternal(true);
			try {
				stepRequest.execute();
			} catch(PICLEngineBusyException pe) {
				new DebugException(new Status(IStatus.ERROR, "com.ibm.debug.picl", IDebugStatusConstants.REQUEST_FAILED, PICLUtils.getResourceString(ERROR + "engine_busy"), null));
			} catch(PICLException pe) {
				new DebugException(new Status(IStatus.ERROR, "com.ibm.debug.picl", IDebugStatusConstants.REQUEST_FAILED, PICLUtils.getResourceString(ERROR + "request_failed"), null));
			}
			startInternalStepping();
		}
	}

	/**
	 * @see IStep
	 */
	public void stepDebug() throws DebugException {

		if (canStepDebug()) {

			// first check to make sure that there isn't a pending engine request
			// by doing this now the state of the debugger won't change
			if (((PICLDebugTarget)getDebugTarget()).isEngineRequestPending())
				return;			
				
			// reset changed flags
			((PICLDebugTarget)getDebugTarget()).resetChanged();
			
			StepDebugRequest stepRequest = new StepDebugRequest((PICLDebugTarget)getDebugTarget(),
															  this,
															  getViewInformation());
			try {
				setStepping(true);   
				stepRequest.execute();
			} catch(PICLEngineBusyException pe) {
				setStepping(false);				
				throw new DebugException(new Status(IStatus.ERROR, "com.ibm.debug.picl", IDebugStatusConstants.REQUEST_FAILED, PICLUtils.getResourceString(ERROR + "engine_busy"), null));
			} catch(PICLException pe) {
				setStepping(false);				
				throw new DebugException(new Status(IStatus.ERROR, "com.ibm.debug.picl", IDebugStatusConstants.REQUEST_FAILED, PICLUtils.getResourceString(ERROR + "request_failed"), null));
			}
		} else
			throw new DebugException(new Status(IStatus.ERROR, "com.ibm.debug.picl", IDebugStatusConstants.NOT_SUPPORTED, PICLUtils.getResourceString(ERROR + "not_supported"), null));
			
	}

    /**
     * @see ISuspendResume#suspend()
     */
	public void suspend() throws DebugException {
		((IDebugTarget) getParent()).suspend();
	}

	/**
	 * @see ITerminate#terminate()
	 */
	public void terminate() throws DebugException {
		new DebugException(new Status(IStatus.ERROR, "com.ibm.debug.picl", IDebugStatusConstants.NOT_SUPPORTED, PICLUtils.getResourceString(NO_THREAD_TERM), null));
	}

    /**
     * @see DebuggeeThreadEventListener#threadChanged(ThreadChangedEvent)
     */
	public void threadChanged(ThreadChangedEvent event) {
    	PICLUtils.logEvent("thread changed",this);
		if (isStepping()) {
//			short state = Short.parseShort(event.getThread().getState().getValue());
			short state = event.getThread().state();
			switch (state) {
				case EPDC.StdThdSuspended :
                    Object property = event.getRequestProperty();
                    if(property instanceof StepRequest && ((StepRequest)property).isInternal()) {
                        int stepKind = -1;
                        if(property instanceof StepOverRequest)
                            stepKind = InternalStepEvent.STEP_OVER;
                        else if(property instanceof StepIntoRequest)
                            stepKind = InternalStepEvent.STEP_INTO;
                        else if(property instanceof StepReturnRequest)
                            stepKind = InternalStepEvent.STEP_RETURN;

                        endInternalStepping(stepKind);
                    } else {
  					    setStepping(false);
                    }
					break;
			}
		}
	}

    /**
     * @see DebuggeeThreadEventListener#threadEnded(ThreadEndedEvent)
     */
	public void threadEnded(ThreadEndedEvent event) {
    	PICLUtils.logEvent("thread end",this);

		// tell the parent to remove this thread
		((PICLThread)getParent()).removeChild(this);
	}

	/**
	 * @see PICLDebugElement
	 */
	 protected void doCleanupDetails() {
		// Unregister as a listener, and reset all flags
		fDebuggeeThread.removeEventListener(this);
		fStepping= false;

		// Stop listening for stack & local expression events
		if (!((PICLDebugTarget)getDebugTarget()).isTerminated()) {
			releaseStackMonitor();
			releaseLocalExpressionsMonitor();
		}

		// clean up parent objects
		if (getRegisterGroupParent() != null) {
			getRegisterGroupParent().doCleanup();
			fRegisterGroupParent = null;
		}

		// Finally, null out the encapsulated PICL thread object
		fDebuggeeThread= null;
	 }

	 public Location getLocation(ViewInformation view) {

	 	if (haveDoneCleanup())
	 		return null;

	 	try {
	 		return fDebuggeeThread.currentLocationWithinView(view);
	 	} catch(Exception e) {
	 		return null;
	 	}
	 }


	/**
	 * Request to monitor an expression.   Caller must supply a marker and a string that is the
	 * expression to be monitored.   An exception will be thrown if there is any failure to add
	 * the monitor.  Once the monitor has been accepted, the monitor itself will be alerted to
	 * changes in the expression that is being monitored.
	 * This thread will be used in the context
	 * @param marker that represents the location where the expression will be evaluated
	 * @param string that represents the expression to be evaluated
	 * @param represents the view type that the marker is associated with e.g. source view, listing view
	 * 		  null will default to the source view.
	 * @throws an exception is thrown if the request to monitor the expression fails
	 * @throws @see PICLEngineBusyException if another request is pending
	 */

	public PICLVariable monitorExpression(IMarker marker,
								  String expression,
								  ViewInformation viewInformation) throws PICLException {

		// default to the source view
		if (viewInformation == null)
			viewInformation = getDebugEngine().getSourceViewInformation();


		MonitorExpressionRequest monitorRequest = new MonitorExpressionRequest((PICLDebugTarget)getDebugTarget(),
																			   this,
																			   marker,
																			   viewInformation,
																			   expression);
		monitorRequest.execute();

		return monitorRequest.getMonitorResult();

	}


	/**
	 * Request to monitor storage.   Caller must supply a marker and a string that is the
	 * expression to be monitored.   An exception will be thrown if there is any failure to add
	 * the monitor.
	 * This thread will be used in the context
	 * @param marker that represents the location where the expression will be evaluated
	 * @param string that represents the expression to be evaluated to get an address
	 * @param represents the view type that the marker is associated with e.g. source view, listing view
	 * 		  null will default to the source view.
	 * @throws an exception is thrown if the request to monitor the expression fails
	 * @throws @see PICLEngineBusyException if another request is pending
	 */


	public void monitorStorage(IMarker marker,
								  String expression,
								  ViewInformation viewInformation) throws PICLException {

		// default to the source view
		if (viewInformation == null)
			viewInformation = getDebugEngine().getSourceViewInformation();


		MonitorStorageRequest monitorRequest = new MonitorStorageRequest((PICLDebugTarget)getDebugTarget(),
																			   this,
																			   marker,
																			   viewInformation,
																			   expression);
		monitorRequest.execute();

	}

	/**
	 * Gets the debuggeeThread
	 * @return Returns a DebuggeeThread
	 */
	public DebuggeeThread getDebuggeeThread() {
		return fDebuggeeThread;
	}
    /**
     * Sets the debuggeeThread
     * @param debuggeeThread The debuggeeThread to set
     */
    public void setDebuggeeThread(DebuggeeThread debuggeeThread) {
        fDebuggeeThread = debuggeeThread;
    }

    /**
     * @see IRegisterSupport#supportsRegisters()
     */
    public boolean supportsRegisters() {
		return getDebugEngine().getCapabilities().getWindowCapabilities().monitorRegistersSupported();
    }

    /**
     * @see IInternalThread#readIntVariable
     */
    public int readIntVariable(String variable) throws DebugException {
        MonitoredExpression monitoredExpression = getMonitoredExpression(variable, null);
        if(!(monitoredExpression.getValue() instanceof ScalarMonitoredExpressionTreeNode))
            throw new DebugException(new Status(IStatus.ERROR, "com.ibm.debug.picl", IDebugStatusConstants.REQUEST_FAILED, PICLUtils.getResourceString(ERROR + "request_failed"), null));

        ScalarMonitoredExpressionTreeNode exprNode = (ScalarMonitoredExpressionTreeNode)monitoredExpression.getValue();
        int value = 0;
        try {
            value = Integer.parseInt(exprNode.getValue());
        } catch(NumberFormatException e) {
            throw new DebugException(new Status(IStatus.ERROR, "com.ibm.debug.picl", IDebugStatusConstants.REQUEST_FAILED, PICLUtils.getResourceString(ERROR + "request_failed"), null));
        }

        return value;
    }

    /**
     * @see IInternalThread#writeVariable(String, int)
     */
    public void writeVariable(String variable, int value) throws DebugException {
        String strValue = Integer.toBinaryString(value);
        getMonitoredExpression(variable, strValue);
    }

    /**
     * @see IInternalThread#readStringVariable
     */
    public String readStringVariable(String variable) throws DebugException {
        MonitoredExpression monitoredExpression = getMonitoredExpression(variable, null);
        if(!(monitoredExpression.getValue() instanceof ScalarMonitoredExpressionTreeNode))
            throw new DebugException(new Status(IStatus.ERROR, "com.ibm.debug.picl", IDebugStatusConstants.REQUEST_FAILED, PICLUtils.getResourceString(ERROR + "request_failed"), null));

        ScalarMonitoredExpressionTreeNode exprNode = (ScalarMonitoredExpressionTreeNode)monitoredExpression.getValue();
        return exprNode.getValue();
    }

    /**
     * @see IInternalThread#writeVariable(String, String)
     */
    public void writeVariable(String variable, String value) throws DebugException {
        getMonitoredExpression(variable, value);
    }

    protected MonitoredExpression getMonitoredExpression(String variable, String value) throws DebugException {
        MonitoredExpression monitoredExpression = null;

        try {
            InternalEvaluateExpressionRequest request = new InternalEvaluateExpressionRequest((PICLDebugTarget)getDebugTarget(),
                                                                                              this,
                                                                                              variable,
                                                                                              value);
            request.execute();
            monitoredExpression = request.getMonitoredExpression();
        } catch(PICLEngineBusyException pe) {
            throw new DebugException(new Status(IStatus.ERROR, "com.ibm.debug.picl", IDebugStatusConstants.REQUEST_FAILED, PICLUtils.getResourceString(ERROR + "engine_busy"), null));
        } catch(PICLException pe) {
        }

        if(monitoredExpression == null)
            throw new DebugException(new Status(IStatus.ERROR, "com.ibm.debug.picl", IDebugStatusConstants.REQUEST_FAILED, PICLUtils.getResourceString(ERROR + "request_failed"), null));

        return monitoredExpression;
    }

	/**
	 * @see PICLDebugElement#hasChildren()
	 */
	public boolean hasChildren() {
		if (fMonitoringStack)
			return super.hasChildren();
		else
			return true;
	}

	/**
	 * Given a stackframe this will start monitoring locals
	 */
	public LocalMonitoredExpressions getLocalMonitoredExpressions(PICLStackFrame stackEntryToMonitor) {
		if (fLocalExpressionsMonitor == null) {
			monitorLocalExpressions(stackEntryToMonitor.getStackNumber());
		}

		return fLocalExpressionsMonitor;
	}

	/**
	 * Reset anything in the stackframes that uses changed flags
	 */

	public void resetChanged() {
		// loop through all of the stackframes and reset the changed flags

		if (!hasChildren())
			return;

		IDebugElement stackFrames[] = null;

		try {
			stackFrames = getChildrenNoExpand();
		} catch(DebugException de) {
			return;
		}
		for (int i = 0;i < stackFrames.length; i++)
			((PICLStackFrame)stackFrames[i]).resetChanged();

	}


}
