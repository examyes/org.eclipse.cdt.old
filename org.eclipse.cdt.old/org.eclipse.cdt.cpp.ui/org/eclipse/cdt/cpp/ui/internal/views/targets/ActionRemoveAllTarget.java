package com.ibm.cpp.ui.internal.views.targets;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.*;
import org.eclipse.jface.viewers.*;
import java.util.*;
import com.ibm.cpp.ui.internal.*;

/**
 *  
 */
public class ActionRemoveAllTarget extends ActionTarget {
	int index = -1;
	private TargetsViewer viewer;
	private TargetsPage page;
	
	// NL enablement
	private static CppPlugin pluginInstance = CppPlugin.getPlugin();
	private static String REMOVE_ALL_ACTION_KEY = "TargetsViewer.Action.Remove_All_Targets";

public ActionRemoveAllTarget(TargetsPage targetsPage) {
	super(targetsPage.getViewer(), pluginInstance.getLocalizedString(REMOVE_ALL_ACTION_KEY));
	setImageDescriptor(CppPluginImages.DESC_ELCL_TRG_REMALL);
	setEnabled(false);
	page = targetsPage;
	viewer = targetsPage.getViewer();
}
/**
 * Implementation of method defined on <code>IAction</code>.
 */
public  void run(){
	
	final Object root = NavigatorSelection.selection;
	
	if(root!=null)
	{
		index = page.getRootIndex(root);
		if(index < 0)
		{
			System.out.println("\n Should never happen - root must exist");
		}
		viewer.getControl().getDisplay().asyncExec(new Runnable() {
		public void run() {
			RootElement rootElement =  (RootElement)page.targetStore.projectList.elementAt(index);
			rootElement.getTargets().removeAllElements();
			rootElement.getDescriptors().removeAllElements();
			rootElement.resetCounter(0);
			rootElement.indexOfSelectedTableItems.removeAllElements();  // target build markers
			List list = new ArrayList();
			list.add((RootElement)page.targetStore.projectList.elementAt(index));
			viewer.setInput(list.toArray());
		}});
		// disabling irrelevant actions
		page.buildAction.setEnabled(false);
		page.removeAction.setEnabled(false);
		page.removeAllAction.setEnabled(false);
	}
}
}
