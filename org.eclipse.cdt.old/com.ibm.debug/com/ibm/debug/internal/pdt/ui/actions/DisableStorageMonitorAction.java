package com.ibm.debug.internal.pdt.ui.actions;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/actions/DisableStorageMonitorAction.java, eclipse, eclipse-dev, 20011128
// Version 1.7 (last modified 11/28/01 16:00:49)
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
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.IDebugUIConstants;
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
import com.ibm.debug.internal.picl.PICLMonitorParent;
import com.ibm.debug.internal.picl.PICLStorage;
import com.ibm.debug.internal.picl.PICLUtils;
import com.ibm.debug.internal.picl.PICLVariable;
import com.ibm.debug.model.Storage;


public class DisableStorageMonitorAction extends Action implements SelectionListener, DisposeListener {
	protected static final String PREFIX= "DisableStorageMonitorAction.";
	private TabFolder tabFolder;


	/**
	 * Constructor for DisableStorageMonitorAction
	 */
	public DisableStorageMonitorAction(TabFolder tabFolder) {
		super(PICLUtils.getResourceString(PREFIX+"label.disable"));
		setToolTipText(PICLUtils.getResourceString(PREFIX+"tooltip.disable"));
		setEnabled(tabFolder.getItems().length > 0);
		setChecked(false);
		this.tabFolder = tabFolder;
		tabFolder.addSelectionListener(this);

		WorkbenchHelp.setHelp(this, new Object[] { PICLUtils.getHelpResourceString("DisableStorageMonitorAction") });
	}

	/**
	 * @see Action
	 */
	public void setChecked(boolean value) {
		super.setChecked(value);
		setText(value ? PICLUtils.getResourceString(PREFIX+"label.enable") : PICLUtils.getResourceString(PREFIX+"label.disable"));
		setToolTipText(value ? PICLUtils.getResourceString(PREFIX+"tooltip.enable") : PICLUtils.getResourceString(PREFIX+"tooltip.disable"));
	}

	protected void doAction(StorageView view) throws DebugException {
		int index = tabFolder.getSelectionIndex();
		TabItem tab = tabFolder.getItem(index);
		if (isChecked()) {
			((StorageViewTab)tab.getData()).getStorage().disable();
			setChecked(true);
		} else {
			((StorageViewTab)tab.getData()).getStorage().enable();
			setChecked(false);
		}
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
				//DebugUIUtils.logError(e);
				return;
			}
		}

		try {
			doAction(view);
		} catch (DebugException de) {
			//DebugUIUtils.logError(de);
		}
		p.bringToTop(view);
	}


	/**
	 * @see SelectionListener#widgetDefaultSelected(SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent event) {
		//not used
	}


	/**
	 * @see SelectionListener#widgetSelected(SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent event) {
		//event.item - tabitem
		//event.source - tabfolder
		if (event.item.getData() != null) {
		 	PICLStorage pStorage =  ((StorageViewTab)event.item.getData()).getStorage();
		 	if (pStorage.getStorage() != null) {
				setEnabled(true);
		 		if (pStorage.getStorage().isEnabled()) {
					setChecked(false);
		 		} else {
					setChecked(true);
		 		}
		 		return;
		 	}
		 }
		setEnabled(false);
	}


	/**
	 * @see DisposeListener#widgetDisposed(DisposeEvent)
	 */
	public void widgetDisposed(DisposeEvent event) {
		if (tabFolder.getItems().length > 0) {
			int index = tabFolder.getSelectionIndex();
			if (index >= 0) {
				TabItem tab = tabFolder.getItem(index);
				if ((tab != null) && !(tab.isDisposed())) {
					StorageViewTab storageTab = (StorageViewTab)tab.getData();
					if (storageTab != null) {
						PICLStorage pStorage = storageTab.getStorage();
						if (pStorage != null) {
							Storage storage = pStorage.getStorage();
							if (storage != null) {
								setEnabled(true);
								if (storage.isEnabled()) {
									setChecked(false);
								} else {
									setChecked(true);
								}
								return;
							}
						}
					}
				}
			}
		}
		setEnabled(false);
	}
}
