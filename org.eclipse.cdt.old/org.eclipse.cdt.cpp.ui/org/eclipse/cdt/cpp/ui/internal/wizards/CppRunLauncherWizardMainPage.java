package org.eclipse.cdt.cpp.ui.internal.wizards;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.CppProjectAttributes;
import org.eclipse.cdt.cpp.ui.internal.CppPlugin;

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.hosts.dialogs.*;

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
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.io.*;
import java.net.URL;
import java.net.*;

import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.*;
import org.eclipse.ui.model.*;
import org.eclipse.ui.actions.*;


/**
 * Main page for the debug load wizard.
 */

public class CppRunLauncherWizardMainPage extends WizardPage implements Listener
{

    // initial value stores
    private String initialProgramFieldValue;
    private String initialParametersFieldValue;

    // widgets
    private Text                             programNameField;
    private Text                             programParametersField;
    protected Combo	                        sourceNameField;
    protected Combo		                     workingDirectoryField;
    protected Button                         workingDirectoryBrowseButton;

    // constants
    private static final int SIZING_TEXT_FIELD_WIDTH = 300;
    protected CppPlugin    _plugin = CppPlugin.getPlugin();
    protected ArrayList    _history;

    private  String        _programName;
    private  DataElement   _directory;
    private  String        _parameters;
    private  String        _workingDirectory;

    /**
     * Creates a <code>WizardNewProjectCreationPage</code> instance.
     *
     * @param pageId this page's internal name
     * @param desktop the current desktop
     */
    public CppRunLauncherWizardMainPage(String pageId, String currentSelectionName, DataElement directory) {
      super(pageId);
      _programName = currentSelectionName;
      _directory = directory;
    	setPageComplete(true);
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
   	
   	createProgramNameGroup(composite);
   	createProgramParametersGroup(composite);

   	createSpacer(composite);

   	createWorkingDirectoryGroup(composite);
   	
   	programParametersField.setFocus();
   	
   	setControl(composite);
    }

    /**
     * Field for entering the name of the program to execute.
     * We populate it with the user's current selection in the navigator,
     * and make it read only.  Essentially forcing user to preselect its executable
     * in the navigator before invoking the debug action.
     * @param parent a <code>Composite</code> that is to be used as the parent
     *     of this group's collection of visual components
     * @see org.eclipse.swt.widgets.Composite
     */
    protected final void createProgramNameGroup(Composite parent)
    {
   	// project specification group
   	Composite programGroup = new Composite(parent,SWT.NONE);
   	GridLayout layout = new GridLayout();
   	layout.numColumns = 2;
   	programGroup.setLayout(layout);
   	programGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_HORIZONTAL));
   	
   	// new program label
   	Label programLabel = new Label(programGroup,SWT.NONE);
   	programLabel.setText(_plugin.getLocalizedString("debugLauncherMain.ProgramName"));
   	
   	// new program name entry field
   	programNameField = new Text(programGroup, SWT.BORDER);
   	GridData data = new GridData(GridData.FILL_HORIZONTAL);
   	data.widthHint = SIZING_TEXT_FIELD_WIDTH;
      programNameField.setText(_programName);
   	programNameField.setLayoutData(data);
   	programNameField.setEnabled(false);
   	
    }

    /**
     * Field for entering the parameters of the program to execute.
     * @param parent a <code>Composite</code> that is to be used as the parent
     *     of this group's collection of visual components
     * @see org.eclipse.swt.widgets.Composite
     */
    protected final void createProgramParametersGroup(Composite parent)
    {
    	// project specification group
    	Composite parametersGroup = new Composite(parent,SWT.NONE);
    	GridLayout layout = new GridLayout();
    	layout.numColumns = 2;
    	parametersGroup.setLayout(layout);
    	parametersGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_HORIZONTAL));
    	
    	// new parameters label
    	Label parametersLabel = new Label(parametersGroup,SWT.NONE);
    	parametersLabel.setText(_plugin.getLocalizedString("debugLauncherMain.ProgramParameters"));
    	
    	// new parameters name entry field
    	programParametersField = new Text(parametersGroup, SWT.BORDER);
    	programParametersField.addListener(SWT.Modify, this);
    	GridData data = new GridData(GridData.FILL_HORIZONTAL);
    	data.widthHint = SIZING_TEXT_FIELD_WIDTH;
    	programParametersField.setLayoutData(data);
    	programParametersField.setEnabled(true);
     /*
   	// source name entry field
   	sourceNameField = new Combo(sourceContainerGroup,SWT.BORDER);
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

   	GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
   	data.widthHint = SIZING_TEXT_FIELD_WIDTH;
   	sourceNameField.setLayoutData(data);
    */


    	
    	if (initialParametersFieldValue != null)
    	    programParametersField.setText(initialParametersFieldValue);



    }

    /**
     * Field for entering the working directory from which to invoke the program to execute.
     * By default, we populate the field with the directory where the program is found, but the user
     * can change it by manualy entering the path, or using the "Browse" button to select it.
     *
     * @param parent a <code>Composite</code> that is to be used as the parent
     *     of this group's collection of visual components
     * @see org.eclipse.swt.widgets.Composite
     */
    protected final void createWorkingDirectoryGroup(Composite parent)
    {
   	// working directory specification group
   	Composite workingDirectoryGroup = new Composite(parent,SWT.NONE);
   	GridLayout layout = new GridLayout();
   	layout.numColumns = 3;
   	workingDirectoryGroup.setLayout(layout);
   	workingDirectoryGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_HORIZONTAL));
   	
   	// working directory name label
   	Label directoryLabel = new Label(workingDirectoryGroup,SWT.NONE);
   	directoryLabel.setText(_plugin.getLocalizedString("debugLauncherMain.WorkingDirectory"));

      GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
   	//data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.GRAB_HORIZONTAL);
   	directoryLabel.setLayoutData(data);


   	// Directory name entry field
   	workingDirectoryField = new Combo(workingDirectoryGroup,SWT.BORDER);
   	workingDirectoryField.addListener(SWT.Modify,this);
   	workingDirectoryField.addListener(SWT.Selection,this);
   	workingDirectoryField.setText(_directory.getSource());
   	data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
   	data.widthHint = SIZING_TEXT_FIELD_WIDTH;
   	data.horizontalSpan = 1;
   	workingDirectoryField.setLayoutData(data);

   	// browse button
   	workingDirectoryBrowseButton = new Button(workingDirectoryGroup, SWT.PUSH);
   	workingDirectoryBrowseButton.setText(_plugin.getLocalizedString("debugLauncherMain.Browse"));
   	workingDirectoryBrowseButton.addListener(SWT.Selection,this);
   	workingDirectoryBrowseButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));	

   	
   	
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
      _parameters = programParametersField.getText();
      _workingDirectory = workingDirectoryField.getText();

   	return true;
    }

    public String getParameters()
    {
		return _parameters;
    }

    public String getWorkingDirectory()
    {
		return _workingDirectory;
    }
/**
 * Returns the current contents of the project name field, or
 * its set initial value if it does not exist yet (which could
 * be <code>null</code>).
 *
 * @return the project name field's current value or anticipated initial value, or <code>null</code>
 */
public String getProgramFieldValue() {
	if (programNameField == null)
		return initialProgramFieldValue;
		
	return programNameField.getText();
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
	
	   if ((source == programNameField) || (source == programParametersField))
   	{
		//resetSelection();
   	}
      else if (source == workingDirectoryBrowseButton)
    	{
      	handleWorkingDirectoryBrowseButtonPressed();
    	}

 	   this.setPageComplete(this.validatePage());
    }

    /**
     *	Open an appropriate source browser so that the user can specify a source
     *	to import from
     */
    protected void handleWorkingDirectoryBrowseButtonPressed()
    {

		DataElement directory = _directory.getDataStore().getHostRoot().get(0).dereference();
		directory = directory.getParent();
		DataElementFileDialog dialog = new DataElementFileDialog("Select Directory", /*_directory*/directory, true);
		dialog.open();
		if (dialog.getReturnCode() == dialog.OK)
	   {
   		DataElement selected = dialog.getSelected();
	  		if (selected != null)
		    {
   	         workingDirectoryField.setText(selected.getSource());
  		    }
	    }
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
   	String workingDirectoryFieldContents = workingDirectoryField.getText();
	   if (workingDirectoryFieldContents.equals(""))
	       return false;
   	return validateProgramNameGroup();
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
    protected boolean validateProgramNameGroup()
    {
	   IWorkspace workspace = _plugin.getPluginWorkspace();
   	String programFieldContents = programNameField.getText();
	   if (programFieldContents.equals(""))
	       return false;
    	return true;
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
     *	Reset the selected resources collection and update the ui appropriately
     */
//    protected void resetSelection()
//    {
//	selectedResource = null;
//    }

    /**
     *	Respond to the user selecting/deselecting items in the
     *	extensions list
     *
     *	@param selection ISelection
     */
    public void selectionChanged(SelectionChangedEvent event)
    {
	    //resetSelection();
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
   /*
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
   */

    }
