package com.ibm.debug.internal.pdt.ui.actions;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/actions/DisableVariableMonitorAction.java, eclipse, eclipse-dev, 20011128
// Version 1.9 (last modified 11/28/01 15:59:17)
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
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.ui.help.WorkbenchHelp;
import com.ibm.debug.internal.pdt.ui.views.MonitorView;
import com.ibm.debug.internal.picl.PICLMonitorParent;
import com.ibm.debug.internal.picl.PICLUtils;
import com.ibm.debug.internal.picl.PICLVariable;

public class DisableVariableMonitorAction extends SelectionProviderAction {
	protected static final String PREFIX= "DisableVariableMonitorAction.";

	/**
	 * Constructor for DisableVariableMonitorAction
	 */
	public DisableVariableMonitorAction(ISelectionProvider provider) {
		super(provider, PICLUtils.getResourceString(PREFIX+"label.disable"));
		setToolTipText(PICLUtils.getResourceString(PREFIX+"tooltip.disable"));
		setEnabled(!getStructuredSelection().isEmpty());

		WorkbenchHelp.setHelp(this, new Object[] { PICLUtils.getHelpResourceString("DisableVariableMonitorAction") });
	}

	protected void doAction(MonitorView view) throws DebugException {
		IStructuredSelection s = getStructuredSelection();
		Iterator vars = s.iterator();
		while (vars.hasNext()) {
			PICLVariable var = (PICLVariable)vars.next();
			if (isChecked()) {
				var.disable();
			} else {
				var.enable();
			}
			view.getViewer().refresh();
		}
	}

	/**
	 * @see Action#run()
	 */
	public void run() {
		// get the view
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
	 * @see Action
	 */
	public void setChecked(boolean value) {
		super.setChecked(value);
		setText(value ? PICLUtils.getResourceString(PREFIX+"label.enable") : PICLUtils.getResourceString(PREFIX+"label.disable"));
		setToolTipText(value ? PICLUtils.getResourceString(PREFIX+"tooltip.enable") : PICLUtils.getResourceString(PREFIX+"tooltip.disable"));
	}


	/**
	 * @see SelectionProviderAction
	 */
	public void selectionChanged(IStructuredSelection sel) {
		if (sel.isEmpty()) {
			setEnabled(false);
			setChecked(false);
			return;
		}

		boolean atLeastOneIsEnabled = false;
		Iterator vars= sel.iterator();
		while (vars.hasNext()) {
			Object item= vars.next();
			if (item instanceof PICLVariable) {
				// only root items can be disabled. if any item is a child, disable the action
				if (! (((PICLVariable)item).getParent() instanceof PICLMonitorParent)) {
					setChecked(false);
					setEnabled(false);
					return;
				} else if (((PICLVariable)item).isEnabled()) {
					atLeastOneIsEnabled = true;
				}
			}
		}

		// for multiple selections - if any monitor is enabled, set the action to "disable monitor" mode
		if (atLeastOneIsEnabled) {
			setChecked(false);
			setEnabled(true);
		} else {	// if we've gotten this far, then every selected item is a parent and is disabled
			setEnabled(true);
			setChecked(true);
		}
	}

}
