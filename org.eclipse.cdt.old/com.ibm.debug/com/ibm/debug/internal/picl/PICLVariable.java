package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/PICLVariable.java, eclipse, eclipse-dev, 20011129
// Version 1.23 (last modified 11/29/01 14:15:47)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.IDebugStatusConstants;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IValueModification;
import org.eclipse.debug.core.model.IVariable;

import com.ibm.debug.epdc.EPDC;
import com.ibm.debug.model.AggregateMonitoredExpressionTreeNode;
import com.ibm.debug.model.DebugEngine;
import com.ibm.debug.model.MonitoredExpression;
import com.ibm.debug.model.MonitoredExpressionChangedEvent;
import com.ibm.debug.model.MonitoredExpressionEndedEvent;
import com.ibm.debug.model.MonitoredExpressionEventListener;
import com.ibm.debug.model.MonitoredExpressionTreeNode;
import com.ibm.debug.model.PointerMonitoredExpressionTreeNode;
import com.ibm.debug.model.Representation;
import com.ibm.debug.model.ScalarMonitoredExpressionTreeNode;

public class PICLVariable extends PICLDebugElement implements IVariable, IValueModification, MonitoredExpressionEventListener {

	// Resource String keys
	private static final String PREFIX= "picl_variable.";
	private static final String ERROR= PREFIX + "error.";
	private static final String CLEANED_UP= ERROR + "cleaned_up";
	private static final String CHILDREN_REQUEST_FAILED= ERROR + "children_request_failed";
	private static final String NO_VAR_MOD= ERROR + "no_var_mod";
	private static final String VARIABLE_NOT_AVAILABLE= ERROR + "variable_not_available";

	/**
	 * Constant for the maximum number of children nodes that will be
	 * retrieved for a variable.
	 */
	private final static int MAX_NUM_CHILDREN= 500;
	/**
	 * The IBM debug model expression node that this variable is a proxy for
	 */
	protected MonitoredExpressionTreeNode fExpressionNode = null;
	private MonitoredExpression fMonitoredExpression = null;

	private PICLValue fvalue;
	private boolean fHasChanged = false;

	/**
	 * Creates a PICL variable.
	 * This is used in 2 ways:
	 * 1) The root PICLVariable that has PICLMonitorParent as its parent used when monitoring variables
	 * 2) The root PICLVariable that has a PICLStackFrame as its parent used for local variables
	 */
	public PICLVariable(IDebugElement parent, MonitoredExpression monitoredExpression) {
		super(parent, IDebugElement.VARIABLE);
		fMonitoredExpression = monitoredExpression;
		fExpressionNode= monitoredExpression.getValue();
		fMonitoredExpression.addEventListener(this);
	}

	/**
	 * Creates a PICL variable that is a child of another <code>PICLVariable</code>
	 */
	protected PICLVariable(IDebugElement parent, MonitoredExpressionTreeNode monitoredExpressionTreeNode) {
		super(parent, IDebugElement.VARIABLE);
		fExpressionNode= monitoredExpressionTreeNode;
	}

	protected MonitoredExpressionTreeNode getExpressionTreeNode() {
		return fExpressionNode;
	}

	/**
	 * A request to get the children of an aggregate variable
	 */
	class ChildrenRequest extends AbstractPICLRequest {

		public ChildrenRequest() {
			super(IPICLRequest.ASYNCHRONOUS);
		}
		/**
		 * @see IPICLRequest
		 */
		public boolean performRequest() throws IOException {
			if (haveDoneCleanup()) {
				throw new IOException(PICLUtils.getResourceString(CLEANED_UP));
			}
			AggregateMonitoredExpressionTreeNode aggregateNode= (AggregateMonitoredExpressionTreeNode) fExpressionNode;
			return aggregateNode.expand(1, Math.min(aggregateNode.getNumberOfChildren(), MAX_NUM_CHILDREN), DebugEngine.sendReceiveDefault);
		}
		/**
		 * @see IPICLRequest
		 */
		public String getErrorMessage() {
			return PICLUtils.getResourceString(CHILDREN_REQUEST_FAILED);
		}
	}


	/**
	 * Returns any child nodes
	 */
	public IDebugElement[] getChildren() {
		if (haveDoneCleanup()) {
			return null;
		}
		if (fChildren == fgEmptyChildren) {
			if (fExpressionNode instanceof AggregateMonitoredExpressionTreeNode) {
				AggregateMonitoredExpressionTreeNode aggregateNode= (AggregateMonitoredExpressionTreeNode) fExpressionNode;
				if (!getDebugEngine().isAcceptingSynchronousRequests()) {
					return null;
				}
				try {
					if (!aggregateNode.expand(1, Math.min(aggregateNode.getNumberOfChildren(), MAX_NUM_CHILDREN), DebugEngine.sendReceiveSynchronously)) {
						return null;
					}
				} catch (IOException e) {
					return null;
				}

				/* Causes timing problems.
				try {
				    //make request
				    performRequest(new ChildrenRequest());
				} catch (DebugException de) {
				    return fgEmptyChildren;
				}
				*/
				//Our node has now changed to be one which has children
				MonitoredExpressionTreeNode[] nodes= ((AggregateMonitoredExpressionTreeNode) fExpressionNode).getChildren();
				if (nodes != null) {
					for (int i= 0; i < nodes.length; i++) {
						if (nodes[i] != null) {
							addChild(new PICLVariable(this, nodes[i]));
						}
					}
				}
			}
		}
		Object obj[] = fChildren.toArray();
		IDebugElement dbg[] = new IDebugElement[obj.length];
		for (int j=0; j<obj.length; j++) {
			dbg[j] = (IDebugElement)obj[j];
		}
		return dbg;
		//return (IDebugElement[])fChildren.toArray();
	}

	/**
	 * @see PICLDebugElement#getChildrenNoExpand()
	 */
	public IDebugElement[] getChildrenNoExpand() throws DebugException {
		return super.getChildren();
	}


	/**
	 * Delete this monitored expression
	 * NOTE: this will only work if it is the parent.   It is not possible to delete a child node
	 * @return will return false if unable to delete because this is a child and not the root node
	 */
	public boolean delete() {

		if (fMonitoredExpression == null)   // only valid if this has a monitored expression
			return false;

		MonitorExpressionDeleteRequest request = new MonitorExpressionDeleteRequest((PICLDebugTarget)getDebugTarget(),
																					this);
		try {
			request.execute();
		} catch(PICLException pe) {
			return false;
		}

		return true;
	}

	/**
	 * Enable this monitored expression
	 * @return false if this is not the root node
	 */
	public boolean enable() {
		if (fMonitoredExpression == null)   // only valid if this has a monitored expression
			return false;

		MonitorExpressionEnableRequest request = new MonitorExpressionEnableRequest((PICLDebugTarget)getDebugTarget(),
																					this);
		try {
			request.execute();
		} catch(PICLException pe) {
			return false;
		}

		return true;
	}

	/**
	 * Disable this monitored expression
	 * @return false if this is not the root node
	 */
	public boolean disable() {
		if (fMonitoredExpression == null)   // only valid if this has a monitored expression
			return false;

		MonitorExpressionDisableRequest request = new MonitorExpressionDisableRequest((PICLDebugTarget)getDebugTarget(),
																					this);
		try {
			request.execute();
		} catch(PICLException pe) {
			return false;
		}

		return true;
	}

	/**
	 * Returns the current representation for this variable
	 * @return @see com.ibm.debug.model.Representation
	 */
	public Representation getCurrentRepresentation() {
		if (fExpressionNode == null)
			return null;
		else
			return fExpressionNode.getCurrentRepresentation();
	}

	/**
	 * Returns the array of possible representations for this variable
	 * @return Array of @see com.ibm.debug.model.Representation
	 */
	public Representation[] getArrayOfRepresentations() {
		if (fExpressionNode == null)
			return null;
		else
			return fExpressionNode.getArrayOfRepresentations();
	}

	/**
	 * Sets the current representation for this variable
	 * It must be in the array of representations returned by @see getArrayOfRepresentations()
	 * @return success or fail
	 */
	public boolean changeRepresentation(Representation rep) {
		if (fExpressionNode == null)   // only valid if this has a monitored expression tree node
			return false;

		MonitorExpressionChangeRep request = new MonitorExpressionChangeRep((PICLDebugTarget)getDebugTarget(),
																			this,
																			rep);
		try {
			request.execute();
		} catch(PICLException pe) {
			return false;
		}

		return true;
	}

	/**
	 * @see IDebugElement
	 */
	public String getName() {
		if (haveDoneCleanup()) {
			return "";
		}
		return fExpressionNode.getName();
	}

	/**
	 * @see IVariable
	 */
	public String getReferenceTypeName() {
		if (haveDoneCleanup()) {
			return "";
		}
		String type = fExpressionNode.getType();
		if (type == null) {
			type = "";
		}
		return type;
	}

	/**
	 * @see IVariable
	 */
	public IValue getValue() {

//		if (haveDoneCleanup())
//			return null;

		if (fvalue == null)
			fvalue = new PICLValue(this);

		return fvalue;
	}

	/**
	 * @see IDebugElement
	 */
	public boolean hasChildren() {
		if (haveDoneCleanup()) {
			return false;
		}
		if (fExpressionNode instanceof AggregateMonitoredExpressionTreeNode) {
			return ((AggregateMonitoredExpressionTreeNode) fExpressionNode).getNumberOfChildren() > 0;
		}
		return super.hasChildren();
	}

	/**
	 * @see com.ibm.debug.model.MonitoredExpressionEventListener
	 */
	public void monitoredExpressionChanged(MonitoredExpressionChangedEvent event) {
		PICLUtils.logEvent("monitor expression changed",this);

		//In order to update we need to get a new node as the value of nodes never changes
		//(it is set in their constructor)
		replaceNode(event.getMonitoredExpression().getValue());
		fireChangeEvent();
		fHasChanged = true;
	}

	/**
	 * @see com.ibm.debug.model.MonitoredExpressionEventListener
	 */
	public void monitoredExpressionEnded(MonitoredExpressionEndedEvent event) {
		PICLUtils.logEvent("monitor expression ended",this);
		((PICLDebugElement)getParent()).removeChild(this);

	}

	/**
	 * Replaces our node with the new one (and tells all of our children to do the same)
	 */
	protected void replaceNode(MonitoredExpressionTreeNode newNode) {
		// Need to attempt an optimization?
		fExpressionNode = newNode;
		if (newNode instanceof AggregateMonitoredExpressionTreeNode) {
			MonitoredExpressionTreeNode[] nodes =
				((AggregateMonitoredExpressionTreeNode) newNode).getChildren();
			if (nodes != null) {
				for (int i = 0; i < fChildren.size(); i++) {
					if (nodes[i] != null) {
						if (fChildren.isEmpty()) {
							continue;
						}
						((PICLVariable) fChildren.get(i)).replaceNode(nodes[i]);
					}
				}
				for (int i = fChildren.size(); i < nodes.length; i++) {
					if (nodes[i] != null) {
						if (fChildren.isEmpty()) {
							continue;
						}
						addChild(new PICLVariable(this, nodes[i]));
					}
				}
			}
		}
	}

	/**
	 * @see PICLDebugElement
	 */
	 protected void doCleanupDetails() {

		if (fMonitoredExpression != null)
			fMonitoredExpression.removeEventListener(this);

	 	fExpressionNode= null;
	 	fMonitoredExpression = null;
	 }

	/**
	 * @see IVariable
	 */
	public boolean isAllocated() {
		return fExpressionNode != null;
	}

	/**
	 * @see IVariableModification
	 */
	public void setValue(String newValue) throws DebugException {

		if (fExpressionNode == null)
			throw new DebugException(new Status(IStatus.ERROR,
												"com.ibm.debug",
												IDebugStatusConstants.NOT_SUPPORTED,
												PICLUtils.getResourceString(NO_VAR_MOD),
												null));

		MonitorExpressionChangeValue request = new MonitorExpressionChangeValue((PICLDebugTarget)getDebugTarget(),
																					this,
																				 newValue);
		try {
			request.execute();
		} catch(PICLException pe) {
			throw new DebugException(new Status(IStatus.ERROR,
												"com.ibm.debug",
												IDebugStatusConstants.REQUEST_FAILED,request.getErrorMessage(),
												null));
		}


	}
	/**
	 * @see IVariableModification
	 */
	public boolean supportsValueModification() {
		return !hasChildren();
	}
	/**
	 * @see IVariableModification
	 */
	public boolean verifyValue(String newValue) {
		return true;
	}

	/**
	 * @see IVariableModification
	 */
	public String getWarningMessage() {
		return "";
	}
	/**
	 * @see DebugElement#getLabel(boolean)
	 */
	public String getLabel(boolean qualified) {
		if (fExpressionNode == null)
			return PICLUtils.getResourceString(VARIABLE_NOT_AVAILABLE);
		else
			return getName();
	}

	public String getLabel(boolean qualified, boolean typed) {
		if (fExpressionNode == null)
			return PICLUtils.getResourceString(VARIABLE_NOT_AVAILABLE);
		else if (typed)
			return getReferenceTypeName() + "  " + getName();
		else
			return getName();

	}

	/**
	 * Gets the monitoredExpression
	 * @return Returns a MonitoredExpression
	 */
	protected MonitoredExpression getMonitoredExpression() {
		return fMonitoredExpression;
	}

	/**
	 * Returns the current enable/disable state of this variable
	 * @return true if enabled
	 */
	public boolean isEnabled() {
		if (fMonitoredExpression == null)
			return true;
		else
			return fMonitoredExpression.isEnabled();
	}

	/**
	 * Returns the current state of "has changed"
	 * @param reset the changed flag - true will reset it
	 * @return true if variable has changed since last reset
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



}
