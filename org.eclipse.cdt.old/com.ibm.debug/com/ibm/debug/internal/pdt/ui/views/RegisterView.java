package com.ibm.debug.internal.pdt.ui.views;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/views/RegisterView.java, eclipse, eclipse-dev, 20011128
// Version 1.26 (last modified 11/28/01 15:59:42)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.ui.AbstractDebugView;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DebugView;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.help.ViewContextComputer;
import org.eclipse.ui.help.WorkbenchHelp;

import com.ibm.debug.internal.pdt.ui.actions.AddRegisterMonitorAction;
import com.ibm.debug.internal.pdt.ui.actions.CopyTreeViewToClipboardAction;
import com.ibm.debug.internal.pdt.ui.actions.EditValueAction;
import com.ibm.debug.internal.pdt.ui.actions.PrintTreeViewAction;
import com.ibm.debug.internal.picl.IPICLDebugConstants;
import com.ibm.debug.internal.picl.IRegister;
import com.ibm.debug.internal.picl.IRegisterGroup;
import com.ibm.debug.internal.picl.PICLDebugElement;
import com.ibm.debug.internal.picl.PICLModelPresentation;
import com.ibm.debug.internal.picl.PICLRegisterGroupParent;
import com.ibm.debug.internal.picl.PICLThread;
import com.ibm.debug.internal.picl.PICLUtils;

public class RegisterView extends AbstractDebugView implements ISelectionListener, ITreeViewerListener, IDoubleClickListener{

	protected final static String PREFIX= "RegistersView.";

	protected EditValueAction editAction;
	protected CopyTreeViewToClipboardAction clipboardAction;
	protected PrintTreeViewAction printAction;
	protected AddRegisterMonitorAction monitorAction;
	private RegisterContentProvider contentProvider;
	private PICLThread thread;


	/**
	 * Configures the toolBar.
	 * Adds two empty groups - IPICLDebugConstants.EMPTY_REGISTER_GROUP and
	 * IWorkbenchActionConstants.MB_ADDITIONS
	 */
	protected void configureToolBar(IToolBarManager tbm) {
		tbm.add(new Separator(IPICLDebugConstants.EMPTY_REGISTER_GROUP));
		tbm.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		tbm.add(clipboardAction);
		tbm.add(printAction);	
	}
	/**
	 * @see IWorkbenchPart
	 */
	public void createPartControl(Composite parent) {

		DebugUIPlugin.getDefault().addSelectionListener(this);
		fViewer = new TreeViewer(parent, SWT.MULTI);
		((TreeViewer)fViewer).addTreeListener(this);
		fViewer.addDoubleClickListener(this);
		fViewer= fViewer;
		fViewer.setContentProvider(contentProvider = new RegisterContentProvider());
		fViewer.setLabelProvider(new PICLModelPresentation());
		fViewer.setUseHashlookup(true);

		// add a context menu
		createContextMenu(((TreeViewer)fViewer).getTree());

		initializeActions();
		initializeToolBar();

		setInitialContent();
		setTitleToolTip(PICLUtils.getResourceString(PREFIX+"titleToolTipText"));
		WorkbenchHelp.setHelp(parent, new ViewContextComputer(this,
			PICLUtils.getHelpResourceString("RegisterView") ));
	}
	/**
	 * Remove myself as a selection listener to the <code>LaunchesView</code> in this perspective.
	 *
	 * @see IWorkbenchPart
	 */
	public void dispose() {
		DebugUIPlugin.getDefault().removeSelectionListener(this);
		super.dispose();
	}

	public void partClosed(IWorkbenchPart part )
	{
		if(!(part instanceof RegisterView))
			return;
		stopMonitoringAllGroups();
		DebugUIPlugin.getDefault().removeSelectionListener(this);
		try{
			if(fViewer != null)
			{
				((TreeViewer)fViewer).removeTreeListener(this);
				((TreeViewer)fViewer).removeDoubleClickListener(this);
			}
		}catch(Exception e) {}

		super.partClosed(part);
	}

  /**
	* Adds items to the context menu including any extension defined actions.
	*/
	protected void fillContextMenu(IMenuManager menu) {
		ISelection selection = getViewer().getSelection();
		if(selection != null)
		{
			if(selection instanceof IStructuredSelection)
			{ //multiple items selected	
				Object[] selections = ((IStructuredSelection)selection).toArray();	
				int count =0;		
				//check if all items in selection support editing
				for (int i = 0; i < selections.length; i++)	
				{
					if(selections[i] == null)
						continue;
					if(selections[i] instanceof IRegister && ((IRegister)selections[i]).supportsValueModification())
						count++;
				}
				if(count == selections.length)
					menu.add(editAction);
			}
			else if(selection instanceof IRegister)
				menu.add(editAction);
		}
		menu.add(new Separator(IPICLDebugConstants.REGISTER_GROUP));		
		menu.add(new Separator(IPICLDebugConstants.EMPTY_REGISTER_GROUP));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	/**
	 * Initializes the actions of this view.
	 */
	protected void initializeActions() {
		editAction= new EditValueAction(fViewer);
		editAction.setEnabled(false);

		clipboardAction= new CopyTreeViewToClipboardAction(fViewer);
		clipboardAction.setHoverImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_CLCL_COPY_VIEW_TO_CLIPBOARD));
		clipboardAction.setDisabledImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_DLCL_COPY_VIEW_TO_CLIPBOARD));
		clipboardAction.setImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_ELCL_COPY_VIEW_TO_CLIPBOARD));

		printAction = new PrintTreeViewAction(fViewer, PICLUtils.getResourceString(PREFIX+"printjobtitle"));
		printAction.setHoverImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_CLCL_PRINT_VIEW));
		printAction.setDisabledImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_DLCL_PRINT_VIEW));
		printAction.setImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_ELCL_PRINT_VIEW));

	}
	/**
	 * The <code>RegisterView</code> listens for selection changes in the <code>DebugView</code>
	 * and the RegisterView.
	 *
	 * @see ISelectionListener
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection sel) {

		if(part instanceof RegisterView && sel instanceof IRegister)
		{
			if( ((IRegister)sel).supportsValueModification())
				editAction.setEnabled(true);
			else editAction.setEnabled(false);
		}
		if ((part instanceof DebugView))
			setViewerInput((IStructuredSelection)sel);

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
					setViewerInput((IStructuredSelection) selection);
				}
			}
		}
	}
	protected void setViewerInput(IStructuredSelection ssel) {

		thread = null;

		if(fViewer == null  || fViewer.getContentProvider() == null)
			return;

		if(ssel == null)
		{
			fViewer.setInput(null);
			return;
		}
		//if change, remember current expansion for next time
		Object current= fViewer.getInput();
		if (current !=null && current instanceof PICLRegisterGroupParent)
			((PICLRegisterGroupParent)current).setExpandedElements(((TreeViewer)fViewer).getExpandedElements());

		if(ssel.size() == 1)
		{
			Object firstElement = ssel.getFirstElement();

			if(firstElement == null)
			{
				fViewer.setInput(null);
				return;
			}

			if(firstElement instanceof PICLDebugElement)
			{
				if(firstElement instanceof IThread)
					thread = (PICLThread) firstElement;
				else //find the thread for this item
				{
					IThread ithread=((PICLDebugElement)firstElement).getThread();
					if(ithread instanceof PICLThread)
						thread = (PICLThread)ithread;
				}
			}
			else //clear view if picl thread not selected
			{
				fViewer.setInput(null);
				return;
			}
		}
		else //multiple selection not supported, so clear view
		{
			fViewer.setInput(null);
			return;
		}

		if (thread == null) //no thread selected, so clear view
		{
			fViewer.setInput(null);
			return;
		}

		PICLRegisterGroupParent parent = thread.getRegisterGroupParent();
		if(parent==null)
		{
			fViewer.setInput(null);
			return;
		}
		//check if there has been a change of parent
		//if not, return and leave view as is
		if (current != null && current.equals(parent)) {
			return;
		}

		//set new parent as input to view
		fViewer.setInput(parent);
		//restore expansion, if previous value stored
		//this will make it appear as it did last time the user looked at it
		if(parent.getExpandedElements()!= null)
			((TreeViewer)fViewer).setExpandedElements(parent.getExpandedElements());


	}




	/**
	 * @see ITreeViewerListener#treeCollapsed(TreeExpansionEvent)
	 * Tells the model to stop monitoring the registers when the tree
	 * collapses and the registers are no longer visible.
	 */
	public void treeCollapsed(TreeExpansionEvent event) {

		if( ((TreeExpansionEvent)event).getElement() instanceof IRegisterGroup)
				((IRegisterGroup)((TreeExpansionEvent)event).getElement()).stopMonitoringRegisterGroup();
	}


	/**
	 * @see ITreeViewerListener#treeExpanded(TreeExpansionEvent)
	 * Event happens after doGetChildren - therefore not useful for monitoring.
	 * Monitoring is instead done when the tree calls doGetChildren().
	 * @see RegisterContentProvider#doGetChildren()
	 */
	public void treeExpanded(TreeExpansionEvent event) {

		if(((TreeExpansionEvent)event).getElement() instanceof IRegisterGroup)
		{
			if(  !((IRegisterGroup)(((TreeExpansionEvent)event).getElement())).isMonitored()  )
				((IRegisterGroup)((TreeExpansionEvent)event).getElement()).startMonitoringRegisterGroup();

		}

	}

	/**
	 * For register groups, this method will expand/collapse the group.
	 * For registers, this method will invoke the editing action.
	 *
	 * @see IDoubleClickListener
	 */
	public void doubleClick(DoubleClickEvent event) {
		IStructuredSelection selection= (IStructuredSelection) fViewer.getSelection();

		if (selection.size() != 1) 	//Single selection only
			return;

		//Get the selected register/group
		IDebugElement source = (IDebugElement) selection.getFirstElement();
		if(source instanceof IRegisterGroup)
		{ //collapse the register group
			if (((TreeViewer)fViewer).getExpandedState(source))
			{
				((IRegisterGroup)source).stopMonitoringRegisterGroup();
				((TreeViewer)fViewer).collapseToLevel(source,1);
			}
			else //expand the group
			{
				if( ! ((IRegisterGroup)source).isMonitored()  )
					((IRegisterGroup)source).startMonitoringRegisterGroup();
				((TreeViewer)fViewer).expandToLevel(source,1);
			}
		}
		else if(source instanceof IRegister && ((IRegister)source).supportsValueModification())  //edit the value of the register
        	editAction.run();

	}
	/**
	 * Returns the PICLThread currently used as the basis for the view.
	 * If the view is empty, null will be returned.
	 */
	public PICLThread getCurrentThread()
	{
		return thread;
	}
	/**
	 * Stop monitoring all register groups.
	 * @see CollapseAllAction
	 */
	public void	stopMonitoringAllGroups()
	{
		try{
			IDebugElement[] groups = getCurrentThread().getRegisterGroupParent().getChildren();
			for (int i=0; i< groups.length; i++)
			{
				if(((IRegisterGroup)groups[i]).isMonitored())
					((IRegisterGroup)groups[i]).stopMonitoringRegisterGroup();
			}
		}catch(Exception e){}
	}
}

