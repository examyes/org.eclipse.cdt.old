package com.ibm.debug.internal.pdt.ui.views;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/views/MonitorView.java, eclipse, eclipse-dev, 20011128
// Version 1.17 (last modified 11/28/01 15:59:19)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.AbstractDebugView;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DebugView;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.ShowTypesAction;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.help.ViewContextComputer;
import org.eclipse.ui.help.WorkbenchHelp;

import com.ibm.debug.internal.pdt.ui.actions.CopyTreeViewToClipboardAction;
import com.ibm.debug.internal.pdt.ui.actions.DisableVariableMonitorAction;
import com.ibm.debug.internal.pdt.ui.actions.ChangeRepresentationAction;
import com.ibm.debug.internal.pdt.ui.actions.EditValueAction;
import com.ibm.debug.internal.pdt.ui.actions.MonitorExpressionAction;
import com.ibm.debug.internal.pdt.ui.actions.PrintTreeViewAction;
import com.ibm.debug.internal.pdt.ui.actions.RemoveVariableFromMonitorAction;
import com.ibm.debug.internal.picl.IPICLDebugConstants;
import com.ibm.debug.internal.picl.PICLDebugElement;
import com.ibm.debug.internal.picl.PICLDebugTarget;
import com.ibm.debug.internal.picl.PICLModelPresentation;
import com.ibm.debug.internal.picl.PICLMonitorParent;
import com.ibm.debug.internal.picl.PICLUtils;
import com.ibm.debug.internal.picl.PICLVariable;
import com.ibm.debug.model.Representation;


public class MonitorView extends AbstractDebugView implements ISelectionListener, IDoubleClickListener, ITreeViewerListener {
	protected final static String PREFIX= "MonitorView.";
	private TreeViewer fTreeViewer = null;

	protected MonitorContentProvider fContentProvider= null;
	protected EditValueAction fEditVariableValueAction;
	protected MonitorExpressionAction fMonitorExpressionAction;
	protected ShowTypesAction fShowTypesAction;
	protected RemoveVariableFromMonitorAction fRemoveVariableFromMonitorAction;
	protected DisableVariableMonitorAction fDisableVariableMonitorAction;
	protected CopyTreeViewToClipboardAction fCopyTreeViewToClipboardAction;
	protected PrintTreeViewAction fPrintTreeViewAction;


	/**
	 * @see WorkbenchPart#createPartControl(Composite)
	 */
	public void createPartControl(Composite parent) {
		DebugUIPlugin.getDefault().addSelectionListener(this);
		fTreeViewer = new TreeViewer(parent, SWT.MULTI);
		fViewer = fTreeViewer;
		fContentProvider = new MonitorContentProvider();
		fTreeViewer.setContentProvider(fContentProvider);
		fTreeViewer.setLabelProvider(new PICLModelPresentation());
		createContextMenu(fTreeViewer.getTree());
		fTreeViewer.addDoubleClickListener(this);
		fTreeViewer.addTreeListener(this);
		fTreeViewer.getControl().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				handleKeyPressed(e);
			}
		});


		initializeActions();
		initializeToolBar();
		setInitialContent();
		setTitleToolTip(PICLUtils.getResourceString(PREFIX+"tooltip"));

		WorkbenchHelp.setHelp(parent, new ViewContextComputer(this, PICLUtils.getHelpResourceString("MonitorView")));
	}

	protected void initializeActions() {
		fEditVariableValueAction= new EditValueAction(fViewer);
		fEditVariableValueAction.setEnabled(false);

		fMonitorExpressionAction= new MonitorExpressionAction(false);
		fMonitorExpressionAction.setHoverImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_CLCL_MONITOR_EXPRESSION));
		fMonitorExpressionAction.setDisabledImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_DLCL_MONITOR_EXPRESSION));
		fMonitorExpressionAction.setImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_ELCL_MONITOR_EXPRESSION));
		fMonitorExpressionAction.setEnabled(false);

		fShowTypesAction= new ShowTypesAction(fViewer);
		fShowTypesAction.setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_TYPE_NAMES));
		fShowTypesAction.setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_TYPE_NAMES));
		fShowTypesAction.setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_TYPE_NAMES));
		fShowTypesAction.setChecked(false);

		fCopyTreeViewToClipboardAction= new CopyTreeViewToClipboardAction(fViewer);
		fCopyTreeViewToClipboardAction.setHoverImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_CLCL_COPY_VIEW_TO_CLIPBOARD));
		fCopyTreeViewToClipboardAction.setDisabledImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_DLCL_COPY_VIEW_TO_CLIPBOARD));
		fCopyTreeViewToClipboardAction.setImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_ELCL_COPY_VIEW_TO_CLIPBOARD));
		fCopyTreeViewToClipboardAction.setChecked(false);

		fPrintTreeViewAction= new PrintTreeViewAction(fViewer, PICLUtils.getResourceString(PREFIX+"printjobtitle"));
		fPrintTreeViewAction.setHoverImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_CLCL_PRINT_VIEW));
		fPrintTreeViewAction.setDisabledImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_DLCL_PRINT_VIEW));
		fPrintTreeViewAction.setImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_ELCL_PRINT_VIEW));
		fPrintTreeViewAction.setChecked(false);

		fRemoveVariableFromMonitorAction= new RemoveVariableFromMonitorAction(fViewer);
		fRemoveVariableFromMonitorAction.setHoverImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_CLCL_REMOVE_MONITOR));
		fRemoveVariableFromMonitorAction.setDisabledImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_DLCL_REMOVE_MONITOR));
		fRemoveVariableFromMonitorAction.setImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_ELCL_REMOVE_MONITOR));
		fRemoveVariableFromMonitorAction.setChecked(false);

		fDisableVariableMonitorAction= new DisableVariableMonitorAction(fViewer);
		fDisableVariableMonitorAction.setHoverImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_CLCL_DISABLE_MONITOR));
		fDisableVariableMonitorAction.setDisabledImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_DLCL_DISABLE_MONITOR));
		fDisableVariableMonitorAction.setImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_ELCL_DISABLE_MONITOR));
		fDisableVariableMonitorAction.setChecked(false);

	}

	/**
	 * @see AbstractDebugView#fillContextMenu(IMenuManager)
	 */
	protected void fillContextMenu(IMenuManager menu) {

		menu.add(new Separator(this.getClass().getName()));
		menu.add(fEditVariableValueAction);
		menu.add(fMonitorExpressionAction);
		menu.add(fRemoveVariableFromMonitorAction);
		menu.add(fDisableVariableMonitorAction);
		menu.add(new Separator(this.getClass().getName()));
		menu.add(fShowTypesAction);
		menu.add(new Separator(this.getClass().getName()));
		
		//add all "change representation" choices for the selected item (single selection only)
		IStructuredSelection selection= (IStructuredSelection) fTreeViewer.getSelection();
		if (selection != null && !selection.isEmpty() && selection.size() == 1) {
			Object var = selection.getFirstElement();
			if (var instanceof PICLVariable) {
				MenuManager submenu= new MenuManager(PICLUtils.getResourceString(PREFIX+"changerepresentation"), "group.representations");
				Representation reps[] = ((PICLVariable)var).getArrayOfRepresentations();
				if (reps != null) {
					for (int i=0; i<reps.length; i++) {
						submenu.add(new ChangeRepresentationAction((PICLVariable)var, reps[i]));
					}
				}
				menu.add(submenu);
			}
		}
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}


	/**
	 * @see AbstractDebugView#configureToolBar(IToolBarManager)
	 */
	protected void configureToolBar(IToolBarManager tbm) {
		tbm.add(new Separator(this.getClass().getName()));
		tbm.add(fMonitorExpressionAction);
		tbm.add(fRemoveVariableFromMonitorAction);
		tbm.add(fDisableVariableMonitorAction);
		tbm.add(new Separator(this.getClass().getName()));
		tbm.add(fShowTypesAction);
		tbm.add(new Separator(this.getClass().getName()));
		tbm.add(fCopyTreeViewToClipboardAction);
		tbm.add(fPrintTreeViewAction);
		tbm.add(new Separator(this.getClass().getName()));
	}


	protected void setInitialContent() {
		IWorkbenchWindow window= DebugUIPlugin.getActiveWorkbenchWindow();
		if (window == null) {
			return;
		}
		IWorkbenchPage p= window.getActivePage();
		if (p == null) {
			return;
		}
		DebugView view= (DebugView) p.findView(IDebugUIConstants.ID_DEBUG_VIEW);
		if (view != null) {
			ISelectionProvider provider= view.getSite().getSelectionProvider();
			if (provider != null) {
				provider.getSelection();
				ISelection selection= provider.getSelection();
				if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
					selectionChanged(view, (IStructuredSelection) selection);
				}
			}
		}
	}

	/**
	 * @see ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
	 */
	public void selectionChanged(IWorkbenchPart view, ISelection selection) {

		// only if from debug view
		if (!(view instanceof DebugView))
			return;

		if (fViewer == null || fViewer.getContentProvider() == null) {
			return;
		}

		IStructuredSelection ssel = (IStructuredSelection) selection;

		PICLDebugElement de = null;

		// check if the selection is a PICLDebugElement
		if ((ssel.getFirstElement() instanceof PICLDebugElement))
			de = (PICLDebugElement)ssel.getFirstElement();
		else
			if ((ssel.getFirstElement() instanceof Launch)) {
				Launch l = (Launch)ssel.getFirstElement();
				if (l.getDebugTarget() instanceof PICLDebugElement)
					de = (PICLDebugTarget)l.getDebugTarget();
			}

		if (de == null)  // nothing selected that the monitor view can show
			fViewer.setInput(null);
		else {
			// check to see if a change is required

			PICLMonitorParent monitorParent = ((PICLDebugTarget)de.getDebugTarget()).getMonitorParent();

			if (fViewer.getInput() != null && fViewer.getInput().equals(monitorParent)) { // no change required because it matches
				fViewer.refresh();
				return;
			} else {
				//save the current tree expanded state in the current monitorparent
				Object current = fTreeViewer.getInput();
				if (current !=null && current instanceof PICLMonitorParent)
					((PICLMonitorParent)current).setExpandedElements(fTreeViewer.getExpandedElements());
				//set the input to the new monitorparent
				fTreeViewer.setInput(monitorParent);
				//restore any previously expanded state of the new parent
				if(monitorParent != null && monitorParent.getExpandedElements() != null)
					fTreeViewer.setExpandedElements(monitorParent.getExpandedElements());
			}
		}


	}
	/**
	 * Removes items from the list
	 */
	public void removeFromMonitor(Object object) {
	}


	/**
	 * @see IDoubleClickListener#doubleClick(DoubleClickEvent)
	 */
	public void doubleClick(DoubleClickEvent event) {
    	IStructuredSelection selection = (IStructuredSelection)event.getSelection();
		if (selection.size() != 1) 	//Single selection only
			return;

		//Get the selected monitored variable
		IDebugElement source = (IDebugElement) selection.getFirstElement();
		if(! (source instanceof IVariable)) { return; }
		try {
			if (((IVariable)source).hasChildren()) { //expand/collapse
				if (fTreeViewer.getExpandedState(source))
					fTreeViewer.collapseToLevel(source,1);
				else
					fTreeViewer.expandToLevel(source,1);
			} else { //edit the value
				fEditVariableValueAction.run();
			}
		} catch (DebugException de) {}
	}

	/**
	 * Handles key events in viewer.  Specifically interested in
	 * the Delete key.
	 */
	protected void handleKeyPressed(KeyEvent event) {
		if (event.character == SWT.DEL && event.stateMask == 0
			&& fRemoveVariableFromMonitorAction.isEnabled()) {
				fRemoveVariableFromMonitorAction.run();
		}
	}


	/**
	 * Remove myself as a selection listener to the <code>LaunchesView</code> in this perspective.
	 *
	 * @see IWorkbenchPart
	 */
	public void dispose() {
		DebugUIPlugin.getDefault().removeSelectionListener(this);
		//exception occurs sometimes when tree already disposed
		try{
			if (fTreeViewer != null) {
				fTreeViewer.removeTreeListener(this);
				fTreeViewer.removeDoubleClickListener(this);
			}
		}catch(Exception e) {}
		super.dispose();
	}

	public void partClosed(IWorkbenchPart part) {
		if (!(part instanceof MonitorView))
			return;
		//TODO: stop add/update events from the model
		DebugUIPlugin.getDefault().removeSelectionListener(this);
		try {
			if (fTreeViewer != null) {
				fTreeViewer.removeTreeListener(this);
				fTreeViewer.removeDoubleClickListener(this);
			}
		} catch (Exception e) {}
		super.partClosed(part);
	}


	/**
	 * @see ITreeViewerListener#treeCollapsed(TreeExpansionEvent)
	 */
	public void treeCollapsed(TreeExpansionEvent event) {
	}
	/**
	 * @see ITreeViewerListener#treeExpanded(TreeExpansionEvent)
	 */
	public void treeExpanded(TreeExpansionEvent event) {
	}
}
