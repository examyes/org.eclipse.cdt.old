package com.ibm.debug.internal.pdt.ui.actions;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/actions/CopyStorageToolbarAction.java, eclipse, eclipse-dev, 20011128
// Version 1.2 (last modified 11/28/01 16:01:23)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.internal.pdt.ui.views.StorageView;
import com.ibm.debug.internal.pdt.ui.views.StorageViewTab;
import com.ibm.debug.internal.picl.PICLUtils;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

public class CopyStorageToolbarAction extends Action {
	protected static final String PREFIX= "CopyViewToClipboardAction.";

	/**
	 * Constructor for CopyStorageToolbarAction
	 */
	public CopyStorageToolbarAction() {
		super(PICLUtils.getResourceString(PREFIX+"label"));
		setToolTipText(PICLUtils.getResourceString(PREFIX+"tooltip"));
	}


	/**
	 * @see Action#run()
	 */
	public void run() {
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
		StorageViewTab storageTab = view.getTopStorageTab();
		if (storageTab != null) {
			TableViewer tableViewer = (TableViewer)storageTab.getViewer();
			if (tableViewer != null) {
				CopyTableViewToClipboardAction fCopyTableViewToClipboardAction = new CopyTableViewToClipboardAction(tableViewer);
				fCopyTableViewToClipboardAction.run();
			}
		}
	}

}
