package com.ibm.debug.internal.pdt.ui.editor;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/editor/BreakpointRulerAction.java, eclipse, eclipse-dev, 20011128
// Version 1.5 (last modified 11/28/01 16:00:33)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import com.ibm.debug.epdc.EPDC;
import com.ibm.debug.internal.picl.EngineSuppliedViewEditorInput;
import com.ibm.debug.internal.picl.IPICLDebugConstants;
import com.ibm.debug.internal.picl.PICLUtils;
import com.ibm.debug.model.Host;
import com.ibm.debug.model.ViewInformation;
import com.ibm.lpex.alef.LpexAbstractTextEditor;
import com.ibm.lpex.alef.LpexMarkerRulerAction;
import com.ibm.lpex.core.LpexView;

// This action is used for adding / removing breakpoints (line and address) from
// the vertical ruler of the Debugger Editor.

public class BreakpointRulerAction extends LpexMarkerRulerAction {

	boolean doAddressBreakpoint = false;
	IResource resource = null;
	IBreakpointManager breakpointManager= DebugPlugin.getDefault().getBreakpointManager();
	String address = null;
	int line = 0;
	EngineSuppliedViewEditorInput engineViewEI = null;
	IFile file = null;
	/**
	 * Constructor for BreakpointRulerAction
	 */
	public BreakpointRulerAction(ResourceBundle bundle,
								String prefix,
								IVerticalRuler ruler,
								ITextEditor editor,
								String markerType) {

		super(bundle, prefix, ruler, editor, markerType, false);
	}

	/**
	 * @see MarkerRulerAction#addMarker()
	 */
	protected void addMarker() {
		IEditorInput input = getTextEditor().getEditorInput();

		line = getVerticalRuler().getLineOfLastMouseButtonActivity() + 1;

		if (input instanceof IFileEditorInput) {
			IFile file = ((IFileEditorInput)input).getFile();

			if (file != null) {
				resource= (IResource) file;
				IWorkspaceRunnable body = new IWorkspaceRunnable()
				{
					public void run(IProgressMonitor monitor) throws CoreException
					{
						try {
							IMarker breakpoint = resource.createMarker(IPICLDebugConstants.PICL_LINE_BREAKPOINT);
							breakpointManager.configureLineBreakpoint(breakpoint, PICLUtils.getModelIdentifier(), true, line, -1, -1);

			   				try {
								breakpointManager.addBreakpoint(breakpoint);
							}
							catch (DebugException de) {
							}
						}
						catch (CoreException ce) {
						}
					}
				};
				try {
					ResourcesPlugin.getWorkspace().run(body, null);
				}
				catch (CoreException ce) {
				}
			}
		} else 	if (input instanceof EngineSuppliedViewEditorInput) {
			engineViewEI = (EngineSuppliedViewEditorInput) input;

			ViewInformation viewInfo = engineViewEI.getViewInformation();
			short kind = viewInfo.kind();

			// Address breakpoint used for mixed and real disassembly (i.e. not AS400 statement)
			// views. Line breakpoint used for source, listing and statement views.
			doAddressBreakpoint = false;
			if (kind == EPDC.View_Class_Mixed ||
					(kind == EPDC.View_Class_Disasm && engineViewEI.getEngineHost() != Host.OS400))
				doAddressBreakpoint = true;

			// EngineSuppliedViews should only be displayed in the Debugger Editor, so we
			// can safely assume the editor is a LpexAbstractTextEditor
			LpexView lpexView = ((LpexAbstractTextEditor)getTextEditor()).getLpexView();

			// The address is determined from the "prefix" area of the source view
			String lineText = lpexView.elementText(line);
			address = lineText.substring(0, engineViewEI.getPrefixLength());
			IProject project = engineViewEI.getProject();

			resource= (IResource) project;
			if (project != null) {
				IWorkspaceRunnable body = new IWorkspaceRunnable()
				{
					public void run(IProgressMonitor monitor) throws CoreException
					{
						String filename = engineViewEI.getName();
						try {
							if (doAddressBreakpoint) {
								IMarker breakpoint = resource.createMarker(IPICLDebugConstants.PICL_ADDRESS_BREAKPOINT);
								//breakpointManager.configureBreakpoint(breakpoint, PICLUtils.getModelIdentifier(), true);
								breakpointManager.configureLineBreakpoint(breakpoint, PICLUtils.getModelIdentifier(), true, line, -1, -1);
								String[] attributeNames = {IPICLDebugConstants.EDITABLE,
															IPICLDebugConstants.ADDRESS_EXPRESSION,
															IPICLDebugConstants.SOURCE_FILE_NAME};

								Object[] values = {new Boolean(true), address, filename};

								breakpoint.setAttributes(attributeNames, values);
								breakpointManager.addBreakpoint(breakpoint);
							}
							else {
								IMarker breakpoint = resource.createMarker(IPICLDebugConstants.PICL_LINE_BREAKPOINT);
								breakpointManager.configureLineBreakpoint(breakpoint, PICLUtils.getModelIdentifier(), true, line, -1, -1);

								// Store source file name in breakpoint so we don't lose it
								if (filename != null) {
									String[] attributeNames = {IPICLDebugConstants.SOURCE_FILE_NAME};
									Object[] values = {filename};
									breakpoint.setAttributes(attributeNames, values);
								}
								try {
									breakpointManager.addBreakpoint(breakpoint);
								}
								catch (DebugException de) {
								}
							}
						}
						catch (CoreException ce) {
						}
					}
				};
				try	{
					ResourcesPlugin.getWorkspace().run(body, null);
				}
				catch (CoreException ce) {
				}
			}
		}
	}

	/**
	 * @see MarkerRulerAction#removeMarkers(List)
	 */
	protected void removeMarkers(List markers) {
		IBreakpointManager breakpointManager= DebugPlugin.getDefault().getBreakpointManager();
		try {

			Iterator e= markers.iterator();
			while (e.hasNext()) {
				breakpointManager.removeBreakpoint((IMarker) e.next(), true);
			}

		} catch (CoreException e) {
		}
	}
}

