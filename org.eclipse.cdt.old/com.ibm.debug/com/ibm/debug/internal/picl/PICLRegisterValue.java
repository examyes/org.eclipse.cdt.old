package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/PICLRegisterValue.java, eclipse, eclipse-dev, 20011128
// Version 1.4 (last modified 11/28/01 15:59:47)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;

import com.ibm.debug.model.MonitoredRegister;

/**
 * This is the value for a @see PICLRegister
 */

public class PICLRegisterValue implements IValue {

	private PICLRegister fRegister = null;
	private MonitoredRegister fMonitoredRegister = null;

    /**
     * Constructor for PICLRegisterValue
     */
    public PICLRegisterValue(PICLRegister register) {
        super();
        fRegister = register;
        fMonitoredRegister = fRegister.getMonitoredRegister();
    }

    /**
     * @see IValue#getName()
     */
    public String getName() {
    	try {
        	return fRegister.getName();
    	} catch(Exception e) {
    		return "";
    	}
    }

    /**
     * @see IValue#getParent()
     */
    public IDebugElement getParent() {
        return null;
    }

    /**
     * @see IValue#getReferenceTypeName()
     */
    public String getReferenceTypeName() throws DebugException {
        return "";
    }

    /**
     * @see IValue#getValueString()
     */
    public String getValueString() throws DebugException {
        return fMonitoredRegister.getValue();
    }

    /**
     * @see IValue#getVariable()
     */
    public IVariable getVariable() {
        return fRegister;
    }

    /**
     * @see IValue#isAllocated()
     * Registers are always allocated.
     */
    public boolean isAllocated() throws DebugException {
        return true;
    }

    /**
     * @see IDebugElement#getChildren()
     * Registers don't have children
     */
    public IDebugElement[] getChildren() throws DebugException {
        return null;
    }

    /**
     * @see IDebugElement#getDebugTarget()
     */
    public IDebugTarget getDebugTarget() {
        return null;
    }

    /**
     * @see IDebugElement#getElementType()
     */
    public int getElementType() {
        return 0;
    }

    /**
     * @see IDebugElement#getLaunch()
     */
    public ILaunch getLaunch() {
        return null;
    }

    /**
     * @see IDebugElement#getModelIdentifier()
     */
    public String getModelIdentifier() {
        return null;
    }

    /**
     * @see IDebugElement#getProcess()
     */
    public IProcess getProcess() {
        return null;
    }

    /**
     * @see IDebugElement#getSourceLocator()
     */
    public ISourceLocator getSourceLocator() {
        return null;
    }

    /**
     * @see IDebugElement#getStackFrame()
     */
    public IStackFrame getStackFrame() {
        return null;
    }

    /**
     * @see IDebugElement#getThread()
     */
    public IThread getThread() {
        return null;
    }

    /**
     * @see IDebugElement#hasChildren()
     */
    public boolean hasChildren() throws DebugException {
        return false;
    }

    /**
     * @see IAdaptable#getAdapter(Class)
     */
    public Object getAdapter(Class adapter) {
        return null;
    }

}

