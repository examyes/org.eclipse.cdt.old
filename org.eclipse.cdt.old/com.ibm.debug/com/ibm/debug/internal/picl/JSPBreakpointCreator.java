package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/JSPBreakpointCreator.java, eclipse, eclipse-dev, 20011128
// Version 1.4 (last modified 11/28/01 15:57:57)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.debug.core.DebugException;
//import com.ibm.dt.core.IBreakpointCreator;
//import com.ibm.dt.internal.core.IDebugConstants;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.ui.IEditorPart;
import org.eclipse.jface.text.IDocument;

/**
 * Creates breakpoint for JSPs. This is only possible if the JSP is
 * currently being debugged, and the current stack frame corresponds to
 * the JSP in which a breakpoint is being set.
 *
 * @deprecated - we will no longer have breakpoint creators
 */

public class JSPBreakpointCreator { //implements IBreakpointCreator {


	protected static String JSP = "jsp";

	public IResource configureBreakpoint(IMarker breakpoint, Object element, IDocument document, int lineNumber) throws DebugException {

		/*String partName = null;
		//partName = (String)element.getElementProperty(IBasicPropertyConstants.P_LABEL);
		breakpoint.setAttribute(IDebugConstants.ATT_PART, partName);
		breakpoint.setAttribute(IDebugConstants.ATT_MODULE, "");
		breakpoint.setAttribute(IDebugConstants.ATT_EXTENSION, JSP);
		breakpoint.setAttribute(IEditorPart.EDITOR_ID_ATTR, "com.ibm.itp.desktop.DefaultTextEditor");
		IResource res = null;
		if (element instanceof IResource) {
			res = (IResource)element;
		} else {
			//res = (IResource)element.getElementProperty(IResource.class);
		}
		return res;*/
		return null;

	}

	/**
	 * @see IBreakpointCreator
	 */
	public Object getElement(IMarker breakpoint) {
		/*if (JSP.equals(breakpoint.getAttribute(IDebugConstants.ATT_EXTENSION))) {
			return breakpoint.getResource();
		} else {*/
			return null;
//		}
	}

	/**
	 * @see IBreakpointCreator
	 */
	public String getLabel(IMarker breakpoint) {
		/*if (JSP.equals(breakpoint.getAttribute(IDebugConstants.ATT_EXTENSION))) {
			IResource r = breakpoint.getResource();
			return r.getName();
		} else {*/
			return null;
		//}
	}
}