package com.ibm.debug.internal.pdt.ui.actions;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/actions/RemoveVariableFromMonitorAction.java, eclipse, eclipse-dev, 20011128
// Version 1.9 (last modified 11/28/01 15:59:18)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.util.Iterator;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.ui.help.WorkbenchHelp;
import com.ibm.debug.internal.pdt.ui.views.MonitorView;
import com.ibm.debug.internal.picl.PICLMonitorParent;
import com.ibm.debug.internal.picl.PICLUtils;
import com.ibm.debug.internal.picl.PICLVariable;


public class RemoveVariableFromMonitorAction extends SelectionProviderAction {
	protected static final String PREFIX= "RemoveVariableFromMonitorAction.";

	/**
	 * Constructor for RemoveVariableFromMonitorAction
	 */
	public RemoveVariableFromMonitorAction(ISelectionProvider provider) {
		super(provider, PICLUtils.getResourceString(PREFIX+"label"));
		setToolTipText(PICLUtils.getResourceString(PREFIX+"tooltip"));
		setEnabled(!getStructuredSelection().isEmpty());

		WorkbenchHelp.setHelp(this, new Object[] { PICLUtils.getHelpResourceString("RemoveVariableFromMonitorAction") });
		}

	protected void doAction(MonitorView view) throws DebugException {
		IStructuredSelection selection= getStructuredSelection();
		Iterator vars= selection.iterator();
		while (vars.hasNext()) {
			Object item= vars.next();
			if (item instanceof PICLVariable) {
				((PICLVariable)item).delete();
			}
		}
	}

	/**
	 * @see Action#run()
	 */
	public void run() {
		// get the MonitorView
		IWorkbenchPage p= DebugUIPlugin.getActiveWorkbenchWindow().getActivePage();
		if (p == null) {
			return;
		}
		MonitorView view= (MonitorView) p.findView("com.ibm.debug.pdt.ui.MonitorView");
		if (view == null) {
			// open a new view
			try {
				view= (MonitorView) p.showView("com.ibm.debug.pdt.ui.MonitorView");
			} catch (PartInitException e) {
				//DebugUIUtils.logError(e);
				return;
			}
		}

		try {
			doAction(view);
		} catch (DebugException de) {
			//DebugUIUtils.logError(de);
		}
	}


	/**
	 * @see SelectionProviderAction
	 */
	public void selectionChanged(IStructuredSelection sel) {
		setChecked(false);
		setEnabled(!sel.isEmpty());
		Iterator iter = sel.iterator();
		Object object = null;
		while (iter.hasNext()) {
			object = iter.next();
			if (object instanceof PICLVariable) {
				PICLVariable monitor= (PICLVariable)object;
				//can only delete root items
				if (! (monitor.getParent() instanceof PICLMonitorParent)) {
					setEnabled(false);
					return;
				}
			} else {
				setEnabled(false);
				return;
			}
		}
	}

}
