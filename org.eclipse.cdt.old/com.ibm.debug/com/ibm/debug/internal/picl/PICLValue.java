package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/PICLValue.java, eclipse, eclipse-dev, 20011128
// Version 1.7 (last modified 11/28/01 15:58:05)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;

import com.ibm.debug.model.MonitoredExpressionTreeNode;
import com.ibm.debug.model.PointerMonitoredExpressionTreeNode;
import com.ibm.debug.model.ScalarMonitoredExpressionTreeNode;


public class PICLValue extends PICLDebugElement implements IValue {

	private static final String PREFIX= "picl_value.";
	private static final String ERROR= PREFIX + "error.";
	private static final String NO_VALUE_AVAILABLE = ERROR + "no_value_available";
	private static final String VALUE_TYPE_UNKNOWN = ERROR + "value_type_unknown";



	private PICLVariable fvar;

	protected PICLValue(PICLVariable var) {
		super(null,0);
		fvar = var;
	}

	public boolean hasChildren() {
		return fvar.hasChildren();
	}
	public IDebugElement[] getChildren() {
		return fvar.getChildren();
	}

	/**
	 * @see IValue#isAllocated()
	 */
	public boolean isAllocated() throws DebugException {
		return false;
	}

	/**
	 * @see IValue#getVariable()
	 */
	public IVariable getVariable() {
		return fvar;
	}

	/**
	 * @see IValue#getName()
	 */
	public String getName() {
		//String name = fvar.getExpressionTreeNode().getName();
		String name = getNodeValue(fvar.getExpressionTreeNode());
		return name;
	}

	/**
	 * @see IValue#getValueString()
	 */
	public String getValueString() throws DebugException {
		String value = getNodeValue(fvar.getExpressionTreeNode());
		return value;
	}

	/**
	 * @see IValue#getReferenceTypeName()
	 */
	public String getReferenceTypeName() throws DebugException {
		return null;
	}

	/**
	 * Returns the value of the <code>MonitoredExpressionTreeNode</code>
	 */
	protected String getNodeValue(MonitoredExpressionTreeNode treeNode) {
		if (treeNode instanceof ScalarMonitoredExpressionTreeNode) {
			return ((ScalarMonitoredExpressionTreeNode) treeNode).getValue();
		}
		if (treeNode instanceof PointerMonitoredExpressionTreeNode) {
			return ((PointerMonitoredExpressionTreeNode) treeNode).getValue();
		}
		return PICLUtils.getResourceString(VALUE_TYPE_UNKNOWN);
	}


	/**
	 * @see DebugElement#getLabel(boolean)
	 */
	public String getLabel(boolean qualified) {
		return getName();
	}

    /**
     * @see PICLDebugElement#doCleanupDetails()
     */
    protected void doCleanupDetails() {
    }

}
