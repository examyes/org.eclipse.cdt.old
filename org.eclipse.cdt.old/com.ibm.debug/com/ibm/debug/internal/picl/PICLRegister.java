package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/PICLRegister.java, eclipse, eclipse-dev, 20011128
// Version 1.17 (last modified 11/28/01 15:59:37)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.IDebugStatusConstants;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;

import com.ibm.debug.model.MonitoredRegister;
import com.ibm.debug.model.MonitoredRegisterChangedEvent;
import com.ibm.debug.model.MonitoredRegisterEndedEvent;
import com.ibm.debug.model.MonitoredRegisterEventListener;

/**
 * Represents a register on the debug engine.  The name/content and grouping of registers are
 * controlled by the debug engine
 */
public class PICLRegister extends PICLDebugElement implements IRegister, MonitoredRegisterEventListener {

	private MonitoredRegister fMonitoredRegister = null;
	private PICLRegisterValue fRegisterValue = null;
	private boolean fHasChanged = false;
    /**
     * Constructor for PICLRegister
     */
    protected PICLRegister(IDebugElement parent, MonitoredRegister monitoredRegister) {
        super(parent, PICLDebugElement.REGISTER);
        fMonitoredRegister = monitoredRegister;
        fMonitoredRegister.addEventListener(this);

    }

    /**
     * @see DebugElement#getLabel(boolean)
     */
    public String getLabel(boolean qualified) {
    	try {
        	return fMonitoredRegister.getName() + " = " + getValue().getValueString();
    	} catch(DebugException de) {
    		return PICLUtils.getResourceString("picl_register.no_label");
    	}

    }

    /**
     * @see PICLDebugElement#doCleanupDetails()
     */
    protected void doCleanupDetails() {

    	// remove as an event listener.
    	if (fMonitoredRegister != null)
    		fMonitoredRegister.removeEventListener(this);

    }

    /**
     * @see IRegister#hasChanged()
     */
    public boolean hasChanged(boolean reset) {

    	if (reset) {
    		boolean currentValue = fHasChanged;
    		fHasChanged = false;
    		return currentValue;
    	} else
    		return fHasChanged;
    }

	/**
	 * Reset the changed flag
	 */
	public void resetChanged() {
		fHasChanged = false;
	}

    /**
     * @see IVariable#getReferenceTypeName()
     */
    public String getReferenceTypeName() throws DebugException {
        return "";
    }

    /**
     * @see IVariable#getValue()
     */
    public IValue getValue() throws DebugException {
    	if (fRegisterValue == null)
	    	fRegisterValue = new PICLRegisterValue(this);
    	return fRegisterValue;
    }

    /**
     * @see IDebugElement#getName()
     */
    public String getName() throws DebugException {
        return fMonitoredRegister.getName();
    }

    /**
     * @see IValueModification#setValue(String)
     */
    public void setValue(String expression) throws DebugException {

    	ModifyRegisterValue modifyRequest = new ModifyRegisterValue((PICLDebugTarget)getDebugTarget(), this, expression);
    	try {
    		modifyRequest.execute();
    	} catch(PICLException pe) {
			throw new DebugException( new Status(IStatus.ERROR, "com.ibm.debug.picl", IDebugStatusConstants.REQUEST_FAILED, pe.getMessage(), null));
    	}

    }

    /**
     * @see IValueModification#supportsValueModification()
     */
    public boolean supportsValueModification() {
    	return fMonitoredRegister.supportsModifying();
    }

    /**
     * @see IValueModification#verifyValue(String)
     * Register changes are verified by the engine so always return true.
     */
    public boolean verifyValue(String expression) throws DebugException {
        return true;
    }

	/**
	 * Gets the monitoredRegister
	 * @return Returns a MonitoredRegister
	 */
	protected MonitoredRegister getMonitoredRegister() {
		return fMonitoredRegister;
	}
    /**
     * @see MonitoredRegisterEventListener#monitoredRegisterEnded(MonitoredRegisterEndedEvent)
     */
    public void monitoredRegisterEnded(MonitoredRegisterEndedEvent event) {
    	PICLUtils.logEvent("Register " + event.getMonitoredRegister().getName() + " has been removed", this);
    	event.getMonitoredRegister().removeEventListener(this);

    }

    /**
     * @see MonitoredRegisterEventListener#monitoredRegisterChanged(MonitoredRegisterChangedEvent)
     */
    public void monitoredRegisterChanged(MonitoredRegisterChangedEvent event) {
    	PICLUtils.logEvent("Register " + event.getMonitoredRegister().getName() + " has changed", this);

    	// set flag to indicate that the register's value has changed
		fHasChanged = true;
		fireChangeEvent();

    }

    /**
     * @see IDebugElement#getThread()
     */
    public IThread getThread()
    {
    	return getParent().getThread();
    }

}

