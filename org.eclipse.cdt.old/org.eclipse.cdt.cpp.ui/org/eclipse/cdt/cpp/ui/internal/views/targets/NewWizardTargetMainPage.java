package org.eclipse.cdt.cpp.ui.internal.views.targets;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.CppPlugin;
import org.eclipse.cdt.cpp.ui.internal.api.*;
import org.eclipse.cdt.dstore.core.model.*;

import java.lang.reflect.InvocationTargetException;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.*;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import java.util.*;

/**
 *	This is the Target creation page 
 */
class NewWizardTargetMainPage extends WizardPage implements Listener 
{
    private IStructuredSelection currentSelection;
    private IContainer currentParent;
    private IWorkbench workbench;
    private TargetElement newTarget;
    
    // widgets
    private NewTargetGroup targetGroup;
    
    // NL enablement
    private static CppPlugin pluginInstance = CppPlugin.getPlugin();
    private static String Dialog_Title= "TargetsViewer.Wizard.Dialog_Title";
    private String Wizard_DESC = "TargetsViewer.Wizard.Description";
    private String Target_Name="TargetsViewer.Wizard.Target_Name";
    

    /**
     *	Create a new instance of this class
     */
    public NewWizardTargetMainPage(String pageName,IStructuredSelection selection) 
    {
	super(pluginInstance.getLocalizedString(Dialog_Title));
	setTitle(pageName);
	setDescription(pluginInstance.getLocalizedString(Wizard_DESC));
	this.currentSelection = selection;
    }

    /**
     *	Answer a Control containing this page's contents
     *
     *	@return com.ibm.swt.widgets.Control
     *	@param parent com.ibm.swt.widgets.Composite
     */
    public void createControl(Composite parent) 
    {
	// top level group
	Composite composite = new Composite(parent,SWT.NONE);
	composite.setLayout(new GridLayout());
	composite.setLayoutData(new GridData(
					     GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
	
	targetGroup = new NewTargetGroup(composite,this,pluginInstance.getLocalizedString(Target_Name));
	targetGroup.setAllowExistingResources(false);
	initializePage();
	
	setControl(composite);
    }

    /**
     *
     */
    protected void createFolder(IFolder folderHandle,IProgressMonitor monitor) throws CoreException 
    {
	folderHandle.create(false,true,monitor);
	
	if (monitor.isCanceled())
	    throw new OperationCanceledException();
    }

    /**
     * 
     */
    protected IFolder createFolderHandle(IPath folderPath) 
    {
	return CppPlugin.getPluginWorkspace().getRoot().getFolder(folderPath);
    }

    /**
     *
     */
    public boolean finish() 
    {
	return getNewTarget() != null;
    }

    /**
     * Returns a new Target  which is created according to the current
     */
    public TargetElement getNewTarget() 
    {
	if (newTarget != null)
	    return newTarget;
	
	String path = new String(targetGroup.getContainerFullPath().toString());
	
	newTarget = new TargetElement(targetGroup.getResource(),path,targetGroup.getInvocation(),null);
	
	return newTarget;
    }
    
    public DataElement getProjectElement()
    {
	DataElement resource = targetGroup.getResourceElement();
	if (resource != null)
	    {
		CppPlugin plugin = CppPlugin.getDefault();
		ModelInterface api = plugin.getModelInterface();
		return api.getProjectFor(resource);
	    }

	return null;
    }


    /**
     *	Handle all events and enablements for widgets on this page
     *
     *	@param e com.ibm.swt.widgets.Event
     */
    public void handleEvent(Event ev) 
	    {
		setPageComplete(validatePage());
	    }
    
    /**
     * Initializes this page's visual components.
     */
    protected void initializePage() 
    {
	targetGroup.setFocus();
	setPageComplete(false);
    }

    /**
     * Returns a <code>boolean</code> indicating whether this page's visual
     * components currently all contain valid values.
     *
     * @return <code>boolean</code> indicating validity of all visual components on this page
     */
    protected boolean validatePage() 
    {	
	boolean valid = true;
	if (!targetGroup.areAllValuesValid()) 
	    {
		if (!targetGroup.getResource().equals("")||!targetGroup.getInvocation().equals(""))	// if blank name then fail silently
		    setErrorMessage(targetGroup.getProblemMessage());
		else
		    setErrorMessage(targetGroup.getProblemMessage());
		valid = false;
	    }
	
	// Avoid draw flicker by clearing error message
	// if all is valid.
	if (valid)
	    setErrorMessage(null);
	
	return valid;
    }
}
