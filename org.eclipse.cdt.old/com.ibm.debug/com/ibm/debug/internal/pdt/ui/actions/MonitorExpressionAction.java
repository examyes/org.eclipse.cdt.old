package com.ibm.debug.internal.pdt.ui.actions;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/actions/MonitorExpressionAction.java, eclipse, eclipse-dev, 20011128
// Version 1.7 (last modified 11/28/01 16:00:47)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DebugView;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;

import org.eclipse.ui.help.WorkbenchHelp;
import com.ibm.debug.PICLDebugPlugin;
import com.ibm.debug.internal.pdt.ui.dialogs.MonitorExpressionDialog;
import com.ibm.debug.internal.picl.PICLDebugElement;
import com.ibm.debug.internal.picl.PICLDebugTarget;
import com.ibm.debug.internal.picl.PICLThread;
import com.ibm.debug.internal.picl.PICLUtils;


public class MonitorExpressionAction extends Action implements ISelectionListener {
	protected static final String PREFIX= "MonitorExpressionAction.";
	private PICLThread thread = null;
	private boolean defaultIsStorageMonitor;

	public MonitorExpressionAction(boolean defaultIsStorageMonitor)
	{
		super(PICLUtils.getResourceString(PREFIX+"label"));
		setToolTipText(PICLUtils.getResourceString(PREFIX+"tooltip"));
		this.defaultIsStorageMonitor = defaultIsStorageMonitor;
		DebugUIPlugin.getDefault().addSelectionListener(this);

		WorkbenchHelp.setHelp(this, new Object[] { PICLUtils.getHelpResourceString("MonitorExpressionAction") });
	}

	/**
	 * @see Action#run()
	 */
	public void run() {
		Shell shell= PICLDebugPlugin.getActiveWorkbenchShell();

		// the current thread was saved in selectionChanged()
		if (thread == null || !(thread instanceof PICLThread) || thread.isTerminated()) {
			return;
		}

		MonitorExpressionDialog dialog= new MonitorExpressionDialog(shell, thread, defaultIsStorageMonitor);
//		if (dialog.checkEngineSupport())
			dialog.open();
//		else
//			dialog = null;
	}

	/**
	 * @see ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
	 * Processes selection changed events from all debug views.
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection sel) {

		thread = null;

		//only single selection of PICLDebugElements is allowed for this action
		if (sel == null || sel.isEmpty() || ((IStructuredSelection)sel).size() > 1 ||
					part == null || !(part instanceof DebugView))
		{
			setEnabled(false);
			return;
		}

		Object elem = ((IStructuredSelection)sel).getFirstElement();

		//Launches are not PICLDebugElements, but their debugtarget may be
		if (elem instanceof Launch) {
			elem = ((Launch)elem).getDebugTarget();
		}

		//this action is only valid for PICLDebugElements
		if (! (elem instanceof PICLDebugElement) ) {
			setEnabled(false);
			return;
		}

		if (elem instanceof PICLDebugTarget) {
			thread = ((PICLDebugTarget)elem).getStoppingThread();
		} else {
			thread = (PICLThread)((PICLDebugElement)elem).getThread();
		}

		if (thread == null || !(thread instanceof PICLThread) || thread.isTerminated()) {
			setEnabled(false);
			return;
		}

		//check to see if engine supports storage monitors
		PICLDebugTarget target = (PICLDebugTarget)thread.getDebugTarget();
		if (target == null || target.isTerminated() || !target.supportsStorageMonitors()) {
			setEnabled(false);
			return;
		}

		setEnabled(true);
	}

}
