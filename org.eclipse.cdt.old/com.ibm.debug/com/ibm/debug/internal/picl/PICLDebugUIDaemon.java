package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/PICLDebugUIDaemon.java, eclipse, eclipse-dev, 20011128
// Version 1.20 (last modified 11/28/01 15:58:00)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILauncher;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.jface.preference.IPreferenceStore;

import com.ibm.debug.PICLDebugPlugin;
import com.ibm.debug.connection.ConnectionInfo;
import com.ibm.debug.connection.DebugDaemon;
import com.ibm.debug.launch.IPICLLauncher;
import com.ibm.debug.launch.PICLAttachInfo;
import com.ibm.debug.launch.PICLDaemonInfo;
import com.ibm.debug.launch.PICLLoadInfo;
import com.ibm.debug.launch.PICLStartupInfo;
import com.ibm.debug.model.DebugEngineWaitingEvent;
import com.ibm.debug.model.EngineDaemon;
import com.ibm.debug.model.EngineDaemonEventListener;
import com.ibm.debug.model.Model;
import com.ibm.debug.WorkspaceSourceLocator;

/**
 * Represents the UI daemon that will listen for incoming requests to start a debug session
 */

public class PICLDebugUIDaemon implements IDebugTarget,EngineDaemonEventListener {

    private EngineDaemon daemon = null;
    private ILauncher launcher;
    private Object selection;
    private String portNumber;
    private int port;

    /**
     * Constructor for PICLDebugUIDaemon
     */
    public PICLDebugUIDaemon(ILauncher launcher, Object obj) {
        this.launcher = launcher;
        this.selection = obj;

        // get the port number from the preference store
        IPreferenceStore preferenceStore = PICLDebugPlugin.getDefault().getPreferenceStore();
        portNumber = preferenceStore.getString(PICLDebugPlugin.DAEMON_PORT);

        // convert the port number
        try {
            port = Integer.parseInt(portNumber);
        } catch(NumberFormatException e) {
        }

        // start the UI daemon listening
        daemon = new EngineDaemon();
        daemon.addEventListener(this);
        daemon.startListening(portNumber);
    }

    String getportNumber() {
        return portNumber;
    }

    /**
     * Get the port the daemon is listening on.
     * @returns The port as an int.
     */
    public int getPort() {
        return port;
    }

    /**
     * @see IAdaptable#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class arg0) {
        return null;
    }

    /**
     * @see IDisconnect#isDisconnected()
     */
    public boolean isDisconnected() {
        return false;
    }

    /**
     * @see IDisconnect#disconnect()
     */
    public void disconnect() throws DebugException {
    }

    /**
     * @see IDisconnect#canDisconnect()
     */
    public boolean canDisconnect() {
        return false;
    }

    /**
     * @see IBreakpointSupport#breakpointChanged(IMarker, IMarkerDelta)
     */
    public void breakpointChanged(IMarker breakpoint, IMarkerDelta delta) {
    }

    /**
     * @see IBreakpointSupport#breakpointRemoved(IMarker, IMarkerDelta)
     */
    public void breakpointRemoved(IMarker breakpoint, IMarkerDelta delta) {
    }

    /**
     * @see IBreakpointSupport#breakpointAdded(IMarker)
     */
    public void breakpointAdded(IMarker breakpoint) {
    }

    /**
     * @see IBreakpointSupport#supportsBreakpoint(IMarker)
     */
    public boolean supportsBreakpoint(IMarker breakpoint) {
        return false;
    }

    /**
     * @see ISuspendResume#suspend()
     */
    public void suspend() throws DebugException {
    }

    /**
     * @see ISuspendResume#resume()
     */
    public void resume() throws DebugException {
    }

    /**
     * @see ISuspendResume#isSuspended()
     */
    public boolean isSuspended() {
        return false;
    }

    /**
     * @see ISuspendResume#canSuspend()
     */
    public boolean canSuspend() {
        return false;
    }

    /**
     * @see ISuspendResume#canResume()
     */
    public boolean canResume() {
        return false;
    }

    /**
     * @see ITerminate#terminate()
     */
    public void terminate() throws DebugException {
        daemon.stopListening();
        daemon.removeEventListener(this);
        DebugPlugin.getDefault().getLaunchManager().deregisterLaunch(getLaunch());
    }

    /**
     * @see ITerminate#isTerminated()
     */
    public boolean isTerminated() {
        return !daemon.isListening();
    }

    /**
     * @see ITerminate#canTerminate()
     */
    public boolean canTerminate() {
        return true;
    }

    /**
     * @see IDebugElement#getModelIdentifier()
     */
    public String getModelIdentifier() {
        return null;
    }

    /**
     * @see IDebugElement#hasChildren()
     */
    public boolean hasChildren() throws DebugException {
        return false;
    }

    /**
     * @see IDebugElement#getThread()
     */
    public IThread getThread() {
        return null;
    }

    /**
     * @see IDebugElement#getStackFrame()
     */
    public IStackFrame getStackFrame() {
        return null;
    }

    /**
     * @see IDebugElement#getSourceLocator()
     */
    public ISourceLocator getSourceLocator() {
        return null;
    }

    /**
     * @see IDebugElement#getParent()
     */
    public IDebugElement getParent() {
        return null;
    }

    /**
     * @see IDebugElement#getName()
     */
    public String getName() throws DebugException {
        return "Debug UI daemon listening on " + portNumber;
    }

    /**
     * @see IDebugElement#getLaunch()
     */
    public ILaunch getLaunch() {
        return DebugPlugin.getDefault().getLaunchManager().findLaunch(this);
    }

    /**
     * @see IDebugElement#getElementType()
     */
    public int getElementType() {
        return 0;
    }

    /**
     * @see IDebugElement#getDebugTarget()
     */
    public IDebugTarget getDebugTarget() {
        return this;
    }

    /**
     * @see IDebugElement#getChildren()
     */
    public IDebugElement[] getChildren() throws DebugException {
        return null;
    }

    /**
     * @see IDebugTarget#getProcess()
     */
    public IProcess getProcess() {
        return null;
    }

    /**
     * @see EngineDaemonEventListener#debugEngineWaiting(DebugEngineWaitingEvent)
     */
    public void debugEngineWaiting(DebugEngineWaitingEvent event) {
        //System.out.println("Engine waiting event");


        DebugDaemon.EngineParameters engineParameters = event.getEngineParameters();
        PICLStartupInfo startupInfo = retrieveStartupInfo(engineParameters.getStartupKey());
        if(startupInfo != null)
        {
            launchDebugSession(startupInfo, event.getConnectionInfo());
        }
        else
        {
            startupInfo = generateStartupInfo(engineParameters);
            PICLLaunchManager.getPICLLaunchManager().handleLaunch(event.getConnectionInfo(), startupInfo, engineParameters.getPairs());
        }
    }

    /**
     * Launch a debug session.
     * @param startupInfo The startup info to use to launch.  If null then
     *                    the session will be terminated immediately.
     * @param connectionInfo The info for connecting to the engine.
     */
    public static void launchDebugSession(PICLStartupInfo startupInfo, ConnectionInfo connectionInfo)
    {
        PICLDebugTarget target = new PICLDebugTarget();
        if(startupInfo != null)
        {
            ILaunch launch = new Launch(startupInfo.getLauncher(),
                                        ILaunchManager.DEBUG_MODE,
                                        startupInfo.getResource(),
                                        startupInfo.getWorkspaceSourceLocator(),
                                        null,
                                        target);
            DebugPlugin.getDefault().getLaunchManager().registerLaunch(launch);
        }

        target.engineIsReadyToConnect(startupInfo, connectionInfo);
    }

    public static void terminateEngine(ConnectionInfo connectionInfo) {
        if(!connectionInfo.isClosed()) {
            PICLDebugTarget target = new PICLDebugTarget();
            target.engineIsReadyToConnect(null, connectionInfo);
            connectionInfo.setClosed(true);
        }
    }

    /*
     * Retrieve saved startup info given a key.
     */
    protected PICLStartupInfo retrieveStartupInfo(String key)
    {
        if(key != null) {
            try
            {
                int integerKey = Integer.parseInt(key);
                PICLStartupInfo startupInfo = PICLDebugPlugin.getDefault().retrieveStartupInfo(integerKey);
                if(startupInfo != null)
                    return startupInfo;
            }
            catch(NumberFormatException e)
            {
            }
        }

        return null;
    }

    /*
     * Generate startup info based on arguments passed to the daemon.
     */
    protected PICLStartupInfo generateStartupInfo(DebugDaemon.EngineParameters engineParameters)
    {
        PICLStartupInfo startupInfo = null;
        String processID = engineParameters.getProcessID();
        if(processID != null)
        {
            startupInfo = new PICLAttachInfo();
            initStartupInfo(engineParameters, (PICLAttachInfo)startupInfo);
        }
        else
        {
            startupInfo = new PICLLoadInfo();
            initStartupInfo(engineParameters, (PICLLoadInfo)startupInfo);
        }

        String projectName = engineParameters.getProject();
        if(projectName != null)
        {
            IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
            if(project != null)
                startupInfo.setResource(project);
        }

        String launcherID = engineParameters.getLauncher();
        if(launcherID != null)
        {
            ILauncher[] launchers = DebugPlugin.getDefault().getLaunchManager().getLaunchers(ILaunchManager.DEBUG_MODE);
            boolean found = false;
            for(int i = 0; !found && i < launchers.length; i++)
            {
                if(launcherID.equals(launchers[i].getIdentifier()))
                {
                    if(launchers[i].getDelegate() instanceof IPICLLauncher)
                        startupInfo.setLauncher(launchers[i]);
                    found = true;
                }
            }
        }

        return startupInfo;
    }

    protected void initStartupInfo(DebugDaemon.EngineParameters engineParameters, PICLAttachInfo attachInfo)
    {
        attachInfo.setProcessID(engineParameters.getProcessID());

        String startupBehaviour = engineParameters.getAttachStartupBehaviour();
        if(startupBehaviour != null)
        {
            if(startupBehaviour.equals("stop"))
                attachInfo.setStartupBehaviour(PICLAttachInfo.STOP);
            else if(startupBehaviour.equals("run"))
                attachInfo.setStartupBehaviour(PICLAttachInfo.RUN);
        }
    }

    protected void initStartupInfo(DebugDaemon.EngineParameters engineParameters, PICLLoadInfo loadInfo)
    {
        loadInfo.setProgramName(engineParameters.getProgramName());
        loadInfo.setProgramParms(engineParameters.getProgramParms());

        String startupBehaviour = engineParameters.getLoadStartupBehaviour();
        if(startupBehaviour != null)
        {
            if(startupBehaviour.equals("runToMain"))
                loadInfo.setStartupBehaviour(PICLLoadInfo.RUN_TO_MAIN);
            else if(startupBehaviour.equals("runToBreakpoint"))
                loadInfo.setStartupBehaviour(PICLLoadInfo.RUN_TO_BREAKPOINT);
            else if(startupBehaviour.equals("debugInitialization"))
                loadInfo.setStartupBehaviour(PICLLoadInfo.DEBUG_INITIALIZATION);
        }
    }
}
