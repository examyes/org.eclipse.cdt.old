package com.ibm.debug.jni.control;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/jni/control/IInternalDebugTarget.java, eclipse-jni, eclipse-dev, 20011128
// Version 1.4 (last modified 11/28/01 16:38:00)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.debug.core.DebugException;

/**
 * Interface to be implemented by debug targets that want to support
 * internal debuggee control.  With internal control, events are sent
 * only to registered internal listeners and can thus be hidden from
 * the user.
 */
public interface IInternalDebugTarget {

    /**
     * Set an internal entry breakpoint.
     * @param entryPoint Name of the entry point.
     * @param module Name of the module (e.g. class name for Java,
     *               library name for compiled).
     * @return Returns a breakpoint ID which can be used to identify the
     *         breakpoint when it is hit or to clear it later.
     */
    public Object setInternalEntryBreakpoint(String entryPoint, String module) throws DebugException;

    /**
     * Clear an internal entry breakpoint.
     * @param breakpointID The breakpoint to clear.  The breakpointID must be
     *                     one that was returned from a setInternalEntryBreakpoint
     *                     call.
     */
    public void clearInternalEntryBreakpoint(Object breakpointID) throws DebugException;

    /**
     * Request an internal suspend of the debuggee (all threads).
     */
    public void internalSuspend() throws DebugException;

    /**
     * Request an internal resume of the debuggee (all threads).
     */
    public void internalResume() throws DebugException;
}
