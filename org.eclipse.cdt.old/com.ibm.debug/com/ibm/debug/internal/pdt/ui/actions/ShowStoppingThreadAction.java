package com.ibm.debug.internal.pdt.ui.actions;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/actions/ShowStoppingThreadAction.java, eclipse, eclipse-dev, 20011128
// Version 1.5 (last modified 11/28/01 15:59:07)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.PICLDebugPlugin;
import com.ibm.debug.internal.picl.PICLDebugElement;
import com.ibm.debug.internal.picl.PICLDebugTarget;
import com.ibm.debug.internal.picl.PICLThread;
import com.ibm.debug.internal.picl.PICLUtils;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.internal.ui.AbstractDebugView;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;


public class ShowStoppingThreadAction implements IViewActionDelegate {
	private IViewPart view;

	/**
	 * Constructor for ShowStoppingThreadAction
	 */
	public ShowStoppingThreadAction() {
		super();
	}

	/**
	 * @see IViewActionDelegate#init(IViewPart)
	 */
	public void init(IViewPart arg0) {
		view = arg0;
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction arg0) {
		IDebugTarget target = PICLDebugPlugin.determineCurrentDebugTarget();
		if(target instanceof PICLDebugTarget)
		{
	   		PICLDebugTarget PICLTarget = (PICLDebugTarget)target;
			PICLThread thread = PICLTarget.getStoppingThread();

			if( view instanceof AbstractDebugView)
			{
				// Want to select top stack frame of stopping thread
				Object[] myArray= new Object[]{thread.getTopStackFrame()};
				StructuredSelection newSelection = new StructuredSelection(myArray);
				((AbstractDebugView) view).getViewer().setSelection(newSelection);
			}
		}
  	   	else
			PICLUtils.logText("ShowStoppingThreadAction - bad target?? ");
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection arg1) {
		Object element = null;
		boolean show = false;
		if (arg1 instanceof IStructuredSelection)
		{
			IStructuredSelection selection = (IStructuredSelection) arg1;
			element = selection.getFirstElement();
		} else
		  element = arg1;

		if (element instanceof PICLDebugElement)
		{
		 	// enable menu item
			show = true;
		}
		else if (element instanceof ILaunch)
		{
		 	IDebugTarget dt =((ILaunch) element).getDebugTarget();
			if (dt instanceof PICLDebugTarget)
				show = true;
		}


		if (show == true)
		{
		 	// enable menu item
			action.setEnabled(true);
		} else
		{
		 	// disable menu item
			action.setEnabled(false);
		}
	}

}

