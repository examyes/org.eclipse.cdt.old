package com.ibm.debug.internal.pdt.ui.actions;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/actions/AddRegisterStorageMonitorAction.java, eclipse, eclipse-dev, 20011128
// Version 1.9 (last modified 11/28/01 15:59:41)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DebugView;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import com.ibm.debug.WorkspaceSourceLocator;
import com.ibm.debug.internal.pdt.ui.views.StorageView;
import com.ibm.debug.internal.picl.IPICLDebugConstants;
import com.ibm.debug.internal.picl.IRegister;
import com.ibm.debug.internal.picl.PICLDebugElement;
import com.ibm.debug.internal.picl.PICLException;
import com.ibm.debug.internal.picl.PICLThread;

public class AddRegisterStorageMonitorAction implements IViewActionDelegate {

	ISelection currentSelection;
	StorageView view;
	IWorkbenchPage p;

	/**
	 * @see IViewActionDelegate#init(IViewPart)
	 */
	public void init(IViewPart arg0) {
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction arg0) {

		if (currentSelection instanceof IStructuredSelection)
		{ //multiple items selected
			Object[] selections = ((IStructuredSelection)currentSelection).toArray();
			for (int i = 0; i < selections.length; i++)
			{
				if(!(selections[i] instanceof IRegister))
					continue;  //ignore register groups
				monitorRegister((IRegister) selections[i],(PICLThread)((IRegister)selections[i]).getThread());
			}
		}
		else if (currentSelection instanceof IRegister)
			monitorRegister((IRegister) currentSelection,(PICLThread)((IRegister)currentSelection).getThread());

		p.bringToTop(view); //bring to top when done adding all monitors
		view = null;  //initialize each time
		p=null;
	}

	private boolean monitorRegister(IRegister register, PICLThread threadContext)
	{
		if (view == null && !findStorageMonitorView())
			return false;

		String expression = "";
		try{
			 expression = register.getName();
		}catch(DebugException e){
			MessageDialog.openError(null, "Monitored Expression Failed", "Expression \"" + expression + "\" could not be evaluated in thread \"" + threadContext.getLabel(true) + "\"");
			return false;
		}

		//Need current selected project to be able to set a marker
		IProject project = getCurrentSelectedProject();
		if(project==null)
		{
			MessageDialog.openError(null, "Monitored Expression Failed", "Expression \"" + expression + "\" could not be evaluated in thread \"" + threadContext.getLabel(true) + "\"");
			return false;
		}
		// create a marker on the resource and set the line number where the expression is to be evaluated
		IMarker monitorMarker = null;
		try {
			monitorMarker = project.createMarker(IPICLDebugConstants.PICL_MONITORED_REGISTER);
		} catch (CoreException ce) {
			MessageDialog.openError(null, "Monitored Expression Failed", "Expression \"" + expression + "\" could not be evaluated in thread \"" + threadContext.getLabel(true) + "\"");
			return false;
		}

		try {
			threadContext.monitorStorage(monitorMarker, expression, null);
		} catch (PICLException pe) {
			MessageDialog.openError(null, "Monitored Expression Failed", "Expression \"" + expression + "\" could not be evaluated in thread \"" + threadContext.getLabel(true) + "\"");
			return false;
		}


		return true;

//		MonitorItem item;
//		try{
//			item = new MonitorItem(register.getName(),register.getValue(),
//									  (Composite)view.getViewer().getControl());
//		}catch(DebugException e){ return; }
//		item = new MonitorItem(register);
//		view.addToMonitor(item);
	}

	private boolean findStorageMonitorView()
	{
		p= DebugUIPlugin.getActiveWorkbenchWindow().getActivePage();
		if (p == null) {
			return false;
		}
		view = (StorageView) p.findView("com.ibm.debug.pdt.ui.StorageView");
		if (view == null) {
			// open a new view
			try {
				IWorkbenchPart activePart= p.getActivePart();
				view= (StorageView) p.showView("com.ibm.debug.pdt.ui.StorageView");
				p.activate(activePart);
			} catch (PartInitException e) {
				//DebugUIUtils.logError(e);
				return false;
			}
		}
		return true;
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction arg0, ISelection arg1)
	{	//only enable action if at least one IRegister is selected
		//done through XML instead
		/*if (arg1 instanceof IStructuredSelection)
		{
			boolean flag=false; //action enablement
			Object[] selections = ((IStructuredSelection)arg1).toArray();
			for (int i = 0; i < selections.length; i++)
			{
				if(selections[i] instanceof IRegister)
				{
					flag = true;
				}
			}
			arg0.setEnabled(flag);
		}
		else if(arg1 instanceof IRegister)
		  	arg0.setEnabled(true);
		else
			arg0.setEnabled(false);
			*/
		currentSelection = arg1;
	}

	private IProject getCurrentSelectedProject()
	{
		IWorkbenchWindow window= DebugUIPlugin.getActiveWorkbenchWindow();
		if (window == null)
			return null;
		IWorkbenchPage p= window.getActivePage();
		if (p == null)
			return null;

		DebugView view= (DebugView) p.findView(IDebugUIConstants.ID_DEBUG_VIEW);

		if (view != null)
		{
			ISelectionProvider provider= view.getSite().getSelectionProvider();
			if (provider != null)
			{
				ISelection selection= provider.getSelection();
				if (!selection.isEmpty() && selection instanceof IStructuredSelection)
				{
					Object firstElement = ((IStructuredSelection)selection).getFirstElement();
					if(firstElement != null && firstElement instanceof PICLDebugElement)
					{
						ISourceLocator locator = ((PICLDebugElement)firstElement).getDebugTarget().getLaunch().getSourceLocator();
						if(locator instanceof WorkspaceSourceLocator)
							return ((WorkspaceSourceLocator)locator).getHomeProject();


					}
				}


			}
		}

		return null;
	}
}

