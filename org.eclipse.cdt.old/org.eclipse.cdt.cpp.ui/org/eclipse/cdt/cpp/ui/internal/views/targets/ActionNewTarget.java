package org.eclipse.cdt.cpp.ui.internal.views.targets;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
 
import org.eclipse.cdt.cpp.ui.internal.CppPlugin;
import org.eclipse.core.resources.*;
import org.eclipse.ui.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.widgets.Shell;

/**
 *  
 */
public class ActionNewTarget extends ActionTarget implements IBasicPropertyConstants{
	
	TargetsViewer viewer;
	private TargetsPage page;
	int index = -1;
	
	
	private ISelection previousSelection;
	private IStructuredSelection selection;
	private Shell shell;

	
	// NL enablement
	private static CppPlugin pluginInstance = CppPlugin.getPlugin();
	private static  String NEW_ACTION_KEY = "TargetsViewer.Action.New_Target";
	

public ActionNewTarget(TargetsPage targetsPage) {
	super(targetsPage.getViewer(), pluginInstance.getLocalizedString(NEW_ACTION_KEY));
	setImageDescriptor(CppPluginImages.DESC_ELCL_TRG_ADD);
	setEnabled(false);
	page = targetsPage;
	viewer = page.getViewer();
}
/**
 * Implementation of method defined on <code>IAction</code>.
 */
public  void run(){
	
	NewTargetWizard wizard = new NewTargetWizard();
	
	selection = (IStructuredSelection)NavigatorSelection.structuredSelection;
	
	wizard.init(CppPlugin.getDefault().getWorkbench(), selection);
	wizard.setNeedsProgressMonitor(true);
	WizardDialog dialog = new WizardDialog(shell,wizard);
	dialog.setTitle("New");
	dialog.open();	
}
}
