package com.ibm.debug.internal.pdt.ui.actions;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/actions/AddEditorBreakpointAction.java, eclipse, eclipse-dev, 20011128
// Version 1.9 (last modified 11/28/01 16:00:05)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.jface.action.*;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IDebugConstants;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.ui.texteditor.MarkerUtilities;
import com.ibm.lpex.core.*;
import com.ibm.debug.epdc.EPDC;
import com.ibm.debug.internal.pdt.ui.editor.DebuggerEditor;
import com.ibm.debug.internal.picl.EngineSuppliedViewEditorInput;
import com.ibm.debug.internal.picl.IPICLDebugConstants;
import com.ibm.debug.internal.picl.PICLDebugTarget;
import com.ibm.debug.internal.picl.PICLUtils;
import com.ibm.debug.model.Host;
import com.ibm.debug.model.ViewInformation;
import com.ibm.lpex.alef.*;

import java.util.Map;
/**
 * This class is used to demonstrate editor action extensions.
 * An extension should be defined in the readme plugin.xml.
 */
public class AddEditorBreakpointAction implements IEditorActionDelegate {
	private IEditorPart editor;
	boolean doAddressBreakpoint = false;
	IResource resource = null;
	IBreakpointManager breakpointManager= DebugPlugin.getDefault().getBreakpointManager();
	String address = null;
	int line = 0;
	EngineSuppliedViewEditorInput engineViewEI = null;
	IFile file = null;
	/**
 	* Creates a new AddEditorBreakpointAction.
 	*/
	public AddEditorBreakpointAction() {
	}

	/**
	 * See IActionDelegate
	 */
	public void run(IAction action) {

		// Make sure we have an editor.  setActiveEditor() may not have been called.
		if (editor == null)
			editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (editor == null)
			return;

		IEditorInput input = editor.getEditorInput();

		LpexView lpexView = null;
		line = 0;

		// Determine line number
		if (editor instanceof LpexAbstractTextEditor) {
			lpexView = ((LpexAbstractTextEditor)editor).getLpexView();
			line = lpexView.currentElement();
		} else if (editor instanceof ITextEditor) {
			ITextEditor textEditor= (ITextEditor) editor;
			IDocumentProvider docProvider= textEditor.getDocumentProvider();
			ISelectionProvider selProvider= textEditor.getSelectionProvider();
			ITextSelection selection= (ITextSelection) selProvider.getSelection();
			line = selection.getStartLine() + 1;
		} else {
			// Don't know what to do in this case, so just return
			return;
		}

		if (input instanceof IFileEditorInput) {
			IFile file = ((IFileEditorInput)input).getFile();

			if (file != null) {
				resource= (IResource) file;
				// Check if there is already a breakpoint on this line.  If so,
				// then we should remove it.  If not, then add one.
				IMarker curBreakpoint = getBreakpoint(resource, line, null);
				if (curBreakpoint != null) {
					// Removing breakpoint
					try {
						breakpointManager.removeBreakpoint(curBreakpoint, true);
					} catch (CoreException ce) {
					}
					return;
				}

				// Adding breakpoint
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
				try
				{
					ResourcesPlugin.getWorkspace().run(body, null);
				}
				catch (CoreException ce) {
				}
			}
		} else 	if (input instanceof EngineSuppliedViewEditorInput) {
			engineViewEI = (EngineSuppliedViewEditorInput) input;

			ViewInformation viewInfo = engineViewEI.getViewInformation();
			short kind = viewInfo.kind();

			// Address breakpoint used for mixed and real disassembly (i.e. not AS400 statement) views.
			// Line breakpoint used for source, listing and statement views.
			doAddressBreakpoint = false;
			if (kind == EPDC.View_Class_Mixed ||
					(kind == EPDC.View_Class_Disasm && engineViewEI.getEngineHost() != Host.OS400))
				doAddressBreakpoint = true;

			String lineText = lpexView.elementText(line);
			address = lineText.substring(0, engineViewEI.getPrefixLength());
			IProject project = engineViewEI.getProject();

			resource= (IResource) project;
			if (project != null) {
				// Check if there is already a breakpoint on this line.  If so,
				// then we should remove it.  If not, then add one.
				IMarker curBreakpoint = getBreakpoint(resource, line, engineViewEI.getName());
				if (curBreakpoint != null) {
					// Removing breakpoint
					try {
						breakpointManager.removeBreakpoint(curBreakpoint, true);
					} catch (CoreException ce) {
					}
					return;
				}

				// Adding breakpoint
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
				try {
					ResourcesPlugin.getWorkspace().run(body, null);
				}
				catch (CoreException ce) {
				}
			}
		}
	}
	/**
	 * The <code>EditorActionDelegate</code> implementation of this
	 * <code>IActionDelegate</code> method
	 *
	 * Selection in the desktop has changed. Plugin provider
	 * can use it to change the availability of the action
	 * or to modify other presentation properties.
	 *
	 * <p>Action delegate cannot be notified about
	 * selection changes before it is loaded. For that reason,
	 * control of action's enable state should also be performed
	 * through simple XML rules defined for the extension
	 * point. These rules allow enable state control before
	 * the delegate has been loaded.</p>
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}
	/**
	 * The <code>EditorActionDelegate</code> implementation of this
	 * <code>IEditorActionDelegate</code> method
	 *
	 * The matching editor has been activated. Notification
	 * guarantees that only editors that match the type for which
	 * this action has been registered will be tracked.
	 *
	 * @param action action proxy that represents this delegate in the desktop
	 * @param editor the matching editor that has been activated
	 */
	public void setActiveEditor(IAction action, IEditorPart editor) {
		this.editor = editor;

		// Check if add breakpoint action should be enabled or not for
		// this editor.  This can only be done if the editor input
		// came from a debug engine.

		IEditorInput input = editor.getEditorInput();
		if (input instanceof EngineSuppliedViewEditorInput) {
			engineViewEI = (EngineSuppliedViewEditorInput) input;
			String bkptType = IPICLDebugConstants.PICL_LINE_BREAKPOINT;

			ViewInformation viewInfo = engineViewEI.getViewInformation();
			short kind = viewInfo.kind();

			// Address breakpoint used for mixed and real disassembly (i.e. not AS400 statement) views.
			// Line breakpoint used for source, listing and statement views.
			if (kind == EPDC.View_Class_Mixed ||
					(kind == EPDC.View_Class_Disasm &&
						engineViewEI.getEngineHost() != Host.OS400))
				bkptType = IPICLDebugConstants.PICL_ADDRESS_BREAKPOINT;

			PICLDebugTarget target = engineViewEI.getPICLDebugTarget();
			boolean supported = target.supportsBrkptType(bkptType);
			action.setEnabled(supported);

		}

	}

	/**
	 * Check if there is already a corresponding PICLLocationBreakpoint on the specified line.
	 * If sourceFile is null then it will not be checked.
	 */

	private IMarker getBreakpoint(IResource resource, int line, String sourceFile) {
		if (resource == null)
			return null;
		try {
			IMarker markers[] =
				resource.findMarkers("com.ibm.debug.PICLLocationBreakpoint",
									true,
									IResource.DEPTH_INFINITE);
			if (markers.length == 0)
				return null;

			// check if marker is the one we are looking for
			for (int i = 0; i < markers.length; i++) {
				if (markers[i] == null)
					continue;
				if (MarkerUtilities.getLineNumber(markers[i]) != line)
					continue;
				if (sourceFile != null) {
					String markerName = (String) markers[i].getAttribute("sourceFileName");
					if (!sourceFile.equals(markerName))
						continue;
				}
				//Must be what we are looking for
				return markers[i];
			}

		} catch (CoreException e) {
		}

		return null;
	}
}
