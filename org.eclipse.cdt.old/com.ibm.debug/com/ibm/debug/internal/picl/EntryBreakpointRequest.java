package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/EntryBreakpointRequest.java, eclipse, eclipse-dev, 20011128
// Version 1.19 (last modified 11/28/01 15:58:59)
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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;

import com.ibm.debug.model.Breakpoint;
import com.ibm.debug.model.EntryBreakpoint;
import com.ibm.debug.model.Function;
import com.ibm.debug.model.Part;
import com.ibm.debug.model.ViewFile;

class EntryBreakpointRequest extends LocationBreakpointRequest {

    private String fFunctionName = null;
    private boolean fCaseSensitive = false;
	protected final String msgKey = super.msgKey + "entry.";


    /**
     * Constructor for EntryBreakpointRequest
     */
    EntryBreakpointRequest(IMarker marker, PICLDebugTarget debugTarget)
        throws DebugException {
        super(marker, debugTarget);


        if (fAttributes.containsKey(IPICLDebugConstants.CASESENSITIVE))
        	fCaseSensitive = ((Boolean)fAttributes.get(IPICLDebugConstants.CASESENSITIVE)).booleanValue();


        if (fAttributes.containsKey(IPICLDebugConstants.FUNCTION_NAME))
            fFunctionName = (String)fAttributes.get(IPICLDebugConstants.FUNCTION_NAME);
    }

    /**
     * @see BreakpointRequest#setBreakpoint()
     */
    public boolean setBreakpoint() throws PICLException {

        boolean rc = false;

        if (getDeferred()) {
            try {
                rc = getDebugTarget().getDebuggeeProcess().setDeferredEntryBreakpoint(getEnabled(),
                                                                                  getFunctionName(),
                                                                                  getModuleName(),
                                                                                  getFoundPartName(),
                                                                                  getThreadAsNumber(),
                                                                                  getEveryValue(),getFromValue(),getToValue(),
                                                                                  getConditionalExpression(),
                                                                                  syncRequest(),
																				  getMarker());
            } catch(IOException e) {
                throw new PICLException(PICLUtils.getResourceString(super.msgKey + "sendError"));
            }
        } else {  // set the breakpoint only if the function is available

			ViewFile viewFile = getDebugTarget().getViewFile(getMarker(),getDebugTarget().getDebugEngine().getSourceViewInformation());
			if (viewFile == null)
				throw new PICLException(PICLUtils.getResourceString(msgKey + "file_not_found"));

        	Part part= viewFile.view().part();

        	Vector functions = null;
        	try {
        		functions = part.getFunctions(getFunctionName(),getCaseSensitive());
        	} catch(IOException ioe) {
                throw new PICLException(PICLUtils.getResourceString(msgKey + "error_getting_functions"));
        	}
        	if(functions == null)
        		throw new PICLException(PICLUtils.getResourceString(msgKey + "function_not_found"));
        	Function function = (Function)functions.firstElement();

        	if (function == null)
        		throw new PICLException(PICLUtils.getResourceString(msgKey + "function_not_found"));


        	try {
	        	rc = function.setBreakpoint(getEnabled(),
	        								getEveryValue(),getFromValue(),getToValue(),
	        								getConditionalExpression(),
	        								getThreadAsNumber(),
	        								syncRequest(),
											getMarker());
        	} catch(IOException ioe) {
        		throw new PICLException(PICLUtils.getResourceString(super.msgKey + "senderror"));
        	}

        }

		if (!rc)
       		throw new PICLException(PICLUtils.getResourceString(super.msgKey + "seterror"));

        return true;
    }


    /**
     * Gets the functionName
     * @return Returns a String
     */
    protected String getFunctionName() {
        return fFunctionName;
    }

	/**
	 * Gets the caseSensitive
	 * @return Returns a boolean
	 */
	protected boolean getCaseSensitive() {
		return fCaseSensitive;
	}
    /**
     * Sets the caseSensitive
     * @param caseSensitive The caseSensitive to set
     */
    protected void setCaseSensitive(boolean caseSensitive) {
        fCaseSensitive = caseSensitive;
    }


	/**
	 * Update the attributes of the marker with the values from the breakpoint
	 * @param The marker that matches the breakpoint
	 * @param The breakpoint from the engine
	 * @return true if successful
	 */
	public static boolean updateAttributes(IMarker marker, Breakpoint breakpoint, PICLDebugTarget debugTarget) {

		EntryBreakpoint bkp = (EntryBreakpoint)breakpoint;

		String[] attributeNames = {IPICLDebugConstants.UPDATE_BREAKPOINT,
                                   IPICLDebugConstants.MODULE_NAME,
								   IPICLDebugConstants.OBJECT_NAME,
								   IPICLDebugConstants.SOURCE_FILE_NAME,
								   IPICLDebugConstants.THREAD,
								   IPICLDebugConstants.EVERY_VALUE,
								   IPICLDebugConstants.TO_VALUE,
								   IPICLDebugConstants.FROM_VALUE,
								   IPICLDebugConstants.CONDITIONAL_EXPRESSION,
								   IPICLDebugConstants.DEFERRED,
								   IPICLDebugConstants.FUNCTION_NAME};

		Object[] values = {new Boolean(false),
                           bkp.getModuleName(),
						   bkp.getPartName(),
						   bkp.getFileName(),
						   String.valueOf(bkp.getThreadID()),
						   new Integer(bkp.getEveryVal()),
						   new Integer(bkp.getToVal()),
						   new Integer(bkp.getFromVal()),
						   bkp.getExpression(),
						   new Boolean(bkp.isDeferred()),
						   bkp.getFunctionName()};


		try {
	        marker.setAttributes(attributeNames, values);
		} catch(CoreException ce) {
			return false;
		}

		return true;
	}

}
