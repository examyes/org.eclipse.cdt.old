package com.ibm.debug.jni.control;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/jni/control/IInternalEventListener.java, eclipse-jni, eclipse-dev, 20011128
// Version 1.4 (last modified 11/28/01 16:38:01)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

/**
 * Internal event listener.  Only registered internal event listeners
 * will be notified of internal events.
 */

public interface IInternalEventListener {

    /**
     * An internal breakpoint was hit.
     * @param event The breakpoint event.
     * @see InternalBreakpointEvent
     */
    public void breakpointHit(InternalBreakpointEvent event);

    /**
     * An internal step request is complete.
     * @param event The step event.
     * @see InternalStepEvent
     */
    public void stepComplete(InternalStepEvent event);

    /**
     * An internal resume request is complete.
     * @param event The resume event.
     * @see InternalResumeEvent
     */
    public void targetResumed(InternalResumeEvent event);

    /**
     * An internal suspend request is complete.
     * @param event The suspend event.
     * @see InternalSuspendEvent
     */
    public void targetSuspended(InternalSuspendEvent event);
}
