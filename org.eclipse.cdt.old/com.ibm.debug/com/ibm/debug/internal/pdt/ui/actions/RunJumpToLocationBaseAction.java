package com.ibm.debug.internal.pdt.ui.actions;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/actions/RunJumpToLocationBaseAction.java, eclipse, eclipse-dev, 20011129
// Version 1.5 (last modified 11/29/01 14:15:58)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.PICLDebugPlugin;
import com.ibm.debug.internal.picl.EngineSuppliedViewEditorInput;
import com.ibm.debug.internal.picl.IPICLDebugConstants;
import com.ibm.debug.internal.picl.PICLDebugElement;
import com.ibm.debug.internal.picl.PICLDebugTarget;
import com.ibm.debug.internal.picl.PICLThread;
import com.ibm.debug.internal.picl.PICLUtils;
import com.ibm.lpex.alef.LpexTextEditor;
import java.util.Iterator;
import org.eclipse.core.internal.resources.Marker;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DebugView;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

// The 4 editor actions RunToLocationAction, RunToLocationRulerAction,
// JumpToLocationAction and JumpToLocationRulerAction are all very similar.
// This base class implements all the functionality and the 4 above classes
// just extend this base class and customize the functionality.


public class RunJumpToLocationBaseAction implements IEditorActionDelegate, ISelectionListener {
	private IEditorPart editor = null;
	private IAction myAction = null;

	// Next two variables should be set by derived classes to configure behaviour
	// isRulerAction : true = vertical ruler menu action; false = context menu action
	protected boolean isRulerAction = false;
	// isJumpAction : true = Jump To Location action; false = Run To Location action
	protected boolean isJumpAction = false;

	/** * Constructor for RunToLocationAction */
	public RunJumpToLocationBaseAction() {
		super();
		// Get hooked into DebugView
		DebugUIPlugin.getDefault().addSelectionListener(this);
	}

 	/** * @see IEditorActionDelegate#setActiveEditor(IAction, IEditorPart) */
	public void setActiveEditor(IAction action, IEditorPart part) {
		editor = part;
		myAction = action;
		myAction.setEnabled(false);
	}

	/** * @see IActionDelegate#run(IAction) */
	public void run(IAction arg0) {

		int line = 0;
		IResource resource = null;
		IMarker locationMarker = null;
		String filename = null;

		// Make sure we have an editor.  setActiveEditor() may not have been called.
		if (editor == null)
			editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (editor == null)
			return;

		if (isRulerAction) {
			// Currently can't get the line number.  Lpex has a workaround
			// to allow us to get it until the problem is fixed
			if (editor instanceof LpexTextEditor) {
				LpexTextEditor textEditor = (LpexTextEditor) editor;
				line = textEditor.getVerticalRuler().getLineOfLastMouseButtonActivity() + 1;
			} else {
			    PICLUtils.logText("Cannot get line number due to a limitation - cancelling action");
			    return;
			}
		} else { // Must be context menu action
			ITextEditor textEditor = (ITextEditor) editor;
			ITextSelection textSelection = (ITextSelection) textEditor.getSelectionProvider().getSelection();
			line = textSelection.getStartLine() + 1;
		}

		IEditorInput input = editor.getEditorInput();
		if (input instanceof IFileEditorInput)
			resource = ((IFileEditorInput)input).getFile();
		else if (input instanceof EngineSuppliedViewEditorInput) {
			EngineSuppliedViewEditorInput viewEI = (EngineSuppliedViewEditorInput) input;
			resource = viewEI.getProject();
			// Store source file name in breakpoint so we don't lose it
			filename = viewEI.getName();
		}
		if (resource == null) {
			PICLUtils.logText("no resource for run/jump - not good.");
			return;
		}
		try {
			locationMarker = resource.createMarker(IPICLDebugConstants.PICL_LOCATION_MARKER);
			locationMarker.setAttribute(IMarker.LINE_NUMBER, line);
			if (filename != null) {
				String[] attributeNames = {IPICLDebugConstants.SOURCE_FILE_NAME};
				Object[] values = {filename};
				locationMarker.setAttributes(attributeNames, values);
			}
		} catch(CoreException ce) {
			return;
		}

		IDebugTarget target = PICLDebugPlugin.determineCurrentDebugTarget();
		if(target instanceof PICLDebugTarget)
		{
    		PICLDebugTarget PICLTarget = (PICLDebugTarget)target;
			PICLThread thread = PICLTarget.getStoppingThread();
		  	if (isJumpAction)
				thread.jumpToLocation(locationMarker);
			else
		  		thread.runToLocation(locationMarker);
	 	}
		else
			PICLUtils.logText("RunJumpToLocationBaseAction - bad target?? ");
	}

	/** * @see IActionDelegate#selectionChanged(IAction, ISelection) */
	public void selectionChanged(IAction action, ISelection sel) {
		// Don't care what is selected in editor window, but do need to save the action
		myAction = action;
	}

	/**
	 * @see ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection arg1) {
		Object element = null;
		boolean show = false;

		// If we haven't been given a handle to the action yet then no point in continuing
		if (myAction == null) {
			return;
		}
		// Only interested in selection changes in the DebugView
		if (part == null || !(part instanceof DebugView)) {
			return;
		}
		if (arg1 instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) arg1;
			element = selection.getFirstElement();
		} else
		  element = arg1;

		if (element instanceof PICLDebugElement) {
		 	// enable menu item
			show = true;
		} else if (element instanceof ILaunch) {
		 	IDebugTarget dt =((ILaunch) element).getDebugTarget();
			if (dt instanceof PICLDebugTarget)
				show = true;
		}

		myAction.setEnabled(show);
	}
}
