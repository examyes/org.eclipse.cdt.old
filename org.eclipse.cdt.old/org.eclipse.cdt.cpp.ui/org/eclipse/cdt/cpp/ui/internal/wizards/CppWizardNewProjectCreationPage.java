package org.eclipse.cdt.cpp.ui.internal.wizards;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.CppProjectAttributes;
import org.eclipse.cdt.cpp.ui.internal.CppPlugin;
import org.eclipse.cdt.cpp.ui.internal.vcm.*;

import org.eclipse.cdt.dstore.ui.connections.*;
import org.eclipse.cdt.dstore.core.model.*;

import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.*;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
//import org.eclipse.core.target.*;
//import org.eclipse.core.internal.target.webdav.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.io.*;
import java.net.URL;
import java.net.*;

import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.*;
import org.eclipse.ui.model.*;
import org.eclipse.ui.actions.*;

import org.eclipse.vcm.internal.core.base.*;
import org.eclipse.vcm.internal.core.*;

/**
 * Concrete implementation of a typical project creation main page.  Isv's can
 * reuse this page as-is, or can subclass it in order to inherit selected aspects
 * of its general project-creation facilities.
 */

public class CppWizardNewProjectCreationPage extends WizardPage implements Listener
{

    // cache of newly-created project
    private FileSystemElement	root;
    private FileSystemElement       selectedResource;

    // initial value stores
    private String initialProjectFieldValue;
    private IProject[] referencedProjects;

    // widgets
    private Text projectNameField;
    protected Button                        defaultMappingRadio;
    protected Button                        localMappingRadio;
    protected Combo	                        sourceNameField;
    protected Button	                sourceBrowseButton;
    protected String                        selectedDirectory="";
    protected Button                        remoteURLRadio;
    protected Button                        remoteHostRadio;
    protected Combo		                remoteURLNameField;

    protected Combo		                remoteHostNameField;
    protected Combo		                remoteHostPortNumberField;
    protected Combo		                remoteHostDirectoryField;
    protected Combo		                remoteHostMountField;
    protected Button                            remoteHostBrowseButton;
    protected Button                            remoteHostUseDaemon;

    protected String                        selectedRemoteDirectory="http://weisz/itp_index";
    protected String                        selectedHostName="";
    protected String                        selectedHostPortNumber="";
    protected String                        selectedHostDirectory="";
    protected String                        selectedHostMount="";
    private   CheckboxTableViewer           referenceProjectsViewer;

    // constants
    private static final int SIZING_TEXT_FIELD_WIDTH = 150;
    private static final int SIZING_INDENTATION_WIDTH = 10;
    protected int  _sourceLocation  = CppProjectAttributes.LOCATION_WORKBENCH;
    protected CppPlugin    _plugin = CppPlugin.getPlugin();
    protected ArrayList    _history;

    private  IProject      _projectHandle;

    /**
     * Creates a <code>WizardNewProjectCreationPage</code> instance.
     *
     * @param pageId this page's internal name
     * @param desktop the current desktop
     */
    public CppWizardNewProjectCreationPage(String pageId) {
	super(pageId);
	setPageComplete(false);
	//	this.initialLocationFieldValue = Platform.getLocation();
    }

    /**
     * Returns this page's initial visual components.
     *
     * @param parent a <code>Composite</code> that is to be used as the parent of this
     *     page's collection of visual components
     * @return a <code>Control</code> that contains this page's collection of visual
     *     components
     * @see org.eclipse.swt.widgets.Composite
     * @see org.eclipse.swt.widgets.Control
     */
    public void createControl(Composite parent)
    {
	Composite composite = new Composite(parent, SWT.NULL);
	
	
	composite.addHelpListener(new HelpListener()
	    {
		public void helpRequested(HelpEvent event)
		{
		    performHelp();
		}
	    });
	
	composite.setLayout(new GridLayout());
	composite.setLayoutData(new GridData(GridData.FILL_BOTH));
	
	createProjectNameGroup(composite);
	//***	createReferencedProjectsGroup(composite);
	
	//***	createSpacer(composite);
	createSourceDirectoryGroup(composite);
	
	projectNameField.setFocus();
	
	setControl(composite);
    }

    /**
     * Creates a concrete project resource from a project handle.  Returns a
     * <code>boolean</code> indicating success.
     *
     * @param projectHandle the project handle to create a project resource with
     * @param monitor the progress monitor to show visual progress with
     * @exception com.ibm.itp.core.api.resources.CoreException
     */
    //protected void createProject(IProject projectHandle,IProgressMonitor monitor) throws CoreException {
    private void createProject(IProjectDescription description,
			       IProject projectHandle, IProgressMonitor monitor)
	throws CoreException, OperationCanceledException
    {
	try
	    {
		monitor.beginTask("",2000);
		
		projectHandle.create(description, new SubProgressMonitor(monitor,1000));
		
		if (monitor.isCanceled())
		    throw new OperationCanceledException();

		projectHandle.open(new SubProgressMonitor(monitor,1000));
		
	    } finally {
		monitor.done();
	    }
	
	if (monitor.isCanceled())
	    {
		throw new OperationCanceledException();
	    }
    }

    /**
     * Creates the project name specification visual components.
     *
     * @param parent a <code>Composite</code> that is to be used as the parent
     *     of this group's collection of visual components
     * @see org.eclipse.swt.widgets.Composite
     */
    protected final void createProjectNameGroup(Composite parent)
    {
	// project specification group
	Composite projectGroup = new Composite(parent,SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.numColumns = 2;
	projectGroup.setLayout(layout);
	projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	
	// new project label
	Label projectLabel = new Label(projectGroup,SWT.NONE);
	projectLabel.setText(_plugin.getLocalizedString("createProjectWizard.ProjectName"));
	
	// new project name entry field
	projectNameField = new Text(projectGroup, SWT.BORDER);
	projectNameField.addListener(SWT.Modify, this);
	GridData data = new GridData(GridData.FILL_HORIZONTAL);
	data.widthHint = SIZING_TEXT_FIELD_WIDTH;
	projectNameField.setLayoutData(data);
	
	if (initialProjectFieldValue != null)
	    projectNameField.setText(initialProjectFieldValue);
    }

    /**
     * Creates the project name specification visual components.
     *
     * @param parent a <code>Composite</code> that is to be used as the parent
     *     of this group's collection of visual components
     * @see org.eclipse.swt.widgets.Composite
     */
    protected final void createReferencedProjectsGroup(Composite parent)
    {
	//only create this group if there are already projects in the workspace
	if (ResourcesPlugin.getWorkspace().getRoot().getProjects().length == 0) {
	    return;
	}

	Composite composite = new Composite(parent, SWT.NONE);
	composite.setLayout(new GridLayout());
	composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	
	new Label(composite, SWT.NONE).setText(_plugin.getLocalizedString("createProjectWizard.ReferencedProjects"));
	
	referenceProjectsViewer = new CheckboxTableViewer(composite, SWT.BORDER);
	referenceProjectsViewer.getTable().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	referenceProjectsViewer.setLabelProvider(new WorkbenchLabelProvider());
	referenceProjectsViewer.setContentProvider(new WorkbenchContentProvider());
	referenceProjectsViewer.setInput(ResourcesPlugin.getWorkspace());
    }

    /**
     * Performs setup code that is to be invoked whenever the user enters this
     * page in the wizard.
     *
     * @param int the direction that this page was entered from.  Possible values
     *     are <code>Wizard.CANCEL</code>, <code>Wizard.PREVIOUS</code>, <code>Wizard.NEXT</code>
     *     and <code>Wizard.FINISH</code>.
     * @see org.eclipse.jface.wizard.Wizard
     */
    protected void enter(int direction)
    {
	setPageComplete(validatePage());
    }

    /**
     * Performs this page's finish code which is invoked when the user presses the Finish
     * button on the parent wizard.  Returns a <code>boolean</code> indicating successful
     * completion, which also acts as permission for the parent wizard to close.  For this
     * page the task to perform at finish time is creation of a new project resource as
     * dictated by the current values of the page's visual components.
     *
     * @return <code>boolean</code> indicating successful completion of this page's finish code
     */
    public boolean finish()
    {
	if (isRemote())
	    {
		setDefaults("DefaultRemoteHostName",      remoteHostNameField);
		setDefaults("DefaultRemoteHostPort",      remoteHostPortNumberField);
		setDefaults("DefaultRemoteHostDirectory", remoteHostDirectoryField);			
		setDefaults("DefaultRemoteHostMount",     remoteHostMountField);
	    }

	return getNewProject() != null;
    }

    public boolean isRemote()
    {
	return (_sourceLocation == CppProjectAttributes.LOCATION_HOST);
    }

    /**
     * Returns a new project resource which is created according to the current
     * values of this page's visual components, or <code>null</code> if there was
     * an error creating this project.  This method should be invoked after the
     * user has pressed Finish on the parent wizard, since the enablement of this
     * button implies that all visual components on this page currently contain
     * valid values.
     * <p>
     * Note that this page caches the new project once it has been successfully
     * created, so subsequent invocations of this method will answer the same
     * project resource.
     * </p>
     * @return the created project resource
     */
    public IProject getNewProject()
    {
	if (_projectHandle != null)
	    {
		return _projectHandle;
	    }
	
  	selectedDirectory      = sourceNameField.getText();
	referencedProjects     = getReferencedProjects();
	selectedHostName       = remoteHostNameField.getText();
	selectedHostPortNumber = remoteHostPortNumberField.getText();
	selectedHostDirectory  = remoteHostDirectoryField.getText();
	selectedHostMount      = remoteHostMountField.getText();

	_projectHandle = getProjectHandle();

	WorkspaceModifyOperation op = new WorkspaceModifyOperation()
	    {
		protected void execute(IProgressMonitor monitor) throws CoreException
		{
		    IWorkspace workspace = ResourcesPlugin.getWorkspace();
		    final IProjectDescription description = workspace.newProjectDescription(_projectHandle.getName());
		
		    switch (_sourceLocation)
			{
			case CppProjectAttributes.LOCATION_LOCAL:
			    String directory = selectedDirectory;
			    ArrayList history = _plugin.readProperty(CppProjectAttributes.LOCATION_LOCAL);
			    int index = history.indexOf(selectedDirectory);
			    if (index != -1)
				history.remove(index);
			    history.add(selectedDirectory);
			    _plugin.writeProperty(CppProjectAttributes.LOCATION_LOCAL, history);
			    if (!directory.equals(""))
				{
				    IPath localPath = new Path(directory);
				
				    // get a project descriptor
				    IPath defaultPath = Platform.getLocation();
				    if (defaultPath.equals(localPath))
					localPath = null;
				    description.setLocation(localPath);
				    createProject(description, _projectHandle, monitor);
				    _projectHandle.refreshLocal(IResource.DEPTH_INFINITE, monitor);
				}
			    break;
			
			    /****
			case CppProjectAttributes.LOCATION_URL:
				 selectedRemoteDirectory = remoteURLNameField.getText();

				 URL                           location = null;
				 // PushPullTarget                target;
				 //IResourceMapping              mapping = null;
				

				 QualifiedName qName = new QualifiedName(CppProjectAttributes.NAME_QUALIFIER, "dav");
				 String urlName = selectedRemoteDirectory;
				 //System.out.println("CppWizardNewProjectCreationPage: URL is " +urlName);
				 try
				 {
				 location = new URL(urlName);
				 }
				 catch (MalformedURLException e)
				 {
				 System.out.println("CppWizardNewProjectCreationPage: A URL exception happened setting up the target.");
				 }
				
				 target = new PushPullTarget();
				 target.setLocation(location);
				 target.setQualifiedName(qName);
				 // Create a mapping so there is a content location on the target.
				 //System.out.println("MapAction calling getMapping");
				 //             mapping = ((Target) target).getMapping(project, IResource.PROJECT_ROOT);
				 mapping = ((Target) target).getMapping(_projectHandle, IResource.PROJECT_ROOT);
				 if (mapping == null)
				 {
				 //System.out.println("MapAction: mapping is null, calling newMapping");
				 mapping = _projectHandle.newMapping(IResource.PROJECT_ROOT, null);
				 }
				 //System.out.println("MapAction: calling setLocation");
				 mapping.setLocation(new Path(location.getFile()));
				 //System.out.println("MapAction calling addMapping");
				
				 ((Target) target).addMapping(_projectHandleHandle, mapping);
				 //System.out.println("MapAction after addMapping");
				 // Register the target with the workspace synchronizer.
				 _projectHandle.getWorkspace().getSynchronizer().add(target);
				 try
				 {
				 QualifiedName qURLName = new QualifiedName(CppProjectAttributes.NAME_QUALIFIER, "URL");
				 target.refresh(_projectHandleHandle, IResource.DEPTH_INFINITE, null);
				 target.deploy(_projectHandleHandle, IResource.DEPTH_INFINITE, null);
				 _projectHandle.setPersistentProperty(qURLName, urlName);
				 }
				 catch (CoreException e)
				 {
				 System.out.println("An error occurred during refresh" +e.toString());
				 }
			    break;
			    ***/
			
			case CppProjectAttributes.LOCATION_HOST:
			    break;
			
			case CppProjectAttributes.LOCATION_WORKBENCH:
			    description.setLocation(null);
			    createProject(description, _projectHandle, monitor);
			default:
			    break;
			}
		}
	    };
	
	try
	    {
		getWizard().getContainer().run(true, true, op);
	    }
	catch (OperationCanceledException e)
	    {
		return null;
	    }
	catch (InterruptedException e)
	    {
		return null;
	    }	
	catch (InvocationTargetException e)
	    {
		// ie.- one of the steps resulted in a core exception
		if (e.getTargetException() instanceof CoreException)
		    {
			ErrorDialog.openError(
					      null,
					      _plugin.getLocalizedString("createProjectWizard.CreationError"),
					      null,	// no special message
					      ((CoreException) e.getTargetException()).getStatus());
		    }
		else
		    {
			// CoreExceptions are handled above, but unexpected runtime exceptions and errors may still occur.
			MessageDialog.openError(getShell(),
						_plugin.getLocalizedString("createProjectWizard.CreationError"),
						_plugin.getLocalizedString("createProjectWizard.InternalError") + e.getTargetException().getMessage());
		    }
		return null;
	    }
	
	return _projectHandle;
    }

/**
 * Returns the current contents of the project name field, or
 * its set initial value if it does not exist yet (which could
 * be <code>null</code>).
 *
 * @return the project name field's current value or anticipated initial value, or <code>null</code>
 */
public String getProjectFieldValue() {
	if (projectNameField == null)
		return initialProjectFieldValue;
		
	return projectNameField.getText();
}
/**
 * Creates a project handle for the current project field value.  The project resource should
 * <b>not</b> be created concretely here; this step is subsequently performed by
 * <code>createProject(IProject,IProgressMonitor)</code>.
 *
 * @return the new project handle
 * @see #createProject(com.ibm.itp.core.api.resources.IProject,com.ibm.itp.common.IProgressMonitor)
 */
protected IProject getProjectHandle()
{
    if (remoteHostRadio.getSelection())
	{
	    String name = getProjectFieldValue();

	    ArrayList args = new ArrayList();
	    args.add("directory");
	    args.add(selectedHostName);
	    args.add(selectedHostPortNumber);
	    args.add(selectedHostDirectory);
	    args.add("false");
	    args.add(remoteHostUseDaemon.getSelection() ? "true" : "false");
	
	    DataStore ds = CppPlugin.getDefault().getDataStore();	
	    DataElement root = ds.getRoot();
	    Connection con = new Connection(name, args, root);

	    org.eclipse.cdt.cpp.ui.internal.vcm.PlatformVCMProvider provider = org.eclipse.cdt.cpp.ui.internal.vcm.PlatformVCMProvider.getInstance();
	    IProject newPrj = (IProject)provider.createRepository(con, con.getRoot());	   	

	    return newPrj;
	}
    else
	{
	    return _plugin.getPluginWorkspace().getRoot().getProject(getProjectFieldValue());
	}
}

/**
 * Returns the names of referenced projects selected by the user.
 * Returns an empty array if none were selected or if no other projects exist.
 */
protected IProject[] getReferencedProjects() {
	if (referenceProjectsViewer == null) {
		return new IProject[0];
	}
	/*****
	java.util.List checked = referenceProjectsViewer.getCheckedElements();
	IProject[] results = new IProject[checked.size()];
	for (int i = 0; i < results.length; i++) {
		results[i] = (IProject)checked.get(i);
	}
	return results;	
	***/
	return null;
}

/**
 * Returns the name of the local filesystem directory where the user
 * wants the project to point to, instead of importing (copying) its contents
 * in the workbench.
 */
protected String getDirectoryName() {
	return selectedDirectory;	
}

    /**
     * Handles all events and enablements for visual components on this page.
     * <b>Subclasses</b> may wish to override this method if they hook listeners
     * to their own visual components.  However, they must ensure that this method
     * is <b>always</b> invoked, even if the event source is a visual component
     * defined in a subclass.
     *
     * @param ev the visual component event
     */
    public void handleEvent(Event ev)
    {
	Widget source = ev.widget;
	
	if ((source == sourceNameField) || (source == remoteURLNameField))
	    {
		resetSelection();
	    }
	else if (source == sourceBrowseButton)
	    {
		handleSourceBrowseButtonPressed();
	    }
	else if (source == defaultMappingRadio)
            {
		resetSelection();
		sourceNameField.setEnabled(false);
		sourceBrowseButton.setEnabled(false);
            	//remoteURLNameField.setEnabled(false);
            	remoteHostNameField.setEnabled(false);
		remoteHostUseDaemon.setEnabled(false);
            	remoteHostPortNumberField.setEnabled(false);
            	remoteHostDirectoryField.setEnabled(false);
            	remoteHostMountField.setEnabled(false);
            	remoteHostBrowseButton.setEnabled(false);
		_sourceLocation = CppProjectAttributes.LOCATION_WORKBENCH;
            }
	else if (source == localMappingRadio)
            {
		resetSelection();
		sourceNameField.setEnabled(true);
		sourceNameField.setFocus();
		sourceBrowseButton.setEnabled(true);
            	remoteHostNameField.setEnabled(false);
            	remoteHostPortNumberField.setEnabled(false);
		remoteHostDirectoryField.setEnabled(false);
		remoteHostMountField.setEnabled(false);
		remoteHostBrowseButton.setEnabled(false);
		remoteHostUseDaemon.setEnabled(false);
		_sourceLocation = CppProjectAttributes.LOCATION_LOCAL;
            }
	else if (source == remoteHostRadio)
            {
            	resetSelection();
		remoteHostNameField.setEnabled(true);
            	remoteHostPortNumberField.setEnabled(true);
            	remoteHostDirectoryField.setEnabled(true);
            	remoteHostMountField.setEnabled(true);
		remoteHostBrowseButton.setEnabled(true);
		remoteHostUseDaemon.setEnabled(true);
            	remoteHostNameField.setFocus();
             	sourceNameField.setEnabled(false);
		sourceBrowseButton.setEnabled(false);
		_sourceLocation = CppProjectAttributes.LOCATION_HOST;
            }
	else if (source == remoteHostBrowseButton)
	    {
		org.eclipse.cdt.dstore.hosts.actions.QuickConnectAction browse =
		    new org.eclipse.cdt.dstore.hosts.actions.QuickConnectAction(remoteHostNameField.getText(),
									remoteHostPortNumberField.getText(),
									remoteHostDirectoryField.getText(),
									remoteHostUseDaemon.getSelection());

		
	        Display d = remoteHostBrowseButton.getShell().getDisplay();
		d.syncExec(browse);

		String selected = browse.getSelected();
		if (selected != null)
		    {
			remoteHostDirectoryField.setText(selected + "/");
		    }
	    }

	this.setPageComplete(this.validatePage());
    }

    public void performHelp()
    {
	// System.out.println("HELP");
    }

    /**
     * Returns a <code>boolean</code> indicating whether this page's visual
     * components currently all contain valid values.
     *
     * @return <code>boolean</code> indicating validity of all visual components on this page
     */
    protected boolean validatePage()
    {
	if (validateProjectNameGroup())
	    {
		if (_sourceLocation == CppProjectAttributes.LOCATION_LOCAL)
		    {
			String dir = sourceNameField.getText();
			java.io.File testFile = new java.io.File(dir);
			if (!testFile.exists())
			    {
				return false;
			    }			
		    }
		if (_sourceLocation == CppProjectAttributes.LOCATION_HOST)
		    {
			String hostName = remoteHostNameField.getText();
			if (hostName.length() == 0)
			    {
				return false;
			    }
		    }
		return true;
	    }

	return false;
    }

    /**
     *	Display an error dialog with the specified message.
     *
     *	@param message java.lang.String
     */
    protected void displayErrorDialog(String message)
    {
	//***	MessageDialog.openError(getShell(),"Export Problems",message);
    }

    /**
     * Returns a <code>boolean</code> indicating whether this page's project name
     * specification group's visual components currently all contain valid values.
     *
     * @return <code>boolean</code> indicating validity of all visual components
     *     in the project name specification group
     */
    protected boolean validateProjectNameGroup()
    {
	IWorkspace workspace = _plugin.getPluginWorkspace();

	
	String projectFieldContents = projectNameField.getText();
	if (projectFieldContents.equals(""))
	    return false;
	
	IStatus result = workspace.validateName(projectFieldContents,IResource.PROJECT);
	if (!result.isOK())
	    {
		//**displayErrorMessage("Invalid project name: " + result.getMessage());
		return false;
	    }
	else
	    {
		// check for duplicate project names

		// compare with local projects
		IProject[] lprojects = workspace.getRoot().getProjects();
		
		for (int i = 0; i < lprojects.length; i++)
		    {	
			IProject lproject = lprojects[i];
			if (lproject.getName().equals(projectFieldContents))
			    {
				return false;
			    }
		    }
		
		// compare with remote projects
		RemoteProjectAdapter rmtAdapter = RemoteProjectAdapter.getInstance();
		if (rmtAdapter != null)
		    {
			IProject[] rprojects = rmtAdapter.getProjects();
			
			if (rprojects != null)
			    {
				for (int j = 0; j < rprojects.length; j++)
				    {	
					IProject rproject = rprojects[j];
					if (rproject.getName().equals(projectFieldContents))
					    {
						return false;
					    }
				    }
			    }
		    }
	    }
	
	if ((_projectHandle != null) && _projectHandle.exists())
	    {
		//	displayErrorMessage("Project already exists.");
		return false;
	    }
	
	return true;
    }
    /*=== dealing with choosing directory location dialog - for local or remote mapping ===*/

    protected void createSourceDirectoryGroup(Composite parent)
    {
	Label label = createBoldLabel(parent, _plugin.getLocalizedString("createProjectWizard.location"));
	
	Composite sourceContainerGroup = new Composite(parent,SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.numColumns = 3;
	sourceContainerGroup.setLayout(layout);
	sourceContainerGroup.setLayoutData(
					   new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
	
	// mapping selection group (default under the workbench, otherwise specify local dir
	Composite projectMappingSelectionGroup = new Composite(sourceContainerGroup,SWT.NONE);
	layout = new GridLayout();
	layout.numColumns = 3;
	projectMappingSelectionGroup.setLayout(layout);
	projectMappingSelectionGroup.setLayoutData(
						   new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
	
	// default mapping location radio button
	defaultMappingRadio = new Button(projectMappingSelectionGroup,SWT.RADIO);
	defaultMappingRadio.setText(_plugin.getLocalizedString("createProjectWizard.DefaultLocation"));
	defaultMappingRadio.addListener(SWT.Selection,this);
	GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
	data.horizontalSpan = 3;
	defaultMappingRadio.setLayoutData(data);
	
	// specify local directory radio button
	localMappingRadio = new Button(projectMappingSelectionGroup,SWT.RADIO);
	localMappingRadio.setText(_plugin.getLocalizedString("createProjectWizard.Local"));
	localMappingRadio.addListener(SWT.Selection,this);
	
	// source name entry field
	sourceNameField = new Combo(projectMappingSelectionGroup,SWT.BORDER);
	sourceNameField.addListener(SWT.Modify,this);
	sourceNameField.addListener(SWT.Selection,this);
	_history = _plugin.readProperty(CppProjectAttributes.LOCATION_LOCAL);
	int size = _history.size();
	for (int i = 0; i < size; i++)
	    {
		String item = (String)_history.get(size - i - 1);
		if (item != null)
		    {
			sourceNameField.add(item, i);
		    }
		
		if (i == 0)
		    {	
			sourceNameField.setText(item);
		    }
	    }
	data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
	data.widthHint = SIZING_TEXT_FIELD_WIDTH;
	sourceNameField.setLayoutData(data);
	
	// source browse button
	sourceBrowseButton = new Button(projectMappingSelectionGroup,SWT.PUSH);
	sourceBrowseButton.setText(_plugin.getLocalizedString("BrowseButton"));
	sourceBrowseButton.addListener(SWT.Selection,this);
	sourceBrowseButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
	
	/*****
	// specify remote URL location radio button
	remoteURLRadio = new Button(projectMappingSelectionGroup,SWT.RADIO);
	remoteURLRadio.setText(_plugin.getLocalizedString("createProjectWizard.RemoteURL"));
	remoteURLRadio.addListener(SWT.Selection,this);
	
	// source name entry field
	remoteURLNameField = new Combo(projectMappingSelectionGroup,SWT.BORDER);
	remoteURLNameField.addListener(SWT.Modify,this);
	remoteURLNameField.addListener(SWT.Selection,this);
	//	data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
	data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.GRAB_HORIZONTAL);
	data.horizontalSpan = 2;
	data.widthHint = SIZING_TEXT_FIELD_WIDTH;
	remoteURLNameField.setLayoutData(data);
	****/


	// remote Host selection group
	// specify remote Host location radio button
	remoteHostRadio = new Button(projectMappingSelectionGroup,SWT.RADIO);
	remoteHostRadio.setText(_plugin.getLocalizedString("createProjectWizard.Host"));
	remoteHostRadio.addListener(SWT.Selection,this);
	data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.GRAB_HORIZONTAL);
	data.horizontalSpan = 3;
	remoteHostRadio.setLayoutData(data);
	
	// Host name label
	Label hostNameLabel = new Label(projectMappingSelectionGroup,SWT.NONE);
	hostNameLabel.setText(_plugin.getLocalizedString("createProjectWizard.Name"));
	data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.GRAB_HORIZONTAL);
	data.horizontalIndent = 30;
	hostNameLabel.setLayoutData(data);
		
	// Host name entry field
 	remoteHostNameField = new Combo(projectMappingSelectionGroup,SWT.BORDER);
	remoteHostNameField.addListener(SWT.Modify,this);
	remoteHostNameField.addListener(SWT.Selection,this);
	data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.GRAB_HORIZONTAL);
	data.horizontalSpan = 2;
	data.widthHint = SIZING_TEXT_FIELD_WIDTH;
	remoteHostNameField.setLayoutData(data);

	
	// port number label
	Label portNumberLabel = new Label(projectMappingSelectionGroup,SWT.NONE);
	portNumberLabel.setText(_plugin.getLocalizedString("createProjectWizard.Port"));
	data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.GRAB_HORIZONTAL);
	data.horizontalIndent = 30;
	portNumberLabel.setLayoutData(data);
	
	// Port number entry field
	remoteHostPortNumberField = new Combo(projectMappingSelectionGroup,SWT.BORDER);
	remoteHostPortNumberField.addListener(SWT.Modify,this);
	remoteHostPortNumberField.addListener(SWT.Selection,this);
	//	data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
	data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
	data.horizontalSpan = 2;
	data.widthHint = SIZING_TEXT_FIELD_WIDTH;
	remoteHostPortNumberField.setLayoutData(data);


	// host directory name label
	Label directoryLabel = new Label(projectMappingSelectionGroup,SWT.NONE);
	directoryLabel.setText(_plugin.getLocalizedString("createProjectWizard.Directory"));
	data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.GRAB_HORIZONTAL);
	data.horizontalIndent = 30;
	directoryLabel.setLayoutData(data);


	// Directory name entry field
	remoteHostDirectoryField = new Combo(projectMappingSelectionGroup,SWT.BORDER);
	remoteHostDirectoryField.addListener(SWT.Modify,this);
	remoteHostDirectoryField.addListener(SWT.Selection,this);
	data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
	data.widthHint = SIZING_TEXT_FIELD_WIDTH;
	data.horizontalSpan = 1;
	remoteHostDirectoryField.setLayoutData(data);

	// browse button
	remoteHostBrowseButton = new Button(projectMappingSelectionGroup, SWT.PUSH);
	remoteHostBrowseButton.setText(_plugin.getLocalizedString("BrowseButton"));
	remoteHostBrowseButton.addListener(SWT.Selection,this);
	remoteHostBrowseButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));	

	remoteHostUseDaemon = new Button(projectMappingSelectionGroup, SWT.CHECK);
	remoteHostUseDaemon.setText(_plugin.getLocalizedString("createProjectWizard.UsingDaemon"));
	remoteHostUseDaemon.setSelection(true);
	GridData dD = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
	dD.horizontalIndent = 30;
	dD.horizontalSpan = 3;
	dD.widthHint = SIZING_TEXT_FIELD_WIDTH;
	remoteHostUseDaemon.setLayoutData(dD);

	// mounted directory name label
	Label mountedDirectoryLabel = new Label(projectMappingSelectionGroup,SWT.NONE);
	mountedDirectoryLabel.setText(_plugin.getLocalizedString("createProjectWizard.LocalMountPoint"));
	data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.GRAB_HORIZONTAL);
	data.horizontalIndent = 30;
	mountedDirectoryLabel.setLayoutData(data);

	// mounted Directory name entry field
	remoteHostMountField = new Combo(projectMappingSelectionGroup,SWT.BORDER);
	remoteHostMountField.addListener(SWT.Modify,this);
	remoteHostMountField.addListener(SWT.Selection,this);
	data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
	data.widthHint = SIZING_TEXT_FIELD_WIDTH;
	data.horizontalSpan = 1;
	remoteHostMountField.setLayoutData(data);


	// remote Host (socket connection)
	remoteHostRadio.setEnabled(true);
	remoteHostNameField.setEnabled(false);
	remoteHostPortNumberField.setEnabled(false);
	remoteHostDirectoryField.setEnabled(false);
	remoteHostMountField.setEnabled(false);
	remoteHostBrowseButton.setEnabled(false);
	remoteHostUseDaemon.setEnabled(false);

	getDefaults("DefaultRemoteHostName",      remoteHostNameField);
	getDefaults("DefaultRemoteHostPort",      remoteHostPortNumberField);
	getDefaults("DefaultRemoteHostDirectory", remoteHostDirectoryField);
	getDefaults("DefaultRemoteHostMount",     remoteHostMountField);
	

	// workbench
	defaultMappingRadio.setSelection(true);
	
	// local mapping
	localMappingRadio.setEnabled(true);
	sourceNameField.setEnabled(false);
	sourceBrowseButton.setEnabled(false);
	
	// remote URL
	resetSelection();
	
    }


    private void getDefaults(String attribute, Combo control)
    {
	ArrayList history = _plugin.readProperty(attribute);
	int size = history.size();
	for (int i = 0; i < size; i++)
	    {
		String item = (String)history.get(i);
		if (item != null)
		    {
			control.add(item, i);
		    }
		
		if (i == 0)
		    {	
			control.setText(item);
		    }
	    }
    }

    private void setDefaults(String attribute, Combo control)
    {
	ArrayList history = new ArrayList();
	history.add(control.getText());
	for (int i = 0; i < control.getItemCount(); i++)
	    {
		String item = (String)control.getItem(i);
		if (!history.contains(item))
		    history.add(item);
	    }	
	
	_plugin.writeProperty(attribute, history);
    }


    /**
     *	Answer the directory name specified as being the import source.
     *	Note that if it ends with a separator then the separator is first
     *	removed so that java treats it as a proper directory
     *
     *	@return java.lang.String
     */
    private String getSourceDirectoryName()
    {
	//System.out.println("CppWizardNewProjectCreationPage: getSourceDirectoryName ");
	IPath result = new Path(sourceNameField.getText().trim());
	
	if (result.getDevice() != null && result.segmentCount() == 0)	// something like "c:"
	    result = result.addTrailingSeparator();
	else
	    result = result.removeTrailingSeparator();
	
	return result.toOSString();
    }

    public String getRemoteMountPoint()
    {
	return selectedHostMount;
    }

    /**
     *	Reset the selected resources collection and update the ui appropriately
     */
    protected void resetSelection()
    {
	selectedResource = null;
	root = null;
    }

    /**
     *	Respond to the user selecting/deselecting items in the
     *	extensions list
     *
     *	@param selection ISelection
     */
    public void selectionChanged(SelectionChangedEvent event)
    {
	if (localMappingRadio.getSelection())
	    resetSelection();
    }

    /**
     *	Open an appropriate source browser so that the user can specify a source
     *	to import from
     */
    protected void handleSourceBrowseButtonPressed()
    {
	//System.out.println("CppWizardNewProjectCreationPage: handleSourceBrowseButtonPressed ");
	DirectoryDialog dialog = new DirectoryDialog(sourceNameField.getShell(),SWT.SAVE);
	dialog.setMessage(_plugin.getLocalizedString("createProjectWizard.source"));
	dialog.setFilterPath(getSourceDirectoryName());
	
	selectedDirectory = dialog.open();
	if (selectedDirectory != null) {
		if (!selectedDirectory.equals(getSourceDirectoryName())) {
		    resetSelection();
		    sourceNameField.setText(selectedDirectory);
		}
	}
    }	

    /**
     * Creates a horizontal spacer line that fills the width of its container.
     */
    protected void createSpacer(Composite parent)
    {
	//	Label spacer = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.SHADOW_OUT);
	Label spacer = new Label(parent, SWT.NONE);
	GridData data = new GridData();
	data.horizontalAlignment = GridData.FILL;
	data.verticalAlignment = GridData.BEGINNING;
	spacer.setLayoutData(data);
    }

    /**
     * Creates a new label with a bold font.
     */
    protected Label createBoldLabel(Composite parent, String text)
    {
	Label label = new Label(parent, SWT.NONE);
	label.setFont(JFaceResources.getBannerFont());
	label.setText(text);
	GridData data = new GridData();
	data.verticalAlignment = GridData.FILL;
	data.horizontalAlignment = GridData.FILL;
	label.setLayoutData(data);
	return label;
    }

    /**
     *	Answer a boolean indicating whether the specified source currently exists
     *	and is valid
     *
     *	@return boolean
     protected boolean ensureSourceIsValid()
     {
     if (new File(getSourceDirectoryName()).isDirectory())
     return true;

     displayErrorDialog("Source directory is not valid or has not been specified.");
     sourceNameField.setFocus();
     return false;
     }
    */
    }
