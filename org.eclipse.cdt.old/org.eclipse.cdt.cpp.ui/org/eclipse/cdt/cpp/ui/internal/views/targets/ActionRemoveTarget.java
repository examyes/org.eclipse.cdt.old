package com.ibm.cpp.ui.internal.views.targets;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
 
import org.eclipse.ui.views.properties.PropertyDescriptor; 
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
public class ActionRemoveTarget extends ActionTarget {
	int index = -1;
	private TargetsViewer viewer;
	private TargetsPage page;
// NL enablement
	private static CppPlugin pluginInstance = CppPlugin.getPlugin();
	private static String REMOVE_ACTION_KEY = "TargetsViewer.Action.Remove_Target";


public ActionRemoveTarget(TargetsPage targetsPage) {
	super(targetsPage.getViewer(), pluginInstance.getLocalizedString(REMOVE_ACTION_KEY));
	setImageDescriptor(CppPluginImages.DESC_ELCL_TRG_REM);
	setEnabled(false);
	page = targetsPage;
	viewer = targetsPage.getViewer();
}
private void removeDescriptor( int x, RootElement root)
{
		Vector vec = new Vector();
		int index = 0;
		for(int i =0; i < root.getPropertyDescriptors().length; i++)
		{
			int numberExtension = x+root.MAX_TARGETS;
			if(!((PropertyDescriptor)root.getPropertyDescriptors()[i]).getId().equals("Command"+numberExtension))
				vec.add(root.getPropertyDescriptors()[index++]);
		}
		// reset the counter
		root.resetCounter(root.getCounter()-1);
		root.setDescriptors(vec);
	
}
private void removeTarget( String key, RootElement root)
{
		Vector vec = new Vector();
		boolean removed = false;
		int index = 0;int rem = 0;
		for(int i =0; i < root.getTargets().size(); i++)
		{
			//System.out.println("\n key = "+((TargetElement)root.getTargets().elementAt(i)).getID());
			if((!((TargetElement)root.getTargets().elementAt(i)).getID().equals(key))||removed)
				vec.add(index++,root.getTargets().elementAt(i));
			else 
			{
				rem = i;
				removed = true;
			}
		}
		root.setTargets(vec);
		removeDescriptor(rem,root);
		updateBuildStatus(rem,root);
		
}
/**
 * 
 */
public void run() {

	final Object root = NavigatorSelection.selection;
	
	if(root!=null)
	{
		index = page.getRootIndex(root);
		if(index < 0)
			System.out.println("\n Should never happen - root must exist");
		
		viewer.getControl().getDisplay().asyncExec(new Runnable() {
		public void run() {
			RootElement rootElement =  (RootElement)page.targetStore.projectList.elementAt(index);
			// iterate through the targets to get the index of the target that need to be removed
			Vector vec = new Vector();
			String key = new String("not defined");
			
			viewer.getSelectionFromWidget(vec);
			Object element = ((TargetsEntry)vec.elementAt(0)).values[0] ; // The vector has only on element - 
			//this element is a list of all the children of the element
			if(element instanceof TargetElement)
				key = ((TargetElement)element).getID();
			
			// remove the selected target - this method calls remove descriptor as well
			removeTarget(key,rootElement);
			
			List list = new ArrayList();
			list.add((RootElement)page.targetStore.projectList.elementAt(index));
			//list.add(rootElement);
			viewer.setInput(list.toArray());
			
			// updating the availabilty of the tool bar actions
			updateActionsEnablement(rootElement);
		}});
	}

}
private void updateActionsEnablement(RootElement root)
{
	if(root.getTargets().size()==0)
		page.removeAllAction.setEnabled(false);
	
	page.buildAction.setEnabled(false);
	page.removeAction.setEnabled(false);

}
private void updateBuildStatus( int x, RootElement root)
{		
		for(int i =0; i < root.indexOfSelectedTableItems.size(); i++)
		{
			Integer val = (Integer)root.indexOfSelectedTableItems.elementAt(i);
			//remove the index if found 
			if(val.intValue()==x)
			{
				root.indexOfSelectedTableItems.removeElementAt(i);
				break;
			}
		}
		
		Vector clone = new Vector();
		
		for(int i =0; i < root.indexOfSelectedTableItems.size(); i++)
		{
			Integer val = (Integer)root.indexOfSelectedTableItems.elementAt(i);
			// reduce the value by one if > than the removed index
			if(val.intValue() > x )
			{
				Integer newVal = new Integer(val.intValue()-1);
				clone.addElement(newVal);
			}
			else
				clone.addElement(root.indexOfSelectedTableItems.elementAt(i));
		}
		root.indexOfSelectedTableItems.removeAllElements();
		for(int i =0; i < clone.size(); i++)
			root.indexOfSelectedTableItems.addElement(clone.elementAt(i));

}
}
