package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/PICLMonitorParent.java, eclipse, eclipse-dev, 20011128
// Version 1.9 (last modified 11/28/01 16:00:11)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.model.MonitoredExpression;
import com.ibm.debug.model.MonitoredExpressionChangedEvent;
import com.ibm.debug.model.MonitoredExpressionEndedEvent;
import com.ibm.debug.model.MonitoredExpressionEventListener;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;


public class PICLMonitorParent extends PICLDebugElement {
	Object[] fExpandedElements = null;


	/**
	 * Constructor for PICLMonitorParent
	 */
	public PICLMonitorParent(IDebugElement parent) {
		super(parent, PICLDebugElement.MONITOR_PARENT);
	}


	/**
	 * Add a monitored expression to this parent
	 * @param The monitored expression
	 */
	public void addMonitoredExpression(MonitoredExpression expression) {
		addChild(new PICLVariable(this,expression));
	}


	/**
	 * @see PICLDebugElement#doCleanupDetails()
	 */
	protected void doCleanupDetails() {
	}


	/**
	 * This label is not displayed.
	 * @see PICLDebugElement#getLabel(boolean)
	 */
	public String getLabel(boolean qualified) {
		return "PICLMonitorParent label";
	}


	/**
	 * @see IDebugElement#getName()
	 */
	public String getName() throws DebugException {
		return "PICLMonitorParent name";
	}


    /**
     * Stores the current expanded state of its child tree.
     * Used to restore tree to current state next time selection returns
     * to current selected thread in debug view.
     *
     * @see getExpandedElements()
     */
    public void setExpandedElements(Object[] state){
    	fExpandedElements = state;
    }

    /**
     * Returns the current expanded state of its child tree.
     * Used to restore tree to current state when selection returns
     * to current selected thread in debug view.
     *
     * @see setExpandedElements()
     */
    public Object[] getExpandedElements(){
		return fExpandedElements;
	}

	/**
	 * Reset all of the monitors owned by this parent
	 */
	public void resetChanged() {
		// loop through all of the monitors and reset the changed flags

		if (!hasChildren())
			return;

		IDebugElement monitors[] = null;

		try {
			monitors = getChildrenNoExpand();
		} catch(DebugException de) {
			return;
		}
		for (int i = 0;i < monitors.length; i++)
			((PICLVariable)monitors[i]).resetChanged();

	}

}
