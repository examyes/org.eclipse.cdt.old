/*
 * Copyright (c) 1995, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.commands;

import org.eclipse.cdt.debug.gdbPicl.DebugSession;
import org.eclipse.cdt.debug.gdbPicl.GdbDebugSession;
import org.eclipse.cdt.debug.gdbPicl.Gdb;

import com.ibm.debug.epdc.EPDC_EngineSession;
import com.ibm.debug.epdc.ERepProgramInput;
import com.ibm.debug.epdc.EReqProgramInput;

public class CmdProgramInput extends Command {

	/**
	 * Constructor for CmdProgramInput.
	 * @param debugSession
	 */
	public CmdProgramInput(DebugSession debugSession, EReqProgramInput req) {
		super(debugSession);
		
		_req = req;
	}

	/**
	 * @see Command#execute(EPDC_EngineSession)
	 */
	public boolean execute(EPDC_EngineSession EPDCSession) {
		_rep = new ERepProgramInput();
		
		String input = _req.getString();
		
		if (Gdb.traceLogger.DBG)
			Gdb.traceLogger.dbg(1,"Program input is: " + input);
		
		if (((GdbDebugSession)_debugSession).getGdbProcess().isReady())
		{
			if (Gdb.traceLogger.DBG)
                Gdb.traceLogger.dbg(1,"GDB in control, execute GDB Command" );
		((GdbDebugSession)_debugSession).executeGdbCommand(input);

		}
		else
		{
			if (Gdb.traceLogger.DBG)
                Gdb.traceLogger.dbg(1,"Program waiting for input, just pass input to process" );
			((GdbDebugSession)_debugSession).getGdbProcess().writeLine(input);
		}
		
		return false;
	}
	
   // Data fields
   private EReqProgramInput _req;

}
