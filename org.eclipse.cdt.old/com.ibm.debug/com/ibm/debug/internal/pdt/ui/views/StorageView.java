package com.ibm.debug.internal.pdt.ui.views;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/views/StorageView.java, eclipse, eclipse-dev
// Version 1.19 (last modified 11/28/01 16:00:51)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventListener;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.internal.ui.AbstractDebugView;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DebugView;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.help.ViewContextComputer;
import org.eclipse.ui.help.WorkbenchHelp;

import com.ibm.debug.internal.pdt.ui.actions.CopyStorageToolbarAction;
import com.ibm.debug.internal.pdt.ui.actions.MonitorExpressionAction;
import com.ibm.debug.internal.pdt.ui.actions.PrintStorageToolbarAction;
import com.ibm.debug.internal.pdt.ui.actions.RemoveStorageMonitorAction;
import com.ibm.debug.internal.pdt.ui.actions.ResetStorageMonitorAction;
import com.ibm.debug.internal.picl.IPICLDebugConstants;
import com.ibm.debug.internal.picl.PICLDebugElement;
import com.ibm.debug.internal.picl.PICLDebugTarget;
import com.ibm.debug.internal.picl.PICLStorage;
import com.ibm.debug.internal.picl.PICLThread;
import com.ibm.debug.internal.picl.PICLUtils;

public class StorageView extends AbstractDebugView implements ISelectionListener, IDebugEventListener {

	protected final static String PREFIX= "StorageView.";
	private TabFolder emptyTabFolder;
	private Hashtable tabFolderHashtable;
	private Composite parent;
	private StackLayout stackLayout;
	private MonitorExpressionAction fMonitorExpressionAction;
	private RemoveStorageMonitorAction fRemoveStorageMonitorAction;
//	private DisableStorageMonitorAction fDisableStorageMonitorAction;
	private ResetStorageMonitorAction fResetStorageMonitorAction;

	private CopyStorageToolbarAction fCopyStorageToolbarAction;
	private PrintStorageToolbarAction fPrintStorageToolbarAction;

	/**
	 * @see WorkbenchPart#createPartControl(Composite)
	 */
	public void createPartControl(Composite parent) {

		this.parent = parent;
		stackLayout = new StackLayout();
		parent.setLayout(stackLayout);
		DebugUIPlugin.getDefault().addSelectionListener(this);
		DebugPlugin.getDefault().addDebugEventListener(this);

		emptyTabFolder = new TabFolder(parent, SWT.NULL);
		stackLayout.topControl = emptyTabFolder;

		tabFolderHashtable = new Hashtable(3);
		initializeActions();
		final IToolBarManager tbm= getViewSite().getActionBars().getToolBarManager();
		configureToolBar(tbm);
		getViewSite().getActionBars().updateActionBars();

		WorkbenchHelp.setHelp(parent, new ViewContextComputer(this, PICLUtils.getHelpResourceString("StorageView")));
	}


	private void createStorageViewTab(PICLStorage storage) {
		TabFolder tabFolder = (TabFolder)stackLayout.topControl;
		if (tabFolder == emptyTabFolder) {
			//if we're here, the storage view was just opened and selectionChanged() hasn't had a chance to run
			//force selectionChanged() so the tab doesn't get added to the emptyTabFolder
			IWorkbenchPage p= DebugUIPlugin.getActiveWorkbenchWindow().getActivePage();
			if (p == null) { return; }
			DebugView view= (DebugView) p.findView(IDebugUIConstants.ID_DEBUG_VIEW);
			if (view == null) {
				try {
					IWorkbenchPart activePart= p.getActivePart();
					view= (DebugView) p.showView(IDebugUIConstants.ID_DEBUG_VIEW);
					p.activate(activePart);
				} catch (PartInitException e) {
					//DebugUIUtils.logError(e);
					return;
				}
			}
			selectionChanged(view, view.getViewer().getSelection());
			tabFolder = (TabFolder)stackLayout.topControl;
		}

		TabItem tab = new TabItem(tabFolder, SWT.NULL);
		StorageViewTab storageTab = new StorageViewTab(storage, tab);
		tab.setControl(storageTab.createFolderPage(tabFolder));
		((Table)tab.getControl()).setTopIndex(storageTab.TABLE_PREBUFFER);
		storageTab.resizeTable();
		String expression = storage.getStorage().getExpression();
		int indexOfAmpersand = expression.indexOf("&", 0);
		while (indexOfAmpersand >= 0) {
			expression = expression.substring(0, indexOfAmpersand) + "&" + expression.substring(indexOfAmpersand, expression.length());
			indexOfAmpersand += 2;
			indexOfAmpersand = expression.indexOf("&", indexOfAmpersand);
		}
		PICLDebugTarget dbgtarget = (PICLDebugTarget)storage.getDebugTarget();
		PICLThread pThread = dbgtarget.getPICLThread(storage.getStorage().getExpressionThread());
		if (pThread != null) {
			tab.setText(expression + " - " + PICLUtils.getResourceString(PREFIX+"thread") + ": " + pThread.getName());
		} else {
			tab.setText(expression);
		}

//		fDisableStorageMonitorAction.setEnabled(true);
//		fDisableStorageMonitorAction.setChecked(false);
		fRemoveStorageMonitorAction.setEnabled(false);
		fResetStorageMonitorAction.setEnabled(true);
		fCopyStorageToolbarAction.setEnabled(true);
		fPrintStorageToolbarAction.setEnabled(true);

		tabFolder.setSelection(tabFolder.indexOf(tab));
	}

	private void initializeActions() {
		fMonitorExpressionAction = new MonitorExpressionAction(true);
		fMonitorExpressionAction.setHoverImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_CLCL_MONITOR_EXPRESSION));
		fMonitorExpressionAction.setDisabledImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_DLCL_MONITOR_EXPRESSION));
		fMonitorExpressionAction.setImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_ELCL_MONITOR_EXPRESSION));
		fMonitorExpressionAction.setEnabled(false);

		fRemoveStorageMonitorAction = new RemoveStorageMonitorAction();
		fRemoveStorageMonitorAction.setHoverImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_CLCL_REMOVE_STORAGE));
		fRemoveStorageMonitorAction.setDisabledImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_DLCL_REMOVE_STORAGE));
		fRemoveStorageMonitorAction.setImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_ELCL_REMOVE_STORAGE));
		fRemoveStorageMonitorAction.setEnabled(false);

//		fDisableStorageMonitorAction = new DisableStorageMonitorAction();
//		fDisableStorageMonitorAction.setHoverImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_CLCL_DISABLE_STORAGE));
//		fDisableStorageMonitorAction.setDisabledImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_DLCL_DISABLE_STORAGE));
//		fDisableStorageMonitorAction.setImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_ELCL_DISABLE_STORAGE));
//		fDisableStorageMonitorAction.setEnabled(false);

		fResetStorageMonitorAction = new ResetStorageMonitorAction();
		fResetStorageMonitorAction.setHoverImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_CLCL_STORAGE_RESET));
		fResetStorageMonitorAction.setDisabledImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_DLCL_STORAGE_RESET));
		fResetStorageMonitorAction.setImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_ELCL_STORAGE_RESET));
		fResetStorageMonitorAction.setEnabled(false);
		
		fCopyStorageToolbarAction = new CopyStorageToolbarAction();
		fCopyStorageToolbarAction.setHoverImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_CLCL_COPY_VIEW_TO_CLIPBOARD));
		fCopyStorageToolbarAction.setDisabledImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_DLCL_COPY_VIEW_TO_CLIPBOARD));
		fCopyStorageToolbarAction.setImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_ELCL_COPY_VIEW_TO_CLIPBOARD));
		fCopyStorageToolbarAction.setEnabled(false);

		fPrintStorageToolbarAction = new PrintStorageToolbarAction(PICLUtils.getResourceString(PREFIX+"printjobtitle"));
		fPrintStorageToolbarAction.setHoverImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_CLCL_PRINT_VIEW));
		fPrintStorageToolbarAction.setDisabledImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_DLCL_PRINT_VIEW));
		fPrintStorageToolbarAction.setImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_ELCL_PRINT_VIEW));
		fPrintStorageToolbarAction.setEnabled(false);
	}
	
	/**
	 * @see AbstractDebugView#fillContextMenu(IMenuManager)
	 */
	protected void fillContextMenu(IMenuManager menu) { }

	/**
	 * @see AbstractDebugView#configureToolBar(IToolBarManager)
	 */
	protected void configureToolBar(IToolBarManager tbm) {
		tbm.add(new Separator(this.getClass().getName()));
		tbm.add(fMonitorExpressionAction);
		tbm.add(fRemoveStorageMonitorAction);
//		tbm.add(fDisableStorageMonitorAction);
		tbm.add(fResetStorageMonitorAction);
		tbm.add(new Separator(this.getClass().getName()));
		tbm.add(fCopyStorageToolbarAction);
		tbm.add(fPrintStorageToolbarAction);
		tbm.add(new Separator(this.getClass().getName()));
		tbm.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	public StorageViewTab getTopStorageTab() {
		TabFolder folder = (TabFolder)stackLayout.topControl;
		int index = folder.getSelectionIndex();
		if (index >= 0) {
			TabItem tab = folder.getItem(index);
			return (StorageViewTab)tab.getData();
		}
		return null;
	}

	public void setFocus() {
		stackLayout.topControl.setFocus();
	}

	public void dispose() {
		DebugUIPlugin.getDefault().removeSelectionListener(this);
		DebugPlugin.getDefault().removeDebugEventListener(this);
		
		if (tabFolderHashtable != null) {
			Enumeration allDbgTargets = tabFolderHashtable.keys();
			List piclStorageMonitors;
			PICLStorage pStorage;
			while (allDbgTargets.hasMoreElements()) {
				PICLDebugTarget dbgtarget = (PICLDebugTarget)allDbgTargets.nextElement();
				if (!dbgtarget.isTerminated()) {
					piclStorageMonitors = ((PICLDebugTarget)dbgtarget).getStorageParent().getChildrenAsList();
					Object iter[] = piclStorageMonitors.toArray();
					for (int i = 0; i < iter.length; i++) {
						pStorage = (PICLStorage)iter[i];
						pStorage.delete();
					}
				}
			}
		}
		super.dispose();
	}

	public void partClosed(IWorkbenchPart part) {
		if (!(part instanceof StorageView))
			return;
		//TODO: stop add/update events from the model
		DebugUIPlugin.getDefault().removeSelectionListener(this);
		DebugPlugin.getDefault().removeDebugEventListener(this);
		
		if (tabFolderHashtable != null) {
			Enumeration allDbgTargets = tabFolderHashtable.keys();
			List piclStorageMonitors;
			PICLStorage pStorage;
			while (allDbgTargets.hasMoreElements()) {
				PICLDebugTarget dbgtarget = (PICLDebugTarget)allDbgTargets.nextElement();
				if (!dbgtarget.isTerminated()) {
					piclStorageMonitors = ((PICLDebugTarget)dbgtarget).getStorageParent().getChildrenAsList();
					Object iter[] = piclStorageMonitors.toArray();
					for (int i = 0; i < iter.length; i++) {
						pStorage = (PICLStorage)iter[i];
						pStorage.delete();
					}
				}
			}
		}
		super.partClosed(part);
	}

	public void handleDebugEvent(DebugEvent event) {
		if (! (event.getSource() instanceof PICLStorage)) { return; }
		switch (event.getKind()) {
			case DebugEvent.CREATE:
				createStorageViewTab((PICLStorage)event.getSource());
				break;
			case DebugEvent.TERMINATE:
				if ((event.getSource() instanceof PICLDebugTarget) && (tabFolderHashtable.containsKey(event.getSource()))) {
					tabFolderHashtable.remove(event.getSource());
				}
				break;
			default:
				break;
		}
	}

	/**
	 * @see ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
	 */
	public void selectionChanged(IWorkbenchPart view, ISelection selection) {

		IStructuredSelection ssel = (IStructuredSelection) selection;

		//only single selection of PICLDebugElements is allowed for this action
		if (selection == null || selection.isEmpty() || ((IStructuredSelection)selection).size() > 1 ||
					view == null || !(view instanceof DebugView))
		{
			emptyFolderAndDisableActions();
			return;
		}

		Object elem = ((IStructuredSelection)selection).getFirstElement();

		//Launches are not PICLDebugElements, but their debugtarget may be
		if (elem instanceof Launch) {
			elem = ((Launch)elem).getDebugTarget();
		}

		//this action is only valid for PICLDebugElements
		if (! (elem instanceof PICLDebugElement) ) {
			emptyFolderAndDisableActions();
			return;
		}

		//any PICLDebugElement can get its debugtarget
		IDebugTarget dbgtarget = ((PICLDebugElement)elem).getDebugTarget();

		if (dbgtarget == null || !(dbgtarget instanceof PICLDebugTarget) || dbgtarget.isTerminated()) {
			emptyFolderAndDisableActions();
			return;
		}

		//if we've got a tabfolder to go with the debugtarget, display it
		if (tabFolderHashtable.containsKey(dbgtarget)) {
			if (stackLayout.topControl != (TabFolder)tabFolderHashtable.get(dbgtarget)) {
				stackLayout.topControl = (TabFolder)tabFolderHashtable.get(dbgtarget);
				parent.layout();
			}
		} else {	//otherwise, add a new one
			tabFolderHashtable.put(dbgtarget, new TabFolder(parent, SWT.NULL));
			stackLayout.topControl = (TabFolder)tabFolderHashtable.get(dbgtarget);
			parent.layout();
		}

		//set toolbar actions enabled/disabled
		TabFolder folder = (TabFolder)stackLayout.topControl;
		int index = folder.getSelectionIndex();
		if (index >= 0) {
			fResetStorageMonitorAction.setEnabled(true);
			fRemoveStorageMonitorAction.setEnabled(false);
			fCopyStorageToolbarAction.setEnabled(true);
			fPrintStorageToolbarAction.setEnabled(true);
		} else {
			fResetStorageMonitorAction.setEnabled(false);
			fRemoveStorageMonitorAction.setEnabled(false);
			fCopyStorageToolbarAction.setEnabled(false);
			fPrintStorageToolbarAction.setEnabled(false);
		}
	}

	private void emptyFolderAndDisableActions() {
		fResetStorageMonitorAction.setEnabled(false);
		fRemoveStorageMonitorAction.setEnabled(false);
		fCopyStorageToolbarAction.setEnabled(false);
		fPrintStorageToolbarAction.setEnabled(false);
		stackLayout.topControl = emptyTabFolder;
		if (!parent.isDisposed()) {
			parent.layout();
		}
	}
}


