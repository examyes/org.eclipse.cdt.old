package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/MonitorExpressionChangeRep.java, eclipse, eclipse-dev, 20011128
// Version 1.5 (last modified 11/28/01 16:00:32)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.model.MonitoredExpressionTreeNode;
import com.ibm.debug.model.Representation;
import java.io.IOException;


/**
 * Request to change the representation of the expression node
 */

public class MonitorExpressionChangeRep extends MonitorRequest {

	private PICLVariable fPICLVariable = null;
	private Representation fRepresentation = null;

	/**
	 * Constructor for MonitorExpressionChangeRep
	 */
	public MonitorExpressionChangeRep(PICLDebugTarget debugTarget,
									  PICLVariable piclVariable,
									  Representation newRepresentation) {
		super(debugTarget);
		fPICLVariable = piclVariable;
		fRepresentation = newRepresentation;

	}

	/**
	 * @see PICLEngineRequest#execute()
	 */
	public void execute() throws PICLException {

		beginRequest();

		MonitoredExpressionTreeNode treeNode = fPICLVariable.getExpressionTreeNode();

		boolean rc = true;

		try {
			rc = treeNode.changeRepresentation(fRepresentation,syncRequest());

            if (!rc)
 	      		throw new PICLException(PICLUtils.getResourceString(msgKey + "send_error"));
		} catch(IOException ioe) {
			throw(new PICLException(PICLUtils.getResourceString(msgKey + "send_error")));
		} finally {
			endRequest();
		}
	}

}

