package com.ibm.cpp.ui.internal.views.targets;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import com.ibm.cpp.ui.internal.CppPlugin;

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
 *	This is the default folder creation page 1
 */
class NewWizardTargetMainPage extends WizardPage implements Listener {
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
public NewWizardTargetMainPage(String pageName,IStructuredSelection selection) {
	super(pluginInstance.getLocalizedString(Dialog_Title));
	setTitle(pageName);
	setDescription(pluginInstance.getLocalizedString(Wizard_DESC));
	//this.workbench = aWorkbench;
	this.currentSelection = selection;
}
/**
 *	Answer a Control containing this page's contents
 *
 *	@return com.ibm.swt.widgets.Control
 *	@param parent com.ibm.swt.widgets.Composite
 */
public void createControl(Composite parent) {
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
 * Creates a concrete folder resource from a folder handle.  Returns a
 * <code>boolean</code> indicating success.
 *
 * @param folderHandle the folder handle to create a folder resource with
 * @param monitor the progress monitor to show visual progress with
 * @exception com.ibm.eclipse.core.runtime.CoreException
 */
protected void createFolder(IFolder folderHandle,IProgressMonitor monitor) throws CoreException {
	folderHandle.create(false,true,monitor);

	if (monitor.isCanceled())
		throw new OperationCanceledException();
}
/**
 * Creates a folder resource handle with a specified path.  The folder resource
 * should <b>not</b> be created concretely here; this step is subsequently
 * performed by <code>createFolder(IFolder)</code>.
 *
 * @param folderPath the path of the folder resource to create a handle for
 * @return the new folder resource handle
 * @see #createFolder(com.ibm.eclipse.core.resources.IFolder)
 */
protected IFolder createFolderHandle(IPath folderPath) {
	return CppPlugin.getPluginWorkspace().getRoot().getFolder(folderPath);
}
/**
 *	It is assumed that if the Finish button was enabled (thus allowing invocation
 *	of this method) that the folder resource and container info supplied is valid.
 *	Answer a boolean indicating whether self was able to create the appropriate
 *	new folder resource
 */
public boolean finish() {
	return getNewTarget() != null;
}
/**
 * Returns a new folder resource which is created according to the current
 * values of this page's visual components, or <code>null</code> if there was
 * an error creating this folder.  This method should be invoked after the
 * user has pressed Finish on the parent wizard, since the enablement of this
 * button implies that all visual components on this page currently contain
 * valid values.
 * <p>
 * Note that this page caches the new folder once it has been successfully
 * created, so subsequent invocations of this method will answer the same
 * folder resource.
 * </p>
 * @return the created folder resource
 */
public TargetElement getNewTarget() {
	if (newTarget != null)
		return newTarget;
	
	String path = new String(targetGroup.getContainerFullPath().toString());

	newTarget = new TargetElement(targetGroup.getResource(),path,targetGroup.getInvocation(),null);
	
	return newTarget;

}
/**
 *	Handle all events and enablements for widgets on this page
 *
 *	@param e com.ibm.swt.widgets.Event
 */
public void handleEvent(Event ev) {
	setPageComplete(validatePage());
}
/**
 * Initializes this page's visual components.
 */
protected void initializePage() {
	Iterator enum = currentSelection.iterator();
	if (enum.hasNext()) {
		Object next = enum.next();
		IResource selectedResource = null;
		if (next instanceof IResource) {
			selectedResource = (IResource)next;
		} else if (next instanceof IAdaptable) {
			selectedResource = (IResource)((IAdaptable)next).getAdapter(IResource.class);
		}
		if (selectedResource != null && !enum.hasNext()) {	// ie.- not a multi-selection
			if (selectedResource.getType() == IResource.FILE)
				selectedResource = selectedResource.getParent();
			if (selectedResource.isAccessible())
				targetGroup.setContainerFullPath(selectedResource.getFullPath());
		}
	}

	targetGroup.setFocus();
	setPageComplete(false);
}
/**
 * Returns a <code>boolean</code> indicating whether this page's visual
 * components currently all contain valid values.
 *
 * @return <code>boolean</code> indicating validity of all visual components on this page
 */
protected boolean validatePage() {
	
	boolean valid = true;
	if (!targetGroup.areAllValuesValid()) {
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
