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

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IDebugConstants;
import org.eclipse.debug.core.IDebugStatusConstants;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.widgets.Shell;

import com.ibm.debug.PICLDebugPlugin;
import com.ibm.debug.WorkspaceSourceLocator;
import com.ibm.debug.connection.ConnectionInfo;
import com.ibm.debug.connection.SocketConnectionInfo;
import com.ibm.debug.epdc.EPDC;
import com.ibm.debug.internal.pdt.ui.dialogs.ExceptionDialog;
import com.ibm.debug.internal.pdt.ui.dialogs.ForkDialog;
import com.ibm.debug.internal.pdt.ui.dialogs.NewProcessDialog;
import com.ibm.debug.jni.control.ICompiledInternalDebugTarget;
import com.ibm.debug.jni.control.IInternalEventListener;
import com.ibm.debug.jni.control.InternalBreakpointEvent;
import com.ibm.debug.jni.control.InternalDebugEvent;
import com.ibm.debug.jni.control.InternalResumeEvent;
import com.ibm.debug.jni.control.InternalSuspendEvent;
import com.ibm.debug.launch.PICLAttachInfo;
import com.ibm.debug.launch.PICLLoadInfo;
import com.ibm.debug.launch.PICLStartupInfo;
import com.ibm.debug.model.AddressBreakpoint;
import com.ibm.debug.model.Breakpoint;
import com.ibm.debug.model.BreakpointAddedEvent;
import com.ibm.debug.model.BreakpointChangedEvent;
import com.ibm.debug.model.BreakpointDeletedEvent;
import com.ibm.debug.model.BreakpointEventListener;
import com.ibm.debug.model.CPlusPlusPICLEngineInfo;
import com.ibm.debug.model.DebugEngine;
import com.ibm.debug.model.DebugEngineCommandLogResponseEvent;
import com.ibm.debug.model.DebugEngineEventListener;
import com.ibm.debug.model.DebugEngineTerminatedEvent;
import com.ibm.debug.model.DebuggeeAttachOptions;
import com.ibm.debug.model.DebuggeeException;
import com.ibm.debug.model.DebuggeePrepareOptions;
import com.ibm.debug.model.DebuggeeProcess;
import com.ibm.debug.model.DebuggeeProcessEventListener;
import com.ibm.debug.model.DebuggeeThread;
import com.ibm.debug.model.EngineArgs;
import com.ibm.debug.model.EngineBreakpointCapabilities;
import com.ibm.debug.model.EngineCapabilities;
import com.ibm.debug.model.EngineCapabilitiesChangedEvent;
import com.ibm.debug.model.EngineFileCapabilities;
import com.ibm.debug.model.EngineInfo;
import com.ibm.debug.model.EntryBreakpoint;
import com.ibm.debug.model.ErrorOccurredEvent;
import com.ibm.debug.model.ExceptionRaisedEvent;
import com.ibm.debug.model.Host;
import com.ibm.debug.model.LineBreakpoint;
import com.ibm.debug.model.MessageReceivedEvent;
import com.ibm.debug.model.Model;
import com.ibm.debug.model.ModelStateChangedEvent;
import com.ibm.debug.model.Module;
import com.ibm.debug.model.ModuleAddedEvent;
import com.ibm.debug.model.ModuleLoadBreakpoint;
import com.ibm.debug.model.MonitoredExpressionAddedEvent;
import com.ibm.debug.model.Part;
import com.ibm.debug.model.ProcessAddedEvent;
import com.ibm.debug.model.ProcessEndedEvent;
import com.ibm.debug.model.ProcessListColumnDetails;
import com.ibm.debug.model.ProcessStopInfo;
import com.ibm.debug.model.ProcessStoppedEvent;
import com.ibm.debug.model.ProductInfo;
import com.ibm.debug.model.SaveRestoreFlags;
import com.ibm.debug.model.StorageAddedEvent;
import com.ibm.debug.model.StorageStyle;
import com.ibm.debug.model.ThreadAddedEvent;
import com.ibm.debug.model.View;
import com.ibm.debug.model.ViewFile;
import com.ibm.debug.model.ViewInformation;
import com.ibm.debug.model.Watchpoint;

import org.eclipse.jface.dialogs.ErrorDialog;

/**
 * Instances of this class represent processes in the debug model.
 */
public class PICLDebugTarget extends PICLDebugElement implements IRegisterSupport,
																 IDebugTarget,
																 DebuggeeProcessEventListener,
																 BreakpointEventListener,
																 DebugEngineEventListener,
																 ICompiledInternalDebugTarget,
																 IStorageSupport {

    // Resource String keys
    private static final String PREFIX= "picl_debug_target.";
    private static final String NAME= PREFIX + "name.";
    private static final String NAME0= NAME + "name0";
    private static final String NAME1= NAME + "name1";
    private static final String NAME2= NAME + "name2";
    private static final String ERROR= PREFIX + "error.";

    private static final String CLIENT_FAILURE= ERROR + "client_failure";
    private static final String SUSPEND_FAILED= ERROR + "suspend_failed";
    private static final String RESUME_FAILED= ERROR + "resume_failed";
    private static final String TERMINATE_FAILED= ERROR + "terminate_failed";
    private static final String DISCONNECT_FAILED= ERROR + "disconnect_failed";
    private static final String SET_BREAKPOINT_FAILED= ERROR + "set_breakpoint_failed";
    private static final String REMOVE_BREAKPOINT_FAILED= ERROR + "remove_breakpoint_failed";
    private static final String ENABLE_BREAKPOINT_FAILED= ERROR + "enable_breakpoint_failed";
    private static final String DISABLE_BREAKPOINT_FAILED= ERROR + "disable_breakpoint_failed";
    private static final String CREATE_BREAKPOINT_FAILED= ERROR + "create_breakpoint_failed";
    private static final String NO_CONNECT= ERROR + "no_connect";
    private static final String NO_DEFERRED= ERROR + "no_deferred";
    private static final String INTERNAL_ERROR= ERROR + "internal_error";
    private static final String NO_HCR= ERROR + "no_hcr";
    private static final String ENGINE_MESSAGE_TITLE= PREFIX + "label.engine_message_title";


    /**
     * Everything needed to start the engine
     */
    protected PICLStartupInfo fStartupInfo = null;

    /**
     * Host object
     */
    protected Host fHost;
    protected ConnectionInfo fConnectionInfo;
    /**
     * The debug engine this process is running in.
     */
    protected DebugEngine fDebugEngine = null;

    /**
     * The IBM debug model process this PICL element is a proxy for.
     */
    protected DebuggeeProcess fDebuggeeProcess = null;

    /**
     * Associated system process (possibly <code>null</code>
     */
    protected IProcess fSystemProcess = null;

    /**
     * String that represents the state
     */
    String fCurrentState = "<not set>";

    /**
     * A flag indicating if this process is waiting to attach
     * (<code>true</code>), or waiting to launch/prepare
     * a program (<code>false</code>). This flag is set in
     * the constructor to the appropriate value.
     */
    protected boolean fAttachOnConnect= true;

    /**
     * Is this process running
     */
    protected boolean fIsRunning= false;

    /**
     * <code>true</code> when this target is allowed to be terminated
     */
    protected boolean fIsTerminateAllowed= false;

    /**
     * <code>true</code> when this target has terminated
     */
    protected boolean fTerminated= false;

	/**
	 * <code>true</code> once a debug engine has connected
	 * and a process has been created.
	 */
	private boolean fInitialized = false;

    /**
     * <code>true</code> once a debug engine has connected
     * and a process has been created.
     */
    protected boolean fConnected= false;

    /**
     * The thread that caused the process to stop
     */
    protected PICLThread fStoppingThread = null;

    /**
     * A hashtable of breakpoints installed in this process.
     * Keys are markers, and values are the resulting breakpoints.
     */
    private Hashtable fBreakpoints= new Hashtable(10);

    /**
     * The exceptions currently enabled.
     */
    protected Vector fEnabledExceptions= new Vector();

    /**
     * A flag indicating if this process was stopped by encountering an exception
     */
    protected boolean fIsStoppedByException= false;

    /**
     * A flag indicating if this process stopped with no reason (Why_none).
     */
    protected boolean fIsStoppedByNone = false;

    /**
     * The current process request.
     */
    protected AbstractPICLRequest fProcessRequest = null;

    /**
     * The exception event that stopped this process. If null, then this process is not currently stopped
     * by an exception
     */
    protected ExceptionRaisedEvent exceptionEventThatStoppedProcess= null;

    /**
     * A flag indicating if this process was stopped by encountering a breakpoint
     */
    protected boolean fIsStoppedByBreakpoint= false;

    /**
     * Cached copy of the underlying DebuggeeProcess's process ID
     */
    protected int fID;

	/**
	 * Parent for module list
	 */
	private PICLModuleParent fModuleParent = new PICLModuleParent(this);

	/**
	 * Parent for monitor list
	 */
	private PICLMonitorParent fMonitorParent = new PICLMonitorParent(this);

	/**
	 * Parent for storage list
	 */
	private PICLStorageParent fStorageParent = new PICLStorageParent(this);


	/**
     * The source view (e.g. source, mixed) that the use wants to be the
     * default view for any new stackframe related source files.
     * If the user has not specified anything then the default is Source view.
     */
    protected ViewInformation fPreferredView = null;


	/**
	 * Pending engine request
	 * NOTE: access to fPendingEngineRequest must be through the accessor methods in order
	 * to synchronize access
	 */
	private PICLEngineRequest fPendingEngineRequest = null;
	private int fLastError = 0;
	private String fLastErrorText = null;

	/**
	 * This flag(s) represent operations that must be done when the model has completed all processing
	 */
	private boolean fThreadSuspend = false;

	/**
	* A flag indicating if this process was stopped by an exec
	*/
	protected boolean fIsStoppedByExec = false;

	/**
	 * A flag indicating if this process was stopped by a fork
	 */
	protected boolean fIsStoppedByFork = false;

	/**
	 * This field indicates the result of the Fork Occurred dialog
	 * Values: ForkDialog.FOLLOW_PARENT - user chose to follow parent
	 * 		   ForkDialog.FOLLOW_CHILD  - user chose to follow child
	 * 		   -1  - dialog has not been displayed
	 *  */
	private int fForkDialogAnswer = -1;

	/**
	 * This field indicates the result of the Exception Occurred dialog
	 * Values: ExceptionDialog.STEP_EXCEPTION - user chose to step into exception handler
	 * 		   ExceptionDialog.RUN_EXCEPTION  - user chose to run exception handler
	 * 		   ExceptionDialog.RETRY_EXCEPTION  - user chose to reset to just before exception happened
	 * 		   -1  - dialog has not been displayed
	 *  */
	private int fExceptionDialogAnswer = -1;

	/**
	 * This field indicates the result of the New Process (Exec) dialog
	 * Values: NewProcessDialog.DEBUG_INITIALIZATION - user chose to debug initialization
	 * 		   NewProcessDialog.RUN_TO_MAIN  - user chose not to debug initialization
	 * 		   -1  - dialog has not been displayed
	 *  */
	private int fNewProcessDialogAnswer = -1;


	private ProcessStopInfo fProcessStopInfo = null;

    /**
     * Event listeners for internal events.
     */
    private Vector fInternalEventListeners = new Vector();

    /**
     * Hash tables to keep track of internal breakpoints.
     */
    private Hashtable fInternalBreakpoints = new Hashtable(50);
    private Hashtable fInternalBreakpointIDs = new Hashtable(50);

    /**
     * Set to the internal breakpoint currently stopped at or null.
     */
    protected InternalBreakpoint fInternalBreakpointHit = null;


    /**
     * Creates a PICL engine process
     */
    public PICLDebugTarget() {
        super(null, IDebugElement.DEBUG_TARGET);
        fCurrentState = PICLUtils.getResourceString(PREFIX + "state.waiting");
    }

    /**
     * A request to suspend a process
     */
    class SuspendProcessRequest extends AbstractPICLRequest {
        public SuspendProcessRequest() {
            // wait for accepting asynchronous even though we send synchronously
            super(IPICLRequest.IMMEDIATE);
        }
        /**
         * @see IPICLRequest
         */
        public boolean performRequest() throws IOException {
        	if(fDebuggeeProcess == null)  //can happen on termination
        		return true;
            return fDebuggeeProcess.halt(DebugEngine.sendReceiveSynchronously, this);
        }
        /**
         * @see IPICLRequest
         */
        public String getErrorMessage() {
            return PICLUtils.getResourceString(SUSPEND_FAILED);
        }
    }

    /**
     * A request to terminate a process
     */
    class TerminateProcessRequest extends AbstractPICLRequest {
        public TerminateProcessRequest() {
            super(IPICLRequest.ASYNCHRONOUS);
        }
        /**
         * @see IPICLRequest
         */
        public String getErrorMessage() {
            return PICLUtils.getResourceString(TERMINATE_FAILED);
        }
        /**
         * @see IPICLRequest
         */
        public boolean performRequest() throws IOException {
        	if(fDebuggeeProcess == null)  //can happen on termination
        		return true;
            return fDebuggeeProcess.terminate(DebugEngine.sendReceiveDefault);
        }
    }

    /**
     * A request to terminate the engine
     */
    class TerminateEngineRequest extends AbstractPICLRequest {
        public TerminateEngineRequest() {
            super(IPICLRequest.ASYNCHRONOUS);
        }
        /**
         * @see IPICLRequest
         */
        public String getErrorMessage() {
            return PICLUtils.getResourceString(TERMINATE_FAILED);
        }
        /**
         * @see IPICLRequest
         */
        public boolean performRequest() throws IOException {
        	if(fDebugEngine == null)
        		return true;
            return fDebugEngine.terminate(DebugEngine.sendReceiveDefault);
        }
    }

    /**
     * A request to disconnect from a process
     */
    class DisconnectProcessRequest extends AbstractPICLRequest {
        public DisconnectProcessRequest() {
            super(IPICLRequest.ASYNCHRONOUS);
        }
        /**
         * @see IPICLRequest
         */
        public boolean performRequest() throws IOException {
        	if(fDebuggeeProcess == null)
        		return true;
            return fDebuggeeProcess.detach(EPDC.ProcessRelease, DebugEngine.sendReceiveDefault);
        }
        /**
         * @see IPICLRequest
         */
        public String getErrorMessage() {
            return PICLUtils.getResourceString(DISCONNECT_FAILED);
        }
    }

    /**
     * @see PICLDebugTarget#breakpointAdded
     */
    public void breakpointAdded(BreakpointAddedEvent event) {
		PICLUtils.logEvent("breakpoint added",this);

        Breakpoint breakpoint= event.getBreakpoint();

        // Get the property from the event so it can be paired with the breakpoint.
        Object property = event.getRequestProperty();

        if(property instanceof IMarker) {
            IMarker marker = (IMarker)property;

            fBreakpoints.put(marker, breakpoint);
            breakpointInstalled(marker);
            breakpoint.addEventListener(this);


		    // update the marker with information from the breakpoint that was set.
		    // this will ensure that the marker has all of the correct information
		    // It also means that the marker can be saved and set again when the
		    // breakpoint is restored.

//		    if (breakpoint instanceof LineBreakpoint)
//			    LineBreakpointRequest.updateAttributes(marker, breakpoint, this);
//		    else
//  			    if (breakpoint instanceof EntryBreakpoint)
//  				    EntryBreakpointRequest.updateAttributes(marker, breakpoint, this);
//  			    else
//  				    if (breakpoint instanceof AddressBreakpoint)
//  		 			    AddressBreakpointRequest.updateAttributes(marker, breakpoint, this);
//  		   		    else
//  		   			    if (breakpoint instanceof Watchpoint)
//  		 				    WatchpointRequest.updateAttributes(marker, breakpoint, this);
//  		 	  		    else
//  		 	  			    if (breakpoint instanceof ModuleLoadBreakpoint)
//  		 					    LoadBreakpointRequest.updateAttributes(marker, breakpoint, this);

        } else if(property instanceof InternalBreakpoint) {
            InternalBreakpoint internalBreakpoint = (InternalBreakpoint)property;

            fInternalBreakpoints.put(internalBreakpoint, breakpoint);
            fInternalBreakpointIDs.put(breakpoint, internalBreakpoint);
            breakpoint.addEventListener(this);
        } else {
            PICLUtils.logText("Error - unrecognized property attached to breakpoint event.");
        }
    }


	/**
	 * increments the breakpoint's install count
	 * @param the breakpoint marker
	 * @return current install count or -1 if it failed
	 */
	private int breakpointInstalled(IMarker marker) {
		int installCount = marker.getAttribute(IPICLDebugConstants.INSTALL_COUNT,0);
		installCount++;
		try {
            String[] attributes = {IPICLDebugConstants.UPDATE_BREAKPOINT,
            					   IPICLDebugConstants.INSTALL_COUNT};
         	Object[] values = {new Boolean(false), new Integer(installCount)};
            marker.setAttributes(attributes,values);
		} catch(CoreException ce) {
			return -1;
		}
		return installCount;
	}

	/**
	 * decrements the breakpoint's install count
	 * @param the breakpoint marker
	 * @return current install count or -1 if it failed
	 */
	private int breakpointUnInstalled(IMarker marker) {

		int installCount = -1;

		installCount = marker.getAttribute(IPICLDebugConstants.INSTALL_COUNT,0);

		if (installCount == 0) {  // breakpoint was not installed
			PICLUtils.logText("Install count is wrong.  Expected > 0 found " + installCount);
			return -1;
		}
		installCount--;
		try {
            String[] attributes = {IPICLDebugConstants.UPDATE_BREAKPOINT,
            					   IPICLDebugConstants.INSTALL_COUNT};
         	Object[] values = {new Boolean(false), new Integer(installCount)};
            marker.setAttributes(attributes,values);
		} catch(CoreException ce) {
			return -1;
		}
		return installCount;
	}


    /**
     * @see BreakpointEventListener
     */
    public void breakpointChanged(BreakpointChangedEvent event) {
    	PICLUtils.logEvent("breakpoint changed",this);

        Breakpoint breakpoint= event.getBreakpoint();

        // Get the marker from the event so it can be paired with the breakpoint.
        IMarker marker = null;
        Object property = event.getRequestProperty();
        if(property != null && property instanceof IMarker)
            marker = (IMarker)property;
        else {
            PICLUtils.logText("Error - no marker attached to breakpoint event.");
            return;
        }

                // update the marker with information from the breakpoint that was set.
                // this will ensure that the marker has all of the correct information
                // It also means that the marker can be saved and set again when the
                // breakpoint is restored.

//		if (breakpoint instanceof LineBreakpoint)
//			LineBreakpointRequest.updateAttributes(marker, breakpoint, this);
//		else
//  			if (breakpoint instanceof EntryBreakpoint)
//  				EntryBreakpointRequest.updateAttributes(marker, breakpoint, this);
//  			else
//  				if (breakpoint instanceof AddressBreakpoint)
//  		 			AddressBreakpointRequest.updateAttributes(marker, breakpoint, this);
//  		   		else
//  		   			if (breakpoint instanceof Watchpoint)
//  		 				WatchpointRequest.updateAttributes(marker, breakpoint, this);
//  		 	  		else
//  		 	  			if (breakpoint instanceof ModuleLoadBreakpoint)
//  		 					LoadBreakpointRequest.updateAttributes(marker, breakpoint, this);


    }

    /**
     * @see BreakpointEventListener
     */
    public void breakpointDeleted(BreakpointDeletedEvent event) {
    	PICLUtils.logEvent("breakpoint deleted",this);
        Breakpoint breakpoint= event.getBreakpoint();
        breakpoint.removeEventListener(this);

        // Get the property from the event so it can be paired with the breakpoint.
        Object property = event.getRequestProperty();

        if(property instanceof IMarker) {
            IMarker marker = (IMarker)property;

   	   		fBreakpoints.remove(marker);
 	   		breakpointUnInstalled(marker);
        } else if(property instanceof InternalBreakpoint) {
            InternalBreakpoint internalBreakpoint = (InternalBreakpoint)property;

            fInternalBreakpoints.remove(internalBreakpoint);
            fInternalBreakpointIDs.remove(breakpoint);
        } else {
            PICLUtils.logText("Error - no property attached to breakpoint event.");
        }
    }

    /**
     * @see IProcess
     */
    public boolean canResume() {
        return isSuspended();
    }

    /**
     * @see IProcess
     */
    public boolean canSuspend() {
        return !isSuspended() && !isTerminated();
    }

    /**
     * @see IProcess
     */
    public boolean canTerminate() {
        return getProcess().canTerminate() && !isTerminated();
    }

    /**
     * @see IDisconnect
     */
    public boolean canDisconnect() {
        if (!haveDoneCleanup() && (fDebugEngine != null)) {
            EngineCapabilities engineCapabilities= fDebugEngine.getCapabilities();
            if (engineCapabilities != null) {
                EngineFileCapabilities cap= engineCapabilities.getFileCapabilities();
                if (cap != null) {
                    return cap.processDetachSupported() && cap.processDetachReleaseSupported() && fAttachOnConnect && !isTerminated();
                }
            }
        }

        return false;
    }

    /**
     * @see DebugEngineEventListener
     */
    public void debugEngineTerminated(DebugEngineTerminatedEvent event) {
    	PICLUtils.logEvent("debug engine terminated",this);
        updateCurrentState("state.engine_terminated");
        fInitialized = false;
    }

    /**
     * @see IDisconnect
     */
    public void disconnect() throws DebugException {
        if (haveDoneCleanup() || isTerminated()) {
            return;
        }
        // must suspend before disconnecting
        suspend();
        performRequest(new DisconnectProcessRequest());
        setIsRunning(false);
    }

    /**
     * @see DebugEngineEventListener
     */
    public void engineCapabilitiesChanged(EngineCapabilitiesChangedEvent event) {
    	PICLUtils.logEvent("engine capabilities changed (not used)",this);
    }

    public void displayErrorMessage()
    {
      final Shell shell = PICLDebugPlugin.getInstance().getShell();
      shell.getDisplay().asyncExec(
         new Runnable()
         {
            public void run()
            {
               IStatus status = new Status(IStatus.ERROR,"com.ibm.debug",
                                           IDebugStatusConstants.TARGET_REQUEST_FAILED,
                                           //"Status: cannot start remote client",
                                           PICLUtils.getResourceString(CLIENT_FAILURE),
                                           null);
             ErrorDialog.openError(shell, "Error starting debug engine. ", null , status);
            }
         }
       );
    }

    /**
     * Establishes a connection with the engine received by the <code>EngineDaemon</code>.
     * <p>
     * Notice that the <code>PICLDebugModel</code> must be registered with the engine <b>after</b>
     * initialization, but before the preparing of the program. Events are not received unless this
     * ordering is maintained.
     *
     */
    public void engineIsReadyToConnect(PICLStartupInfo startupInfo, ConnectionInfo connectionInfo) {
        PICLUtils.logText("PICLDebugTarget.engineIsReadyToConnect()");
        boolean success = false;

        fStartupInfo = startupInfo;

        // Get a host and an engine object
        Host host= Model.getHost(connectionInfo);
        fDebugEngine= host.getNewDebugEngine(true);

        // the following connects to the engine using the ConnectionInfo object passed in the event.
        try {
            fDebugEngine.connect(connectionInfo, true);

            String searchPath = null;
            if(startupInfo != null) {
                WorkspaceSourceLocator sourceLocator = startupInfo.getWorkspaceSourceLocator();
                if(sourceLocator != null)
                    searchPath = sourceLocator.getSearchPath();
            }
            fDebugEngine.initialize(EPDC.LANG_CPP, null, null, searchPath, DebugEngine.sendReceiveSynchronously);

            fDebugEngine.addEventListener(this);
            fInitialized = true;
        } catch (IOException e) {
            if(startupInfo != null)
            {
               // Assert.isTrue(false, PICLUtils.getFormattedString(NO_CONNECT, connectionInfo.getHost()));
              fTerminated= true;
              fInitialized = false;
              fireTerminateEvent();
              try {
                  terminateEngine();
              } catch (DebugException de) {
                  logError(de);
              }

              displayErrorMessage();
              return;
            }
        }


        if(startupInfo == null)
            try {
                terminateEngine();
            } catch (DebugException de) {
                logError(de);
            }
        else if(startupInfo instanceof PICLLoadInfo)
            success = load((PICLLoadInfo)startupInfo);
        else if(startupInfo instanceof PICLAttachInfo)
            success = attach((PICLAttachInfo)startupInfo);

        if (!success)
              displayErrorMessage();
    }


    /**
     * Do a program load
     * @return result of load attempt
     */
    public boolean load(PICLLoadInfo loadInfo) {

        byte language = EPDC.LANG_CPP;
        if (!launchEngine(language))
            return false;

        int saveRestoreFlags = 0;
/*
        if(loadInfo.getUseProfile()) {
*/
            saveRestoreFlags = SaveRestoreFlags.AUTOSAVE |
                                   SaveRestoreFlags.DEFAULT_DATA_REPRESENTATIONS |
                                   SaveRestoreFlags.EXCEPTION_FILTERS |
                                   SaveRestoreFlags.BREAKPOINTS |
                                   SaveRestoreFlags.PROGRAM_MONITORS |
                                   SaveRestoreFlags.STORAGE;
/*
        }
*/
	boolean debugInitialization = loadInfo.getStartupBehaviour() ==
	                              loadInfo.DEBUG_INITIALIZATION;

        DebuggeePrepareOptions prepareOptions = new DebuggeePrepareOptions(

                    loadInfo.getProgramName(),      // program name
                    loadInfo.getProgramParms(),     // program arguments
                    saveRestoreFlags,               // what to restore - 0 means nothing
                    null,                           // save restore directory - null = default
                    !debugInitialization,            // run to main
                    false,                          // restore saved objects
                    false,                          // execute after prepare
                    null,                           // AS/400 job name
                    language);

        boolean result = false;
        try {
            result = fDebugEngine.prepareProgram(prepareOptions, DebugEngine.sendReceiveSynchronously);
            PICLUtils.logText("Engine prepared");
        } catch (IOException ioe) {
            fTerminated= true;
            fireTerminateEvent();
            System.out.println("PICLDebugTarget:load() failed - terminate");
            result = false;
        }
        return result;
    }

    /**
     * Do a process attach
     * @return result of attach attempt
     */
    public boolean attach(PICLAttachInfo attachInfo) {

        byte language = EPDC.LANG_CPP;
        if (!launchEngine(language))
            return false;

        int saveRestoreFlags = 0;
/*
        if(attachInfo.getUseProfile()) {
*/
            saveRestoreFlags = SaveRestoreFlags.AUTOSAVE |
                                   SaveRestoreFlags.DEFAULT_DATA_REPRESENTATIONS |
                                   SaveRestoreFlags.EXCEPTION_FILTERS |
                                   SaveRestoreFlags.BREAKPOINTS |
                                   SaveRestoreFlags.PROGRAM_MONITORS |
                                   SaveRestoreFlags.STORAGE;
/*
        }
*/

        DebuggeeAttachOptions attachOptions = null;
        if(attachInfo.getProcess() == null) {
            int processID = -1;
            try {
                processID = Integer.parseInt(attachInfo.getProcessID());
            } catch(NumberFormatException e) {
                fTerminated= true;
                fireTerminateEvent();
                return false;
            }
            attachOptions = new DebuggeeAttachOptions(
                attachInfo.getProcessPath(),
                processID,
                saveRestoreFlags,               // what to restore - 0 means nothing
                null,                           // save restore directory - null = default
                false,                          // restore saved objects
                false,                          // execute after attach
                language);
        } else {
            attachOptions = new DebuggeeAttachOptions(
                attachInfo.getProcessPath(),
                attachInfo.getProcess(),
                saveRestoreFlags,               // what to restore - 0 means nothing
                null,                           // save restore directory - null = default
                false,                          // restore saved objects
                language);
        }

        boolean result = false;
        try {
            result = fDebugEngine.attach(attachOptions, DebugEngine.sendReceiveSynchronously);
            PICLUtils.logText("Process attached");
        } catch (IOException ioe) {
            fTerminated= true;
            fireTerminateEvent();
            result = false;
        }
        return result;
    }

    /**
     * Get list of processes
     * @return process list (Vector of SystemProcess objects)
     */
    public Vector getProcessList() {

        if (!launchEngine(EPDC.LANG_CPP))
            return null;

        Vector processes = null;
        try {
            processes = fDebugEngine.getSystemProcessList();
        } catch (IOException ioe) {
            fTerminated= true;
            fireTerminateEvent();
        }

        return processes;
    }

    /**
     * Get process list column details
     * @return process list column details
     */
    public ProcessListColumnDetails[] getProcessListColumnDetails() {

        if (!launchEngine(EPDC.LANG_CPP))
            return null;

        ProcessListColumnDetails[] processDetails = null;
        try {
            processDetails = fDebugEngine.getProcessListColumnDetails();
        } catch (IOException ioe) {
            fTerminated= true;
            fireTerminateEvent();
        }

        return processDetails;
    }

    /**
     * Launch a debug engine and initialize it.
     * @return result of engine launch attempt
     */
    protected boolean launchEngine(byte language) {
        // Do not launch if already launched.
        if(fDebugEngine != null)
            return true;

        PICLUtils.logText("In PICLDebugTarget.launchEngine()");

        // Get a host and an engine object
        fHost = Model.getLocalHost(false);

        // default for now to the C++ engine
        EngineInfo engineInfo = new CPlusPlusPICLEngineInfo();
        ProductInfo productInfo = new ProductInfo(null);
        EngineArgs engineArgs = new EngineArgs(false,null,null,null,true);
        fConnectionInfo = new SocketConnectionInfo(null,null);

        // this starts a local copy of the engine (irmtdbgc)
        fHost.loadEngine(engineInfo,productInfo,fConnectionInfo,engineArgs);
        fDebugEngine = fHost.getNewDebugEngine(true);

        // now connect to it
        try {
            updateCurrentState("state.connecting");
            fDebugEngine.connect(fConnectionInfo);
            PICLUtils.logText("engine connected");
            updateCurrentState("state.connected");
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }

        // now initialize the engine
        try {
            fDebugEngine.initialize(language, null, null, null,
                                    DebugEngine.sendReceiveSynchronously);
            PICLUtils.logText("engine initialized");
            fDebugEngine.addEventListener(this);
            fInitialized = true;
        } catch (IOException e) {
            // insert error code here
            PICLUtils.logText("error occurred starting engine");
            return false;
        }

        return true;
    }

    /**
     * @see DebugEngineEventListener
     */
    public void errorOccurred(ErrorOccurredEvent event) {
    	PICLUtils.logEvent(">>>> Error event from model!!!!! <<<<",this);
    	PICLUtils.logEvent(">>>> Error code=" + event.getReturnCode() + " text=" + event.getMessage(),this);

    	// copy the error so that it can be displayed for debug purposes

    	fLastError = event.getReturnCode();
    	fLastErrorText = event.getMessage();
    	updateCurrentState("state.error");

    	// pass this error to the last request
    	if (getPendingEngineRequest() != null)
    		getPendingEngineRequest().setError(event);

    }

    /**
     * @see com.ibm.debug.model.DebuggeeProcessEventListener
     */
    public void exceptionRaised(ExceptionRaisedEvent event) {
    	PICLUtils.logEvent("exception raised",this);
//        String name= event.exceptionMsg();
//        Enumeration exs= fEnabledExceptions.elements();
//        boolean resume= true;
//      while (exs.hasMoreElements()) {
//          IException ex= (IException) exs.nextElement();
//          if (ex.getName().equals(name)) {
//              if (ex.getState() != IException.EXCEPTION_DISABLED) {
//                  resume= false;
                  fIsStoppedByException= true;
                  exceptionEventThatStoppedProcess= event;
//              }
//          }
//      }
//      if (resume) {
//          // resume() can't be called from this thread because
//          // we are in a callback from the Debug Engine.
//          new Thread(new Runnable() {
//              public void run() {
//                  try {
//                      resume();
//                  } catch (DebugException de) {
//                      logError(de);
//                  }
//              }
//          }).start();
//      }
    }

    /**
     * Clears the flag indicating whether this process is currently stopped by an exception
     */
    public void clearStoppedByException() {
        fIsStoppedByException= false;
    }

    /**
     * Returns whether this process is currently stopped by an exception
     */
    public boolean isStoppedByException() {
        return fIsStoppedByException;
    }

    /**
     * Clears the flag indicating whether this process is currently stopped by a breakpoint
     */
    public void clearStoppedByBreakpoint() {
        fIsStoppedByBreakpoint= false;
    }

    /**
     * Returns whether this processed is currently stopped by a breakpoint
     */
    public boolean isStoppedByBreakpoint() {
        return fIsStoppedByBreakpoint;
    }

    /**
     * Returns the ExceptionRaisedEvent that stopped this process
     */
    public ExceptionRaisedEvent getExceptionEventThatStoppedProcess() {
        return exceptionEventThatStoppedProcess;
    }

    /**
     * Returns the debug engine that this element is contained in.
     */
    public DebugEngine getDebugEngine() {
        return fDebugEngine;
    }

    /**
     * Returns the debug engine that this element is contained in.
     */
    public DebugEngine getGdbDebugEngine() {
        return fDebugEngine;
    }




    /**
     * @see IProcess
     */
    public int getID() {
        return fID;
    }

    /**
     * @see IDebugElement
     */
    public String getName() {
        PICLUtils.logText("PICLDebugTarget.getName()");

        return getName0(false);
    }

    private String getName0(boolean qualified) {
        String nameString= null;
        if (fConnected) {
            String idString= String.valueOf(getID());
            String processName= getDebuggeeProcess().qualifiedName();
            if (!qualified) {
                int index = processName.lastIndexOf('.');
                if (index != -1) {
                    processName= processName.substring(index +1);
                }
            }
            nameString= PICLUtils.getFormattedString(NAME0, new String[] {idString, processName});
        } else {
            if (isTerminated()) {
                nameString= PICLUtils.getResourceString(NAME1);
            } else {
                nameString= PICLUtils.getResourceString(NAME2);
            }
        }
        return nameString;
    }

    /**
     * Produces that label that will show up on the debug plugin
     * @return String that represents state of plugin
     */

    public String getLabel(boolean qualified) {
        String label = null;
        String status = "";
        String platform = "";
        String PID = "";
        String connection = "";
        String program = "";

        status = fCurrentState;

        if (fHost !=null) {
            switch (fHost.getPlatformID()) {
            	case 	Host.OS2 :
            		platform = PICLUtils.getResourceString(PREFIX + "platform.OS2");
            		break;
                case    Host.WindowsNT :
                    platform = PICLUtils.getResourceString(PREFIX + "platform.NT");
                    break;
                case    Host.Windows95 :
                    platform = PICLUtils.getResourceString(PREFIX + "platform.95");
                    break;
                case    Host.AIX :
                    platform = PICLUtils.getResourceString(PREFIX + "platform.AIX");
                    break;
                case    Host.Linux :
                    platform = PICLUtils.getResourceString(PREFIX + "platform.Linux");
                    break;
                case    Host.OS400 :
                    platform = PICLUtils.getResourceString(PREFIX + "platform.OS400");
                    break;
                case    Host.OS390 :
                    platform = PICLUtils.getResourceString(PREFIX + "platform.OS390");
                    break;
                case    Host.JVM :
                    platform = PICLUtils.getResourceString(PREFIX + "platform.JVM");
                    break;
                case    Host.HPUX :
                    platform = PICLUtils.getResourceString(PREFIX + "platform.HPUX");
                    break;
                case    Host.SUNOS :
                    platform = PICLUtils.getResourceString(PREFIX + "platform.SUNOS");
                    break;
                default :
                    platform = PICLUtils.getResourceString(PREFIX + "platform.unknown");
            }


        }

        if (fConnectionInfo != null) {
            if (fConnectionInfo.getHost() == null)
                connection = "localhost";
            else
                connection = fConnectionInfo.getHost() + ":" + fConnectionInfo.getConduit();
        }

        if (fDebuggeeProcess != null) {
            PID = String.valueOf(fDebuggeeProcess.processID());
            program = fDebuggeeProcess.qualifiedName();
        }

        if (qualified) {
            String[] substitutionText = {PID,status,platform,connection,program};
            label = PICLUtils.getFormattedString(PREFIX + "label.full",substitutionText);
            if (fLastError > 0)
	            label = label + " [Last error=(" + fLastError + ") text=" + fLastErrorText;
        } else {
            String[] substitutionText = {PID,status,program};
            label = PICLUtils.getFormattedString(PREFIX + "label.simple",substitutionText);
        }

        return label;
    }

    /**
     * @see IDebugTarget
     */
    public IProcess getProcess() {
    	if (fSystemProcess == null)
    		fSystemProcess = new PICLProcess(fDebuggeeProcess, this);
        return fSystemProcess;
    }

    /**
     * @see IDebugElement
     */
    public IStackFrame getStackFrame() {
        return null;
    }
    /**
     * @see IDebugElement
     */
    public IThread getThread() {
        return null;
    }

    /**
     * Returns the thread that stopped this debuggee
     * @return Thread that stopped
     */
    public PICLThread getStoppingThread() {
        return fStoppingThread;
    }

    /**
     * Returns the PICLThread that corresponds to the DebuggeeThread
     * @return PICLThread that corresponds to the DebuggeeThread
     */
    public PICLThread getPICLThread(DebuggeeThread dThread) {
        Iterator iter = fChildren.iterator();
        Object pThread = null;
        while(iter.hasNext()) {
                pThread = iter.next();
                if ((pThread instanceof PICLThread) && (((PICLThread)pThread).getDebuggeeThread() == dThread)) {
                     return (PICLThread)pThread;
                }
        }
        return null;
    }

    /**
     * @see IBreakpointSupport
     */
    public boolean isBreakpointEnabled(IMarker marker) {
    	return getBreakpointManager().isEnabled(marker);

    }

    /**
     * @see IBreakpointSupport
     */
    public boolean isBreakpointInstalled(IMarker marker) {
        return fBreakpoints.get(marker) != null;
    }

	/**
	 * static method that returns if breakpoint is installed and active
	 * @param IMarker that represents the breakpoint
	 * @return true if breakpoint installed
	 */
	public static boolean isBreakpointActive(IMarker marker) {
		if (marker.getAttribute(IPICLDebugConstants.INSTALL_COUNT,0) > 0)
			return true;
		else
			return false;
	}

	/**
	 * Returns true if the engine will accept a request.  False indicates that the engine
	 * is busy with a pending request
	 * @return True if engine is not busy and can accept a request
	 */
	public boolean isNotBusy() {
		return (getPendingEngineRequest() == null);
	}


    /**
     * @see IRuntimeState
     */
    public boolean isStepping() {
        return false;
    }

    /**
     * Returns <code>true</code> if this process stops in main automatically,
     * otherwise <code>false</code>.
     */
    public boolean isStopInMain() {
        return true;
    }

    /**
     * @see IProcess
     */
    public boolean isSuspended() {
        return !fIsRunning && !isTerminated();
    }

    /**
     * @see IProcess
     */
    public boolean isTerminated() {
        return fTerminated;
    }

    /**
     * @see IDisconnect
     */
    public boolean isDisconnected() {
        return isTerminated();
    }

	/**
	 * Returns status of engine
	 */
	public boolean isInitialized() {
		return fInitialized;
	}

    /**
     * @see DebugEngineEventListener
     */
    public void messageReceived(MessageReceivedEvent event) {
    	PICLUtils.logEvent("message received ",this);
    	if (isTerminated())
    		return;
		if (fIsStoppedByBreakpoint) {
			//show message to user
			final String msg = event.getMessage();
			if (msg == null)
				return;

			final Shell shell = PICLDebugPlugin.getInstance().getShell();
			shell.getDisplay().asyncExec(new Runnable() {
				public void run() {
					MessageDialog.openInformation(shell,
							PICLUtils.getResourceString(ENGINE_MESSAGE_TITLE),
	   						msg);
				}
			});
		}
    }


    /**
     * @see DebugEngineEventListener
     */
    public void modelStateChanged(ModelStateChangedEvent event) {
    	byte modelStateFlags = 0;
		if (System.getProperty("EVENTS") !=null) {
	    	StringBuffer status = new StringBuffer("<< model state changed >> (");
	    	modelStateFlags = event.getNewStateFlags();

	    	if ((modelStateFlags & DebugEngine.debugEngineIsBusyFlag) >0)
	    		status.append("[BUSY] ");
	    	if ((modelStateFlags & DebugEngine.modelIsBeingUpdatedFlag) >0)
	    		status.append("[UPDATE-IN-PROGRESS] ");
	    	if ((modelStateFlags & DebugEngine.queuedEventsAreBeingFiredFlag) >0)
	    		status.append("[EVENTS-BEING-FIRED] ");
	    	if ((modelStateFlags & DebugEngine.savedObjectsAreBeingRestoredFlag) >0)
	    		status.append("[SAVED-OBJECTS-RESTORING] ");

			if (modelStateFlags == 0)
				status.append("[READY] ");

	    	PICLUtils.logEvent(status.toString(),this);
		}

		// some processing must be done once the model has completed all of its tasks

		if (modelStateFlags == 0) {  // model is now in ready state
			if (fThreadSuspend) {   // must send thread suspend event
				fThreadSuspend = false;
		        int detail = -1;
                if (fInternalBreakpointHit != null) {
                    InternalDebugEvent internalEvent = new InternalBreakpointEvent(fStoppingThread, fInternalBreakpointHit);
                    fInternalBreakpointHit = null;
                    fireInternalEvent(internalEvent);
                } else {
    		    	if (fIsStoppedByBreakpoint)
    					detail= DebugEvent.BREAKPOINT;
   		      		fStoppingThread.setIsRunning(false,detail);
                }
			}
			// If fork has occurred then need to ask user what to do about it
			if (fIsStoppedByFork) {
				fIsStoppedByFork = false;
				final Shell shell = PICLDebugPlugin.getInstance().getShell();
				final PICLDebugTarget thisTarget = this;
				shell.getDisplay().syncExec(new Runnable() {
					public void run() {
						ForkDialog dialog = new ForkDialog(shell, thisTarget);
						dialog.open();
					}
				});

				int result = getForkDialogAnswer();
				DebuggeeThread thread=fStoppingThread.getDebuggeeThread();
				try {
					if (result == ForkDialog.FOLLOW_PARENT) {
						thread.executeForkAndFollowParent(getDebugEngine().sendReceiveDefault);
					} else if (result == ForkDialog.FOLLOW_CHILD) {
						thread.executeForkAndFollowChild(getDebugEngine().sendReceiveDefault);
					}
				} catch (IOException e) {
				}
			}
			// If exec has occurred then need to tell user about it
			if (fIsStoppedByExec) {
				fIsStoppedByExec = false;
				final Shell shell = PICLDebugPlugin.getInstance().getShell();
				final PICLDebugTarget thisTarget = this;
				shell.getDisplay().syncExec(new Runnable() {
					public void run() {
						NewProcessDialog dialog = new NewProcessDialog(shell, thisTarget);
						dialog.open();
					}
				});

				int result = getNewProcessDialogAnswer();
				DebuggeeThread thread=fStoppingThread.getDebuggeeThread();
				try {
					if (result == NewProcessDialog.RUN_TO_MAIN) {
						fDebuggeeProcess.forceRunToMainEntryPoint(getDebugEngine().sendReceiveSynchronously);
					}
				} catch  (IOException e) {
				}
			}
			// If exception has occurred then need to ask user what to do about it
			if (fIsStoppedByException) {
				fIsStoppedByException = false;
				final Shell shell = PICLDebugPlugin.getInstance().getShell();
				final PICLDebugTarget thisTarget = this;
				shell.getDisplay().syncExec(new Runnable() {
					public void run() {
						ExceptionDialog dialog = new ExceptionDialog(shell, thisTarget);
						dialog.open();
					}
				});

				int result = getExceptionDialogAnswer();
				DebuggeeThread thread=fStoppingThread.getDebuggeeThread();
				try {
					if (result == ExceptionDialog.STEP_EXCEPTION) {
						thread.stepException(getDebugEngine().sendReceiveSynchronously);
					} else if (result == ExceptionDialog.RUN_EXCEPTION) {
						thread.runException(getDebugEngine().sendReceiveDefault);
					} else if (result == ExceptionDialog.RETRY_EXCEPTION) {
						thread.ignoreException(getDebugEngine().sendReceiveSynchronously);
					}
				} catch  (IOException e) {
				}
			}
            if (fIsStoppedByNone) {
                fIsStoppedByNone = false;
                AbstractPICLRequest request = fProcessRequest;
                fProcessRequest = null;
                if (request != null && request.isInternal()) {
                    if (request instanceof SuspendProcessRequest) {
                        InternalDebugEvent internalEvent = new InternalSuspendEvent(this);
                        fireInternalEvent(internalEvent);
                    }
                }
            }
		}
    }

    /**
     * @see com.ibm.debug.model.DebuggeeProcessEventListener
     */
    public void moduleAdded(ModuleAddedEvent event) {
        PICLUtils.logEvent("module added",this);
        fModuleParent.addModule(event.getModule());

    }

    /**
     * @see com.ibm.debug.model.DebuggeeProcessEventListener
     */
    public void monitoredExpressionAdded(MonitoredExpressionAddedEvent event) {
        PICLUtils.logEvent("Monitored Expression added to process",this);

        if (getPendingEngineRequest() instanceof MonitorExpressionRequest)
	        ((MonitorExpressionRequest)getPendingEngineRequest()).setMonitoredExpression(event.getMonitoredExpression());
	    else
	    	PICLUtils.logText("Mismatch... expected monitor pending request");

	    fMonitorParent.addMonitoredExpression(event.getMonitoredExpression());
    }

    /**
     * @see DebugEngineEventListener
     */
    public void processAdded(ProcessAddedEvent event) {
    	PICLUtils.logEvent("process added",this);
    	fDebuggeeProcess = event.getProcess();
        loadCachedPropertyValues();
        fDebuggeeProcess.addEventListener(this);
        fConnected= true;
        // load breakpoints set previously now that we are connected to
        // an engine
        loadBreakpoints();
        fireCreationEvent();
        updateCurrentState("state.processadded");
    }

    /**
     * Retrieve property values from the underlying DebuggeeProcess and cache them
     * in this object.  In this way, a PICLDebugTarget acts as a handle to a
     * DebuggeeProcess, allowing the cached properties to be accessed even after
     * the DebuggeeProcess is gone.
     */
    protected void loadCachedPropertyValues() {
        fID= fDebuggeeProcess.processID();
    }

    /**
     * @see com.ibm.debug.model.DebuggeeProcessEventListener
     */
    public void processEnded(ProcessEndedEvent event) {
    	PICLUtils.logEvent("process ended",this);
    	fTerminated = true;   	// this is required to indicate to the children not to send any more
    							// requests to the engine.
        doCleanup();
        updateCurrentState("state.processended");
        fireTerminateEvent();
    }

    /**
     * A process has stopped
     * We want to record the thread which caused the process to stop and
     * mark the process as not updated.
     * Later, during updating, we will get the thread and stack frames to show.
     *
     * @see com.ibm.debug.model.DebuggeeProcessEventListener
     */
    public void processStopped(ProcessStoppedEvent event) {
    	PICLUtils.logEvent("Process stopped event",this);

        fProcessStopInfo= event.getProcessStopInfo();
        DebuggeeThread thread= fProcessStopInfo.getThreadThatCausedProcessToStop();

		// Determine which of the PICLThreads is the stopping thread
		List c= getChildrenAsList();
		Iterator threads= c.iterator();
		while (threads.hasNext()) {
    		PICLThread pt= (PICLThread) threads.next();
    		if (pt !=null && pt.fDebuggeeThread.equals(thread)) {
  		      	fStoppingThread = pt;   // save the stopping thread
     		   	break;
    		}
		}

        if (event.getRequestProperty() instanceof AbstractPICLRequest)
            fProcessRequest = (AbstractPICLRequest)event.getRequestProperty();

		fIsStoppedByBreakpoint = fProcessStopInfo.getReason() == EPDC.Why_break;
		fIsStoppedByFork = fProcessStopInfo.getReason() == EPDC.Why_PgmForked;
		fIsStoppedByExec = fProcessStopInfo.getReason() == EPDC.Why_PgmExeced;
		fIsStoppedByNone = fProcessStopInfo.getReason() == EPDC.Why_none;
		fStoppingThread.setStepping(false);

        // Check if stopped at an internal breakpoint.
        if (fIsStoppedByBreakpoint) {
            Breakpoint breakpoints[] = fProcessStopInfo.getBreakpointsHit();
            if (breakpoints != null) {
                for (int i = 0; i < breakpoints.length; i++) {
                    if(fInternalBreakpointIDs.containsKey(breakpoints[i])) {
                        fInternalBreakpointHit = (InternalBreakpoint)fInternalBreakpointIDs.get(breakpoints[i]);
                        break;
                    }
                }
            }
        }


        setIsRunning(false);  // mark this process as running


		// The behaviour of PICL engines is that all threads are stopped when the process is stopped.
		// The debug plugin expects a suspend event for the thread that stopped.
		// The process stopped event is too soon and it must be sent once the model has finished processing
		// all events.
		// The following flag will be checked when the model has gone to ready state.  At that time the thread's
		// suspend event will be sent.

		fThreadSuspend = true;

        updateCurrentState("state.processstopped");

		if (getPendingEngineRequest() !=null) {
			PICLEngineRequest request = getPendingEngineRequest();
			if (request instanceof StepRequest ||
				request instanceof RunRequest) {
					try {
						request.endRequest();
					} catch(PICLException pe) {
						PICLUtils.logText("PICLDebugTarget: PICLException occurred. Text is >" + pe.getMessage());
					}
				}
			else
				PICLUtils.logText("ERROR-PICLDebugTarget:Pending request not handled =" + request.toString());
		} else
			PICLUtils.logText("!!! PICLDebugTarget:Expected pending request not set");

    }


    /**
     * @see IProcess
     */
    public void resume() throws DebugException {
        if (haveDoneCleanup() || !isSuspended()) {
            // already resumed
            return;
        }

        updateCurrentState("state.running");

        // this is so the threads get updated in the UI to have no children
        Iterator threads= fChildren.iterator();
        while (threads.hasNext()) {
            PICLThread thread= (PICLThread) threads.next();
            thread.fireResumeEvent();
        }

        doResume();


    }

    protected void doResume() throws DebugException {

		// first check to make sure that there isn't a pending engine request
		// by doing this now the state of the debugger won't change
		if (isEngineRequestPending())
			throw new DebugException(new Status(IStatus.ERROR, "com.ibm.debug.picl", IDebugStatusConstants.REQUEST_FAILED, PICLUtils.getResourceString(ERROR + "engine_busy"), null));
    	
		// reset change flags before running the debuggee
		resetChanged();

        RunRequest runRequest = new RunRequest(this);

        try {
	        setIsRunning(true);
 	     	fIsStoppedByException= false;
  	  		fIsStoppedByBreakpoint= false;
   		    fStoppingThread = null;

        	runRequest.execute();
        } catch(PICLEngineBusyException pe) {
        	setIsRunning(false);
			throw new DebugException(new Status(IStatus.ERROR, "com.ibm.debug.picl", IDebugStatusConstants.REQUEST_FAILED, PICLUtils.getResourceString(ERROR + "engine_busy"), null));
		} catch(PICLException pe) {
			setIsRunning(false);
			throw new DebugException(new Status(IStatus.ERROR, "com.ibm.debug.picl", IDebugStatusConstants.REQUEST_FAILED, PICLUtils.getResourceString(ERROR + "request_failed"), null));
		}

    }

    /**
     * @see IBreakpointSupport#breakpointAdded(IMarker)
     * NOTE: marker is required
     */
    public void breakpointAdded(IMarker marker) {
        PICLUtils.logText("PICLDebugTarget.breakpointAdded(IMarker)");

        if (fConnected) {
            if (marker.getResource() != null) {
                try {
                    setBreakpointRequest(marker);
                } catch (DebugException de) {
                    logError(de);
                }
            }
        }
    }

    /**
     * Determines the breakpoint type requested and calls the appropriate method to process it
     * @param The marker that represents the breakpoint to be set.
     * @exception if there is any problem setting the breakpoint
     */
    protected void setBreakpointRequest(IMarker marker) throws DebugException {

        String breakpointType = null;

        // Get the type of the breakpoint (specified in the plugin.xml for this debugger)
        // this type will determine how it should be handled.

        try {
            breakpointType = marker.getType();
        } catch(CoreException e) {
            throw new DebugException(new Status(IStatus.ERROR,"com.ibm.debug",IStatus.OK,PICLUtils.getResourceString(ERROR + "brktype_error"),e));
        }

        // Determine breakpoint type

		BreakpointCreateRequest breakpointRequest = null;

        if (breakpointType.equals(IPICLDebugConstants.PICL_LINE_BREAKPOINT) && supportsBrkptType(IPICLDebugConstants.PICL_LINE_BREAKPOINT)) {
            PICLUtils.logText("setting a PICLLineBreakpoint");
            breakpointRequest = new LineBreakpointRequest(marker,this);
        } else
	        if (breakpointType.equals(IPICLDebugConstants.PICL_ENTRY_BREAKPOINT) && supportsBrkptType(IPICLDebugConstants.PICL_ENTRY_BREAKPOINT)) {
	            PICLUtils.logText("setting a PICLEntryBreakpoint");
	            breakpointRequest = new EntryBreakpointRequest(marker,this);
	        } else
		        if (breakpointType.equals(IPICLDebugConstants.PICL_WATCH_BREAKPOINT) && supportsBrkptType(IPICLDebugConstants.PICL_WATCH_BREAKPOINT)) {
		            PICLUtils.logText("setting a PICLWatchBreakpoint");
		            breakpointRequest = new WatchpointRequest(marker,this);
		        } else
			        if (breakpointType.equals(IPICLDebugConstants.PICL_LOAD_BREAKPOINT) && supportsBrkptType(IPICLDebugConstants.PICL_LOAD_BREAKPOINT)) {
			            PICLUtils.logText("setting a PICLLoadBreakpoint");
			            breakpointRequest = new LoadBreakpointRequest(marker, this);
			        } else
				        if (breakpointType.equals(IPICLDebugConstants.PICL_ADDRESS_BREAKPOINT) && supportsBrkptType(IPICLDebugConstants.PICL_ADDRESS_BREAKPOINT)) {
				            PICLUtils.logText("setting a PICLAddressBreakpoint");
				            breakpointRequest = new AddressBreakpointRequest(marker, this);
				        } else // bad breakpoint type
				            throw new DebugException(new Status(IStatus.ERROR,"com.ibm.debug",IStatus.OK,PICLUtils.getResourceString(ERROR + "brktype_error"),null));

		try {
			breakpointRequest.execute();
		} catch(PICLEngineBusyException pe) {
			PICLUtils.logText("Engine busy... breakpoint not set");
        	throw new DebugException(new Status(IStatus.ERROR,"com.ibm.debug",IStatus.OK,PICLUtils.getResourceString(ERROR + "engine_busy"),null));
		} catch(PICLException pe) {
        	throw new DebugException(new Status(IStatus.ERROR,"com.ibm.debug",IStatus.OK,PICLUtils.getResourceString(ERROR + "set_breakpoint_failed"),null));
		}


		return;

    }

	/**
	 * Check to see if the engine supports this type of breakpoint
	 * @param String that represents the breakpoint type as described in IPICLDebugContants
	 * @return true if engine supports type of breakpoint
	 */

	public boolean supportsBrkptType(String BrkptType) {
		try{ //null pointers thrown if engine capabilities not known yet
		if (BrkptType.equals(IPICLDebugConstants.PICL_LINE_BREAKPOINT))
			return (fDebugEngine.getCapabilities().getBreakpointCapabilities().lineBreakpointsSupported());
		else
			if (BrkptType.equals(IPICLDebugConstants.PICL_ADDRESS_BREAKPOINT))
				return (fDebugEngine.getCapabilities().getBreakpointCapabilities().addressBreakpointsSupported());
			else
				if (BrkptType.equals(IPICLDebugConstants.PICL_ENTRY_BREAKPOINT))
					return (fDebugEngine.getCapabilities().getBreakpointCapabilities().functionBreakpointsSupported());
				else
					if (BrkptType.equals(IPICLDebugConstants.PICL_LOAD_BREAKPOINT))
						return (fDebugEngine.getCapabilities().getBreakpointCapabilities().moduleLoadBreakpointsSupported());
					else
						if (BrkptType.equals(IPICLDebugConstants.PICL_WATCH_BREAKPOINT))
							return (fDebugEngine.getCapabilities().getBreakpointCapabilities().watchpointsSupported());
						else
							return false;
		}catch(Exception e){return false;}
	}


	/**
	 * Returns a class that can be used to query breakpoint capabilities of the engine associated
	 * with this debug target
	 * @return A class whose methods can be used to query the breakpoint capabilities of this debug target
	 */

	public EngineBreakpointCapabilities getBreakpointCapabilities() {
		try{ //capabilities might be null if called during init
			if (fDebugEngine != null)
				return fDebugEngine.getCapabilities().getBreakpointCapabilities();
			else
				return null;
		}catch(Exception e) { return null; }
	}

	/**
	 * Returns an ID that can be used to uniquely identify this debug target
	 * @return a string that represents this debug target
	 */

	public String getUniqueID() {
		return String.valueOf(fID);
	}

    /**
     * @see IExceptionSupport
     */
//  public void exceptionAdded(IException exception) {
//      exceptionChanged(exception);
//  }

    /**
     * @see IExceptionSupport
     */
//  public void exceptionChanged(IException exception) {
//      if (fConnected) {
//          int state= exception.getState();
//          boolean caught= (state == IException.EXCEPTION_ENABLED_ALL || state == IException.EXCEPTION_ENABLED_CAUGHT);
//          boolean uncaught= (state == IException.EXCEPTION_ENABLED_ALL || state == IException.EXCEPTION_ENABLED_UNCAUGHT);
//
//          if (caught || uncaught) {
//              DebuggeeException ex= getDebuggeeException(exception);
//              if (ex != null) {
//                  fEnabledExceptions.addElement(exception);
//                  ex.enable();
//                  try {
//                      if (!fDebugEngine.changeExceptionStatus()) {
//                          DebugException de= new DebugException(IDebugStatusConstants.TARGET_REQUEST_FAILED, "PICLDebugTarget#exceptionChanged", null);
//                          logError(de);
//                      }
//                  } catch (IOException ioe) {
//                      logError(new DebugException(IDebugStatusConstants.TARGET_REQUEST_FAILED, PICLUtils.getResourceString(COMM_FAILURE), ioe));
//                  }
//              }
//          } else {
//              exceptionRemoved(exception);
//          }
//      }
//  }

    /**
     * @see IExceptionSupport
     */
//  public void exceptionRemoved(IException exception) {
//      if (fConnected) {
//          DebuggeeException ex= getDebuggeeException(exception);
//          if (ex != null) {
//              fEnabledExceptions.removeElement(exception);
//              ex.disable();
//              try {
//                  if (!fDebugEngine.changeExceptionStatus()) {
//                      DebugException de= new DebugException(IDebugStatusConstants.TARGET_REQUEST_FAILED, "PICLDebugTarget#exceptionRemoved", null);
//                      logError(de);
//                  }
//              } catch (IOException ioe) {
//                  logError(new DebugException(IDebugStatusConstants.TARGET_REQUEST_FAILED, PICLUtils.getResourceString(COMM_FAILURE), ioe));
//              }
//          }
//      }
//  }

    /**
     * Returns the <code>DebuggeeException</code> corresponding to the debug plugin excpetion
     */
//  protected DebuggeeException getDebuggeeException(IException e) {
//      DebuggeeException[] exceptions= fDebugEngine.getExceptions();
//      for (int i= 0; i < exceptions.length; i++) {
//          if (exceptions[i].name().equals(e.getName())) {
//              return exceptions[i];
//          }
//      }
//      return null;
//  }

    /**
     * Sets whether the process is running or not, and DOES NOT fire
     * the applicable resume or suspend event.
     */
    protected void setIsRunning(boolean running) {
        fIsRunning= running;
    }

    /**
     * Sets whether the process is running or not, and DOES NOT fire
     * the applicable resume or suspend event.
     */
    protected void setIsRunning(boolean running, int detail) {
        fIsRunning= running;
    }


    /**
     * @see com.ibm.debug.model.DebuggeeProcessEventListener
     */
    public void storageAdded(StorageAddedEvent event) {
    	PICLUtils.logEvent("Storage added",this);

        if (getPendingEngineRequest() instanceof MonitorStorageRequest)
	        ;
	    else
	    	PICLUtils.logText("Mismatch... expected monitor pending request");

		fStorageParent.addStorage(event.getStorage());
    }

    /**
     * @see IExceptionSupport
     */
    public boolean supportsExceptions() {
        return !isTerminated();
    }

    /**
     * @see IProcess
     */
    public void suspend() throws DebugException {
        if (haveDoneCleanup() || isSuspended()) {
            return;
        }
        performRequest(new SuspendProcessRequest());
    }

    /**
     * @see IProcess
     */
    public void terminate() throws DebugException {
        if (haveDoneCleanup() || isTerminated()) {
            return;
        }
        suspend();
        setIsRunning(false);
        performRequest(new TerminateProcessRequest());
    }

    protected void terminateEngine() throws DebugException {
        if (haveDoneCleanup() || isTerminated()) {
            return;
        }
        performRequest(new TerminateEngineRequest());
    }

    /**
     * Adds a thread to our list of threads for this process
     *
     * @see com.ibm.debug.model.DebuggeeProcessEventListener
     */
    public void threadAdded(ThreadAddedEvent event) {
    	PICLUtils.logEvent("Thread added event",this);
        PICLThread threadElement= new PICLThread(this, event.getThread());
        addChild(threadElement);
    }

    /**
     * Sets all the breakpoints to be uninstalled.
     */
    protected void uninstallAllBreakpoints() {
        Enumeration markers= ((Hashtable) fBreakpoints.clone()).keys();
        IBreakpointManager manager= getBreakpointManager();
        while (markers.hasMoreElements()) {
            IMarker marker= (IMarker) markers.nextElement();
            fBreakpoints.remove(marker);
            breakpointUnInstalled(marker);

        }
    }

    /**
     * @see IBreakpointSupport
     */
    public void breakpointRemoved(IMarker marker, IMarkerDelta delta) {
        PICLUtils.logText("PICLDebugTarget.breakpointRemoved(IMarker,IMarkerDelta)");

        if (!fConnected)
            return;

        Breakpoint breakpoint= (Breakpoint) fBreakpoints.get(marker);
        if (breakpoint != null) {
            try {
            	BreakpointRequest brkreq = new BreakpointDeleteRequest(this, breakpoint, marker);

            	brkreq.execute();
            } catch(PICLException pe) {
        	}

        }
    }

    /**
     * @see IBreakpointSupport
     */
    public void breakpointChanged(IMarker marker, IMarkerDelta delta) {
        PICLUtils.logText("PICLDebugTarget.breakpointChanged(IMarker,IMarkerDelta)");

        if (!fConnected)
            return;

		// first check to see if we have a breakpoint that matches this change request
		// if one can't be found then create one.
		// if a breakpoint does already exist, check to see if this is a request to
		// update the breakpoint on the engine

		Breakpoint breakpoint = (Breakpoint) fBreakpoints.get(marker);

		// if this is a request to update breakpoints on the engine and there is no breakpoint
		// then create one.

		if (marker.getAttribute(IPICLDebugConstants.UPDATE_BREAKPOINT,false) && breakpoint == null) {

       		if (marker.getResource() != null) {
               	try {
                   	setBreakpointRequest(marker);
               	} catch (DebugException de) {
                   	logError(de);
               	}
           	}
		} else {   				// there is a matching breakpoint... update it.

			// check to see if this change requires a call to the debug engine

			// first check the enable/disable setting
			try{
				if (marker.getAttribute(IDebugConstants.ENABLED,true) != breakpoint.isEnabled()
					&& getBreakpointCapabilities().breakpointEnableDisableSupported()) {
					BreakpointEnableRequest req = new BreakpointEnableRequest(this,
																		  breakpoint,
																		  marker);
					try {
						req.execute();
					} catch(PICLEngineBusyException pe) {
						PICLUtils.logText("Engine busy... breakpoint enablement changed");
					} catch(PICLException pe) {
						PICLUtils.logText("Error changing breakpoint");
					}
				}


				// now do the attribute changes

				if (marker.getAttribute(IPICLDebugConstants.UPDATE_BREAKPOINT,false)
					&& getBreakpointCapabilities().breakpointModifySupported()) {
					// the request is to update the breakpoint on the engine side then use the
					// changes in the attributes to do that.

					BreakpointModifyRequest breakpointRequest = null;

					if (breakpoint instanceof LineBreakpoint
						&& supportsBrkptType(IPICLDebugConstants.PICL_LINE_BREAKPOINT)) {
						PICLUtils.logText("modifying a PICLLineBreakpoint");
						breakpointRequest = new LineBreakpointModifyRequest(this, marker, delta, breakpoint);
					} else
		  				if (breakpoint instanceof EntryBreakpoint
		  					&& supportsBrkptType(IPICLDebugConstants.PICL_ENTRY_BREAKPOINT)) {
							PICLUtils.logText("modifying a PICLEntryBreakpoint");
							breakpointRequest = new EntryBreakpointModifyRequest(this, marker, delta, breakpoint);
		  				}
		  				else
		  					if (breakpoint instanceof AddressBreakpoint
		  						&& supportsBrkptType(IPICLDebugConstants.PICL_ADDRESS_BREAKPOINT)) {
								PICLUtils.logText("modifying a PICLAddressBreakpoint");
								breakpointRequest = new AddressBreakpointModifyRequest(this, marker, delta, breakpoint);
		  					} else
		  		   				if (breakpoint instanceof Watchpoint
		  		   					&& supportsBrkptType(IPICLDebugConstants.PICL_WATCH_BREAKPOINT)) {
									PICLUtils.logText("modifying a PICLWatchpoint");
									breakpointRequest = new WatchpointModifyRequest(this, marker, delta, breakpoint);
			  		   			} else
		 	 		 	  			if (breakpoint instanceof ModuleLoadBreakpoint
		  			 	  				&& supportsBrkptType(IPICLDebugConstants.PICL_LOAD_BREAKPOINT)) {
										PICLUtils.logText("modifying a PICLLoadBreakpoint");
										breakpointRequest = new LoadBreakpointModifyRequest(this, marker, delta, breakpoint);
						 	       } else // bad breakpoint type
						  	          return;  // error condition.

					try {
						breakpointRequest.execute();
					} catch(PICLEngineBusyException pe) {
						PICLUtils.logText("Engine busy... breakpoint not changed");
					} catch(PICLException pe) {
						PICLUtils.logText("Error changing breakpoint");
					}


				}
			}catch(Exception e){ PICLUtils.logText("Engine busy... breakpoint not changed");}
		}

    }


    /**
     * @see PICLDebugElement
     */
     protected void doCleanupDetails() {
     	try{
        	// Unregister as a process listener, and reset all flags
        	fDebuggeeProcess.removeEventListener(this);
        	fIsRunning= false;
        	fTerminated= true;
        	fConnected= false;
        	fStoppingThread = null;

	        // Process specific cleanup
 	       uninstallAllBreakpoints();

	        // Finally, null out the encapsulated PICL process object
 	       fDebuggeeProcess= null;
  	      fSystemProcess = null;

	        // remove the parent objects
 	       if (getModuleParent() != null) {
	 	       getModuleParent().doCleanup();
 	 	      fModuleParent = null;
    	    }
     	   if (getMonitorParent() != null) {
      		  	getMonitorParent().doCleanup();
        		fMonitorParent = null;
        	}
        	if (getStorageParent() != null) {
        		getStorageParent().doCleanup();
        		fStorageParent = null;
        	}
     	}catch(Exception e) {}  //may happen when workbench closed without terminating debuggee first

     }

     /**
      * @see IVariableLookup
      */
     public IVariable findVariable(String name) {
        // not yet implemented
        return null;
     }

     /**
     * @see IBreakpointSupport#supportsBreakpoint(IMarker)
     * Determines if this debug target supports the breakpoint type
     */
    public boolean supportsBreakpoint(IMarker breakpoint) {
        PICLUtils.logText("PICLDebugTarget.supportsBreakpoint(IMarker)");

        String breakpointModelIdentifier = getBreakpointManager().getModelIdentifier(breakpoint);
        String modelIdentifier = getModelIdentifier();

        if (modelIdentifier.equals(breakpointModelIdentifier))
            return true;

        return false;  // unsupported breakpoint type
    }

    public void commandLogResponse(DebugEngineCommandLogResponseEvent event) {
    	PICLUtils.logEvent("command log response (not used)",this);
    }

    /**
     * remoteExtension(String)
     * @param File name that will have extension removed.
     * @return Returns string minus extension
     */
    protected String removeExtension(String name) {
        int posn = name.lastIndexOf('.');
        return name.substring(0,posn);
    }

    /**
     * Loads breakpoints from the breakpoint manager that were set before this debug session
     * started.
     */

    private void loadBreakpoints()  {
        IMarker[] breakpoints= getDebugPlugin().getBreakpointManager().getBreakpoints();
        for (int i= 0; i < breakpoints.length; i++) {
            if (supportsBreakpoint(breakpoints[i])) {  // check to make sure these breakpoints are for this debug engine
            	// force the breakpoint to deferred and using update on change will create the breakpoint
            	try {
            		String[] attributes = {IPICLDebugConstants.UPDATE_BREAKPOINT,
            							   IPICLDebugConstants.DEFERRED};
         			Object[] values = {new Boolean(true), new Boolean(true)};
            		breakpoints[i].setAttributes(attributes,values);
            	} catch(CoreException ce) {}

//                breakpointAdded(breakpoints[i]);
            }
        }

    }

    /**
     * Updates the state of the debug target and notifies that a change has happened.
     * The change event will update the UI with the new state
     * @param label from resource that represents the new state
     */

    protected void updateCurrentState(String stateLabel) {
        fCurrentState = PICLUtils.getResourceString(PREFIX + stateLabel);
        fireChangeEvent();
    }

    /**
     * Returns an array of DebuggeeException objects. This information is available only after the debug engine has been
     * successfully initialized via the initialize() method.
     */
    public DebuggeeException[] getSupportedExceptions()
    {
        if(fDebugEngine == null)
            return null;
        return fDebugEngine.getExceptions();
    }

    //Temporary method
    //Right now, dialog is sending enable/disable events to DebuggeeExceptions directly
    //and then using this dialog to commit the changes to all exceptions at once.
    public void commitExceptionChanges(boolean setAsDefaults)
    {
    	try{
    		if(!fDebugEngine.commitPendingExceptionStateChanges(setAsDefaults))
    		;		//TODO: throw up dialog
    	}catch(IOException e){//TODO: throw up dialog
    	}
    }

     /**
      * TODO: create IExceptionSupport...
     * @see IExceptionSupport#supportsExceptionFiltering()
     */
    public boolean supportsExceptionFiltering() {
    	try{
    		return fDebugEngine.getCapabilities().getExceptionCapabilities().exceptionFilterSupported();
    	}catch(Exception e) { return false; }
    }

    /**
     * Helper class - given a marker this will return a Location object.
     * @param marker that represents the location in the source
     * @param the view that the marker represents.  e.g. source view
     * @return a Location object that represents a location in a program file
     */
    protected ViewFile getViewFile(IMarker marker, ViewInformation viewInformation) {

        // get the name of the file from the marker
        IResource resource = marker.getResource();
        String resourceFileName = resource.getName();

  		// If the resource is a project then the real file name should be stored in the marker
		if (resource instanceof IProject) {
			try {
				if ((marker.getType() == IPICLDebugConstants.PICL_LINE_BREAKPOINT) ||
					(marker.getType() == IPICLDebugConstants.PICL_MONITORED_EXPRESSION) ||
					(marker.getType() == IPICLDebugConstants.PICL_LOCATION_MARKER))
					resourceFileName = (String) marker.getAttribute(IPICLDebugConstants.SOURCE_FILE_NAME);
			} catch (CoreException ce)
			{}
		}

         // get a list of the modules first
        Module[] modules = fDebuggeeProcess.getModulesArray();

        if (modules == null || modules.length == 0)
            return null;

        // loop through modules and process each part
        for (int i = 0; i < modules.length; i++ ) {
            Module m = modules[i];
            if (m != null) {
                Part[] parts = m.getPartsArray();
                if (parts == null || parts.length == 0)
                    continue;

                // loop through the parts and get the view
                for (int j = 0; j < parts.length; j++) {
                    Part p = parts[j];
                    if (p == null)
                        continue;

                    View v = p.view(viewInformation);
                    Vector files = null;
                    try {
                        files = v.getFiles();
                    } catch(Exception e){
                        continue;
                    }
                    if (files == null || files.size() == 0)
                        continue;
                    Enumeration enum = files.elements();

                    // loop through the files in the view looking for the matching file name
                    while (enum.hasMoreElements()) {
                        ViewFile vf = (ViewFile)enum.nextElement();
                        if (vf == null)
                        	continue;
                        try {
                            if (vf.baseFileName().equals(resourceFileName)) {
                                PICLUtils.logText("Found file! Module=" + m.getQualifiedName() +
                                				  " Part=" + p.name() +
                                				  " View=" + v.kind() +
                                				  " File=" + vf.name());
                                return vf;
                            }
                        } catch(Exception e) {}
                    }
                }
            }
        }


        return null;
    }
	/**
	 * Gets the debuggeeProcess
	 * @return Returns a DebuggeeProcess
	 */
	public DebuggeeProcess getDebuggeeProcess() {
		return fDebuggeeProcess;
	}
    /**
     * Sets the systemProcess
     * @param systemProcess The systemProcess to set
     */
    public void setProcess(IProcess systemProcess) {
        fSystemProcess = systemProcess;
    }

	/**
	 * Gets the pendingEngineRequest
	 * @return Returns a PICLEngineRequest
	 */
	public PICLEngineRequest getPendingEngineRequest() {
		return fPendingEngineRequest;
	}

    /**
     * Sets the pendingEngineRequest
     * @param pendingEngineRequest The pendingEngineRequest to set
     */
    public synchronized void setPendingEngineRequest(PICLEngineRequest pendingEngineRequest) {
        fPendingEngineRequest = pendingEngineRequest;
    }

	/**
	 * Checks if pending request
	 * 	@return true if pending request
	 */
	public boolean isEngineRequestPending() {
		return (getPendingEngineRequest() != null);
	}

    /**
     * @see IRegisterSupport#supportsRegisters()
     */
    public boolean supportsRegisters() {
    	try{
	    	return (fDebugEngine.getCapabilities().getWindowCapabilities().monitorRegistersSupported());
    	}catch(Exception e) {return false;}
    }

	/**
	 * Get the parent to the module list
	 * @return PICLModuleParent
	 */
	public PICLModuleParent getModuleParent() {
		return fModuleParent;
	}

	/**
	 * Get the parent to the monitor list
	 * @return PICLMonitorParent
	 */
	public PICLMonitorParent getMonitorParent() {
		return fMonitorParent;
	}

	/**
	 * Get the parent to the Storage list
	 * @return PICLMonitorParent
	 */
	public PICLStorageParent getStorageParent() {
		return fStorageParent;
	}

	/**
	 * Gets the preferredView
	 * @return Returns a ViewInformation
	 */
	public ViewInformation getPreferredView() {
		return fPreferredView;
	}
	/**
	 * Sets the preferredView
	 * @param preferredView The preferredView to set
	 */
	public void setPreferredView(ViewInformation preferredView) {
		fPreferredView = preferredView;
	}

	/**
	 * Reset changed flags for all objects that support them and are owned
	 * by this debug target
	 */
	public void resetChanged() {

		if (getMonitorParent() !=null)
			getMonitorParent().resetChanged();

		// loop through threads (children of this object) and reset the register changed flags
		// and the stack local variables

		if (!hasChildren())
			return;

		IDebugElement threads[] = null;
		try {
			threads = getChildrenNoExpand();
		} catch(DebugException de) {
			return;
		}

		for (int i = 0;i < threads.length; i++) {
			if (threads[i] != null) {   // process only non-null thread entries
				((PICLThread)threads[i]).resetChanged();
				PICLRegisterGroupParent parent = ((PICLThread)threads[i]).getRegisterGroupParent();
				if (parent != null)
					parent.resetChanged();
			}
		}
	}

	/**
	 * Given a breakpoint marker, return the corresponding Breakpoint
	 */
	public Breakpoint getBreakpoint(IMarker marker) {
		return (Breakpoint) fBreakpoints.get(marker);
	}

	/**
	 * This method will be called by the ForkDialog (which is run on
	 * the UI thread) to store the user's selection from the dialog.
	 * See fForkDialogAnswer for possible values.
	 */
	public void setForkDialogAnswer(int value) {
		fForkDialogAnswer = value;
	}

	/**
	 * Get the stored result of the ForkDialog.
	 * See fForkDialogAnswer for possible values.
	 */
	public int getForkDialogAnswer() {
		return fForkDialogAnswer;
	}

	/**
	 * This method will be called by the ExceptionDialog (which is run on
	 * the UI thread) to store the user's selection from the dialog.
	 * See fExceptionDialogAnswer for possible values.
	 */
	public void setExceptionDialogAnswer(int value) {
		fExceptionDialogAnswer = value;
	}

	/**
	 * Get the stored result of the ExceptionDialog.
	 * See fExceptionDialogAnswer for possible values.
	 */
	public int getExceptionDialogAnswer() {
		return fExceptionDialogAnswer;
	}

	/**
	 * This method will be called by the NewProcessDialog (which is run on
	 * the UI thread) to store the user's selection from the dialog.
	 * See fNewProcessDialogAnswer for possible values.
	 */
	public void setNewProcessDialogAnswer(int value) {
		fNewProcessDialogAnswer = value;
	}

	/**
	 * Get the stored result of the NewProcessDialog.
	 * See fNewProcesssDialogAnswer for possible values.
	 */
	public int getNewProcessDialogAnswer() {
		return fNewProcessDialogAnswer;
	}
	/**
	 * Gets the processStopInfo
	 * @return Returns a ProcessStopInfo
	 */
	public ProcessStopInfo getProcessStopInfo() {
		return fProcessStopInfo;
	}

    public void setInternalEventListener(IInternalEventListener listener) {
        fInternalEventListeners.add(listener);
    }

    public void clearInternalEventListener(IInternalEventListener listener) {
        fInternalEventListeners.remove(listener);
    }

    protected void fireInternalEvent(InternalDebugEvent event) {
        for(int i = 0; i < fInternalEventListeners.size(); i++) {
            event.fireEvent((IInternalEventListener)fInternalEventListeners.get(i));
        }
    }

    protected class InternalBreakpoint {
        protected Breakpoint fBreakpoint;

        public Breakpoint getBreakpoint() {
            return fBreakpoint;
        }

        public void setBreakpoint(Breakpoint breakpoint) {
            fBreakpoint = breakpoint;
        }
    }

    public Object setInternalEntryBreakpoint(String entryPoint, String module) {
        InternalBreakpoint internalBreakpoint = new InternalBreakpoint();
        InternalBreakpointCreateRequest breakpointRequest =
            new InternalEntryBreakpointRequest(entryPoint,
                                               module,
                                               null,
                                               this,
                                               internalBreakpoint);
        try {
            breakpointRequest.execute();
        } catch(PICLEngineBusyException pe) {
            PICLUtils.logText("Engine busy... breakpoint not set");
            return null;
        } catch(PICLException pe) {
            PICLUtils.logText("PICL exception: " + pe);
            return null;
        }

        return internalBreakpoint;
    }

    public void clearInternalEntryBreakpoint(Object breakpointID) {
        if(!(breakpointID instanceof InternalBreakpoint))
            return;

        Breakpoint breakpoint = ((InternalBreakpoint)breakpointID).getBreakpoint();
        if(breakpoint == null)
            return;

        try {
            BreakpointRequest breakpointRequest =
                new BreakpointDeleteRequest(this, breakpoint, breakpointID);
            breakpointRequest.execute();
        } catch(PICLException pe) {
        }
    }

    public void internalSuspend() throws DebugException {
        if (haveDoneCleanup() || isSuspended()) {
            return;
        }
        SuspendProcessRequest request = new SuspendProcessRequest();
        request.setInternal(true);
        performRequest(request);
    }

    public void internalResume() throws DebugException {
        if (haveDoneCleanup() || !isSuspended()) {
            // already resumed
            return;
        }

        doResume();

        InternalDebugEvent event = new InternalResumeEvent(this);
        fireInternalEvent(event);
    }
    /**
     * @see IStorageSupport#supportsStorageMonitors()
     */
    public boolean supportsStorageMonitors() {
    	if (fDebugEngine == null)
    		return false;
    	else {
    		try{
    			// First, does the engine support the storage view
    			if (!fDebugEngine.getCapabilities().getWindowCapabilities().monitorStorageSupported())
    				return false;

	    		// for now we look for a specific storage style

 		   		StorageStyle stgStyle = StorageStyle.getStorageStyle(EPDC.StorageStyleByteHexCharacter);

 		   		if (!stgStyle.isSupported(fDebugEngine))
   		 			return false;
    		}catch (Exception e){ return false;}

    	}
    	return true;
    }

    /**
     * @see IStorageSupport#supportsStorageMapping()
     */
    public boolean supportsStorageMapping() {
        return false;
    }
    
}

