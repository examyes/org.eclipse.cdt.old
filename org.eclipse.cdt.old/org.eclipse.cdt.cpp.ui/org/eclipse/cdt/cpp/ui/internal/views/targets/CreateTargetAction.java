package org.eclipse.cdt.cpp.ui.internal.views.targets;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.core.resources.*;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import java.util.*;

/**
 *	Action for creating folder resources in the project explorer.
 */
 //, ISelectionChangedListener
public class CreateTargetAction implements IActionDelegate{
	private IResource resource = null;
	private String path;
	private IStructuredSelection selection;
  	private Shell shell;
 	// NL enablement
	private CppPlugin pluginInstance = CppPlugin.getPlugin();
	private String DIALOG_TITLE = "TargetsViewer.TargetAction.Dialog_Title";
/**
 * 
 */
	public  void run(IAction action)
	{
		NewTargetWizard wizard = new NewTargetWizard();
		//selection = (IStructuredSelection)page.getStructuredSelection();
			 // to  initialize target container root
		selection = NavigatorSelection.structuredSelection;
		wizard.init(CppPlugin.getDefault().getWorkbench(), selection);
		wizard.setNeedsProgressMonitor(true);
		WizardDialog dialog = new WizardDialog(shell,wizard);
		dialog.setTitle(pluginInstance.getLocalizedString(DIALOG_TITLE));
		dialog.open();
	}
	public void selectionChanged(IAction action, ISelection aSelection) 
	{
		selection = (IStructuredSelection)aSelection;
		if(selection!=null && selection instanceof IStructuredSelection)
		{
			NavigatorSelection.structuredSelection = (IStructuredSelection)selection;
			Object root = ((IStructuredSelection)selection).getFirstElement();
				if(root instanceof IProject)
						NavigatorSelection.selection = (IResource)root;
				else if (root instanceof IFolder)
					NavigatorSelection.selection = ((IFolder)root).getProject();
				else if (root instanceof IFile)
					NavigatorSelection.selection = ((IFile)root).getProject();			

			if (aSelection instanceof IStructuredSelection)
			{
				IStructuredSelection structuredSelection= (IStructuredSelection)aSelection;
				IResource _resource= (IResource)structuredSelection.getFirstElement();
 	 			if (_resource != null)
 	   			{
 	      			IProject project = _resource.getProject();	
					if (CppPlugin.isCppProject(project))
	    			{
	        	        ((Action)action).setEnabled(true);
	       			}
 	      	 		else
 	          		{
        	     	   ((Action)action).setEnabled(false);
					}
				}
			}
		}

		Object root = ((IStructuredSelection)selection).getFirstElement();
		if(root instanceof IProject)
			NavigatorSelection.selection  = (IProject)root;
			
	}
}
