package org.eclipse.cdt.cpp.ui.internal.editor;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 * All Rights Reserved.
 */


import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.search.ui.SearchUI;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.MarkerUtilities;

import com.ibm.debug.internal.pdt.IPICLDebugConstants;
import com.ibm.debug.pdt.PICLModelPresentation;

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
		try
      {
         IMarker breakpoint = getMarker();
			String type= breakpoint.getType();
			
//			if (MarkerUtilities.isMarkerType(getMarker(), IDebugConstants.BREAKPOINT_MARKER)) {
//			if (MarkerUtilities.isMarkerType(getMarker(), "com.ibm.debug.PICLLineBreakpoint"))
			if (MarkerUtilities.isMarkerType(getMarker(), IPICLDebugConstants.PICL_LINE_BREAKPOINT))
         {
				setLayer(2);
          /*  gone with R2
          	IBreakpointManager breakpointManager= DebugPlugin.getDefault().getBreakpointManager();
      		if (!breakpointManager.isEnabled(breakpoint))
            {
		         _breakpointImage = DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_BREAKPOINT_DISABLED);
            }
            else
            {
               _breakpointImage = DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_BREAKPOINT);
            }

				setImage(_breakpointImage);
           */

				PICLModelPresentation model = new PICLModelPresentation();
    			Image breakpointImage = model.getImage(breakpoint);
				setImage(breakpointImage);
				return;						
			} else if (MarkerUtilities.isMarkerType(getMarker(), SearchUI.SEARCH_MARKER)) {
				setLayer(2);
				setImage(SearchUI.getSearchMarkerImage());
				return;
			}
			
		} catch (CoreException e) {
		}
		
		super.initialize();
	}
}
