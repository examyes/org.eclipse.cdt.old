/*
 ** (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
*/

/**
 * @author pmuldoon
 * @version 1.0
 *
 * S/RPM  export  Defines the page that is shown to the user when they choose
 * to export to an S/RPM. Defines the UI elements shown, and the basic validation 
 * 
 */
package org.eclipse.cdt.rpm.ui;

import org.eclipse.cdt.rpm.core.*;

import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.MultiStatus;

import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;

import java.util.ArrayList;
import java.util.Iterator;


/**
 * @author pmuldoon
 *
 * RPMExportPage. Called by RPMExportWizard.  Class can not be subclassed
 *extends WizardPage for the RPM export wizard, implements Listener
 *
 */
public class RPMExportPage extends WizardPage implements Listener {
	private RPMExportOperation rpmExport;
	private RPMCore rpmExportCore;

	// Checkbox Buttons

	private Button generatePatch;
	private Button exportBinary;
	private Button exportSource;
	private Text rpmVersion;
	private Text rpmRelease;
	private IStructuredSelection selection;

	//Composite Project Listbox control	
	private List projectList;
	private Combo specFileCombo;
	private boolean patchNeeded = false;
	private Group patchNeedHintGrid;
	private static String path_to_specfile;
	static final String file_sep = System.getProperty("file.separator"); //$NON-NLS-1$
	static final String line_sep = System.getProperty("line.separator"); //$NON-NLS-1$
	
	public RPMExportPage(IStructuredSelection currentSelection) {
		super(Messages.getString("RPMExportPage.Export_SRPM"), //$NON-NLS-1$
				  Messages.getString("RPMExportPage.Export_SRPM_from_project"), null); //$NON-NLS-1$ //$NON-NLS-2$
		setDescription(Messages.getString("RPMExportPage.Select_project_export")); //$NON-NLS-1$
		setPageComplete(true);
		selection = currentSelection;
	}

	/**
	 * Method returnProject.
	 * @return String - returned selected project
	 *
	 * Returns a string from the selected project
	 * in the control list box.
	 */
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

	/**
	 * Method returnProjectPath
	 * @return returns the path of the currently selected project in 
	 * projectList
	 */
	public String returnProjectPath() {
		String[] projectSelection = projectList.getSelection();

		// As we only allow a single selection in the listbox, and the listbox always
		// comes with the first element selected, we can assume the first element
		// in the returned array is valid. Need to add a try/catch group to check for
		// null though
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject projectDetail = workspaceRoot.getProject(projectSelection[0]);

		return projectDetail.getLocation().toOSString();
	}

	/**
	 * Method setPatchModifier
	 * @param patchDelta - sets whether patch is required in a project
	 */
	private void setPatchModifier(boolean patchDelta) {
		patchNeeded = patchDelta;
	}

	/**
	 * Method patchNeeded
	 * @return boolean - is a patch needed?
	 */
	public boolean patchNeeded() {
		return patchNeeded;
	}

	/**
	 * Method returnSpecFiles
	 *
	 * @param projname - Name of the project to mine for spec files
	 * @return ArrayList - ArrayList of found spec files
	 *
	 * Method that mines a selected project for spec files
	 * Will return an ArrayList of returned spec files
	 *
	 */
	public ArrayList returnSpecFiles(String givenProjectName) {
		IProject[] internalProjects;
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		internalProjects = workspaceRoot.getProjects();

		String projectName;
		ArrayList specFileList = new ArrayList();

		for (int numberOfProjects = 0; numberOfProjects < internalProjects.length; numberOfProjects++) {
			projectName = internalProjects[numberOfProjects].getName();

			if (projectName.equals(givenProjectName)) {
				try {
					final IResource[] projectResourceList = internalProjects[numberOfProjects].members();

					for (int numberOfResources = 0; numberOfResources < projectResourceList.length; numberOfResources++) {
						String filenameExtension = projectResourceList[numberOfResources].getFileExtension();

						if (filenameExtension != null) {
							if (filenameExtension.equals("spec")) { //$NON-NLS-1$
								specFileList.add(projectResourceList[numberOfResources].getName().toString());
							}
						}
					}
				} catch (CoreException e) {
				}
			}
		}

		return specFileList;
	}

	/**
	 * Method hasMakefile.
	 * @param givenProjectName - project name to check
	 * @return boolean - true if project has makefile, false if not
	 *
	 *  Returns boolean on whether the currently selected project
	 *  has a Makefile that is visible and can be sourced
	 */
	public boolean hasMakefile(String givenProjectName) {
		IProject[] internalProjectList;
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		internalProjectList = workspaceRoot.getProjects();

		String projectName;
		IResource resourceList;

		for (int a = 0; a < internalProjectList.length; a++) {
			projectName = internalProjectList[a].getName();

			if (projectName.equals(givenProjectName)) {
				resourceList = internalProjectList[a].findMember(Messages.getString(
							"RPMExportPage.Makefile_pc")); //$NON-NLS-1$

				if (resourceList != null) {
					return true;
				}

				resourceList = internalProjectList[a].findMember(Messages.getString(
							"RPMExportPage.makefile_lc")); //$NON-NLS-1$

				if (resourceList != null) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(Composite)
	 *
	 * Parent control. Creates the listbox, Destination box, and options box
	 *
	 */
	public void createControl(Composite parent) {
		
		Composite composite = new Composite(parent, SWT.NULL);

		// Create a layout for the wizard page
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		setControl(composite);

		// Create contols on the page
		createProjectBox(composite);
		createSpecFileField(composite);
		setSpecFileComboData();
		createSpacer(composite);
		createExportType(composite);
		setPatchHint(composite);

		// Check if the project checksums are different
		// and therefore the project needs a path
		if (compareCheckSum(returnProjectPath()).equals("patch")) //$NON-NLS-1$
		 {
			patchNeedHintGrid.setVisible(true);
			setPatchModifier(true);
			setPageComplete(true);
		} else {
			setPatchModifier(false);
			setPageComplete(false);
		}
	}

	/**
	 * Method compareCheckSum
	 * 
	 * @param givenProject - project to check
	 * @return = match
	 * @return = legacy
	 * @return = patch
	 * @return = error
	 * 
	 * Returns one of the above status depending on project status
	 * 
	 */
	public String compareCheckSum(String givenProject) {
		try {
			// Create an instance of the RPMExport class
			rpmExportCore = new RPMCore();
			
			// Mine the .srpmInfo file if there is one
			if (rpmExportCore.firstSRPM(givenProject)) {
				return "legacy";
			}
			ArrayList srpmProjectInfo = rpmExportCore.getSRPMexportinfo(givenProject);

			// If the generated checksum, and the one in the srpmInfo file are the same
			// then the project has not changed since last import and does not need a patch
			if (rpmExportCore.generateChecksum(givenProject, 0) == Long.parseLong(
						(String) srpmProjectInfo.get(6))) {
				setPatchModifier(false);

				return "match"; //$NON-NLS-1$
						
				
				// If we cannot find an srpmInfo file, this is a legacy project
			} else if (Long.parseLong((String) srpmProjectInfo.get(6)) == -1) {
				setPatchModifier(false);

				return "legacy"; //$NON-NLS-1$
			// Otherwise they don't match and we need to patch
				} else {
					setPatchModifier(true);

					return "patch";  //$NON-NLS-1$
				}
		} catch (CoreException e) {
				return "error"; //$NON-NLS-1$
		}
	}

	/**
	 * Method checkPageComplete.
	 * @return boolean
	 *
	 * Check if export rpm name is valid
	 * Check if project has makefile
	 * Check a valid project has been selected
	 */
	public boolean checkPageComplete() {
		// Method invoked to check we have all the required data from the user
		// before we  allow the wizard to execute. However they can cancel at 
		// anytime.
		// Check the contents of the RPM destination filename
		// Needs work, least of all the variable name ;)
		// Check to ensure that the selected project has a makefile
		if (hasMakefile(returnProject()) == false) {
			setErrorMessage(Messages.getString(
					"RPMExportPage.project_does_not_have_Makefile")); //$NON-NLS-1$

			return false;
		}

		// Check to ensure the select project is actually a project        
		String projDetails = returnProject();

		if (projDetails.equals(Messages.getString(
						"RPMExportPage.No_c/c++_projects_found")) == true) { //$NON-NLS-1$
			setErrorMessage(Messages.getString(
					"RPMExportPage.Invald_project_specified")); //$NON-NLS-1$

			return false;
		}
   
		// If all tests pass, then we are okay to go.        
		return true;
	}

	/**
	 * Method finish. Performs the actual work.
	 * @return boolean
	 * @throws CoreException
	 * @throws CoreException
	 */
	public boolean finish(String[] givenPatchData) throws CoreException {
		// Selected project location
		String projectLocation = null;

		// Check that we can finish the export operation	
		if (checkPageComplete() == false) {
			return false;
		}

		// As we only allow a single selection in the listbox, and the listbox always
		// comes with the first element selected, we can assume the first element
		// in the returned array is valid. Need to add a try/catch group to check for
		// null though
		projectLocation = returnProjectPath();

		String exportType = null;

		// Calculate the export source type
		// -ba = build all
		// -bs = build source
		// -bb = build binary
		if ((exportSource.getSelection() == true) &&
				(exportBinary.getSelection() == true)) {
			exportType = "-ba"; //$NON-NLS-1$
		} else if (exportSource.getSelection() == true) {
			exportType = "-bs"; //$NON-NLS-1$
		} else if (exportBinary.getSelection() == true) {
			exportType = "-bb"; //$NON-NLS-1$
		}

		// Create a new instance of rpmExportOperation build class
		try {
			rpmExport = new RPMExportOperation(returnProject(),
					projectLocation, specFileCombo.getText(), "", givenPatchData[0], //$NON-NLS-1$
					givenPatchData[1] + line_sep + 
					givenPatchData[2] + line_sep + line_sep, rpmVersion.getText(),
					rpmRelease.getText(), exportType, patchNeeded);
		} catch (Exception e) {
			 setErrorMessage(e.toString());
			 return false;
		 }
		 
		 // Run the export
		  try {
				getContainer().run(true, true, rpmExport);
			} catch (InvocationTargetException e1) {
				setErrorMessage(e1.toString());
				return false;
			} catch (InterruptedException e1) {		
				setErrorMessage(e1.toString());
			}

		MultiStatus status = rpmExport.getStatus();

		if (!status.isOK()) {
			ErrorDialog.openError(getContainer().getShell(),
				Messages.getString(
					"RPMExportPage.Errors_encountered_importing_SRPM"), //$NON-NLS-1$
				null, // no special message
				status);

			return false;
		}

		// Need to return some meaninful status. Should only return true if the wizard completed
		// successfully.
		return true;
	}

	private void createExportType(Composite parent) { //Create a group for the control and set up the layout.

		Group group = new Group(parent, SWT.NONE);
		group.setLayout(new GridLayout());
		group.setText(Messages.getString("RPMExportPage.Composite_Export_Type")); //$NON-NLS-1$
		group.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL |
				GridData.HORIZONTAL_ALIGN_FILL));

		// Create the export binary checkbox
		exportBinary = new Button(group, SWT.CHECK);
		exportBinary.setText(Messages.getString("RPMExportPage.Export_Binary")); //$NON-NLS-1$
		exportBinary.setSelection(true);
		exportBinary.setToolTipText(Messages.getString(
				"RPMExportPage.toolTip_Export_Binary")); //$NON-NLS-1$
		exportBinary.addSelectionListener(new SelectionListener(){
				public void widgetSelected(SelectionEvent e) {
					handleEvent(null);
				}
				public void widgetDefaultSelected(SelectionEvent e) {
				}
		});
		
		// Create the export source checkbox
		exportSource = new Button(group, SWT.CHECK);
		exportSource.setText(Messages.getString("RPMExportPage.Export_Source")); //$NON-NLS-1$
		exportSource.setSelection(true);
		exportSource.setToolTipText(Messages.getString(
				"RPMExportPage.toolTip_Export_Source")); //$NON-NLS-1$
		exportSource.addSelectionListener(new SelectionListener(){
			public void widgetSelected(SelectionEvent e) {
				handleEvent(null);
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
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
			projectList.setSelection(0);//if none is selected select first project
		
		// Add a listener to set the name in the location box as 
		// it is selected in project box
		projectList.addListener(SWT.Selection,
			new Listener() {
				public void handleEvent(Event event) {
					rpmVersion.setText("");
					rpmRelease.setText("");
					setSpecFileComboData();
					if (compareCheckSum(returnProjectPath()).equals("patch")) //$NON-NLS-1$
					  {
						 setPatchModifier(true);
						 patchNeedHintGrid.setVisible(true);
						 setPageComplete(true);
					 } else {
						 patchNeedHintGrid.setVisible(false);
						 setPatchModifier(false);
						 setPageComplete(false);
					 }
				 }
			});
	}

	/**
	 * Method setSpecFileComboData
	 * 
	 * Populates the specFile Combo Box
	 * 
	 */
	private void setSpecFileComboData() {
		specFileCombo.clearSelection();
		specFileCombo.removeAll();

		final ArrayList specFileList = returnSpecFiles(returnProject());
		Iterator i = specFileList.iterator();

		while (i.hasNext())
			specFileCombo.add(i.next().toString());

		if (specFileList.size() > 0) {
			specFileCombo.setText(specFileList.get(0).toString());
			setVersionReleaseFields();
		}
	}

	/**
	 * Method createSpecFileField
	 * @param parent
	 *
	 * Creates the Spec file combo box
	 */
	protected void createSpecFileField(Composite parent) {
		

		Group specGrid = new Group(parent, SWT.NONE);
		specGrid.setLayout(new GridLayout());
		specGrid.setText(Messages.getString("RPMExportPage.SPEC_file")); //$NON-NLS-1$
		specGrid.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL |
				GridData.HORIZONTAL_ALIGN_FILL));

		Composite  composite = new Composite(specGrid, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL |
				GridData.GRAB_HORIZONTAL));

		specFileCombo = new Combo(composite, SWT.BORDER);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		
		specFileCombo.setLayoutData(gridData);
		specFileCombo.setToolTipText(Messages.getString(
				"RPMExportPage.toolTip_SpecFile")); //$NON-NLS-1$
		
		specFileCombo.addListener(SWT.Selection,
			new Listener() {
				public void handleEvent(Event event) {
					if (!specFileCombo.getText().equals("")) { //$NON-NLS-1$
						setVersionReleaseFields();
					}
				}
			});

		Button rpmBrowseButton = new Button(composite, SWT.PUSH);
		rpmBrowseButton.setToolTipText(Messages.getString(
				"RPMExportPage.toolTip_file_navigator")); //$NON-NLS-1$
		rpmBrowseButton.setText(Messages.getString("RPMExportPage.Browse")); //$NON-NLS-1$
		rpmBrowseButton.addListener(SWT.Selection,
			new Listener() {
				public void handleEvent(Event event) {
					FileDialog rpmFileDialog = new FileDialog(getContainer()
															 			.getShell(),	SWT.OPEN);
					IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace()
																	.getRoot();
					IProject detailProject = workspaceRoot.getProject(returnProject());
					IPath detailProjectLocation = detailProject.getLocation();
					rpmFileDialog.setFilterPath(detailProjectLocation.toString());

					String selectedSpecName = rpmFileDialog.open();

					if (selectedSpecName != null) {
						specFileCombo.setText(selectedSpecName);
						setVersionReleaseFields();
					}
				}
			});


		KeyListener trapPatch = new KeyListener() {
				public void keyReleased(KeyEvent e) {
					handleEvent(null);
				}

				public void keyPressed(KeyEvent e) {
					handleEvent(null);
				}
			};
            
		   ModifyListener trapChange = new ModifyListener(){
				public void modifyText(ModifyEvent e) {
					handleEvent(null);
				}
		   };	

		Composite versionReleaseComposite = new Composite(specGrid, SWT.NONE);
		GridLayout versionReleaseLayout = new GridLayout();
		versionReleaseLayout.numColumns = 5;
		versionReleaseComposite.setLayout(versionReleaseLayout);
		versionReleaseComposite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL |
				GridData.GRAB_HORIZONTAL));

		GridData lineGridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL |
				GridData.GRAB_HORIZONTAL);
		Label line = new Label(versionReleaseComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		lineGridData.widthHint = 5;
		line.setLayoutData(lineGridData);

		GridData versionGridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL |
				GridData.GRAB_HORIZONTAL);
		new Label(versionReleaseComposite, SWT.NONE).setText(Messages.getString(
				"RPMExportPage.Version")); //$NON-NLS-1$
		rpmVersion = new Text(versionReleaseComposite, SWT.BORDER);
		rpmVersion.setToolTipText(Messages.getString(
				"RPMExportPage.toolTip_Version")); //$NON-NLS-1$

		rpmVersion.setLayoutData(versionGridData);
		rpmVersion.addKeyListener(trapPatch);
		rpmVersion.addModifyListener(trapChange);

		GridData releaseGridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL |
				GridData.GRAB_HORIZONTAL);

		new Label(versionReleaseComposite, SWT.NONE).setText(Messages.getString(
				"RPMExportPage.Release")); //$NON-NLS-1$
		rpmRelease = new Text(versionReleaseComposite, SWT.BORDER);
		rpmRelease.setToolTipText(Messages.getString(
				"RPMExportPage.toolTip_Release")); //$NON-NLS-1$

		rpmRelease.setLayoutData(releaseGridData);
		rpmRelease.addKeyListener(trapPatch);
		rpmRelease.addModifyListener(trapChange);
	}

	/**
	 * Method setPatchHint
	 * 
	 * Draws a patch hint for the user
	 * @param parent - composite to draw on
	 * 
	 */
	private void setPatchHint(Composite parent) {
		Display display = null;
		patchNeedHintGrid = new Group(parent, SWT.NONE);
		patchNeedHintGrid.setVisible(false);
		patchNeedHintGrid.setLayout(new GridLayout());
		patchNeedHintGrid.setText(Messages.getString("RPMExportPage.groupPatchTitle")); //$NON-NLS-1$
		patchNeedHintGrid.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL |
				GridData.HORIZONTAL_ALIGN_FILL));

		Composite patchHintComposite = new Composite(patchNeedHintGrid, SWT.NONE);

		GridLayout patchHintLayout = new GridLayout();
		patchHintLayout.numColumns = 2;
		patchHintComposite.setLayout(patchHintLayout);
		patchHintComposite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL |
				GridData.GRAB_HORIZONTAL));
		final Image patchHintImage = new Image(display,getClass().getResourceAsStream("redhat-system_tools.png"));
		
		Canvas canvas = new Canvas(patchHintComposite,SWT.NO_REDRAW_RESIZE);
		canvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
			 e.gc.drawImage(patchHintImage,0,0);
			}
		}); 
		new Label(patchHintComposite, SWT.NONE).setText(Messages.getString(
				"RPMExportPage.needPatch_Project")  + //$NON-NLS-1$
			Messages.getString("RPMExportPage.needPatch_desc")); //$NON-NLS-1$
	}

	/**
	 * Method setVersionReleaseFields
	 * 
	 * Sets the Version and Release fields to the 
	 * values mined from the selected spec file
	 */
	private void setVersionReleaseFields() {
		String specFileLocation = ""; //$NON-NLS-1$

		String selectSpecFile = specFileCombo.getText();
		specFileLocation = returnSpecFilePath(selectSpecFile);
		path_to_specfile = specFileLocation;
		//Calculate spec file's physical location

		ArrayList specVersionReleaseTag = returnSpecVersionRelease(specFileLocation);

		// Mine the spec file's version and release information from
		// the provided spec file 

		if (specVersionReleaseTag.size() == 3) {
			rpmVersion.setText(specVersionReleaseTag.get(0).toString());
			rpmRelease.setText(specVersionReleaseTag.get(1).toString());
		}
	}

	private String returnSpecFilePath(String giveSpecPath)
	{		
		if (!giveSpecPath.startsWith(file_sep)) { 

			IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace()
																		.getRoot();
			IProject projectDetail = workspaceRoot.getProject(returnProject());
			String projectLocation = projectDetail.getLocation().toOSString();
			return  projectLocation + file_sep + specFileCombo.getText();
		} else {
			return specFileCombo.getText();
		}

	}
	
	/**
	 * Method returnSpecVersionRelease
	 * 
	 * Method to return the Spec File version
	 * and release
	 * @param giveSpecFileLocation
	 * @return
	 */
	private ArrayList returnSpecVersionRelease(String giveSpecFileLocation)
	{
		
		ArrayList specFileVersionTag;
		
		try {
			rpmExportCore = new RPMCore();
			specFileVersionTag = rpmExportCore.getNameVerRel(giveSpecFileLocation);
		} catch (FileNotFoundException e) {
			setErrorMessage(Messages.getString(
					"RPMExportPage.Cannont_find_file") + giveSpecFileLocation); //$NON-NLS-1$

			return null;
		} catch (CoreException e) {
			setErrorMessage(Messages.getString("RPMExportPage.Core_Exception") +
				e.getMessage()); //$NON-NLS-1$

			return null;
		}
	
	return specFileVersionTag;
	}
	
	/**
	 * Method createSpacer.
	 * @param parent - parent widget
	 *
	 * Create a generic filler control so that we can dump
	 * controls in a better layout
	 */
	protected void createSpacer(Composite parent) {
		Label spacer = new Label(parent, SWT.NONE);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.BEGINNING;
		spacer.setLayoutData(data);
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

		
		if (checkVersionReleaseFields() == false) {
			return false;
		}
        
        // Make sure either export binary/source is checked
		if (exportBinary.getSelection() == false && exportSource.getSelection() == false)
			return false;
        	
	   return true;
	}

	/**
	 * Method checkVersionReleaseFields
	 * 
	 * Check that Version and Release
	 * are not empty
	 * @return  boolean - true if valid
	 */
	private boolean checkVersionReleaseFields() {
		if (!rpmVersion.getText().equals("")) { //$NON-NLS-1$

			if (!rpmRelease.getText().equals("")) { //$NON-NLS-1$

				return true;
			}
		}

		return false;
	}

	/**
	 * Method canGoNext()
	 * 
	 * Method to enable Next button
	 * @return
	 */
	public boolean canGoNext() {
		// if a patch is needed, the next button should
		// be enabled
		if (patchNeeded()) {
			return true;
		} else {
			return false;
		}
	}

	public void handleEvent(Event e) {
		setPageComplete(canGoNext());
	}
	public static String getSpecFilePath() {
		return path_to_specfile;
	}
}
