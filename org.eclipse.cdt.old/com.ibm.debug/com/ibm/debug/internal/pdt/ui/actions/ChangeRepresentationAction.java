package com.ibm.debug.internal.pdt.ui.actions;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/actions/ChangeRepresentationAction.java, eclipse, eclipse-dev, 20011128
// Version 1.6 (last modified 11/28/01 16:00:24)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.internal.picl.PICLUtils;
import com.ibm.debug.internal.picl.PICLVariable;
import com.ibm.debug.model.Representation;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.help.WorkbenchHelp;

public class ChangeRepresentationAction extends Action {
	protected static final String PREFIX= "ChangeRepresentationAction.";
	private PICLVariable fVar;
	private Representation fRep;

	public ChangeRepresentationAction(PICLVariable var, Representation rep) {
		super(rep.name());
		fVar = var;
		fRep = rep;
		setToolTipText(PICLUtils.getResourceString(PREFIX+"tooltip"));
		WorkbenchHelp.setHelp(this, new Object[] { PICLUtils.getHelpResourceString("ChangeRepresentationAction") });
	}


	/**
	 * @see Action#run()
	 */
	public void run() {
		fVar.changeRepresentation(fRep);
	}

}
