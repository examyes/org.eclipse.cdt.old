package org.eclipse.cdt.cpp.ui.internal.editor;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 * All Rights Reserved.
 */


import org.eclipse.core.resources.IMarker;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.search.ui.SearchUI;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.MarkerUtilities;

public class CppMarkerAnnotation extends MarkerAnnotation {		
	Image _breakpointImage;
	
	public CppMarkerAnnotation(IMarker marker) {
		super(marker);
		//      System.out.println("CppMarkerAnnotation()");
	}
	/**
	 * @see MarkerAnnotation#getUnknownImageName(IMarker)
	 */
	protected String getUnknownImageName(IMarker marker) {
		return null;
	}
	/**
	 * Initializes the annotation's icon representation and its drawing layer
	 * based upon the properties of the underlying marker.
	 */
	protected void initialize()
   {
	//      System.out.println("CppMarkerAnnotation.initialize()");
	IMarker breakpoint = getMarker();

	if (MarkerUtilities.isMarkerType(breakpoint, IBreakpoint.BREAKPOINT_MARKER)) {

		IDebugModelPresentation fPresentation =
			DebugUITools.newDebugModelPresentation();

		setLayer(2);
		setImage(fPresentation.getImage(breakpoint));
		return;
	} else if (MarkerUtilities.isMarkerType(getMarker(), SearchUI.SEARCH_MARKER)) {
		setLayer(2);
		setImage(SearchUI.getSearchMarkerImage());
		return;
	}

	super.initialize();
}}
