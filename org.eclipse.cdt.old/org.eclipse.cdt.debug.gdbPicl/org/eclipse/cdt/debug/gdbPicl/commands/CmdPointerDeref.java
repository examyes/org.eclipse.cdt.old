/*
 * Copyright (c) 1995, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.commands;

import org.eclipse.cdt.debug.gdbPicl.DebugSession;
import org.eclipse.cdt.debug.gdbPicl.GdbVariableMonitorManager;

import com.ibm.debug.epdc.EPDC;
import com.ibm.debug.epdc.EPDC_EngineSession;
import com.ibm.debug.epdc.ERepPointerDeref;
import com.ibm.debug.epdc.EReqPartSet;
import com.ibm.debug.epdc.EReqPointerDeref;

public class CmdPointerDeref extends Command {

	/**
	 * Constructor for CmdPointerDeref.
	 * @param debugSession
	 */
	public CmdPointerDeref(DebugSession debugSession, EReqPointerDeref req) {
		super(debugSession);
		_req = req;
	}

	/**
	 * @see Command#execute(EPDC_EngineSession)
	 */
	public boolean execute(EPDC_EngineSession EPDCSession) {

        _rep = new ERepPointerDeref();
        boolean ok = ((GdbVariableMonitorManager)_debugSession.getVariableMonitorManager()).dereference(_req.exprID(), _req.exprTreeNode());
        
        if (!ok)
        {
        	_rep.setMessage("Failed to Dereference Pointer.");
        	_rep.setReturnCode(EPDC.ExecRc_Error);
        }
        
		return false;
	}
	
   // Data fields
   private EReqPointerDeref _req;

}
