package com.ibm.cpp.ui.internal.views.targets;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.ui.internal.api.*;
import com.ibm.dstore.core.model.*;

import org.eclipse.swt.custom.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.*;
import org.eclipse.jface.viewers.*;
import java.util.*;


import org.eclipse.swt.widgets.*;

/**
 *  
 */
public class ActionBuildTarget extends ActionTarget 
{
	private TargetElement _currentTarget;
	private TargetsPage page;
	
	// NL enablement
	private static CppPlugin pluginInstance = CppPlugin.getPlugin();
	private static  String BUILD_ACTION_KEY = "TargetsViewer.Action.Build_Target";

  public ActionBuildTarget(TargetsPage targetsPage) 
  {
	super(targetsPage.getViewer(),pluginInstance.getLocalizedString(BUILD_ACTION_KEY));
	setImageDescriptor(CppPluginImages.DESC_ELCL_TRG_BUILD);
	setEnabled(false);
	page = targetsPage;
  }          
/**
 * Implementation of method defined on <code>IAction</code>.
 */
public  void run(){
	
	Vector vec = new Vector();
	viewer.getSelectionFromWidget(vec);

	for(int i=0; i < vec.size();i++) 
	{
		Object element = ((TargetsEntry)vec.elementAt(i)).values[0];
		if(element instanceof TargetElement)
		{
			_currentTarget = (TargetElement)element;
			
			String path = (String)_currentTarget.getWorkingDirectory();
			String invocation = (String)_currentTarget.getMakeInvocation();

			ModelInterface api = CppPlugin.getModelInterface();	
			DataElement status = api.command(path, invocation);
			
			// updating the status field
			Table table = viewer.tableTree.getTable();
			table.getItem(table.getSelectionIndex()).setText(2,status.getValue());
			// new ode
			int index = -1;
			RootElement parent = (RootElement)_currentTarget.getParent("");
			for(int y = 0 ; y < parent.getTargets().size(); y++)
			{
				if(((TargetElement)parent.getTargets().elementAt(y)).getID().equals(_currentTarget.getID()))
					index = y;
			}
			((RootElement)_currentTarget.getParent(null)).indexOfSelectedTableItems.add(new Integer(index));
			_currentTarget.setStatus(status);

		}
	}
	// enable / disable relevant tool bar actions
	page.removeAction.setEnabled(false);
	page.buildAction.setEnabled(false);

  }  
}
