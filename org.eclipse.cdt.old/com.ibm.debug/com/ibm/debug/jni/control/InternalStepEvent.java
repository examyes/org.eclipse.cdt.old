package com.ibm.debug.jni.control;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/jni/control/InternalStepEvent.java, eclipse-jni, eclipse-dev, 20011128
// Version 1.4 (last modified 11/28/01 16:38:06)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

/**
 * Internal step event.
 */

public class InternalStepEvent extends InternalDebugEvent {

    /**
     * Used to represent the kind of step event.
     */
    public static final int STEP_INTO = 0;
    public static final int STEP_OVER = 1;
    public static final int STEP_RETURN = 2;

    protected int fKind;

    /**
     * Constructor for InternalStepEvent.
     * @param source The source of the event, for this event the source
     *               should be an IThread.
     * @param kind The kind of step event.
     * @see org.eclipse.debug.core.model.IThread
     * @see #STEP_INTO
     */
    public InternalStepEvent(Object source, int kind) {
        super(source);
        fKind = kind;
    }

    /**
     * @see InternalDebugEvent#fireEvent
     */
    public void fireEvent(IInternalEventListener listener) {
        listener.stepComplete(this);
    }

    /**
     * Return the kind of step event.
     * @return The kind of step event.
     * @see #STEP_INTO
     */
    public int getKind() {
        return fKind;
    }
}
