package com.ibm.debug.internal.pdt.ui.actions;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/actions/ExpandAllAction.java, eclipse, eclipse-dev, 20011128
// Version 1.3 (last modified 11/28/01 15:59:45)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.debug.internal.ui.AbstractDebugView;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.actions.SelectionProviderAction;

public class ExpandAllAction implements IViewActionDelegate {

	TreeViewer treeViewer;

	/**
	 * @see IViewActionDelegate#init(IViewPart)
	 */
	public void init(IViewPart viewPart)
	{
		if(viewPart instanceof AbstractDebugView)
		{
			Viewer viewer = ((AbstractDebugView) viewPart).getViewer();
			if(viewer instanceof TreeViewer)
				treeViewer = ((TreeViewer) viewer);
		}
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction arg0) {
		if(treeViewer != null)
		treeViewer.expandAll();
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction arg0, ISelection arg1) {
	}

}

