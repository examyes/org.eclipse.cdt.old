package com.ibm.debug.internal.pdt.ui.actions;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/actions/ResetStorageMonitorAction.java, eclipse, eclipse-dev, 20011128
// Version 1.10 (last modified 11/28/01 16:01:02)
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
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.ui.help.WorkbenchHelp;
import com.ibm.debug.internal.pdt.ui.views.MonitorView;
import com.ibm.debug.internal.pdt.ui.views.StorageView;
import com.ibm.debug.internal.pdt.ui.views.StorageViewTab;
import com.ibm.debug.internal.picl.IPICLDebugConstants;
import com.ibm.debug.internal.picl.PICLMonitorParent;
import com.ibm.debug.internal.picl.PICLStorage;
import com.ibm.debug.internal.picl.PICLUtils;
import com.ibm.debug.internal.picl.PICLVariable;
import com.ibm.debug.model.Storage;

public class ResetStorageMonitorAction extends Action {
	protected static final String PREFIX= "ResetStorageMonitorAction.";
	private StorageViewTab storageTab = null;

	/**
	 * Constructor for ResetStorageMonitorAction
	 */
	//intended for use with the StorageView
	public ResetStorageMonitorAction() {
		super(PICLUtils.getResourceString(PREFIX+"label"));
		setToolTipText(PICLUtils.getResourceString(PREFIX+"tooltip"));

		WorkbenchHelp.setHelp(this, new Object[] { PICLUtils.getHelpResourceString("ResetStorageMonitorAction") });
	}

	/**
	 * Constructor for ResetStorageMonitorAction
	 */
	//intended for use with the StorageViewTab
	public ResetStorageMonitorAction(StorageViewTab sTab) {
		super(PICLUtils.getResourceString(PREFIX+"label"));
		setToolTipText(PICLUtils.getResourceString(PREFIX+"tooltip"));
		storageTab = sTab;

		WorkbenchHelp.setHelp(this, new Object[] { PICLUtils.getHelpResourceString("ResetStorageMonitorAction") });
	}


	/**
	 * @see Action#run()
	 */
	public void run() {
		if (storageTab == null) {
			// get the StorageView
			IWorkbenchPage p= DebugUIPlugin.getActiveWorkbenchWindow().getActivePage();
			if (p == null) { return; }
			StorageView view= (StorageView) p.findView("com.ibm.debug.pdt.ui.StorageView");
			if (view == null) {
				try {
					view= (StorageView) p.showView("com.ibm.debug.pdt.ui.StorageView");
				} catch (PartInitException e) {
					return;
				}
			}
			p.bringToTop(view);
			storageTab = view.getTopStorageTab();
			if (storageTab != null) {
				storageTab.reloadTable(storageTab.getStorage().getAddress(), true);
			}
			storageTab = null;
		} else {
			storageTab.reloadTable(storageTab.getStorage().getAddress(), true);
		}
	}

}
