package com.ibm.debug.jni.control;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/jni/control/InternalSuspendEvent.java, eclipse-jni, eclipse-dev, 20011128
// Version 1.4 (last modified 11/28/01 16:38:07)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

/**
 * Internal suspend event.
 */

public class InternalSuspendEvent extends InternalDebugEvent {

    /**
     * Constructor for InternalSuspendEvent.
     * @param source The sourcce of the event, for this event the source
     *               should be an IDebugTarget.
     * @see org.eclipse.debug.core.model.IDebugTarget
     */
    public InternalSuspendEvent(Object source) {
        super(source);
    }

    /**
     * @see InternalDebugEvent#fireEvent
     */
    public void fireEvent(IInternalEventListener listener) {
        listener.targetSuspended(this);
    }
}
