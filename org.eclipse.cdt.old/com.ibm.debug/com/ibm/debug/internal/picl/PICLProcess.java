package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/PICLProcess.java, eclipse, eclipse-dev, 20011128
// Version 1.5 (last modified 11/28/01 15:59:08)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.model.DebuggeeProcess;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;

public class PICLProcess implements IProcess {

	private DebuggeeProcess fDebuggeeProcess = null;
	private PICLDebugTarget fDebugTarget = null;

    /**
     * Constructor for PICLProcess
     */
    public PICLProcess(DebuggeeProcess debuggeeProcess, PICLDebugTarget debugTarget) {
        super();
        fDebuggeeProcess = debuggeeProcess;
        fDebugTarget = debugTarget;
    }

    /**
     * @see IProcess#getAttribute(String)
     */
    public String getAttribute(String key) {
        return null;
    }

    /**
     * @see IProcess#getLabel()
     */
    public String getLabel() {
        return null;
    }

    /**
     * @see IProcess#getLaunch()
     */
    public ILaunch getLaunch() {
        return fDebugTarget.getLaunch();
    }

    /**
     * @see IProcess#getStreamsProxy()
     */
    public IStreamsProxy getStreamsProxy() {
        return null;
    }

    /**
     * @see IProcess#setAttribute(String, String)
     */
    public void setAttribute(String key, String value) {
    }

    /**
     * @see IAdaptable#getAdapter(Class)
     */
    public Object getAdapter(Class adapter) {
        return null;
    }

    /**
     * @see ITerminate#canTerminate()
     */
    public boolean canTerminate() {
        return true;
    }

    /**
     * @see ITerminate#isTerminated()
     */
    public boolean isTerminated() {
        return fDebugTarget.isTerminated();
    }

    /**
     * @see ITerminate#terminate()
     */
    public void terminate() throws DebugException {
    	fDebugTarget.terminate();
    }

	/**
	 * Gets the debuggeeProcess
	 * @return Returns a DebuggeeProcess
	 */
	public DebuggeeProcess getDebuggeeProcess() {
		return fDebuggeeProcess;
	}
}

