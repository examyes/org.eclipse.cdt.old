package com.ibm.debug.internal.pdt.ui.editor;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/editor/DebuggerMarkerAnnotationModel.java, eclipse, eclipse-dev, 20011128
// Version 1.4 (last modified 11/28/01 16:00:27)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModel;

public class DebuggerMarkerAnnotationModel
	extends ResourceMarkerAnnotationModel {
	private String filename = null;

	/**
	 * Constructor for DebuggerMarkerAnnotationModel
	 */
	public DebuggerMarkerAnnotationModel(IResource resource) {
		super(resource);

	}

	public DebuggerMarkerAnnotationModel(IResource resource, String name) {
		super(resource);

		filename = name;
	}
	/**
	 * @see AbstractMarkerAnnotationModel#createMarkerAnnotation(IMarker)
	 */
	protected MarkerAnnotation createMarkerAnnotation(IMarker marker) {

			return new DebuggerMarkerAnnotation(marker, filename);

	}

}

