package org.eclipse.cdt.cpp.ui.internal.actions;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.api.*;
import org.eclipse.cdt.cpp.ui.internal.*;

import org.eclipse.cdt.dstore.ui.actions.*;
import org.eclipse.cdt.dstore.core.model.*;

import java.io.*; 
import java.util.*;

import org.eclipse.jface.action.*;

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.internal.dialogs.PropertyPageContributorManager;
import org.eclipse.ui.internal.dialogs.PropertyPageManager;
import org.eclipse.ui.internal.dialogs.PropertyDialog;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.widgets.Shell;

public class OpenPropertiesAction extends CustomAction
{ 
  public OpenPropertiesAction(DataElement subject, String label, DataElement command, DataStore dataStore)
      {	
        super(subject, label, command, dataStore);
      }

    public void run()
    {
	PropertyPageManager pageManager = new PropertyPageManager();
	String title = "";//$NON-NLS-1$

	// get selection
	ModelInterface api = ModelInterface.getInstance();
	IProject project = api.findProjectResource(_subject);
	
	if (project != null)
	    {
		Shell shell = api.getDummyShell();
		
		// load pages for the selection
		// fill the manager with contributions from the matching contributors
		PropertyPageContributorManager.getManager().contribute(pageManager, project);
		
		// testing if there are pages in the manager
		Iterator pages = pageManager.getElements(PreferenceManager.PRE_ORDER).iterator();
		String name = project.getName();
		
		if (!pages.hasNext()) 
		    {
			MessageDialog.openInformation(
						      shell,
						      WorkbenchMessages.getString("PropertyDialog.messageTitle"), //$NON-NLS-1$
						      WorkbenchMessages.format("PropertyDialog.noPropertyMessage", new Object[] {name})); //$NON-NLS-1$
			return;
		    } 
		else
		    {
			title = WorkbenchMessages.format("PropertyDialog.propertyMessage", new Object[] {name}); //$NON-NLS-1$
		    }
		
		
		
		PropertyDialog propertyDialog = new PropertyDialog(shell, pageManager, new StructuredSelection(project)); 
		propertyDialog.create();
		propertyDialog.getShell().setText(title);
		WorkbenchHelp.setHelp(propertyDialog.getShell(), new Object[]{IHelpContextIds.PROPERTY_DIALOG});
		propertyDialog.open();
	    }
	else
	    {
		System.out.println("project is null");
	    }
    }
}


