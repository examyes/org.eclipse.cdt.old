package com.ibm.debug.internal.pdt.ui.editor;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/editor/DebuggerMarkerAnnotation.java, eclipse, eclipse-dev, 20011128
// Version 1.4 (last modified 11/28/01 16:00:26)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.internal.picl.IPICLDebugConstants;
import com.ibm.debug.internal.picl.PICLModelPresentation;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.IDebugConstants;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.MarkerUtilities;

public class DebuggerMarkerAnnotation extends MarkerAnnotation {

	private String filename = null;
	private boolean mySetupDone = false;
	/**
	 * Constructor for DebuggerMarkerAnnotation
	 */
	public DebuggerMarkerAnnotation(IMarker marker, String name) {
		super(marker);
		filename = name;
		// super(marker) calls initialize() before I have a chance to
		// save the filename, so I need to skip running initialize() until
		// I can save the filename.  This is the purpose of the mySetupDone
		// flag and the check of it in initialize().
		mySetupDone = true;
		initialize();
	}


	/**
	 * @see MarkerAnnotation#initialize()
	 */
	protected void initialize() {
		if (!mySetupDone)
			return;
		try {
			IMarker breakpoint = getMarker();
			String type= breakpoint.getType();

			if (MarkerUtilities.isMarkerType(getMarker(), IPICLDebugConstants.PICL_LINE_BREAKPOINT) ||
				MarkerUtilities.isMarkerType(getMarker(), IPICLDebugConstants.PICL_ADDRESS_BREAKPOINT) ||
				MarkerUtilities.isMarkerType(getMarker(), IPICLDebugConstants.PICL_ENTRY_BREAKPOINT)) {
				if (filename !=null) {
					// Need to make sure this marker should be shown for this editor input
					String sourceFilename = (String) breakpoint.getAttribute(IPICLDebugConstants.SOURCE_FILE_NAME);
					if (!filename.equals(sourceFilename))
						return;
				}
				setLayer(2);
				PICLModelPresentation model = new PICLModelPresentation();
    			Image breakpointImage = model.getImage(breakpoint);
				setImage(breakpointImage);
				return;
//			} else if (MarkerUtilities.isMarkerType(getMarker(), SearchUI.SEARCH_MARKER)) {
//				setLayer(2);
//				setImage(SearchUI.getSearchMarkerImage());
//				return;
			}

		} catch (CoreException e) {
		}

		super.initialize();
	}

}

