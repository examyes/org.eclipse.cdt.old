package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/EntryBreakpointModifyRequest.java, eclipse, eclipse-dev, 20011128
// Version 1.6 (last modified 11/28/01 16:00:43)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.IOException;
import java.util.Vector;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;

import com.ibm.debug.model.Breakpoint;
import com.ibm.debug.model.EntryBreakpoint;
import com.ibm.debug.model.Function;
import com.ibm.debug.model.Part;
import com.ibm.debug.model.ViewFile;

public class EntryBreakpointModifyRequest extends BreakpointModifyRequest {

	private IMarker fMarker = null;
	private IMarkerDelta fDelta = null;
	/**
	 * Constructor for LineBreakpointModifyRequest
	 */

	public EntryBreakpointModifyRequest(PICLDebugTarget debugTarget,
										IMarker marker,
										IMarkerDelta delta,
										Breakpoint breakpoint) {

		super(debugTarget, breakpoint);
		fDelta = delta;
		fMarker = marker;
	}


	/**
	 * @see PICLEngineRequest#execute()
	 */
	public void execute() throws PICLException {

		beginRequest();

    	boolean rc = true;

    	String threadStr = null;

    	// first get all of the attributes from the breakpoint


    	try {
    		String[] attributes = {IPICLDebugConstants.MODULE_NAME,
   									IPICLDebugConstants.OBJECT_NAME,
   									IPICLDebugConstants.EVERY_VALUE,
   									IPICLDebugConstants.FROM_VALUE,
   									IPICLDebugConstants.TO_VALUE,
   									IPICLDebugConstants.CONDITIONAL_EXPRESSION,
   									IPICLDebugConstants.THREAD,
   									IPICLDebugConstants.FUNCTION_NAME,
   									IPICLDebugConstants.CASESENSITIVE };

    		Object[] values = fMarker.getAttributes(attributes);

    		int threadID = 0;
    		// check the thread value, if "Every" then make it 0
    		if (((String)values[6]).equalsIgnoreCase("every"))
    			threadID = 0;
    		else
    			threadID = Integer.parseInt((String)values[6]);

    		boolean caseSearch = ((Boolean)values[8]).booleanValue();

    		if (fBreakpoint.isDeferred()) {
 				rc = ((EntryBreakpoint)fBreakpoint).modify((String)values[7],
 														  (String)values[0],
 														  (String)values[1],
 														  ((Integer)values[2]).intValue(),
 														  ((Integer)values[3]).intValue(),
 														  ((Integer)values[4]).intValue(),
 														  (String)values[5],
 														  threadID,
 														  syncRequest(),
														  fMarker);
    		} else {

    			// compare the function from the existing breakpoint to see if a new function is required.

    			EntryBreakpoint bkp = (EntryBreakpoint)fBreakpoint;
    			String newFunction = (String)values[7];
    			Function fcn = null;

    			if (bkp.getFunctionName().equals(newFunction)) {
    				fcn = bkp.getFunction();   // no change to function so use existing
    			} else {
					ViewFile viewFile = getDebugTarget().getViewFile(fMarker,getDebugTarget().getDebugEngine().getSourceViewInformation());
					if (viewFile == null)
						throw(new PICLException(PICLUtils.getResourceString(msgKey + "file_error")));

    		    	Part part= viewFile.view().part();

		        	Vector functions = null;
  			      	try {
     			   		functions = part.getFunctions(newFunction,caseSearch);
        			} catch(IOException ioe) {
						throw(new PICLException(PICLUtils.getResourceString(msgKey + "function_error")));
        			}

        			if (functions == null)
						throw(new PICLException(PICLUtils.getResourceString(msgKey + "function_not_found")));

        			fcn = (Function)functions.firstElement();

        			if (fcn == null)
						throw(new PICLException(PICLUtils.getResourceString(msgKey + "function_not_found")));
    			}

	    		rc = ((EntryBreakpoint)fBreakpoint).modify(fcn,
															((Integer)values[2]).intValue(),
															((Integer)values[3]).intValue(),
															((Integer)values[4]).intValue(),
  	  												  		(String)values[5],
    												  		threadID,
    												  		syncRequest(),
															fMarker);

                if (!rc)
 		      		throw new PICLException(PICLUtils.getResourceString(msgKey + "entry_error"));



    		}
    	} catch(IOException ioe) {
			throw(new PICLException(PICLUtils.getResourceString(msgKey + "send_error")));
    	} catch(CoreException ce) {
			throw(new PICLException(PICLUtils.getResourceString(msgKey + "modify_error")));
    	} finally {
    		endRequest();
    	}


	}

}

