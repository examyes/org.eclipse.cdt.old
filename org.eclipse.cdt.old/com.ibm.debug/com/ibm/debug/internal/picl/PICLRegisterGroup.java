package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/PICLRegisterGroup.java, eclipse, eclipse-dev, 20011128
// Version 1.11 (last modified 11/28/01 15:59:38)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.util.Iterator;
import java.util.Vector;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IThread;

import com.ibm.debug.model.MonitoredRegister;
import com.ibm.debug.model.MonitoredRegisterGroup;
import com.ibm.debug.model.RegisterGroup;

/**
 * Represents a register group from the debug engine.   When this group is monitored it will return
 * @see IRegister objects as children of this object
 */

public class PICLRegisterGroup
    extends PICLDebugElement
    implements IRegisterGroup {

	private RegisterGroup fRegisterGroup = null;
	private PICLThread fThread = null;
	private MonitoredRegisterGroup fMonitoredRegisterGroup = null;

    /**
     * Constructor for PICLRegisterGroup
     */
    protected PICLRegisterGroup(IDebugElement parent, PICLThread threadToMonitor, RegisterGroup registerGroup) {
        super(parent, PICLDebugElement.REGISTER_GROUP);
		fThread = threadToMonitor;
        fRegisterGroup = registerGroup;
    }

    /**
     * @see DebugElement#getLabel(boolean)
     */
    public String getLabel(boolean qualified) {
    	return fRegisterGroup.getGroupName();
    }

    /**
     * @see IDebugElement#getThread()
     */
    public IThread getThread()
    {
    	return fThread;
    }

    /**
     * @see PICLDebugElement#doCleanupDetails()
     */
    protected void doCleanupDetails() {}

    /**
     * @see IRegisterGroup#startMonitoringRegisterGroup()
     */
    public IDebugElement[] startMonitoringRegisterGroup() {

		if (fMonitoredRegisterGroup == null) { // make sure we are not already monitoring


			MonitorRegisterGroupRequest request = new MonitorRegisterGroupRequest((PICLDebugTarget)getDebugTarget(),fThread,this);

			try {
				request.execute();
			} catch(PICLException pe) {
				return null;
			}

			if (request.isError())  // error occurred
				return null;

			fMonitoredRegisterGroup = request.getMonitoredRegisterGroup();
			// return the registers that are now being monitored.

			if (fMonitoredRegisterGroup == null)
				return null;

			Vector monitoredRegisters = fMonitoredRegisterGroup.getMonitoredRegisters();

			Iterator iterator = monitoredRegisters.iterator();

			while (iterator.hasNext()) {
				Object o = iterator.next();
				if (o != null)
					addChild(new PICLRegister(this,(MonitoredRegister)o));
			}
		} else
			PICLUtils.logText("Internal warning - request to monitor a register group that is already being monitored");

		try {
			return getChildren();
		} catch(DebugException de) {
			return null;
		}
    }

    /**
     * @see IRegisterGroup#stopMonitoringRegisterGroup()
     */
    public void stopMonitoringRegisterGroup() {

    	if (fMonitoredRegisterGroup != null) {  // make sure we are in fact monitoring
	    	MonitorRegisterGroupEndRequest endRequest = new MonitorRegisterGroupEndRequest((PICLDebugTarget)getDebugTarget(), this);

	    	try {
 		   		endRequest.execute();
   		 	} catch(PICLException pe) {
    			PICLUtils.logText("Error trying to stop monitoring register group");
    			return;
	    	}

	    	removeAllChildren();
	    	fMonitoredRegisterGroup = null;
    	} else
    		PICLUtils.logText("Internal warning - request to stop monitoring when register group is not monitored");

    }

    /**
     * @see IDebugElement#getName()
     */
    public String getName() throws DebugException {
        return "Register group name";
    }

	/**
	 * Gets the registerGroup
	 * @return Returns a RegisterGroup
	 */
	public RegisterGroup getRegisterGroup() {
		return fRegisterGroup;
	}
	/**
	 * Gets the monitoredRegisterGroup
	 * @return Returns a MonitoredRegisterGroup
	 */
	protected MonitoredRegisterGroup getMonitoredRegisterGroup() {
		return fMonitoredRegisterGroup;
	}

	/**
	 * Reset the changed flag in this group's registers
	 */
	public void resetChanged() {

		// loop through this group's registers and reset the changed flags

		if (!hasChildren())
			return;

		IDebugElement registers[] = null;
		try {
			registers = getChildren();
		} catch(DebugException de) {
			return;
		}
		for (int i = 0;i < registers.length; i++)
			((PICLRegister)registers[i]).resetChanged();
	}


    /**
     * Returns true if this register group is currently monitored. False otherwise.
     */
    public boolean isMonitored()
    {
    	if( getMonitoredRegisterGroup() != null)
    		return true;
    	return false;
    }

}

