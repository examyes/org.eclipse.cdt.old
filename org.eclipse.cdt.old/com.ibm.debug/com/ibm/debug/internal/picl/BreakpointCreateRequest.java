package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/BreakpointCreateRequest.java, eclipse, eclipse-dev, 20011129
// Version 1.8 (last modified 11/29/01 14:15:59)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.IOException;
import java.util.Map;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;

import com.ibm.debug.epdc.EPDC;
import com.ibm.debug.model.Breakpoint;
import com.ibm.debug.model.ErrorOccurredEvent;
import com.ibm.debug.model.Module;
import com.ibm.debug.model.Part;

abstract class BreakpointCreateRequest extends BreakpointRequest {


    private IMarker fMarker = null;

    protected Map fAttributes = null;
    private boolean fUseNamesFromResource = true;
    private String fModuleName = null;
    private String fFoundPartName = null;
    private String fResourceFileName = null;


    private String fThread = "0";

    private int fEveryValue = 0;
    private int fToValue = 0;
    private int fFromValue = 0;

    private int fViewNumber = 1;  // default to view 1

    private int fLineNumber = 0;

    private boolean fEnabled = true;

    BreakpointCreateRequest(IMarker marker, PICLDebugTarget debugTarget) throws DebugException {
		super(debugTarget);

        fMarker = marker;
        try {
            fAttributes = marker.getAttributes();
        } catch(CoreException e) {}

        if (fAttributes != null) {
            if (fAttributes.containsKey(IPICLDebugConstants.MODULE_NAME)) {
                fModuleName = (String)fAttributes.get(IPICLDebugConstants.MODULE_NAME);
                fUseNamesFromResource = false;
            }
            if (fAttributes.containsKey(IPICLDebugConstants.OBJECT_NAME)) {
                fFoundPartName = (String)fAttributes.get(IPICLDebugConstants.OBJECT_NAME);
                fUseNamesFromResource = false;
            }
            if (fAttributes.containsKey(IPICLDebugConstants.SOURCE_FILE_NAME)) {
                fResourceFileName = (String)fAttributes.get(IPICLDebugConstants.SOURCE_FILE_NAME);
                fUseNamesFromResource = false;
            }
            if (fAttributes.containsKey(IPICLDebugConstants.THREAD))
                fThread = (String)fAttributes.get(IPICLDebugConstants.THREAD);


            if (fAttributes.containsKey(IPICLDebugConstants.EVERY_VALUE))
                fEveryValue = ((Integer)fAttributes.get(IPICLDebugConstants.EVERY_VALUE)).intValue();


            if (fAttributes.containsKey(IPICLDebugConstants.TO_VALUE))
                fToValue = ((Integer)fAttributes.get(IPICLDebugConstants.TO_VALUE)).intValue();


            if (fAttributes.containsKey(IPICLDebugConstants.FROM_VALUE))
                fFromValue = ((Integer)fAttributes.get(IPICLDebugConstants.FROM_VALUE)).intValue();


        }


        fEnabled = DebugPlugin.getDefault().getBreakpointManager().isEnabled(marker);

		// if the source file name is not in the attributes then get the name from the resource

        if (fUseNamesFromResource) {
            IResource resource = marker.getResource();
            if (resource instanceof IFile)
            	fResourceFileName = resource.getName();
        }

    }

    /**
     * Sets the breakpoint
     * @return returns true if request to set breakpoint was successful.   This does not mean that the breakpoint
     * was actually set.
     */
    protected abstract boolean setBreakpoint() throws PICLException;


	/**
	 * executes the set breakpoint request
	 * @return true if set breakpoint was successful
	 */
	public void execute() throws PICLException {

		beginRequest();

		try {
			setBreakpoint();
		} finally {
			endRequest();
		}
	}

    /**
     * remoteExtension(String)
     * @param File name that will have extension removed.
     * @return Returns string minus extension
     */
    protected String removeExtension(String name) {
        int posn = name.lastIndexOf('.');
        if (posn > 0)
            return name.substring(0,posn);
        else
            return name;
    }


    /**
     * Check if names should come from the IMarker's resource
     * @return Returns a boolean, true means to use the name from the resource associated with the marker
     */
    protected boolean getUseNamesFromResource() {
        return fUseNamesFromResource;
    }

    /**
     * Gets the lineNumber
     * @return Returns a int
     */
    protected int getLineNumber() {
        return fLineNumber;
    }
    /**
     * Sets the lineNumber
     * @param lineNumber The lineNumber to set
     */
    protected void setLineNumber(int lineNumber) {
        fLineNumber = lineNumber;
    }

    /**
     * Gets the thread
     * @return Returns a String
     */
    protected String getThread() {
        return fThread;
    }
    /**
     * Sets the thread
     * @param thread The thread to set
     */
    protected void setThread(String thread) {
        fThread = thread;
    }

    /**
     * Gets the enabled
     * @return Returns a boolean
     */
    protected boolean getEnabled() {
        return fEnabled;
    }
    /**
     * Sets the enabled
     * @param enabled The enabled to set
     */
    protected void setEnabled(boolean enabled) {
        fEnabled = enabled;
    }

    /**
     * Gets the moduleName
     * @return Returns a String
     */
    protected String getModuleName() {
        return fModuleName;
    }
    /**
     * Sets the moduleName
     * @param moduleName The moduleName to set
     */
    protected void setModuleName(String moduleName) {
        fModuleName = moduleName;
    }

    /**
     * Gets the foundPartName
     * @return Returns a String
     */
    protected String getFoundPartName() {
        if(fFoundPartName == null)
            return null;

        return fFoundPartName.toLowerCase();   // lower case because the engine seems to be case sensitive
    }
    /**
     * Sets the foundPartName
     * @param foundPartName The foundPartName to set
     */
    protected void setFoundPartName(String foundPartName) {
        fFoundPartName = foundPartName;
    }

    /**
     * Gets the resourceFileName
     * @return Returns a String
     */
    protected String getResourceFileName() {
        return fResourceFileName;
    }
    /**
     * Sets the resourceFileName
     * @param resourceFileName The resourceFileName to set
     */
    protected void setResourceFileName(String resourceFileName) {
        fResourceFileName = resourceFileName;
    }

    /**
     * Gets the everyValue
     * @return Returns a int
     */
    protected int getEveryValue() {
        return fEveryValue;
    }
    /**
     * Sets the everyValue
     * @param everyValue The everyValue to set
     */
    protected void setEveryValue(int everyValue) {
        fEveryValue = everyValue;
    }

    /**
     * Gets the fromValue
     * @return Returns a int
     */
    protected int getFromValue() {
        return fFromValue;
    }
    /**
     * Sets the fromValue
     * @param fromValue The fromValue to set
     */
    protected void setFromValue(int fromValue) {
        fFromValue = fromValue;
    }

    /**
     * Gets the toValue
     * @return Returns a int
     */
    protected int getToValue() {
        return fToValue;
    }
    /**
     * Sets the toValue
     * @param toValue The toValue to set
     */
    protected void setToValue(int toValue) {
        fToValue = toValue;
    }

    /**
     * Gets the marker
     * @return Returns a IMarker
     */
    protected IMarker getMarker() {
        return fMarker;
    }

    /**
     * Gets the thread ID as a number
     * @return thread ID (returns 0 if invalid)
     */
    protected int getThreadAsNumber() {

        int threadAsNumber = 0;
        try {
            threadAsNumber = Integer.parseInt(getThread());   // try to get a number from the thread passed in
        } catch(NumberFormatException e) {}

        return threadAsNumber;
    }

    /**
     * Sets error attribute of breakpoint
     * @param status of setting breakpoint, true means successful
     * @param the attribute that was in error if known, null if not known
     * @param the text of the message describing the error, null if none available
     */
    protected void setError(boolean error, String errorAttribute, String errorMsgText) {

    	String[] attributes = {IPICLDebugConstants.ERROR,
    						   IPICLDebugConstants.ERROR_ATTRIBUTE,
    						   IPICLDebugConstants.ERROR_MSGTEXT};

    	Object[] values = {new Boolean(error), errorAttribute, errorMsgText};

    	try {
	    	fMarker.setAttributes(attributes,values);
	   	} catch(CoreException ce) {}
    }

    /**
     * Clears the error attributes of the breakpoint
     */
    protected void clearError() {

    	String[] attributes = {IPICLDebugConstants.ERROR,
    						   IPICLDebugConstants.ERROR_ATTRIBUTE,
    						   IPICLDebugConstants.ERROR_MSGTEXT};

    	Object[] values = {new Boolean(false), null, null};

    	try {
	    	fMarker.setAttributes(attributes,values);
	   	} catch(CoreException ce) {}

    }

    /**
     * @see IPICLEngineRequestError#setError(ErrorOccurredEvent)
     * Handle errors when setting breakpoints by updating the attributes of the marker with the error message
     * and the attribute in error if available.
     */
    public void setError(ErrorOccurredEvent errorEvent) {

		super.setError(errorEvent);
    	String attributeInError = null;

		// compare the ID of the breakpoint to the ID of this debug target.  If they match
		// then it is ok to update the error attributes.
		// This is done to make sure that only the engine that is associated with the setting of the
		// breakpoint is the one that sets the error attributes.

		String IDfromBkpt = fMarker.getAttribute(IPICLDebugConstants.ENGINE_ID_ATTRIBUTE,"not set");

		if (!(getDebugTarget().getUniqueID().equals(IDfromBkpt)))
			return;  // not the debug engine that this breakpoint was initiated on.


    	// get the error code to determine the attribute in error if that is possible

    	int errorCode = errorEvent.getReturnCode();

    	switch (errorCode) {
    		case EPDC.ExecRc_BadLineNum:
    			attributeInError = IPICLDebugConstants.LINE_NUMBER;
    			break;
    		case EPDC.ExecRc_BadExpr:
    			attributeInError = IPICLDebugConstants.ADDRESS_EXPRESSION;
    			break;
    		default:
    			break;
    	}

    	setError(true, attributeInError, errorEvent.getMessage());

    }


}
