package com.ibm.debug.internal.pdt.ui.dialogs;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/dialogs/EntryBPWizardPage.java, eclipse, eclipse-dev, 20011128
// Version 1.18 (last modified 11/28/01 15:58:19)
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
import com.ibm.debug.internal.picl.IPICLDebugConstants;
import com.ibm.debug.internal.picl.PICLUtils;



/** The first page in the add entry breakpoint wizard.*/
public class EntryBPWizardPage extends BreakpointWizardPage implements IDialogFieldListener, IStringButtonAdapter, ISettingsWriter {

	private StringButtonDialogField  entryField;
	private StringButtonDialogField  sourceField;
	private SelectionButtonDialogField caseButton;
	private SelectionButtonDialogField deferButton;
	private	WorkspaceSourceLocator locator;
	private IResource selectedSource;
	private String selectedEntry;  //? type

	private static final String PAGE_NAME= "EntryBPWizard.page1";
	private static IDialogSettings section;
	private static final String ENTRY ="Entry"; //profile key
	private static final String SOURCE = "Source";
	private static final String PROJECT = "Project";
	private static final String CASE = "Case";
	private static final String DEFER = "Defer";

    //supports deferred breakpoints
	private boolean supportsDeferred = false;

	/**
	 * Constructor for EntryBPWizardPage
	 */
	protected EntryBPWizardPage(String pageName, String title, ImageDescriptor titleImage, boolean supportsDeferred) {
		super(pageName, title, titleImage);
		setDescription(PICLUtils.getResourceString(PAGE_NAME+".description"));
		this.supportsDeferred = supportsDeferred;
	}

	/**
	 * Constructor for EntryBPWizardPage
	 */
	protected EntryBPWizardPage(String pageName, String title, ImageDescriptor titleImage, boolean supportsDeferred, IMarker breakpoint) {
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
		entryField = new StringButtonDialogField(this);
		entryField.setDialogFieldListener(this);
		entryField.setLabelText(PICLUtils.getResourceString(PAGE_NAME+".entryLabel"));
		entryField.setButtonLabel(PICLUtils.getResourceString(PAGE_NAME+".browseLabel"));
		entryField.setEnabled(false);
		entryField.enableButton(false);  //disable until IDebuggableProject implemented

		projectField = new StringButtonDialogField(this);
		projectField.setDialogFieldListener(this);
		projectField.setLabelText(PICLUtils.getResourceString(PAGE_NAME+".projectLabel"));
		projectField.setButtonLabel(PICLUtils.getResourceString(PAGE_NAME+".browseLabel"));

		sourceField = new StringButtonDialogField(this);
		sourceField.setLabelText(PICLUtils.getResourceString(PAGE_NAME+".sourceLabel"));
		sourceField.setDialogFieldListener(this);
		sourceField.setButtonLabel(PICLUtils.getResourceString(PAGE_NAME+".browseLabel"));
		sourceField.setEnabled(false);

		caseButton = new SelectionButtonDialogField(SWT.CHECK|SWT.LEFT);
		caseButton.setLabelText(PICLUtils.getResourceString(PAGE_NAME+".caseLabel"));
		caseButton.setSelection(false);
		caseButton.setDialogFieldListener(this);
		caseButton.setEnabled(false);

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
		sourceField.doFillIntoGrid(composite, nColumns);
		entryField.doFillIntoGrid(composite, nColumns);
		DialogField.createEmptySpace(composite,1);
		caseButton.doFillIntoGrid(composite, 2);

		String pageHelpID = PICLUtils.getHelpResourceString("EntryBPWizardPage");
		//sets the help for any helpless widget on the page
		WorkbenchHelp.setHelp(getShell(), new DialogPageContextComputer(this, pageHelpID));
		//set widget specific help, with page help as backup
		WorkbenchHelp.setHelp(projectField.getTextControl(composite), new Object[] {PICLUtils.getHelpResourceString("EntryBPWizardPage.projectField") , pageHelpID });
		WorkbenchHelp.setHelp(projectField.getChangeControl(composite), new Object[] {PICLUtils.getHelpResourceString("EntryBPWizardPage.projectBrowse") , pageHelpID });
		WorkbenchHelp.setHelp(entryField.getChangeControl(composite), new Object[] {PICLUtils.getHelpResourceString("EntryBPWizardPage.entryField") , pageHelpID });
		WorkbenchHelp.setHelp(entryField.getChangeControl(composite), new Object[] {PICLUtils.getHelpResourceString("EntryBPWizardPage.entryBrowse") , pageHelpID });
		WorkbenchHelp.setHelp(sourceField.getTextControl(composite), new Object[] {PICLUtils.getHelpResourceString("EntryBPWizardPage.sourceField") , pageHelpID});
		WorkbenchHelp.setHelp(sourceField.getTextControl(composite), new Object[] {PICLUtils.getHelpResourceString("EntryBPWizardPage.sourceBrowse") , pageHelpID});
		if(supportsDeferred)
			WorkbenchHelp.setHelp(deferButton.getSelectionButton(composite), new Object[] {PICLUtils.getHelpResourceString("EntryBPWizardPage.deferCheckBox") , pageHelpID });
		WorkbenchHelp.setHelp(caseButton.getSelectionButton(composite), new Object[] {PICLUtils.getHelpResourceString("EntryBPWizardPage.caseCheckBox") , pageHelpID });

		restoreSettings();
	}

	public void dialogFieldChanged(DialogField field)
	{
		if(!projectField.getText().equals("") && !editing)
			sourceField.setEnabled(true);
		else sourceField.setEnabled(false);

		if(!sourceField.getText().equals("") && !projectField.getText().equals(""))
		{
			entryField.setEnabled(true);
			entryField.enableButton(false);  //disable until IDebuggableProject implemented
			caseButton.setEnabled(true);
		}
		else
		{
			entryField.setEnabled(false);
			caseButton.setEnabled(false);
		}

		if(entryField.getText().equals("") || projectField.getText().equals("") || sourceField.getText().equals(""))
			setPageComplete(false);
		else setPageComplete(true);
	}

	/** see <code> IStringButtonAdapter </code>
	 *  One of the browse buttons was pressed.
	 */
	public void changeControlPressed(DialogField field)
	{
		if (field == sourceField)
		{
			selectedSource = chooseSource();
			if (selectedSource!=null && selectedSource.getName()!=null)
				sourceField.setText(selectedSource.getName());
		}
		else if (field == projectField)
		{
			selectedProject = chooseProject();
			if (selectedProject!=null && selectedProject.getName()!=null)
				projectField.setText(selectedProject.getName());
		}
		else if (field == entryField)
		{
			selectedEntry = chooseEntry();
			if (selectedEntry!=null)
				entryField.setText(selectedEntry);
		}
	}

	private String chooseEntry()
	{
		//check if user typed source instead of selecting from browse dialog or edited after selection
		if(selectedSource==null || !selectedSource.getName().equals(sourceField.getText()))
			selectedSource = getSourceResource();

		ILabelProvider labelProvider = new WorkbenchLabelProvider();
		FileTreeContentProvider contentProvider = new FileTreeContentProvider();
		ElementTreeSelectionDialog dialog= new ElementTreeSelectionDialog(getShell(),
			PICLUtils.getResourceString( "FileTreeSelectionDialog.title"),null,
			labelProvider, contentProvider, false, true);

		//dialog.setValidator(validator);
		//dialog.setSorter(new PackageViewerSorter());
		dialog.setMessage(PICLUtils.getResourceString("FileTreeSelectionDialog.description"));
		//dialog.addFilter(filter);

		if (selectedSource instanceof IFile)
		{
		/*	ArrayList functions = ModelInterface.getFunctions((IFile)selectedSource);
			for (int i = 0; i < functions.size(); i++)
			{
	 		   	String function = (String)functions.get(i);
	   			System.out.println(function);
			}*/

		/*	if(dialog.open(selectedSource) == dialog.OK){
				Object element= dialog.getPrimaryResult();
				if (element instanceof IResource) {
					return (IResource)element;
				}
			}*/
		}
			return null;
	}

	/**
	 * Returns function name specified in the text field.
	 * For use by Wizard to set marker attributes.
	 */
	public String getEntryName()
	{
			return entryField.getText();
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

	/**
	 * Returns the state of the case sensitive check box.
	 * For use by Wizard to set marker attributes.
	 */
	public Boolean isCaseSensitive()
	{
		return new Boolean(caseButton.isSelected());
	}

	/**
	 * Returns the IResource that corresponds to the source specified in the text field.
	 * For use by Wizard to create the marker.
	 */
	public IResource getSourceResource()
	{
		locator = new WorkspaceSourceLocator();

		if(projectField.getText() == null)
			return locator.findFile(sourceField.getText());

		return locator.findFile(getProjectResource(),sourceField.getText() );

	}

	/**
	 * Returns the name of the source specified in the text field.
	 * For use by Wizard to create the marker when getSourceResource returns null.
	 */
	public String getSourceName()
	{
		return sourceField.getText();
	}


	// ------------- choose source container dialog
	private IResource chooseSource()
	{
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
			if(existingBP.getAttribute(IPICLDebugConstants.FUNCTION_NAME) != null)
				entryField.setText((String)existingBP.getAttribute(IPICLDebugConstants.FUNCTION_NAME) );

			if(existingBP.getResource().getProject()!=null)
				projectField.setText(existingBP.getResource().getProject().getName());
		 	projectField.setEnabled(false);

		 	if(existingBP.getAttribute(IPICLDebugConstants.SOURCE_FILE_NAME)!=null)
				sourceField.setText((String)existingBP.getAttribute(IPICLDebugConstants.SOURCE_FILE_NAME));
			sourceField.setEnabled(false);

			if(existingBP.getAttribute(IPICLDebugConstants.CASESENSITIVE)!=null)
				caseButton.setSelection(((Boolean)existingBP.getAttribute(IPICLDebugConstants.CASESENSITIVE)).booleanValue());
			else caseButton.setSelection(false);

			if(!supportsDeferred)
				return;

			//Will be null if not set through dialog originally
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

  		String text = section.get(ENTRY);
  		if( text != null)
	  		entryField.setText(text);

	  	text = section.get(PROJECT);
	  	if( text != null)
	  		projectField.setText(text);
	  	else   //use name of current selected debug target's project
	  		projectField.setText(getNameOfCurrentSelectedProject());

	  	text = section.get(SOURCE);
	  	if( text != null)
	  		sourceField.setText(text);

	  	caseButton.setSelection(section.getBoolean(CASE));
	  	if(supportsDeferred)
	  		deferButton.setSelection(section.getBoolean(DEFER));
  	}

  	/**
	 * @see ISettingsWriter#writeSettings
	 */
	public void writeSettings()
	{
		section.put(ENTRY, entryField.getText());
		section.put(PROJECT, projectField.getText());
		section.put(SOURCE, sourceField.getText());
		section.put(CASE, caseButton.isSelected());
		if(supportsDeferred)
			section.put(DEFER, deferButton.isSelected());
		else section.put(DEFER, section.get(DEFER));
	}


}

