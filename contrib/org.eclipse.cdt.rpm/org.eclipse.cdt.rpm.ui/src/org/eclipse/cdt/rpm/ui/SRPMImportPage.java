/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
*/

/**
 * @author pmuldoon
 * @version 1.0
 *
 * RPM GUI import  page. Defines the page the is shown to the user when they choose
 * to export to and RPM. Defines the UI elements shown, and the basic validation (need to add to
 * this)
 */
package org.eclipse.cdt.rpm.ui;

import java.io.File;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;

import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;

import org.eclipse.ui.IWorkbench;

/**
 * @author pmuldoon
 *
 * SRPMImportPage. Called by SRPMImportwizard.  Class can not be subclassed
 * extends WizardPage and implements Listener (for events)
 *
 */
public class SRPMImportPage extends WizardPage implements Listener {
	
	private IWorkbench workbench;

	// Convienience SRPM import operation class 
	private SRPMImportOperation srpmImport;

	// GUI Control variables	
	private Combo sourceSRPM;
	private Button applyPatch;
	private Button runAutoConf;
	private Button buildSource;
	private List projectList;
	private IStructuredSelection selection;

	static private Vector srpmVector;
	
	/**
	 * @see java.lang.Object#Object()
	 *
	 * Constructor for SRPMImportPage class
	 * @param aWorkbench - Workbench
	 * @param selection - IStructuredSelection
	 */
	public SRPMImportPage(IWorkbench aWorkbench, IStructuredSelection currentSelection) {
		super(Messages.getString("SRPMImportPage.Import_SRPM"), //$NON-NLS-1$
			Messages.getString("SRPMImportPage.Select_project_to_import"), null); //$NON-NLS-1$ //$NON-NLS-2$

		this.workbench = aWorkbench;
		setPageComplete(false);
		setDescription(Messages.getString(
				"SRPMImportPage.Select_project_to_import")); //$NON-NLS-1$
		selection = currentSelection;
	}

	
	public String returnProject() {
		String projSelect;
		String[] projDetails = projectList.getSelection();

		if (projDetails.length > 0) {
			projSelect = projDetails[0];
		} else {
			projSelect = ""; //$NON-NLS-1$
		}

		return projSelect;
	}

	public void createControl(Composite parent) {
		// Set Page complete to false. Don't allow the user to execute wizard
		// until we have all the required data
		setPageComplete(false);

		// Create a generic composite to hold ui variable
		Composite composite = new Composite(parent, SWT.NULL);

		// Create a layout for the wizard page
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		setControl(composite);

		// Create contols on the page
		sourceRPMcombo(composite);
		createProjectBox(composite);
		createOptions(composite);
	}

	protected void sourceRPMcombo(Composite parent) {
		Group specGrid = new Group(parent, SWT.NONE);
		specGrid.setLayout(new GridLayout());
		specGrid.setText(Messages.getString("SRPMImportPage.SRPM_Name")); //$NON-NLS-1$
		specGrid.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL |
				GridData.HORIZONTAL_ALIGN_FILL));

		Composite sourceSpecComposite = new Composite(specGrid, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		sourceSpecComposite.setLayout(layout);
		sourceSpecComposite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL |
				GridData.GRAB_HORIZONTAL));

		sourceSRPM = new Combo(sourceSpecComposite, SWT.BORDER);
		sourceSRPM.setToolTipText(Messages.getString(
				"SRPMImportPage.toolTip_SRPM_Name")); //$NON-NLS-1$

		if (srpmVector == null)
			srpmVector = new Vector();
		for (int i = srpmVector.size(); i > 0; i--)
			sourceSRPM.add((String)(srpmVector.elementAt(i - 1)));
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		sourceSRPM.setLayoutData(gridData);
		sourceSRPM.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					handleEvent(null);
				}
			});

		Button srpmBrowse = new Button(sourceSpecComposite, SWT.PUSH);
		srpmBrowse.setToolTipText(Messages.getString(
				"SRPMImportPage.toolTip_Open_file_navigator")); //$NON-NLS-1$
		srpmBrowse.setText(Messages.getString("RPMExportPage.Browse")); //$NON-NLS-1$
		srpmBrowse.addListener(SWT.Selection,
			new Listener() {
				public void handleEvent(Event event) {
					FileDialog srpmBrowseDialog = new FileDialog(getContainer()
																				 .getShell(), SWT.OPEN);
					String selectedSRPM_name = srpmBrowseDialog.open();
					if (selectedSRPM_name != null)
					{
						File testSRPMfilename = new File(selectedSRPM_name);
						if (testSRPMfilename.isFile())
							sourceSRPM.setText(selectedSRPM_name);
					}
				}
			});
		srpmBrowse.addListener(SWT.FocusOut, this);
	}

	protected void createOptions(Composite parent) {
		//Create a group for the control and set up the layout.
		Group group = new Group(parent, SWT.NONE);
		group.setLayout(new GridLayout());
		group.setText(Messages.getString("RPMExportPage.Build_Options")); //$NON-NLS-1$
		group.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL |
				GridData.HORIZONTAL_ALIGN_FILL));

		// Create the build environment checkbox
		applyPatch = new Button(group, SWT.CHECK);
		applyPatch.setText(Messages.getString("SRPMImportPage.ApplyPatches")); //$NON-NLS-1$
		applyPatch.setSelection(true);
		applyPatch.setToolTipText(Messages.getString(
				"SRPMImportPage.toolTip_ApplyPatches")); //$NON-NLS-1$

		runAutoConf = new Button(group, SWT.CHECK);
		runAutoConf.setText(Messages.getString("SRPMImportPage.runAutoConf")); //$NON-NLS-1$
		runAutoConf.setSelection(true);
		runAutoConf.setToolTipText(Messages.getString(
				"SRPMImportPage.toolTip_runAutoConf")); //$NON-NLS-1$

	}

	/**
	 * Method createProjectBox.
	 * @param parent - parent widget
	 *
	 * Create a list box and populate it with
	 * the list of current projects in the workspace
	 */
	protected void createProjectBox(Composite parent) {
		// Creates a control that enumerates all the projects in the current 
		// Workspace and places them in a listbox. 
		// Need to check what to do if the user chooses to export an RPM
		// when there are no current projects in the workspace. Right now 
		// the other export wizard just open, with empty treeviews (?)
		// Declare an array of IProject;
		IProject[] internalProjectList;
		String Proj_Enum;

		//Get the current workspace root.
		final IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace()
															  .getRoot();

		//Create a group for the control and set up the layout. Even though it is a single control, 
		// we want to seperate it from the other widgets on the wizard dialog box
		Group group = new Group(parent, SWT.NONE);
		group.setLayout(new GridLayout());
		group.setText(Messages.getString("RPMExportPage.Select_a_project")); //$NON-NLS-1$
		group.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL |
				GridData.HORIZONTAL_ALIGN_FILL));

		// Creata a new SWT listbox. Only allow single selection of items	 
		// Set up the layout data
		projectList = new List(group, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
		projectList.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL |
				GridData.HORIZONTAL_ALIGN_FILL));
		projectList.setToolTipText(Messages.getString(
				"SRPMImportPage.toolTip_project_destination")); //$NON-NLS-1$

		// Set the height to 4 elements high
		GridData projectLayout = new GridData(GridData.GRAB_HORIZONTAL |
				GridData.HORIZONTAL_ALIGN_FILL);
		projectLayout.heightHint = projectList.getItemHeight() * 4;
		projectList.setLayoutData(projectLayout);

		// From the current Workspace root, get a list of all current projects
		// This should come back to us as an array of IProject.
		internalProjectList = workspaceRoot.getProjects();

		if (internalProjectList.length < 1) {
			projectList.add(Messages.getString(
					"RPMExportPage.No_c/c++_projects_found_2")); //$NON-NLS-1$
			return;
		}
		// Stuff the listbox with the text name of the projects 
		// using the getName() method
		// Find the first selected project in the workspace

		Iterator iter = selection.iterator();
		Object selectedObject= null;
		IProject selectedProject = null;
		boolean isSelection = false;
		if (iter.hasNext())
		{
			selectedObject = iter.next();
			if (selectedObject instanceof IResource)
			{
				selectedProject = ((IResource) selectedObject).getProject();
				isSelection = true;
			}
		}

		// Stuff the listbox with the text names of the projects 
		// using the getName() method and select the selected 
		// project if available
		
		for (int a = 0; a < internalProjectList.length; a++) 
		{

			try {
				IProjectNature cNature = internalProjectList[a].getNature(CProjectNature.C_NATURE_ID);
				if (cNature!=null)
					projectList.add(internalProjectList[a].getName());
					if (isSelection && internalProjectList[a].equals(selectedProject))
						projectList.setSelection(a);
				} catch (CoreException e) {
	
			}
		}
		
		if (!isSelection)
			projectList.setSelection(0);//if none is selected select first
										// project
		else
			projectList.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					handleEvent(null);
				}

				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});

		projectList.addListener(SWT.FocusOut, this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 */
	public void handleEvent(Event event) {
		setPageComplete(canFinish());
	}

	
	/**
	 * canFinish()
	 * 
	 * Hot validation. Called to determine whether Finish
	 * button can be set to true
	 * @return boolean. true if finish can be activated
	 */
	public boolean canFinish() {
		// Make sure project has been selected
		if (returnProject().equals("")) { //$NON-NLS-1$
			return false;
		}

		// Make sure an rpm name has been provided
		if (sourceSRPM.getText().equals("")) { //$NON-NLS-1$
			return false;
		}
  
		return true;
	}

	/**
	 * validateFinish()
	 * Second validation step. Validates minimum conditions
	 * for starting import
	 * @return boolean - true for go; false for problems
	 */
	public boolean validateFinish(){
		File srpmExists = new File(sourceSRPM.getText());
		if (!srpmExists.isFile()){
			setErrorMessage(Messages.getString("SRPMImportPage.Source_not_Valid"));
			return false;
		}
		if (sourceSRPM.getText().lastIndexOf(".src.rpm") == -1)
		{
			setErrorMessage(Messages.getString("SRPMImportPage.No_src_rpm_ext"));
			return false;
		}
		
		return true;
		
			
	}
	
	
	/**
	 * finish()
	 * 
	 * Perform finish after finish button is pressed
	 * @return boolean
	 * @throws CoreException
	 * 	 */
	public boolean finish() throws CoreException {
		IPath detailedProjectLocation = null;
		
		// Check second step validation
		if (!validateFinish())
			return false;
			
		// Get the handle to the current activate Workspace	    
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();

		// Get the current selected member from the list box (projectList)
		String[] selectedProject = projectList.getSelection();

		// As we only allow a single selection in the listbox, and the listbox always
		// comes with the first element selected, we can assume the first element
		// in the returned array is valid.
		IProject detailedProject = workspaceRoot.getProject(selectedProject[0]);
			
		// Add this SRPM to srpmList
		for (int i = 0; i < srpmVector.size(); i++)
		{	// There can only be one occurance 
			if (srpmVector.elementAt(i).equals(sourceSRPM.getText()))
			{
				srpmVector.remove(i);
				break;
			}
		}
		srpmVector.add((String)(sourceSRPM.getText()));
		
		// Create a new instance of rpmExportOperation build class
		try {
			srpmImport = new SRPMImportOperation(detailedProject,
					sourceSRPM.getText(), applyPatch.getSelection(),
					runAutoConf.getSelection());
			getContainer().run(true, true, srpmImport);
		} catch (Exception e) {
			setErrorMessage(e.toString());
			return false;
		}

		// Get the status of the operation
		IStatus srpmImportStatus = srpmImport.getStatus();

		// If the status does not come back clean, open error dialog
		if (!srpmImportStatus.isOK()) {
			ErrorDialog.openError(getContainer().getShell(),
				Messages.getString("RPMExportPage.Errors_importing_SRPM"), //$NON-NLS-1$
				null, // no special message
				srpmImportStatus);

			return false;
		}

		return true;
	}
}
