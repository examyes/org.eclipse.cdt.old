package com.ibm.debug.internal.pdt.ui.views;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/views/ModulesView.java, eclipse, eclipse-dev, 20011128
// Version 1.9 (last modified 11/28/01 15:59:55)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.internal.picl.PICLDebugElement;
import com.ibm.debug.internal.picl.PICLDebugTarget;
import com.ibm.debug.internal.picl.PICLModelPresentation;
import com.ibm.debug.internal.picl.PICLModuleParent;
import com.ibm.debug.internal.picl.PICLUtils;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.internal.ui.AbstractDebugView;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DebugView;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

public class ModulesView extends AbstractDebugView implements ISelectionListener, IDoubleClickListener {


	private TreeViewer fTreeViewer = null;

	/**
	 * Constructor for ModulesView
	 */
	public ModulesView() {
		super();
		PICLUtils.logText("In constructor of ModulesView");
	}

	/**
	 * @see WorkbenchPart#createPartControl(Composite)
	 */
	public void createPartControl(Composite parent) {
		PICLUtils.logText("ModulesView.createPartControl()");

		// add this view as a listener of the main debug view selection events
		DebugUIPlugin.getDefault().addSelectionListener(this);
		fTreeViewer = new TreeViewer(parent, SWT.MULTI);
		fViewer = fTreeViewer;
		fTreeViewer.setContentProvider(new ModulesContentProvider());
		fTreeViewer.setLabelProvider(new PICLModelPresentation());
		fTreeViewer.addDoubleClickListener(this);

	}

	public void partClosed(IWorkbenchPart part )
	{
		if(!(part instanceof ModulesView))
			return;
		//TODO: stop add/update events from model
		DebugUIPlugin.getDefault().removeSelectionListener(this);
		try{
			if(fViewer != null)
				((TreeViewer)fViewer).removeDoubleClickListener(this);
		}catch(Exception e) {}

		super.partClosed(part);
	}

	/**
	 * @see AbstractDebugView#fillContextMenu(IMenuManager)
	 */
	protected void fillContextMenu(IMenuManager arg0) {
		PICLUtils.logText("ModulesView.fillContextMenu()");
	}

	/**
	 * @see AbstractDebugView#configureToolBar(IToolBarManager)
	 */
	protected void configureToolBar(IToolBarManager arg0) {
		PICLUtils.logText("ModulesView.configureToolBar()");
	}

	/**
	 * @see ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
	 */
	public void selectionChanged(IWorkbenchPart view, ISelection selection) {
		PICLUtils.logText("ModulesView.selectionChanged()");


		// only if from debug view
		if (!(view instanceof DebugView))
			return;

		if(fViewer == null  || fViewer.getContentProvider() == null)
			return;

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

		if (de == null)  // nothing selected that the modules view can show
			fViewer.setInput(null);
		else {
			// check to see if a change is required

			PICLModuleParent moduleParent = ((PICLDebugTarget)de.getDebugTarget()).getModuleParent();
			TreeViewer treeViewer = (TreeViewer)fViewer;

			if (treeViewer.getInput() != null && treeViewer.getInput().equals(moduleParent)) { // no change required because it matches
				treeViewer.refresh();
				return;
			} else {
				// first store off the expanded setting of the current tree
				if (treeViewer.getInput() != null)
					((PICLModuleParent)treeViewer.getInput()).saveExpandedElements(treeViewer.getExpandedElements());
				// set it to the new module parent
				treeViewer.setInput(moduleParent);
				// restore expanded setting
				if (moduleParent != null && moduleParent.getExpandedElements() != null)
					treeViewer.setExpandedElements(moduleParent.getExpandedElements());
			}
		}

	}

    /**
     * @see IDoubleClickListener#doubleClick(DoubleClickEvent)
     */
    public void doubleClick(DoubleClickEvent event) {
    	IStructuredSelection structuredSelection = (IStructuredSelection)event.getSelection();

    	if (structuredSelection.size() != 1)   // only expand/collapse if 1 selected
    		return;

    	Object selection = structuredSelection.getFirstElement();

    	if (fTreeViewer.getExpandedState(selection))
   			fTreeViewer.collapseToLevel(selection,1);
   		else
   			fTreeViewer.expandToLevel(selection,1);

    }

}