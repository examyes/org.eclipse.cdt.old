package com.ibm.debug.internal.pdt.ui.actions;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/actions/AddSnippetToStorageMonitorAction.java, eclipse, eclipse-dev, 20011128
// Version 1.6 (last modified 11/28/01 16:00:48)
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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DebugView;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

import com.ibm.debug.internal.pdt.ui.views.StorageView;
import com.ibm.debug.internal.picl.EngineSuppliedViewEditorInput;
import com.ibm.debug.internal.picl.IPICLDebugConstants;
import com.ibm.debug.internal.picl.PICLDebugElement;
import com.ibm.debug.internal.picl.PICLDebugTarget;
import com.ibm.debug.internal.picl.PICLException;
import com.ibm.debug.internal.picl.PICLThread;
//import com.ibm.debug.internal.picl.PICLVariable;
import com.ibm.debug.internal.picl.PICLUtils;
import com.ibm.debug.model.ViewInformation;


public class AddSnippetToStorageMonitorAction implements IEditorActionDelegate, ISelectionListener {
	protected final static String PREFIX= "AddSnippetToStorageMonitorAction.";
	private IEditorPart editor;
	private IAction proxyAction;
	private ISelection currentSelection = null;

	public AddSnippetToStorageMonitorAction() {
		super();
		DebugUIPlugin.getDefault().addSelectionListener(this);
		editor = null;
		proxyAction = null;
	}


 	/** * @see IEditorActionDelegate#setActiveEditor(IAction, IEditorPart) */
	public void setActiveEditor(IAction pAction, IEditorPart editorPart) {
		proxyAction = pAction;
		proxyAction.setEnabled(false);
		this.editor = editorPart;
	}


	/** * @see IActionDelegate#run(IAction) */
	public void run(IAction arg0) {

		//get the current editor
		if (editor == null)
			editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (editor != null) {
			//get the selected text
			ITextEditor textEditor = (ITextEditor) editor;
			ITextSelection textSelection = (ITextSelection) textEditor.getSelectionProvider().getSelection();
			String snippet = textSelection.getText().trim();
			if (snippet.length() == 0) { return; }
			int lineNum = textSelection.getStartLine();
			if (lineNum < 0) { return; }

			// the current selection in the debug view was saved in selectionChanged()
			if (currentSelection == null || currentSelection.isEmpty() || ((IStructuredSelection)currentSelection).size() > 1) {
				return;
			}

			Object elem = ((IStructuredSelection)currentSelection).getFirstElement();

			//if a launch or debugtarget is selected, use the currently stopped thread from the debugtarget
			if (elem instanceof Launch) {
				elem = ((Launch)elem).getDebugTarget();
			}
			if (elem instanceof PICLDebugTarget) {
				elem = ((PICLDebugTarget)elem).getStoppingThread();
			}
			//if the selected item isn't a PICLDebugElement we can't do anything with it
			if (! (elem instanceof PICLDebugElement) ) {
				return;
			}

			//any PICLDebugElement can get its parent thread
			IThread thread = ((PICLDebugElement)elem).getThread();
			if (thread == null || !(thread instanceof PICLThread) || thread.isTerminated()) {
				return;
			}

			//get the resource and viewinformation associated with the editorInput
			IEditorInput editorInput = editor.getEditorInput();
			IResource inputResource = null;
			ViewInformation viewInfo = null;
			if (editorInput instanceof IFileEditorInput) {
				//the resource is simply the file
				inputResource = ((IFileEditorInput)editorInput).getFile();

				//since this is an IFileEditorInput, the viewinformation must be sourceview
				IDebugTarget target = thread.getDebugTarget();
				if (!(target instanceof PICLDebugTarget)) { return; }
				viewInfo = ((PICLDebugTarget)target).getDebugEngine().getSourceViewInformation();

			} else if (editorInput instanceof EngineSuppliedViewEditorInput) {
				//enginesuppliedviews do not correspond to actual files, so we hang the marker off the project
				inputResource = ((EngineSuppliedViewEditorInput)editorInput).getProject();

				//since this is an EngineSuppliedView, we can ask for the viewinformation directly
				viewInfo = ((EngineSuppliedViewEditorInput)editorInput).getViewInformation();

				//the enginesuppliedvieweditorinput only displays a partial buffer, so do some math to find the real line number
				lineNum += ((EngineSuppliedViewEditorInput)editorInput).getBufferStartLine() -
								((EngineSuppliedViewEditorInput)editorInput).getFileStartLine();
			} else {
				return;
			}

			// create a marker on the resource and set the line number where the expression is to be evaluated
			IMarker monitorMarker = null;
			try {
				monitorMarker = inputResource.createMarker(IPICLDebugConstants.PICL_MONITORED_EXPRESSION);
				monitorMarker.setAttribute(IMarker.LINE_NUMBER, lineNum);

				if (editorInput instanceof EngineSuppliedViewEditorInput) {
					//since in this case the marker is hung off the project, store the real part name in the marker
					monitorMarker.setAttribute(IPICLDebugConstants.SOURCE_FILE_NAME, ((EngineSuppliedViewEditorInput)editorInput).getName());
				}
			} catch (CoreException ce) {
				return;
			}

			//open a new view if necessary
			IWorkbenchPage p= DebugUIPlugin.getActiveWorkbenchWindow().getActivePage();
			if (p == null) { return; }
			StorageView view= (StorageView) p.findView("com.ibm.debug.pdt.ui.StorageView");
			if (view == null) {
				try {
					IWorkbenchPart activePart= p.getActivePart();
					view= (StorageView) p.showView("com.ibm.debug.pdt.ui.StorageView");
					p.activate(activePart);
				} catch (PartInitException e) {
					//DebugUIUtils.logError(e);
					return;
				}
			}
			p.bringToTop(view);



			try {
				((PICLThread)thread).monitorStorage(monitorMarker, snippet, viewInfo);
			} catch (PICLException pe) {
				int indexOfAmpersand = snippet.indexOf("&", 0);
				while (indexOfAmpersand >= 0) {;
					snippet = snippet.substring(0, indexOfAmpersand) + "&" + snippet.substring(indexOfAmpersand, snippet.length());
					indexOfAmpersand += 2;
					indexOfAmpersand = snippet.indexOf("&", indexOfAmpersand);
				}
				MessageDialog.openError(null, PICLUtils.getResourceString(PREFIX+"evaluationfailed"), PICLUtils.getResourceString(PREFIX+"expression") + " \"" + snippet + "\" " + PICLUtils.getResourceString(PREFIX+"couldnotbeevaluated") + " \"" + ((PICLThread)thread).getLabel(true) + "\"");
				return;
			}

		} else {}

  	}


	/** * @see IActionDelegate#selectionChanged(IAction, ISelection) */
	public void selectionChanged(IAction action, ISelection sel) {
		//I'd like to disable the action if nothing was selected in the editor
		//sel always seems to be empty - what exactly is it supposed to hold?
//		if (proxyAction == null) { return; }
//		if (sel.isEmpty()) {
//			proxyAction.setEnabled(false);
//		} else {
//			proxyAction.setEnabled(true);
//		}
//		System.out.println("In AddSnippetToMonitorAction.selectionChanged1 method");
	}


	/**
	 * @see ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection sel) {
		currentSelection = sel;  //save the current selection for when the action is run

		if (proxyAction == null) { return; }

		//only single selection of PICLDebugElements is allowed for this action
		if (sel == null || sel.isEmpty() || ((IStructuredSelection)sel).size() > 1 ||
					part == null || !(part instanceof DebugView))
		{
			proxyAction.setEnabled(false);
			return;
		}

		Object elem = ((IStructuredSelection)sel).getFirstElement();

		//Launches are not PICLDebugElements, but their debugtarget may be
		if (elem instanceof Launch) {
			elem = ((Launch)elem).getDebugTarget();
		}

		//this action is only valid for PICLDebugElements
		if (! (elem instanceof PICLDebugElement) ) {
			proxyAction.setEnabled(false);
			return;
		}

		//any PICLDebugElement can get its debugtarget
		IDebugTarget dbgtarget = ((PICLDebugElement)elem).getDebugTarget();

		if (dbgtarget == null ||
				!(dbgtarget instanceof PICLDebugTarget) ||
				dbgtarget.isTerminated() ||
				!((PICLDebugTarget)dbgtarget).supportsStorageMonitors()) {
			proxyAction.setEnabled(false);
			return;
		}

		proxyAction.setEnabled(true);
	}

}
