package com.ibm.debug.internal.pdt.ui.dialogs;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/dialogs/LineBPWizardPage.java, eclipse, eclipse-dev, 20011128
// Version 1.17 (last modified 11/28/01 15:58:21)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.help.DialogPageContextComputer;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.ibm.debug.WorkspaceSourceLocator;
import com.ibm.debug.internal.pdt.ui.util.DialogField;
import com.ibm.debug.internal.pdt.ui.util.FileTreeContentProvider;
import com.ibm.debug.internal.pdt.ui.util.IDialogFieldListener;
import com.ibm.debug.internal.pdt.ui.util.IStringButtonAdapter;
import com.ibm.debug.internal.pdt.ui.util.SelectionButtonDialogField;
import com.ibm.debug.internal.pdt.ui.util.StringButtonDialogField;
import com.ibm.debug.internal.pdt.ui.util.StringDialogField;
import com.ibm.debug.internal.picl.IPICLDebugConstants;
import com.ibm.debug.internal.picl.PICLUtils;


/** The first page in the add line breakpoint wizard.*/
public class LineBPWizardPage extends BreakpointWizardPage implements IDialogFieldListener, IStringButtonAdapter, ISettingsWriter{


	private StringDialogField lineField;
	private StringButtonDialogField sourceField;
	private SelectionButtonDialogField deferButton;
	private	WorkspaceSourceLocator locator;
	private IResource selectedSource;

	private static final String PAGE_NAME= "LineBPWizard.page1";

	private static IDialogSettings section;
	private static final String LINE ="Line"; //profile key
	private static final String SOURCE = "Source";
	private static final String PROJECT = "Project";
	private static final String DEFER = "Defer";

	//supports deferred breakpoints
	private boolean supportsDeferred = false;

	/**
	 * Constructor for LineBPWizardPage
	 */
	protected LineBPWizardPage(String pageName, String title, ImageDescriptor titleImage, boolean supportsDeferred) {
		super(pageName, title, titleImage);

		setDescription(PICLUtils.getResourceString(PAGE_NAME+".description"));
		this.supportsDeferred = supportsDeferred;

	}

	/**
	 * Constructor for LineBPWizardPage
	 */
	protected LineBPWizardPage(String pageName, String title, ImageDescriptor titleImage, boolean supportsDeferred, IMarker breakpoint) {
		super(pageName, title, titleImage, breakpoint);

		setDescription(PICLUtils.getResourceString(PAGE_NAME+".description"));
		this.supportsDeferred = supportsDeferred;
	}


	protected void createRequiredFields()
	{
		if(supportsDeferred)
		{
			deferButton = new SelectionButtonDialogField(SWT.CHECK|SWT.LEFT);
			deferButton.setLabelText(PICLUtils.getResourceString(PAGE_NAME+".deferLabel"));
			deferButton.setDialogFieldListener(this);
		}

		projectField = new StringButtonDialogField(this);
		projectField.setLabelText(PICLUtils.getResourceString(PAGE_NAME+".projectLabel"));
		projectField.setDialogFieldListener(this);
		projectField.setButtonLabel(PICLUtils.getResourceString(PAGE_NAME+".browseLabel"));

		sourceField = new StringButtonDialogField(this);
		sourceField.setLabelText(PICLUtils.getResourceString(PAGE_NAME+".sourceLabel"));
		sourceField.setDialogFieldListener(this);
		sourceField.setButtonLabel(PICLUtils.getResourceString(PAGE_NAME+".browseLabel"));

		lineField = new StringDialogField();
		lineField.setLabelText(PICLUtils.getResourceString(PAGE_NAME+".lineLabel"));
		lineField.setDialogFieldListener(this);		

		sourceField.setEnabled(false);
	}


	/**
	 * @see WizardPage#createControl
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);

		int nColumns= 3;
		if(supportsDeferred)
			deferButton.doFillIntoGrid(composite, nColumns);
		projectField.doFillIntoGrid(composite, nColumns);
		//objectField.doFillIntoGrid(composite, nColumns);
		sourceField.doFillIntoGrid(composite, nColumns);
		lineField.doFillIntoGrid(composite, nColumns);

		String pageHelpID = PICLUtils.getHelpResourceString("LineBPWizardPage");
		//sets the help for any helpless widget on the page
		WorkbenchHelp.setHelp(getShell(), new DialogPageContextComputer(this, pageHelpID));
		//set widget specific help, with page help as backup
		WorkbenchHelp.setHelp(projectField.getTextControl(composite), new Object[] {PICLUtils.getHelpResourceString("LineBPWizardPage.projectField") , pageHelpID });
		WorkbenchHelp.setHelp(projectField.getChangeControl(composite), new Object[] {PICLUtils.getHelpResourceString("LineBPWizardPage.projectBrowse") , pageHelpID });
		WorkbenchHelp.setHelp(sourceField.getTextControl(composite), new Object[] {PICLUtils.getHelpResourceString("LineBPWizardPage.sourceField") , pageHelpID});
		WorkbenchHelp.setHelp(sourceField.getChangeControl(composite), new Object[] {PICLUtils.getHelpResourceString("LineBPWizardPage.sourceBrowse") , pageHelpID });
		WorkbenchHelp.setHelp(lineField.getTextControl(composite), new Object[] {PICLUtils.getHelpResourceString("LineBPWizardPage.lineField") , pageHelpID});
		if(supportsDeferred)
			WorkbenchHelp.setHelp(deferButton.getSelectionButton(composite), new Object[] {PICLUtils.getHelpResourceString("AddressBPWizardPage.deferCheckBox") , pageHelpID });

		restoreSettings();
	}

	public void dialogFieldChanged(DialogField field)
	{
		if(supportsDeferred && field == deferButton)
		{
			if(deferButton.isSelected())
				sourceField.setLabelText(PICLUtils.getResourceString(PAGE_NAME+".optionalSourceLabel"));
			else
				sourceField.setLabelText(PICLUtils.getResourceString(PAGE_NAME+".sourceLabel"));
		}

		if(!projectField.getText().equals("") && !editing)
			sourceField.setEnabled(true);
		else sourceField.setEnabled(false);

		if(lineField.getText().equals("") || projectField.getText().equals("") || sourceField.getText().equals(""))
			setPageComplete(false);
		else setPageComplete(true);
	}

	/** see <code> IStringButtonAdapter </code>
	 *  One of the browse buttons was pressed.
	 */
	public void changeControlPressed(DialogField field){
		if (field == sourceField)
		{
			selectedSource = chooseSource();
			sourceField.setText(selectedSource.getName());
		}
		else if (field == projectField)
		{
			selectedProject = chooseProject();
			if (selectedProject!=null && selectedProject.getName()!=null)
				projectField.setText(selectedProject.getName());
		}
	}

	public int getLineNumber()
	{
		try{
			return Integer.parseInt(lineField.getText());
		}
		catch(NumberFormatException e){
			return 0;  //fix me
		}
	}

	/**
	 * Returns the state of the deferred check box.
	 * For use by Wizard to set marker attributes.
	 */
	public Boolean isDeferred()
	{
		if(supportsDeferred)
			return new Boolean(deferButton.isSelected());
		return new Boolean(false);
	}

	public IResource getSourceResource()
	{
		locator = new WorkspaceSourceLocator();
		IProject project = getProjectResource();

		//project not specified
		if(projectField.getText().equals(""))
			return locator.findFile(sourceField.getText());

		//project not found as typed
		if(project == null)
			//specify error in status info object;
			return null;

		return locator.findFile(project,sourceField.getText());

	}


	/**
	 * Returns the name of the source specified in the text field.
	 * For use by Wizard to create the marker when getSourceResource returns null.
	 */
	public String getSourceName()
	{
		return sourceField.getText();
	}


	// ----starting for browse dialog



	// ------------- choose source container dialog
	private IResource chooseSource(){

		ILabelProvider labelProvider = new WorkbenchLabelProvider();
		FileTreeContentProvider contentProvider = new FileTreeContentProvider();
		ElementTreeSelectionDialog dialog= new ElementTreeSelectionDialog(getShell(),PICLUtils.getResourceString( "FileTreeSelectionDialog.title"),null, labelProvider, contentProvider, false, true);

		//dialog.setValidator(validator);
		//dialog.setSorter(new PackageViewerSorter());
		dialog.setMessage(PICLUtils.getResourceString("FileTreeSelectionDialog.description"));
		//dialog.addFilter(filter);

		//check if user typed project instead of selecting from browse dialog or edited after selection
		if(selectedProject==null || !selectedProject.getName().equals(projectField.getText()))
			selectedProject = getProjectResource();

		if(dialog.open(selectedProject) == dialog.OK){
			Object element= dialog.getPrimaryResult();
			if (element instanceof IResource) {
				return (IResource)element;
			}
		}
		//IJavaModel root= JavaCore.create(fWorkspaceRoot);
		/*if (dialog.open(root, initElement) == dialog.OK) {
			Object element= dialog.getPrimaryResult();
			if (element instanceof IJavaProject) {
				IJavaProject jproject= (IJavaProject)element;
				return jproject.getPackageFragmentRoot(jproject.getProject());
			} else if (element instanceof IPackageFragmentRoot) {
				return (IPackageFragmentRoot)element;
			}
			return null;
		}*/
		return null;
	}

	/**
	 * This method initializes the dialog fields with the values of the existing
	 * breakpoint that the user is editing.
	 */
	private void initUsingOldBreakpoint()
	{

		try{
			lineField.setText(((Integer)existingBP.getAttribute(IPICLDebugConstants.LINE_NUMBER)).toString() );
			lineField.setEnabled(false);
			projectField.setText(existingBP.getResource().getProject().getName()); //TODO
		 	projectField.setEnabled(false);
		 	if(existingBP.getAttribute(IPICLDebugConstants.SOURCE_FILE_NAME) != null)
				sourceField.setText((String)existingBP.getAttribute(IPICLDebugConstants.SOURCE_FILE_NAME));
			else if(existingBP.getResource() instanceof IFile)
				sourceField.setText(existingBP.getResource().getName());
			sourceField.setEnabled(false);
			if(!supportsDeferred)
				return;
	  		if(existingBP.getAttribute(IPICLDebugConstants.DEFERRED)!= null)
				deferButton.setSelection( ((Boolean)(existingBP.getAttribute(IPICLDebugConstants.DEFERRED))).booleanValue() );
			else deferButton.setSelection(false);
	  		deferButton.setEnabled(false);
	  	}catch(CoreException e){}
	}

	private void restoreSettings()
	{
		if(section == null)
		{
			IDialogSettings dialogSettings = getDialogSettings();
			if((section=dialogSettings.getSection(PAGE_NAME)) == null)
			{
				section=dialogSettings.addNewSection(PAGE_NAME);
			}
		}

		if(editing)
		{
			initUsingOldBreakpoint();
			return;
		}

  		String text = section.get(LINE);
  		if( text != null)
	  		lineField.setText(text);

	  	text = section.get(PROJECT);
	  	if( text != null)
	  		projectField.setText(text);
	  	else   //use name of current selected debug target's project
	  		projectField.setText(getNameOfCurrentSelectedProject());

	  	text = section.get(SOURCE);
	  	if(text != null)
	  		sourceField.setText(text);
	  	if(supportsDeferred)
		  	deferButton.setSelection(section.getBoolean(DEFER));

  	}

	/**
	 * @see ISettingsWriter#writeSettings
	 */
	public void writeSettings()
	{
		section.put(LINE, lineField.getText());
		section.put(PROJECT, projectField.getText());
		section.put(SOURCE, sourceField.getText());
		if(supportsDeferred)
			section.put(DEFER, deferButton.isSelected());
		else section.put(DEFER, section.get(DEFER));
	}


}

