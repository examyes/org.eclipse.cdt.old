package com.ibm.debug.internal.pdt.ui.actions;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/actions/AddExceptionAction.java, eclipse, eclipse-dev, 20011128
// Version 1.9 (last modified 11/28/01 15:58:54)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DebugView;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;

import com.ibm.debug.PICLDebugPlugin;
import com.ibm.debug.internal.pdt.ui.dialogs.AddExceptionDialog;
import com.ibm.debug.internal.picl.PICLDebugElement;
import com.ibm.debug.internal.picl.PICLDebugTarget;


public class AddExceptionAction implements IViewActionDelegate, ISelectionListener {

	IAction myAction;
	//static int instantiationCounter=0;

	public AddExceptionAction()
	{
	//	instantiationCounter+=1;
		//System.out.println("%%%%%%%%%%%%%%%%%%%%%Exception action created. "+instantiationCounter);
	}

	/**
	 * @see IViewActionDelegate#init(IViewPart)
	 */
	public void init(IViewPart view) {
		//want to be notified when picl stack frame selected so action can be enabled.
		DebugUIPlugin.getDefault().addSelectionListener(this);

		//find debug view and ask for current selection
		//add selection listener to plugin for getting current
	}

	public void run(IAction action) {

		Shell shell= PICLDebugPlugin.getActiveWorkbenchShell();
		AddExceptionDialog dialog= new AddExceptionDialog(shell);
		if (dialog.checkEngineSupport())
			dialog.open();
		else
			dialog = null;
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 * Processes selection changed events from breakpoint view only.
	 */
	public void selectionChanged(IAction action, ISelection sel) {
		if(myAction == null)
			myAction=action;
		//System.out.println("exception:Selection Changed in breakpoint view event");

		//send selection changed event in case action wasn't created when stack selection event sent
		IWorkbenchPage p= DebugUIPlugin.getActiveWorkbenchWindow().getActivePage();
		if (p == null) 	return;
		DebugView view= (DebugView) p.findView(IDebugUIConstants.ID_DEBUG_VIEW);
		if(view==null)	return;

		ISelection currentSelection = view.getViewer().getSelection();
		selectionChanged(view, currentSelection);
	}


	/**
	 * @see ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
	 * Processes selection changed events from all debug views.
	 * Action will be enabled if a non-terminated, PICLStackFrame was selected in debug view.
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection sel) {

		//System.out.println("exception:Selection Changed event");

		if (! (part instanceof DebugView))
			return;

		Object element = null;
		if (sel instanceof IStructuredSelection)
		{
			IStructuredSelection selection = (IStructuredSelection) sel;
			element = selection.getFirstElement();
		} else
		  element = sel;

		//check if stack frame, process, or thread for live debug target
		if (element instanceof PICLDebugElement && !((IDebugElement)element).getDebugTarget().isTerminated()
			&& ((PICLDebugTarget)((IDebugElement)element).getDebugTarget()).supportsExceptionFiltering())
			myAction.setEnabled(true);
		else if (element instanceof ILaunch) //launcher
		{
		 	IDebugTarget dt =((ILaunch) element).getDebugTarget();
			if (dt instanceof PICLDebugTarget && !((PICLDebugTarget)dt).isTerminated()
				&& ((PICLDebugTarget)dt).supportsExceptionFiltering())
				myAction.setEnabled(true);
			else myAction.setEnabled(false);
		}
		else myAction.setEnabled(false);
	}

}
